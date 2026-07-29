package xyz.nachaos.memosyou.data.repository

import com.skydoves.sandwich.ApiResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import xyz.nachaos.memosyou.data.api.MemosV0Api
import xyz.nachaos.memosyou.data.api.MemosRowStatus
import xyz.nachaos.memosyou.data.api.MemosV0Memo
import xyz.nachaos.memosyou.data.api.MemosV0Resource
import xyz.nachaos.memosyou.data.api.MemosVisibility
import xyz.nachaos.memosyou.data.model.Account
import xyz.nachaos.memosyou.data.model.Memo
import xyz.nachaos.memosyou.data.model.MemoVisibility as AppMemoVisibility
import xyz.nachaos.memosyou.data.model.Resource
import xyz.nachaos.memosyou.data.model.User
import java.time.Instant

class MemosV0RepositoryTest {

    private val mockApi = mockk<MemosV0Api>()
    private val account = Account.MemosV0(
        xyz.nachaos.memosyou.data.model.MemosAccount(
            host = "https://memos.example.com",
            remoteIdentifier = "user-123"
        )
    )
    private val repository = MemosV0Repository(mockApi, account)

    @Test
    fun `convertMemo maps all fields correctly`() = runTest {
        val apiMemo = MemosV0Memo(
            id = 123,
            createdTs = 1700000000,
            creatorId = 100,
            creatorName = "Test User",
            content = "Test content",
            pinned = true,
            rowStatus = MemosRowStatus.NORMAL,
            updatedTs = 1700000100,
            visibility = MemosVisibility.PRIVATE,
            resourceList = listOf(
                MemosV0Resource(
                    id = 1,
                    createdTs = 1700000000,
                    creatorId = 100,
                    filename = "image.png",
                    size = 1024,
                    type = "image/png",
                    updatedTs = 1700000000,
                    externalLink = null,
                    publicId = null,
                    name = null,
                    uid = null
                )
            )
        )

        val memo = repository.convertMemo(apiMemo)

        assertEquals("123", memo.remoteId)
        assertEquals("Test content", memo.content)
        assertEquals(Instant.ofEpochSecond(1700000000), memo.date)
        assertEquals(true, memo.pinned)
        assertEquals(AppMemoVisibility.PRIVATE, memo.visibility)
        assertEquals(1, memo.resources.size)
        assertEquals("1", memo.resources[0].remoteId)
        assertEquals("image.png", memo.resources[0].filename)
        assertEquals("https://memos.example.com/o/r/1/image.png", memo.resources[0].uri)
        assertEquals(false, memo.archived)
        assertEquals(Instant.ofEpochSecond(1700000100), memo.updatedAt)
        assertEquals(User("100", "Test User", Instant.ofEpochSecond(1700000000)), memo.creator)
    }

    @Test
    fun `convertMemo handles null resourceList`() = runTest {
        val apiMemo = MemosV0Memo(
            id = 123,
            createdTs = 1700000000,
            creatorId = 100,
            creatorName = null,
            content = "Test",
            pinned = false,
            rowStatus = MemosRowStatus.ARCHIVED,
            updatedTs = 1700000100,
            visibility = MemosVisibility.PUBLIC,
            resourceList = null
        )

        val memo = repository.convertMemo(apiMemo)

        assertEquals("123", memo.remoteId)
        assertEquals("Test", memo.content)
        assertEquals(true, memo.archived)
        assertEquals(AppMemoVisibility.PUBLIC, memo.visibility)
        assertEquals(emptyList<Resource>(), memo.resources)
        assertEquals(null, memo.creator)
    }

    @Test
    fun `convertResource maps fields correctly`() = runTest {
        val apiResource = MemosV0Resource(
            id = 42,
            createdTs = 1700000000,
            creatorId = 100,
            filename = "doc.pdf",
            size = 2048,
            type = "application/pdf",
            updatedTs = 1700000000,
            externalLink = "https://external.example.com/doc.pdf",
            publicId = null,
            name = null,
            uid = null
        )

        val resource = repository.convertResource(apiResource)

        assertEquals("42", resource.remoteId)
        assertEquals(Instant.ofEpochSecond(1700000000), resource.date)
        assertEquals("doc.pdf", resource.filename)
        assertEquals("https://external.example.com/doc.pdf", resource.uri)
        assertEquals("application/pdf", resource.mimeType)
    }

    @Test
    fun `listMemos returns converted memos`() = runTest {
        coEvery {
            mockApi.listMemo(rowStatus = MemosRowStatus.NORMAL)
        } returns ApiResponse.Success(
            listOf(
                MemosV0Memo(
                    id = 1,
                    createdTs = 1700000000,
                    creatorId = 100,
                    content = "Memo 1",
                    pinned = false,
                    rowStatus = MemosRowStatus.NORMAL,
                    updatedTs = 1700000000,
                    visibility = MemosVisibility.PRIVATE
                )
            )
        )

        val result = repository.listMemos()

        assertTrue(result is ApiResponse.Success)
        val memos = (result as ApiResponse.Success).data
        assertEquals(1, memos.size)
        assertEquals("Memo 1", memos[0].content)
    }

    @Test
    fun `listArchivedMemos uses ARCHIVED rowStatus`() = runTest {
        coEvery {
            mockApi.listMemo(rowStatus = MemosRowStatus.ARCHIVED)
        } returns ApiResponse.Success(emptyList())

        val result = repository.listArchivedMemos()

        assertTrue(result is ApiResponse.Success)
        coVerify { mockApi.listMemo(rowStatus = MemosRowStatus.ARCHIVED) }
    }

    @Test
    fun `createMemo returns created memo and ignores tag errors`() = runTest {
        val apiMemo = MemosV0Memo(
            id = 1,
            createdTs = 1700000000,
            creatorId = 100,
            content = "New memo",
            pinned = false,
            rowStatus = MemosRowStatus.NORMAL,
            updatedTs = 1700000000,
            visibility = MemosVisibility.PRIVATE
        )
        coEvery { mockApi.createMemo(any()) } returns ApiResponse.Success(apiMemo)
        coEvery { mockApi.updateTag(any()) } returns ApiResponse.Exception(Exception("Tag error"))

        val result = repository.createMemo(
            content = "New memo",
            visibility = AppMemoVisibility.PRIVATE,
            resourceRemoteIds = emptyList(),
            tags = listOf("tag1"),
            createdAt = null
        )

        assertTrue(result is ApiResponse.Success)
        val memo = (result as ApiResponse.Success).data
        assertEquals("New memo", memo.content)
        coVerify { mockApi.updateTag(any()) }
    }

    @Test
    fun `updateMemo with pinned only calls organizer API`() = runTest {
        val apiMemo = MemosV0Memo(
            id = 1,
            createdTs = 1700000000,
            creatorId = 100,
            content = "Memo",
            pinned = true,
            rowStatus = MemosRowStatus.NORMAL,
            updatedTs = 1700000000,
            visibility = MemosVisibility.PRIVATE
        )
        coEvery {
            mockApi.updateMemoOrganizer(1, any())
        } returns ApiResponse.Success(apiMemo)

        val result = repository.updateMemo(
            remoteId = "1",
            content = null,
            resourceRemoteIds = null,
            visibility = null,
            tags = null,
            pinned = true,
            archived = null
        )

        assertTrue(result is ApiResponse.Success)
        coVerify { mockApi.updateMemoOrganizer(eq(1), any()) }
        coVerify(exactly = 0) { mockApi.patchMemo(any(), any()) }
    }

    @Test
    fun `updateMemo with content calls patch API`() = runTest {
        val apiMemo = MemosV0Memo(
            id = 1,
            createdTs = 1700000000,
            creatorId = 100,
            content = "Updated",
            pinned = false,
            rowStatus = MemosRowStatus.NORMAL,
            updatedTs = 1700000000,
            visibility = MemosVisibility.PUBLIC
        )
        coEvery {
            mockApi.patchMemo(eq(1), any())
        } returns ApiResponse.Success(apiMemo)

        val result = repository.updateMemo(
            remoteId = "1",
            content = "Updated",
            resourceRemoteIds = null,
            visibility = AppMemoVisibility.PUBLIC,
            tags = null,
            pinned = null,
            archived = null
        )

        assertTrue(result is ApiResponse.Success)
        coVerify { mockApi.patchMemo(eq(1), any()) }
    }

    @Test
    fun `updateMemo with no changes returns invalid parameter`() = runTest {
        val result = repository.updateMemo(
            remoteId = "1",
            content = null,
            resourceRemoteIds = null,
            visibility = null,
            tags = null,
            pinned = null,
            archived = null
        )

        assertTrue(result is ApiResponse.Exception)
        assertEquals(MoeMemosException.invalidParameter, (result as ApiResponse.Exception).run { this })
    }

    @Test
    fun `deleteMemo calls API with long id`() = runTest {
        coEvery { mockApi.deleteMemo(42L) } returns ApiResponse.Success(Unit)

        val result = repository.deleteMemo("42")

        assertTrue(result is ApiResponse.Success)
        coVerify { mockApi.deleteMemo(42L) }
    }

    @Test
    fun `listResources converts resources`() = runTest {
        coEvery {
            mockApi.getResources()
        } returns ApiResponse.Success(
            listOf(
                MemosV0Resource(
                    id = 1,
                    createdTs = 1700000000,
                    creatorId = 100,
                    filename = "file.txt",
                    size = 100,
                    type = "text/plain",
                    updatedTs = 1700000000,
                    externalLink = null,
                    publicId = null,
                    name = null,
                    uid = null
                )
            )
        )

        val result = repository.listResources()

        assertTrue(result is ApiResponse.Success)
        val resources = (result as ApiResponse.Success).data
        assertEquals(1, resources.size)
        assertEquals("1", resources[0].remoteId)
    }

    @Test
    fun `listWorkspaceMemos paginates correctly`() = runTest {
        val memo1 = MemosV0Memo(
            id = 1,
            createdTs = 1700000000,
            creatorId = 100,
            content = "Memo 1",
            pinned = false,
            rowStatus = MemosRowStatus.NORMAL,
            updatedTs = 1700000000,
            visibility = MemosVisibility.PRIVATE
        )
        val memo2 = MemosV0Memo(
            id = 2,
            createdTs = 1700000001,
            creatorId = 200,
            content = "Memo 2",
            pinned = false,
            rowStatus = MemosRowStatus.NORMAL,
            updatedTs = 1700000001,
            visibility = MemosVisibility.PRIVATE
        )
        coEvery {
            mockApi.listAllMemo(limit = 10, offset = "0")
        } returns ApiResponse.Success(listOf(memo1, memo2))

        val result = repository.listWorkspaceMemos(pageSize = 10, pageToken = null)

        assertTrue(result is ApiResponse.Success)
        val (memos, nextToken) = (result as ApiResponse.Success).data
        assertEquals(2, memos.size)
        assertEquals("10", nextToken) // next page token is current offset + pageSize
    }

    @Test
    fun `listWorkspaceMemos returns null nextToken when fewer results than pageSize`() = runTest {
        val memo1 = MemosV0Memo(
            id = 1,
            createdTs = 1700000000,
            creatorId = 100,
            content = "Memo 1",
            pinned = false,
            rowStatus = MemosRowStatus.NORMAL,
            updatedTs = 1700000000,
            visibility = MemosVisibility.PRIVATE
        )
        coEvery {
            mockApi.listAllMemo(limit = 10, offset = "0")
        } returns ApiResponse.Success(listOf(memo1))

        val result = repository.listWorkspaceMemos(pageSize = 10, pageToken = null)

        assertTrue(result is ApiResponse.Success)
        val (memos, nextToken) = (result as ApiResponse.Success).data
        assertEquals(1, memos.size)
        assertEquals(null, nextToken)
    }

    @Test
    fun `getCurrentUser converts to User`() = runTest {
        coEvery {
            mockApi.me()
        } returns ApiResponse.Success(
            MemosV0User(
                createdTs = 1700000000,
                email = "test@example.com",
                id = 100,
                name = "Test User",
                role = xyz.nachaos.memosyou.data.api.MemosRole.USER,
                rowStatus = MemosRowStatus.NORMAL,
                updatedTs = 1700000000,
                nickname = "Testy",
                username = "testuser",
                avatarUrl = "https://example.com/avatar.png"
            )
        )

        val result = repository.getCurrentUser()

        assertTrue(result is ApiResponse.Success)
        val user = (result as ApiResponse.Success).data
        assertEquals("100", user.identifier)
        assertEquals("Testy", user.name)
        assertEquals("https://example.com/avatar.png", user.avatarUrl)
    }

    @Test
    fun `listMemoComments returns empty list`() = runTest {
        val result = repository.listMemoComments("memos/1", 10, null)

        assertTrue(result is ApiResponse.Success)
        val (comments, _) = (result as ApiResponse.Success).data
        assertEquals(emptyList<Memo>(), comments)
    }

    @Test
    fun `createMemoComment returns not supported error`() = runTest {
        val result = repository.createMemoComment("memos/1", "comment")

        assertTrue(result is ApiResponse.Exception)
        assertEquals("Comments not supported in Memos V0 API", (result as ApiResponse.Exception).run { this })
    }
}

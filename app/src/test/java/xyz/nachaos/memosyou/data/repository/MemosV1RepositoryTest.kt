package xyz.nachaos.memosyou.data.repository

import com.skydoves.sandwich.ApiResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import xyz.nachaos.memosyou.data.api.MemosV1Api
import xyz.nachaos.memosyou.data.api.MemosV1Memo
import xyz.nachaos.memosyou.data.api.MemosV1Resource
import xyz.nachaos.memosyou.data.api.MemosV1State
import xyz.nachaos.memosyou.data.api.MemosVisibility
import xyz.nachaos.memosyou.data.model.Account
import xyz.nachaos.memosyou.data.model.Memo
import xyz.nachaos.memosyou.data.model.MemoLocation
import xyz.nachaos.memosyou.data.model.MemoRelation
import xyz.nachaos.memosyou.data.model.MemoRelationRef
import xyz.nachaos.memosyou.data.model.MemoVisibility as AppMemoVisibility
import xyz.nachaos.memosyou.data.model.RelationType
import xyz.nachaos.memosyou.data.model.Resource
import xyz.nachaos.memosyou.data.model.User
import java.time.Instant

class MemosV1RepositoryTest {

    private val mockApi = mockk<MemosV1Api>()
    private val account = Account.MemosV1(
        xyz.nachaos.memosyou.data.model.MemosAccount(
            host = "https://memos.example.com",
            remoteIdentifier = "user-123"
        )
    )
    private val repository = MemosV1Repository(mockApi, account)

    @Test
    fun `convertMemo maps all fields correctly`() = runTest {
        val now = Instant.now()
        val apiMemo = MemosV1Memo(
            name = "memos/abc123",
            state = MemosV1State.NORMAL,
            creator = "user-123",
            createTime = now,
            updateTime = now,
            content = "Test content",
            visibility = MemosVisibility.PRIVATE,
            pinned = true,
            attachments = listOf(
                MemosV1Resource(
                    name = "res-1",
                    createTime = now,
                    filename = "image.png",
                    type = "image/png"
                )
            ),
            tags = listOf("tag1", "tag2"),
            relations = listOf(
                xyz.nachaos.memosyou.data.api.MemosV1MemoRelation(
                    memo = xyz.nachaos.memosyou.data.api.MemosV1RelationMemo(name = "memos/ref1", snippet = "ref snippet"),
                    relatedMemo = xyz.nachaos.memosyou.data.api.MemosV1RelationMemo(name = "memos/ref2", snippet = "related snippet"),
                    type = xyz.nachaos.memosyou.data.api.MemosV1RelationType.REFERENCE
                )
            ),
            location = xyz.nachaos.memosyou.data.api.MemosV1Location(
                placeholder = "Some Place",
                latitude = 37.7749,
                longitude = -122.4194
            )
        )

        val memo = repository.convertMemo(apiMemo)

        assertEquals("abc123", memo.remoteId)
        assertEquals("Test content", memo.content)
        assertEquals(now, memo.date)
        assertEquals(true, memo.pinned)
        assertEquals(AppMemoVisibility.PRIVATE, memo.visibility)
        assertEquals(1, memo.resources.size)
        assertEquals("res-1", memo.resources[0].remoteId)
        assertEquals("image.png", memo.resources[0].filename)
        assertEquals("https://memos.example.com/file/res-1/image.png", memo.resources[0].uri)
        assertEquals(false, memo.archived)
        assertEquals(now, memo.updatedAt)
        assertEquals(MemoLocation("Some Place", 37.7749, -122.4194), memo.location)
        assertEquals(1, memo.relations.size)
        assertEquals(RelationType.REFERENCE, memo.relations[0].type)
    }

    @Test
    fun `convertMemo handles null fields`() = runTest {
        val apiMemo = MemosV1Memo(
            name = "memos/xyz",
            state = null,
            creator = null,
            createTime = null,
            updateTime = null,
            content = null,
            visibility = null,
            pinned = null,
            attachments = null,
            tags = null,
            relations = null,
            location = null
        )

        val memo = repository.convertMemo(apiMemo)

        assertEquals("xyz", memo.remoteId)
        assertEquals("", memo.content)
        assertEquals(Instant.now(), memo.date) // falls back to now
        assertEquals(false, memo.pinned)
        assertEquals(AppMemoVisibility.PRIVATE, memo.visibility) // default
        assertEquals(emptyList<Resource>(), memo.resources)
        assertEquals(false, memo.archived)
        assertEquals(null, memo.updatedAt)
        assertEquals(null, memo.location)
        assertEquals(emptyList<MemoRelation>(), memo.relations)
    }

    @Test
    fun `convertResource maps fields correctly`() = runTest {
        val now = Instant.now()
        val apiResource = MemosV1Resource(
            name = "res-1",
            createTime = now,
            filename = "doc.pdf",
            type = "application/pdf",
            externalLink = "https://external.example.com/doc.pdf"
        )

        val resource = repository.convertResource(apiResource)

        assertEquals("res-1", resource.remoteId)
        assertEquals(now, resource.date)
        assertEquals("doc.pdf", resource.filename)
        assertEquals("https://external.example.com/doc.pdf", resource.uri)
        assertEquals("application/pdf", resource.mimeType)
    }

    @Test
    fun `convertResource falls back to now when createTime is null`() = runTest {
        val apiResource = MemosV1Resource(
            name = "res-1",
            createTime = null,
            filename = "doc.pdf",
            type = "application/pdf"
        )

        val resource = repository.convertResource(apiResource)

        assertEquals(Instant.now(), resource.date)
    }

    @Test
    fun `getId extracts id from name`() {
        assertEquals("abc", repository.getId("memos/abc"))
        assertEquals("abc", repository.getId("memos/abc|extra"))
        assertEquals("abc", repository.getId("prefix/memos/abc"))
    }

    @Test
    fun `getName extracts name before pipe`() {
        assertEquals("memos/abc", repository.getName("memos/abc|extra"))
        assertEquals("memos/abc", repository.getName("memos/abc"))
        assertEquals("", repository.getName("|extra"))
    }

    @Test
    fun `listMemos returns converted memos`() = runTest {
        val now = Instant.now()
        coEvery {
            mockApi.listMemos(200, "", MemosV1State.NORMAL, "creator == \"user-123\"")
        } returns ApiResponse.Success(
            xyz.nachaos.memosyou.data.api.ListMemosResponse(
                memos = listOf(
                    MemosV1Memo(
                        name = "memos/1",
                        createTime = now,
                        content = "Hello",
                        visibility = MemosVisibility.PRIVATE
                    )
                ),
                nextPageToken = null
            )
        )

        val result = repository.listMemos()

        assertTrue(result is ApiResponse.Success)
        val memos = (result as ApiResponse.Success).data
        assertEquals(1, memos.size)
        assertEquals("Hello", memos[0].content)
    }

    @Test
    fun `listArchivedMemos uses ARCHIVED state`() = runTest {
        val now = Instant.now()
        coEvery {
            mockApi.listMemos(200, "", MemosV1State.ARCHIVED, "creator == \"user-123\"")
        } returns ApiResponse.Success(
            xyz.nachaos.memosyou.data.api.ListMemosResponse(
                memos = emptyList(),
                nextPageToken = null
            )
        )

        val result = repository.listArchivedMemos()

        assertTrue(result is ApiResponse.Success)
        coVerify { mockApi.listMemos(200, "", MemosV1State.ARCHIVED, "creator == \"user-123\"") }
    }

    @Test
    fun `createMemo converts request correctly`() = runTest {
        val now = Instant.now()
        coEvery {
            mockApi.createMemo(any())
        } returns ApiResponse.Success(
            MemosV1Memo(
                name = "memos/new-memo",
                createTime = now,
                content = "New memo",
                visibility = MemosVisibility.PUBLIC
            )
        )

        val result = repository.createMemo(
            content = "New memo",
            visibility = AppMemoVisibility.PUBLIC,
            resourceRemoteIds = emptyList(),
            tags = null,
            createdAt = now
        )

        assertTrue(result is ApiResponse.Success)
        val memo = (result as ApiResponse.Success).data
        assertEquals("New memo", memo.content)
        assertEquals(AppMemoVisibility.PUBLIC, memo.visibility)
    }

    @Test
    fun `updateMemo converts request correctly`() = runTest {
        val now = Instant.now()
        coEvery {
            mockApi.updateMemo(eq("abc"), any())
        } returns ApiResponse.Success(
            MemosV1Memo(
                name = "memos/abc",
                createTime = now,
                content = "Updated",
                visibility = MemosVisibility.PROTECTED,
                state = MemosV1State.ARCHIVED
            )
        )

        val result = repository.updateMemo(
            remoteId = "memos/abc|extra",
            content = "Updated",
            resourceRemoteIds = null,
            visibility = AppMemoVisibility.PROTECTED,
            tags = null,
            pinned = null,
            archived = true
        )

        assertTrue(result is ApiResponse.Success)
        val memo = (result as ApiResponse.Success).data
        assertEquals("Updated", memo.content)
        assertEquals(AppMemoVisibility.PROTECTED, memo.visibility)
        assertEquals(true, memo.archived)
        coVerify { mockApi.updateMemo(eq("abc"), any()) }
    }

    @Test
    fun `deleteMemo extracts id correctly`() = runTest {
        coEvery { mockApi.deleteMemo("abc") } returns ApiResponse.Success(Unit)

        val result = repository.deleteMemo("memos/abc|extra")

        assertTrue(result is ApiResponse.Success)
        coVerify { mockApi.deleteMemo("abc") }
    }

    @Test
    fun `listResources converts resources`() = runTest {
        val now = Instant.now()
        coEvery {
            mockApi.listResources()
        } returns ApiResponse.Success(
            xyz.nachaos.memosyou.data.api.ListResourceResponse(
                attachments = listOf(
                    MemosV1Resource(
                        name = "res-1",
                        createTime = now,
                        filename = "file.txt",
                        type = "text/plain"
                    )
                )
            )
        )

        val result = repository.listResources()

        assertTrue(result is ApiResponse.Success)
        val resources = (result as ApiResponse.Success).data
        assertEquals(1, resources.size)
        assertEquals("res-1", resources[0].remoteId)
        assertEquals("file.txt", resources[0].filename)
    }

    @Test
    fun `convertRelation maps all types correctly`() = runTest {
        val apiRelation = xyz.nachaos.memosyou.data.api.MemosV1MemoRelation(
            memo = xyz.nachaos.memosyou.data.api.MemosV1RelationMemo(name = "memos/m1", snippet = "s1"),
            relatedMemo = xyz.nachaos.memosyou.data.api.MemosV1RelationMemo(name = "memos/m2", snippet = "s2"),
            type = xyz.nachaos.memosyou.data.api.MemosV1RelationType.COMMENT
        )

        val relation = repository.convertRelation(apiRelation)

        assertEquals("m1", relation.memo.name)
        assertEquals("s1", relation.memo.snippet)
        assertEquals("m2", relation.relatedMemo.name)
        assertEquals("s2", relation.relatedMemo.snippet)
        assertEquals(RelationType.COMMENT, relation.type)
    }

    @Test
    fun `convertLocation maps fields correctly`() = runTest {
        val apiLocation = xyz.nachaos.memosyou.data.api.MemosV1Location(
            placeholder = "Office",
            latitude = 40.7128,
            longitude = -74.0060
        )

        val location = repository.convertLocation(apiLocation)

        assertEquals("Office", location.placeholder)
        assertEquals(40.7128, location.latitude, 0.0001)
        assertEquals(-74.0060, location.longitude, 0.0001)
    }
}

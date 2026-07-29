package xyz.nachaos.memosyou.data.service

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import xyz.nachaos.memosyou.BuildConfig
import xyz.nachaos.memosyou.data.local.FileStorage
import xyz.nachaos.memosyou.data.local.MoeMemosDatabase
import xyz.nachaos.memosyou.data.local.entity.ResourceEntity
import xyz.nachaos.memosyou.data.model.Account
import xyz.nachaos.memosyou.data.model.LocalAccount
import xyz.nachaos.memosyou.data.model.MemosAccount
import xyz.nachaos.memosyou.data.model.UserData
import xyz.nachaos.memosyou.data.model.UserSettings
import okhttp3.OkHttpClient
import java.io.File

class AccountServiceTest {

    private lateinit var mockContext: android.content.Context
    private lateinit var mockOkHttpClient: OkHttpClient
    private lateinit var mockDatabase: MoeMemosDatabase
    private lateinit var mockFileStorage: FileStorage
    private lateinit var mockSecureTokenStorage: SecureTokenStorage
    private lateinit var accountService: AccountService

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        mockContext = mock<android.content.Context>()
        mockOkHttpClient = mock<OkHttpClient>()
        mockDatabase = mock<MoeMemosDatabase>()
        mockFileStorage = mock<FileStorage>()
        mockSecureTokenStorage = mock<SecureTokenStorage>()

        whenever(BuildConfig.DEBUG).thenReturn(false)

        accountService = AccountService(
            context = mockContext,
            okHttpClient = mockOkHttpClient,
            database = mockDatabase,
            fileStorage = mockFileStorage,
            secureTokenStorage = mockSecureTokenStorage
        )
    }

    @Test
    fun `createMemosV0Client adds authorization header when token provided`() = runTest {
        val mockClient = mock<OkHttpClient>()
        val mockApi = mock<MemosV0Api>()
        val mockBuilder = mock<OkHttpClient.Builder>()
        whenever(mockOkHttpClient.newBuilder()).thenReturn(mockBuilder)
        whenever(mockBuilder.addNetworkInterceptor(any())).thenReturn(mockBuilder)
        whenever(mockBuilder.build()).thenReturn(mockClient)

        val (client, api) = accountService.createMemosV0Client("https://memos.example.com", "test-token")

        assertNotNull(client)
        assertNotNull(api)
    }

    @Test
    fun `createMemosV0Client does not add authorization header when token is null`() = runTest {
        val (client, api) = accountService.createMemosV0Client("https://memos.example.com", null)

        assertNotNull(client)
        assertNotNull(api)
    }

    @Test
    fun `createMemosV1Client adds logging interceptor only in debug`() = runTest {
        whenever(BuildConfig.DEBUG).thenReturn(true)

        val (client, api) = accountService.createMemosV1Client("https://memos.example.com", "test-token")

        assertNotNull(client)
        assertNotNull(api)
    }

    @Test
    fun `createMemosV1Client does not add logging interceptor in release`() = runTest {
        whenever(BuildConfig.DEBUG).thenReturn(false)

        val (client, api) = accountService.createMemosV1Client("https://memos.example.com", "test-token")

        assertNotNull(client)
        assertNotNull(api)
    }

    @Test
    fun `sanitizePathComponent removes null bytes`() {
        val method = AccountService::class.java.getDeclaredMethod(
            "sanitizePathComponent", String::class.java
        )
        method.isAccessible = true

        val result = method.invoke(accountService, "file\u0000name") as String
        assertEquals("filename", result)
    }

    @Test
    fun `sanitizePathComponent removes path separators`() {
        val method = AccountService::class.java.getDeclaredMethod(
            "sanitizePathComponent", String::class.java
        )
        method.isAccessible = true

        val result = method.invoke(accountService, "path/to/file") as String
        assertEquals("path_to_file", result)
    }

    @Test
    fun `sanitizePathComponent removes parent directory traversal`() {
        val method = AccountService::class.java.getDeclaredMethod(
            "sanitizePathComponent", String::class.java
        )
        method.isAccessible = true

        val result = method.invoke(accountService, "..") as String
        assertEquals("__", result)
    }

    @Test
    fun `sanitizePathComponent removes leading dots`() {
        val method = AccountService::class.java.getDeclaredMethod(
            "sanitizePathComponent", String::class.java
        )
        method.isAccessible = true

        val result = method.invoke(accountService, ".hidden") as String
        assertEquals("hidden", result)
    }

    @Test
    fun `sanitizePathComponent truncates long names`() {
        val method = AccountService::class.java.getDeclaredMethod(
            "sanitizePathComponent", String::class.java
        )
        method.isAccessible = true

        val longName = "a".repeat(300)
        val result = method.invoke(accountService, longName) as String
        assertEquals(200, result.length)
    }

    @Test
    fun `sanitizePathComponent returns unnamed for blank`() {
        val method = AccountService::class.java.getDeclaredMethod(
            "sanitizePathComponent", String::class.java
        )
        method.isAccessible = true

        val result = method.invoke(accountService, "") as String
        assertEquals("unnamed", result)
    }

    @Test
    fun `sanitizePathComponent handles windows separator`() {
        val method = AccountService::class.java.getDeclaredMethod(
            "sanitizePathComponent", String::class.java
        )
        method.isAccessible = true

        val result = method.invoke(accountService, "folder\\file") as String
        assertEquals("folder_file", result)
    }

    @Test
    fun `shouldAttachAccessToken returns true for same host`() {
        val method = AccountService::class.java.getDeclaredMethod(
            "shouldAttachAccessToken",
            okhttp3.HttpUrl::class.java,
            String::class.java
        )
        method.isAccessible = true

        val requestUrl = "https://memos.example.com/api/v1/memos".toHttpUrl()
        val result = method.invoke(accountService, requestUrl, "https://memos.example.com") as Boolean
        assertTrue(result)
    }

    @Test
    fun `shouldAttachAccessToken returns false for different host`() {
        val method = AccountService::class.java.getDeclaredMethod(
            "shouldAttachAccessToken",
            okhttp3.HttpUrl::class.java,
            String::class.java
        )
        method.isAccessible = true

        val requestUrl = "https://other.example.com/api/v1/memos".toHttpUrl()
        val result = method.invoke(accountService, requestUrl, "https://memos.example.com") as Boolean
        assertFalse(result)
    }

    @Test
    fun `shouldAttachAccessToken returns false for different scheme`() {
        val method = AccountService::class.java.getDeclaredMethod(
            "shouldAttachAccessToken",
            okhttp3.HttpUrl::class.java,
            String::class.java
        )
        method.isAccessible = true

        val requestUrl = "http://memos.example.com/api/v1/memos".toHttpUrl()
        val result = method.invoke(accountService, requestUrl, "https://memos.example.com") as Boolean
        assertFalse(result)
    }

    @Test
    fun `shouldAttachAccessToken returns false for different port`() {
        val method = AccountService::class.java.getDeclaredMethod(
            "shouldAttachAccessToken",
            okhttp3.HttpUrl::class.java,
            String::class.java
        )
        method.isAccessible = true

        val requestUrl = "https://memos.example.com:8443/api/v1/memos".toHttpUrl()
        val result = method.invoke(accountService, requestUrl, "https://memos.example.com") as Boolean
        assertFalse(result)
    }

    @Test
    fun `shouldAttachAccessToken returns false for invalid host`() {
        val method = AccountService::class.java.getDeclaredMethod(
            "shouldAttachAccessToken",
            okhttp3.HttpUrl::class.java,
            String::class.java
        )
        method.isAccessible = true

        val requestUrl = "https://memos.example.com/api/v1/memos".toHttpUrl()
        val result = method.invoke(accountService, requestUrl, "not-a-valid-url") as Boolean
        assertFalse(result)
    }

    @Test
    fun `uniqueMemoBaseName generates unique names`() {
        val method = AccountService::class.java.getDeclaredMethod(
            "uniqueMemoBaseName",
            java.time.Instant::class.java,
            java.util.HashMap::class.java
        )
        method.isAccessible = true

        val collisionMap = HashMap<String, Int>()
        val now = java.time.Instant.now()

        val name1 = method.invoke(accountService, now, collisionMap) as String
        val name2 = method.invoke(accountService, now, collisionMap) as String

        assertTrue(name1 != name2)
        assertTrue(name2.startsWith(name1))
    }

    @Test
    fun `exportFileExtension returns filename extension`() {
        val method = AccountService::class.java.getDeclaredMethod(
            "exportFileExtension",
            ResourceEntity::class.java,
            File::class.java
        )
        method.isAccessible = true

        val resource = ResourceEntity(
            identifier = "memo-1",
            remoteId = null,
            accountKey = "local",
            date = java.time.Instant.now(),
            filename = "document.pdf",
            uri = "file:///path/to/document.pdf",
            localUri = "file:///path/to/document.pdf",
            mimeType = "application/pdf",
            memoId = null
        )
        val file = File("/path/to/document.pdf")

        val result = method.invoke(accountService, resource, file) as String
        assertEquals("pdf", result)
    }

    @Test
    fun `exportFileExtension falls back to source extension`() {
        val method = AccountService::class.java.getDeclaredMethod(
            "exportFileExtension",
            ResourceEntity::class.java,
            File::class.java
        )
        method.isAccessible = true

        val resource = ResourceEntity(
            identifier = "memo-1",
            remoteId = null,
            accountKey = "local",
            date = java.time.Instant.now(),
            filename = "document",
            uri = "file:///path/to/document.pdf",
            localUri = "file:///path/to/document.pdf",
            mimeType = "application/pdf",
            memoId = null
        )
        val file = File("/path/to/document.pdf")

        val result = method.invoke(accountService, resource, file) as String
        assertEquals("pdf", result)
    }

    @Test
    fun `exportFileExtension falls back to mime type`() {
        val method = AccountService::class.java.getDeclaredMethod(
            "exportFileExtension",
            ResourceEntity::class.java,
            File::class.java
        )
        method.isAccessible = true

        val resource = ResourceEntity(
            identifier = "memo-1",
            remoteId = null,
            accountKey = "local",
            date = java.time.Instant.now(),
            filename = "document",
            uri = "file:///path/to/document",
            localUri = "file:///path/to/document",
            mimeType = "application/pdf",
            memoId = null
        )
        val file = File("/path/to/document")

        val result = method.invoke(accountService, resource, file) as String
        assertEquals("pdf", result)
    }

    @Test
    fun `exportFileExtension returns empty when no extension found`() {
        val method = AccountService::class.java.getDeclaredMethod(
            "exportFileExtension",
            ResourceEntity::class.java,
            File::class.java
        )
        method.isAccessible = true

        val resource = ResourceEntity(
            identifier = "memo-1",
            remoteId = null,
            accountKey = "local",
            date = java.time.Instant.now(),
            filename = "document",
            uri = "file:///path/to/document",
            localUri = "file:///path/to/document",
            mimeType = "application/octet-stream",
            memoId = null
        )
        val file = File("/path/to/document")

        val result = method.invoke(accountService, resource, file) as String
        assertEquals("", result)
    }

    @Test
    fun `localFileForResource returns null for non-file scheme`() {
        val method = AccountService::class.java.getDeclaredMethod(
            "localFileForResource",
            ResourceEntity::class.java
        )
        method.isAccessible = true

        val resource = ResourceEntity(
            identifier = "memo-1",
            remoteId = null,
            accountKey = "local",
            date = java.time.Instant.now(),
            filename = "doc.pdf",
            uri = "content://external/document.pdf",
            localUri = "content://external/document.pdf",
            mimeType = "application/pdf",
            memoId = null
        )

        val result = method.invoke(accountService, resource) as File?
        assertEquals(null, result)
    }

    @Test
    fun `localFileForResource returns null for empty path`() {
        val method = AccountService::class.java.getDeclaredMethod(
            "localFileForResource",
            ResourceEntity::class.java
        )
        method.isAccessible = true

        val resource = ResourceEntity(
            identifier = "memo-1",
            remoteId = null,
            accountKey = "local",
            date = java.time.Instant.now(),
            filename = "doc.pdf",
            uri = "file://",
            localUri = "file://",
            mimeType = "application/pdf",
            memoId = null
        )

        val result = method.invoke(accountService, resource) as File?
        assertEquals(null, result)
    }

    @Test
    fun `localFileForResource returns file for valid file uri`() {
        val method = AccountService::class.java.getDeclaredMethod(
            "localFileForResource",
            ResourceEntity::class.java
        )
        method.isAccessible = true

        val resource = ResourceEntity(
            identifier = "memo-1",
            remoteId = null,
            accountKey = "local",
            date = java.time.Instant.now(),
            filename = "doc.pdf",
            uri = "file:///path/to/doc.pdf",
            localUri = "file:///path/to/doc.pdf",
            mimeType = "application/pdf",
            memoId = null
        )

        val result = method.invoke(accountService, resource) as File?
        assertNotNull(result)
        assertEquals("/path/to/doc.pdf", result!!.absolutePath)
    }

    @Test
    fun `localFileForResource falls back to uri when localUri is null`() {
        val method = AccountService::class.java.getDeclaredMethod(
            "localFileForResource",
            ResourceEntity::class.java
        )
        method.isAccessible = true

        val resource = ResourceEntity(
            identifier = "memo-1",
            remoteId = null,
            accountKey = "local",
            date = java.time.Instant.now(),
            filename = "doc.pdf",
            uri = "file:///fallback/path/doc.pdf",
            localUri = null,
            mimeType = "application/pdf",
            memoId = null
        )

        val result = method.invoke(accountService, resource) as File?
        assertNotNull(result)
        assertEquals("/fallback/path/doc.pdf", result!!.absolutePath)
    }
}

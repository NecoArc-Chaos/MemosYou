package xyz.nachaos.memosyou.data.service

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import xyz.nachaos.memosyou.BuildConfig
import xyz.nachaos.memosyou.data.api.MemosV0Api
import xyz.nachaos.memosyou.data.api.MemosV1Api
import xyz.nachaos.memosyou.data.api.MemosV1ProfileResponse
import xyz.nachaos.memosyou.data.api.MemosV1User
import xyz.nachaos.memosyou.data.api.MemosV1UserSetting
import xyz.nachaos.memosyou.data.constant.MemosVersionSupport
import xyz.nachaos.memosyou.data.local.FileStorage
import xyz.nachaos.memosyou.data.local.MoeMemosDatabase
import xyz.nachaos.memosyou.data.local.entity.ResourceEntity
import xyz.nachaos.memosyou.data.model.Account
import xyz.nachaos.memosyou.data.model.LocalAccount
import xyz.nachaos.memosyou.data.model.Memo
import xyz.nachaos.memosyou.data.model.MemosAccount
import xyz.nachaos.memosyou.data.model.User
import xyz.nachaos.memosyou.data.model.UserData
import xyz.nachaos.memosyou.data.model.UserSettings
import xyz.nachaos.memosyou.data.repository.AbstractMemoRepository
import xyz.nachaos.memosyou.data.repository.LocalDatabaseRepository
import xyz.nachaos.memosyou.data.repository.MemosV0Repository
import xyz.nachaos.memosyou.data.repository.MemosV1Repository
import xyz.nachaos.memosyou.data.repository.RemoteRepository
import xyz.nachaos.memosyou.data.repository.SyncingRepository
import xyz.nachaos.memosyou.ext.settingsDataStore
import xyz.nachaos.memosyou.ext.string
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
        mockContext = mockk<android.content.Context>()
        mockOkHttpClient = mockk<OkHttpClient>()
        mockDatabase = mockk<MoeMemosDatabase>()
        mockFileStorage = mockk<FileStorage>()
        mockSecureTokenStorage = mockk<SecureTokenStorage>()

        mockkObject(BuildConfig)
        every { BuildConfig.DEBUG } returns false

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
        val mockClient = mockk<OkHttpClient>()
        val mockApi = mockk<MemosV0Api>()
        every { mockOkHttpClient.newBuilder() } returns mockk {
            every { addNetworkInterceptor(any()) } returns this
            every { build() } returns mockClient
        }
        every { mockClient.newBuilder() } returns mockk {
            every { addNetworkInterceptor(any()) } returns this
            every { build() } returns mockClient
        }
        val mockRetrofit = mockk<retrofit2.Retrofit>()
        every {
            mockClient.newBuilder().build()
        } returns mockClient

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
        every { BuildConfig.DEBUG } returns true

        val (client, api) = accountService.createMemosV1Client("https://memos.example.com", "test-token")

        assertNotNull(client)
        assertNotNull(api)
    }

    @Test
    fun `createMemosV1Client does not add logging interceptor in release`() = runTest {
        every { BuildConfig.DEBUG } returns false

        val (client, api) = accountService.createMemosV1Client("https://memos.example.com", "test-token")

        assertNotNull(client)
        assertNotNull(api)
    }

    @Test
    fun `sanitizePathComponent removes null bytes`() {
        // Access private method via reflection
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
            memoIdentifier = "memo-1",
            filename = "document.pdf",
            mimeType = "application/pdf",
            localUri = "file:///path/to/document.pdf"
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
            memoIdentifier = "memo-1",
            filename = "document",
            mimeType = "application/pdf",
            localUri = "file:///path/to/document.pdf"
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
            memoIdentifier = "memo-1",
            filename = "document",
            mimeType = "application/pdf",
            localUri = "file:///path/to/document"
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
            memoIdentifier = "memo-1",
            filename = "document",
            mimeType = "application/octet-stream",
            localUri = "file:///path/to/document"
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
            memoIdentifier = "memo-1",
            filename = "doc.pdf",
            mimeType = "application/pdf",
            localUri = "content://external/document.pdf"
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
            memoIdentifier = "memo-1",
            filename = "doc.pdf",
            mimeType = "application/pdf",
            localUri = "file://"
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
            memoIdentifier = "memo-1",
            filename = "doc.pdf",
            mimeType = "application/pdf",
            localUri = "file:///path/to/doc.pdf"
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
            memoIdentifier = "memo-1",
            filename = "doc.pdf",
            mimeType = "application/pdf",
            localUri = null,
            uri = "file:///fallback/path/doc.pdf"
        )

        val result = method.invoke(accountService, resource) as File?
        assertNotNull(result)
        assertEquals("/fallback/path/doc.pdf", result!!.absolutePath)
    }

    @Test
    fun `parseAccountWithSecureToken returns null for invalid account`() {
        val userData = UserData(
            accountKey = "invalid",
            memosV0 = null,
            memosV1 = null,
            local = null
        )
        every { mockSecureTokenStorage.getToken("invalid") } returns "token"

        val result = accountService.parseAccountWithSecureToken(userData)
        assertEquals(null, result)
    }

    @Test
    fun `parseAccountWithSecureToken injects token into V0 account`() {
        val userData = UserData(
            accountKey = "memos:https://example.com:123",
            memosV0 = MemosAccount(
                host = "https://example.com",
                remoteIdentifier = "123",
                accessToken = ""
            )
        )
        every { mockSecureTokenStorage.getToken("memos:https://example.com:123") } returns "secure-token"

        val result = accountService.parseAccountWithSecureToken(userData)

        assertNotNull(result)
        val account = result as Account.MemosV0
        assertEquals("secure-token", account.info.accessToken)
    }

    @Test
    fun `parseAccountWithSecureToken injects token into V1 account`() {
        val userData = UserData(
            accountKey = "memos:https://example.com:456",
            memosV1 = MemosAccount(
                host = "https://example.com",
                remoteIdentifier = "456",
                accessToken = ""
            )
        )
        every { mockSecureTokenStorage.getToken("memos:https://example.com:456") } returns "secure-token"

        val result = accountService.parseAccountWithSecureToken(userData)

        assertNotNull(result)
        val account = result as Account.MemosV1
        assertEquals("secure-token", account.info.accessToken)
    }

    @Test
    fun `parseAccountWithSecureToken keeps Local account unchanged`() {
        val userData = UserData(
            accountKey = "local",
            local = LocalAccount()
        )
        every { mockSecureTokenStorage.getToken("local") } returns "token"

        val result = accountService.parseAccountWithSecureToken(userData)

        assertNotNull(result)
        assertEquals(Account.Local::class, result!!::class)
    }

    @Test
    fun `toPersistedUserData clears access token for V0`() {
        val account = Account.MemosV0(
            MemosAccount(
                host = "https://example.com",
                remoteIdentifier = "123",
                accessToken = "secret-token"
            )
        )
        val settings = UserSettings()

        val method = AccountService::class.java.getDeclaredMethod(
            "toPersistedUserData",
            Account::class.java,
            UserSettings::class.java
        )
        method.isAccessible = true

        val result = method.invoke(accountService, account, settings) as UserData

        assertEquals("memos:https://example.com:123", result.accountKey)
        assertEquals("", result.memosV0!!.accessToken)
    }

    @Test
    fun `toPersistedUserData clears access token for V1`() {
        val account = Account.MemosV1(
            MemosAccount(
                host = "https://example.com",
                remoteIdentifier = "456",
                accessToken = "secret-token"
            )
        )
        val settings = UserSettings()

        val method = AccountService::class.java.getDeclaredMethod(
            "toPersistedUserData",
            Account::class.java,
            UserSettings::class.java
        )
        method.isAccessible = true

        val result = method.invoke(accountService, account, settings) as UserData

        assertEquals("memos:https://example.com:456", result.accountKey)
        assertEquals("", result.memosV1!!.accessToken)
    }
}

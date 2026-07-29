package xyz.nachaos.memosyou.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class AccountTest {

    @Test
    fun `MemosV0 accountKey returns correct format`() {
        val account = Account.MemosV0(
            MemosAccount(
                host = "https://memos.example.com",
                remoteIdentifier = "12345"
            )
        )
        assertEquals("memos:https://memos.example.com:12345", account.accountKey())
    }

    @Test
    fun `MemosV1 accountKey returns correct format`() {
        val account = Account.MemosV1(
            MemosAccount(
                host = "https://memos.example.com",
                remoteIdentifier = "67890"
            )
        )
        assertEquals("memos:https://memos.example.com:67890", account.accountKey())
    }

    @Test
    fun `Local accountKey returns local`() {
        val account = Account.Local(LocalAccount())
        assertEquals("local", account.accountKey())
    }

    @Test
    fun `MemosV0 toUser returns correct user`() {
        val account = Account.MemosV0(
            MemosAccount(
                host = "https://memos.example.com",
                remoteIdentifier = "12345",
                name = "Test User",
                startDateEpochSecond = 1700000000L,
                defaultVisibility = "PUBLIC"
            )
        )
        val user = account.toUser()
        assertEquals("12345", user.identifier)
        assertEquals("Test User", user.name)
        assertEquals(MemoVisibility.PUBLIC, user.defaultVisibility)
        assertEquals(Instant.ofEpochSecond(1700000000L), user.startDate)
    }

    @Test
    fun `MemosV1 toUser returns correct user with avatar`() {
        val account = Account.MemosV1(
            MemosAccount(
                host = "https://memos.example.com",
                remoteIdentifier = "67890",
                name = "Test User",
                avatarUrl = "https://example.com/avatar.png",
                startDateEpochSecond = 1700000000L,
                defaultVisibility = "PROTECTED"
            )
        )
        val user = account.toUser()
        assertEquals("67890", user.identifier)
        assertEquals("Test User", user.name)
        assertEquals("https://example.com/avatar.png", user.avatarUrl)
        assertEquals(MemoVisibility.PROTECTED, user.defaultVisibility)
    }

    @Test
    fun `Local toUser returns correct user`() {
        val account = Account.Local(
            LocalAccount(startDateEpochSecond = 1700000000L)
        )
        val user = account.toUser()
        assertEquals("local", user.identifier)
        assertEquals("Local Account", user.name)
        assertEquals(Instant.ofEpochSecond(1700000000L), user.startDate)
    }

    @Test
    fun `withUser updates account info`() {
        val original = Account.MemosV1(
            MemosAccount(
                host = "https://memos.example.com",
                remoteIdentifier = "12345",
                name = "Old Name",
                startDateEpochSecond = 1000000000L,
                defaultVisibility = "PRIVATE"
            )
        )
        val newUser = User(
            identifier = "12345",
            name = "New Name",
            startDate = Instant.ofEpochSecond(2000000000L),
            defaultVisibility = MemoVisibility.PUBLIC
        )
        val updated = original.withUser(newUser)
        val user = updated.toUser()
        assertEquals("New Name", user.name)
        assertEquals(MemoVisibility.PUBLIC, user.defaultVisibility)
        assertEquals(Instant.ofEpochSecond(2000000000L), user.startDate)
    }

    @Test
    fun `parseUserData returns MemosV0 for V0 account`() {
        val userData = UserData(
            accountKey = "memos:https://example.com:123",
            memosV0 = MemosAccount(host = "https://example.com", remoteIdentifier = "123")
        )
        val account = Account.parseUserData(userData)
        assertNotNull(account)
        assertEquals(Account.MemosV0::class, account!!::class)
    }

    @Test
    fun `parseUserData returns MemosV1 for V1 account`() {
        val userData = UserData(
            accountKey = "memos:https://example.com:456",
            memosV1 = MemosAccount(host = "https://example.com", remoteIdentifier = "456")
        )
        val account = Account.parseUserData(userData)
        assertNotNull(account)
        assertEquals(Account.MemosV1::class, account!!::class)
    }

    @Test
    fun `parseUserData returns Local for local account`() {
        val userData = UserData(
            accountKey = "local",
            local = LocalAccount()
        )
        val account = Account.parseUserData(userData)
        assertNotNull(account)
        assertEquals(Account.Local::class, account!!::class)
    }

    @Test
    fun `parseUserData returns null for ACCOUNT_NOT_SET`() {
        val userData = UserData(accountKey = "")
        val account = Account.parseUserData(userData)
        assertNull(account)
    }

    @Test
    fun `getAccountInfo returns null for Local account`() {
        val account = Account.Local(LocalAccount())
        assertNull(account.getAccountInfo())
    }

    @Test
    fun `getAccountInfo returns info for remote account`() {
        val memosAccount = MemosAccount(host = "https://example.com", name = "Test")
        val account = Account.MemosV1(memosAccount)
        assertEquals(memosAccount, account.getAccountInfo())
    }
}

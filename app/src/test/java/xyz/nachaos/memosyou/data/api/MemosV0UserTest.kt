package xyz.nachaos.memosyou.data.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import xyz.nachaos.memosyou.data.model.MemoVisibility
import java.time.Instant

class MemosV0UserTest {

    @Test
    fun `toUser with all fields`() {
        val user = MemosV0User(
            createdTs = 1700000000,
            email = "test@example.com",
            id = 100,
            name = "Test User",
            role = MemosRole.USER,
            rowStatus = MemosRowStatus.NORMAL,
            updatedTs = 1700000100,
            nickname = "Testy",
            username = "testuser",
            avatarUrl = "https://example.com/avatar.png",
            userSettingList = listOf(
                MemosV0UserSetting(
                    key = MemosV0UserSettingKey.MEMO_VISIBILITY,
                    value = "\"PUBLIC\""
                )
            )
        )

        val result = user.toUser()

        assertEquals("100", result.identifier)
        assertEquals("Testy", result.name)
        assertEquals(Instant.ofEpochSecond(1700000000), result.startDate)
        assertEquals(MemoVisibility.PUBLIC, result.defaultVisibility)
        assertEquals("https://example.com/avatar.png", result.avatarUrl)
    }

    @Test
    fun `toUser with nickname only`() {
        val user = MemosV0User(
            createdTs = 1700000000,
            email = "test@example.com",
            id = 100,
            name = null,
            role = MemosRole.USER,
            rowStatus = MemosRowStatus.NORMAL,
            updatedTs = 1700000100,
            nickname = "Testy",
            username = "testuser",
            avatarUrl = null
        )

        val result = user.toUser()

        assertEquals("Testy", result.name)
        assertNull(result.avatarUrl)
    }

    @Test
    fun `toUser with name only`() {
        val user = MemosV0User(
            createdTs = 1700000000,
            email = "test@example.com",
            id = 100,
            name = "Test User",
            role = MemosRole.USER,
            rowStatus = MemosRowStatus.NORMAL,
            updatedTs = 1700000100,
            nickname = null,
            username = "testuser",
            avatarUrl = null
        )

        val result = user.toUser()

        assertEquals("Test User", result.name)
    }

    @Test
    fun `toUser with no name or nickname uses empty string`() {
        val user = MemosV0User(
            createdTs = 1700000000,
            email = "test@example.com",
            id = 100,
            name = null,
            role = MemosRole.USER,
            rowStatus = MemosRowStatus.NORMAL,
            updatedTs = 1700000100,
            nickname = null,
            username = "testuser",
            avatarUrl = null
        )

        val result = user.toUser()

        assertEquals("", result.name)
    }

    @Test
    fun `toUser defaults to PRIVATE visibility`() {
        val user = MemosV0User(
            createdTs = 1700000000,
            email = "test@example.com",
            id = 100,
            name = "Test",
            role = MemosRole.USER,
            rowStatus = MemosRowStatus.NORMAL,
            updatedTs = 1700000100,
            nickname = null,
            username = "testuser",
            avatarUrl = null,
            userSettingList = emptyList()
        )

        val result = user.toUser()

        assertEquals(MemoVisibility.PRIVATE, result.defaultVisibility)
    }

    @Test
    fun `toUser handles invalid visibility value`() {
        val user = MemosV0User(
            createdTs = 1700000000,
            email = "test@example.com",
            id = 100,
            name = "Test",
            role = MemosRole.USER,
            rowStatus = MemosRowStatus.NORMAL,
            updatedTs = 1700000100,
            nickname = null,
            username = "testuser",
            avatarUrl = null,
            userSettingList = listOf(
                MemosV0UserSetting(
                    key = MemosV0UserSettingKey.MEMO_VISIBILITY,
                    value = "\"INVALID\""
                )
            )
        )

        val result = user.toUser()

        assertEquals(MemoVisibility.PRIVATE, result.defaultVisibility)
    }

    @Test
    fun `displayEmail returns email when present`() {
        val user = MemosV0User(
            createdTs = 1700000000,
            email = "test@example.com",
            id = 100,
            name = null,
            role = MemosRole.USER,
            rowStatus = MemosRowStatus.NORMAL,
            updatedTs = 1700000100,
            nickname = null,
            username = "testuser",
            avatarUrl = null
        )

        assertEquals("test@example.com", user.displayEmail)
    }

    @Test
    fun `displayEmail falls back to username`() {
        val user = MemosV0User(
            createdTs = 1700000000,
            email = null,
            id = 100,
            name = null,
            role = MemosRole.USER,
            rowStatus = MemosRowStatus.NORMAL,
            updatedTs = 1700000100,
            nickname = null,
            username = "testuser",
            avatarUrl = null
        )

        assertEquals("testuser", user.displayEmail)
    }

    @Test
    fun `displayEmail returns empty string when both are null`() {
        val user = MemosV0User(
            createdTs = 1700000000,
            email = null,
            id = 100,
            name = null,
            role = MemosRole.USER,
            rowStatus = MemosRowStatus.NORMAL,
            updatedTs = 1700000100,
            nickname = null,
            username = null,
            avatarUrl = null
        )

        assertEquals("", user.displayEmail)
    }

    @Test
    fun `displayName returns nickname when present`() {
        val user = MemosV0User(
            createdTs = 1700000000,
            email = "test@example.com",
            id = 100,
            name = "Test User",
            role = MemosRole.USER,
            rowStatus = MemosRowStatus.NORMAL,
            updatedTs = 1700000100,
            nickname = "Testy",
            username = "testuser",
            avatarUrl = null
        )

        assertEquals("Testy", user.displayName)
    }

    @Test
    fun `displayName falls back to name`() {
        val user = MemosV0User(
            createdTs = 1700000000,
            email = "test@example.com",
            id = 100,
            name = "Test User",
            role = MemosRole.USER,
            rowStatus = MemosRowStatus.NORMAL,
            updatedTs = 1700000100,
            nickname = null,
            username = "testuser",
            avatarUrl = null
        )

        assertEquals("Test User", user.displayName)
    }

    @Test
    fun `displayName returns empty string when both are null`() {
        val user = MemosV0User(
            createdTs = 1700000000,
            email = "test@example.com",
            id = 100,
            name = null,
            role = MemosRole.USER,
            rowStatus = MemosRowStatus.NORMAL,
            updatedTs = 1700000100,
            nickname = null,
            username = "testuser",
            avatarUrl = null
        )

        assertEquals("", user.displayName)
    }
}

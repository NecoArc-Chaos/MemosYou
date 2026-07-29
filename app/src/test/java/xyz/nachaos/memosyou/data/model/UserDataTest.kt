package xyz.nachaos.memosyou.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class UserDataTest {

    @Test
    fun `accountCase returns MEMOS_V0 when memosV0 is set`() {
        val userData = UserData(
            accountKey = "key",
            memosV0 = MemosAccount(),
            memosV1 = null,
            local = null
        )
        assertEquals(UserData.AccountCase.MEMOS_V0, userData.accountCase)
    }

    @Test
    fun `accountCase returns MEMOS_V1 when memosV1 is set`() {
        val userData = UserData(
            accountKey = "key",
            memosV0 = null,
            memosV1 = MemosAccount(),
            local = null
        )
        assertEquals(UserData.AccountCase.MEMOS_V1, userData.accountCase)
    }

    @Test
    fun `accountCase returns LOCAL when local is set`() {
        val userData = UserData(
            accountKey = "key",
            memosV0 = null,
            memosV1 = null,
            local = LocalAccount()
        )
        assertEquals(UserData.AccountCase.LOCAL, userData.accountCase)
    }

    @Test
    fun `accountCase returns ACCOUNT_NOT_SET when all are null`() {
        val userData = UserData(
            accountKey = "key",
            memosV0 = null,
            memosV1 = null,
            local = null
        )
        assertEquals(UserData.AccountCase.ACCOUNT_NOT_SET, userData.accountCase)
    }

    @Test
    fun `accountCase prioritizes memosV0 over memosV1`() {
        val userData = UserData(
            accountKey = "key",
            memosV0 = MemosAccount(),
            memosV1 = MemosAccount(),
            local = null
        )
        assertEquals(UserData.AccountCase.MEMOS_V0, userData.accountCase)
    }

    @Test
    fun `accountCase prioritizes memosV1 over local`() {
        val userData = UserData(
            accountKey = "key",
            memosV0 = null,
            memosV1 = MemosAccount(),
            local = LocalAccount()
        )
        assertEquals(UserData.AccountCase.MEMOS_V1, userData.accountCase)
    }
}

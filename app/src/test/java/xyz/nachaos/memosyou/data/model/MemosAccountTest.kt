package xyz.nachaos.memosyou.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class MemosAccountTest {

    @Test
    fun `displayTitle returns accountLabel when present`() {
        val account = MemosAccount(
            host = "https://memos.example.com",
            accountLabel = "My Label"
        )
        assertEquals("My Label", account.displayTitle())
    }

    @Test
    fun `displayTitle returns name when accountLabel is empty`() {
        val account = MemosAccount(
            host = "https://memos.example.com",
            accountLabel = "",
            name = "Test User"
        )
        assertEquals("Test User", account.displayTitle())
    }

    @Test
    fun `displayTitle returns host when accountLabel and name are empty`() {
        val account = MemosAccount(
            host = "https://memos.example.com",
            accountLabel = "",
            name = ""
        )
        assertEquals("memos.example.com", account.displayTitle())
    }

    @Test
    fun `displayTitle returns empty string when host is empty`() {
        val account = MemosAccount(
            host = "",
            accountLabel = "",
            name = ""
        )
        assertEquals("", account.displayTitle())
    }

    @Test
    fun `displayTitle trims whitespace`() {
        val account = MemosAccount(
            host = "  https://memos.example.com  ",
            accountLabel = "  My Label  ",
            name = "  Test User  "
        )
        assertEquals("My Label", account.displayTitle())
    }

    @Test
    fun `displayTitle extracts host from URL`() {
        val account = MemosAccount(
            host = "https://sub.memos.example.com:8443",
            accountLabel = "",
            name = ""
        )
        assertEquals("sub.memos.example.com", account.displayTitle())
    }

    @Test
    fun `displayTitle falls back to raw host on invalid URL`() {
        val account = MemosAccount(
            host = "not-a-valid-url",
            accountLabel = "",
            name = ""
        )
        assertEquals("not-a-valid-url", account.displayTitle())
    }
}

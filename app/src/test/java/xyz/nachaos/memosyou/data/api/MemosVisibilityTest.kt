package xyz.nachaos.memosyou.data.api

import org.junit.Assert.assertEquals
import org.junit.Test
import xyz.nachaos.memosyou.data.model.MemoVisibility

class MemosVisibilityTest {

    @Test
    fun `PRIVATE converts to PRIVATE`() {
        assertEquals(MemoVisibility.PRIVATE, MemosVisibility.PRIVATE.toMemoVisibility())
    }

    @Test
    fun `PROTECTED converts to PROTECTED`() {
        assertEquals(MemoVisibility.PROTECTED, MemosVisibility.PROTECTED.toMemoVisibility())
    }

    @Test
    fun `PUBLIC converts to PUBLIC`() {
        assertEquals(MemoVisibility.PUBLIC, MemosVisibility.PUBLIC.toMemoVisibility())
    }

    @Test
    fun `VISIBILITY_UNSPECIFIED converts to PRIVATE`() {
        assertEquals(MemoVisibility.PRIVATE, MemosVisibility.VISIBILITY_UNSPECIFIED.toMemoVisibility())
    }

    @Test
    fun `fromMemoVisibility PRIVATE converts correctly`() {
        assertEquals(MemosVisibility.PRIVATE, MemosVisibility.fromMemoVisibility(MemoVisibility.PRIVATE))
    }

    @Test
    fun `fromMemoVisibility PROTECTED converts correctly`() {
        assertEquals(MemosVisibility.PROTECTED, MemosVisibility.fromMemoVisibility(MemoVisibility.PROTECTED))
    }

    @Test
    fun `fromMemoVisibility PUBLIC converts correctly`() {
        assertEquals(MemosVisibility.PUBLIC, MemosVisibility.fromMemoVisibility(MemoVisibility.PUBLIC))
    }
}

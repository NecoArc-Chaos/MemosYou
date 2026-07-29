package xyz.nachaos.memosyou.data.api

import org.junit.Assert.assertEquals
import org.junit.Test
import java.net.URI

class MemosV1ResourceTest {

    @Test
    fun `uri with external link returns external link`() {
        val resource = MemosV1Resource(
            name = "test-name",
            filename = "test.jpg",
            externalLink = "https://external.example.com/image.jpg"
        )
        val uri = resource.uri("https://memos.example.com")
        assertEquals("https://external.example.com/image.jpg", uri.toString())
    }

    @Test
    fun `uri with name and filename returns correct path`() {
        val resource = MemosV1Resource(
            name = "abc123",
            filename = "photo.png"
        )
        val uri = resource.uri("https://memos.example.com")
        assertEquals("https://memos.example.com/file/abc123/photo.png", uri.toString())
    }

    @Test
    fun `uri with only name returns host`() {
        val resource = MemosV1Resource(
            name = "abc123",
            filename = null
        )
        val uri = resource.uri("https://memos.example.com")
        assertEquals("https://memos.example.com", uri.toString())
    }

    @Test
    fun `uri with only filename returns host`() {
        val resource = MemosV1Resource(
            name = null,
            filename = "photo.png"
        )
        val uri = resource.uri("https://memos.example.com")
        assertEquals("https://memos.example.com", uri.toString())
    }

    @Test
    fun `uri with no name or filename returns host`() {
        val resource = MemosV1Resource()
        val uri = resource.uri("https://memos.example.com")
        assertEquals("https://memos.example.com", uri.toString())
    }

    @Test
    fun `uri handles special characters in name`() {
        val resource = MemosV1Resource(
            name = "a/b/c",
            filename = "test.txt"
        )
        val uri = resource.uri("https://memos.example.com")
        // The appendEncodedPath should encode special characters
        assertEquals("https://memos.example.com/file/a/b/c/test.txt", uri.toString())
    }
}

package xyz.nachaos.memosyou.data.api

import org.junit.Assert.assertEquals
import org.junit.Test

class MemosV0ResourceTest {

    @Test
    fun `uri with external link returns external link`() {
        val resource = MemosV0Resource(
            id = 1,
            createdTs = 1700000000,
            creatorId = 100,
            filename = "test.jpg",
            size = 1024,
            type = "image/jpeg",
            updatedTs = 1700000000,
            externalLink = "https://external.example.com/image.jpg",
            publicId = null,
            name = null,
            uid = null
        )
        val uri = resource.uri("https://memos.example.com")
        assertEquals("https://external.example.com/image.jpg", uri.toString())
    }

    @Test
    fun `uri with uid returns uid path`() {
        val resource = MemosV0Resource(
            id = 1,
            createdTs = 1700000000,
            creatorId = 100,
            filename = "test.jpg",
            size = 1024,
            type = "image/jpeg",
            updatedTs = 1700000000,
            externalLink = null,
            publicId = null,
            name = null,
            uid = "uid-123"
        )
        val uri = resource.uri("https://memos.example.com")
        assertEquals("https://memos.example.com/o/r/uid-123", uri.toString())
    }

    @Test
    fun `uri with name returns name path`() {
        val resource = MemosV0Resource(
            id = 1,
            createdTs = 1700000000,
            creatorId = 100,
            filename = "test.jpg",
            size = 1024,
            type = "image/jpeg",
            updatedTs = 1700000000,
            externalLink = null,
            publicId = null,
            name = "resource-name",
            uid = null
        )
        val uri = resource.uri("https://memos.example.com")
        assertEquals("https://memos.example.com/o/r/resource-name", uri.toString())
    }

    @Test
    fun `uri with publicId returns publicId path`() {
        val resource = MemosV0Resource(
            id = 1,
            createdTs = 1700000000,
            creatorId = 100,
            filename = "test.jpg",
            size = 1024,
            type = "image/jpeg",
            updatedTs = 1700000000,
            externalLink = null,
            publicId = "public-123",
            name = null,
            uid = null
        )
        val uri = resource.uri("https://memos.example.com")
        assertEquals("https://memos.example.com/o/r/1/public-123", uri.toString())
    }

    @Test
    fun `uri with no external link uid name or publicId returns id filename path`() {
        val resource = MemosV0Resource(
            id = 42,
            createdTs = 1700000000,
            creatorId = 100,
            filename = "document.pdf",
            size = 2048,
            type = "application/pdf",
            updatedTs = 1700000000,
            externalLink = null,
            publicId = null,
            name = null,
            uid = null
        )
        val uri = resource.uri("https://memos.example.com")
        assertEquals("https://memos.example.com/o/r/42/document.pdf", uri.toString())
    }
}

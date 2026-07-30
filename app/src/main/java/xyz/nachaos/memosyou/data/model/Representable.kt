package xyz.nachaos.memosyou.data.model

import java.time.Instant
import kotlinx.serialization.Serializable

interface ResourceRepresentable {
    val remoteId: String?
    val date: Instant
    val filename: String
    val mimeType: String?
    val uri: String
    val localUri: String?
}

interface MemoRepresentable {
    val remoteId: String?
    val content: String
    val date: Instant
    val pinned: Boolean
    val visibility: MemoVisibility
    val resources: List<ResourceRepresentable>
    val archived: Boolean
    val relations: List<MemoRelation>
    val location: MemoLocation?
}

@Serializable
data class MemoRelation(
    val memo: MemoRelationRef,
    val relatedMemo: MemoRelationRef,
    val type: RelationType
)

@Serializable
data class MemoRelationRef(
    val name: String,
    val snippet: String
)

@Serializable
enum class RelationType {
    REFERENCE, COMMENT, UNKNOWN
}

@Serializable
data class MemoLocation(
    val placeholder: String?,
    val latitude: Double?,
    val longitude: Double?
)

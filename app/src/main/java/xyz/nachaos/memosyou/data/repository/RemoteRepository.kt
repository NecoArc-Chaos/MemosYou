package xyz.nachaos.memosyou.data.repository

import com.skydoves.sandwich.ApiResponse
import xyz.nachaos.memosyou.data.api.MemosV1MemoShare
import xyz.nachaos.memosyou.data.model.Memo
import xyz.nachaos.memosyou.data.model.MemoRelation
import xyz.nachaos.memosyou.data.model.MemoVisibility
import xyz.nachaos.memosyou.data.model.Resource
import xyz.nachaos.memosyou.data.model.User
import okhttp3.MediaType
import java.io.InputStream
import java.time.Instant

abstract class RemoteRepository {
    abstract suspend fun listMemos(): ApiResponse<List<Memo>>
    abstract suspend fun listArchivedMemos(): ApiResponse<List<Memo>>
    abstract suspend fun listWorkspaceMemos(pageSize: Int, pageToken: String?): ApiResponse<Pair<List<Memo>, String?>>

    abstract suspend fun createMemo(
        content: String,
        visibility: MemoVisibility,
        resourceRemoteIds: List<String>,
        tags: List<String>? = null,
        createdAt: Instant? = null
    ): ApiResponse<Memo>

    abstract suspend fun updateMemo(
        remoteId: String,
        content: String? = null,
        resourceRemoteIds: List<String>? = null,
        visibility: MemoVisibility? = null,
        tags: List<String>? = null,
        pinned: Boolean? = null,
        archived: Boolean? = null
    ): ApiResponse<Memo>

    abstract suspend fun deleteMemo(remoteId: String): ApiResponse<Unit>

    abstract suspend fun listResources(): ApiResponse<List<Resource>>

    abstract suspend fun createResource(
        filename: String,
        type: MediaType?,
        contentLength: Long?,
        openInputStream: () -> InputStream,
        memoRemoteId: String? = null
    ): ApiResponse<Resource>

    abstract suspend fun deleteResource(remoteId: String): ApiResponse<Unit>
    abstract suspend fun getCurrentUser(): ApiResponse<User>

    abstract suspend fun listMemoComments(memoName: String, pageSize: Int?, pageToken: String?): ApiResponse<Pair<List<Memo>, String?>>
    abstract suspend fun createMemoComment(memoName: String, content: String): ApiResponse<Memo>
    abstract suspend fun getMemo(memoName: String): ApiResponse<Memo>
    abstract suspend fun getSharedMemo(shareToken: String): ApiResponse<Memo>
    abstract suspend fun createMemoShare(parentMemoName: String): ApiResponse<Unit>
    abstract suspend fun listMemoShares(parentMemoName: String): ApiResponse<List<MemosV1MemoShare>>
    abstract suspend fun deleteMemoShare(shareName: String): ApiResponse<Unit>
    abstract suspend fun setMemoRelations(memoName: String, relations: List<MemoRelation>): ApiResponse<Unit>
    abstract suspend fun listMemoRelations(memoName: String, pageSize: Int?, pageToken: String?): ApiResponse<Pair<List<MemoRelation>, String?>>
}

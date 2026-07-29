package xyz.nachaos.memosyou.data.repository

import android.net.Uri
import com.skydoves.sandwich.ApiResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import xyz.nachaos.memosyou.data.local.entity.MemoEntity
import xyz.nachaos.memosyou.data.local.entity.ResourceEntity
import xyz.nachaos.memosyou.data.model.Memo
import xyz.nachaos.memosyou.data.model.MemoRelation
import xyz.nachaos.memosyou.data.model.MemoVisibility
import xyz.nachaos.memosyou.data.model.SyncStatus
import xyz.nachaos.memosyou.data.model.User
import xyz.nachaos.memosyou.data.api.MemosV1MemoShare
import okhttp3.MediaType

/**
 * Abstract base class for memo repositories.
 *
 * Subclasses that support syncing MUST override [syncStatus] to provide
 * their own sync status flow. The default implementation returns a
 * static idle status for repositories that don't sync (e.g. local-only).
 */
abstract class AbstractMemoRepository {
    /**
     * Sync status for this repository. Override in syncing repositories
     * to provide live sync state. Non-syncing repositories return idle.
     */
    open val syncStatus: StateFlow<SyncStatus> = MutableStateFlow(SyncStatus()).asStateFlow()

    abstract suspend fun listMemos(): ApiResponse<List<MemoEntity>>
    abstract suspend fun listArchivedMemos(): ApiResponse<List<MemoEntity>>
    abstract suspend fun createMemo(content: String, visibility: MemoVisibility, resources: List<ResourceEntity>, tags: List<String>? = null): ApiResponse<MemoEntity>
    abstract suspend fun updateMemo(identifier: String, content: String? = null, resources: List<ResourceEntity>? = null, visibility: MemoVisibility? = null, tags: List<String>? = null, pinned: Boolean? = null): ApiResponse<MemoEntity>
    abstract suspend fun deleteMemo(identifier: String): ApiResponse<Unit>
    abstract suspend fun archiveMemo(identifier: String): ApiResponse<Unit>
    abstract suspend fun restoreMemo(identifier: String): ApiResponse<Unit>

    abstract suspend fun listTags(): ApiResponse<List<String>>

    abstract suspend fun listResources(): ApiResponse<List<ResourceEntity>>
    abstract suspend fun createResource(filename: String, type: MediaType?, contentUri: Uri, memoIdentifier: String? = null): ApiResponse<ResourceEntity>
    abstract suspend fun deleteResource(identifier: String): ApiResponse<Unit>

    abstract suspend fun getCurrentUser(): ApiResponse<User>

    open suspend fun getMemo(memoName: String): ApiResponse<Memo> {
        return ApiResponse.exception(RuntimeException("Get memo detail not supported"))
    }

    open suspend fun listMemoComments(memoName: String, pageSize: Int?, pageToken: String?): ApiResponse<Pair<List<Memo>, String?>> {
        return ApiResponse.Success(emptyList<Memo>() to null)
    }
    open suspend fun createMemoComment(memoName: String, content: String): ApiResponse<Memo> {
        return ApiResponse.exception(RuntimeException("Comments not supported"))
    }

    open suspend fun getSharedMemo(shareToken: String): ApiResponse<Memo> {
        return ApiResponse.exception(RuntimeException("Shared memo not supported"))
    }

    open suspend fun createMemoShare(parentMemoName: String): ApiResponse<Unit> {
        return ApiResponse.exception(RuntimeException("Shared memo not supported"))
    }

    open suspend fun listMemoShares(parentMemoName: String): ApiResponse<List<MemosV1MemoShare>> {
        return ApiResponse.Success(emptyList())
    }

    open suspend fun deleteMemoShare(shareName: String): ApiResponse<Unit> {
        return ApiResponse.exception(RuntimeException("Shared memo not supported"))
    }

    open suspend fun setMemoRelations(memoName: String, relations: List<MemoRelation>): ApiResponse<Unit> {
        return ApiResponse.exception(RuntimeException("Relations not supported"))
    }

    open suspend fun listMemoRelations(memoName: String, pageSize: Int?, pageToken: String?): ApiResponse<Pair<List<MemoRelation>, String?>> {
        return ApiResponse.Success(emptyList<MemoRelation>() to null)
    }

    open fun observeMemos(): Flow<List<MemoEntity>> = emptyFlow()

    open suspend fun cacheResourceFile(identifier: String, downloadedUri: Uri): ApiResponse<Unit> {
        return ApiResponse.Success(Unit)
    }

    open suspend fun sync(): ApiResponse<Unit> {
        return ApiResponse.Success(Unit)
    }

    open fun close() = Unit
}

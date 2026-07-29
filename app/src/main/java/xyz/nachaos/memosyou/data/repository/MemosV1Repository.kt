package xyz.nachaos.memosyou.data.repository

import com.skydoves.sandwich.ApiResponse
import com.skydoves.sandwich.getOrNull
import com.skydoves.sandwich.mapSuccess
import com.skydoves.sandwich.onSuccess
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import xyz.nachaos.memosyou.data.api.MemosV1Api
import xyz.nachaos.memosyou.data.api.MemosV1CreateMemoRequest
import xyz.nachaos.memosyou.data.api.MemosV1CreateMemoShareRequest
import xyz.nachaos.memosyou.data.api.MemosV1ListMemoSharesResponse
import xyz.nachaos.memosyou.data.api.MemosV1ListMemoRelationsResponse
import xyz.nachaos.memosyou.data.api.MemosV1Location
import xyz.nachaos.memosyou.data.api.MemosV1Memo
import xyz.nachaos.memosyou.data.api.MemosV1MemoRelation
import xyz.nachaos.memosyou.data.api.MemosV1MemoShare
import xyz.nachaos.memosyou.data.api.MemosV1RelationMemo
import xyz.nachaos.memosyou.data.api.MemosV1RelationType
import xyz.nachaos.memosyou.data.api.MemosV1Resource
import xyz.nachaos.memosyou.data.api.MemosV1SetMemoRelationsRequest
import xyz.nachaos.memosyou.data.api.MemosV1State
import xyz.nachaos.memosyou.data.api.MemosVisibility
import xyz.nachaos.memosyou.data.api.UpdateMemoRequest
import xyz.nachaos.memosyou.data.api.CreateMemoCommentBody
import xyz.nachaos.memosyou.data.api.InstanceSettingResponse
import xyz.nachaos.memosyou.data.constant.MoeMemosException
import xyz.nachaos.memosyou.data.model.Account
import xyz.nachaos.memosyou.data.model.Memo
import xyz.nachaos.memosyou.data.model.MemoLocation
import xyz.nachaos.memosyou.data.model.MemoRelation
import xyz.nachaos.memosyou.data.model.MemoRelationRef
import xyz.nachaos.memosyou.data.model.MemoVisibility
import xyz.nachaos.memosyou.data.model.RelationType
import xyz.nachaos.memosyou.data.model.Resource
import xyz.nachaos.memosyou.data.model.User
import okhttp3.MediaType
import java.time.Instant

private const val PAGE_SIZE = 200
private const val USER_CONCURRENT_LIMIT = 5
private const val MAX_PAGINATION_PAGES = 50 // 50 * 200 = 10,000 memos max

class MemosV1Repository(
    internal val memosApi: MemosV1Api,
    private val account: Account.MemosV1
): RemoteRepository() {
    private val remoteUserIdentifier = account.info.remoteIdentifier

    private fun convertResource(resource: MemosV1Resource): Resource {
        return Resource(
            remoteId = requireNotNull(resource.name),
            date = resource.createTime ?: Instant.now(),
            filename = resource.filename ?: "",
            uri = resource.uri(account.info.host).toString(),
            mimeType = resource.type
        )
    }

    private fun convertMemo(memo: MemosV1Memo): Memo {
        return Memo(
            remoteId = memo.name,
            content = memo.content ?: "",
            date = memo.createTime ?: Instant.now(),
            pinned = memo.pinned ?: false,
            visibility = memo.visibility?.toMemoVisibility() ?: MemoVisibility.PRIVATE,
            resources = memo.attachments?.map { convertResource(it) } ?: emptyList(),
            tags = emptyList(),
            archived = memo.state == MemosV1State.ARCHIVED,
            relations = memo.relations?.map { convertRelation(it) } ?: emptyList(),
            location = memo.location?.let { convertLocation(it) },
            updatedAt = memo.updateTime
        )
    }

    private fun convertRelation(relation: MemosV1MemoRelation): MemoRelation {
        return MemoRelation(
            memo = MemoRelationRef(
                name = relation.memo?.name.orEmpty(),
                snippet = relation.memo?.snippet.orEmpty()
            ),
            relatedMemo = MemoRelationRef(
                name = relation.relatedMemo?.name.orEmpty(),
                snippet = relation.relatedMemo?.snippet.orEmpty()
            ),
            type = when (relation.type) {
                MemosV1RelationType.REFERENCE -> RelationType.REFERENCE
                MemosV1RelationType.COMMENT -> RelationType.COMMENT
                else -> RelationType.UNKNOWN
            }
        )
    }

    private fun convertLocation(location: MemosV1Location): MemoLocation {
        return MemoLocation(
            placeholder = location.placeholder,
            latitude = location.latitude,
            longitude = location.longitude
        )
    }

    private suspend fun listMemosByFilter(state: MemosV1State, filter: String): ApiResponse<List<Memo>> {
        var nextPageToken = ""
        val memos = arrayListOf<Memo>()
        var pageCount = 0

        do {
            if (pageCount >= MAX_PAGINATION_PAGES) {
                return ApiResponse.Failure.Exception(
                    RuntimeException("Exceeded maximum pagination limit ($MAX_PAGINATION_PAGES pages)")
                )
            }
            pageCount++

            val resp = memosApi.listMemos(PAGE_SIZE, nextPageToken, state, filter)
                .onSuccess { nextPageToken = data.nextPageToken.orEmpty() }
                .mapSuccess { this.memos.map { convertMemo(it) } }
            if (resp is ApiResponse.Success) {
                memos.addAll(resp.data)
            } else {
                return resp
            }
        } while (nextPageToken.isNotEmpty())
        return ApiResponse.Success(memos)
    }

    private fun getId(identifier: String): String {
        return identifier.substringBefore('|').substringAfterLast('/')
    }

    private fun getName(identifier: String): String {
        return identifier.substringBefore('|')
    }

    private suspend fun listCurrentUserMemos(state: MemosV1State): ApiResponse<List<Memo>> {
        return listMemosByFilter(state, "creator == \"$remoteUserIdentifier\"")
    }

    override suspend fun listMemos(): ApiResponse<List<Memo>> {
        return listCurrentUserMemos(MemosV1State.NORMAL)
    }

    override suspend fun listArchivedMemos(): ApiResponse<List<Memo>> {
        return listCurrentUserMemos(MemosV1State.ARCHIVED)
    }

    override suspend fun listWorkspaceMemos(
        pageSize: Int,
        pageToken: String?
    ): ApiResponse<Pair<List<Memo>, String?>> {
        val resp = memosApi.listMemos(pageSize, pageToken, filter = "visibility in [\"PUBLIC\", \"PROTECTED\"]")
        if (resp !is ApiResponse.Success) {
            return resp.mapSuccess { emptyList<Memo>() to null }
        }
        val users = resp.data.memos.mapNotNull { it.creator }.map { getId(it) }.toSet()
        val semaphore = Semaphore(USER_CONCURRENT_LIMIT)
        val userResp = coroutineScope {
            users.map { userId ->
                async {
                    semaphore.withPermit {
                        memosApi.getUser(userId).getOrNull()
                    }
                }
            }.awaitAll()
        }.filterNotNull()
        val userMap = mapOf(*userResp.map { user -> user.name to user }.toTypedArray())

        return resp
            .mapSuccess { this.memos.map {
                convertMemo(it).copy(
                    creator = it.creator?.let { creator ->
                        userMap[creator]?.let { user ->
                            User(
                                user.name,
                                user.displayName ?: user.username,
                                user.createTime ?: Instant.now(),
                                avatarUrl = user.avatarUrl
                            )
                        }
                    }
                )
            } to this.nextPageToken?.ifEmpty { null } }
    }

    override suspend fun createMemo(
        content: String,
        visibility: MemoVisibility,
        resourceRemoteIds: List<String>,
        tags: List<String>?,
        createdAt: Instant?
    ): ApiResponse<Memo> {
        val resp = memosApi.createMemo(
            MemosV1CreateMemoRequest(
                content = content,
                visibility = MemosVisibility.fromMemoVisibility(visibility),
                attachments = resourceRemoteIds.map { MemosV1Resource(name = getName(it)) },
                createTime = createdAt
            )
        )
            .mapSuccess { convertMemo(this) }
        return resp
    }

    override suspend fun updateMemo(
        remoteId: String,
        content: String?,
        resourceRemoteIds: List<String>?,
        visibility: MemoVisibility?,
        tags: List<String>?,
        pinned: Boolean?,
        archived: Boolean?
    ): ApiResponse<Memo> {
        val resp = memosApi.updateMemo(getId(remoteId), UpdateMemoRequest(
            content = content,
            visibility = visibility?.let { MemosVisibility.fromMemoVisibility(it) },
            pinned = pinned,
            state = archived?.let { isArchived -> if (isArchived) MemosV1State.ARCHIVED else MemosV1State.NORMAL },
            updateTime = Instant.now(),
            attachments = resourceRemoteIds?.map { MemosV1Resource(name = getName(it)) }
        )).mapSuccess { convertMemo(this) }
        return resp
    }

    override suspend fun deleteMemo(remoteId: String): ApiResponse<Unit> {
        return memosApi.deleteMemo(getId(remoteId))
    }

    override suspend fun listResources(): ApiResponse<List<Resource>> {
        return memosApi.listResources().mapSuccess { this.attachments.map { convertResource(it) } }
    }

    override suspend fun createResource(
        filename: String,
        type: MediaType?,
        contentLength: Long?,
        openInputStream: () -> java.io.InputStream,
        memoRemoteId: String?
    ): ApiResponse<Resource> {
        val requestBody = StreamingBase64JsonRequestBody(
            filename = filename,
            type = type?.toString() ?: "application/octet-stream",
            memo = memoRemoteId?.let { getName(it) },
            contentLength = contentLength,
            openInputStream = openInputStream
        )
        return memosApi.createResource(requestBody).mapSuccess { convertResource(this) }
    }

    override suspend fun deleteResource(remoteId: String): ApiResponse<Unit> {
        return memosApi.deleteResource(getId(remoteId))
    }

    override suspend fun getCurrentUser(): ApiResponse<User> {
        val resp = memosApi.getCurrentUser().mapSuccess {
            if (user == null) {
                throw MoeMemosException.notLogin
            }
            User(
                user.name,
                user.displayName ?: user.username,
                user.createTime ?: Instant.now(),
                avatarUrl = user.avatarUrl
            )
        }
        if (resp !is ApiResponse.Success) {
            return resp
        }

        return memosApi.getUserSetting(getId(resp.data.identifier)).mapSuccess {
            resp.data.copy(
                defaultVisibility = generalSetting?.memoVisibility?.toMemoVisibility() ?: MemoVisibility.PRIVATE
            )
        }
    }

    // ─── Comments ───
    override suspend fun listMemoComments(memoName: String, pageSize: Int?, pageToken: String?): ApiResponse<Pair<List<Memo>, String?>> {
        val resp = memosApi.listMemoComments(name = memoName, pageSize = pageSize, pageToken = pageToken)
        if (resp !is ApiResponse.Success) {
            return resp.mapSuccess { emptyList<Memo>() to null }
        }
        // Fetch user info for comment creators with bounded concurrency.
        val users = resp.data.memos.mapNotNull { it.creator }.map { getId(it) }.toSet()
        val semaphore = Semaphore(USER_CONCURRENT_LIMIT)
        val userResp = coroutineScope {
            users.map { userId ->
                async {
                    semaphore.withPermit {
                        memosApi.getUser(userId).getOrNull()
                    }
                }
            }.awaitAll()
        }.filterNotNull()
        val userMap = mapOf(*userResp.map { user -> user.name to user }.toTypedArray())

        return resp.mapSuccess {
            this.memos.map { memo ->
                convertMemo(memo).copy(
                    creator = memo.creator?.let { creator ->
                        userMap[creator]?.let { user ->
                            User(
                                user.name,
                                user.displayName ?: user.username,
                                user.createTime ?: Instant.now(),
                                avatarUrl = user.avatarUrl
                            )
                        }
                    }
                )
            } to this.nextPageToken?.ifEmpty { null }
        }
    }

    override suspend fun createMemoComment(memoName: String, content: String): ApiResponse<Memo> {
        return memosApi.createMemoComment(
            name = memoName,
            body = CreateMemoCommentBody(content = content)
        ).mapSuccess { convertMemo(this) }
    }

    // ─── Instance Settings ───
    suspend fun getInstanceSetting(name: String): ApiResponse<InstanceSettingResponse> {
        return memosApi.getInstanceSetting(name)
    }

    override suspend fun getMemo(memoName: String): ApiResponse<Memo> {
        return memosApi.getMemo(memoName).mapSuccess { convertMemo(this) }
    }

    // ─── Shared Memo ───
    override suspend fun getSharedMemo(shareToken: String): ApiResponse<Memo> {
        return memosApi.getSharedMemo(shareToken).mapSuccess { convertMemo(this) }
    }

    override suspend fun createMemoShare(parentMemoName: String): ApiResponse<Unit> {
        memosApi.createMemoShare(parentMemoName, MemosV1CreateMemoShareRequest(MemosV1MemoShare()))
        return ApiResponse.Success(Unit)
    }

    override suspend fun listMemoShares(parentMemoName: String): ApiResponse<List<MemosV1MemoShare>> {
        return memosApi.listMemoShares(parentMemoName).mapSuccess { this.memoShares.orEmpty() }
    }

    override suspend fun deleteMemoShare(shareName: String): ApiResponse<Unit> {
        return memosApi.deleteMemoShare(shareName)
    }

    // ─── Relations ───
    override suspend fun setMemoRelations(memoName: String, relations: List<MemoRelation>): ApiResponse<Unit> {
        val apiRelations = relations.map {
            MemosV1MemoRelation(
                memo = MemosV1RelationMemo(name = it.memo.name, snippet = it.memo.snippet),
                relatedMemo = MemosV1RelationMemo(name = it.relatedMemo.name, snippet = it.relatedMemo.snippet),
                type = when (it.type) {
                    RelationType.REFERENCE -> MemosV1RelationType.REFERENCE
                    RelationType.COMMENT -> MemosV1RelationType.COMMENT
                    else -> MemosV1RelationType.TYPE_UNSPECIFIED
                }
            )
        }
        return memosApi.setMemoRelations(memoName, MemosV1SetMemoRelationsRequest(relations = apiRelations))
    }

    override suspend fun listMemoRelations(memoName: String, pageSize: Int?, pageToken: String?): ApiResponse<Pair<List<MemoRelation>, String?>> {
        return memosApi.listMemoRelations(memoName, pageSize, pageToken).mapSuccess {
            val relations = this.relations?.map { relation ->
                MemoRelation(
                    memo = MemoRelationRef(
                        name = relation.memo?.name.orEmpty(),
                        snippet = relation.memo?.snippet.orEmpty()
                    ),
                    relatedMemo = MemoRelationRef(
                        name = relation.relatedMemo?.name.orEmpty(),
                        snippet = relation.relatedMemo?.snippet.orEmpty()
                    ),
                    type = when (relation.type) {
                        MemosV1RelationType.REFERENCE -> RelationType.REFERENCE
                        MemosV1RelationType.COMMENT -> RelationType.COMMENT
                        else -> RelationType.UNKNOWN
                    }
                )
            } ?: emptyList()
            relations to this.nextPageToken
        }
    }
}

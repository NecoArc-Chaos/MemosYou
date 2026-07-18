package xyz.nachaos.memosyou.data.datasource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.skydoves.sandwich.ApiResponse
import com.skydoves.sandwich.getOrThrow
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import xyz.nachaos.memosyou.data.model.Memo
import xyz.nachaos.memosyou.data.repository.MemosV1Repository
import xyz.nachaos.memosyou.data.repository.RemoteRepository
import xyz.nachaos.memosyou.ui.component.memoCommentName

const val EXPLORE_PAGE_SIZE = 20

class ExplorePagingSource(
    private val remoteRepository: RemoteRepository
) : PagingSource<String, Memo>() {

    override fun getRefreshKey(state: PagingState<String, Memo>): String? {
        return null
    }

    override suspend fun load(params: LoadParams<String>): LoadResult<String, Memo> {
        val response = remoteRepository.listWorkspaceMemos(EXPLORE_PAGE_SIZE, params.key)

        return try {
            val memosWithCounts = when (response) {
                is ApiResponse.Success -> {
                    val (memos, nextPageToken) = response.data
                    // Fetch comment counts in parallel
                    coroutineScope {
                        memos.map { memo ->
                            async {
                                if (remoteRepository is MemosV1Repository) {
                                    val name = memoCommentName(memo.remoteId)
                                    val commentResp = remoteRepository.memosApi.listMemoComments(name, pageSize = 1, pageToken = null)
                                    val count = if (commentResp is ApiResponse.Success) {
                                        commentResp.data.memos.size
                                    } else 0
                                    memo.copy(commentCount = count)
                                } else {
                                    memo
                                }
                            }
                        }.awaitAll()
                    }.let { memosWithCounts ->
                        LoadResult.Page(
                            data = memosWithCounts,
                            prevKey = null,
                            nextKey = nextPageToken,
                        )
                    }
                }
                is ApiResponse.Failure -> LoadResult.Error(Exception(response.toString()))
                else -> LoadResult.Error(Exception("Unknown response type"))
            }
            memosWithCounts
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}

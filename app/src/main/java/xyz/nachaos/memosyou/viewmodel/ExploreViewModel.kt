package xyz.nachaos.memosyou.viewmodel

import androidx.compose.runtime.compositionLocalOf
import com.skydoves.sandwich.ApiResponse
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import xyz.nachaos.memosyou.data.datasource.EXPLORE_PAGE_SIZE
import xyz.nachaos.memosyou.data.datasource.ExplorePagingSource
import xyz.nachaos.memosyou.data.model.Account
import xyz.nachaos.memosyou.data.model.Memo
import xyz.nachaos.memosyou.data.service.AccountService
import xyz.nachaos.memosyou.data.api.ApiResponse
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ExploreViewModel @Inject constructor(
    private val accountService: AccountService
) : ViewModel() {

    private val _commentCache = mutableStateMapOf<String, List<Memo>>()
    val commentCache: Map<String, List<Memo>> = _commentCache

    private val _commentCountCache = mutableStateMapOf<String, Int>()
    val commentCountCache: Map<String, Int> = _commentCountCache

    private val _reactionCache = mutableStateMapOf<String, List<xyz.nachaos.memosyou.data.api.ReactionItem>>()
    val reactionCache: Map<String, List<xyz.nachaos.memosyou.data.api.ReactionItem>> = _reactionCache

    private val _loadedMemos = mutableSetOf<String>()

    fun cacheComments(memoId: String, comments: List<Memo>) {
        _commentCache[memoId] = comments
        _commentCountCache[memoId] = comments.size
    }

    fun getComments(memoId: String): List<Memo> {
        return _commentCache[memoId] ?: emptyList()
    }

    fun getCommentCount(memoId: String): Int {
        return _commentCountCache[memoId] ?: 0
    }

    fun cacheReactions(memoId: String, reactions: List<xyz.nachaos.memosyou.data.api.ReactionItem>) {
        _reactionCache[memoId] = reactions
    }

    fun getReactions(memoId: String): List<xyz.nachaos.memosyou.data.api.ReactionItem> {
        return _reactionCache[memoId] ?: emptyList()
    }

    /**
     * Preload comment count + reactions for a memo in the background.
     * Only runs once per memoId unless force=true.
     */
    fun preloadMemoData(memoId: String, force: Boolean = false) {
        if (!force && memoId in _loadedMemos) return
        _loadedMemos.add(memoId)

        viewModelScope.launch {
            try {
                val repo = accountService.getRepository()
                // Load comment count
                val commentResp = repo.listMemoComments(memoId, pageSize = 1, pageToken = null)
                if (commentResp is ApiResponse.Success) {
                    val count = commentResp.data.first.size
                    _commentCountCache[memoId] = if (commentResp.data.second != null) count + 1 else count
                }
            } catch (_: Exception) { }
        }
    }

    val exploreMemos = accountService.currentAccount
        .flatMapLatest { account ->
            if (account == null || account is Account.Local) {
                return@flatMapLatest flowOf(PagingData.empty<Memo>())
            }

            val remoteRepository = accountService.getRemoteRepository()
                ?: return@flatMapLatest flowOf(PagingData.empty<Memo>())

            Pager(PagingConfig(pageSize = EXPLORE_PAGE_SIZE)) {
                ExplorePagingSource(remoteRepository)
            }.flow
        }
        .cachedIn(viewModelScope)
}

val LocalExploreViewModel = compositionLocalOf<ExploreViewModel> {
    error("ExploreViewModel not found")
}

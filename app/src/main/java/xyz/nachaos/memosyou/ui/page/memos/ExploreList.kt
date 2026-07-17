package xyz.nachaos.memosyou.ui.page.memos

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import xyz.nachaos.memosyou.ui.component.ExploreMemoCard
import xyz.nachaos.memosyou.ui.component.ExpressiveSkeletonCard
import xyz.nachaos.memosyou.ui.component.ExpressiveWaveProgress
import xyz.nachaos.memosyou.ui.util.edgeToEdgeContentPadding
import xyz.nachaos.memosyou.viewmodel.ExploreViewModel
import xyz.nachaos.memosyou.viewmodel.LocalExploreViewModel

@Composable
fun ExploreList(
    viewModel: ExploreViewModel = hiltViewModel(),
    contentPadding: PaddingValues
) {
    val memos = viewModel.exploreMemos.collectAsLazyPagingItems()
    val listContentPadding = edgeToEdgeContentPadding(contentPadding)
    val isLoading = memos.loadState.refresh is LoadState.Loading && memos.itemCount == 0

    CompositionLocalProvider(LocalExploreViewModel provides viewModel) {
        Box(Modifier.fillMaxSize().consumeWindowInsets(contentPadding)) {
            if (isLoading) {
                LazyColumn(contentPadding = listContentPadding) {
                    items(5) { ExpressiveSkeletonCard() }
                }
                ExpressiveWaveProgress(Modifier.align(Alignment.TopCenter).padding(top = 2.dp))
            } else {
                LazyColumn(contentPadding = listContentPadding) {
                    items(memos.itemCount, key = { index ->
                        memos[index]?.remoteId ?: index
                    }) { index ->
                        memos[index]?.let { ExploreMemoCard(it) }
                    }
                }
            }
        }
    }
}

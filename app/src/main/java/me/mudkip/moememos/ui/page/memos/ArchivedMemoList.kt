package xyz.nachaos.memosyou.ui.page.memos

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import xyz.nachaos.memosyou.ui.util.edgeToEdgeContentPadding
import xyz.nachaos.memosyou.ui.component.ArchivedMemoCard
import xyz.nachaos.memosyou.viewmodel.ArchivedMemoListViewModel
import xyz.nachaos.memosyou.viewmodel.LocalArchivedMemos

@Composable
fun ArchivedMemoList(
    viewModel: ArchivedMemoListViewModel = hiltViewModel(),
    contentPadding: PaddingValues
) {
    val listContentPadding = edgeToEdgeContentPadding(contentPadding)

    CompositionLocalProvider(LocalArchivedMemos provides viewModel) {
        LazyColumn(
            modifier = Modifier.consumeWindowInsets(contentPadding),
            contentPadding = listContentPadding
        ) {
            items(viewModel.memos, key = { it.identifier }) { memo ->
                ArchivedMemoCard(memo)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadMemos()
    }
}
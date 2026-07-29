package xyz.nachaos.memosyou.ui.page.memos

import android.text.format.DateUtils
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import com.skydoves.sandwich.suspendOnSuccess
import com.skydoves.sandwich.suspendOnError
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Row
import xyz.nachaos.memosyou.R
import xyz.nachaos.memosyou.data.model.Account
import xyz.nachaos.memosyou.data.model.Memo
import xyz.nachaos.memosyou.ext.getErrorMessage
import xyz.nachaos.memosyou.ext.popBackStackIfLifecycleIsResumed
import xyz.nachaos.memosyou.ext.string
import xyz.nachaos.memosyou.ui.component.MemoContent
import xyz.nachaos.memosyou.viewmodel.LocalMemos
import xyz.nachaos.memosyou.viewmodel.LocalUserState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedMemoPage(
    navController: NavHostController,
    shareToken: String
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val layoutDirection = LocalLayoutDirection.current
    val memosViewModel = LocalMemos.current
    val userStateViewModel = LocalUserState.current
    val scope = rememberCoroutineScope()
    var memo by remember { mutableStateOf<Memo?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(shareToken) {
        val remoteRepo = memosViewModel.getRemoteRepository()
        if (remoteRepo != null) {
            val result = remoteRepo.getSharedMemo(shareToken)
            result.suspendOnSuccess {
                memo = data
            }
            result.suspendOnError {
                error = getErrorMessage() ?: "Failed to load shared memo"
            }
        } else {
            error = "Shared memo requires a remote account"
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = R.string.shared_memo.string) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStackIfLifecycleIsResumed(lifecycleOwner) }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = R.string.back.string)
                    }
                }
            )
        }
    ) { innerPadding ->
        if (error != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = error ?: "Unknown error")
            }
            return@Scaffold
        }

        if (memo == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = R.string.loading.string)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .consumeWindowInsets(innerPadding)
                .padding(
                    start = innerPadding.calculateStartPadding(layoutDirection),
                    top = innerPadding.calculateTopPadding(),
                    end = innerPadding.calculateEndPadding(layoutDirection)
                )
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .padding(start = 20.dp, top = 16.dp, end = 20.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    DateUtils.getRelativeTimeSpanString(
                        memo!!.date.toEpochMilli(),
                        System.currentTimeMillis(),
                        DateUtils.SECOND_IN_MILLIS
                    ).toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            MemoContent(
                memo = memo!!,
                selectable = true,
                checkboxChange = { _, _, _ -> }
            )

            Spacer(modifier = Modifier.height(innerPadding.calculateBottomPadding()))
        }
    }
}

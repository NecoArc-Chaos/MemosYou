package xyz.nachaos.memosyou.ui.page.memos

import android.text.format.DateUtils
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Computer
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import xyz.nachaos.memosyou.R
import xyz.nachaos.memosyou.data.model.Account
import xyz.nachaos.memosyou.ext.icon
import xyz.nachaos.memosyou.ext.popBackStackIfLifecycleIsResumed
import xyz.nachaos.memosyou.ext.string
import xyz.nachaos.memosyou.ext.titleResource
import xyz.nachaos.memosyou.ui.component.MemoContent
import xyz.nachaos.memosyou.ui.component.MemosCardActionButton
import xyz.nachaos.memosyou.viewmodel.LocalMemos
import xyz.nachaos.memosyou.viewmodel.LocalUserState
import xyz.nachaos.memosyou.data.local.entity.MemoEntity
import xyz.nachaos.memosyou.data.model.RelationType
import com.skydoves.sandwich.getOrNull

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoDetailPage(
    navController: NavHostController,
    memoIdentifier: String
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val layoutDirection = LocalLayoutDirection.current
    val memosViewModel = LocalMemos.current
    val userStateViewModel = LocalUserState.current
    val currentAccount by userStateViewModel.currentAccount.collectAsState()
    val scope = rememberCoroutineScope()
    var memo by rememberSaveable(memoIdentifier) { mutableStateOf<MemoEntity?>(null) }
    var hadMemo by rememberSaveable(memoIdentifier) { mutableStateOf(false) }

    LaunchedEffect(memoIdentifier) {
        val localMemo = memosViewModel.memos.firstOrNull { it.identifier == memoIdentifier }
        if (localMemo != null) {
            memo = localMemo
            hadMemo = true
        } else if (hadMemo) {
            navController.popBackStackIfLifecycleIsResumed(lifecycleOwner)
        }
    }

    LaunchedEffect(memo?.identifier) {
        val currentMemo = memo ?: return@LaunchedEffect
        val remoteRepo = memosViewModel.getRemoteRepository()
        if (remoteRepo != null && currentMemo.remoteId != null) {
            val remoteMemo = remoteRepo.getMemo(currentMemo.remoteId).getOrNull()
            if (remoteMemo != null && (remoteMemo.relations.isNotEmpty() || remoteMemo.location != null)) {
                memo = currentMemo.copy(
                    relations = remoteMemo.relations,
                    location = remoteMemo.location
                )
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = R.string.memo.string) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStackIfLifecycleIsResumed(lifecycleOwner) }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = R.string.back.string)
                    }
                },
                actions = {
                    memo?.let { MemosCardActionButton(it) }
                }
            )
        }
    ) { innerPadding ->
        if (memo == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = R.string.memo_not_found.string)
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
            val currentMemo = memo ?: return@Column
            Row(
                modifier = Modifier
                    .padding(start = 20.dp, top = 16.dp, end = 20.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    DateUtils.getRelativeTimeSpanString(
                        currentMemo.date.toEpochMilli(),
                        System.currentTimeMillis(),
                        DateUtils.SECOND_IN_MILLIS
                    ).toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (currentAccount !is Account.Local && currentMemo.needsSync) {
                    Icon(
                        imageVector = Icons.Outlined.CloudOff,
                        contentDescription = R.string.memo_sync_pending.string,
                        modifier = Modifier
                            .padding(start = 5.dp)
                            .size(20.dp),
                    )
                }
                if (userStateViewModel.currentUser?.defaultVisibility != currentMemo.visibility) {
                    Icon(
                        imageVector = currentMemo.visibility.icon,
                        contentDescription = stringResource(currentMemo.visibility.titleResource),
                        modifier = Modifier
                            .padding(start = 5.dp)
                            .size(20.dp)
                    )
                }
            }

            MemoContent(
                memo = currentMemo,
                selectable = true,
                checkboxChange = { checked, startOffset, endOffset ->
                    scope.launch {
                        var text = currentMemo.content.substring(startOffset, endOffset)
                        text = if (checked) {
                            text.replace("[ ]", "[x]")
                        } else {
                            text.replace("[x]", "[ ]")
                        }
                        memosViewModel.editMemo(
                            currentMemo.identifier,
                            currentMemo.content.replaceRange(startOffset, endOffset, text),
                            currentMemo.resources,
                            currentMemo.visibility
                        )
                    }
                }
            )

            if (currentMemo.relations.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = R.string.relations.string,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                currentMemo.relations.forEach { relation ->
                    Row(
                        modifier = Modifier
                            .padding(start = 20.dp, end = 20.dp, top = 4.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = when (relation.type) {
                                RelationType.REFERENCE -> "↗ "
                                RelationType.COMMENT -> "💬 "
                                else -> "🔗 "
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = relation.relatedMemo.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (relation.relatedMemo.snippet.isNotBlank()) {
                            Text(
                                text = " — ${relation.relatedMemo.snippet}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            currentMemo.location?.let { location ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Computer,
                        contentDescription = R.string.location.string,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.padding(start = 4.dp))
                    Text(
                        text = location.placeholder
                            ?: listOfNotNull(location.latitude?.toString(), location.longitude?.toString()).joinToString(", ")
                            .ifBlank { R.string.location.string },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(innerPadding.calculateBottomPadding()))
        }
    }
}

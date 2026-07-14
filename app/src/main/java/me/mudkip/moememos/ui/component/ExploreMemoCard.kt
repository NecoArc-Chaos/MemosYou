package me.mudkip.moememos.ui.component

import android.text.TextUtils
import android.text.format.DateUtils
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import me.mudkip.moememos.R
import me.mudkip.moememos.data.model.Account
import me.mudkip.moememos.data.model.Memo
import me.mudkip.moememos.ext.string
import me.mudkip.moememos.viewmodel.LocalUserState

/**
 * Extract memos/{uid} from a full resource name like users/1/memos/abc.
 */
private fun memoCommentName(remoteId: String?): String {
    if (remoteId == null) return ""
    val idx = remoteId.lastIndexOf("/memos/")
    return if (idx >= 0) remoteId.substring(idx + 1) else remoteId
}

@Composable
fun ExploreMemoCard(
    memo: Memo
) {
    val userStateViewModel = LocalUserState.current
    val currentAccount by userStateViewModel.currentAccount.collectAsState()
    val isRemote = currentAccount !is Account.Local
    val scope = rememberCoroutineScope()
    var showComments by remember { mutableStateOf(false) }
    var commentText by remember { mutableStateOf("") }
    var comments by remember { mutableStateOf<List<Memo>>(emptyList()) }
    var loadingComments by remember { mutableStateOf(false) }

    fun loadComments() {
        if (loadingComments) return
        val name = memoCommentName(memo.remoteId)
        scope.launch {
            loadingComments = true
            try {
                userStateViewModel.accountService.getRepository()
                    .listMemoComments(name, pageSize = 20, pageToken = null)
                    .let { resp ->
                        if (resp is com.skydoves.sandwich.ApiResponse.Success) {
                            comments = resp.data.first
                        }
                    }
            } finally {
                loadingComments = false
            }
        }
    }

    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(modifier = Modifier.padding(bottom = 4.dp)) {
            // ── Header row with avatar ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Creator avatar
                val avatarUrl = memo.creator?.avatarUrl
                val creatorName = memo.creator?.name
                if (!avatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = creatorName,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.tertiary
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color.White
                        )
                    }
                }

                Spacer(Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    if (memo.creator != null && !TextUtils.isEmpty(creatorName)) {
                        Text(
                            creatorName,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        DateUtils.getRelativeTimeSpanString(
                            memo.date.toEpochMilli(),
                            System.currentTimeMillis(),
                            DateUtils.SECOND_IN_MILLIS
                        ).toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            MemoContent(memo, previewMode = false)

            // ── Action bar ──
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isRemote) {
                    TextButton(onClick = {
                        showComments = !showComments
                        if (showComments && comments.isEmpty() && !loadingComments) {
                            loadComments()
                        }
                    }) {
                        Icon(
                            Icons.Outlined.ChatBubbleOutline,
                            contentDescription = R.string.comment.string,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            R.string.comment.string,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            // ── Expandable comments section ──
            AnimatedVisibility(
                visible = showComments,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    if (loadingComments) {
                        Text(
                            "Loading\u2026",
                            modifier = Modifier.padding(vertical = 12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else if (comments.isEmpty()) {
                        Text(
                            R.string.no_comments.string,
                            modifier = Modifier.padding(vertical = 12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        comments.forEach { commentMemo ->
                            CommentItem(commentMemo)
                        }
                    }

                    // Comment input
                    if (isRemote) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = commentText,
                                onValueChange = { commentText = it },
                                modifier = Modifier.weight(1f),
                                placeholder = {
                                    Text(
                                        "Write a comment...",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodySmall
                            )
                            Spacer(Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    if (commentText.isNotBlank()) {
                                        val text = commentText.trim()
                                        scope.launch {
                                            val name = memoCommentName(memo.remoteId)
                                            val repo = userStateViewModel.accountService.getRepository()
                                            val resp = repo.createMemoComment(name, text)
                                            if (resp is com.skydoves.sandwich.ApiResponse.Success) {
                                                comments = comments + resp.data
                                                commentText = ""
                                            }
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Outlined.Send,
                                    contentDescription = "Send",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun CommentItem(memo: Memo) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            if (memo.creator != null && !TextUtils.isEmpty(memo.creator.name)) {
                Text(
                    memo.creator.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                memo.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                DateUtils.getRelativeTimeSpanString(
                    memo.date.toEpochMilli(),
                    System.currentTimeMillis(),
                    DateUtils.SECOND_IN_MILLIS
                ).toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

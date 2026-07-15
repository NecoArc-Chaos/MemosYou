package me.mudkip.moememos.ui.component

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.text.format.DateUtils
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddReaction
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.skydoves.sandwich.ApiResponse
import com.skydoves.sandwich.getOrNull
import kotlinx.coroutines.launch
import me.mudkip.moememos.R
import me.mudkip.moememos.data.api.ReactionItem
import me.mudkip.moememos.data.api.UpsertReactionRequest
import me.mudkip.moememos.data.model.Account
import me.mudkip.moememos.data.model.Memo
import me.mudkip.moememos.data.repository.MemosV1Repository
import me.mudkip.moememos.ext.string
import me.mudkip.moememos.viewmodel.LocalUserState

private fun memoCommentName(remoteId: String?): String {
    if (remoteId == null) return ""
    val idx = remoteId.lastIndexOf("/memos/")
    return if (idx >= 0) remoteId.substring(idx + 1) else remoteId
}

private fun resolveUrl(host: String, url: String?): String {
    if (url.isNullOrBlank()) return ""
    if (url.startsWith("http")) return url
    return host.trimEnd('/') + "/" + url.trimStart('/')
}

private val QUICK_EMOJIS = listOf("👍", "❤️", "😄", "🎉", "😢", "🔥")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExploreMemoCard(memo: Memo) {
    val userStateViewModel = LocalUserState.current
    val currentAccount by userStateViewModel.currentAccount.collectAsState()
    val host = userStateViewModel.host
    val isRemote = currentAccount !is Account.Local
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showComments by remember { mutableStateOf(false) }
    var commentText by remember { mutableStateOf("") }
    var comments by remember { mutableStateOf<List<Memo>>(emptyList()) }
    var loadingComments by remember { mutableStateOf(false) }
    var reactions by remember { mutableStateOf<List<ReactionItem>>(emptyList()) }
    var menuExpanded by remember { mutableStateOf(false) }
    var showReactionPicker by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Load reactions for this memo
    LaunchedEffect(memo.remoteId) {
        if (isRemote && memo.remoteId != null) {
            try {
                val repo = userStateViewModel.accountService.getRepository()
                if (repo is MemosV1Repository) {
                    val name = memoCommentName(memo.remoteId)
                    val resp = repo.memosApi.listMemoReactions(name)
                    if (resp is ApiResponse.Success) {
                        reactions = resp.data.reactions
                    }
                }
            } catch (_: Exception) { }
        }
    }

    fun loadComments() {
        if (loadingComments) return
        val name = memoCommentName(memo.remoteId)
        scope.launch {
            loadingComments = true
            try {
                userStateViewModel.accountService.getRepository()
                    .listMemoComments(name, pageSize = 20, pageToken = null)
                    .let { resp ->
                        if (resp is ApiResponse.Success) comments = resp.data.first
                    }
            } finally { loadingComments = false }
        }
    }

    fun addReaction(emoji: String) {
        scope.launch {
            try {
                val repo = userStateViewModel.accountService.getRepository()
                if (repo is MemosV1Repository) {
                    val name = memoCommentName(memo.remoteId)
                    repo.memosApi.upsertMemoReaction(name, UpsertReactionRequest(emoji))
                    // Refresh reactions
                    val resp = repo.memosApi.listMemoReactions(name)
                    if (resp is ApiResponse.Success) reactions = resp.data.reactions
                }
            } catch (_: Exception) { }
        }
    }

    val creatorName = memo.creator?.name ?: ""
    val avatarUrl = resolveUrl(host, memo.creator?.avatarUrl)

    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(bottom = 4.dp)) {
            // ── Header ──
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 16.dp, end = 8.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (avatarUrl.isNotBlank()) {
                    AsyncImage(model = avatarUrl, contentDescription = creatorName,
                        modifier = Modifier.size(32.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                } else {
                    Box(Modifier.size(32.dp).clip(CircleShape).background(
                        Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary))),
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Person, null, Modifier.size(18.dp), tint = Color.White)
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    if (!TextUtils.isEmpty(creatorName)) {
                        Text(creatorName, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Text(DateUtils.getRelativeTimeSpanString(memo.date.toEpochMilli(), System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString(),
                        style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                // More menu
                Box {
                    IconButton(onClick = { menuExpanded = true }) { Icon(Icons.Filled.MoreVert, null) }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        DropdownMenuItem(text = { Text("Copy content") }, onClick = {
                            (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
                                .setPrimaryClip(ClipData.newPlainText("memo", memo.content))
                            menuExpanded = false
                        }, leadingIcon = { Icon(Icons.Outlined.ContentCopy, null) })
                        if (isRemote) {
                            DropdownMenuItem(text = { Text("Copy link") }, onClick = {
                                val memoUrl = "$host/${memo.remoteId?.substringAfterLast('/') ?: ""}"
                                val sendIntent = Intent(Intent.ACTION_SEND).apply { putExtra(Intent.EXTRA_TEXT, memoUrl); type = "text/plain" }
                                context.startActivity(Intent.createChooser(sendIntent, null))
                                menuExpanded = false
                            }, leadingIcon = { Icon(Icons.Outlined.Link, null) })
                        }
                        if (isRemote) {
                            DropdownMenuItem(text = { Text("Delete") }, onClick = {
                                showDeleteDialog = true; menuExpanded = false
                            }, colors = MenuDefaults.itemColors(textColor = MaterialTheme.colorScheme.error, leadingIconColor = MaterialTheme.colorScheme.error),
                            leadingIcon = { Icon(Icons.Outlined.Delete, null) })
                        }
                    }
                }
            }

            MemoContent(memo, previewMode = false)

            // ── Reactions bar ──
            if (isRemote && reactions.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val grouped = reactions.groupBy { it.reactionType }
                    grouped.forEach { (emoji, items) ->
                        AssistChip(
                            onClick = { addReaction(emoji) },
                            label = { Text("$emoji ${items.size}") },
                            shape = RoundedCornerShape(16.dp),
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            }

            // ── Action bar ──
            HorizontalDivider(Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isRemote) {
                    // Quick reaction emojis
                    Row(Modifier.padding(start = 4.dp)) {
                        QUICK_EMOJIS.take(4).forEach { emoji ->
                            Text(emoji, Modifier.clickable { addReaction(emoji) }.padding(horizontal = 6.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
                Spacer(Modifier.weight(1f))
                if (isRemote) {
                    TextButton(onClick = {
                        showComments = !showComments
                        if (showComments && comments.isEmpty() && !loadingComments) loadComments()
                    }) {
                        Icon(Icons.Outlined.ChatBubbleOutline, R.string.comment.string, Modifier.size(20.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(R.string.comment.string, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            // ── Comments section ──
            AnimatedVisibility(showComments, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    if (loadingComments) Text("Loading…", Modifier.padding(vertical = 12.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    else if (comments.isEmpty()) Text(R.string.no_comments.string, Modifier.padding(vertical = 12.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    else comments.forEach { CommentItem(it, isRemote, host) }
                    if (isRemote) {
                        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(commentText, { commentText = it }, Modifier.weight(1f), placeholder = { Text("Write a comment...", style = MaterialTheme.typography.bodySmall) }, singleLine = true, textStyle = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.width(8.dp))
                            IconButton(onClick = {
                                if (commentText.isNotBlank()) {
                                    scope.launch {
                                        val name = memoCommentName(memo.remoteId)
                                        val resp = userStateViewModel.accountService.getRepository().createMemoComment(name, commentText.trim())
                                        if (resp is ApiResponse.Success) { comments = comments + resp.data; commentText = "" }
                                    }
                                }
                            }) { Icon(Icons.Outlined.Send, "Send", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }

    if (showDeleteDialog) AlertDialog(
        onDismissRequest = { showDeleteDialog = false },
        title = { Text("Delete memo?") },
        confirmButton = { TextButton({ scope.launch {
            try { userStateViewModel.accountService.getRepository().deleteMemo(memo.remoteId ?: ""); showDeleteDialog = false } catch (_: Exception) { }
        }}, colors = ButtonDefaults.buttonColors(contentColor = MaterialTheme.colorScheme.error, containerColor = MaterialTheme.colorScheme.errorContainer)) { Text("Delete") } },
        dismissButton = { TextButton({ showDeleteDialog = false }) { Text("Cancel") } }
    )
}

@Composable
private fun CommentItem(memo: Memo, isRemote: Boolean, host: String) {
    val avatarUrl = resolveUrl(host, memo.creator?.avatarUrl)
    val scope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf(false) }

    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        if (avatarUrl.isNotBlank()) {
            AsyncImage(model = avatarUrl, contentDescription = null, modifier = Modifier.size(24.dp).clip(CircleShape), contentScale = ContentScale.Crop)
        } else {
            Box(Modifier.size(24.dp).clip(CircleShape).background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary))),
                contentAlignment = Alignment.Center) { Icon(Icons.Filled.Person, null, Modifier.size(14.dp), tint = Color.White) }
        }
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            if (memo.creator != null && !TextUtils.isEmpty(memo.creator.name))
                Text(memo.creator.name, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Text(memo.content, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(DateUtils.getRelativeTimeSpanString(memo.date.toEpochMilli(), System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString(),
                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

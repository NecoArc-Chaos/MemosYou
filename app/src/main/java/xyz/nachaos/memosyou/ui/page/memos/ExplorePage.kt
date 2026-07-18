package xyz.nachaos.memosyou.ui.page.memos

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import android.util.Log
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.skydoves.sandwich.getOrNull
import kotlinx.coroutines.launch
import xyz.nachaos.memosyou.R
import xyz.nachaos.memosyou.data.repository.SyncingRepository
import xyz.nachaos.memosyou.ext.string
import xyz.nachaos.memosyou.viewmodel.LocalUserState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplorePage(
    drawerState: DrawerState? = null
) {
    val scope = rememberCoroutineScope()
    val userStateViewModel = LocalUserState.current
    val currentAccount by userStateViewModel.currentAccount.collectAsState()
    val host = userStateViewModel.host
    var showServerMenu by remember { mutableStateOf(false) }

    // Server branding from instance settings
    var serverTitle by remember { mutableStateOf("") }
    var serverLogoUrl by remember { mutableStateOf("") }

    LaunchedEffect(host) {
        if (host.isNotBlank()) {
            try {
                val repo = userStateViewModel.accountService.getRepository()
                if (repo is SyncingRepository) {
                    val (title, logo) = repo.getServerBranding()
                    serverTitle = title.ifBlank { "" }
                    serverLogoUrl = logo
                }
            } catch (_: Exception) { }
        }
    }

    val displayHost = host.removePrefix("https://").removePrefix("http://").trimEnd('/')
    val displayTitle = serverTitle.ifBlank {
        if (host.isNotBlank()) displayHost else "Memos"
    }
    val fullLogoUrl = if (serverLogoUrl.isNotBlank()) {
        if (serverLogoUrl.startsWith("http")) serverLogoUrl
        else host.trimEnd('/') + "/" + serverLogoUrl.trimStart('/')
    } else ""

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = R.string.explore.string) },
                navigationIcon = {
                    if (drawerState != null) {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = R.string.menu.string)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showServerMenu = true }) {
                        if (fullLogoUrl.isNotBlank()) {
                            AsyncImage(
                                model = fullLogoUrl,
                                contentDescription = displayTitle,
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            AsyncImage(
                                model = R.drawable.memos_logo,
                                contentDescription = "Server",
                                modifier = Modifier.size(28.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = showServerMenu,
                        onDismissRequest = { showServerMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        displayTitle,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        if (host.isNotBlank()) displayHost else "Local",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = { showServerMenu = false },
                            leadingIcon = {
                                if (fullLogoUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = fullLogoUrl,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(6.dp)),
                                        contentScale = ContentScale.Fit
                                    )
                                } else {
                                    AsyncImage(
                                        model = R.drawable.memos_logo,
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }
                        )
                        HorizontalDivider(Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
            )
        },

        content = { innerPadding ->
            ExploreList(
                contentPadding = innerPadding
            )
        }
    )
}

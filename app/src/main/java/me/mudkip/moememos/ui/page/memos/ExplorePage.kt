package me.mudkip.moememos.ui.page.memos

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import me.mudkip.moememos.R
import me.mudkip.moememos.ext.string
import me.mudkip.moememos.viewmodel.LocalUserState

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
                    // Server icon
                    IconButton(onClick = { showServerMenu = true }) {
                        Icon(
                            Icons.Outlined.Dns,
                            contentDescription = "Server",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(
                        expanded = showServerMenu,
                        onDismissRequest = { showServerMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    host.ifBlank { "Local" }
                                        .removePrefix("https://")
                                        .removePrefix("http://"),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            onClick = { showServerMenu = false },
                            leadingIcon = {
                                Icon(Icons.Outlined.Dns, contentDescription = null)
                            }
                        )
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

package me.mudkip.moememos.ui.page.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Source
import androidx.compose.material.icons.outlined.Web
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.mudkip.moememos.R
import me.mudkip.moememos.data.model.Account
import me.mudkip.moememos.data.model.MemoEditGesture
import me.mudkip.moememos.data.model.Settings
import me.mudkip.moememos.data.model.displayTitle
import me.mudkip.moememos.ext.popBackStackIfLifecycleIsResumed
import me.mudkip.moememos.ext.settingsDataStore
import me.mudkip.moememos.ext.string
import me.mudkip.moememos.ui.component.MemosIcon
import me.mudkip.moememos.ui.component.ThemePresetPicker
import me.mudkip.moememos.ui.page.common.RouteName
import me.mudkip.moememos.ui.security.AppLockAuthenticator
import me.mudkip.moememos.ui.security.AppLockSession
import me.mudkip.moememos.viewmodel.LocalUserState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(
    navController: NavHostController
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val userStateViewModel = LocalUserState.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()
    val accounts by userStateViewModel.accounts.collectAsState()
    val currentAccount by userStateViewModel.currentAccount.collectAsState()
    val settings by context.settingsDataStore.data.collectAsState(initial = Settings())
    val appLockSupported = remember(context, AppLockSession.foregroundGeneration) {
        AppLockAuthenticator.canAuthenticate(context)
    }
    var showEditGestureDialog by remember { mutableStateOf(false) }

    fun setAppLockEnabled(enabled: Boolean) {
        if (enabled && !appLockSupported) {
            return
        }
        if (enabled) {
            AppLockSession.lock()
        }
        scope.launch(Dispatchers.IO) {
            context.settingsDataStore.updateData { existingSettings ->
                existingSettings.copy(appLockEnabled = enabled)
            }
        }
    }
    val currentEditGesture = settings.usersList
        .firstOrNull { it.accountKey == settings.currentUser }
        ?.settings
        ?.editGesture
        ?: MemoEditGesture.NONE

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(text = R.string.settings.string) },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStackIfLifecycleIsResumed(lifecycleOwner)
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = R.string.back.string)
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        LazyColumn(contentPadding = innerPadding) {
            item {
                Text(
                    R.string.accounts.string,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp, 10.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            accounts.forEach { account ->
                when (account) {
                    is Account.MemosV0 -> item {
                        SettingItem(
                            icon = MemosIcon,
                            text = account.info.displayTitle(),
                            subtitle = account.info.host,
                            trailingIcon = {
                                if (currentAccount?.accountKey() == account.accountKey()) {
                                    Icon(Icons.Outlined.Check,
                                        contentDescription = R.string.selected.string,
                                        modifier = Modifier.padding(start = 16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                        }) {
                            navController.navigate("${RouteName.ACCOUNT}?accountKey=${account.accountKey()}")
                        }
                    }
                    is Account.MemosV1 -> item {
                        SettingItem(
                            icon = MemosIcon,
                            text = account.info.displayTitle(),
                            subtitle = account.info.host,
                            trailingIcon = {
                                if (currentAccount?.accountKey() == account.accountKey()) {
                                    Icon(Icons.Outlined.Check,
                                        contentDescription = R.string.selected.string,
                                        modifier = Modifier.padding(start = 16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                        }) {
                            navController.navigate("${RouteName.ACCOUNT}?accountKey=${account.accountKey()}")
                        }
                    }
                    is Account.Local -> item {
                        SettingItem(icon = Icons.Outlined.Home, text = R.string.local_account.string, trailingIcon = {
                            if (currentAccount?.accountKey() == account.accountKey()) {
                                Icon(Icons.Outlined.Check,
                                    contentDescription = R.string.selected.string,
                                    modifier = Modifier.padding(start = 16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }) {
                            navController.navigate("${RouteName.ACCOUNT}?accountKey=${account.accountKey()}")
                        }
                    }
                }
            }

            item {
                SettingItem(icon = Icons.Outlined.PersonAdd, text = R.string.add_account.string) {
                    navController.navigate(RouteName.ADD_ACCOUNT)
                }
            }

            item {
                Text(
                    R.string.preferences.string,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp, 10.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            item {
                ThemePresetPicker(
                    selectedId = settings.presetThemeId,
                    onSelect = { id ->
                        scope.launch(Dispatchers.IO) {
                            context.settingsDataStore.updateData { it.copy(presetThemeId = id) }
                        }
                    }
                )
                    Text(R.string.close.string)
                }
            }
        )
    }
}

private val MemoEditGesture.titleResource: Int
    get() = when (this) {
        MemoEditGesture.NONE -> R.string.edit_gesture_none
        MemoEditGesture.SINGLE -> R.string.edit_gesture_single
        MemoEditGesture.DOUBLE -> R.string.edit_gesture_double
        MemoEditGesture.LONG -> R.string.edit_gesture_long
    }

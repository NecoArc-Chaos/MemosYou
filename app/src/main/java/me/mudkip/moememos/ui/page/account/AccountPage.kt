package me.mudkip.moememos.ui.page.account

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import me.mudkip.moememos.R
import me.mudkip.moememos.data.model.MemoVisibility
import me.mudkip.moememos.ext.icon
import me.mudkip.moememos.ext.popBackStackIfLifecycleIsResumed
import me.mudkip.moememos.ext.string
import me.mudkip.moememos.ext.titleResource
import me.mudkip.moememos.ext.visibilityList
import me.mudkip.moememos.ui.component.MemosIcon
import me.mudkip.moememos.ui.component.SelectablePreference
import me.mudkip.moememos.ui.page.common.LocalRootNavController
import me.mudkip.moememos.ui.page.common.RouteName
import me.mudkip.moememos.viewmodel.LocalUserState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountPage(
    navController: NavHostController,
    currentAccountKey: String?
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val layoutDirection = LocalLayoutDirection.current
    val userStateViewModel = LocalUserState.current
    val rootNavController = LocalRootNavController.current
    val accounts by userStateViewModel.sortedAccounts.collectAsState()
    val currentAccountKeyState by userStateViewModel.currentAccount.collectAsState()
    val displayedAccountKey = currentAccountKey ?: currentAccountKeyState?.accountKey ?: ""
    val currentAccount = remember(accounts, displayedAccountKey) {
        accounts.firstOrNull { it.accountKey == displayedAccountKey }
    }

    LaunchedEffect(currentAccount) {
        if (currentAccount == null) {
            navController.popBackStackIfLifecycleIsResumed(lifecycleOwner)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = currentAccount?.getAccountInfo()?.host ?: R.string.account.string) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStackIfLifecycleIsResumed(lifecycleOwner) }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = R.string.back.string)
                    }
                }
            )
        }
    ) { innerPadding ->
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
            SelectablePreference(
                title = R.string.default_visibility.string,
                values = MemoVisibility.visibilityList.map { stringResource(it.titleResource) },
                currentIndex = MemoVisibility.visibilityList.indexOf(currentAccount?.getAccountInfo()?.defaultVisibility ?: MemoVisibility.PRIVATE),
                onSelect = { index ->
                    userStateViewModel.updateVisibilitySetting(
                        MemoVisibility.visibilityList[index],
                        displayedAccountKey
                    )
                }
            )

            SelectablePreference(
                title = R.string.edit_gesture.string,
                values = listOf(
                    R.string.double_click.string,
                    R.string.long_press.string,
                    R.string.single_click.string,
                    R.string.off.string
                ),
                currentIndex = (currentAccount?.getAccountInfo()?.memoEditGesture?.ordinal ?: 0),
                onSelect = { index ->
                    userStateViewModel.updateEditGestureSetting(
                        index,
                        displayedAccountKey
                    )
                }
            )

            Spacer(modifier = Modifier.height(innerPadding.calculateBottomPadding()))
        }
    }
}

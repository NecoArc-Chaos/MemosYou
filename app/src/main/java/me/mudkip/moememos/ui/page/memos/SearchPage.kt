package me.mudkip.moememos.ui.page.memos

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import me.mudkip.moememos.R
import me.mudkip.moememos.ext.string
import me.mudkip.moememos.ext.popBackStackIfLifecycleIsResumed
import androidx.lifecycle.compose.LocalLifecycleOwner
import me.mudkip.moememos.ui.page.common.LocalRootNavController
import me.mudkip.moememos.ui.page.common.RouteName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchPage(navController: NavHostController) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val layoutDirection = LocalLayoutDirection.current
    val rootNavController = LocalRootNavController.current
    var searchText by rememberSaveable { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        placeholder = { Text(R.string.search.string) },
                        singleLine = true,
                        shape = ShapeDefaults.Large
                    )
                },
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
        ) {
            MemosList(
                searchString = searchText,
                onTagClick = { tag ->
                    rootNavController.navigate("${RouteName.TAG}/${java.net.URLEncoder.encode(tag, "UTF-8")}") {
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

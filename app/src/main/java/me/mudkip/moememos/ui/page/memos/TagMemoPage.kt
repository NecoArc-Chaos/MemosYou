package me.mudkip.moememos.ui.page.memos

import androidx.compose.foundation.layout.padding
import androidx.compose.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import me.mudkip.moememos.R
import me.mudkip.moememos.ext.popBackStackIfLifecycleIsResumed
import me.mudkip.moememos.ext.string
import java.net.URLDecoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagMemoPage(
    navController: NavHostController,
    tag: String
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = URLDecoder.decode(tag, "UTF-8")) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStackIfLifecycleIsResumed(lifecycleOwner) }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = R.string.back.string)
                    }
                }
            )
        }
    ) { innerPadding ->
        MemosList(
            tag = tag,
            contentPadding = innerPadding
        )
    }
}

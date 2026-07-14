package me.mudkip.moememos.ui.page.memoinput

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import me.mudkip.moememos.R
import me.mudkip.moememos.data.model.MemoVisibility
import me.mudkip.moememos.ext.string
import me.mudkip.moememos.ui.component.InputImage
import me.mudkip.moememos.ui.page.common.RouteName
import me.mudkip.moememos.viewmodel.LocalMemos
import me.mudkip.moememos.viewmodel.LocalUserState
import me.mudkip.moememos.viewmodel.MemoInputViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoInputContent(
    navController: NavHostController,
    inputViewModel: MemoInputViewModel
) {
    val memosViewModel = LocalMemos.current
    val userStateViewModel = LocalUserState.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val focus = LocalFocusManager.current
    val currentAccount by userStateViewModel.currentAccount.collectAsState()
    var menuExpanded by remember { mutableStateOf(false) }
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uris.forEach { uri ->
            inputViewModel.addResource(uri)
        }
    }
    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uris.forEach { uri ->
            inputViewModel.addResource(uri)
        }
    }

    LaunchedEffect(Unit) {
        inputViewModel.loadMemo()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = if (inputViewModel.memoIdentifier == null) R.string.new_memo.string else R.string.edit.string) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = R.string.back.string)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                inputViewModel.createOrUpdateMemo()
                                memosViewModel.loadMemos()
                                navController.popBackStack()
                            }
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = R.string.send.string)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.padding(top = 8.dp)
            ) {
                IconButton(
                    onClick = { galleryLauncher.launch("image/*") }
                ) {
                    Icon(Icons.Outlined.AddPhotoAlternate, contentDescription = R.string.add_photo.string)
                }
                IconButton(
                    onClick = { fileLauncher.launch("*/*") }
                ) {
                    Icon(Icons.Outlined.AttachFile, contentDescription = R.string.attach_file.string)
                }
            }

            val resources by inputViewModel.inputResources.collectAsState()
            if (resources.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .height(100.dp)
                ) {
                    resources.forEach { resource ->
                        InputImage(
                            resource = resource,
                            inputViewModel = inputViewModel
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }

            OutlinedTextField(
                value = inputViewModel.inputText,
                onValueChange = { inputViewModel.updateInputText(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                placeholder = { Text(R.string.whats_on_your_mind.string) },
                enabled = !inputViewModel.saving,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Editing toolbar
            MemoInputFormattingBar(
                inputViewModel = inputViewModel
            )
        }
    }
}

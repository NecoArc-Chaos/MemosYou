package xyz.nachaos.memosyou

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.AndroidEntryPoint
import xyz.nachaos.memosyou.ui.page.common.Navigation
import xyz.nachaos.memosyou.ui.security.AppLockGate
import xyz.nachaos.memosyou.viewmodel.LocalMemos
import xyz.nachaos.memosyou.viewmodel.LocalUserState
import xyz.nachaos.memosyou.viewmodel.MemosViewModel
import xyz.nachaos.memosyou.viewmodel.UserStateViewModel

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    private val userStateViewModel: UserStateViewModel by viewModels()
    private val memosViewModel: MemosViewModel by viewModels()

    companion object {
        const val ACTION_NEW_MEMO = "xyz.nachaos.memosyou.action.NEW_MEMO"
        const val ACTION_EDIT_MEMO = "xyz.nachaos.memosyou.action.EDIT_MEMO"
        const val ACTION_VIEW_MEMO = "xyz.nachaos.memosyou.action.VIEW_MEMO"
        const val EXTRA_MEMO_ID = "memoId"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        setContent {
            CompositionLocalProvider(
                LocalUserState provides userStateViewModel,
                LocalMemos provides memosViewModel
            ) {
                AppLockGate {
                    Navigation()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}

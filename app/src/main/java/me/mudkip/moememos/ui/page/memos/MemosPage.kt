package xyz.nachaos.memosyou.ui.page.memos

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import androidx.window.core.layout.WindowSizeClass
import kotlinx.coroutines.launch
import xyz.nachaos.memosyou.ui.component.SideDrawer

@Composable
fun MemosPage() {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val memosNavController = rememberNavController()

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    if (windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)) {
        PermanentNavigationDrawer(
            drawerContent = {
                PermanentDrawerSheet { SideDrawer(memosNavController) }
            }
        ) {
            MemosNavigation(navController = memosNavController)
        }
    } else {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(Modifier.fillMaxWidth(2f/3f)) {
                    SideDrawer(memosNavController, drawerState)
                }
            },
            scrimColor = Color.Black.copy(alpha = 0.32f)
        ) {
            MemosNavigation(drawerState = drawerState, navController = memosNavController)
        }
    }
}

package xyz.nachaos.memosyou.widget

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Utility class to update all widget instances when memos are changed
 */
object WidgetUpdater {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Update all widget instances
     */
    fun updateWidgets(context: Context) {
        scope.launch {
            WidgetUpdateScheduler.updateAllWidgets(context)
        }
    }
}

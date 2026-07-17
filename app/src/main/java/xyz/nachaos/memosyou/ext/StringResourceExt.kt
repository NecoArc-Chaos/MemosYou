package xyz.nachaos.memosyou.ext

import xyz.nachaos.memosyou.MemosYouApp

/**
 * Get the string resources by the R.string.xx.string
 *
 * To support i18n
 * @author Xeu<thankrain@qq.com>
 */
val Int.string get() = MemosYouApp.CONTEXT.getString(this)
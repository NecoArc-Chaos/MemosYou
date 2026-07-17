package xyz.nachaos.memosyou.widget

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import xyz.nachaos.memosyou.data.service.MemoService

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun memoService(): MemoService
}

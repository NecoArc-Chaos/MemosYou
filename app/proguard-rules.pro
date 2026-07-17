# Add project specific ProGuard rules here.

# Keep Application class
-keep class xyz.nachaos.memosyou.MemosYouApp { *; }
-keep class xyz.nachaos.memosyou.MainActivity { *; }
-keep class xyz.nachaos.memosyou.MemosYouFileProvider { *; }

# Keep widget classes (referenced via reflection)
-keep class xyz.nachaos.memosyou.widget.MemosYouGlanceWidgetReceiver { *; }
-keep class xyz.nachaos.memosyou.widget.MemosYouGlanceWidget { *; }
-keep class xyz.nachaos.memosyou.widget.MoeMemosGlanceWidgetReceiver { *; }
-keep class xyz.nachaos.memosyou.widget.MoeMemosGlanceWidget { *; }
-keep class xyz.nachaos.memosyou.widget.WidgetUpdateScheduler { *; }

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class xyz.nachaos.memosyou.** { *; }

# Keep Room database
-keep class xyz.nachaos.memosyou.data.local.MoeMemosDatabase { *; }
-keep class xyz.nachaos.memosyou.data.local.dao.** { *; }

# Keep JSON serialization
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
    @kotlinx.serialization.Serializable <fields>;
}

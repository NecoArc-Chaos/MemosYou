# Add project specific ProGuard rules here.

# Keep Application class
-keep class xyz.nachaos.memosyou.MemosYouApp { *; }
-keep class xyz.nachaos.memosyou.MainActivity { *; }
-keep class xyz.nachaos.memosyou.MemosYouFileProvider { *; }

# Keep widget classes (referenced via reflection)
-keep class xyz.nachaos.memosyou.widget.MoeMemosGlanceWidgetReceiver { *; }
-keep class xyz.nachaos.memosyou.widget.MemoryGlanceWidgetReceiver { *; }
-keep class xyz.nachaos.memosyou.widget.MemoryGlanceWidget { *; }
-keep class xyz.nachaos.memosyou.widget.MoeMemosGlanceWidget { *; }
-keep class xyz.nachaos.memosyou.widget.WidgetUpdateScheduler { *; }

# Keep Hilt generated classes and ViewModels
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel { *; }

# Keep Room database, DAOs, and entities
-keep class xyz.nachaos.memosyou.data.local.MoeMemosDatabase { *; }
-keep class xyz.nachaos.memosyou.data.local.dao.** { *; }
-keep class xyz.nachaos.memosyou.data.local.entity.** { *; }
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *

# Keep Retrofit API interfaces
-keep interface xyz.nachaos.memosyou.data.api.** { *; }

# Keep JSON serialization
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
    @kotlinx.serialization.Serializable <fields>;
}

# Keep Markdown renderer
-keep class com.mikepenz.multiplatformmarkdownrenderer.** { *; }

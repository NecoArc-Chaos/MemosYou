package me.mudkip.moememos.ui.theme

import android.app.Activity
import android.graphics.Color
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import me.mudkip.moememos.data.model.DarkMode
import me.mudkip.moememos.data.model.Settings
import me.mudkip.moememos.ext.settingsDataStore

val ExpressiveShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp), small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp), large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp),
)

@Composable
fun MoeMemosTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val settings by context.settingsDataStore.data.collectAsState(initial = Settings())
    val systemDark = isSystemInDarkTheme()
    val darkTheme = when (settings.darkMode) {
        DarkMode.SYSTEM -> systemDark
        DarkMode.LIGHT -> false
        DarkMode.DARK -> true
    }

    val preset = if (settings.presetThemeId.isNotBlank() && settings.presetThemeId != "auto") presetById(settings.presetThemeId) else null

    val targetScheme = when {
        preset != null -> preset.get(darkTheme)
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> Default.dark
        else -> Default.light
    }

    // Smooth color transition between light/dark
    val animSpec = tween<androidx.compose.ui.graphics.Color>(400)
    val animatedScheme = targetScheme.copy(
        primary = animateColorAsState(targetScheme.primary, animSpec, label = "primary").value,
        onPrimary = animateColorAsState(targetScheme.onPrimary, animSpec, label = "onPrimary").value,
        primaryContainer = animateColorAsState(targetScheme.primaryContainer, animSpec, label = "pContainer").value,
        onPrimaryContainer = animateColorAsState(targetScheme.onPrimaryContainer, animSpec, label = "onPContainer").value,
        secondary = animateColorAsState(targetScheme.secondary, animSpec, label = "secondary").value,
        onSecondary = animateColorAsState(targetScheme.onSecondary, animSpec, label = "onSecondary").value,
        secondaryContainer = animateColorAsState(targetScheme.secondaryContainer, animSpec, label = "sContainer").value,
        onSecondaryContainer = animateColorAsState(targetScheme.onSecondaryContainer, animSpec, label = "onSContainer").value,
        tertiary = animateColorAsState(targetScheme.tertiary, animSpec, label = "tertiary").value,
        onTertiary = animateColorAsState(targetScheme.onTertiary, animSpec, label = "onTertiary").value,
        tertiaryContainer = animateColorAsState(targetScheme.tertiaryContainer, animSpec, label = "tContainer").value,
        onTertiaryContainer = animateColorAsState(targetScheme.onTertiaryContainer, animSpec, label = "onTContainer").value,
        background = animateColorAsState(targetScheme.background, animSpec, label = "bg").value,
        onBackground = animateColorAsState(targetScheme.onBackground, animSpec, label = "onBg").value,
        surface = animateColorAsState(targetScheme.surface, animSpec, label = "surface").value,
        onSurface = animateColorAsState(targetScheme.onSurface, animSpec, label = "onSurface").value,
        surfaceVariant = animateColorAsState(targetScheme.surfaceVariant, animSpec, label = "sVariant").value,
        onSurfaceVariant = animateColorAsState(targetScheme.onSurfaceVariant, animSpec, label = "onSVariant").value,
        surfaceTint = animateColorAsState(targetScheme.surfaceTint, animSpec, label = "sTint").value,
        inverseSurface = animateColorAsState(targetScheme.inverseSurface, animSpec, label = "invSurface").value,
        inverseOnSurface = animateColorAsState(targetScheme.inverseOnSurface, animSpec, label = "invOnSurface").value,
        outline = animateColorAsState(targetScheme.outline, animSpec, label = "outline").value,
        outlineVariant = animateColorAsState(targetScheme.outlineVariant, animSpec, label = "outlineVariant").value,
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.TRANSPARENT
            window.navigationBarColor = Color.TRANSPARENT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isStatusBarContrastEnforced = false
                window.isNavigationBarContrastEnforced = false
            }
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(colorScheme = animatedScheme, typography = ExpressiveTypography, shapes = ExpressiveShapes, content = content)
}

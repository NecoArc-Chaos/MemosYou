package me.mudkip.moememos.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

data class PresetTheme(val id: String, val name: String, val light: ColorScheme, val dark: ColorScheme) {
    fun get(dark: Boolean) = if (dark) this.dark else this.light
}

fun presetById(id: String): PresetTheme? = Presets.find { it.id == id }

// ─── Sakura ───
private val Sakura = PresetTheme("sakura", "Sakura",
        primary = Color(0xFF8E4955), onPrimary = Color.White, primaryContainer = Color(0xFFFFD9DD), onPrimaryContainer = Color(0xFF72333E),
        secondary = Color(0xFF76565A), onSecondary = Color.White, secondaryContainer = Color(0xFFFFD9DD), onSecondaryContainer = Color(0xFF5C3F43),
        tertiary = Color(0xFF785831), onTertiary = Color.White, tertiaryContainer = Color(0xFFFFDDB8), onTertiaryContainer = Color(0xFF5E411C),
        error = Color(0xFFBA1A1A), onError = Color.White, errorContainer = Color(0xFFFFDAD6), onErrorContainer = Color(0xFF93000A),
        background = Color(0xFFFFF8F7), onBackground = Color(0xFF22191A), surface = Color(0xFFFFF8F7), onSurface = Color(0xFF22191A),
        surfaceVariant = Color(0xFFF3DDDF), onSurfaceVariant = Color(0xFF524345), outline = Color(0xFF847374), outlineVariant = Color(0xFFD7C1C3),
        inverseSurface = Color(0xFF382E2F), inverseOnSurface = Color(0xFFFEEDED), inversePrimary = Color(0xFFFFB2BC),
        surfaceContainerLowest = Color.White, surfaceContainerLow = Color(0xFFFFF0F1), surfaceContainer = Color(0xFFFBEAEB),
        surfaceContainerHigh = Color(0xFFF6E4E5), surfaceContainerHighest = Color(0xFFF0DEDF),
    ),
    dark = darkColorScheme(
        primary = Color(0xFFFFB2BC), onPrimary = Color(0xFF561D28), primaryContainer = Color(0xFF72333E), onPrimaryContainer = Color(0xFFFFD9DD),
        secondary = Color(0xFFE5BDC1), onSecondary = Color(0xFF43292D), secondaryContainer = Color(0xFF5C3F43), onSecondaryContainer = Color(0xFFFFD9DD),
        tertiary = Color(0xFFEABF8F), onTertiary = Color(0xFF452B07), tertiaryContainer = Color(0xFF5E411C), onTertiaryContainer = Color(0xFFFFDDB8),
        error = Color(0xFFFFB4AB), onError = Color(0xFF690005), errorContainer = Color(0xFF93000A), onErrorContainer = Color(0xFFFFDAD6),
        background = Color(0xFF1A1112), onBackground = Color(0xFFF0DEDF), surface = Color(0xFF1A1112), onSurface = Color(0xFFF0DEDF),
        surfaceVariant = Color(0xFF524345), onSurfaceVariant = Color(0xFFD7C1C3), outline = Color(0xFF9F8C8E), outlineVariant = Color(0xFF524345),
        inverseSurface = Color(0xFFF0DEDF), inverseOnSurface = Color(0xFF382E2F), inversePrimary = Color(0xFF8E4955),
        surfaceContainerLowest = Color(0xFF140C0D), surfaceContainerLow = Color(0xFF22191A), surfaceContainer = Color(0xFF261D1E),
        surfaceContainerHigh = Color(0xFF312828), surfaceContainerHighest = Color(0xFF3D3233),
    )
)

// ─── Ocean ───
private val Ocean = PresetTheme("ocean", "Ocean",
    light = lightColorScheme(
        primary = Color(0xFF116682), onPrimary = Color.White, primaryContainer = Color(0xFFBDE9FF), onPrimaryContainer = Color(0xFF004D64),
        secondary = Color(0xFF4D616C), onSecondary = Color.White, secondaryContainer = Color(0xFFD0E6F2), onSecondaryContainer = Color(0xFF354A53),
        tertiary = Color(0xFF5D5B7D), onTertiary = Color.White, tertiaryContainer = Color(0xFFE3DFFF), onTertiaryContainer = Color(0xFF454364),
        error = Color(0xFFBA1A1A), onError = Color.White, errorContainer = Color(0xFFFFDAD6), onErrorContainer = Color(0xFF93000A),
        background = Color(0xFFF6FAFD), onBackground = Color(0xFF171C1F), surface = Color(0xFFF6FAFD), onSurface = Color(0xFF171C1F),
        surfaceVariant = Color(0xFFDCE4E9), onSurfaceVariant = Color(0xFF40484C), outline = Color(0xFF70787D), outlineVariant = Color(0xFFC0C8CD),
        inverseSurface = Color(0xFF2C3134), inverseOnSurface = Color(0xFFEDF1F5), inversePrimary = Color(0xFF8BD0EF),
        surfaceContainerLowest = Color.White, surfaceContainerLow = Color(0xFFF0F4F8), surfaceContainer = Color(0xFFEAEEF2),
        surfaceContainerHigh = Color(0xFFE4E9EC), surfaceContainerHighest = Color(0xFFDFE3E7),
    ),
    dark = darkColorScheme(
        primary = Color(0xFF8BD0EF), onPrimary = Color(0xFF003546), primaryContainer = Color(0xFF004D64), onPrimaryContainer = Color(0xFFBDE9FF),
        secondary = Color(0xFFB4CAD6), onSecondary = Color(0xFF1F333C), secondaryContainer = Color(0xFF354A53), onSecondaryContainer = Color(0xFFD0E6F2),
        tertiary = Color(0xFFC6C2EA), onTertiary = Color(0xFF2E2D4D), tertiaryContainer = Color(0xFF454364), onTertiaryContainer = Color(0xFFE3DFFF),
        error = Color(0xFFFFB4AB), onError = Color(0xFF690005), errorContainer = Color(0xFF93000A), onErrorContainer = Color(0xFFFFDAD6),
        background = Color(0xFF0F1417), onBackground = Color(0xFFDFE3E7), surface = Color(0xFF0F1417), onSurface = Color(0xFFDFE3E7),
        surfaceVariant = Color(0xFF40484C), onSurfaceVariant = Color(0xFFC0C8CD), outline = Color(0xFF8A9297), outlineVariant = Color(0xFF40484C),
        inverseSurface = Color(0xFFDFE3E7), inverseOnSurface = Color(0xFF2C3134), inversePrimary = Color(0xFF116682),
        surfaceContainerLowest = Color(0xFF0A0F11), surfaceContainerLow = Color(0xFF171C1F), surfaceContainer = Color(0xFF1B2023),
        surfaceContainerHigh = Color(0xFF262B2D), surfaceContainerHighest = Color(0xFF303538),
    )
)

// ─── Spring (Green) ───
private val Spring = PresetTheme("spring", "Spring",
    light = lightColorScheme(
        primary = Color(0xFF3B6F37), onPrimary = Color.White, primaryContainer = Color(0xFFBCFDAF), onPrimaryContainer = Color(0xFF002204),
        secondary = Color(0xFF53634E), onSecondary = Color.White, secondaryContainer = Color(0xFFD6E8CE), onSecondaryContainer = Color(0xFF111F0F),
        tertiary = Color(0xFF386568), onTertiary = Color.White, tertiaryContainer = Color(0xFFBCEBEE), onTertiaryContainer = Color(0xFF002022),
        error = Color(0xFFBA1A1A), onError = Color.White, errorContainer = Color(0xFFFFDAD6), onErrorContainer = Color(0xFF93000A),
        background = Color(0xFFF8FBF1), onBackground = Color(0xFF191D17), surface = Color(0xFFF8FBF1), onSurface = Color(0xFF191D17),
        surfaceVariant = Color(0xFFDEE5D8), onSurfaceVariant = Color(0xFF424940), outline = Color(0xFF73796F), outlineVariant = Color(0xFFC2C9BD),
        inverseSurface = Color(0xFF2E322B), inverseOnSurface = Color(0xFFF0F2E9), inversePrimary = Color(0xFFA0E095),
        surfaceContainerLowest = Color.White, surfaceContainerLow = Color(0xFFF3F5EC), surfaceContainer = Color(0xFFEDF0E6),
        surfaceContainerHigh = Color(0xFFE7EAE1), surfaceContainerHighest = Color(0xFFE2E4DB),
    ),
    dark = darkColorScheme(
        primary = Color(0xFFA0E095), onPrimary = Color(0xFF043B0E), primaryContainer = Color(0xFF245322), onPrimaryContainer = Color(0xFFBCFDAF),
        secondary = Color(0xFFBACCB3), onSecondary = Color(0xFF263422), secondaryContainer = Color(0xFF3C4B38), onSecondaryContainer = Color(0xFFD6E8CE),
        tertiary = Color(0xFFA0CFD2), onTertiary = Color(0xFF013739), tertiaryContainer = Color(0xFF1E4D50), onTertiaryContainer = Color(0xFFBCEBEE),
        error = Color(0xFFFFB4AB), onError = Color(0xFF690005), errorContainer = Color(0xFF93000A), onErrorContainer = Color(0xFFFFDAD6),
        background = Color(0xFF11140F), onBackground = Color(0xFFE2E4DB), surface = Color(0xFF11140F), onSurface = Color(0xFFE2E4DB),
        surfaceVariant = Color(0xFF424940), onSurfaceVariant = Color(0xFFC2C9BD), outline = Color(0xFF8C9388), outlineVariant = Color(0xFF424940),
        inverseSurface = Color(0xFFE2E4DB), inverseOnSurface = Color(0xFF2E322B), inversePrimary = Color(0xFF3B6F37),
        surfaceContainerLowest = Color(0xFF0C0F0A), surfaceContainerLow = Color(0xFF191D17), surfaceContainer = Color(0xFF1D211B),
        surfaceContainerHigh = Color(0xFF272C25), surfaceContainerHighest = Color(0xFF323730),
    )
)

// ─── Autumn (Amber) ───
private val Autumn = PresetTheme("autumn", "Autumn",
    light = lightColorScheme(
        primary = Color(0xFF8C5000), onPrimary = Color.White, primaryContainer = Color(0xFFFFDCBC), onPrimaryContainer = Color(0xFF2D1600),
        secondary = Color(0xFF725A41), onSecondary = Color.White, secondaryContainer = Color(0xFFFEDDBD), onSecondaryContainer = Color(0xFF574223),
        tertiary = Color(0xFF55603A), onTertiary = Color.White, tertiaryContainer = Color(0xFFD9E5B4), onTertiaryContainer = Color(0xFF3C4803),
        error = Color(0xFFBA1A1A), onError = Color.White, errorContainer = Color(0xFFFFDAD6), onErrorContainer = Color(0xFF93000A),
        background = Color(0xFFFFF8F4), onBackground = Color(0xFF211A13), surface = Color(0xFFFFF8F4), onSurface = Color(0xFF211A13),
        surfaceVariant = Color(0xFFF2DFD1), onSurfaceVariant = Color(0xFF51443A), outline = Color(0xFF837469), outlineVariant = Color(0xFFD6C3B5),
        inverseSurface = Color(0xFF372F27), inverseOnSurface = Color(0xFFFEEEE3), inversePrimary = Color(0xFFFFB870),
        surfaceContainerLowest = Color.White, surfaceContainerLow = Color(0xFFFFF1E8), surfaceContainer = Color(0xFFFAEBE0),
        surfaceContainerHigh = Color(0xFFF4E5DA), surfaceContainerHighest = Color(0xFFEEE0D5),
    ),
    dark = darkColorScheme(
        primary = Color(0xFFFFB870), onPrimary = Color(0xFF4B2800), primaryContainer = Color(0xFF6B3C00), onPrimaryContainer = Color(0xFFFFDCBC),
        secondary = Color(0xFFE1C1A2), onSecondary = Color(0xFF402C17), secondaryContainer = Color(0xFF574223), onSecondaryContainer = Color(0xFFFEDDBD),
        tertiary = Color(0xFFBDC99A), onTertiary = Color(0xFF283210), tertiaryContainer = Color(0xFF3E4823), onTertiaryContainer = Color(0xFFD9E5B4),
        error = Color(0xFFFFB4AB), onError = Color(0xFF690005), errorContainer = Color(0xFF93000A), onErrorContainer = Color(0xFFFFDAD6),
        background = Color(0xFF19120B), onBackground = Color(0xFFEEE0D5), surface = Color(0xFF19120B), onSurface = Color(0xFFEEE0D5),
        surfaceVariant = Color(0xFF51443A), onSurfaceVariant = Color(0xFFD6C3B5), outline = Color(0xFFA08E81), outlineVariant = Color(0xFF51443A),
        inverseSurface = Color(0xFFEEE0D5), inverseOnSurface = Color(0xFF372F27), inversePrimary = Color(0xFF8C5000),
        surfaceContainerLowest = Color(0xFF130D07), surfaceContainerLow = Color(0xFF211A13), surfaceContainer = Color(0xFF261E17),
        surfaceContainerHigh = Color(0xFF312821), surfaceContainerHighest = Color(0xFF3C332B),
    )
)

// ─── Black ───
private val Black = PresetTheme("black", "Black",
    light = lightColorScheme(
        primary = Color(0xFF606060), onPrimary = Color.White, primaryContainer = Color(0xFFE6E6E6), onPrimaryContainer = Color(0xFF424242),
        secondary = Color(0xFF424242), onSecondary = Color.White, secondaryContainer = Color(0xFFF3F3F3), onSecondaryContainer = Color(0xFF575757),
        tertiary = Color(0xFF343434), onTertiary = Color.White, tertiaryContainer = Color(0xFF444444), onTertiaryContainer = Color(0xFFB5B5B5),
        error = Color(0xFFDC2626), onError = Color.White, errorContainer = Color(0xFFFEE2E2), onErrorContainer = Color(0xFF991B1B),
        background = Color.White, onBackground = Color(0xFF252525), surface = Color.White, onSurface = Color(0xFF252525),
        surfaceVariant = Color(0xFFF7F7F7), onSurfaceVariant = Color(0xFF444444), outline = Color(0xFFB5B5B5), outlineVariant = Color(0xFFEBEBEB),
        inverseSurface = Color(0xFF343434), inverseOnSurface = Color(0xFFEEEEF0), inversePrimary = Color(0xFFEBEBEB),
        surfaceContainerLowest = Color.White, surfaceContainerLow = Color(0xFFF8F8F8), surfaceContainer = Color(0xFFF7F7F7),
        surfaceContainerHigh = Color(0xFFEBEBEB), surfaceContainerHighest = Color(0xFFE8E8E8),
    ),
    dark = darkColorScheme(
        primary = Color(0xFFEBEBEB), onPrimary = Color(0xFF343434), primaryContainer = Color(0xFF3B3B3B), onPrimaryContainer = Color(0xFFB5B5B5),
        secondary = Color(0xFFB5B5B5), onSecondary = Color(0xFF343434), secondaryContainer = Color(0xFF444444), onSecondaryContainer = Color(0xFFFCFCFC),
        tertiary = Color(0xFFEBEBEB), onTertiary = Color(0xFF343434), tertiaryContainer = Color(0xFF444444), onTertiaryContainer = Color(0xFFB5B5B5),
        error = Color(0xFFEF4444), onError = Color(0xFF7F1D1D), errorContainer = Color(0xFF991B1B), onErrorContainer = Color(0xFFFEE2E2),
        background = Color(0xFF1C1C1C), onBackground = Color(0xFFFCFCFC), surface = Color(0xFF1C1C1C), onSurface = Color(0xFFFCFCFC),
        surfaceVariant = Color(0xFF444444), onSurfaceVariant = Color(0xFFB5B5B5), outline = Color(0xFF8E8E8E), outlineVariant = Color(0xFF444444),
        inverseSurface = Color(0xFFFCFCFC), inverseOnSurface = Color(0xFF343434), inversePrimary = Color(0xFF8E8E8E),
        surfaceContainerLowest = Color(0xFF1A1A1A), surfaceContainerLow = Color(0xFF252525), surfaceContainer = Color(0xFF2A2A2A),
        surfaceContainerHigh = Color(0xFF343434), surfaceContainerHighest = Color(0xFF3F3F3F),
    )
)

val Presets = listOf(Sakura, Ocean, Spring, Autumn, Black)
val Default = Sakura

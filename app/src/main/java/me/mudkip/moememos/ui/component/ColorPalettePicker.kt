package me.mudkip.moememos.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp

data class PalettePreset(val name: String, val colors: List<Color>)

val MD3E_THEMES = listOf(
    PalettePreset("Ocean", listOf(Color(0xFF386A97), Color(0xFF5E7D9C), Color(0xFFBC6C25))),
    PalettePreset("Forest", listOf(Color(0xFF386A20), Color(0xFF5A7D42), Color(0xFF9C6B3F))),
    PalettePreset("Berry", listOf(Color(0xFF8E254D), Color(0xFF6A4078), Color(0xFF3D6A80))),
    PalettePreset("Sunset", listOf(Color(0xFFB04A30), Color(0xFF8B504A), Color(0xFF3C6B6B))),
    PalettePreset("Lavender", listOf(Color(0xFF6C4EA0), Color(0xFF5A5D8C), Color(0xFF3C7A6A))),
    PalettePreset("Slate", listOf(Color(0xFF4A5D73), Color(0xFF5E6A7E), Color(0xFF8B6A4A))),
)

@Composable
fun ColorPalettePicker(
    selectedColorHex: String,
    onColorSelected: (String) -> Unit
) {
    val selectedColor = try {
        if (selectedColorHex.isNotBlank()) Color(android.graphics.Color.parseColor(selectedColorHex)) else null
    } catch (_: Exception) { null }

    Column {
        Text("MD3 Expressive Themes", style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp))

        // Auto/default option
        Row(modifier = Modifier.fillMaxWidth().clickable { onColorSelected("") }
            .padding(vertical = 8.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Check, null, Modifier.size(16.dp),
                tint = if (selectedColor == null) MaterialTheme.colorScheme.primary else Color.Transparent)
            Spacer(Modifier.width(8.dp))
            Text("Dynamic (System)", style = MaterialTheme.typography.bodyMedium)
        }

        MD3E_THEMES.forEach { theme ->
            val isSelected = selectedColor?.toArgb() == theme.colors[0].toArgb()
            Row(modifier = Modifier.fillMaxWidth().clickable { onColorSelected(String.format("#%06X", 0xFFFFFF and theme.colors[0].toArgb())) }
                .padding(vertical = 8.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Check, null, Modifier.size(16.dp),
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                Spacer(Modifier.width(8.dp))
                // Three color dots horizontal
                theme.colors.forEach { c ->
                    Spacer(Modifier.size(24.dp).clip(RoundedCornerShape(6.dp)).background(c))
                    Spacer(Modifier.width(6.dp))
                }
                Spacer(Modifier.width(4.dp))
                Text(theme.name, style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

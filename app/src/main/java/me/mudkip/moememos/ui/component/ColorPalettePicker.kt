package me.mudkip.moememos.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

data class PalettePreset(val name: String, val seed: Color)

val MD3E_PRESETS = listOf(
    PalettePreset("Sapphire", Color(0xFF415F91)),
    PalettePreset("Ruby", Color(0xFF9C4146)),
    PalettePreset("Emerald", Color(0xFF3B6E46)),
    PalettePreset("Amber", Color(0xFF9E6100)),
    PalettePreset("Violet", Color(0xFF7B4FBF)),
    PalettePreset("Rose", Color(0xFFB93C66)),
    PalettePreset("Teal", Color(0xFF006B5E)),
    PalettePreset("Slate", Color(0xFF5E6068)),
    PalettePreset("Coral", Color(0xFFCD5B3F)),
    PalettePreset("Lavender", Color(0xFF8A6DC7)),
    PalettePreset("Mint", Color(0xFF3F8E6B)),
    PalettePreset("Sky", Color(0xFF3B6EB5)),
)

@Composable
fun ColorPalettePicker(
    selectedColorHex: String,
    onColorSelected: (String) -> Unit
) {
    val selectedColor = try {
        if (selectedColorHex.isNotBlank()) Color(android.graphics.Color.parseColor(selectedColorHex)) else null
    } catch (_: Exception) { null }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Presets", style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PaletteSwatch(null, selectedColor == null, "Auto") { onColorSelected("") }
            MD3E_PRESETS.take(6).forEach { preset ->
                PaletteSwatch(preset.seed, selectedColor?.toArgb() == preset.seed.toArgb(), preset.name) {
                    onColorSelected(String.format("#%06X", 0xFFFFFF and preset.seed.toArgb()))
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MD3E_PRESETS.drop(6).forEach { preset ->
                PaletteSwatch(preset.seed, selectedColor?.toArgb() == preset.seed.toArgb(), preset.name) {
                    onColorSelected(String.format("#%06X", 0xFFFFFF and preset.seed.toArgb()))
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Custom", style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(horizontal = 4.dp))

        var hexInput by remember(selectedColorHex) { mutableStateOf(selectedColorHex.ifBlank { "" }) }
        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(36.dp).clip(CircleShape).background(
                selectedColor ?: Brush.horizontalGradient(
                    listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta))))
            Spacer(Modifier.width(12.dp))
            OutlinedTextField(value = hexInput, onValueChange = { v ->
                hexInput = v.take(7)
                if (v.length == 7 && v.startsWith("#")) {
                    try { android.graphics.Color.parseColor(v); onColorSelected(v) } catch (_: Exception) { }
                }
            }, modifier = Modifier.weight(1f),
                placeholder = { Text("#RRGGBB", style = MaterialTheme.typography.bodySmall) },
                singleLine = true, textStyle = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun PaletteSwatch(color: Color?, isSelected: Boolean, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .then(if (color != null) Modifier.background(color)
            else Modifier.background(
                Brush.sweepGradient(listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red))))
            .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected)
                Icon(Icons.Filled.Check, null, Modifier.size(20.dp),
                    tint = if (color != null) Color.White else MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

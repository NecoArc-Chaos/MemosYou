package me.mudkip.moememos.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
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

    fun hex(p: PalettePreset) = String.format("#%06X", 0xFFFFFF and p.seed.toArgb())
    fun isSel(p: PalettePreset) = selectedColor?.toArgb() == p.seed.toArgb()

    Column {
        Text("Presets",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp))

        ColorCircle(null, selectedColor == null, "Auto") { onColorSelected("") }

        Spacer(Modifier.height(8.dp))

        MD3E_PRESETS.forEach { preset ->
            ColorCircle(preset.seed, isSel(preset), preset.name) { onColorSelected(hex(preset)) }
        }
    }
}

@Composable
private fun ColorCircle(
    color: Color?,
    isSelected: Boolean,
    label: String,
    onClick: () -> Unit
) {
    val brush: Brush = if (color != null) SolidColor(color)
    else Brush.sweepGradient(listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red))

    val mod = Modifier
        .fillMaxWidth()
        .padding(vertical = 2.dp, horizontal = 4.dp)
        .clip(MaterialTheme.shapes.small)
        .clickable(onClick = onClick)
        .padding(vertical = 10.dp, horizontal = 12.dp)

    androidx.compose.foundation.layout.Column(modifier = mod) {
        Box(Modifier.size(32.dp).clip(CircleShape).background(brush)
            .then(if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape) else Modifier),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) Icon(Icons.Filled.Check, null, Modifier.size(16.dp), tint = Color.White)
        }
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
    }
}

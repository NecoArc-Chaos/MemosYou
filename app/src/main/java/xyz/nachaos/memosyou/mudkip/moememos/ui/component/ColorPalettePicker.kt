package xyz.nachaos.memosyou.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import xyz.nachaos.memosyou.ui.theme.PresetTheme
import xyz.nachaos.memosyou.ui.theme.Presets

@Composable
fun ThemePresetPicker(selectedId: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Auto / System
        val isAuto = selectedId.isBlank() || selectedId == "auto"
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clip(RoundedCornerShape(16.dp))
                .clickable(remember { MutableInteractionSource() }, LocalIndication.current, onClick = { onSelect("auto") })
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Canvas(Modifier.clip(CircleShape).size(48.dp)) {
                    drawCircle(Brush.sweepGradient(listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red)))
                    drawCircle(color = Color.White, radius = if (isAuto) 12.dp.toPx() else 6.dp.toPx(), center = Offset(size.width / 2, size.height / 2))
                }
                if (isAuto) Icon(Icons.Filled.Check, null, Modifier.size(18.dp), tint = Color.Black)
            }
            Text("Auto", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }

        Presets.forEach { theme ->
            val sel = theme.id == selectedId
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clip(RoundedCornerShape(16.dp))
                    .clickable(remember { MutableInteractionSource() }, LocalIndication.current, onClick = { onSelect(theme.id) })
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val s = theme.get(false)
                    Canvas(Modifier.clip(CircleShape).size(48.dp)) {
                        drawRect(color = s.primaryContainer, size = size)
                        drawRect(color = s.secondaryContainer, size = size, topLeft = Offset(size.width / 2, 0f))
                        drawRect(color = s.tertiaryContainer, size = size, topLeft = Offset(size.width / 2, size.height / 2))
                        drawCircle(color = s.primary, radius = if (sel) 12.dp.toPx() else 8.dp.toPx(), center = Offset(size.width / 2, size.height / 2))
                    }
                    if (sel) Icon(Icons.Filled.Check, null, Modifier.size(18.dp), tint = s.onPrimary)
                }
                Text(theme.name, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

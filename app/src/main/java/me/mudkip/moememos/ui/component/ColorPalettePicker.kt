package me.mudkip.moememos.ui.component

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
import androidx.compose.ui.unit.dp
import me.mudkip.moememos.ui.theme.PresetTheme
import me.mudkip.moememos.ui.theme.Presets

@Composable
fun ThemePresetPicker(selectedId: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Presets.forEach { theme ->
            ThemePresetButton(theme = theme, selected = theme.id == selectedId, onClick = { onSelect(theme.id) })
        }
    }
}

@Composable
private fun ThemePresetButton(theme: PresetTheme, selected: Boolean, onClick: () -> Unit) {
    val scheme = theme.get(false)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(remember { MutableInteractionSource() }, LocalIndication.current, onClick = onClick)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(Modifier.clip(CircleShape).size(48.dp)) {
                drawRect(color = scheme.primaryContainer, size = size)
                drawRect(color = scheme.secondaryContainer, size = size, topLeft = Offset(size.width / 2, 0f))
                drawRect(color = scheme.tertiaryContainer, size = size, topLeft = Offset(size.width / 2, size.height / 2))
                drawCircle(color = scheme.primary, radius = if (selected) 12.dp.toPx() else 8.dp.toPx(), center = Offset(size.width / 2, size.height / 2))
            }
            if (selected) Icon(Icons.Filled.Check, null, Modifier.size(18.dp), tint = scheme.onPrimary)
        }
        Text(theme.name, style = MaterialTheme.typography.labelMedium, color = scheme.primary)
    }
}

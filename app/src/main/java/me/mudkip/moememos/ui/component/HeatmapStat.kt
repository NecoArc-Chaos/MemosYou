package me.mudkip.moememos.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import me.mudkip.moememos.data.model.DailyUsageStat
import java.time.LocalDate

@Composable
fun HeatmapStat(day: DailyUsageStat) {
    val borderWidth = if (day.date == LocalDate.now()) 1.dp else 0.dp
    val colorScheme = MaterialTheme.colorScheme
    val color = when (day.count) {
        0 -> colorScheme.surfaceContainerHighest
        1 -> colorScheme.primaryContainer.copy(alpha = 0.5f)
        2 -> colorScheme.primaryContainer.copy(alpha = 0.7f)
        in 3..4 -> colorScheme.primary.copy(alpha = 0.7f)
        else -> colorScheme.primary
    }
    val shape = MaterialTheme.shapes.extraSmall
    var modifier = Modifier
        .fillMaxSize()
        .aspectRatio(1F, true)
        .clip(shape)
        .background(color = color)
    if (day.date == LocalDate.now()) {
        modifier = modifier.border(
            borderWidth,
            colorScheme.onBackground,
            shape = shape
        )
    }

    Box(modifier = modifier)
}

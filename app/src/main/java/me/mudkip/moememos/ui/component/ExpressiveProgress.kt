package xyz.nachaos.memosyou.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ═══ MD3 Expressive Loading Animations ═══

@Composable
fun ExpressiveWaveProgress(
    modifier: Modifier = Modifier,
    height: Dp = 3.dp,
    amplitude: Dp = 6.dp
) {
    val color = MaterialTheme.colorScheme.primary
    val transition = rememberInfiniteTransition("wave")
    val phase by transition.animateFloat(0f, 360f,
        infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Restart), "phase")
    val glow by transition.animateFloat(0f, 1f,
        infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse), "glow")

    Canvas(modifier = modifier.fillMaxWidth().height(height + amplitude * 2)) {
        val amp = amplitude.toPx()
        val midY = size.height / 2
        val waveLen = size.width / 3
        val path = Path()
        path.moveTo(0f, midY)
        var x = 0f
        while (x < size.width) {
            val angle = ((x / waveLen) * 360f + phase) * Math.PI.toFloat() / 180f
            path.lineTo(x, midY + kotlin.math.sin(angle) * amp)
            x += 3f
        }
        drawPath(path, Brush.horizontalGradient(
            listOf(color.copy(alpha = 0.1f), lerp(color, Color.White, glow), color.copy(alpha = 0.1f))),
            style = Stroke(width = height.toPx(), cap = StrokeCap.Round))
    }
}

@Composable
fun ExpressivePulseIndicator(modifier: Modifier = Modifier, size: Dp = 12.dp) {
    val color = MaterialTheme.colorScheme.primary
    val transition = rememberInfiniteTransition("pulse")
    val scale by transition.animateFloat(0.3f, 1f,
        infiniteRepeatable(tween(800), RepeatMode.Reverse), "scale")
    val alpha by transition.animateFloat(0.3f, 1f,
        infiniteRepeatable(tween(800), RepeatMode.Reverse), "alpha")
    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(size * scale)) {
            drawCircle(color = color.copy(alpha = alpha), radius = size.toPx() / 2)
        }
    }
}

@Composable
fun ExpressiveSkeletonCard(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition("skeleton")
    val shimmer by transition.animateFloat(-1f, 2f,
        infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Restart), "shimmer")

    val brush = Brush.linearGradient(
        listOf(
            MaterialTheme.colorScheme.surfaceContainerHighest,
            MaterialTheme.colorScheme.surfaceContainer,
            MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        start = Offset(shimmer * 500f, 0f),
        end = Offset(shimmer * 500f + 500f, 500f)
    )

    Card(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(Modifier.fillMaxWidth()) {
                Box(Modifier.size(32.dp).clip(CircleShape).background(brush))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Box(Modifier.fillMaxWidth(0.5f).height(14.dp).clip(RoundedCornerShape(4.dp)).background(brush))
                    Spacer(Modifier.height(6.dp))
                    Box(Modifier.fillMaxWidth(0.3f).height(10.dp).clip(RoundedCornerShape(4.dp)).background(brush))
                }
            }
            Spacer(Modifier.height(16.dp))
            Box(Modifier.fillMaxWidth().height(14.dp).clip(RoundedCornerShape(4.dp)).background(brush))
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth(0.8f).height(14.dp).clip(RoundedCornerShape(4.dp)).background(brush))
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth(0.4f).height(14.dp).clip(RoundedCornerShape(4.dp)).background(brush))
        }
    }
}

@Composable
fun ExpressiveFullScreenLoading(message: String = "Loading…", modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.weight(1f))
        ExpressivePulseIndicator(size = 20.dp)
        Spacer(Modifier.height(16.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(12.dp))
        ExpressiveWaveProgress(Modifier.fillMaxWidth(0.4f))
        Spacer(Modifier.weight(1f))
    }
}

package xyz.nachaos.memosyou.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

object MotionDuration { const val Short=200; const val Medium=300; const val Long=400; const val ExtraLong=500 }
object MotionEasing { val Emphasized=FastOutSlowInEasing; val Decelerate=LinearOutSlowInEasing; val Accelerate=FastOutSlowInEasing }
object MotionSpecs {
    val defaultTransition = tween<Float>(durationMillis=MotionDuration.Medium, easing=MotionEasing.Emphasized)
    val springBounce = spring<Float>(dampingRatio=Spring.DampingRatioMediumBouncy, stiffness=Spring.StiffnessLow)
    val springGentle = spring<Float>(dampingRatio=Spring.DampingRatioLowBouncy, stiffness=Spring.StiffnessVeryLow)
    val springSnap = spring<Float>(dampingRatio=Spring.DampingRatioNoBouncy, stiffness=Spring.StiffnessMediumLow)
}

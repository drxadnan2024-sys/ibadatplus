package com.ibadatplus.app.tasbeeh.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TasbeehCircularCounter(
    currentCount: Int,
    targetCount: Int,
    progress: Float,
    isTargetReached: Boolean,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Smooth press spring animation
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "CounterScale"
    )

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "ProgressAnimation"
    )

    val primaryGreen = Color(0xFF10B981)
    val glowGreen = Color(0xFF34D399)

    val ringColor by animateColorAsState(
        targetValue = if (isTargetReached) Color(0xFFF59E0B) else primaryGreen,
        label = "RingColor"
    )

    Box(
        modifier = modifier
            .size(280.dp)
            .scale(scale)
            .shadow(
                elevation = if (isPressed) 4.dp else 16.dp,
                shape = CircleShape,
                spotColor = if (isTargetReached) Color(0xFFF59E0B) else primaryGreen
            )
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF0F5132), // Emerald center
                        Color(0xFF064E3B)  // Deep forest edge
                    )
                ),
                shape = CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(bounded = true, color = Color.White),
                onClick = onTap
            ),
        contentAlignment = Alignment.Center
    ) {
        // Circular Progress Arc
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 10.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeftOffset = androidx.compose.ui.geometry.Offset(strokeWidth / 2f, strokeWidth / 2f)
            val arcSize = androidx.compose.ui.geometry.Size(diameter, diameter)

            // Background Track
            drawArc(
                color = Color(0x33FFFFFF),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeftOffset,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Active Progress
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                topLeft = topLeftOffset,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // Inner Counter Display
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$currentCount",
                fontSize = 62.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                color = Color.White,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            Surface(
                color = Color(0x33FFFFFF),
                shape = CircleShape
            ) {
                Text(
                    text = if (targetCount <= 0) "TARGET: ∞ FREE" else "TARGET: $targetCount",
                    color = if (isTargetReached) Color(0xFFFFD700) else Color(0xFFE2E8F0),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "TAP TO COUNT",
                color = Color(0x88FFFFFF),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.5.sp
            )
        }
    }
}

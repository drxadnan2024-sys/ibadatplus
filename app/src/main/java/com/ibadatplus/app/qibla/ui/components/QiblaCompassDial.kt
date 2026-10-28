package com.ibadatplus.app.qibla.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun QiblaCompassDial(
    deviceAzimuth: Float,
    qiblaBearing: Float,
    isAligned: Boolean,
    modifier: Modifier = Modifier
) {
    // Smooth angle animations for fluid rotation
    val animatedAzimuth by animateFloatAsState(
        targetValue = deviceAzimuth,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "CompassDialRotation"
    )

    val animatedNeedleAngle by animateFloatAsState(
        targetValue = (qiblaBearing - deviceAzimuth),
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "QiblaNeedleRotation"
    )

    val primaryGreen = Color(0xFF10B981)
    val darkEmerald = Color(0xFF064E3B)
    val softGold = Color(0xFFF59E0B)

    val dialBorderColor by animateColorAsState(
        targetValue = if (isAligned) primaryGreen else Color(0xFFE5E7EB),
        animationSpec = tween(300),
        label = "BorderColor"
    )

    val dialBackgroundColor by animateColorAsState(
        targetValue = if (isAligned) Color(0xFFECFDF5) else Color.White,
        animationSpec = tween(300),
        label = "BgColor"
    )

    Box(
        modifier = modifier
            .size(300.dp)
            .shadow(
                elevation = if (isAligned) 14.dp else 6.dp,
                shape = CircleShape,
                spotColor = if (isAligned) primaryGreen else Color(0x33000000)
            )
            .background(dialBackgroundColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // 1. Rotating Compass Rose (Cardinals N, E, S, W & Degree Ticks)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .rotate(-animatedAzimuth)
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = (size.minDimension / 2f) - 16.dp.toPx()

            // Outer dial circle
            drawCircle(
                color = dialBorderColor,
                radius = radius,
                center = center,
                style = Stroke(width = if (isAligned) 4.dp.toPx() else 2.dp.toPx())
            )

            // Inner subtle ring
            drawCircle(
                color = Color(0xFFF3F4F6),
                radius = radius * 0.72f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            // Degree marks every 15 degrees
            for (degree in 0 until 360 step 15) {
                val isMajor = (degree % 90 == 0)
                val isMedium = (degree % 30 == 0)

                val tickLength = when {
                    isMajor -> 14.dp.toPx()
                    isMedium -> 9.dp.toPx()
                    else -> 5.dp.toPx()
                }

                val tickColor = when {
                    degree == 0 -> Color(0xFFEF4444) // North is Red
                    isMajor -> Color(0xFF1F2937)
                    isMedium -> Color(0xFF9CA3AF)
                    else -> Color(0xFFD1D5DB)
                }

                val tickWidth = if (isMajor) 3.dp.toPx() else if (isMedium) 2.dp.toPx() else 1.dp.toPx()

                rotate(degrees = degree.toFloat(), pivot = center) {
                    drawLine(
                        color = tickColor,
                        start = Offset(center.x, center.y - radius),
                        end = Offset(center.x, center.y - radius + tickLength),
                        strokeWidth = tickWidth
                    )
                }
            }

            // Draw Kaaba Marker along the outer rim at qiblaBearing
            rotate(degrees = qiblaBearing, pivot = center) {
                val kaabaMarkerCenter = Offset(center.x, center.y - radius)

                // Golden halo around Kaaba marker
                drawCircle(
                    color = softGold,
                    radius = 9.dp.toPx(),
                    center = kaabaMarkerCenter
                )

                // Miniature Black Kaaba cube
                drawRect(
                    color = Color(0xFF111827),
                    topLeft = Offset(kaabaMarkerCenter.x - 5.dp.toPx(), kaabaMarkerCenter.y - 5.dp.toPx()),
                    size = androidx.compose.ui.geometry.Size(10.dp.toPx(), 10.dp.toPx())
                )

                // Golden Kiswah belt
                drawLine(
                    color = Color(0xFFFBBF24),
                    start = Offset(kaabaMarkerCenter.x - 5.dp.toPx(), kaabaMarkerCenter.y - 2.dp.toPx()),
                    end = Offset(kaabaMarkerCenter.x + 5.dp.toPx(), kaabaMarkerCenter.y - 2.dp.toPx()),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }

        // Cardinal Letter Overlays (N, E, S, W) rotating with dial
        Box(
            modifier = Modifier
                .fillMaxSize()
                .rotate(-animatedAzimuth)
        ) {
            Text(
                text = "N",
                color = Color(0xFFDC2626),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 22.dp)
            )
            Text(
                text = "S",
                color = Color(0xFF4B5563),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 22.dp)
            )
            Text(
                text = "E",
                color = Color(0xFF4B5563),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 22.dp)
            )
            Text(
                text = "W",
                color = Color(0xFF4B5563),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 22.dp)
            )
        }

        // 2. The Dynamic Qibla Needle (Points directly towards Kaaba)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .rotate(animatedNeedleAngle)
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val needleLength = (size.minDimension / 2f) - 34.dp.toPx()
            val needleWidth = 13.dp.toPx()

            val needleColor = if (isAligned) primaryGreen else darkEmerald
            val tailColor = if (isAligned) Color(0xFF86EFAC) else Color(0xFFCBD5E1)

            // North/Qibla pointing head (sharp spear)
            val headPath = Path().apply {
                moveTo(center.x, center.y - needleLength)
                lineTo(center.x + needleWidth, center.y - 10.dp.toPx())
                lineTo(center.x, center.y)
                close()
            }
            drawPath(headPath, needleColor)

            val headPathLeft = Path().apply {
                moveTo(center.x, center.y - needleLength)
                lineTo(center.x - needleWidth, center.y - 10.dp.toPx())
                lineTo(center.x, center.y)
                close()
            }
            drawPath(headPathLeft, if (isAligned) Color(0xFF059669) else Color(0xFF047857))

            // South/Opposite tail
            val tailPath = Path().apply {
                moveTo(center.x, center.y + (needleLength * 0.45f))
                lineTo(center.x + (needleWidth * 0.8f), center.y + 10.dp.toPx())
                lineTo(center.x, center.y)
                close()
            }
            drawPath(tailPath, tailColor)

            val tailPathLeft = Path().apply {
                moveTo(center.x, center.y + (needleLength * 0.45f))
                lineTo(center.x - (needleWidth * 0.8f), center.y + 10.dp.toPx())
                lineTo(center.x, center.y)
                close()
            }
            drawPath(tailPathLeft, if (isAligned) Color(0xFF4ADE80) else Color(0xFF94A3B8))

            // Center Pin
            drawCircle(
                color = if (isAligned) primaryGreen else Color(0xFF1E293B),
                radius = 10.dp.toPx(),
                center = center
            )
            drawCircle(
                color = if (isAligned) Color(0xFFFEF08A) else Color.White,
                radius = 4.dp.toPx(),
                center = center
            )
        }

        // 3. Mini Kaaba Icon in center when aligned
        if (isAligned) {
            Surface(
                shape = CircleShape,
                color = primaryGreen,
                modifier = Modifier
                    .size(46.dp)
                    .align(Alignment.Center)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "🕋",
                        fontSize = 20.sp
                    )
                }
            }
        }
    }
}

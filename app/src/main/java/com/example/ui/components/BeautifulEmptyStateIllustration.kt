package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun BeautifulEmptyStateIllustration(
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    
    // Slow infinite animations for organic floating parallax effect
    val infiniteTransition = rememberInfiniteTransition(label = "IllustrationMotion")
    
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Float"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Canvas(modifier = modifier.size(130.dp)) {
        val w = size.width
        val h = size.height
        val centerX = w / 2f
        val centerY = h / 2f + floatAnim

        // 1. Draw dynamic ambient glow circle in the background
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(primaryColor.copy(alpha = 0.22f * pulseAlpha), Color.Transparent),
                center = Offset(centerX, centerY),
                radius = w * 0.48f
            ),
            center = Offset(centerX, centerY),
            radius = w * 0.48f
        )

        // 2. Draw outer glass-like ring (Focus Bridge time ring)
        drawCircle(
            brush = Brush.linearGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.5f),
                    secondaryColor.copy(alpha = 0.08f)
                ),
                start = Offset(centerX - w * 0.35f, centerY - h * 0.35f),
                end = Offset(centerX + w * 0.35f, centerY + h * 0.35f)
            ),
            center = Offset(centerX, centerY),
            radius = w * 0.30f,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // 3. Draw a stylized calendar/grid card in the center
        val cardWidth = w * 0.36f
        val cardHeight = h * 0.42f
        val cardX = centerX - cardWidth / 2f
        val cardY = centerY - cardHeight / 2f

        // Draw Calendar card shadow/glow
        drawRoundRect(
            color = Color.Black.copy(alpha = if (isDark) 0.15f else 0.04f),
            topLeft = Offset(cardX + 4.dp.toPx(), cardY + 5.dp.toPx()),
            size = Size(cardWidth, cardHeight),
            cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
        )

        // Calendar base
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = if (isDark) {
                    listOf(Color(0xFF24223B), Color(0xFF16152B))
                } else {
                    listOf(Color.White, Color(0xFFECEFF8))
                }
            ),
            topLeft = Offset(cardX, cardY),
            size = Size(cardWidth, cardHeight),
            cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
            style = androidx.compose.ui.graphics.drawscope.Fill
        )

        // Calendar border
        drawRoundRect(
            color = primaryColor.copy(alpha = 0.25f),
            topLeft = Offset(cardX, cardY),
            size = Size(cardWidth, cardHeight),
            cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
            style = Stroke(width = 1.5.dp.toPx())
        )

        // Calendar header bar
        val headerHeight = cardHeight * 0.28f
        drawRoundRect(
            brush = Brush.horizontalGradient(
                colors = listOf(primaryColor, secondaryColor)
            ),
            topLeft = Offset(cardX, cardY),
            size = Size(cardWidth, headerHeight),
            cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
        )

        // Trim the rounded bottom corner of the header by overlaying a rect slightly
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(primaryColor, secondaryColor)
            ),
            topLeft = Offset(cardX, cardY + headerHeight / 2f),
            size = Size(cardWidth, headerHeight / 2f)
        )

        // Drawing calendar rings (binder hooks on top)
        drawRoundRect(
            color = secondaryColor.copy(alpha = 0.8f),
            topLeft = Offset(cardX + cardWidth * 0.25f - 2.dp.toPx(), cardY - 5.dp.toPx()),
            size = Size(4.dp.toPx(), 8.dp.toPx()),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
        )
        drawRoundRect(
            color = secondaryColor.copy(alpha = 0.8f),
            topLeft = Offset(cardX + cardWidth * 0.75f - 2.dp.toPx(), cardY - 5.dp.toPx()),
            size = Size(4.dp.toPx(), 8.dp.toPx()),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
        )

        // Stylized lines inside the card
        val lineX = cardX + cardWidth * 0.15f
        val lineW1 = cardWidth * 0.7f
        val lineW2 = cardWidth * 0.45f
        val lineYStart = cardY + headerHeight + 8.dp.toPx()
        val spacingY = 7.dp.toPx()
        val strokeW = 2.5f.dp.toPx()

        drawLine(
            color = primaryColor.copy(alpha = 0.18f),
            start = Offset(lineX, lineYStart),
            end = Offset(lineX + lineW1, lineYStart),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )

        drawLine(
            color = primaryColor.copy(alpha = 0.18f),
            start = Offset(lineX, lineYStart + spacingY),
            end = Offset(lineX + lineW2, lineYStart + spacingY),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )

        drawLine(
            color = primaryColor.copy(alpha = 0.18f),
            start = Offset(lineX, lineYStart + spacingY * 2),
            end = Offset(lineX + lineW1 * 0.75f, lineYStart + spacingY * 2),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )

        // Accent sparkles / star shapes near the main calendar object
        val starX = centerX + w * 0.28f
        val starY = centerY - h * 0.20f
        drawCircle(
            color = secondaryColor.copy(alpha = 0.8f),
            center = Offset(starX, starY),
            radius = 3.dp.toPx()
        )
        drawCircle(
            color = primaryColor.copy(alpha = 0.8f),
            center = Offset(centerX - w * 0.28f, centerY + h * 0.22f),
            radius = 4.dp.toPx()
        )
    }
}

package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // Core base gradient colors matching deep navy background (#090B24)
    val baseBgStart = DeepNavyBg
    val baseBgEnd = Color(0xFF040510) // Ultra-rich dark navy shadow
    
    // Glowing blob colors
    val blobColor1 = BlobColorPrimary
    val blobColor2 = BlobColorSecondary
    val blobColor3 = BlobColorTertiary

    // Slow organic motion animation
    val infiniteTransition = rememberInfiniteTransition(label = "AuroraMotion")
    
    val timeAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(22000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Time"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(baseBgStart, baseBgEnd)
                )
            )
            .drawBehind {
                val width = size.width
                val height = size.height
                
                // Calculate dynamic positions using trig functions for smooth organic loops
                val x1 = width * (0.3f + 0.15f * sin(timeAnim))
                val y1 = height * (0.2f + 0.12f * cos(timeAnim))
                
                val x2 = width * (0.7f + 0.18f * cos(timeAnim + 1f))
                val y2 = height * (0.7f + 0.15f * sin(timeAnim + 1f))
                
                val x3 = width * (0.4f + 0.2f * sin(timeAnim * 0.5f + 2f))
                val y3 = height * (0.5f + 0.2f * cos(timeAnim * 0.5f + 2f))

                // Draw Blob 1
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(blobColor1, Color.Transparent),
                        center = Offset(x1, y1),
                        radius = width * 0.65f
                    ),
                    center = Offset(x1, y1),
                    radius = width * 0.65f
                )

                // Draw Blob 2
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(blobColor2, Color.Transparent),
                        center = Offset(x2, y2),
                        radius = width * 0.75f
                    ),
                    center = Offset(x2, y2),
                    radius = width * 0.75f
                )

                // Draw Blob 3
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(blobColor3, Color.Transparent),
                        center = Offset(x3, y3),
                        radius = width * 0.55f
                    ),
                    center = Offset(x3, y3),
                    radius = width * 0.55f
                )
            }
    ) {
        content()
    }
}

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
    val isDark = isSystemInDarkTheme()
    
    // Core base gradient colors
    val baseBgStart = if (isDark) Color(0xFF07050E) else Color(0xFFF1F3F9)
    val baseBgEnd = if (isDark) Color(0xFF14132B) else Color(0xFFE8ECF5)
    
    // Glowing blob colors
    val blobColor1 = if (isDark) Color(0x2906B6D4) else Color(0x184F46E5) // Cyan / Indigo
    val blobColor2 = if (isDark) Color(0x228B5CF6) else Color(0x14E64899) // Purple / Pink
    val blobColor3 = if (isDark) Color(0x1AEC4899) else Color(0x100EA5E9) // Rose / Sky Blue

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

package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp, // Cozy M3-friendly corner radius
    borderWidth: Dp = 1.2.dp,
    elevation: Dp = 12.dp, // Higher elevation for premium layered shadow depth
    content: @Composable BoxScope.() -> Unit
) {
    // Premium frosted indigo glass diagonal reflection gradient
    val backgroundBrush = Brush.linearGradient(
        colors = listOf(
            Color(0x361F2459), // Brighter top-left glass overlay (approx 21% opacity)
            Color(0x1E121535)  // Rich indigo glass body (approx 12% opacity)
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )
    
    // Diagonal border brush with soft violet-to-lavender highlights
    val borderBrush = Brush.linearGradient(
        colors = listOf(
            Color(0x668B7BFF), // Top-left high specularity soft violet highlight (40% opacity)
            Color(0x12D6CFFF)  // Bottom-right lavender fade (7% opacity)
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                clip = false,
                ambientColor = Color(0xFF090B24).copy(alpha = 0.5f),
                spotColor = SoftVioletAccent.copy(alpha = 0.25f) // Gorgeous futuristic neon ambient glow shadow
            )
            .clip(shape)
            .background(backgroundBrush)
            .border(BorderStroke(borderWidth, borderBrush), shape)
    ) {
        Box(
            modifier = Modifier.padding(18.dp),
            content = content
        )
    }
}

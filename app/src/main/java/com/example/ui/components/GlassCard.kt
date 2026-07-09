package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
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
    cornerRadius: Dp = 28.dp, // Premium 28dp radius default
    borderWidth: Dp = 1.2.dp,
    elevation: Dp = 8.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    
    // Premium frosted glass diagonal light reflection gradient
    val backgroundBrush = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                Color(0x1CFFFFFF), // ~11% White
                Color(0x06FFFFFF)  // ~2.5% White
            ),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xCCFFFFFF), // ~80% White
                Color(0x73FFFFFF)  // ~45% White
            ),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    }
    
    // Diagonal border brush to capture bright reflection highlights on edges
    val borderBrush = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                Color(0x3DFFFFFF), // ~24% White (Highlights top-left)
                Color(0x0AFFFFFF)  // ~4% White (Fades bottom-right)
            ),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0x8CFFFFFF), // ~55% White (Light specular highlight)
                Color(0x24000000)  // ~14% Black (Shadow outline)
            ),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    }

    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                clip = false,
                ambientColor = Color.Black.copy(alpha = if (isDark) 0.15f else 0.05f),
                spotColor = Color.Black.copy(alpha = if (isDark) 0.25f else 0.08f)
            )
            .clip(shape)
            .background(backgroundBrush)
            .border(BorderStroke(borderWidth, borderBrush), shape)
    ) {
        Box(
            modifier = Modifier.padding(18.dp), // Premium slightly larger padding for spaciousness
            content = content
        )
    }
}

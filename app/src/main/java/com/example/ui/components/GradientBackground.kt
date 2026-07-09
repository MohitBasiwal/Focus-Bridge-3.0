package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    
    val backgroundBrush = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                DarkBackgroundStart,
                Color(0xFF131127),
                DarkBackgroundEnd
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                LightBackgroundStart,
                Color(0xFFEDE9FE),
                LightBackgroundEnd
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        content()
    }
}

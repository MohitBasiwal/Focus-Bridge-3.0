package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    secondary = SecondaryDark,
    tertiary = TertiaryDark,
    background = DeepNavyBg,
    surface = IndigoGlass,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextPrimaryWhite,
    onSurface = TextPrimaryWhite
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    secondary = SecondaryLight,
    tertiary = TertiaryLight,
    background = DeepNavyBg, // Force Deep Navy Background even in light mode for cohesive premium design
    surface = IndigoGlass,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextPrimaryWhite,
    onSurface = TextPrimaryWhite
)

@Composable
fun FocusBridgeTheme(
    darkTheme: Boolean = true, // Default to true for futuristic sci-fi vibe
    dynamicColor: Boolean = false, // Set to false by default so our theme is preserved
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}


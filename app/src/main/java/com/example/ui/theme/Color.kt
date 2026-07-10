package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Premium Futuristic Glass Color Palette (Design Reference)
val DeepNavyBg = Color(0xFF090B24)
val IndigoGlass = Color(0x2C121535)       // Frosted indigo glass
val IndigoGlassLight = Color(0x4D1A1E4A)  // Lighter indigo glass surface
val SoftVioletAccent = Color(0xFF8B7BFF)  // Main brand color
val LavenderHighlight = Color(0xFFD6CFFF) // Bright accent highlights
val TextPrimaryWhite = Color(0xFFFFFFFF)
val TextSecondaryGray = Color(0xFFABABBF)
val GlassBorderLowOpacity = Color(0x338B7BFF) // Low-opacity soft violet/lavender border

// Background Gradients
val DarkBackgroundStart = Color(0xFF090B24)
val DarkBackgroundEnd = Color(0xFF040510)

// Dynamic aurora blob colors for GradientBackground
val BlobColorPrimary = Color(0x2D8B7BFF)   // Moving Soft Violet
val BlobColorSecondary = Color(0x22121535) // Deep Indigo Blur
val BlobColorTertiary = Color(0x1ED6CFFF)  // Lavender Glow

// Legacy compatibility values (mapped to new palette to prevent compile errors)
val GlassWhite10 = IndigoGlass
val GlassWhite20 = Color(0x40121535)
val GlassWhite70 = Color(0xB3FFFFFF)
val GlassWhite80 = Color(0xCCFFFFFF)

val GlassBorderDark = GlassBorderLowOpacity
val GlassBorderLight = Color(0x408B7BFF)

val NeonCyan = SoftVioletAccent
val NeonCyanVariant = Color(0xFF7A6AFF)
val NeonViolet = LavenderHighlight

val DeepIndigo = Color(0xFF1E214A)
val SoftPink = LavenderHighlight
val LightBackgroundStart = Color(0xFF090B24) // Map light background to deep navy for dark first UI
val LightBackgroundEnd = Color(0xFF040510)

val PrimaryDark = SoftVioletAccent
val SecondaryDark = LavenderHighlight
val TertiaryDark = Color(0xFFB5A9FF)

val PrimaryLight = SoftVioletAccent
val SecondaryLight = LavenderHighlight
val TertiaryLight = Color(0xFFB5A9FF)

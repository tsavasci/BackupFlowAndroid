package com.example.backupflow.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Core Brand Palette from App Icon
val DarkBg = Color(0xFF0F141C)
val DarkSurface = Color(0xFF171F2C)
val DarkSurfaceElevated = Color(0xFF202B3D)
val DarkBorder = Color(0xFF2A384E)

// Gradient Accent Colors
val CyanAccent = Color(0xFF38C3F8)
val ElectricBlue = Color(0xFF2A72E5)
val PurpleAccent = Color(0xFF8B5CF6)
val MagentaPink = Color(0xFFE052D0)
val NeonGreen = Color(0xFF34D399)
val AmberWarning = Color(0xFFF59E0B)
val RoseError = Color(0xFFF43F5E)

// Text Colors
val TextPrimary = Color(0xFFF3F4F6)
val TextSecondary = Color(0xFF9CA3AF)
val TextMuted = Color(0xFF6B7280)

// Pre-packaged Brushes for Glassmorphic components
val PrimaryGradient = Brush.horizontalGradient(
    colors = listOf(CyanAccent, ElectricBlue, PurpleAccent)
)

val SecondaryGradient = Brush.horizontalGradient(
    colors = listOf(MagentaPink, PurpleAccent, ElectricBlue)
)

val HeroCardGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF1E293B),
        Color(0xFF0F172A)
    )
)

val CardBorderGradient = Brush.horizontalGradient(
    colors = listOf(
        CyanAccent.copy(alpha = 0.5f),
        PurpleAccent.copy(alpha = 0.3f),
        Color.Transparent
    )
)

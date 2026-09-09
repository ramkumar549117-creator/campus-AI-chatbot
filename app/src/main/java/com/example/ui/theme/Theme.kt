package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FuturisticDarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color(0xFF04101A),
    primaryContainer = Color(0xFF003847),
    onPrimaryContainer = NeonCyanLight,
    secondary = CyberViolet,
    onSecondary = Color(0xFF131838),
    secondaryContainer = Color(0xFF272F55),
    onSecondaryContainer = Color(0xFFD6DBFF),
    tertiary = CyberPink,
    onTertiary = Color(0xFF380820),
    background = CyberDarkBg,
    onBackground = TextPrimary,
    surface = CyberSurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = CyberCardDark,
    onSurfaceVariant = TextSecondary,
    outline = NeonCyanDark,
    outlineVariant = CyberCardBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to futuristic dark
    dynamicColor: Boolean = false, // Keep distinctive futuristic neon theme
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FuturisticDarkColorScheme,
        typography = Typography,
        content = content
    )
}


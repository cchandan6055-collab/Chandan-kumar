package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MGAiColorScheme = darkColorScheme(
    primary = NeonPinkPrimary,
    onPrimary = Color.White,
    primaryContainer = NeonPinkContainer,
    onPrimaryContainer = NeonPinkOnContainer,
    secondary = NeonPinkSecondary,
    onSecondary = Color.White,
    secondaryContainer = NeonPinkContainer,
    onSecondaryContainer = NeonPinkOnContainer,
    tertiary = CyberVioletAccent,
    onTertiary = Color.White,
    tertiaryContainer = CyberVioletContainer,
    onTertiaryContainer = Color(0xFFDDD2FF),
    background = CyberBlackBackground,
    onBackground = TextPrimary,
    surface = CyberDarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = CyberCardSurface,
    onSurfaceVariant = TextSecondary,
    surfaceContainerHighest = CyberCardElevated,
    outline = CyberBorder,
    outlineVariant = CyberBorderNeon,
    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorContainer,
    onErrorContainer = Color(0xFFFFB4AB)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Preserve bespoke neon pink & black branding
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MGAiColorScheme,
        typography = Typography,
        content = content
    )
}

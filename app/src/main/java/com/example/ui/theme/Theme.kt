package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SafeLockColorScheme = darkColorScheme(
    primary = SafeLockCyan,
    onPrimary = Color(0xFF021B2B),
    primaryContainer = Color(0xFF0C3854),
    onPrimaryContainer = Color(0xFFBCE6FF),
    secondary = SafeLockIndigo,
    onSecondary = Color(0xFF13183E),
    secondaryContainer = Color(0xFF2E356A),
    onSecondaryContainer = Color(0xFFD7DBFF),
    tertiary = SafeLockEmerald,
    onTertiary = Color(0xFF022B18),
    background = SafeLockBackground,
    onBackground = SafeLockTextPrimary,
    surface = SafeLockSurface,
    onSurface = SafeLockTextPrimary,
    surfaceVariant = SafeLockSurfaceVariant,
    onSurfaceVariant = SafeLockTextSecondary,
    outline = SafeLockBorder,
    error = SafeLockRose,
    onError = Color(0xFF2C050B)
)

@Composable
fun SafeLockTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SafeLockColorScheme,
        typography = Typography,
        content = content
    )
}

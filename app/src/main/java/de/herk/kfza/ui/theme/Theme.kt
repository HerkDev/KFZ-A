package de.herk.kfza.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme

private val LightColorScheme = lightColorScheme(
    primary = KfzaInputFocusedBorder,
    onPrimary = Color.White,
    primaryContainer = KfzaTopBar,
    onPrimaryContainer = Color.White,
    background = KfzaBackground,
    onBackground = KfzaPrimaryText,
    surface = KfzaCardBackground,
    onSurface = KfzaPrimaryText,
    onSurfaceVariant = KfzaSecondaryText,
    outline = KfzaInputBorder,
    outlineVariant = KfzaDivider
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF9FC7F2),
    onPrimary = Color(0xFF06233F),
    primaryContainer = Color(0xFF1B4C78),
    onPrimaryContainer = Color.White,
    background = Color(0xFF101419),
    onBackground = Color(0xFFE9EEF3),
    surface = Color(0xFF1A2027),
    onSurface = Color(0xFFE9EEF3),
    onSurfaceVariant = Color(0xFFC1CAD3),
    outline = Color(0xFFACB8C4),
    outlineVariant = Color(0xFF56616D)
)

@Composable
fun KFZATheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}


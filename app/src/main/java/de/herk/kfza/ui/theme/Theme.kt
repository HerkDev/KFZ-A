package de.herk.kfza.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = KfzaInputFocusedBorder,
    onPrimary = Color.White,
    background = KfzaBackground,
    onBackground = KfzaPrimaryText,
    surface = KfzaBackground,
    onSurface = KfzaPrimaryText,
    onSurfaceVariant = KfzaSecondaryText,
    outline = KfzaInputBorder,
    outlineVariant = KfzaDivider
)

@Composable
fun KFZATheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}


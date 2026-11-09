
package com.example.autoclassify.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = PrimaryCoral,
    onPrimary = TextOnPrimary,

    secondary = PrimaryCoral,
    onSecondary = TextOnPrimary,

    tertiary = PrimaryCoral,
    onTertiary = TextOnPrimary,

    background = AppBackground,
    onBackground = TextPrimary,

    surface = CardBackground,
    onSurface = TextPrimary,

    surfaceVariant = CardBackground,
    onSurfaceVariant = TextSecondary
)

@Composable
fun AutoClassifyTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}

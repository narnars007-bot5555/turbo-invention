package com.tokar.frez.cnc.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val IndustrialDarkColorScheme = darkColorScheme(
    primary = IndustrialCyan,
    onPrimary = IndustrialDarkBg,
    primaryContainer = IndustrialCardBg,
    onPrimaryContainer = TextPrimary,
    secondary = IndustrialYellow,
    onSecondary = IndustrialDarkBg,
    background = IndustrialDarkBg,
    onBackground = TextPrimary,
    surface = IndustrialCardBg,
    onSurface = TextPrimary,
    outline = IndustrialCardBorder
)

@Composable
fun TokarFrezCncTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = IndustrialDarkColorScheme,
        content = content
    )
}

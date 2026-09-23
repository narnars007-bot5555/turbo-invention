package com.tokar.frez.cnc.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

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

private val IndustrialLightColorScheme = lightColorScheme(
    primary = Color(0xFF007A87),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F7FA),
    onPrimaryContainer = Color(0xFF002022),
    secondary = Color(0xFFB78100),
    onSecondary = Color.White,
    background = Color(0xFFF4F6F8),
    onBackground = Color(0xFF1E242B),
    surface = Color.White,
    onSurface = Color(0xFF1E242B),
    outline = Color(0xFFB0BEC5)
)

@Composable
fun TokarFrezCncTheme(
    useDarkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colors = if (useDarkTheme) IndustrialDarkColorScheme else IndustrialLightColorScheme
    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}

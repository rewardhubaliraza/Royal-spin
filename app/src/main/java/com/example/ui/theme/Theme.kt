package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CasinoGold,
    onPrimary = CasinoPurpleDark,
    primaryContainer = CasinoGoldDark,
    onPrimaryContainer = Color.White,
    secondary = CasinoCyan,
    onSecondary = Color.Black,
    secondaryContainer = CasinoPurpleSurface,
    onSecondaryContainer = CasinoGoldLight,
    tertiary = CasinoGreen,
    onTertiary = Color.Black,
    background = CasinoPurpleDark,
    onBackground = TextPrimary,
    surface = CasinoPurpleCard,
    onSurface = TextPrimary,
    surfaceVariant = CasinoPurpleSurface,
    onSurfaceVariant = TextSecondary,
    outline = CasinoPurpleCardBorder,
    error = CasinoRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

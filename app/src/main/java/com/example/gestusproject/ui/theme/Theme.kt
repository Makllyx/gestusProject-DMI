package com.example.gestusproject.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = OrangePrimary,
    onPrimary = Color.White,
    secondary = NavyPrimary,
    onSecondary = Color.White,
    background = NavyDark,
    onBackground = Color(0xFFEFF3FF),
    surface = NavyDark,
    onSurface = Color(0xFFEFF3FF)
)

private val LightColorScheme = lightColorScheme(
    primary = OrangePrimary,
    onPrimary = Color.White,
    secondary = NavyPrimary,
    onSecondary = Color.White,
    background = Color(0xFFF6F8FF),
    onBackground = NavyDark,
    surface = Color(0xFFFFFFFF),
    onSurface = NavyDark
)

@Composable
fun GestusProjectTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
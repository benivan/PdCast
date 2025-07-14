package com.example.pdcast.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Colors
val Purple200 = Color(0xFFBB86FC)
val Purple500 = Color(0xFF6200EE)
val Purple700 = Color(0xFF3700B3)
val Teal200 = Color(0xFF03DAC5)
val White = Color(0xFFFFFFFF)
val Black = Color(0xFF000000)
val Gray = Color(0xFF6D6D6D)
val LightGray = Color(0xFFF5F5F5)

// Light theme colors
private val LightColorScheme = lightColorScheme(
    primary = Purple500,
    onPrimary = White,
    secondary = Teal200,
    onSecondary = Black,
    background = White,
    onBackground = Black,
    surface = White,
    onSurface = Black,
    error = Color(0xFFB00020),
    onError = White
)

// Dark theme colors
private val DarkColorScheme = darkColorScheme(
    primary = Purple200,
    onPrimary = Black,
    secondary = Teal200,
    onSecondary = Black,
    background = Black,
    onBackground = White,
    surface = Color(0xFF121212),
    onSurface = White,
    error = Color(0xFFCF6679),
    onError = Black
)

// Typography
val Typography = androidx.compose.material3.Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    )
)

// Shapes
val Shapes = androidx.compose.material3.Shapes(
    // You can define custom shapes here
)

@Composable
fun PdCastTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
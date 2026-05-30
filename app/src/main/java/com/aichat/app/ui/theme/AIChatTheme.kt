package com.aichat.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.aichat.app.data.model.ThemeMode

private val LightColors: ColorScheme = lightColorScheme(
    primary = Color(0xFF7A3F2A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF1E5D8),
    onPrimaryContainer = Color(0xFF2F1B12),
    secondary = Color(0xFF2F6F61),
    tertiary = Color(0xFF5E548E),
    background = Color(0xFFFAF7F2),
    surface = Color(0xFFFFFCF7),
    surfaceVariant = Color(0xFFE9E0D5),
    onSurface = Color(0xFF24201B),
    onSurfaceVariant = Color(0xFF686057),
    outline = Color(0xFF8E8175),
    outlineVariant = Color(0xFFD8CDC1),
)

private val DarkColors: ColorScheme = darkColorScheme(
    primary = Color(0xFFE2A184),
    onPrimary = Color(0xFF3F1D11),
    primaryContainer = Color(0xFF4A2B20),
    onPrimaryContainer = Color(0xFFFFE6D8),
    secondary = Color(0xFF8BC7B7),
    tertiary = Color(0xFFC7BDF2),
    background = Color(0xFF171512),
    surface = Color(0xFF211F1B),
    surfaceVariant = Color(0xFF34302A),
    onSurface = Color(0xFFF1EDE6),
    onSurfaceVariant = Color(0xFFCFC5BA),
    outline = Color(0xFF9E9286),
    outlineVariant = Color(0xFF4D463E),
)

@Composable
fun AIChatTheme(
    themeMode: ThemeMode,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        content = content,
    )
}

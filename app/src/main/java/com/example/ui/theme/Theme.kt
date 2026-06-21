package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val SophisticatedDarkColorScheme = darkColorScheme(
    primary = SophisticatedDarkPrimary,
    onPrimary = SophisticatedDarkOnPrimary,
    primaryContainer = SophisticatedDarkPrimaryContainer,
    onPrimaryContainer = SophisticatedDarkOnPrimaryContainer,
    secondary = SophisticatedDarkPrimary,
    onSecondary = SophisticatedDarkOnPrimary,
    background = SophisticatedDarkBackground,
    onBackground = SophisticatedDarkOnBackground,
    surface = SophisticatedDarkSurface,
    onSurface = SophisticatedDarkOnSurface,
    surfaceVariant = SophisticatedDarkSurfaceVariant,
    onSurfaceVariant = SophisticatedDarkOnBackground.copy(alpha = 0.8f),
    outline = SophisticatedDarkOutline,
    outlineVariant = SophisticatedDarkOutlineVariant,
    error = SophisticatedDarkError,
    onError = SophisticatedDarkOnError,
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC)
)

private val SophisticatedLightColorScheme = lightColorScheme(
    primary = Color(0xFF6750A4),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF625B71),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),
    background = Color(0xFFFEF7FF),
    onBackground = Color(0xFF1D1B20),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1D1B20),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B)
)

@Composable
fun MyApplicationTheme(
    themeSetting: String = "System",
    content: @Composable () -> Unit,
) {
    val isDark = when (themeSetting) {
        "Light" -> false
        "Dark" -> true
        else -> isSystemInDarkTheme()
    }
    val colorScheme = if (isDark) SophisticatedDarkColorScheme else SophisticatedLightColorScheme

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

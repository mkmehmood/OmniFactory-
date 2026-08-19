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

enum class ThemeMode {
    SYSTEM,
    DARK,
    LIGHT
}

enum class AccentTheme(
    val displayName: String,
    val darkPrimary: Color,
    val lightPrimary: Color
) {
    AQUA("Aqua Glass", GlassAccents.AquaGlassPrimary, GlassAccents.AquaGlassLight),
    VIOLET("Violet Glass", GlassAccents.VioletGlassPrimary, GlassAccents.VioletGlassLight),
    AMBER("Amber Glass", GlassAccents.AmberGlassPrimary, GlassAccents.AmberGlassLight),
    EMERALD("Emerald Glass", GlassAccents.EmeraldGlassPrimary, GlassAccents.EmeraldGlassLight),
    ICE("Ice Glass", GlassAccents.IceGlassPrimary, GlassAccents.IceGlassLight)
}

@Composable
fun MyApplicationTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    accentTheme: AccentTheme = AccentTheme.AQUA,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val primary = if (isDark) accentTheme.darkPrimary else accentTheme.lightPrimary
    val onPrimary = if (isDark) Color.Black else Color.White

    val darkScheme = darkColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primary.copy(alpha = 0.2f),
        onPrimaryContainer = primary,
        secondary = primary.copy(alpha = 0.85f),
        onSecondary = onPrimary,
        background = PureDarkBackground,
        onBackground = PureDarkTextPrimary,
        surface = PureDarkSurface,
        onSurface = PureDarkTextPrimary,
        surfaceVariant = PureDarkGlassSurface,
        onSurfaceVariant = PureDarkTextSecondary,
        outline = PureDarkGlassBorder
    )

    val lightScheme = lightColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primary.copy(alpha = 0.15f),
        onPrimaryContainer = primary,
        secondary = primary.copy(alpha = 0.85f),
        onSecondary = onPrimary,
        background = PureLightBackground,
        onBackground = PureLightTextPrimary,
        surface = PureLightSurface,
        onSurface = PureLightTextPrimary,
        surfaceVariant = PureLightGlassSurface,
        onSurfaceVariant = PureLightTextSecondary,
        outline = PureLightGlassBorder
    )

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> darkScheme
        else -> lightScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

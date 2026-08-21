package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

enum class ThemeMode {
    SYSTEM,
    DARK,
    LIGHT
}

enum class AccentTheme(
    val displayName: String,
    val description: String,
    val darkPrimary: Color,
    val lightPrimary: Color,
    val previewGradient: List<Color>
) {
    AQUA(
        "Aqua Cyan",
        "Vibrant cyan crystal glow",
        GlassAccents.AquaGlassPrimary,
        GlassAccents.AquaGlassLight,
        listOf(Color(0xFF00E5FF), Color(0xFF00B0FF))
    ),
    VIOLET(
        "Electric Violet",
        "Deep amethyst & neon violet",
        GlassAccents.VioletGlassPrimary,
        GlassAccents.VioletGlassLight,
        listOf(Color(0xFFD500F9), Color(0xFF8E24AA))
    ),
    AMBER(
        "Molten Amber",
        "Warm golden amber glow",
        GlassAccents.AmberGlassPrimary,
        GlassAccents.AmberGlassLight,
        listOf(Color(0xFFFFB300), Color(0xFFFF6F00))
    ),
    EMERALD(
        "Emerald Mint",
        "Vivid neon emerald green",
        GlassAccents.EmeraldGlassPrimary,
        GlassAccents.EmeraldGlassLight,
        listOf(Color(0xFF00E676), Color(0xFF00B0FF))
    ),
    ICE(
        "Arctic Sky",
        "Crisp glacial sky blue",
        GlassAccents.IceGlassPrimary,
        GlassAccents.IceGlassLight,
        listOf(Color(0xFF38BDF8), Color(0xFF0284C7))
    ),
    ROSE(
        "Crimson Rose",
        "Radiant ruby rose pink",
        GlassAccents.RoseGlassPrimary,
        GlassAccents.RoseGlassLight,
        listOf(Color(0xFFF43F5E), Color(0xFFBE123C))
    ),
    INDIGO(
        "Royal Sapphire",
        "Deep ultramarine & indigo",
        GlassAccents.IndigoGlassPrimary,
        GlassAccents.IndigoGlassLight,
        listOf(Color(0xFF6366F1), Color(0xFF4338CA))
    ),
    CORAL(
        "Sunset Coral",
        "Warm luminous coral orange",
        GlassAccents.CoralGlassPrimary,
        GlassAccents.CoralGlassLight,
        listOf(Color(0xFFFB923C), Color(0xFFEA580C))
    ),
    LIME(
        "Cyber Lime",
        "High-contrast neon acid lime",
        GlassAccents.LimeGlassPrimary,
        GlassAccents.LimeGlassLight,
        listOf(Color(0xFFA3E635), Color(0xFF65A30D))
    ),
    SLATE(
        "Titanium Slate",
        "Minimalist titanium monochrome",
        GlassAccents.SlateGlassPrimary,
        GlassAccents.SlateGlassLight,
        listOf(Color(0xFF94A3B8), Color(0xFF475569))
    )
}

enum class ShapeStructureStyle(
    val displayName: String,
    val description: String
) {
    ROUNDED("Rounded Squircle", "Smooth 18dp rounded corners"),
    PILL("Pill / Capsule", "Continuous 50% capsule geometry"),
    LINE_BORDER("Line / Minimalist", "Crisp architectural line frame"),
    SHARP("Geometric Modern", "Sleek sharp 4dp beveled edges")
}

fun ShapeStructureStyle.toButtonShape(): CornerBasedShape = when (this) {
    ShapeStructureStyle.ROUNDED -> RoundedCornerShape(18.dp)
    ShapeStructureStyle.PILL -> RoundedCornerShape(50)
    ShapeStructureStyle.LINE_BORDER -> RoundedCornerShape(8.dp)
    ShapeStructureStyle.SHARP -> RoundedCornerShape(4.dp)
}

fun ShapeStructureStyle.toInputShape(): CornerBasedShape = when (this) {
    ShapeStructureStyle.ROUNDED -> RoundedCornerShape(12.dp)
    ShapeStructureStyle.PILL -> RoundedCornerShape(24.dp)
    ShapeStructureStyle.LINE_BORDER -> RoundedCornerShape(6.dp)
    ShapeStructureStyle.SHARP -> RoundedCornerShape(4.dp)
}

fun ShapeStructureStyle.toCardShape(): CornerBasedShape = when (this) {
    ShapeStructureStyle.ROUNDED -> RoundedCornerShape(18.dp)
    ShapeStructureStyle.PILL -> RoundedCornerShape(24.dp)
    ShapeStructureStyle.LINE_BORDER -> RoundedCornerShape(8.dp)
    ShapeStructureStyle.SHARP -> RoundedCornerShape(4.dp)
}

fun ShapeStructureStyle.toSmallShape(): CornerBasedShape = when (this) {
    ShapeStructureStyle.ROUNDED -> RoundedCornerShape(8.dp)
    ShapeStructureStyle.PILL -> RoundedCornerShape(50)
    ShapeStructureStyle.LINE_BORDER -> RoundedCornerShape(4.dp)
    ShapeStructureStyle.SHARP -> RoundedCornerShape(2.dp)
}

fun ShapeStructureStyle.toChipShape(): CornerBasedShape = when (this) {
    ShapeStructureStyle.ROUNDED -> RoundedCornerShape(10.dp)
    ShapeStructureStyle.PILL -> RoundedCornerShape(50)
    ShapeStructureStyle.LINE_BORDER -> RoundedCornerShape(6.dp)
    ShapeStructureStyle.SHARP -> RoundedCornerShape(3.dp)
}

val LocalShapeStructure = staticCompositionLocalOf { ShapeStructureStyle.ROUNDED }

@Composable
fun MyApplicationTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    accentTheme: AccentTheme = AccentTheme.AQUA,
    shapeStructureStyle: ShapeStructureStyle = ShapeStructureStyle.ROUNDED,
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

    val m3Shapes = Shapes(
        extraSmall = shapeStructureStyle.toSmallShape(),
        small = shapeStructureStyle.toInputShape(),
        medium = shapeStructureStyle.toCardShape(),
        large = shapeStructureStyle.toButtonShape(),
        extraLarge = shapeStructureStyle.toButtonShape()
    )

    CompositionLocalProvider(LocalShapeStructure provides shapeStructureStyle) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = m3Shapes,
            content = content
        )
    }
}

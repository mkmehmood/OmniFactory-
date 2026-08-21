package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CutCornerShape
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    ROUNDED("Rounded Squircle", "Smooth 18dp modern rounded corners"),
    PILL("Pill / Capsule", "Continuous 50% capsule / stadium geometry"),
    LINE_BORDER("Line / Minimalist", "Crisp 6dp architectural line frame"),
    SHARP("Geometric Modern", "Sleek sharp 4dp beveled edges"),
    CHAMFER_CUT("Chamfer Cut Edge", "Futuristic 14dp diagonal cut corners"),
    TEARDROP("Teardrop Leaf", "Dynamic asymmetric organic corner geometry"),
    OVAL_STADIUM("Oval Stadium", "Ultra smooth 32dp deep curved geometry"),
    SOFT_BUBBLE("Soft Cloud Bubble", "Plump 26dp ultra-soft rounded corners")
}

fun ShapeStructureStyle.toButtonShape(): CornerBasedShape = when (this) {
    ShapeStructureStyle.ROUNDED -> RoundedCornerShape(18.dp)
    ShapeStructureStyle.PILL -> RoundedCornerShape(50)
    ShapeStructureStyle.LINE_BORDER -> RoundedCornerShape(8.dp)
    ShapeStructureStyle.SHARP -> RoundedCornerShape(4.dp)
    ShapeStructureStyle.CHAMFER_CUT -> CutCornerShape(14.dp)
    ShapeStructureStyle.TEARDROP -> RoundedCornerShape(topStart = 26.dp, topEnd = 6.dp, bottomEnd = 26.dp, bottomStart = 6.dp)
    ShapeStructureStyle.OVAL_STADIUM -> RoundedCornerShape(32.dp)
    ShapeStructureStyle.SOFT_BUBBLE -> RoundedCornerShape(26.dp)
}

fun ShapeStructureStyle.toInputShape(): CornerBasedShape = when (this) {
    ShapeStructureStyle.ROUNDED -> RoundedCornerShape(12.dp)
    ShapeStructureStyle.PILL -> RoundedCornerShape(24.dp)
    ShapeStructureStyle.LINE_BORDER -> RoundedCornerShape(6.dp)
    ShapeStructureStyle.SHARP -> RoundedCornerShape(4.dp)
    ShapeStructureStyle.CHAMFER_CUT -> CutCornerShape(10.dp)
    ShapeStructureStyle.TEARDROP -> RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomEnd = 16.dp, bottomStart = 4.dp)
    ShapeStructureStyle.OVAL_STADIUM -> RoundedCornerShape(22.dp)
    ShapeStructureStyle.SOFT_BUBBLE -> RoundedCornerShape(18.dp)
}

fun ShapeStructureStyle.toCardShape(): CornerBasedShape = when (this) {
    ShapeStructureStyle.ROUNDED -> RoundedCornerShape(18.dp)
    ShapeStructureStyle.PILL -> RoundedCornerShape(26.dp)
    ShapeStructureStyle.LINE_BORDER -> RoundedCornerShape(8.dp)
    ShapeStructureStyle.SHARP -> RoundedCornerShape(4.dp)
    ShapeStructureStyle.CHAMFER_CUT -> CutCornerShape(16.dp)
    ShapeStructureStyle.TEARDROP -> RoundedCornerShape(topStart = 24.dp, topEnd = 8.dp, bottomEnd = 24.dp, bottomStart = 8.dp)
    ShapeStructureStyle.OVAL_STADIUM -> RoundedCornerShape(28.dp)
    ShapeStructureStyle.SOFT_BUBBLE -> RoundedCornerShape(24.dp)
}

fun ShapeStructureStyle.toSmallShape(): CornerBasedShape = when (this) {
    ShapeStructureStyle.ROUNDED -> RoundedCornerShape(8.dp)
    ShapeStructureStyle.PILL -> RoundedCornerShape(50)
    ShapeStructureStyle.LINE_BORDER -> RoundedCornerShape(4.dp)
    ShapeStructureStyle.SHARP -> RoundedCornerShape(2.dp)
    ShapeStructureStyle.CHAMFER_CUT -> CutCornerShape(6.dp)
    ShapeStructureStyle.TEARDROP -> RoundedCornerShape(topStart = 12.dp, topEnd = 3.dp, bottomEnd = 12.dp, bottomStart = 3.dp)
    ShapeStructureStyle.OVAL_STADIUM -> RoundedCornerShape(16.dp)
    ShapeStructureStyle.SOFT_BUBBLE -> RoundedCornerShape(12.dp)
}

fun ShapeStructureStyle.toChipShape(): CornerBasedShape = when (this) {
    ShapeStructureStyle.ROUNDED -> RoundedCornerShape(10.dp)
    ShapeStructureStyle.PILL -> RoundedCornerShape(50)
    ShapeStructureStyle.LINE_BORDER -> RoundedCornerShape(6.dp)
    ShapeStructureStyle.SHARP -> RoundedCornerShape(3.dp)
    ShapeStructureStyle.CHAMFER_CUT -> CutCornerShape(8.dp)
    ShapeStructureStyle.TEARDROP -> RoundedCornerShape(topStart = 14.dp, topEnd = 4.dp, bottomEnd = 14.dp, bottomStart = 4.dp)
    ShapeStructureStyle.OVAL_STADIUM -> RoundedCornerShape(20.dp)
    ShapeStructureStyle.SOFT_BUBBLE -> RoundedCornerShape(16.dp)
}

enum class ComponentSizeStyle(
    val displayName: String,
    val description: String,
    val keypadSpacing: Dp,
    val keypadPaddingH: Dp,
    val keypadPaddingV: Dp,
    val keyFontScale: Float,
    val cardPadding: Dp,
    val inputHeight: Dp,
    val iconSize: Dp
) {
    COMPACT(
        "Compact (Dense)",
        "Dense layout with 8dp spacing, ideal for single-hand speed",
        keypadSpacing = 8.dp,
        keypadPaddingH = 12.dp,
        keypadPaddingV = 14.dp,
        keyFontScale = 0.88f,
        cardPadding = 10.dp,
        inputHeight = 48.dp,
        iconSize = 20.dp
    ),
    STANDARD(
        "Standard (Balanced)",
        "Default balanced Material 3 spacing and touch target dimensions",
        keypadSpacing = 12.dp,
        keypadPaddingH = 18.dp,
        keypadPaddingV = 20.dp,
        keyFontScale = 1.0f,
        cardPadding = 14.dp,
        inputHeight = 56.dp,
        iconSize = 24.dp
    ),
    LARGE(
        "Large (Comfort)",
        "Spacious 15dp spacing with enlarged buttons and easy reach",
        keypadSpacing = 15.dp,
        keypadPaddingH = 20.dp,
        keypadPaddingV = 22.dp,
        keyFontScale = 1.15f,
        cardPadding = 18.dp,
        inputHeight = 62.dp,
        iconSize = 26.dp
    ),
    EXTRA_LARGE(
        "Jumbo (Accessible)",
        "Maximized 18dp spacing and high-comfort large text touch targets",
        keypadSpacing = 18.dp,
        keypadPaddingH = 22.dp,
        keypadPaddingV = 26.dp,
        keyFontScale = 1.30f,
        cardPadding = 22.dp,
        inputHeight = 68.dp,
        iconSize = 30.dp
    )
}

enum class AnimationIntensity(
    val displayName: String,
    val description: String,
    val scaleFactor: Float,
    val translationFactor: Float
) {
    OFF("Disabled (Instant)", "No bounce or compression animations", 1.0f, 0f),
    SUBTLE("Subtle (0.6x)", "Gentle tactile micro-compression", 0.94f, 2.0f),
    BALANCED("Balanced (1.0x)", "Natural fluid spring rebound physics", 0.88f, 3.5f),
    HIGH_SPRING("High Spring (1.6x)", "Snappy bouncy compression and deep recoil", 0.80f, 5.0f),
    EXTREME_BOUNCE("Extreme Bounce (2.4x)", "Deep dynamic elastic bounce & kinetic energy", 0.72f, 7.5f)
}

enum class HapticIntensity(
    val displayName: String,
    val description: String
) {
    OFF("Disabled", "No haptic vibration"),
    LIGHT("Light", "Subtle haptic tick sensation"),
    MEDIUM("Medium", "Balanced standard tactile click"),
    STRONG("Strong", "Heavy tactile vibration pulse"),
    EXTREME("Extreme", "Maximum dual-pulse hardware vibration")
}

enum class SoundIntensity(
    val displayName: String,
    val description: String
) {
    OFF("Muted", "Silent mode without audio clicks"),
    SOFT("Soft (Discreet)", "Quiet acoustic tick sound"),
    STANDARD("Standard (Crisp)", "Crisp digital audio click"),
    LOUD("Loud (Resonant)", "High-resonance tactile chime"),
    EXTREME_BOOST("Extreme Boost (Maximum)", "100% volume dual acoustic burst - clears freely & loudly")
}

val LocalShapeStructure = staticCompositionLocalOf { ShapeStructureStyle.ROUNDED }
val LocalComponentSizing = staticCompositionLocalOf { ComponentSizeStyle.STANDARD }
val LocalAnimationIntensity = staticCompositionLocalOf { AnimationIntensity.BALANCED }

@Composable
fun MyApplicationTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    accentTheme: AccentTheme = AccentTheme.AQUA,
    shapeStructureStyle: ShapeStructureStyle = ShapeStructureStyle.ROUNDED,
    componentSizeStyle: ComponentSizeStyle = ComponentSizeStyle.STANDARD,
    animationIntensity: AnimationIntensity = AnimationIntensity.BALANCED,
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

    CompositionLocalProvider(
        LocalShapeStructure provides shapeStructureStyle,
        LocalComponentSizing provides componentSizeStyle,
        LocalAnimationIntensity provides animationIntensity
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = m3Shapes,
            content = content
        )
    }
}

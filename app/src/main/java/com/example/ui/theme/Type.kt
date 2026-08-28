package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

enum class AppFontStyle(
    val displayName: String,
    val category: String,
    val description: String,
    val previewSample: String,
    val fontFamily: FontFamily,
    val numberFontFamily: FontFamily,
    val defaultFontWeight: FontWeight = FontWeight.Normal,
    val defaultFontStyle: FontStyle = FontStyle.Normal,
    val fontFeatureSettings: String? = null
) {
    // ==========================================
    // MODERN CALLIGRAPHY & SCRIPT STYLES
    // ==========================================
    MODERN_CALLIGRAPHY_SIGNATURE(
        displayName = "Modern Calligraphy Signature",
        category = "Modern Calligraphy",
        description = "Graceful flowing signature calligraphy with contemporary rhythm",
        previewSample = "123,456 • Modern Calligraphy 123 + 456 = 579",
        fontFamily = FontFamily.Cursive,
        numberFontFamily = FontFamily.Cursive,
        defaultFontWeight = FontWeight.Medium,
        defaultFontStyle = FontStyle.Normal
    ),
    ROMANTIC_FLOURISH_CALLIGRAPHY(
        displayName = "Romantic Flourish Calligraphy",
        category = "Flourish Script",
        description = "Swirling delicate loops, decorative tails and romantic calligraphic curves",
        previewSample = "123,456 • Romantic Flourish 789 × 12 = 9,468",
        fontFamily = FontFamily.Cursive,
        numberFontFamily = FontFamily.Cursive,
        defaultFontWeight = FontWeight.SemiBold,
        defaultFontStyle = FontStyle.Italic
    ),
    CHIC_BRUSH_CALLIGRAPHY(
        displayName = "Chic Brush Lettering",
        category = "Brush Script",
        description = "Rich artistic brush pen calligraphy with expressive weight and stroke variation",
        previewSample = "123,456 • Chic Brush Art 3.14159 × 42",
        fontFamily = FontFamily.Cursive,
        numberFontFamily = FontFamily.Cursive,
        defaultFontWeight = FontWeight.Bold,
        defaultFontStyle = FontStyle.Normal
    ),
    PARISIAN_HAUTE_CALLIGRAPHY(
        displayName = "Parisian Haute Calligraphy",
        category = "Haute Couture",
        description = "Sophisticated French atelier fashion script calligraphy with slanted grace",
        previewSample = "123,456 • Atelier Paris 888 + 999",
        fontFamily = FontFamily.Cursive,
        numberFontFamily = FontFamily.Cursive,
        defaultFontWeight = FontWeight.Bold,
        defaultFontStyle = FontStyle.Italic
    ),
    MINIMALIST_MONOLINE_SCRIPT(
        displayName = "Minimalist Monoline Calligraphy",
        category = "Monoline Script",
        description = "Clean uniform-stroke modern minimalist cursive calligraphy",
        previewSample = "123,456 • Monoline Calligraphy 24/7",
        fontFamily = FontFamily.Cursive,
        numberFontFamily = FontFamily.Cursive,
        defaultFontWeight = FontWeight.Medium,
        defaultFontStyle = FontStyle.Normal
    ),
    ROYAL_MAJESTIC_CALLIGRAPHY(
        displayName = "Royal Majestic Calligraphy",
        category = "Royal Calligraphy",
        description = "Dramatic luxury cursive calligraphy with bold, commanding strokes",
        previewSample = "123,456 • Royal Luxury 1,000,000",
        fontFamily = FontFamily.Cursive,
        numberFontFamily = FontFamily.Cursive,
        defaultFontWeight = FontWeight.ExtraBold,
        defaultFontStyle = FontStyle.Normal
    ),
    DANCING_BOUNCY_SCRIPT(
        displayName = "Dancing Bouncy Script",
        category = "Bouncy Script",
        description = "Playful rhythmic baseline bounce and flowing cursive letterforms",
        previewSample = "123,456 • Dancing Script 123 + 456",
        fontFamily = FontFamily.Cursive,
        numberFontFamily = FontFamily.Cursive,
        defaultFontWeight = FontWeight.SemiBold,
        defaultFontStyle = FontStyle.Italic
    ),
    CASUAL_HANDWRITING(
        displayName = "Casual Human Handwriting",
        category = "Handwritten",
        description = "Friendly, informal human handwriting aesthetic",
        previewSample = "123,456 • Everyday Notes 123 + 456",
        fontFamily = FontFamily.Cursive,
        numberFontFamily = FontFamily.Cursive,
        defaultFontWeight = FontWeight.Normal,
        defaultFontStyle = FontStyle.Normal
    ),
    BOLD_MARKER_SCRIPT(
        displayName = "Bold Marker Sketch",
        category = "Whiteboard Marker",
        description = "Chunky, expressive whiteboard marker and street art lettering",
        previewSample = "123,456 • Marker Sketch 550 + 250",
        fontFamily = FontFamily.Cursive,
        numberFontFamily = FontFamily.Cursive,
        defaultFontWeight = FontWeight.Bold,
        defaultFontStyle = FontStyle.Normal
    ),

    // ==========================================
    // DIGITAL & ELECTRONIC DISPLAY STYLES
    // ==========================================
    CLEAN_MONOSPACE(
        displayName = "Digital Monospace",
        category = "Digital LED",
        description = "Standard digital fixed-width calculator typography",
        previewSample = "123,456 • 123 + 456 = 579",
        fontFamily = FontFamily.Monospace,
        numberFontFamily = FontFamily.Monospace,
        defaultFontWeight = FontWeight.Normal,
        defaultFontStyle = FontStyle.Normal
    ),
    LCD_DIGITAL_BOLD(
        displayName = "LCD 7-Segment Bold",
        category = "7-Segment",
        description = "High-contrast segmented electronic calculator display",
        previewSample = "123,456 • 123 456 789",
        fontFamily = FontFamily.Monospace,
        numberFontFamily = FontFamily.Monospace,
        defaultFontWeight = FontWeight.ExtraBold,
        defaultFontStyle = FontStyle.Normal,
        fontFeatureSettings = "tnum, zero"
    ),
    CYBER_CONDENSED(
        displayName = "Cyberpunk HUD Matrix",
        category = "Sci-Fi HUD",
        description = "Ultra-tall condensed digital geometry for sci-fi displays",
        previewSample = "123,456 • 123 MATRIX HUD",
        fontFamily = FontFamily.Monospace,
        numberFontFamily = FontFamily.Monospace,
        defaultFontWeight = FontWeight.Bold,
        defaultFontStyle = FontStyle.Normal,
        fontFeatureSettings = "tnum"
    ),
    TERMINAL_MONOSPACE(
        displayName = "Retro Terminal Monospace",
        category = "Hacker Terminal",
        description = "Bold developer terminal monospace commands and digits",
        previewSample = "123,456 • 123 ROOT EXEC",
        fontFamily = FontFamily.Monospace,
        numberFontFamily = FontFamily.Monospace,
        defaultFontWeight = FontWeight.Bold,
        defaultFontStyle = FontStyle.Normal
    ),
    CONDENSED_DIGITAL(
        displayName = "Compact Digital Ledger",
        category = "Matrix Display",
        description = "Space-saving high-density numeric matrix typography",
        previewSample = "123,456 • 123 DATA LOG",
        fontFamily = FontFamily.Monospace,
        numberFontFamily = FontFamily.Monospace,
        defaultFontWeight = FontWeight.Medium,
        defaultFontStyle = FontStyle.Normal,
        fontFeatureSettings = "tnum"
    ),
    TYPEWRITER_DIGITAL(
        displayName = "Vintage Slab Monospace",
        category = "Mechanical",
        description = "Mechanical slab-serif monospace matrix numbers and text",
        previewSample = "123,456 • 123 SLAB TECH",
        fontFamily = FontFamily.Monospace,
        numberFontFamily = FontFamily.Monospace,
        defaultFontWeight = FontWeight.SemiBold,
        defaultFontStyle = FontStyle.Normal
    ),
    INDUSTRIAL_DIGITAL(
        displayName = "Industrial Heavy LCD",
        category = "Heavy Matrix",
        description = "Massive punchy digital display for industrial hardware",
        previewSample = "123,456 • 123 POWER ON",
        fontFamily = FontFamily.Monospace,
        numberFontFamily = FontFamily.Monospace,
        defaultFontWeight = FontWeight.Black,
        defaultFontStyle = FontStyle.Normal,
        fontFeatureSettings = "zero"
    ),

    // ==========================================
    // TEXT & EDITORIAL TYPOGRAPHY STYLES
    // ==========================================
    SYSTEM(
        displayName = "System Clean Sans",
        category = "Modern Sans",
        description = "Clean standard Android device system typography applied uniformly",
        previewSample = "123,456 • Precision Clean 123",
        fontFamily = FontFamily.Default,
        numberFontFamily = FontFamily.Default,
        defaultFontWeight = FontWeight.Normal,
        defaultFontStyle = FontStyle.Normal
    ),
    CLASSIC_SERIF(
        displayName = "Classic Editorial Serif",
        category = "Editorial Serif",
        description = "Distinguished literary serif typography with refined serifs",
        previewSample = "123,456 • Editorial Books 123",
        fontFamily = FontFamily.Serif,
        numberFontFamily = FontFamily.Serif,
        defaultFontWeight = FontWeight.Normal,
        defaultFontStyle = FontStyle.Normal
    ),
    LUXURY_SERIF_ITALIC(
        displayName = "Luxury Serif Italic",
        category = "High Fashion",
        description = "Dramatic, high-contrast italic serif for upscale aesthetics",
        previewSample = "123,456 • Luxury Fashion 123",
        fontFamily = FontFamily.Serif,
        numberFontFamily = FontFamily.Serif,
        defaultFontWeight = FontWeight.Bold,
        defaultFontStyle = FontStyle.Italic
    ),
    VINTAGE_TYPEWRITER(
        displayName = "Vintage Typewriter",
        category = "Vintage",
        description = "Distressed mechanical typewriter slab-serif letterforms and digits",
        previewSample = "123,456 • Typewriter 1974",
        fontFamily = FontFamily.Monospace,
        numberFontFamily = FontFamily.Monospace,
        defaultFontWeight = FontWeight.Medium,
        defaultFontStyle = FontStyle.Normal
    ),
    MINIMALIST_LIGHT(
        displayName = "Minimalist Hairline",
        category = "Minimalist",
        description = "Ultra-thin, airy, Scandinavian architectural aesthetic",
        previewSample = "123,456 • Hairline Nordic 123",
        fontFamily = FontFamily.SansSerif,
        numberFontFamily = FontFamily.SansSerif,
        defaultFontWeight = FontWeight.Light,
        defaultFontStyle = FontStyle.Normal
    ),
    INDUSTRIAL_BLACK(
        displayName = "Industrial Ultra-Black",
        category = "Display Black",
        description = "Massive punchy heavyweight typeface with bold presence",
        previewSample = "123,456 • Heavy Black 123",
        fontFamily = FontFamily.SansSerif,
        numberFontFamily = FontFamily.SansSerif,
        defaultFontWeight = FontWeight.Black,
        defaultFontStyle = FontStyle.Normal
    ),
    SMALL_CAPS_LEDGER(
        displayName = "Small Caps Serif",
        category = "Accounting",
        description = "Distinguished financial accounting with small capital letters",
        previewSample = "123,456 • Ledger Account 123",
        fontFamily = FontFamily.Serif,
        numberFontFamily = FontFamily.Serif,
        defaultFontWeight = FontWeight.Normal,
        defaultFontStyle = FontStyle.Normal,
        fontFeatureSettings = "smcp"
    ),
    GEOMETRIC_TECHNO(
        displayName = "Geometric Modern Sans",
        category = "Geometric Sans",
        description = "Well-balanced geometric sans with modern contrast and symmetry",
        previewSample = "123,456 • Neo Geometric 123",
        fontFamily = FontFamily.SansSerif,
        numberFontFamily = FontFamily.SansSerif,
        defaultFontWeight = FontWeight.Medium,
        defaultFontStyle = FontStyle.Normal
    )
}

val LocalAppFontStyle = staticCompositionLocalOf { AppFontStyle.SYSTEM }

// Dynamic reactive font family for numbers and display that follows user font selection
val DigitalMonospaceFont: FontFamily
    @Composable
    @ReadOnlyComposable
    get() = LocalAppFontStyle.current.numberFontFamily

fun createAppTypography(fontStyle: AppFontStyle = AppFontStyle.SYSTEM): Typography {
    val uniformFont = fontStyle.fontFamily
    val fStyle = fontStyle.defaultFontStyle
    val fSettings = fontStyle.fontFeatureSettings

    return Typography(
        displayLarge = TextStyle(
            fontFamily = uniformFont,
            fontWeight = fontStyle.defaultFontWeight,
            fontStyle = fStyle,
            fontFeatureSettings = fSettings,
            fontSize = 54.sp,
            lineHeight = 60.sp,
            letterSpacing = 1.sp
        ),
        displayMedium = TextStyle(
            fontFamily = uniformFont,
            fontWeight = fontStyle.defaultFontWeight,
            fontStyle = fStyle,
            fontFeatureSettings = fSettings,
            fontSize = 42.sp,
            lineHeight = 48.sp,
            letterSpacing = 0.5.sp
        ),
        displaySmall = TextStyle(
            fontFamily = uniformFont,
            fontWeight = fontStyle.defaultFontWeight,
            fontStyle = fStyle,
            fontFeatureSettings = fSettings,
            fontSize = 28.sp,
            lineHeight = 34.sp,
            letterSpacing = 0.5.sp
        ),
        headlineLarge = TextStyle(
            fontFamily = uniformFont,
            fontWeight = FontWeight.Bold,
            fontStyle = fStyle,
            fontFeatureSettings = fSettings,
            fontSize = 24.sp,
            lineHeight = 32.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = uniformFont,
            fontWeight = FontWeight.SemiBold,
            fontStyle = fStyle,
            fontFeatureSettings = fSettings,
            fontSize = 20.sp,
            lineHeight = 28.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = uniformFont,
            fontWeight = FontWeight.SemiBold,
            fontStyle = fStyle,
            fontFeatureSettings = fSettings,
            fontSize = 18.sp,
            lineHeight = 26.sp
        ),
        titleLarge = TextStyle(
            fontFamily = uniformFont,
            fontWeight = FontWeight.SemiBold,
            fontStyle = fStyle,
            fontFeatureSettings = fSettings,
            fontSize = 18.sp,
            lineHeight = 24.sp
        ),
        titleMedium = TextStyle(
            fontFamily = uniformFont,
            fontWeight = FontWeight.Medium,
            fontStyle = fStyle,
            fontFeatureSettings = fSettings,
            fontSize = 16.sp,
            lineHeight = 22.sp
        ),
        titleSmall = TextStyle(
            fontFamily = uniformFont,
            fontWeight = FontWeight.Medium,
            fontStyle = fStyle,
            fontFeatureSettings = fSettings,
            fontSize = 14.sp,
            lineHeight = 20.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = uniformFont,
            fontWeight = fontStyle.defaultFontWeight,
            fontStyle = fStyle,
            fontFeatureSettings = fSettings,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.25.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = uniformFont,
            fontWeight = fontStyle.defaultFontWeight,
            fontStyle = fStyle,
            fontFeatureSettings = fSettings,
            fontSize = 14.sp,
            lineHeight = 20.sp
        ),
        bodySmall = TextStyle(
            fontFamily = uniformFont,
            fontWeight = fontStyle.defaultFontWeight,
            fontStyle = fStyle,
            fontFeatureSettings = fSettings,
            fontSize = 12.sp,
            lineHeight = 16.sp
        ),
        labelLarge = TextStyle(
            fontFamily = uniformFont,
            fontWeight = FontWeight.SemiBold,
            fontStyle = fStyle,
            fontFeatureSettings = fSettings,
            fontSize = 15.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.5.sp
        ),
        labelMedium = TextStyle(
            fontFamily = uniformFont,
            fontWeight = FontWeight.Medium,
            fontStyle = fStyle,
            fontFeatureSettings = fSettings,
            fontSize = 12.sp,
            lineHeight = 16.sp
        ),
        labelSmall = TextStyle(
            fontFamily = uniformFont,
            fontWeight = FontWeight.Medium,
            fontStyle = fStyle,
            fontFeatureSettings = fSettings,
            fontSize = 10.sp,
            lineHeight = 14.sp
        )
    )
}

val Typography = createAppTypography(AppFontStyle.SYSTEM)

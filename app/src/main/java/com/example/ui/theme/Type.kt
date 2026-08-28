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

enum class FontType(val displayName: String) {
    ALL("All Fonts"),
    DIGITAL("Digital & LED"),
    TEXT("Text & Typography")
}

enum class AppFontStyle(
    val displayName: String,
    val fontType: FontType,
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
    // DIGITAL & ELECTRONIC DISPLAY STYLES
    // ==========================================
    CLEAN_MONOSPACE(
        displayName = "Digital Monospace",
        fontType = FontType.DIGITAL,
        category = "Digital LED",
        description = "Standard digital fixed-width calculator typography",
        previewSample = "123,456 • 123 + 456 = 579",
        fontFamily = FontFamily.Monospace,
        numberFontFamily = FontFamily.Monospace,
        defaultFontWeight = FontWeight.Normal
    ),
    LCD_DIGITAL_BOLD(
        displayName = "LCD 7-Segment Bold",
        fontType = FontType.DIGITAL,
        category = "7-Segment",
        description = "High-contrast segmented electronic calculator display",
        previewSample = "123,456 • 123 456 789",
        fontFamily = FontFamily.Monospace,
        numberFontFamily = FontFamily.Monospace,
        defaultFontWeight = FontWeight.ExtraBold,
        fontFeatureSettings = "tnum, zero"
    ),
    CYBER_CONDENSED(
        displayName = "Cyberpunk HUD Matrix",
        fontType = FontType.DIGITAL,
        category = "Sci-Fi HUD",
        description = "Ultra-tall condensed digital geometry for sci-fi displays",
        previewSample = "123,456 • 123 MATRIX",
        fontFamily = FontFamily.Monospace,
        numberFontFamily = FontFamily.Monospace,
        defaultFontWeight = FontWeight.Bold,
        fontFeatureSettings = "tnum"
    ),
    TERMINAL_MONOSPACE(
        displayName = "Retro Terminal Code",
        fontType = FontType.DIGITAL,
        category = "Hacker Terminal",
        description = "Bold developer terminal monospace commands",
        previewSample = "123,456 • 123 ROOT EXEC",
        fontFamily = FontFamily.Monospace,
        numberFontFamily = FontFamily.Monospace,
        defaultFontWeight = FontWeight.Bold
    ),
    CONDENSED_DIGITAL(
        displayName = "Compact Digital Ledger",
        fontType = FontType.DIGITAL,
        category = "Matrix Display",
        description = "Space-saving high-density numeric matrix typography",
        previewSample = "123,456 • 123 DATA LOG",
        fontFamily = FontFamily.Monospace,
        numberFontFamily = FontFamily.Monospace,
        defaultFontWeight = FontWeight.Medium,
        fontFeatureSettings = "tnum"
    ),
    TYPEWRITER_DIGITAL(
        displayName = "Vintage Slab Matrix",
        fontType = FontType.DIGITAL,
        category = "Mechanical",
        description = "Mechanical slab-serif monospace matrix numbers",
        previewSample = "123,456 • 123 SLAB TECH",
        fontFamily = FontFamily.Monospace,
        numberFontFamily = FontFamily.Monospace,
        defaultFontWeight = FontWeight.SemiBold
    ),
    INDUSTRIAL_DIGITAL(
        displayName = "Industrial Heavy LCD",
        fontType = FontType.DIGITAL,
        category = "Heavy Matrix",
        description = "Massive punchy digital display for industrial hardware",
        previewSample = "123,456 • 123 POWER ON",
        fontFamily = FontFamily.Monospace,
        numberFontFamily = FontFamily.Monospace,
        defaultFontWeight = FontWeight.Black,
        fontFeatureSettings = "zero"
    ),

    // ==========================================
    // TEXT & EDITORIAL TYPOGRAPHY STYLES
    // ==========================================
    SYSTEM(
        displayName = "System Clean Sans",
        fontType = FontType.TEXT,
        category = "Modern Sans",
        description = "Clean standard Android device system typography",
        previewSample = "123,456 • Precision 123",
        fontFamily = FontFamily.Default,
        numberFontFamily = FontFamily.Monospace,
        defaultFontWeight = FontWeight.Normal
    ),
    CASUAL_HANDWRITING(
        displayName = "Casual Human Script",
        fontType = FontType.TEXT,
        category = "Handwritten",
        description = "Friendly, informal human handwriting aesthetic",
        previewSample = "123,456 • Precision 123",
        fontFamily = FontFamily.Cursive,
        numberFontFamily = FontFamily.Cursive,
        defaultFontWeight = FontWeight.Normal
    ),
    BOLD_CASUAL(
        displayName = "Bold Marker Sketch",
        fontType = FontType.TEXT,
        category = "Street Art",
        description = "Chunky, expressive whiteboard marker typography",
        previewSample = "123,456 • Precision 123",
        fontFamily = FontFamily.Cursive,
        numberFontFamily = FontFamily.Cursive,
        defaultFontWeight = FontWeight.Bold
    ),
    DANCING_CURSIVE(
        displayName = "Dancing Cursive Script",
        fontType = FontType.TEXT,
        category = "Calligraphic",
        description = "Flowing, elegant calligraphic script handwriting",
        previewSample = "123,456 • Precision 123",
        fontFamily = FontFamily.Cursive,
        numberFontFamily = FontFamily.Cursive,
        defaultFontWeight = FontWeight.Medium
    ),
    ROYAL_CALLIGRAPHY(
        displayName = "Royal Script Bold",
        fontType = FontType.TEXT,
        category = "Luxury Script",
        description = "Dramatic luxury cursive calligraphy with bold strokes",
        previewSample = "123,456 • Precision 123",
        fontFamily = FontFamily.Cursive,
        numberFontFamily = FontFamily.Cursive,
        defaultFontWeight = FontWeight.ExtraBold
    ),
    CLASSIC_SERIF(
        displayName = "Classic Editorial Serif",
        fontType = FontType.TEXT,
        category = "Editorial",
        description = "Distinguished literary serif typography with refined serifs",
        previewSample = "123,456 • Precision 123",
        fontFamily = FontFamily.Serif,
        numberFontFamily = FontFamily.Serif,
        defaultFontWeight = FontWeight.Normal
    ),
    LUXURY_SERIF_ITALIC(
        displayName = "Luxury Serif Italic",
        fontType = FontType.TEXT,
        category = "High Fashion",
        description = "Dramatic, high-contrast italic serif for upscale aesthetics",
        previewSample = "123,456 • Precision 123",
        fontFamily = FontFamily.Serif,
        numberFontFamily = FontFamily.Serif,
        defaultFontWeight = FontWeight.Bold,
        defaultFontStyle = FontStyle.Italic
    ),
    VINTAGE_TYPEWRITER(
        displayName = "Vintage Typewriter",
        fontType = FontType.TEXT,
        category = "Vintage",
        description = "Distressed mechanical typewriter slab-serif letterforms",
        previewSample = "123,456 • Precision 123",
        fontFamily = FontFamily.Monospace,
        numberFontFamily = FontFamily.Monospace,
        defaultFontWeight = FontWeight.Medium
    ),
    MINIMALIST_LIGHT(
        displayName = "Minimalist Hairline",
        fontType = FontType.TEXT,
        category = "Minimalist",
        description = "Ultra-thin, airy, Scandinavian architectural aesthetic",
        previewSample = "123,456 • Precision 123",
        fontFamily = FontFamily.SansSerif,
        numberFontFamily = FontFamily.SansSerif,
        defaultFontWeight = FontWeight.Light
    ),
    INDUSTRIAL_BLACK(
        displayName = "Industrial Ultra-Black",
        fontType = FontType.TEXT,
        category = "Display Bold",
        description = "Massive punchy heavyweight typeface with bold presence",
        previewSample = "123,456 • Precision 123",
        fontFamily = FontFamily.SansSerif,
        numberFontFamily = FontFamily.SansSerif,
        defaultFontWeight = FontWeight.Black
    ),
    SMALL_CAPS_LEDGER(
        displayName = "Small Caps Ledger",
        fontType = FontType.TEXT,
        category = "Accounting",
        description = "Distinguished financial accounting in small capital letters",
        previewSample = "123,456 • Precision 123",
        fontFamily = FontFamily.Serif,
        numberFontFamily = FontFamily.Monospace,
        defaultFontWeight = FontWeight.Normal,
        fontFeatureSettings = "smcp, c2sc"
    ),
    GEOMETRIC_TECHNO(
        displayName = "Geometric Modern Sans",
        fontType = FontType.TEXT,
        category = "Neo-Modern",
        description = "Well-balanced geometric sans with modern contrast",
        previewSample = "123,456 • Precision 123",
        fontFamily = FontFamily.SansSerif,
        numberFontFamily = FontFamily.Monospace,
        defaultFontWeight = FontWeight.Medium
    )
}

val LocalAppFontStyle = staticCompositionLocalOf { AppFontStyle.SYSTEM }

// Dynamic reactive font family for numbers and display that follows user font selection
val DigitalMonospaceFont: FontFamily
    @Composable
    @ReadOnlyComposable
    get() = LocalAppFontStyle.current.numberFontFamily

fun createAppTypography(fontStyle: AppFontStyle = AppFontStyle.SYSTEM): Typography {
    val bodyFont = fontStyle.fontFamily
    val numFont = fontStyle.numberFontFamily
    val fStyle = fontStyle.defaultFontStyle
    val fSettings = fontStyle.fontFeatureSettings

    return Typography(
        displayLarge = TextStyle(
            fontFamily = numFont,
            fontWeight = fontStyle.defaultFontWeight,
            fontStyle = fStyle,
            fontFeatureSettings = fSettings,
            fontSize = 54.sp,
            lineHeight = 60.sp,
            letterSpacing = 1.sp
        ),
        displayMedium = TextStyle(
            fontFamily = numFont,
            fontWeight = fontStyle.defaultFontWeight,
            fontStyle = fStyle,
            fontFeatureSettings = fSettings,
            fontSize = 42.sp,
            lineHeight = 48.sp,
            letterSpacing = 0.5.sp
        ),
        displaySmall = TextStyle(
            fontFamily = numFont,
            fontWeight = fontStyle.defaultFontWeight,
            fontStyle = fStyle,
            fontFeatureSettings = fSettings,
            fontSize = 28.sp,
            lineHeight = 34.sp,
            letterSpacing = 0.5.sp
        ),
        headlineLarge = TextStyle(
            fontFamily = bodyFont,
            fontWeight = FontWeight.Bold,
            fontStyle = fStyle,
            fontFeatureSettings = fSettings,
            fontSize = 24.sp,
            lineHeight = 32.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = bodyFont,
            fontWeight = FontWeight.SemiBold,
            fontStyle = fStyle,
            fontFeatureSettings = fSettings,
            fontSize = 20.sp,
            lineHeight = 28.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = bodyFont,
            fontWeight = FontWeight.SemiBold,
            fontStyle = fStyle,
            fontFeatureSettings = fSettings,
            fontSize = 18.sp,
            lineHeight = 26.sp
        ),
        titleLarge = TextStyle(
            fontFamily = bodyFont,
            fontWeight = FontWeight.SemiBold,
            fontStyle = fStyle,
            fontFeatureSettings = fSettings,
            fontSize = 18.sp,
            lineHeight = 24.sp
        ),
        titleMedium = TextStyle(
            fontFamily = bodyFont,
            fontWeight = FontWeight.Medium,
            fontStyle = fStyle,
            fontFeatureSettings = fSettings,
            fontSize = 16.sp,
            lineHeight = 22.sp
        ),
        titleSmall = TextStyle(
            fontFamily = bodyFont,
            fontWeight = FontWeight.Medium,
            fontStyle = fStyle,
            fontFeatureSettings = fSettings,
            fontSize = 14.sp,
            lineHeight = 20.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = bodyFont,
            fontWeight = fontStyle.defaultFontWeight,
            fontStyle = fStyle,
            fontFeatureSettings = fSettings,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.25.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = bodyFont,
            fontWeight = fontStyle.defaultFontWeight,
            fontStyle = fStyle,
            fontFeatureSettings = fSettings,
            fontSize = 14.sp,
            lineHeight = 20.sp
        ),
        bodySmall = TextStyle(
            fontFamily = bodyFont,
            fontWeight = fontStyle.defaultFontWeight,
            fontStyle = fStyle,
            fontFeatureSettings = fSettings,
            fontSize = 12.sp,
            lineHeight = 16.sp
        ),
        labelLarge = TextStyle(
            fontFamily = numFont,
            fontWeight = FontWeight.SemiBold,
            fontStyle = fStyle,
            fontFeatureSettings = fSettings,
            fontSize = 15.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.5.sp
        ),
        labelMedium = TextStyle(
            fontFamily = bodyFont,
            fontWeight = FontWeight.Medium,
            fontStyle = fStyle,
            fontFeatureSettings = fSettings,
            fontSize = 12.sp,
            lineHeight = 16.sp
        ),
        labelSmall = TextStyle(
            fontFamily = bodyFont,
            fontWeight = FontWeight.Medium,
            fontStyle = fStyle,
            fontFeatureSettings = fSettings,
            fontSize = 10.sp,
            lineHeight = 14.sp
        )
    )
}

val Typography = createAppTypography(AppFontStyle.SYSTEM)


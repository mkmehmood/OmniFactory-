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
    // MODERN CALLIGRAPHY & SCRIPT FONTS
    // ==========================================
    MODERN_CALLIGRAPHY(
        displayName = "Modern Flow Calligraphy",
        category = "Modern Calligraphy",
        description = "Contemporary chic calligraphy with flowing organic flourishes",
        previewSample = "123,456 • Modern Calligraphy 789",
        fontFamily = FontFamily.Cursive,
        numberFontFamily = FontFamily.Cursive,
        defaultFontWeight = FontWeight.Medium
    ),
    LUXURY_RIBBON_SCRIPT(
        displayName = "Luxury Ribbon Script",
        category = "Ribbon Flourish",
        description = "High-contrast elegant ribbon script with dramatic graceful loops",
        previewSample = "123,456 • Ribbon Script 789",
        fontFamily = FontFamily.Cursive,
        numberFontFamily = FontFamily.Cursive,
        defaultFontWeight = FontWeight.Bold
    ),
    PARISIAN_SIGNATURE(
        displayName = "Parisian Chic Signature",
        category = "Signature Script",
        description = "Modern boutique signature hand-lettered calligraphy with chic slant",
        previewSample = "123,456 • Parisian Calligraphy 789",
        fontFamily = FontFamily.Cursive,
        numberFontFamily = FontFamily.Cursive,
        defaultFontWeight = FontWeight.SemiBold,
        defaultFontStyle = FontStyle.Italic
    ),
    NASTALIQ_CALLIGRAPHY(
        displayName = "Harmonic Nastaliq Script",
        category = "Eastern Calligraphy",
        description = "Harmonic rhythmic cursive strokes inspired by Persian & Nastaliq calligraphy",
        previewSample = "123,456 • Nastaliq Calligraphy 789",
        fontFamily = FontFamily.Cursive,
        numberFontFamily = FontFamily.Cursive,
        defaultFontWeight = FontWeight.Bold
    ),
    GOTHIC_CALLIGRAPHY(
        displayName = "Gothic Blackletter Calligraphy",
        category = "Broad-Nib Gothic",
        description = "Structured architectural broad-nib blackletter calligraphy",
        previewSample = "123,456 • Gothic Calligraphy 789",
        fontFamily = FontFamily.Serif,
        numberFontFamily = FontFamily.Serif,
        defaultFontWeight = FontWeight.ExtraBold
    ),
    BRUSH_PEN_CALLIGRAPHY(
        displayName = "Zen Brush Pen Calligraphy",
        category = "Brush Script",
        description = "Expressive organic Japanese sumi-e & modern brush pen strokes",
        previewSample = "123,456 • Brush Calligraphy 789",
        fontFamily = FontFamily.Cursive,
        numberFontFamily = FontFamily.Cursive,
        defaultFontWeight = FontWeight.Black
    ),
    COPPERPLATE_FLOURISH(
        displayName = "Copperplate Formal Calligraphy",
        category = "Spencerian Flourish",
        description = "Classic 19th-century pointed-pen Spencerian & Copperplate script",
        previewSample = "123,456 • Copperplate Script 789",
        fontFamily = FontFamily.Cursive,
        numberFontFamily = FontFamily.Cursive,
        defaultFontWeight = FontWeight.Normal,
        defaultFontStyle = FontStyle.Italic
    ),
    BOHEMIAN_HAND_LETTERING(
        displayName = "Boho Aesthetic Calligraphy",
        category = "Hand Lettering",
        description = "Artistic warm casual modern calligraphy with bespoke character",
        previewSample = "123,456 • Boho Lettering 789",
        fontFamily = FontFamily.Cursive,
        numberFontFamily = FontFamily.Cursive,
        defaultFontWeight = FontWeight.SemiBold
    ),
    ROYAL_CALLIGRAPHY(
        displayName = "Royal Majestic Calligraphy",
        category = "Court Calligraphy",
        description = "Dramatic luxury cursive calligraphy with bold commanding strokes",
        previewSample = "123,456 • Royal Calligraphy 789",
        fontFamily = FontFamily.Cursive,
        numberFontFamily = FontFamily.Cursive,
        defaultFontWeight = FontWeight.ExtraBold
    ),
    DANCING_CURSIVE(
        displayName = "Dancing Cursive Script",
        category = "Dancing Script",
        description = "Lively flowing rhythmic calligraphic cursive handwriting",
        previewSample = "123,456 • Dancing Script 789",
        fontFamily = FontFamily.Cursive,
        numberFontFamily = FontFamily.Cursive,
        defaultFontWeight = FontWeight.Medium
    ),
    CASUAL_HANDWRITING(
        displayName = "Casual Human Script",
        category = "Handwritten",
        description = "Friendly, informal human handwriting aesthetic",
        previewSample = "123,456 • Human Script 789",
        fontFamily = FontFamily.Cursive,
        numberFontFamily = FontFamily.Cursive,
        defaultFontWeight = FontWeight.Normal
    ),
    BOLD_CASUAL(
        displayName = "Bold Marker Sketch",
        category = "Marker Script",
        description = "Chunky, expressive whiteboard marker hand-drawn typography",
        previewSample = "123,456 • Marker Script 789",
        fontFamily = FontFamily.Cursive,
        numberFontFamily = FontFamily.Cursive,
        defaultFontWeight = FontWeight.Bold
    ),

    // ==========================================
    // EDITORIAL & SERIF FONTS
    // ==========================================
    CLASSIC_SERIF(
        displayName = "Classic Editorial Serif",
        category = "Editorial",
        description = "Distinguished literary serif typography with refined serifs",
        previewSample = "123,456 • Editorial Serif 789",
        fontFamily = FontFamily.Serif,
        numberFontFamily = FontFamily.Serif,
        defaultFontWeight = FontWeight.Normal
    ),
    LUXURY_SERIF_ITALIC(
        displayName = "Luxury Serif Italic",
        category = "High Fashion",
        description = "Dramatic, high-contrast italic serif for upscale aesthetics",
        previewSample = "123,456 • Luxury Italic 789",
        fontFamily = FontFamily.Serif,
        numberFontFamily = FontFamily.Serif,
        defaultFontWeight = FontWeight.Bold,
        defaultFontStyle = FontStyle.Italic
    ),

    // ==========================================
    // DIGITAL, MATRIX & MONOSPACE FONTS
    // ==========================================
    CLEAN_MONOSPACE(
        displayName = "Digital Monospace",
        category = "Digital LED",
        description = "Standard digital fixed-width calculator typography",
        previewSample = "123,456 • Monospace 789",
        fontFamily = FontFamily.Monospace,
        numberFontFamily = FontFamily.Monospace,
        defaultFontWeight = FontWeight.Normal
    ),
    LCD_DIGITAL_BOLD(
        displayName = "LCD 7-Segment Bold",
        category = "7-Segment",
        description = "High-contrast segmented electronic calculator display",
        previewSample = "123,456 • Segment Bold 789",
        fontFamily = FontFamily.Monospace,
        numberFontFamily = FontFamily.Monospace,
        defaultFontWeight = FontWeight.ExtraBold,
        fontFeatureSettings = "tnum, zero"
    ),
    CYBER_CONDENSED(
        displayName = "Cyberpunk HUD Matrix",
        category = "Sci-Fi HUD",
        description = "Ultra-tall condensed digital geometry for sci-fi displays",
        previewSample = "123,456 • Matrix HUD 789",
        fontFamily = FontFamily.Monospace,
        numberFontFamily = FontFamily.Monospace,
        defaultFontWeight = FontWeight.Bold,
        fontFeatureSettings = "tnum"
    ),
    TERMINAL_MONOSPACE(
        displayName = "Retro Terminal Code",
        category = "Hacker Terminal",
        description = "Bold developer terminal monospace commands",
        previewSample = "123,456 • Terminal 789",
        fontFamily = FontFamily.Monospace,
        numberFontFamily = FontFamily.Monospace,
        defaultFontWeight = FontWeight.Bold
    ),
    CONDENSED_DIGITAL(
        displayName = "Compact Digital Ledger",
        category = "Matrix Display",
        description = "Space-saving high-density numeric matrix typography",
        previewSample = "123,456 • Ledger Matrix 789",
        fontFamily = FontFamily.Monospace,
        numberFontFamily = FontFamily.Monospace,
        defaultFontWeight = FontWeight.Medium,
        fontFeatureSettings = "tnum"
    ),
    TYPEWRITER_DIGITAL(
        displayName = "Vintage Slab Matrix",
        category = "Mechanical",
        description = "Mechanical slab-serif monospace matrix letterforms",
        previewSample = "123,456 • Slab Matrix 789",
        fontFamily = FontFamily.Monospace,
        numberFontFamily = FontFamily.Monospace,
        defaultFontWeight = FontWeight.SemiBold
    ),
    INDUSTRIAL_DIGITAL(
        displayName = "Industrial Heavy LCD",
        category = "Heavy Matrix",
        description = "Massive punchy digital display for industrial hardware",
        previewSample = "123,456 • Heavy LCD 789",
        fontFamily = FontFamily.Monospace,
        numberFontFamily = FontFamily.Monospace,
        defaultFontWeight = FontWeight.Black,
        fontFeatureSettings = "zero"
    ),
    VINTAGE_TYPEWRITER(
        displayName = "Vintage Typewriter",
        category = "Vintage",
        description = "Distressed mechanical typewriter slab-serif letterforms",
        previewSample = "123,456 • Typewriter 789",
        fontFamily = FontFamily.Monospace,
        numberFontFamily = FontFamily.Monospace,
        defaultFontWeight = FontWeight.Medium
    ),

    // ==========================================
    // MODERN SANS & MINIMALIST FONTS
    // ==========================================
    SYSTEM(
        displayName = "System Clean Sans",
        category = "Modern Sans",
        description = "Clean standard Android device system typography",
        previewSample = "123,456 • Clean Sans 789",
        fontFamily = FontFamily.SansSerif,
        numberFontFamily = FontFamily.SansSerif,
        defaultFontWeight = FontWeight.Normal
    ),
    MINIMALIST_LIGHT(
        displayName = "Minimalist Hairline",
        category = "Minimalist",
        description = "Ultra-thin, airy, Scandinavian architectural aesthetic",
        previewSample = "123,456 • Hairline Sans 789",
        fontFamily = FontFamily.SansSerif,
        numberFontFamily = FontFamily.SansSerif,
        defaultFontWeight = FontWeight.Light
    ),
    INDUSTRIAL_BLACK(
        displayName = "Industrial Ultra-Black",
        category = "Display Bold",
        description = "Massive punchy heavyweight typeface with bold presence",
        previewSample = "123,456 • Heavy Black 789",
        fontFamily = FontFamily.SansSerif,
        numberFontFamily = FontFamily.SansSerif,
        defaultFontWeight = FontWeight.Black
    ),
    SMALL_CAPS_LEDGER(
        displayName = "Small Caps Ledger",
        category = "Accounting",
        description = "Distinguished financial accounting in small capital letters",
        previewSample = "123,456 • Small Caps 789",
        fontFamily = FontFamily.Serif,
        numberFontFamily = FontFamily.Serif,
        defaultFontWeight = FontWeight.Normal,
        fontFeatureSettings = "smcp, c2sc"
    ),
    GEOMETRIC_TECHNO(
        displayName = "Geometric Modern Sans",
        category = "Neo-Modern",
        description = "Well-balanced geometric sans with modern contrast",
        previewSample = "123,456 • Neo Sans 789",
        fontFamily = FontFamily.SansSerif,
        numberFontFamily = FontFamily.SansSerif,
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



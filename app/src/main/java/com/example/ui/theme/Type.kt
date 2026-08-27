package com.example.ui.theme

import android.graphics.Typeface
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
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
    val numberFontFamily: FontFamily
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
        fontFamily = FontFamily(Typeface.create("monospace", Typeface.NORMAL)),
        numberFontFamily = FontFamily(Typeface.create("monospace", Typeface.NORMAL))
    ),
    LCD_DIGITAL_BOLD(
        displayName = "LCD 7-Segment Bold",
        fontType = FontType.DIGITAL,
        category = "7-Segment",
        description = "High-contrast segmented electronic calculator display",
        previewSample = "123,456 • 123 456 789",
        fontFamily = FontFamily(Typeface.create("monospace", Typeface.BOLD)),
        numberFontFamily = FontFamily(Typeface.create("monospace", Typeface.BOLD))
    ),
    CYBER_CONDENSED(
        displayName = "Cyberpunk HUD Matrix",
        fontType = FontType.DIGITAL,
        category = "Sci-Fi HUD",
        description = "Ultra-tall condensed digital geometry for sci-fi displays",
        previewSample = "123,456 • 123 MATRIX",
        fontFamily = FontFamily(Typeface.create("sans-serif-condensed", Typeface.BOLD)),
        numberFontFamily = FontFamily(Typeface.create("monospace", Typeface.BOLD))
    ),
    TERMINAL_MONOSPACE(
        displayName = "Retro Terminal Code",
        fontType = FontType.DIGITAL,
        category = "Hacker Terminal",
        description = "Bold green developer terminal monospace commands",
        previewSample = "123,456 • 123 ROOT EXEC",
        fontFamily = FontFamily(Typeface.create("monospace", Typeface.BOLD)),
        numberFontFamily = FontFamily(Typeface.create("monospace", Typeface.BOLD))
    ),
    CONDENSED_DIGITAL(
        displayName = "Compact Digital Ledger",
        fontType = FontType.DIGITAL,
        category = "Matrix Display",
        description = "Space-saving high-density numeric matrix typography",
        previewSample = "123,456 • 123 DATA LOG",
        fontFamily = FontFamily(Typeface.create("sans-serif-condensed", Typeface.NORMAL)),
        numberFontFamily = FontFamily(Typeface.create("monospace", Typeface.NORMAL))
    ),
    TYPEWRITER_DIGITAL(
        displayName = "Vintage Slab Matrix",
        fontType = FontType.DIGITAL,
        category = "Mechanical",
        description = "Mechanical slab-serif monospace matrix numbers",
        previewSample = "123,456 • 123 SLAB TECH",
        fontFamily = FontFamily(Typeface.create("serif-monospace", Typeface.NORMAL)),
        numberFontFamily = FontFamily(Typeface.create("serif-monospace", Typeface.BOLD))
    ),
    INDUSTRIAL_DIGITAL(
        displayName = "Industrial Heavy LCD",
        fontType = FontType.DIGITAL,
        category = "Heavy Matrix",
        description = "Massive punchy digital display for industrial hardware",
        previewSample = "123,456 • 123 POWER ON",
        fontFamily = FontFamily(Typeface.create("sans-serif-black", Typeface.BOLD)),
        numberFontFamily = FontFamily(Typeface.create("monospace", Typeface.BOLD))
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
        numberFontFamily = FontFamily(Typeface.create("monospace", Typeface.NORMAL))
    ),
    CASUAL_HANDWRITING(
        displayName = "Casual Human Script",
        fontType = FontType.TEXT,
        category = "Handwritten",
        description = "Friendly, informal human handwriting aesthetic",
        previewSample = "123,456 • Precision 123",
        fontFamily = FontFamily(Typeface.create("casual", Typeface.NORMAL)),
        numberFontFamily = FontFamily(Typeface.create("casual", Typeface.BOLD))
    ),
    BOLD_CASUAL(
        displayName = "Bold Marker Sketch",
        fontType = FontType.TEXT,
        category = "Street Art",
        description = "Chunky, expressive whiteboard marker typography",
        previewSample = "123,456 • Precision 123",
        fontFamily = FontFamily(Typeface.create("casual", Typeface.BOLD)),
        numberFontFamily = FontFamily(Typeface.create("casual", Typeface.BOLD))
    ),
    DANCING_CURSIVE(
        displayName = "Dancing Cursive Script",
        fontType = FontType.TEXT,
        category = "Calligraphic",
        description = "Flowing, elegant calligraphic script handwriting",
        previewSample = "123,456 • Precision 123",
        fontFamily = FontFamily(Typeface.create("cursive", Typeface.NORMAL)),
        numberFontFamily = FontFamily(Typeface.create("cursive", Typeface.BOLD))
    ),
    ROYAL_CALLIGRAPHY(
        displayName = "Royal Script Bold",
        fontType = FontType.TEXT,
        category = "Luxury Script",
        description = "Dramatic luxury cursive calligraphy with bold strokes",
        previewSample = "123,456 • Precision 123",
        fontFamily = FontFamily(Typeface.create("cursive", Typeface.BOLD)),
        numberFontFamily = FontFamily(Typeface.create("cursive", Typeface.BOLD))
    ),
    CLASSIC_SERIF(
        displayName = "Classic Editorial Serif",
        fontType = FontType.TEXT,
        category = "Editorial",
        description = "Distinguished literary serif typography with refined serifs",
        previewSample = "123,456 • Precision 123",
        fontFamily = FontFamily(Typeface.create("serif", Typeface.NORMAL)),
        numberFontFamily = FontFamily(Typeface.create("serif", Typeface.NORMAL))
    ),
    LUXURY_SERIF_ITALIC(
        displayName = "Luxury Serif Italic",
        fontType = FontType.TEXT,
        category = "High Fashion",
        description = "Dramatic, high-contrast italic serif for upscale aesthetics",
        previewSample = "123,456 • Precision 123",
        fontFamily = FontFamily(Typeface.create("serif", Typeface.BOLD_ITALIC)),
        numberFontFamily = FontFamily(Typeface.create("serif", Typeface.BOLD_ITALIC))
    ),
    VINTAGE_TYPEWRITER(
        displayName = "Vintage Typewriter",
        fontType = FontType.TEXT,
        category = "Vintage",
        description = "Distressed mechanical typewriter slab-serif letterforms",
        previewSample = "123,456 • Precision 123",
        fontFamily = FontFamily(Typeface.create("serif-monospace", Typeface.NORMAL)),
        numberFontFamily = FontFamily(Typeface.create("serif-monospace", Typeface.NORMAL))
    ),
    MINIMALIST_LIGHT(
        displayName = "Minimalist Hairline",
        fontType = FontType.TEXT,
        category = "Minimalist",
        description = "Ultra-thin, airy, Scandinavian architectural aesthetic",
        previewSample = "123,456 • Precision 123",
        fontFamily = FontFamily(Typeface.create("sans-serif-light", Typeface.NORMAL)),
        numberFontFamily = FontFamily(Typeface.create("sans-serif-light", Typeface.NORMAL))
    ),
    INDUSTRIAL_BLACK(
        displayName = "Industrial Ultra-Black",
        fontType = FontType.TEXT,
        category = "Display Bold",
        description = "Massive punchy heavyweight typeface with bold presence",
        previewSample = "123,456 • Precision 123",
        fontFamily = FontFamily(Typeface.create("sans-serif-black", Typeface.BOLD)),
        numberFontFamily = FontFamily(Typeface.create("sans-serif-black", Typeface.BOLD))
    ),
    SMALL_CAPS_LEDGER(
        displayName = "Small Caps Ledger",
        fontType = FontType.TEXT,
        category = "Accounting",
        description = "Distinguished financial accounting in small capital letters",
        previewSample = "123,456 • Precision 123",
        fontFamily = FontFamily(Typeface.create("sans-serif-smallcaps", Typeface.NORMAL)),
        numberFontFamily = FontFamily(Typeface.create("sans-serif-smallcaps", Typeface.NORMAL))
    ),
    GEOMETRIC_TECHNO(
        displayName = "Geometric Modern Sans",
        fontType = FontType.TEXT,
        category = "Neo-Modern",
        description = "Well-balanced geometric sans with modern contrast",
        previewSample = "123,456 • Precision 123",
        fontFamily = FontFamily(Typeface.create("sans-serif-medium", Typeface.NORMAL)),
        numberFontFamily = FontFamily(Typeface.create("sans-serif-medium", Typeface.NORMAL))
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

    return Typography(
        displayLarge = TextStyle(
            fontFamily = numFont,
            fontWeight = FontWeight.Light,
            fontSize = 54.sp,
            lineHeight = 60.sp,
            letterSpacing = 1.sp
        ),
        displayMedium = TextStyle(
            fontFamily = numFont,
            fontWeight = FontWeight.Normal,
            fontSize = 42.sp,
            lineHeight = 48.sp,
            letterSpacing = 0.5.sp
        ),
        displaySmall = TextStyle(
            fontFamily = numFont,
            fontWeight = FontWeight.Medium,
            fontSize = 28.sp,
            lineHeight = 34.sp,
            letterSpacing = 0.5.sp
        ),
        headlineLarge = TextStyle(
            fontFamily = bodyFont,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            lineHeight = 32.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = bodyFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            lineHeight = 28.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = bodyFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            lineHeight = 26.sp
        ),
        titleLarge = TextStyle(
            fontFamily = bodyFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            lineHeight = 24.sp
        ),
        titleMedium = TextStyle(
            fontFamily = bodyFont,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            lineHeight = 22.sp
        ),
        titleSmall = TextStyle(
            fontFamily = bodyFont,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = bodyFont,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.25.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = bodyFont,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp
        ),
        bodySmall = TextStyle(
            fontFamily = bodyFont,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp
        ),
        labelLarge = TextStyle(
            fontFamily = numFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.5.sp
        ),
        labelMedium = TextStyle(
            fontFamily = bodyFont,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp
        ),
        labelSmall = TextStyle(
            fontFamily = bodyFont,
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp,
            lineHeight = 14.sp
        )
    )
}

val Typography = createAppTypography(AppFontStyle.SYSTEM)


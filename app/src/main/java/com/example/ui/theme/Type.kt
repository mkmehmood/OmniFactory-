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

enum class AppFontStyle(
    val displayName: String,
    val category: String,
    val description: String,
    val previewSample: String,
    val fontFamily: FontFamily,
    val numberFontFamily: FontFamily
) {
    SYSTEM(
        displayName = "System Default",
        category = "Clean",
        description = "Standard device system font with digital alignment",
        previewSample = "123,456.78 • Precision Suite",
        fontFamily = FontFamily.Default,
        numberFontFamily = FontFamily(Typeface.create("monospace", Typeface.NORMAL))
    ),
    CASUAL_HANDWRITING(
        displayName = "Casual Handwriting",
        category = "Handwritten",
        description = "Friendly, informal human handwriting aesthetic",
        previewSample = "123,456.78 • Precision Suite",
        fontFamily = FontFamily(Typeface.create("casual", Typeface.NORMAL)),
        numberFontFamily = FontFamily(Typeface.create("casual", Typeface.BOLD))
    ),
    BOLD_CASUAL(
        displayName = "Bold Marker Sketch",
        category = "Street Art",
        description = "Chunky, expressive whiteboard marker typography",
        previewSample = "123,456.78 • Precision Suite",
        fontFamily = FontFamily(Typeface.create("casual", Typeface.BOLD)),
        numberFontFamily = FontFamily(Typeface.create("casual", Typeface.BOLD))
    ),
    DANCING_CURSIVE(
        displayName = "Dancing Cursive",
        category = "Calligraphic",
        description = "Flowing, elegant calligraphic script handwriting",
        previewSample = "123,456.78 • Precision Suite",
        fontFamily = FontFamily(Typeface.create("cursive", Typeface.NORMAL)),
        numberFontFamily = FontFamily(Typeface.create("cursive", Typeface.BOLD))
    ),
    ROYAL_CALLIGRAPHY(
        displayName = "Royal Script Bold",
        category = "Artistic",
        description = "Dramatic luxury cursive calligraphy with bold strokes",
        previewSample = "123,456.78 • Precision Suite",
        fontFamily = FontFamily(Typeface.create("cursive", Typeface.BOLD)),
        numberFontFamily = FontFamily(Typeface.create("cursive", Typeface.BOLD))
    ),
    CLASSIC_SERIF(
        displayName = "Classic Editorial Serif",
        category = "Editorial",
        description = "Distinguished literary serif typography with refined serifs",
        previewSample = "123,456.78 • Precision Suite",
        fontFamily = FontFamily(Typeface.create("serif", Typeface.NORMAL)),
        numberFontFamily = FontFamily(Typeface.create("serif", Typeface.NORMAL))
    ),
    LUXURY_SERIF_ITALIC(
        displayName = "Luxury Serif Italic",
        category = "High Fashion",
        description = "Dramatic, high-contrast italic serif for upscale aesthetics",
        previewSample = "123,456.78 • Precision Suite",
        fontFamily = FontFamily(Typeface.create("serif", Typeface.BOLD_ITALIC)),
        numberFontFamily = FontFamily(Typeface.create("serif", Typeface.BOLD_ITALIC))
    ),
    VINTAGE_TYPEWRITER(
        displayName = "Vintage Typewriter",
        category = "Vintage",
        description = "Distressed mechanical typewriter slab-serif letterforms",
        previewSample = "123,456.78 • Precision Suite",
        fontFamily = FontFamily(Typeface.create("serif-monospace", Typeface.NORMAL)),
        numberFontFamily = FontFamily(Typeface.create("serif-monospace", Typeface.NORMAL))
    ),
    CYBER_CONDENSED(
        displayName = "Cyberpunk Condensed",
        category = "Futuristic",
        description = "Ultra-tall, high-impact condensed sci-fi HUD geometry",
        previewSample = "123,456.78 • Precision Suite",
        fontFamily = FontFamily(Typeface.create("sans-serif-condensed", Typeface.BOLD)),
        numberFontFamily = FontFamily(Typeface.create("sans-serif-condensed", Typeface.BOLD))
    ),
    CONDENSED_CLEAN(
        displayName = "Condensed Ledger",
        category = "Finance",
        description = "Compact space-saving font engineered for high data density",
        previewSample = "123,456.78 • Precision Suite",
        fontFamily = FontFamily(Typeface.create("sans-serif-condensed", Typeface.NORMAL)),
        numberFontFamily = FontFamily(Typeface.create("sans-serif-condensed", Typeface.NORMAL))
    ),
    MINIMALIST_LIGHT(
        displayName = "Minimalist Hairline",
        category = "Minimalist",
        description = "Ultra-thin, airy, Scandinavian architectural aesthetic",
        previewSample = "123,456.78 • Precision Suite",
        fontFamily = FontFamily(Typeface.create("sans-serif-light", Typeface.NORMAL)),
        numberFontFamily = FontFamily(Typeface.create("sans-serif-light", Typeface.NORMAL))
    ),
    INDUSTRIAL_BLACK(
        displayName = "Industrial Ultra-Black",
        category = "Industrial",
        description = "Massive punchy heavyweight typeface with bold presence",
        previewSample = "123,456.78 • Precision Suite",
        fontFamily = FontFamily(Typeface.create("sans-serif-black", Typeface.BOLD)),
        numberFontFamily = FontFamily(Typeface.create("sans-serif-black", Typeface.BOLD))
    ),
    TERMINAL_MONOSPACE(
        displayName = "Terminal Hacker Code",
        category = "Developer",
        description = "High-contrast bold monospace matrix terminal typography",
        previewSample = "123,456.78 • Precision Suite",
        fontFamily = FontFamily(Typeface.create("monospace", Typeface.BOLD)),
        numberFontFamily = FontFamily(Typeface.create("monospace", Typeface.BOLD))
    ),
    CLEAN_MONOSPACE(
        displayName = "Digital Monospace",
        category = "Calculator",
        description = "Standard clean fixed-width digital calculator font",
        previewSample = "123,456.78 • Precision Suite",
        fontFamily = FontFamily(Typeface.create("monospace", Typeface.NORMAL)),
        numberFontFamily = FontFamily(Typeface.create("monospace", Typeface.NORMAL))
    ),
    SMALL_CAPS_LEDGER(
        displayName = "Small Caps Ledger",
        category = "Accounting",
        description = "Distinguished financial accounting format in small capital letters",
        previewSample = "123,456.78 • Precision Suite",
        fontFamily = FontFamily(Typeface.create("sans-serif-smallcaps", Typeface.NORMAL)),
        numberFontFamily = FontFamily(Typeface.create("sans-serif-smallcaps", Typeface.NORMAL))
    ),
    GEOMETRIC_TECHNO(
        displayName = "Geometric Medium",
        category = "Modern",
        description = "Well-balanced geometric sans with modern contrast",
        previewSample = "123,456.78 • Precision Suite",
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


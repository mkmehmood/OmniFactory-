package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

// Uploaded Begonia custom font family
val BegoniaFontFamily = FontFamily(
    Font(R.font.begonia, FontWeight.Normal)
)

enum class AppFontStyle(
    val displayName: String = "Begonia",
    val category: String = "Custom Typography",
    val description: String = "Pure Begonia Custom Font",
    val previewSample: String = "176 • 123 + 456 = 579",
    val defaultFontWeight: FontWeight = FontWeight.Normal,
    val defaultFontStyle: FontStyle = FontStyle.Normal,
    val fontFeatureSettings: String? = null
) {
    BEGONIA,
    SYSTEM,
    // Preserved enum symbols for stored preference backwards-compatibility; all map to Begonia
    MODERN_CALLIGRAPHY_SIGNATURE,
    ROMANTIC_FLOURISH_CALLIGRAPHY,
    CHIC_BRUSH_CALLIGRAPHY,
    PARISIAN_HAUTE_CALLIGRAPHY,
    MINIMALIST_MONOLINE_SCRIPT,
    ROYAL_MAJESTIC_CALLIGRAPHY,
    DANCING_BOUNCY_SCRIPT,
    CASUAL_HANDWRITING,
    BOLD_MARKER_SCRIPT,
    CLEAN_MONOSPACE,
    LCD_DIGITAL_BOLD,
    CYBER_CONDENSED,
    TERMINAL_MONOSPACE,
    CONDENSED_DIGITAL,
    TYPEWRITER_DIGITAL,
    INDUSTRIAL_DIGITAL,
    CLASSIC_SERIF,
    LUXURY_SERIF_ITALIC,
    VINTAGE_TYPEWRITER,
    MINIMALIST_LIGHT,
    INDUSTRIAL_BLACK,
    SMALL_CAPS_LEDGER,
    GEOMETRIC_TECHNO;

    val fontFamily: FontFamily
        get() = BegoniaFontFamily

    val numberFontFamily: FontFamily
        get() = BegoniaFontFamily
}

val LocalAppFontStyle = staticCompositionLocalOf { AppFontStyle.BEGONIA }

// Dynamic reactive font family for numbers and display that uses Begonia custom font
val DigitalMonospaceFont: FontFamily
    @Composable
    @ReadOnlyComposable
    get() = BegoniaFontFamily

fun createAppTypography(fontStyle: AppFontStyle? = null): Typography {
    val uniformFont = BegoniaFontFamily

    return Typography(
        displayLarge = TextStyle(
            fontFamily = uniformFont,
            fontWeight = FontWeight.Normal,
            fontSize = 54.sp,
            lineHeight = 60.sp,
            letterSpacing = 1.sp
        ),
        displayMedium = TextStyle(
            fontFamily = uniformFont,
            fontWeight = FontWeight.Normal,
            fontSize = 42.sp,
            lineHeight = 48.sp,
            letterSpacing = 0.5.sp
        ),
        displaySmall = TextStyle(
            fontFamily = uniformFont,
            fontWeight = FontWeight.Normal,
            fontSize = 28.sp,
            lineHeight = 34.sp,
            letterSpacing = 0.5.sp
        ),
        headlineLarge = TextStyle(
            fontFamily = uniformFont,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            lineHeight = 32.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = uniformFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            lineHeight = 28.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = uniformFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            lineHeight = 26.sp
        ),
        titleLarge = TextStyle(
            fontFamily = uniformFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            lineHeight = 24.sp
        ),
        titleMedium = TextStyle(
            fontFamily = uniformFont,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            lineHeight = 22.sp
        ),
        titleSmall = TextStyle(
            fontFamily = uniformFont,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = uniformFont,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.25.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = uniformFont,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp
        ),
        bodySmall = TextStyle(
            fontFamily = uniformFont,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp
        ),
        labelLarge = TextStyle(
            fontFamily = uniformFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.5.sp
        ),
        labelMedium = TextStyle(
            fontFamily = uniformFont,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp
        ),
        labelSmall = TextStyle(
            fontFamily = uniformFont,
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp,
            lineHeight = 14.sp
        )
    )
}

val Typography: Typography = createAppTypography(null)

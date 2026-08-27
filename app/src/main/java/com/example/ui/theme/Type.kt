package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

enum class AppFontStyle(
    val displayName: String,
    val description: String,
    val previewSample: String,
    val fontFamily: FontFamily,
    val numberFontFamily: FontFamily
) {
    SYSTEM(
        displayName = "System Default",
        description = "Standard device system font",
        previewSample = "123,456.78 • Precision Suite",
        fontFamily = FontFamily.Default,
        numberFontFamily = FontFamily.Monospace
    ),
    SANS_SERIF(
        displayName = "Modern Sans-Serif",
        description = "Clean, geometric and high-readability sans-serif",
        previewSample = "123,456.78 • Precision Suite",
        fontFamily = FontFamily.SansSerif,
        numberFontFamily = FontFamily.SansSerif
    ),
    MONOSPACE(
        displayName = "Digital Monospace",
        description = "Fixed-width terminal code & scientific calculator font",
        previewSample = "123,456.78 • Precision Suite",
        fontFamily = FontFamily.Monospace,
        numberFontFamily = FontFamily.Monospace
    ),
    SERIF(
        displayName = "Classic Serif",
        description = "Refined literary editorial serif with elegant strokes",
        previewSample = "123,456.78 • Precision Suite",
        fontFamily = FontFamily.Serif,
        numberFontFamily = FontFamily.Serif
    ),
    CURSIVE(
        displayName = "Creative Cursive",
        description = "Artistic handwriting and calligraphic script aesthetic",
        previewSample = "123,456.78 • Precision Suite",
        fontFamily = FontFamily.Cursive,
        numberFontFamily = FontFamily.Cursive
    )
}

val LocalAppFontStyle = staticCompositionLocalOf { AppFontStyle.SYSTEM }

// Digital Calculator monospaced font family for clean numeric alignment and calculator-first aesthetics
val DigitalMonospaceFont = FontFamily.Monospace

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


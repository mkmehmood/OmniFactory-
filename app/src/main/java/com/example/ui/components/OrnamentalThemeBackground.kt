package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.ui.theme.AppThemeSkin

/**
 * Clean, lightweight, zero-overhead background following the Photo Theme:
 * Pure Dark Charcoal (OLED Black #000000) or Pure Light (#FFFFFF).
 * All heavy ornamental animations and texture draws removed for maximum performance.
 */
@Composable
fun OrnamentalThemeBackground(
    appThemeSkin: AppThemeSkin,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}
) {
    val baseBg = MaterialTheme.colorScheme.background

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseBg)
            .testTag("ornamental_theme_background")
    ) {
        content()
    }
}

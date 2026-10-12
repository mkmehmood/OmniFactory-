package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import com.example.ui.theme.AppThemeSkin

/**
 * Flat soft-UI surface. Raised and inset parts are moulded from this same colour,
 * so the background itself stays plain.
 */
@Composable
fun OrnamentalThemeBackground(
    appThemeSkin: AppThemeSkin,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}
) {
    val baseBg = MaterialTheme.colorScheme.background
    val isDark = baseBg.luminance() < 0.5f

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseBg)
            .testTag("ornamental_theme_background")
    ) {
        content()
    }
}

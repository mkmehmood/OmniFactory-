package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import com.example.ui.theme.AppThemeSkin

/**
 * Skeuomorphic chassis behind every screen: a matte moulded body with a soft
 * overhead light falloff and fine horizontal machining lines. Drawn with a handful of
 * cheap vector primitives (no bitmaps) so it costs next to nothing per frame.
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
            .drawBehind {
                // Overhead light falloff
                drawRect(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (isDark) 0.06f else 0.30f),
                            Color.Transparent,
                            Color.Black.copy(alpha = if (isDark) 0.35f else 0.10f)
                        )
                    )
                )
                // Fine machining lines
                val step = 3f * density
                var y = 0f
                val line = if (isDark) Color.White.copy(alpha = 0.018f) else Color.Black.copy(alpha = 0.025f)
                while (y < size.height) {
                    drawLine(line, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                    y += step
                }
            }
            .testTag("ornamental_theme_background")
    ) {
        content()
    }
}

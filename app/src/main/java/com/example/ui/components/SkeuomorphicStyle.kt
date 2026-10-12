package com.example.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.lerp
import com.example.ui.theme.AppThemeSkin
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Skeuomorphic design primitives.
 *
 * The whole app is built from three physical materials:
 *  - a RAISED plate (keys, buttons, panels): lit from the top-left, casts a shadow, has a bevelled rim
 *  - an INSET well (text fields, display window): recessed into the body, shadow falls inside from the top
 *  - an LED (status lamp): glowing jewel with a specular dot
 */

internal fun Color.lighten(fraction: Float): Color = lerp(this, Color.White, fraction.coerceIn(0f, 1f))
internal fun Color.darken(fraction: Float): Color = lerp(this, Color.Black, fraction.coerceIn(0f, 1f))

/** Opaque colour of a key / button cap for this skin (legacy skins use translucent variants). */
internal fun AppThemeSkin.plate(): Color = surfaceVariant.compositeOver(surface)

/**
 * Raised plate. [pressed] collapses the shadow and inverts the light so the part looks pushed down.
 */
fun Modifier.skeuoRaised(
    shape: Shape,
    base: Color,
    isDark: Boolean,
    elevation: Dp = 6.dp,
    pressed: Boolean = false,
    accent: Color? = null,
    sheen: Float = 1f
): Modifier = this
    .shadow(
        elevation = if (pressed) 1.dp else elevation,
        shape = shape,
        ambientColor = Color.Black.copy(alpha = if (isDark) 0.65f else 0.28f),
        spotColor = Color.Black.copy(alpha = if (isDark) 0.80f else 0.38f)
    )
    .clip(shape)
    .drawBehind {
        val top = if (pressed) base.darken(0.10f) else base.lighten(if (isDark) 0.10f else 0.35f)
        val bottom = if (pressed) base.darken(0.02f) else base.darken(if (isDark) 0.22f else 0.10f)
        drawRect(Brush.verticalGradient(listOf(top, bottom)))

        // Soft overhead sheen on the upper half, like a moulded plastic or enamel surface
        if (!pressed && sheen > 0f) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = (if (isDark) 0.10f else 0.35f) * sheen),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = size.height * 0.55f
                )
            )
        }

        // Pressed parts pick up the shade cast by the rim above them
        if (pressed) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Black.copy(alpha = if (isDark) 0.45f else 0.18f), Color.Transparent),
                    startY = 0f,
                    endY = size.height * 0.35f
                )
            )
        }

        if (accent != null) {
            drawRect(accent.copy(alpha = if (isDark) 0.14f else 0.10f))
        }
    }
    .border(
        width = 1.dp,
        brush = Brush.linearGradient(
            colors = if (pressed) {
                listOf(Color.Black.copy(alpha = 0.45f), Color.White.copy(alpha = if (isDark) 0.10f else 0.45f))
            } else {
                listOf(
                    Color.White.copy(alpha = if (isDark) 0.28f else 0.85f),
                    Color.Black.copy(alpha = if (isDark) 0.65f else 0.30f)
                )
            },
            start = Offset(0f, 0f),
            end = Offset(0f, 200f)
        ),
        shape = shape
    )

/**
 * Inset well for text fields, the calculator window and any recessed slot.
 */
fun Modifier.skeuoInset(
    shape: Shape,
    base: Color,
    isDark: Boolean,
    focused: Boolean = false,
    accent: Color = Color(0xFFFF9F0A),
    error: Boolean = false,
    errorColor: Color = Color(0xFFB3261E)
): Modifier = this
    .clip(shape)
    .drawBehind {
        drawRect(
            Brush.verticalGradient(
                listOf(base.darken(if (isDark) 0.45f else 0.14f), base.darken(if (isDark) 0.25f else 0.04f))
            )
        )
        // Shadow cast into the well by the upper lip
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Black.copy(alpha = if (isDark) 0.60f else 0.24f), Color.Transparent),
                startY = 0f,
                endY = size.height.coerceAtMost(14.dp.toPx())
            )
        )
        if (focused) {
            drawRect(accent.copy(alpha = if (isDark) 0.10f else 0.08f))
        }
    }
    .border(
        width = if (focused || error) 1.5.dp else 1.dp,
        brush = when {
            error -> Brush.linearGradient(listOf(errorColor, errorColor))
            focused -> Brush.linearGradient(listOf(accent, accent.lighten(0.25f)))
            else -> Brush.verticalGradient(
                listOf(
                    Color.Black.copy(alpha = if (isDark) 0.70f else 0.35f),
                    Color.White.copy(alpha = if (isDark) 0.18f else 0.90f)
                )
            )
        },
        shape = shape
    )

/** Glowing jewel lamp used as a status indicator. */
@Composable
fun SkeuoLed(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 10.dp,
    lit: Boolean = true
) {
    val lamp = if (lit) color else color.darken(0.65f)
    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = if (lit) 5.dp else 0.dp,
                shape = CircleShape,
                ambientColor = color,
                spotColor = color
            )
            .clip(CircleShape)
            .drawBehind {
                drawRect(
                    Brush.radialGradient(
                        colors = listOf(lamp.lighten(0.55f), lamp, lamp.darken(0.45f)),
                        center = Offset(this.size.width * 0.38f, this.size.height * 0.32f),
                        radius = this.size.width * 0.85f
                    )
                )
            }
            .border(1.dp, Color.Black.copy(alpha = 0.45f), CircleShape)
    )
}

package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.composed
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import com.example.ui.theme.AppThemeSkin

/**
 * Soft-UI (neumorphic) primitives.
 *
 * Everything is moulded from one material that matches the screen colour:
 *  - RAISED: a white highlight falls to the top-left, a soft tinted shadow to the bottom-right
 *  - INSET:  the same two shadows fall *inside* the shape, so it reads as pressed into the surface
 *  - LED:    a small glowing lamp
 *
 * The shadows use Paint.setShadowLayer, which renders on all API levels in software
 * and on API 28+ with hardware acceleration.
 */

internal fun Color.lighten(fraction: Float): Color = lerp(this, Color.White, fraction.coerceIn(0f, 1f))
internal fun Color.darken(fraction: Float): Color = lerp(this, Color.Black, fraction.coerceIn(0f, 1f))

/** Opaque colour of a key / button cap for this skin (legacy skins use translucent variants). */
internal fun AppThemeSkin.plate(): Color = surfaceVariant.compositeOver(surface)

internal fun softDark(isDark: Boolean) = if (isDark) Color(0xCC0C0E12) else Color(0xB3A3B1CA)
internal fun softLight(isDark: Boolean) = if (isDark) Color(0x663C4452) else Color(0xFFFFFFFF)

private fun DrawScope.shapePath(shape: Shape): Path {
    val outline = shape.createOutline(size, layoutDirection, this)
    return Path().apply { addOutline(outline) }
}

private fun DrawScope.drawShapeShadow(path: Path, fill: Color, shadow: Color, dx: Float, dy: Float, blur: Float, alpha: Float = 1f) {
    drawIntoCanvas { canvas ->
        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = fill.toArgb()
            setShadowLayer(blur, dx, dy, shadow.toArgb())
            this.alpha = (alpha.coerceIn(0f, 1f) * 255f).roundToInt()
        }
        canvas.nativeCanvas.drawPath(path.asAndroidPath(), paint)
    }
}

private fun DrawScope.drawInnerShadow(path: Path, shadow: Color, dx: Float, dy: Float, blur: Float) {
    clipPath(path) {
        drawIntoCanvas { canvas ->
            val ring = android.graphics.Path().apply {
                fillType = android.graphics.Path.FillType.EVEN_ODD
                addRect(
                    -size.width, -size.height, size.width * 2f, size.height * 2f,
                    android.graphics.Path.Direction.CW
                )
                addPath(path.asAndroidPath())
            }
            val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.BLACK
                setShadowLayer(blur, dx, dy, shadow.toArgb())
            }
            canvas.nativeCanvas.drawPath(ring, paint)
        }
    }
}

/**
 * Raised soft plate. [pressed] flips it into an inset so the part looks pushed into the surface.
 */
fun Modifier.skeuoRaised(
    shape: Shape,
    base: Color,
    isDark: Boolean,
    elevation: Dp = 6.dp,
    pressed: Boolean = false,
    accent: Color? = null,
    sheen: Float = 1f
): Modifier = composed {
    // Press and selection ease in and out instead of snapping, which is what makes keys feel physical
    val press by animateFloatAsState(
        targetValue = if (pressed) 1f else 0f,
        animationSpec = tween(durationMillis = 110, easing = FastOutSlowInEasing),
        label = "soft_press"
    )
    val tint by animateFloatAsState(
        targetValue = if (accent != null) 1f else 0f,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "soft_tint"
    )
    val lastAccent = remember { arrayOf(accent ?: Color.Transparent) }
    if (accent != null) lastAccent[0] = accent

    this.drawBehind {
        val p = press
        val r = 1f - p
        val path = shapePath(shape)
        // Scale the shadow to the part so small pills don't get huge halos
        val d = minOf(elevation.toPx() * 0.85f, size.minDimension * 0.11f).coerceAtLeast(1.5f)
        val blur = d * 2f

        if (r > 0.01f) {
            val reach = 0.4f + 0.6f * r
            drawShapeShadow(path, base, softLight(isDark), -d * reach, -d * reach, blur, r)
            drawShapeShadow(path, base, softDark(isDark), d * reach, d * reach, blur, r)
            drawPath(
                path,
                Brush.linearGradient(
                    colors = listOf(
                        base.lighten((if (isDark) 0.05f else 0.18f) * sheen),
                        base.darken(if (isDark) 0.06f else 0.03f)
                    ),
                    start = Offset.Zero,
                    end = Offset(size.width, size.height)
                ),
                alpha = r
            )
        }
        if (p > 0.01f) {
            drawPath(path, base.darken(if (isDark) 0.10f else 0.03f), alpha = p)
            drawInnerShadow(path, softDark(isDark).let { it.copy(alpha = it.alpha * p) }, d * 0.6f, d * 0.6f, blur * 0.7f)
            drawInnerShadow(path, softLight(isDark).let { it.copy(alpha = it.alpha * p) }, -d * 0.6f, -d * 0.6f, blur * 0.7f)
        }
        if (tint > 0.01f) {
            drawPath(path, lastAccent[0].copy(alpha = (if (isDark) 0.16f else 0.12f) * tint))
        }
    }
}

/**
 * Inset well for text fields, the calculator window and any recessed slot.
 */
fun Modifier.skeuoInset(
    shape: Shape,
    base: Color,
    isDark: Boolean,
    focused: Boolean = false,
    accent: Color = Color(0xFFFF7A59),
    error: Boolean = false,
    errorColor: Color = Color(0xFFB3261E)
): Modifier = this
    .drawBehind {
        val path = shapePath(shape)
        val d = 3.dp.toPx()
        drawPath(path, base.darken(if (isDark) 0.10f else 0.025f))
        drawInnerShadow(path, softDark(isDark), d, d, d * 2.2f)
        drawInnerShadow(path, softLight(isDark), -d, -d, d * 2.2f)
        if (focused) drawPath(path, accent.copy(alpha = 0.07f))
    }
    .clip(shape)
    .let {
        if (focused || error) it.border(1.5.dp, if (error) errorColor else accent, shape) else it
    }

/** Soft glowing lamp used as a status indicator. */
@Composable
fun SkeuoLed(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 10.dp,
    lit: Boolean = true
) {
    val lamp = if (lit) color else color.darken(0.55f)
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
                        colors = listOf(lamp.lighten(0.5f), lamp),
                        center = Offset(this.size.width * 0.38f, this.size.height * 0.32f),
                        radius = this.size.width * 0.85f
                    )
                )
            }
    )
}

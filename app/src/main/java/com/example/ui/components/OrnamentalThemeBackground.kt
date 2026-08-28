package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.testTag
import com.example.ui.theme.AppThemeSkin
import com.example.ui.theme.OrnamentalPatternStyle
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Renders rich procedural ornamental backgrounds, aromatic botanicals, sacred mandalas,
 * damask crests, and atmospheric filigree behind app content.
 */
@Composable
fun OrnamentalThemeBackground(
    appThemeSkin: AppThemeSkin,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "OrnamentalPulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlow"
    )

    val pattern = appThemeSkin.ornamentalPattern
    val baseBg = appThemeSkin.background
    val tint = appThemeSkin.ornamentalTintColor

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseBg)
            .testTag("ornamental_theme_background")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height

            // 1. Base ambient radial lighting for depth (skip for true black OLED)
            if (appThemeSkin != AppThemeSkin.OLED_TRUE_BLACK) {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            tint.copy(alpha = if (appThemeSkin.isDark) 0.12f * pulseGlow else 0.08f * pulseGlow),
                            Color.Transparent
                        ),
                        center = Offset(canvasW * 0.5f, canvasH * 0.35f),
                        radius = canvasW * 0.85f
                    )
                )

                // Secondary corner ambient warm glow
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            appThemeSkin.defaultAccent.copy(alpha = if (appThemeSkin.isDark) 0.08f else 0.05f),
                            Color.Transparent
                        ),
                        center = Offset(canvasW * 0.85f, canvasH * 0.85f),
                        radius = canvasW * 0.65f
                    )
                )
            }

            // 2. Draw pattern specific ornamental layers
            when (pattern) {
                OrnamentalPatternStyle.AROMATIC_LAVENDER -> {
                    drawLavenderBotanicalBackground(canvasW, canvasH, tint, appThemeSkin.isDark)
                }
                OrnamentalPatternStyle.SACRED_MANDALA -> {
                    drawSacredMandalaBackground(canvasW, canvasH, tint, appThemeSkin.isDark)
                }
                OrnamentalPatternStyle.BAROQUE_JASMINE -> {
                    drawBaroqueJasmineBackground(canvasW, canvasH, tint, appThemeSkin.isDark)
                }
                OrnamentalPatternStyle.VICTORIAN_DAMASK -> {
                    drawVictorianDamaskBackground(canvasW, canvasH, tint, appThemeSkin.isDark)
                }
                OrnamentalPatternStyle.BYZANTINE_GOLD -> {
                    drawByzantineGoldBackground(canvasW, canvasH, tint, appThemeSkin.isDark)
                }
                OrnamentalPatternStyle.MEDITERRANEAN_NEROLI -> {
                    drawMediterraneanNeroliBackground(canvasW, canvasH, tint, appThemeSkin.isDark)
                }
                OrnamentalPatternStyle.FESTIVE_RIBBON -> {
                    drawFestiveRibbonBackground(canvasW, canvasH, tint, appThemeSkin.isDark)
                }
                OrnamentalPatternStyle.SAKURA_PETALS -> {
                    drawSakuraPetalsBackground(canvasW, canvasH, tint, appThemeSkin.isDark)
                }
                OrnamentalPatternStyle.ROYAL_FLEUR_DE_LIS -> {
                    drawRoyalFleurDeLisBackground(canvasW, canvasH, tint, appThemeSkin.isDark)
                }
                OrnamentalPatternStyle.CYBER_CIRCUITS -> {
                    drawCyberCircuitsBackground(canvasW, canvasH, tint, appThemeSkin.isDark)
                }
                OrnamentalPatternStyle.CONSTELLATION_NEBULA -> {
                    drawConstellationsBackground(canvasW, canvasH, tint, appThemeSkin.isDark)
                }
                OrnamentalPatternStyle.MINIMALIST_AURA -> {
                    // Subtle ambient gradient circles
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(tint.copy(alpha = 0.06f), Color.Transparent),
                            center = Offset(canvasW * 0.15f, canvasH * 0.15f),
                            radius = canvasW * 0.4f
                        ),
                        radius = canvasW * 0.4f,
                        center = Offset(canvasW * 0.15f, canvasH * 0.15f)
                    )
                }
                OrnamentalPatternStyle.NONE -> {
                    // No pattern or glow for pure minimalist & OLED true black
                }
            }
        }

        // Render actual screen content over ornamental backdrop
        content()
    }
}

// =========================================================================
// 1. AROMATIC LAVENDER & BOTANICAL VINES
// =========================================================================
private fun DrawScope.drawLavenderBotanicalBackground(
    w: Float,
    h: Float,
    tint: Color,
    isDark: Boolean
) {
    val alphaMultiplier = if (isDark) 1.0f else 0.75f
    val strokeColor = tint.copy(alpha = 0.22f * alphaMultiplier)
    val petalColor = tint.copy(alpha = 0.35f * alphaMultiplier)
    val highlightPetal = Color(0xFFE9D5FF).copy(alpha = 0.45f * alphaMultiplier)

    // Top-right botanical corner spray
    translate(left = w * 0.82f, top = h * 0.08f) {
        drawLavenderSprig(petalColor, highlightPetal, strokeColor, scale = 1.3f, angle = 25f)
    }
    translate(left = w * 0.92f, top = h * 0.15f) {
        drawLavenderSprig(petalColor, highlightPetal, strokeColor, scale = 1.0f, angle = 45f)
    }

    // Top-left botanical corner spray
    translate(left = w * 0.14f, top = h * 0.06f) {
        drawLavenderSprig(petalColor, highlightPetal, strokeColor, scale = 1.1f, angle = -30f)
    }

    // Bottom decorative botanical border vine
    translate(left = w * 0.5f, top = h * 0.92f) {
        drawBotanicalVineScroll(strokeColor, petalColor, scale = 1.2f)
    }

    // Delicate rising aromatic scent curve trails
    val scentPath1 = Path().apply {
        moveTo(w * 0.15f, h * 0.85f)
        cubicTo(w * 0.1f, h * 0.65f, w * 0.25f, h * 0.5f, w * 0.18f, h * 0.35f)
        cubicTo(w * 0.12f, h * 0.22f, w * 0.22f, h * 0.12f, w * 0.15f, h * 0.05f)
    }
    drawPath(
        path = scentPath1,
        color = strokeColor.copy(alpha = 0.14f * alphaMultiplier),
        style = Stroke(width = 1.5f, cap = StrokeCap.Round)
    )

    val scentPath2 = Path().apply {
        moveTo(w * 0.85f, h * 0.88f)
        cubicTo(w * 0.9f, h * 0.68f, w * 0.75f, h * 0.52f, w * 0.82f, h * 0.36f)
        cubicTo(w * 0.88f, h * 0.24f, w * 0.78f, h * 0.14f, w * 0.85f, h * 0.04f)
    }
    drawPath(
        path = scentPath2,
        color = strokeColor.copy(alpha = 0.12f * alphaMultiplier),
        style = Stroke(width = 1.5f, cap = StrokeCap.Round)
    )

    // Scattered fragrant blossom dots
    val dots = listOf(
        Offset(w * 0.3f, h * 0.18f),
        Offset(w * 0.7f, h * 0.22f),
        Offset(w * 0.45f, h * 0.82f),
        Offset(w * 0.2f, h * 0.75f),
        Offset(w * 0.8f, h * 0.7f)
    )
    dots.forEach { pt ->
        drawCircle(color = highlightPetal.copy(alpha = 0.25f), radius = 3.5f, center = pt)
        drawCircle(color = strokeColor, radius = 7f, center = pt, style = Stroke(width = 1f))
    }
}

private fun DrawScope.drawLavenderSprig(
    petalColor: Color,
    highlight: Color,
    stemColor: Color,
    scale: Float,
    angle: Float
) {
    rotate(angle) {
        scale(scale) {
            // Main stem
            drawLine(
                color = stemColor,
                start = Offset(0f, 60f),
                end = Offset(0f, -80f),
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )
            // Lavender bud clusters along the stem
            val heights = listOf(-60f, -45f, -30f, -15f, 0f, 15f, 30f)
            heights.forEachIndexed { index, y ->
                val spread = 12f + (index % 2) * 4f
                // Left petal
                drawOval(
                    color = if (index % 2 == 0) petalColor else highlight,
                    topLeft = Offset(-spread - 6f, y - 4f),
                    size = Size(12f, 8f)
                )
                // Right petal
                drawOval(
                    color = if (index % 2 == 0) highlight else petalColor,
                    topLeft = Offset(spread - 6f, y - 4f),
                    size = Size(12f, 8f)
                )
                // Center bud
                drawCircle(
                    color = petalColor,
                    radius = 3.5f,
                    center = Offset(0f, y)
                )
            }
            // Top bud
            drawOval(
                color = highlight,
                topLeft = Offset(-4f, -88f),
                size = Size(8f, 14f)
            )
        }
    }
}

private fun DrawScope.drawBotanicalVineScroll(
    stemColor: Color,
    petalColor: Color,
    scale: Float
) {
    scale(scale) {
        val path = Path().apply {
            moveTo(-120f, 0f)
            cubicTo(-60f, -25f, -30f, 20f, 0f, 0f)
            cubicTo(30f, -20f, 60f, 25f, 120f, 0f)
        }
        drawPath(
            path = path,
            color = stemColor,
            style = Stroke(width = 2f, cap = StrokeCap.Round)
        )
        // Little leaves
        val leafOffsets = listOf(-90f, -45f, 0f, 45f, 90f)
        leafOffsets.forEach { lx ->
            drawOval(
                color = petalColor,
                topLeft = Offset(lx - 6f, -14f),
                size = Size(12f, 8f)
            )
        }
    }
}

// =========================================================================
// 2. SACRED MANDALA & OUD ARABESQUE
// =========================================================================
private fun DrawScope.drawSacredMandalaBackground(
    w: Float,
    h: Float,
    tint: Color,
    isDark: Boolean
) {
    val alpha = if (isDark) 0.28f else 0.20f
    val strokeTint = tint.copy(alpha = alpha)
    val accentTint = Color(0xFFFFD700).copy(alpha = alpha * 0.8f)

    // Center Large Watermark Mandala
    translate(left = w * 0.5f, top = h * 0.45f) {
        drawMandalaMedallion(strokeTint, accentTint, radius = w * 0.42f, petals = 12)
    }

    // Top-Left Corner Mandala Segment
    translate(left = 0f, top = 0f) {
        drawMandalaMedallion(strokeTint, accentTint, radius = w * 0.32f, petals = 8)
    }

    // Bottom-Right Corner Mandala Segment
    translate(left = w, top = h) {
        drawMandalaMedallion(strokeTint, accentTint, radius = w * 0.32f, petals = 8)
    }

    // Aromatic Oud Incense Smoke Coils
    val incensePath = Path().apply {
        moveTo(w * 0.12f, h * 0.95f)
        cubicTo(w * 0.18f, h * 0.75f, w * 0.05f, h * 0.6f, w * 0.15f, h * 0.4f)
        cubicTo(w * 0.22f, h * 0.25f, w * 0.08f, h * 0.15f, w * 0.2f, h * 0.02f)
    }
    drawPath(
        path = incensePath,
        color = accentTint.copy(alpha = alpha * 0.6f),
        style = Stroke(width = 1.8f, cap = StrokeCap.Round)
    )
}

private fun DrawScope.drawMandalaMedallion(
    primary: Color,
    accent: Color,
    radius: Float,
    petals: Int
) {
    // Concentric geometric rings
    drawCircle(color = primary, radius = radius * 0.25f, style = Stroke(width = 1.2f))
    drawCircle(color = accent, radius = radius * 0.50f, style = Stroke(width = 1.5f))
    drawCircle(color = primary, radius = radius * 0.75f, style = Stroke(width = 1.2f))
    drawCircle(color = accent, radius = radius * 1.00f, style = Stroke(width = 1.8f))

    // Radial petal curves
    val angleStep = (2 * PI / petals).toFloat()
    for (i in 0 until petals) {
        val angleRad = i * angleStep
        val angleDeg = (angleRad * 180f / PI).toFloat()

        rotate(angleDeg) {
            // Petal loop
            val petalPath = Path().apply {
                moveTo(0f, radius * 0.25f)
                cubicTo(
                    radius * 0.22f, radius * 0.45f,
                    radius * 0.18f, radius * 0.70f,
                    0f, radius * 0.98f
                )
                cubicTo(
                    -radius * 0.18f, radius * 0.70f,
                    -radius * 0.22f, radius * 0.45f,
                    0f, radius * 0.25f
                )
            }
            drawPath(path = petalPath, color = primary, style = Stroke(width = 1.2f))

            // Inner flower leaf
            val innerPath = Path().apply {
                moveTo(0f, radius * 0.28f)
                cubicTo(
                    radius * 0.10f, radius * 0.40f,
                    radius * 0.08f, radius * 0.55f,
                    0f, radius * 0.72f
                )
                cubicTo(
                    -radius * 0.08f, radius * 0.55f,
                    -radius * 0.10f, radius * 0.40f,
                    0f, radius * 0.28f
                )
            }
            drawPath(path = innerPath, color = accent, style = Stroke(width = 1.0f))

            // Outer petal tip dot
            val dotY = radius * 1.05f
            drawCircle(color = accent, radius = 3.5f, center = Offset(0f, dotY))
        }
    }
}

// =========================================================================
// 3. BAROQUE JASMINE & WHITE TEA LACE
// =========================================================================
private fun DrawScope.drawBaroqueJasmineBackground(
    w: Float,
    h: Float,
    tint: Color,
    isDark: Boolean
) {
    val alpha = if (isDark) 0.26f else 0.22f
    val strokeColor = tint.copy(alpha = alpha)
    val petalColor = if (isDark) Color(0xFFFDF4FF).copy(alpha = alpha * 1.2f) else Color(0xFF64748B).copy(alpha = alpha * 0.9f)
    val goldCenter = Color(0xFFFBBF24).copy(alpha = alpha * 1.5f)

    // Corner Jasmine Wreaths
    translate(left = w * 0.85f, top = h * 0.12f) {
        drawJasmineFlower(petalColor, strokeColor, goldCenter, radius = 28f)
    }
    translate(left = w * 0.72f, top = h * 0.06f) {
        drawJasmineFlower(petalColor, strokeColor, goldCenter, radius = 20f)
    }
    translate(left = w * 0.15f, top = h * 0.08f) {
        drawJasmineFlower(petalColor, strokeColor, goldCenter, radius = 26f)
    }
    translate(left = w * 0.28f, top = h * 0.14f) {
        drawJasmineFlower(petalColor, strokeColor, goldCenter, radius = 18f)
    }

    // Baroque Lace Lattice in Background
    val latticeStep = 60f
    for (x in 0 until (w / latticeStep).toInt() + 2) {
        for (y in 0 until (h / latticeStep).toInt() + 2) {
            val posX = x * latticeStep
            val posY = y * latticeStep
            drawCircle(
                color = strokeColor.copy(alpha = alpha * 0.35f),
                radius = 1.5f,
                center = Offset(posX, posY)
            )
            if ((x + y) % 2 == 0) {
                // Small 4-petal lace flowerlet
                drawCircle(
                    color = strokeColor.copy(alpha = alpha * 0.25f),
                    radius = 8f,
                    center = Offset(posX, posY),
                    style = Stroke(width = 0.8f)
                )
            }
        }
    }

    // Bottom Jasmine Blossom Garland
    translate(left = w * 0.5f, top = h * 0.94f) {
        drawJasmineFlower(petalColor, strokeColor, goldCenter, radius = 22f)
        translate(left = -60f, top = 0f) {
            drawJasmineFlower(petalColor, strokeColor, goldCenter, radius = 16f)
        }
        translate(left = 60f, top = 0f) {
            drawJasmineFlower(petalColor, strokeColor, goldCenter, radius = 16f)
        }
    }
}

private fun DrawScope.drawJasmineFlower(
    petalFill: Color,
    outline: Color,
    centerColor: Color,
    radius: Float
) {
    val petals = 5
    val step = (2 * PI / petals).toFloat()
    for (i in 0 until petals) {
        val angleDeg = (i * step * 180f / PI).toFloat()
        rotate(angleDeg) {
            val petalPath = Path().apply {
                moveTo(0f, 0f)
                cubicTo(radius * 0.35f, radius * 0.5f, radius * 0.30f, radius * 0.9f, 0f, radius * 1.15f)
                cubicTo(-radius * 0.30f, radius * 0.9f, -radius * 0.35f, radius * 0.5f, 0f, 0f)
            }
            drawPath(path = petalPath, color = petalFill, style = Fill)
            drawPath(path = petalPath, color = outline, style = Stroke(width = 1f))
        }
    }
    drawCircle(color = centerColor, radius = radius * 0.25f)
    drawCircle(color = outline, radius = radius * 0.25f, style = Stroke(width = 1f))
}

// =========================================================================
// 4. VICTORIAN DAMASK & TAPESTRY CRESTS
// =========================================================================
private fun DrawScope.drawVictorianDamaskBackground(
    w: Float,
    h: Float,
    tint: Color,
    isDark: Boolean
) {
    val alpha = if (isDark) 0.24f else 0.18f
    val damaskTint = tint.copy(alpha = alpha)
    val goldTint = Color(0xFFFFD700).copy(alpha = alpha * 0.85f)

    // Repeat Damask Crests in a luxury staggered diamond grid
    val spacingX = w * 0.5f
    val spacingY = h * 0.32f

    for (row in 0..4) {
        val offsetY = row * spacingY - 40f
        val shiftX = if (row % 2 == 1) spacingX * 0.5f else 0f

        for (col in -1..2) {
            val offsetX = col * spacingX + shiftX
            translate(left = offsetX, top = offsetY) {
                drawDamaskCrest(damaskTint, goldTint, scale = 0.85f)
            }
        }
    }
}

private fun DrawScope.drawDamaskCrest(
    primary: Color,
    accent: Color,
    scale: Float
) {
    scale(scale) {
        // Central Damask Acanthus Crown
        val crownPath = Path().apply {
            moveTo(0f, -50f)
            cubicTo(18f, -42f, 32f, -25f, 25f, -5f)
            cubicTo(15f, 15f, 5f, 25f, 0f, 40f)
            cubicTo(-5f, 25f, -15f, 15f, -25f, -5f)
            cubicTo(-32f, -25f, -18f, -42f, 0f, -50f)
        }
        drawPath(path = crownPath, color = primary, style = Stroke(width = 1.8f, cap = StrokeCap.Round, join = StrokeJoin.Round))

        // Side Floral Wings
        val leftWing = Path().apply {
            moveTo(0f, 0f)
            cubicTo(-25f, -10f, -45f, -30f, -35f, -50f)
            cubicTo(-28f, -60f, -15f, -55f, -20f, -40f)
            cubicTo(-25f, -25f, -10f, -10f, 0f, 15f)
        }
        drawPath(path = leftWing, color = primary, style = Stroke(width = 1.4f))

        val rightWing = Path().apply {
            moveTo(0f, 0f)
            cubicTo(25f, -10f, 45f, -30f, 35f, -50f)
            cubicTo(28f, -60f, 15f, -55f, 20f, -40f)
            cubicTo(25f, -25f, 10f, -10f, 0f, 15f)
        }
        drawPath(path = rightWing, color = primary, style = Stroke(width = 1.4f))

        // Inner Heart Jewel & Dots
        drawCircle(color = accent, radius = 4f, center = Offset(0f, -15f))
        drawCircle(color = primary, radius = 7f, center = Offset(0f, -15f), style = Stroke(width = 1f))
        drawCircle(color = accent, radius = 3f, center = Offset(0f, 25f))
    }
}

// =========================================================================
// 5. BYZANTINE ARABESQUE & GOLDEN STARS
// =========================================================================
private fun DrawScope.drawByzantineGoldBackground(
    w: Float,
    h: Float,
    tint: Color,
    isDark: Boolean
) {
    val alpha = if (isDark) 0.28f else 0.22f
    val gold = Color(0xFFFFD700).copy(alpha = alpha)
    val amber = Color(0xFFF59E0B).copy(alpha = alpha * 0.8f)

    // Center Byzantine 8-point Star Medallion
    translate(left = w * 0.5f, top = h * 0.40f) {
        drawEightPointedStar(gold, amber, radius = w * 0.38f)
    }

    // Top Header & Bottom Decorative Borders
    for (i in 0..10) {
        val bx = (i * w / 10f)
        translate(left = bx, top = 20f) {
            drawSmallStar(gold, radius = 8f)
        }
        translate(left = bx, top = h - 30f) {
            drawSmallStar(gold, radius = 8f)
        }
    }

    // Geometric Interlocking Tessellation
    val step = w * 0.25f
    for (x in 0..4) {
        for (y in 0..6) {
            val cx = x * step
            val cy = y * step
            drawCircle(color = amber.copy(alpha = alpha * 0.3f), radius = step * 0.45f, center = Offset(cx, cy), style = Stroke(width = 0.8f))
        }
    }
}

private fun DrawScope.drawEightPointedStar(
    primary: Color,
    accent: Color,
    radius: Float
) {
    // 8-point geometric Islamic / Byzantine star
    val starPath1 = Path()
    val starPath2 = Path()

    // Square 1
    val s1 = radius * 0.75f
    starPath1.addRect(androidx.compose.ui.geometry.Rect(-s1, -s1, s1, s1))
    drawPath(path = starPath1, color = primary, style = Stroke(width = 1.6f))

    // Square 2 (rotated 45 deg)
    rotate(45f) {
        starPath2.addRect(androidx.compose.ui.geometry.Rect(-s1, -s1, s1, s1))
        drawPath(path = starPath2, color = primary, style = Stroke(width = 1.6f))
    }

    // Inner concentric star & rings
    drawCircle(color = accent, radius = radius * 0.45f, style = Stroke(width = 1.2f))
    drawCircle(color = primary, radius = radius * 0.25f, style = Stroke(width = 1.5f))
    drawCircle(color = accent, radius = 6f)

    // Outer rays
    for (a in 0 until 8) {
        rotate(a * 45f) {
            drawLine(
                color = accent,
                start = Offset(0f, radius * 0.75f),
                end = Offset(0f, radius * 1.05f),
                strokeWidth = 1.8f
            )
            drawCircle(color = primary, radius = 3.5f, center = Offset(0f, radius * 1.08f))
        }
    }
}

private fun DrawScope.drawSmallStar(color: Color, radius: Float) {
    val s = radius * 0.7f
    val path = Path()
    path.addRect(androidx.compose.ui.geometry.Rect(-s, -s, s, s))
    drawPath(path, color, style = Stroke(width = 1f))
    rotate(45f) {
        val path2 = Path()
        path2.addRect(androidx.compose.ui.geometry.Rect(-s, -s, s, s))
        drawPath(path2, color, style = Stroke(width = 1f))
    }
}

// =========================================================================
// 6. MEDITERRANEAN NEROLI & CITRUS TILE MAJOLICA
// =========================================================================
private fun DrawScope.drawMediterraneanNeroliBackground(
    w: Float,
    h: Float,
    tint: Color,
    isDark: Boolean
) {
    val alpha = if (isDark) 0.25f else 0.20f
    val neroliOrange = Color(0xFFF97316).copy(alpha = alpha * 1.2f)
    val tileAzure = Color(0xFF0284C7).copy(alpha = alpha)
    val leafGreen = Color(0xFF16A34A).copy(alpha = alpha * 0.9f)

    // 4-corner Majolica tile medallions
    val corners = listOf(
        Offset(50f, 50f),
        Offset(w - 50f, 50f),
        Offset(50f, h - 60f),
        Offset(w - 50f, h - 60f)
    )
    corners.forEach { pt ->
        translate(left = pt.x, top = pt.y) {
            drawMajolicaCorner(neroliOrange, tileAzure, leafGreen, size = 42f)
        }
    }

    // Center Sunburst Tile
    translate(left = w * 0.5f, top = h * 0.42f) {
        drawCircle(color = tileAzure, radius = 80f, style = Stroke(width = 1.5f))
        drawCircle(color = neroliOrange, radius = 55f, style = Stroke(width = 1.2f))
        drawCircle(color = leafGreen, radius = 30f, style = Stroke(width = 1.0f))
        for (i in 0 until 12) {
            rotate(i * 30f) {
                drawOval(
                    color = neroliOrange,
                    topLeft = Offset(-4f, 40f),
                    size = Size(8f, 18f)
                )
            }
        }
    }
}

private fun DrawScope.drawMajolicaCorner(
    orange: Color,
    azure: Color,
    green: Color,
    size: Float
) {
    drawCircle(color = azure, radius = size, style = Stroke(width = 1.4f))
    drawCircle(color = orange, radius = size * 0.6f, style = Stroke(width = 1.2f))
    for (i in 0 until 4) {
        rotate(i * 90f + 45f) {
            drawOval(color = green, topLeft = Offset(-3f, size * 0.25f), size = Size(6f, 14f))
        }
    }
}

// =========================================================================
// 7. FESTIVE RIBBON & GIFT CELEBRATION
// =========================================================================
private fun DrawScope.drawFestiveRibbonBackground(
    w: Float,
    h: Float,
    tint: Color,
    isDark: Boolean
) {
    val alpha = if (isDark) 0.30f else 0.24f
    val gold = Color(0xFFFFD700).copy(alpha = alpha)
    val ruby = Color(0xFFFF2A55).copy(alpha = alpha * 0.8f)

    // Diagonal Satin Gift Ribbon Stripes
    val ribbonWidth = 24f
    val ribbonPath = Path().apply {
        moveTo(0f, h * 0.25f)
        lineTo(w, h * 0.65f)
    }
    drawPath(
        path = ribbonPath,
        color = ruby,
        style = Stroke(width = ribbonWidth)
    )
    // Gold Trim Borders on Ribbon
    drawPath(
        path = ribbonPath,
        color = gold,
        style = Stroke(width = 2.5f)
    )

    // Top-Right Holiday Gift Bow Knot
    translate(left = w * 0.85f, top = h * 0.12f) {
        drawCircle(color = gold, radius = 18f, style = Stroke(width = 2f))
        drawOval(color = ruby, topLeft = Offset(-35f, -14f), size = Size(30f, 28f))
        drawOval(color = ruby, topLeft = Offset(5f, -14f), size = Size(30f, 28f))
        drawCircle(color = gold, radius = 6f)
    }

    // Twinkling Holiday Stars
    val stars = listOf(
        Offset(w * 0.2f, h * 0.12f),
        Offset(w * 0.15f, h * 0.45f),
        Offset(w * 0.85f, h * 0.75f),
        Offset(w * 0.4f, h * 0.85f),
        Offset(w * 0.75f, h * 0.35f)
    )
    stars.forEach { pt ->
        translate(left = pt.x, top = pt.y) {
            drawTwinkleStar(gold, size = 12f)
        }
    }
}

private fun DrawScope.drawTwinkleStar(color: Color, size: Float) {
    val path = Path().apply {
        moveTo(0f, -size)
        cubicTo(0f, 0f, 0f, 0f, size, 0f)
        cubicTo(0f, 0f, 0f, 0f, 0f, size)
        cubicTo(0f, 0f, 0f, 0f, -size, 0f)
        cubicTo(0f, 0f, 0f, 0f, 0f, -size)
    }
    drawPath(path = path, color = color, style = Fill)
}

// =========================================================================
// 8. SAKURA PETALS & SPRING BLOSSOM
// =========================================================================
private fun DrawScope.drawSakuraPetalsBackground(
    w: Float,
    h: Float,
    tint: Color,
    isDark: Boolean
) {
    val alpha = if (isDark) 0.26f else 0.22f
    val pink = Color(0xFFFB7185).copy(alpha = alpha)
    val softRose = Color(0xFFFDA4AF).copy(alpha = alpha * 1.2f)

    // Gentle Wind Breeze Lines
    val wind = Path().apply {
        moveTo(-20f, h * 0.2f)
        cubicTo(w * 0.3f, h * 0.15f, w * 0.6f, h * 0.35f, w + 20f, h * 0.28f)
    }
    drawPath(path = wind, color = softRose.copy(alpha = alpha * 0.4f), style = Stroke(width = 1.5f))

    // Floating 5-petal Sakura Flowers & Petals
    val petals = listOf(
        Triple(w * 0.18f, h * 0.15f, 22f),
        Triple(w * 0.82f, h * 0.22f, 18f),
        Triple(w * 0.75f, h * 0.68f, 25f),
        Triple(w * 0.25f, h * 0.80f, 20f),
        Triple(w * 0.50f, h * 0.92f, 16f)
    )
    petals.forEach { (px, py, rad) ->
        translate(left = px, top = py) {
            drawSakuraFlower(softRose, pink, radius = rad)
        }
    }
}

private fun DrawScope.drawSakuraFlower(petalColor: Color, centerColor: Color, radius: Float) {
    val petals = 5
    val step = (2 * PI / petals).toFloat()
    for (i in 0 until petals) {
        val angleDeg = (i * step * 180f / PI).toFloat()
        rotate(angleDeg) {
            val petal = Path().apply {
                moveTo(0f, 0f)
                cubicTo(radius * 0.4f, radius * 0.5f, radius * 0.35f, radius * 0.9f, 0f, radius * 1.05f)
                // Distinctive sakura petal notch
                lineTo(0f, radius * 0.92f)
                cubicTo(-radius * 0.35f, radius * 0.9f, -radius * 0.4f, radius * 0.5f, 0f, 0f)
            }
            drawPath(path = petal, color = petalColor, style = Fill)
            drawPath(path = petal, color = centerColor, style = Stroke(width = 0.8f))
        }
    }
    drawCircle(color = centerColor, radius = radius * 0.2f)
}

// =========================================================================
// 9. ROYAL FLEUR-DE-LIS & IMPERIAL HERALDRY
// =========================================================================
private fun DrawScope.drawRoyalFleurDeLisBackground(
    w: Float,
    h: Float,
    tint: Color,
    isDark: Boolean
) {
    val alpha = if (isDark) 0.24f else 0.18f
    val gold = Color(0xFFFFD700).copy(alpha = alpha)
    val purple = tint.copy(alpha = alpha * 0.9f)

    // Center Large Heraldic Fleur-de-Lis
    translate(left = w * 0.5f, top = h * 0.42f) {
        drawFleurDeLis(gold, purple, scale = 1.4f)
    }

    // Top-Left and Top-Right Crests
    translate(left = w * 0.18f, top = h * 0.10f) {
        drawFleurDeLis(gold, purple, scale = 0.8f)
    }
    translate(left = w * 0.82f, top = h * 0.10f) {
        drawFleurDeLis(gold, purple, scale = 0.8f)
    }
}

private fun DrawScope.drawFleurDeLis(primary: Color, accent: Color, scale: Float) {
    scale(scale) {
        // Center Petal (Flame Spear)
        val centerPetal = Path().apply {
            moveTo(0f, -40f)
            cubicTo(12f, -25f, 10f, -10f, 0f, 5f)
            cubicTo(-10f, -10f, -12f, -25f, 0f, -40f)
        }
        drawPath(path = centerPetal, color = primary, style = Fill)
        drawPath(path = centerPetal, color = accent, style = Stroke(width = 1f))

        // Left Curved Wing
        val leftWing = Path().apply {
            moveTo(0f, 0f)
            cubicTo(-15f, -5f, -30f, -20f, -25f, -30f)
            cubicTo(-20f, -35f, -10f, -25f, -12f, -15f)
            cubicTo(-14f, -5f, -5f, 5f, 0f, 5f)
        }
        drawPath(path = leftWing, color = primary, style = Fill)

        // Right Curved Wing
        val rightWing = Path().apply {
            moveTo(0f, 0f)
            cubicTo(15f, -5f, 30f, -20f, 25f, -30f)
            cubicTo(20f, -35f, 10f, -25f, 12f, -15f)
            cubicTo(14f, -5f, 5f, 5f, 0f, 5f)
        }
        drawPath(path = rightWing, color = primary, style = Fill)

        // Horizontal Belt Band
        val belt = Path().apply {
            moveTo(-20f, 6f)
            lineTo(20f, 6f)
            lineTo(16f, 12f)
            lineTo(-16f, 12f)
            close()
        }
        drawPath(path = belt, color = accent, style = Fill)

        // Bottom Stem Flake
        val bottomStem = Path().apply {
            moveTo(0f, 12f)
            cubicTo(10f, 22f, 8f, 28f, 0f, 35f)
            cubicTo(-8f, 28f, -10f, 22f, 0f, 12f)
        }
        drawPath(path = bottomStem, color = primary, style = Fill)
    }
}

// =========================================================================
// 10. CYBER CIRCUITS & DIGITAL LATTICE
// =========================================================================
private fun DrawScope.drawCyberCircuitsBackground(
    w: Float,
    h: Float,
    tint: Color,
    isDark: Boolean
) {
    val alpha = if (isDark) 0.26f else 0.18f
    val neon = tint.copy(alpha = alpha)

    // Circuit traces & nodes
    val path1 = Path().apply {
        moveTo(0f, h * 0.15f)
        lineTo(w * 0.35f, h * 0.15f)
        lineTo(w * 0.50f, h * 0.25f)
        lineTo(w * 0.85f, h * 0.25f)
        lineTo(w, h * 0.35f)
    }
    drawPath(path1, neon, style = Stroke(width = 1.5f))

    val path2 = Path().apply {
        moveTo(0f, h * 0.82f)
        lineTo(w * 0.25f, h * 0.82f)
        lineTo(w * 0.40f, h * 0.72f)
        lineTo(w * 0.75f, h * 0.72f)
        lineTo(w, h * 0.88f)
    }
    drawPath(path2, neon, style = Stroke(width = 1.5f))

    // Hexagon corners
    val hexOffsets = listOf(
        Offset(w * 0.35f, h * 0.15f),
        Offset(w * 0.50f, h * 0.25f),
        Offset(w * 0.85f, h * 0.25f),
        Offset(w * 0.25f, h * 0.82f),
        Offset(w * 0.75f, h * 0.72f)
    )
    hexOffsets.forEach { pt ->
        drawCircle(color = neon, radius = 4f, center = pt)
        drawCircle(color = neon.copy(alpha = alpha * 0.5f), radius = 8f, center = pt, style = Stroke(width = 1f))
    }
}

// =========================================================================
// 11. CONSTELLATIONS & CELESTIAL STARRY NIGHT
// =========================================================================
private fun DrawScope.drawConstellationsBackground(
    w: Float,
    h: Float,
    tint: Color,
    isDark: Boolean
) {
    val alpha = if (isDark) 0.28f else 0.18f
    val starColor = Color(0xFFF8FAFC).copy(alpha = alpha)
    val lineCol = tint.copy(alpha = alpha * 0.6f)

    // Major constellation 1 (Ursa style)
    val pts1 = listOf(
        Offset(w * 0.15f, h * 0.12f),
        Offset(w * 0.28f, h * 0.10f),
        Offset(w * 0.42f, h * 0.18f),
        Offset(w * 0.55f, h * 0.14f),
        Offset(w * 0.68f, h * 0.22f),
        Offset(w * 0.58f, h * 0.30f),
        Offset(w * 0.42f, h * 0.28f)
    )
    for (i in 0 until pts1.size - 1) {
        drawLine(lineCol, pts1[i], pts1[i + 1], strokeWidth = 1.2f)
    }
    drawLine(lineCol, pts1[6], pts1[2], strokeWidth = 1.2f)
    pts1.forEach { pt ->
        drawCircle(starColor, radius = 3.5f, center = pt)
    }

    // Southern cross constellation
    val pts2 = listOf(
        Offset(w * 0.82f, h * 0.75f),
        Offset(w * 0.82f, h * 0.90f),
        Offset(w * 0.74f, h * 0.82f),
        Offset(w * 0.90f, h * 0.82f)
    )
    drawLine(lineCol, pts2[0], pts2[1], strokeWidth = 1.2f)
    drawLine(lineCol, pts2[2], pts2[3], strokeWidth = 1.2f)
    pts2.forEach { pt ->
        drawCircle(starColor, radius = 3.5f, center = pt)
    }
}

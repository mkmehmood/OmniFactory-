package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardHide
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import com.example.ui.theme.toButtonShape
import com.example.ui.theme.toCardShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.util.HapticFeedbackHelper
import com.example.domain.util.SoundFeedbackHelper
import com.example.ui.theme.AnimationIntensity
import com.example.ui.theme.ComponentSizeStyle
import com.example.ui.theme.HapticIntensity
import com.example.ui.theme.LocalAnimationIntensity
import com.example.ui.theme.LocalComponentSizing
import com.example.ui.theme.LocalShapeStructure
import com.example.ui.theme.PureDarkGlassBorder
import com.example.ui.theme.PureDarkGlassSurface
import com.example.ui.theme.PureDarkGlassSurfacePressed
import com.example.ui.theme.PureDarkTextPrimary
import com.example.ui.theme.PureLightGlassBorder
import com.example.ui.theme.PureLightGlassSurface
import com.example.ui.theme.PureLightGlassSurfacePressed
import com.example.ui.theme.PureLightTextPrimary
import com.example.ui.theme.ShapeStructureStyle
import com.example.ui.theme.SoundIntensity
import com.example.ui.theme.toButtonShape
import kotlinx.coroutines.launch

enum class KeyType {
    NUMBER,
    DOUBLE_ZERO,
    OPERATOR,
    ACTION,
    EQUALS
}

@Composable
fun CalculatorKeypad(
    isAllClear: Boolean,
    onDigit: (String) -> Unit,
    onDoubleZero: () -> Unit,
    onOperator: (String) -> Unit,
    onPercent: () -> Unit,
    onClear: () -> Unit,
    onBackspace: () -> Unit,
    onEquals: () -> Unit,
    onNegate: () -> Unit,
    showDoubleZeroKey: Boolean = true,
    animationIntensity: AnimationIntensity = AnimationIntensity.BALANCED,
    hapticIntensity: HapticIntensity = HapticIntensity.MEDIUM,
    soundIntensity: SoundIntensity = SoundIntensity.STANDARD,
    componentSizeStyle: ComponentSizeStyle = ComponentSizeStyle.STANDARD,
    shapeStructureStyle: ShapeStructureStyle = ShapeStructureStyle.ROUNDED,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current

    val triggerFeedback = {
        HapticFeedbackHelper.performKeypadTap(context, view, hapticIntensity)
        SoundFeedbackHelper.playClickSound(context, view, soundIntensity)
    }

    val isDark = MaterialTheme.colorScheme.background.red < 0.2f
    val spacing = componentSizeStyle.keypadSpacing
    val padH = componentSizeStyle.keypadPaddingH
    val padV = componentSizeStyle.keypadPaddingV

    // Frosted glass keypad container
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
            .background(
                if (isDark) Color(0x14FFFFFF) else Color(0x33FFFFFF)
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        if (isDark) Color(0x33FFFFFF) else Color(0x40000000),
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            )
            .padding(horizontal = padH, vertical = padV)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing)
        ) {
            // Row 1: AC/C, ⌫ (Delete/Backspace SVG Icon), %, ÷
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                LiquidGlassKey(
                    text = if (isAllClear) "AC" else "C",
                    type = KeyType.ACTION,
                    onClick = { triggerFeedback(); onClear() },
                    testTag = "key_clear",
                    animationIntensity = animationIntensity,
                    componentSizeStyle = componentSizeStyle,
                    shapeStructureStyle = shapeStructureStyle,
                    modifier = Modifier.weight(1f)
                )
                LiquidGlassKey(
                    icon = Icons.AutoMirrored.Filled.Backspace,
                    iconContentDescription = "Delete",
                    type = KeyType.ACTION,
                    onClick = { triggerFeedback(); onBackspace() },
                    testTag = "key_backspace",
                    animationIntensity = animationIntensity,
                    componentSizeStyle = componentSizeStyle,
                    shapeStructureStyle = shapeStructureStyle,
                    modifier = Modifier.weight(1f)
                )
                LiquidGlassKey(
                    icon = Icons.Default.Percent,
                    iconContentDescription = "Percent",
                    type = KeyType.ACTION,
                    onClick = { triggerFeedback(); onPercent() },
                    testTag = "key_percent",
                    animationIntensity = animationIntensity,
                    componentSizeStyle = componentSizeStyle,
                    shapeStructureStyle = shapeStructureStyle,
                    modifier = Modifier.weight(1f)
                )
                LiquidGlassKey(
                    text = "÷",
                    type = KeyType.OPERATOR,
                    onClick = { triggerFeedback(); onOperator("÷") },
                    testTag = "key_divide",
                    animationIntensity = animationIntensity,
                    componentSizeStyle = componentSizeStyle,
                    shapeStructureStyle = shapeStructureStyle,
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 2: 7, 8, 9, ×
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                LiquidGlassKey(
                    text = "7",
                    type = KeyType.NUMBER,
                    onClick = { triggerFeedback(); onDigit("7") },
                    testTag = "key_7",
                    animationIntensity = animationIntensity,
                    componentSizeStyle = componentSizeStyle,
                    shapeStructureStyle = shapeStructureStyle,
                    modifier = Modifier.weight(1f)
                )
                LiquidGlassKey(
                    text = "8",
                    type = KeyType.NUMBER,
                    onClick = { triggerFeedback(); onDigit("8") },
                    testTag = "key_8",
                    animationIntensity = animationIntensity,
                    componentSizeStyle = componentSizeStyle,
                    shapeStructureStyle = shapeStructureStyle,
                    modifier = Modifier.weight(1f)
                )
                LiquidGlassKey(
                    text = "9",
                    type = KeyType.NUMBER,
                    onClick = { triggerFeedback(); onDigit("9") },
                    testTag = "key_9",
                    animationIntensity = animationIntensity,
                    componentSizeStyle = componentSizeStyle,
                    shapeStructureStyle = shapeStructureStyle,
                    modifier = Modifier.weight(1f)
                )
                LiquidGlassKey(
                    text = "×",
                    type = KeyType.OPERATOR,
                    onClick = { triggerFeedback(); onOperator("×") },
                    testTag = "key_multiply",
                    animationIntensity = animationIntensity,
                    componentSizeStyle = componentSizeStyle,
                    shapeStructureStyle = shapeStructureStyle,
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 3: 4, 5, 6, −
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                LiquidGlassKey(
                    text = "4",
                    type = KeyType.NUMBER,
                    onClick = { triggerFeedback(); onDigit("4") },
                    testTag = "key_4",
                    animationIntensity = animationIntensity,
                    componentSizeStyle = componentSizeStyle,
                    shapeStructureStyle = shapeStructureStyle,
                    modifier = Modifier.weight(1f)
                )
                LiquidGlassKey(
                    text = "5",
                    type = KeyType.NUMBER,
                    onClick = { triggerFeedback(); onDigit("5") },
                    testTag = "key_5",
                    animationIntensity = animationIntensity,
                    componentSizeStyle = componentSizeStyle,
                    shapeStructureStyle = shapeStructureStyle,
                    modifier = Modifier.weight(1f)
                )
                LiquidGlassKey(
                    text = "6",
                    type = KeyType.NUMBER,
                    onClick = { triggerFeedback(); onDigit("6") },
                    testTag = "key_6",
                    animationIntensity = animationIntensity,
                    componentSizeStyle = componentSizeStyle,
                    shapeStructureStyle = shapeStructureStyle,
                    modifier = Modifier.weight(1f)
                )
                LiquidGlassKey(
                    text = "−",
                    type = KeyType.OPERATOR,
                    onClick = { triggerFeedback(); onOperator("−") },
                    testTag = "key_subtract",
                    animationIntensity = animationIntensity,
                    componentSizeStyle = componentSizeStyle,
                    shapeStructureStyle = shapeStructureStyle,
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 4: 1, 2, 3, +
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                LiquidGlassKey(
                    text = "1",
                    type = KeyType.NUMBER,
                    onClick = { triggerFeedback(); onDigit("1") },
                    testTag = "key_1",
                    animationIntensity = animationIntensity,
                    componentSizeStyle = componentSizeStyle,
                    shapeStructureStyle = shapeStructureStyle,
                    modifier = Modifier.weight(1f)
                )
                LiquidGlassKey(
                    text = "2",
                    type = KeyType.NUMBER,
                    onClick = { triggerFeedback(); onDigit("2") },
                    testTag = "key_2",
                    animationIntensity = animationIntensity,
                    componentSizeStyle = componentSizeStyle,
                    shapeStructureStyle = shapeStructureStyle,
                    modifier = Modifier.weight(1f)
                )
                LiquidGlassKey(
                    text = "3",
                    type = KeyType.NUMBER,
                    onClick = { triggerFeedback(); onDigit("3") },
                    testTag = "key_3",
                    animationIntensity = animationIntensity,
                    componentSizeStyle = componentSizeStyle,
                    shapeStructureStyle = shapeStructureStyle,
                    modifier = Modifier.weight(1f)
                )
                LiquidGlassKey(
                    text = "+",
                    type = KeyType.OPERATOR,
                    onClick = { triggerFeedback(); onOperator("+") },
                    testTag = "key_add",
                    animationIntensity = animationIntensity,
                    componentSizeStyle = componentSizeStyle,
                    shapeStructureStyle = shapeStructureStyle,
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 5: 0, 00 / +/-, ., =
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                LiquidGlassKey(
                    text = "0",
                    type = KeyType.NUMBER,
                    onClick = { triggerFeedback(); onDigit("0") },
                    testTag = "key_0",
                    animationIntensity = animationIntensity,
                    componentSizeStyle = componentSizeStyle,
                    shapeStructureStyle = shapeStructureStyle,
                    modifier = Modifier.weight(1f)
                )
                if (showDoubleZeroKey) {
                    LiquidGlassKey(
                        text = "00",
                        type = KeyType.DOUBLE_ZERO,
                        onClick = { triggerFeedback(); onDoubleZero() },
                        testTag = "key_double_zero",
                        animationIntensity = animationIntensity,
                        componentSizeStyle = componentSizeStyle,
                        shapeStructureStyle = shapeStructureStyle,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    LiquidGlassKey(
                        text = "+/-",
                        type = KeyType.ACTION,
                        onClick = { triggerFeedback(); onNegate() },
                        testTag = "key_negate",
                        animationIntensity = animationIntensity,
                        componentSizeStyle = componentSizeStyle,
                        shapeStructureStyle = shapeStructureStyle,
                        modifier = Modifier.weight(1f)
                    )
                }
                LiquidGlassKey(
                    text = ".",
                    type = KeyType.NUMBER,
                    onClick = { triggerFeedback(); onDigit(".") },
                    testTag = "key_decimal",
                    animationIntensity = animationIntensity,
                    componentSizeStyle = componentSizeStyle,
                    shapeStructureStyle = shapeStructureStyle,
                    modifier = Modifier.weight(1f)
                )
                LiquidGlassKey(
                    text = "=",
                    type = KeyType.EQUALS,
                    onClick = { triggerFeedback(); onEquals() },
                    testTag = "key_equals",
                    animationIntensity = animationIntensity,
                    componentSizeStyle = componentSizeStyle,
                    shapeStructureStyle = shapeStructureStyle,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun LiquidGlassKey(
    text: String? = null,
    icon: ImageVector? = null,
    iconContentDescription: String? = null,
    type: KeyType,
    onClick: () -> Unit,
    testTag: String,
    animationIntensity: AnimationIntensity,
    componentSizeStyle: ComponentSizeStyle = ComponentSizeStyle.STANDARD,
    shapeStructureStyle: ShapeStructureStyle = ShapeStructureStyle.ROUNDED,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.2f
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scope = rememberCoroutineScope()
    val bounceAnim = remember { Animatable(1f) }

    val hasAnim = animationIntensity != AnimationIntensity.OFF
    val targetScale = if (isPressed && hasAnim) animationIntensity.scaleFactor else 1f
    val targetTransY = if (isPressed && hasAnim) animationIntensity.translationFactor else 0f

    // Fluid responsive press scale & translation physics
    val pressScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = when (animationIntensity) {
                AnimationIntensity.EXTREME_BOUNCE -> Spring.StiffnessLow
                AnimationIntensity.HIGH_SPRING -> Spring.StiffnessMedium
                else -> Spring.StiffnessMedium
            }
        ),
        label = "glass_press_scale"
    )

    val translationY by animateFloatAsState(
        targetValue = targetTransY,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "glass_press_translation"
    )

    val primaryColor = MaterialTheme.colorScheme.primary

    val (glassBg, contentColor, glassBorder) = when (type) {
        KeyType.NUMBER -> {
            val bg = if (isDark) {
                if (isPressed) PureDarkGlassSurfacePressed else PureDarkGlassSurface
            } else {
                if (isPressed) PureLightGlassSurfacePressed else PureLightGlassSurface
            }
            val textCol = if (isDark) PureDarkTextPrimary else PureLightTextPrimary
            val border = if (isDark) {
                if (isPressed) primaryColor.copy(alpha = 0.6f) else PureDarkGlassBorder
            } else {
                if (isPressed) primaryColor.copy(alpha = 0.5f) else PureLightGlassBorder
            }
            Triple(bg, textCol, border)
        }
        KeyType.DOUBLE_ZERO -> {
            val bg = if (isDark) {
                if (isPressed) primaryColor.copy(alpha = 0.40f) else primaryColor.copy(alpha = 0.15f)
            } else {
                if (isPressed) primaryColor.copy(alpha = 0.30f) else primaryColor.copy(alpha = 0.12f)
            }
            val textCol = primaryColor
            val border = primaryColor.copy(alpha = if (isPressed) 0.9f else 0.45f)
            Triple(bg, textCol, border)
        }
        KeyType.OPERATOR -> {
            val bg = if (isDark) {
                if (isPressed) primaryColor.copy(alpha = 0.50f) else primaryColor.copy(alpha = 0.25f)
            } else {
                if (isPressed) primaryColor.copy(alpha = 0.35f) else primaryColor.copy(alpha = 0.18f)
            }
            val textCol = primaryColor
            val border = primaryColor.copy(alpha = if (isPressed) 0.9f else 0.5f)
            Triple(bg, textCol, border)
        }
        KeyType.EQUALS -> {
            val bg = if (isPressed) primaryColor.copy(alpha = 0.85f) else primaryColor
            val textCol = if (isDark) Color.Black else Color.White
            val border = if (isDark) Color(0x80FFFFFF) else Color(0x33000000)
            Triple(bg, textCol, border)
        }
        KeyType.ACTION -> {
            val bg = if (isDark) {
                if (isPressed) Color(0x44FFFFFF) else Color(0x24FFFFFF)
            } else {
                if (isPressed) Color(0x77CBD5E1) else Color(0x4DCBD5E1)
            }
            val textCol = if (isDark) PureDarkTextPrimary else PureLightTextPrimary
            val border = if (isDark) {
                if (isPressed) primaryColor.copy(alpha = 0.5f) else PureDarkGlassBorder
            } else {
                if (isPressed) primaryColor.copy(alpha = 0.4f) else PureLightGlassBorder
            }
            Triple(bg, textCol, border)
        }
    }

    val shape = shapeStructureStyle.toButtonShape()
    val borderWidth = if (shapeStructureStyle == ShapeStructureStyle.LINE_BORDER) 1.5.dp else 1.dp
    val fontScale = componentSizeStyle.keyFontScale

    Box(
        modifier = modifier
            .graphicsLayer {
                val combinedScale = pressScale * bounceAnim.value
                scaleX = combinedScale
                scaleY = combinedScale
                this.translationY = translationY.dp.toPx()
            }
            .aspectRatio(1.15f)
            .clip(shape)
            .background(glassBg)
            .border(
                width = borderWidth,
                color = glassBorder,
                shape = shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(
                    bounded = true,
                    color = if (type == KeyType.EQUALS) Color.White else primaryColor
                ),
                onClick = {
                    if (hasAnim) {
                        scope.launch {
                            val snapVal = if (animationIntensity == AnimationIntensity.EXTREME_BOUNCE) 0.70f else 0.88f
                            bounceAnim.snapTo(snapVal)
                            bounceAnim.animateTo(
                                targetValue = 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            )
                        }
                    }
                    onClick()
                }
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = iconContentDescription,
                tint = contentColor,
                modifier = Modifier.size(componentSizeStyle.iconSize)
            )
        } else if (text != null) {
            val baseFontSize = when {
                type == KeyType.EQUALS || type == KeyType.OPERATOR -> 28f
                type == KeyType.DOUBLE_ZERO -> 22f
                text.length > 2 -> 16f
                else -> 24f
            }

            Text(
                text = text,
                color = contentColor,
                fontSize = (baseFontSize * fontScale).sp,
                fontWeight = if (type == KeyType.EQUALS || type == KeyType.OPERATOR || type == KeyType.DOUBLE_ZERO) FontWeight.Bold else FontWeight.Medium,
                fontFamily = FontFamily.SansSerif
            )
        }
    }
}

/**
 * Universal, high-efficiency Smart Station Keypad for dedicated inputs (Converter, Sales, Mixture, Batch, etc.)
 * Provides haptic/audio feedback, physics animations, double-zero key, and dismiss actions.
 */
@Composable
fun SmartStationKeypad(
    activeFieldLabel: String? = null,
    onDigit: (String) -> Unit,
    onDoubleZero: (() -> Unit)? = null,
    onDecimal: (() -> Unit)? = null,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onNegate: (() -> Unit)? = null,
    onDone: (() -> Unit)? = null,
    showDoubleZeroKey: Boolean = true,
    showNegateKey: Boolean = false,
    showDecimalKey: Boolean = true,
    animationIntensity: AnimationIntensity = AnimationIntensity.BALANCED,
    hapticIntensity: HapticIntensity = HapticIntensity.MEDIUM,
    soundIntensity: SoundIntensity = SoundIntensity.STANDARD,
    componentSizeStyle: ComponentSizeStyle = ComponentSizeStyle.STANDARD,
    shapeStructureStyle: ShapeStructureStyle = ShapeStructureStyle.ROUNDED,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val isDark = MaterialTheme.colorScheme.background.red < 0.2f
    val primaryColor = MaterialTheme.colorScheme.primary

    val triggerFeedback = {
        HapticFeedbackHelper.performKeypadTap(context, view, hapticIntensity)
        SoundFeedbackHelper.playClickSound(context, view, soundIntensity)
    }

    val spacing = 8.dp
    val cardShape = shapeStructureStyle.toCardShape()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .border(
                1.dp,
                if (isDark) Color(0x22FFFFFF) else Color(0x18000000),
                cardShape
            )
            .testTag("smart_station_keypad"),
        shape = cardShape,
        color = if (isDark) Color(0xFF131C2E) else Color(0xFFFFFFFF),
        shadowElevation = if (isDark) 0.dp else 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(spacing)
        ) {
            // Active Field Header & Hide Keypad Button
            if (activeFieldLabel != null || onDone != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (activeFieldLabel != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = primaryColor.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "⌨️ $activeFieldLabel",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    if (onDone != null) {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    triggerFeedback()
                                    onDone()
                                }
                                .testTag("smart_keypad_done_button"),
                            shape = RoundedCornerShape(8.dp),
                            color = if (isDark) Color(0x20FFFFFF) else Color(0x15000000)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Done",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryColor
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.KeyboardHide,
                                    contentDescription = "Hide Keypad",
                                    tint = primaryColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Row 1: 7, 8, 9, ⌫
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                LiquidGlassKey(
                    text = "7",
                    type = KeyType.NUMBER,
                    onClick = { triggerFeedback(); onDigit("7") },
                    testTag = "key_7",
                    animationIntensity = animationIntensity,
                    componentSizeStyle = componentSizeStyle,
                    shapeStructureStyle = shapeStructureStyle,
                    modifier = Modifier.weight(1f)
                )
                LiquidGlassKey(
                    text = "8",
                    type = KeyType.NUMBER,
                    onClick = { triggerFeedback(); onDigit("8") },
                    testTag = "key_8",
                    animationIntensity = animationIntensity,
                    componentSizeStyle = componentSizeStyle,
                    shapeStructureStyle = shapeStructureStyle,
                    modifier = Modifier.weight(1f)
                )
                LiquidGlassKey(
                    text = "9",
                    type = KeyType.NUMBER,
                    onClick = { triggerFeedback(); onDigit("9") },
                    testTag = "key_9",
                    animationIntensity = animationIntensity,
                    componentSizeStyle = componentSizeStyle,
                    shapeStructureStyle = shapeStructureStyle,
                    modifier = Modifier.weight(1f)
                )
                LiquidGlassKey(
                    icon = Icons.AutoMirrored.Filled.Backspace,
                    iconContentDescription = "Backspace",
                    type = KeyType.ACTION,
                    onClick = { triggerFeedback(); onBackspace() },
                    testTag = "key_backspace",
                    animationIntensity = animationIntensity,
                    componentSizeStyle = componentSizeStyle,
                    shapeStructureStyle = shapeStructureStyle,
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 2: 4, 5, 6, C (Clear)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                LiquidGlassKey(
                    text = "4",
                    type = KeyType.NUMBER,
                    onClick = { triggerFeedback(); onDigit("4") },
                    testTag = "key_4",
                    animationIntensity = animationIntensity,
                    componentSizeStyle = componentSizeStyle,
                    shapeStructureStyle = shapeStructureStyle,
                    modifier = Modifier.weight(1f)
                )
                LiquidGlassKey(
                    text = "5",
                    type = KeyType.NUMBER,
                    onClick = { triggerFeedback(); onDigit("5") },
                    testTag = "key_5",
                    animationIntensity = animationIntensity,
                    componentSizeStyle = componentSizeStyle,
                    shapeStructureStyle = shapeStructureStyle,
                    modifier = Modifier.weight(1f)
                )
                LiquidGlassKey(
                    text = "6",
                    type = KeyType.NUMBER,
                    onClick = { triggerFeedback(); onDigit("6") },
                    testTag = "key_6",
                    animationIntensity = animationIntensity,
                    componentSizeStyle = componentSizeStyle,
                    shapeStructureStyle = shapeStructureStyle,
                    modifier = Modifier.weight(1f)
                )
                LiquidGlassKey(
                    text = "C",
                    type = KeyType.ACTION,
                    onClick = { triggerFeedback(); onClear() },
                    testTag = "key_clear",
                    animationIntensity = animationIntensity,
                    componentSizeStyle = componentSizeStyle,
                    shapeStructureStyle = shapeStructureStyle,
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 3: 1, 2, 3, ± (or AC)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                LiquidGlassKey(
                    text = "1",
                    type = KeyType.NUMBER,
                    onClick = { triggerFeedback(); onDigit("1") },
                    testTag = "key_1",
                    animationIntensity = animationIntensity,
                    componentSizeStyle = componentSizeStyle,
                    shapeStructureStyle = shapeStructureStyle,
                    modifier = Modifier.weight(1f)
                )
                LiquidGlassKey(
                    text = "2",
                    type = KeyType.NUMBER,
                    onClick = { triggerFeedback(); onDigit("2") },
                    testTag = "key_2",
                    animationIntensity = animationIntensity,
                    componentSizeStyle = componentSizeStyle,
                    shapeStructureStyle = shapeStructureStyle,
                    modifier = Modifier.weight(1f)
                )
                LiquidGlassKey(
                    text = "3",
                    type = KeyType.NUMBER,
                    onClick = { triggerFeedback(); onDigit("3") },
                    testTag = "key_3",
                    animationIntensity = animationIntensity,
                    componentSizeStyle = componentSizeStyle,
                    shapeStructureStyle = shapeStructureStyle,
                    modifier = Modifier.weight(1f)
                )
                if (showNegateKey && onNegate != null) {
                    LiquidGlassKey(
                        text = "±",
                        type = KeyType.ACTION,
                        onClick = { triggerFeedback(); onNegate() },
                        testTag = "key_negate",
                        animationIntensity = animationIntensity,
                        componentSizeStyle = componentSizeStyle,
                        shapeStructureStyle = shapeStructureStyle,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    LiquidGlassKey(
                        text = "AC",
                        type = KeyType.ACTION,
                        onClick = { triggerFeedback(); onClear() },
                        testTag = "key_all_clear",
                        animationIntensity = animationIntensity,
                        componentSizeStyle = componentSizeStyle,
                        shapeStructureStyle = shapeStructureStyle,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Row 4: 0, 00, ., ✓ (Done/Hide)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                LiquidGlassKey(
                    text = "0",
                    type = KeyType.NUMBER,
                    onClick = { triggerFeedback(); onDigit("0") },
                    testTag = "key_0",
                    animationIntensity = animationIntensity,
                    componentSizeStyle = componentSizeStyle,
                    shapeStructureStyle = shapeStructureStyle,
                    modifier = Modifier.weight(1f)
                )
                if (showDoubleZeroKey && onDoubleZero != null) {
                    LiquidGlassKey(
                        text = "00",
                        type = KeyType.DOUBLE_ZERO,
                        onClick = { triggerFeedback(); onDoubleZero() },
                        testTag = "key_double_zero",
                        animationIntensity = animationIntensity,
                        componentSizeStyle = componentSizeStyle,
                        shapeStructureStyle = shapeStructureStyle,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    LiquidGlassKey(
                        text = "000",
                        type = KeyType.DOUBLE_ZERO,
                        onClick = { triggerFeedback(); onDigit("000") },
                        testTag = "key_triple_zero",
                        animationIntensity = animationIntensity,
                        componentSizeStyle = componentSizeStyle,
                        shapeStructureStyle = shapeStructureStyle,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (showDecimalKey && onDecimal != null) {
                    LiquidGlassKey(
                        text = ".",
                        type = KeyType.NUMBER,
                        onClick = { triggerFeedback(); onDecimal() },
                        testTag = "key_decimal",
                        animationIntensity = animationIntensity,
                        componentSizeStyle = componentSizeStyle,
                        shapeStructureStyle = shapeStructureStyle,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    LiquidGlassKey(
                        text = "",
                        type = KeyType.NUMBER,
                        onClick = {},
                        testTag = "key_empty",
                        animationIntensity = animationIntensity,
                        componentSizeStyle = componentSizeStyle,
                        shapeStructureStyle = shapeStructureStyle,
                        modifier = Modifier.weight(1f)
                    )
                }

                LiquidGlassKey(
                    icon = Icons.Default.Check,
                    iconContentDescription = "Done",
                    type = KeyType.EQUALS,
                    onClick = {
                        triggerFeedback()
                        onDone?.invoke()
                    },
                    testTag = "key_done",
                    animationIntensity = animationIntensity,
                    componentSizeStyle = componentSizeStyle,
                    shapeStructureStyle = shapeStructureStyle,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

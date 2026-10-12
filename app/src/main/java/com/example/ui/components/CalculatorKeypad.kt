package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardHide
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
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
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.geometry.Offset
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
import androidx.compose.ui.graphics.luminance
import com.example.ui.theme.LocalAppThemeSkin
import com.example.ui.theme.AppThemeSkin
import com.example.ui.theme.AnimationIntensity
import com.example.ui.theme.ComponentSizeStyle
import com.example.ui.theme.HapticIntensity
import com.example.ui.theme.LocalAnimationIntensity
import com.example.ui.theme.LocalComponentSizing
import com.example.ui.theme.LocalShapeStructure
import com.example.ui.theme.DigitalMonospaceFont
import com.example.ui.theme.LocalAppFontStyle
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import android.widget.Toast
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

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val currentSkin = LocalAppThemeSkin.current
    val spacing = componentSizeStyle.keypadSpacing
    val padH = componentSizeStyle.keypadPaddingH
    val padV = componentSizeStyle.keypadPaddingV

    // Clean keypad container
    Box(
        modifier = modifier
            .fillMaxWidth()
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
    animationIntensity: AnimationIntensity = AnimationIntensity.OFF,
    componentSizeStyle: ComponentSizeStyle = ComponentSizeStyle.STANDARD,
    shapeStructureStyle: ShapeStructureStyle = ShapeStructureStyle.ROUNDED,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val currentSkin = LocalAppThemeSkin.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val isCharcoalTheme = currentSkin == AppThemeSkin.DARK_CHARCOAL
    val isPureLightTheme = currentSkin == AppThemeSkin.PURE_LIGHT

    val isSkeuoDark = currentSkin == AppThemeSkin.SKEUO_BAKELITE
    val isSkeuoLight = currentSkin == AppThemeSkin.SKEUO_IVORY

    val (keyBg, contentColor, borderColor) = when {
        isSkeuoDark -> when (type) {
            KeyType.NUMBER, KeyType.DOUBLE_ZERO ->
                Triple(if (isPressed) Color(0xFF3A322C) else Color(0xFF4A4038), Color(0xFFF2E8D5), Color.Transparent)
            KeyType.ACTION ->
                Triple(if (isPressed) Color(0xFF211C19) else Color(0xFF2B2521), Color(0xFFD9C7A3), Color.Transparent)
            KeyType.OPERATOR ->
                Triple(if (isPressed) Color(0xFFB38235) else Color(0xFFC8963E), Color(0xFF2A1D0A), Color.Transparent)
            KeyType.EQUALS ->
                Triple(if (isPressed) Color(0xFFB8471F) else Color(0xFFD4552A), Color(0xFFFFFFFF), Color.Transparent)
        }
        isSkeuoLight -> when (type) {
            KeyType.NUMBER, KeyType.DOUBLE_ZERO ->
                Triple(if (isPressed) Color(0xFFE8E1D0) else Color(0xFFF6F1E4), Color(0xFF2B2620), Color.Transparent)
            KeyType.ACTION ->
                Triple(if (isPressed) Color(0xFFB8B09E) else Color(0xFFC7BFAE), Color(0xFF2B2620), Color.Transparent)
            KeyType.OPERATOR ->
                Triple(if (isPressed) Color(0xFF2C2924) else Color(0xFF3B3731), Color(0xFFF6F1E4), Color.Transparent)
            KeyType.EQUALS ->
                Triple(if (isPressed) Color(0xFFC9561A) else Color(0xFFE0651F), Color(0xFFFFFFFF), Color.Transparent)
        }
        isCharcoalTheme -> when (type) {
            KeyType.NUMBER, KeyType.DOUBLE_ZERO -> {
                val bg = if (isPressed) Color(0xFF1C1C1F) else Color(0xFF060606)
                val textCol = Color(0xFFFFFFFF)
                val border = Color(0x38FFFFFF)
                Triple(bg, textCol, border)
            }
            KeyType.ACTION -> {
                val bg = if (isPressed) Color(0xFF2E2E32) else Color(0xFF141416)
                val textCol = Color(0xFFE4E4E7)
                val border = Color(0x4DFFFFFF)
                Triple(bg, textCol, border)
            }
            KeyType.OPERATOR -> {
                val bg = if (isPressed) Color(0xFFE08900) else Color(0xFFFF9F0A)
                val textCol = Color(0xFFFFFFFF)
                val border = Color(0x66FFFFFF)
                Triple(bg, textCol, border)
            }
            KeyType.EQUALS -> {
                val bg = if (isPressed) Color(0xFFE08900) else Color(0xFFFF9F0A)
                val textCol = Color(0xFFFFFFFF)
                val border = Color(0x80FFFFFF)
                Triple(bg, textCol, border)
            }
        }
        isPureLightTheme || !isDark -> when (type) {
            KeyType.NUMBER, KeyType.DOUBLE_ZERO -> {
                val bg = if (isPressed) Color(0xFFE5E5EA) else Color(0xFFFFFFFF)
                val textCol = Color(0xFF1C1C1E)
                val border = Color(0x22000000)
                Triple(bg, textCol, border)
            }
            KeyType.ACTION -> {
                val bg = if (isPressed) Color(0xFFD1D1D6) else Color(0xFFE5E5EA)
                val textCol = Color(0xFF1C1C1E)
                val border = Color(0x1F000000)
                Triple(bg, textCol, border)
            }
            KeyType.OPERATOR -> {
                val bg = if (isPressed) Color(0xFFE08900) else Color(0xFFFF9F0A)
                val textCol = Color(0xFFFFFFFF)
                val border = Color(0x26000000)
                Triple(bg, textCol, border)
            }
            KeyType.EQUALS -> {
                val bg = if (isPressed) Color(0xFFE08900) else Color(0xFFFF9F0A)
                val textCol = Color(0xFFFFFFFF)
                val border = Color(0x33000000)
                Triple(bg, textCol, border)
            }
        }
        else -> when (type) {
            KeyType.NUMBER, KeyType.DOUBLE_ZERO -> {
                val bg = if (isPressed) currentSkin.surfacePressed else currentSkin.surfaceVariant
                val textCol = currentSkin.textPrimary
                val border = if (isPressed) primaryColor.copy(alpha = 0.65f) else currentSkin.outline
                Triple(bg, textCol, border)
            }
            KeyType.ACTION -> {
                val bg = if (isDark) {
                    if (isPressed) Color(0x40FFFFFF) else Color(0x26FFFFFF)
                } else {
                    if (isPressed) Color(0x30000000) else Color(0x18000000)
                }
                val textCol = currentSkin.textPrimary
                val border = currentSkin.outline
                Triple(bg, textCol, border)
            }
            KeyType.OPERATOR -> {
                val bg = if (isPressed) primaryColor.copy(alpha = 0.85f) else primaryColor
                val textCol = onPrimaryColor
                val border = if (isDark) Color(0x4DFFFFFF) else Color(0x26000000)
                Triple(bg, textCol, border)
            }
            KeyType.EQUALS -> {
                val bg = if (isPressed) primaryColor.copy(alpha = 0.85f) else primaryColor
                val textCol = onPrimaryColor
                val border = if (isDark) Color(0x80FFFFFF) else Color(0x33000000)
                Triple(bg, textCol, border)
            }
        }
    }

    val shape = shapeStructureStyle.toButtonShape()
    val fontScale = componentSizeStyle.keyFontScale

    // Engraved legend: a 1px counter-light under the glyph makes it look cut into the cap
    val legendShadow = Shadow(
        color = if (contentColor.luminance() > 0.5f) Color.Black.copy(alpha = 0.55f) else Color.White.copy(alpha = 0.65f),
        offset = Offset(0f, 1.5f),
        blurRadius = 1f
    )

    Box(
        modifier = modifier
            .aspectRatio(1.1f)
            .graphicsLayer { translationY = if (isPressed) 2.dp.toPx() else 0f }
            .skeuoRaised(
                shape = shape,
                base = keyBg.compositeOver(currentSkin.surface),
                isDark = isDark,
                elevation = 7.dp,
                pressed = isPressed
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
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
                else -> 25f
            }

            Text(
                text = text,
                color = contentColor,
                fontSize = (baseFontSize * fontScale).sp,
                fontWeight = if (type == KeyType.EQUALS || type == KeyType.OPERATOR || type == KeyType.ACTION) FontWeight.SemiBold else FontWeight.Medium,
                fontFamily = DigitalMonospaceFont,
                fontStyle = LocalAppFontStyle.current.defaultFontStyle,
                style = androidx.compose.ui.text.TextStyle(shadow = legendShadow)
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
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val currentSkin = LocalAppThemeSkin.current
    val primaryColor = MaterialTheme.colorScheme.primary

    val triggerFeedback = {
        HapticFeedbackHelper.performKeypadTap(context, view, hapticIntensity)
        SoundFeedbackHelper.playClickSound(context, view, soundIntensity)
    }

    val spacing = 8.dp
    val cardShape = shapeStructureStyle.toCardShape()
    val actualOnDoubleZero = onDoubleZero ?: { onDigit("00") }
    val actualOnDecimal = onDecimal ?: { onDigit(".") }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .skeuoRaised(
                shape = cardShape,
                base = currentSkin.surface.compositeOver(currentSkin.background),
                isDark = isDark,
                elevation = 8.dp
            )
            .testTag("smart_station_keypad"),
        shape = cardShape,
        color = Color.Transparent,
        shadowElevation = 0.dp
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
                            modifier = Modifier.skeuoInset(RoundedCornerShape(8.dp), currentSkin.surface, isDark, accent = primaryColor),
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Transparent
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
                                .skeuoRaised(RoundedCornerShape(8.dp), currentSkin.plate(), isDark, elevation = 3.dp)
                                .clickable {
                                    triggerFeedback()
                                    onDone()
                                }
                                .testTag("smart_keypad_done_button"),
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Transparent
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
                if (showDoubleZeroKey) {
                    LiquidGlassKey(
                        text = "00",
                        type = KeyType.DOUBLE_ZERO,
                        onClick = { triggerFeedback(); actualOnDoubleZero() },
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

                if (showDecimalKey) {
                    LiquidGlassKey(
                        text = ".",
                        type = KeyType.NUMBER,
                        onClick = { triggerFeedback(); actualOnDecimal() },
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

/**
 * Reusable Long-Press Context Menu for Copy and Paste across all numeric input fields.
 */
@Composable
fun InputFieldCopyPasteMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    value: String,
    onPaste: (String) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest
    ) {
        if (value.isNotEmpty()) {
            DropdownMenuItem(
                text = { Text("Copy", fontSize = 13.sp, fontWeight = FontWeight.Medium) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        modifier = Modifier.size(18.dp)
                    )
                },
                onClick = {
                    clipboardManager.setText(AnnotatedString(value))
                    Toast.makeText(context, "Copied $value", Toast.LENGTH_SHORT).show()
                    onDismissRequest()
                }
            )
        }
        DropdownMenuItem(
            text = { Text("Paste", fontSize = 13.sp, fontWeight = FontWeight.Medium) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.ContentPaste,
                    contentDescription = "Paste",
                    modifier = Modifier.size(18.dp)
                )
            },
            onClick = {
                val clip = clipboardManager.getText()?.text
                if (!clip.isNullOrBlank()) {
                    val sanitized = clip.trim().filter { it.isDigit() || it == '.' || it == '-' }
                    if (sanitized.isNotEmpty()) {
                        onPaste(sanitized)
                        Toast.makeText(context, "Pasted $sanitized", Toast.LENGTH_SHORT).show()
                    } else {
                        onPaste(clip.trim())
                    }
                }
                onDismissRequest()
            }
        )
    }
}

/**
 * Animated Blinking Cursor Pointer for active numeric input fields.
 * Clearly shows the user which field has active focus and pointer for key entry.
 */
@Composable
fun BlinkingCursorPointer(
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
    cursorHeight: androidx.compose.ui.unit.Dp = 18.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cursor_blink")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 500,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor_alpha"
    )
    Box(
        modifier = modifier
            .width(2.dp)
            .height(cursorHeight)
            .graphicsLayer { this.alpha = alpha }
            .background(color, RoundedCornerShape(1.dp))
    )
}


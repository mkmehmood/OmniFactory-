package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.ui.theme.PureDarkGlassBorder
import com.example.ui.theme.PureDarkGlassSurface
import com.example.ui.theme.PureDarkGlassSurfacePressed
import com.example.ui.theme.PureDarkTextPrimary
import com.example.ui.theme.PureLightGlassBorder
import com.example.ui.theme.PureLightGlassSurface
import com.example.ui.theme.PureLightGlassSurfacePressed
import com.example.ui.theme.PureLightTextPrimary
import com.example.ui.theme.ShapeStructureStyle
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
    enableAnimations: Boolean = true,
    hapticEnabled: Boolean = true,
    shapeStructureStyle: ShapeStructureStyle = ShapeStructureStyle.ROUNDED,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current

    val triggerHaptic = {
        HapticFeedbackHelper.performKeypadTap(context, view, hapticEnabled)
    }

    val isDark = MaterialTheme.colorScheme.background.red < 0.2f

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
            .padding(horizontal = 18.dp, vertical = 20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Row 1: AC/C, ⌫ (Delete/Backspace SVG Icon), %, ÷
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LiquidGlassKey(
                    text = if (isAllClear) "AC" else "C",
                    type = KeyType.ACTION,
                    onClick = { triggerHaptic(); onClear() },
                    testTag = "key_clear",
                    enableAnimations = enableAnimations,
                    shapeStructureStyle = shapeStructureStyle,
                    modifier = Modifier.weight(1f)
                )
                LiquidGlassKey(
                    icon = Icons.AutoMirrored.Filled.Backspace,
                    iconContentDescription = "Delete",
                    type = KeyType.ACTION,
                    onClick = { triggerHaptic(); onBackspace() },
                    testTag = "key_backspace",
                    enableAnimations = enableAnimations,
                    shapeStructureStyle = shapeStructureStyle,
                    modifier = Modifier.weight(1f)
                )
                LiquidGlassKey(
                    icon = Icons.Default.Percent,
                    iconContentDescription = "Percent",
                    type = KeyType.ACTION,
                    onClick = { triggerHaptic(); onPercent() },
                    testTag = "key_percent",
                    enableAnimations = enableAnimations,
                    shapeStructureStyle = shapeStructureStyle,
                    modifier = Modifier.weight(1f)
                )
                LiquidGlassKey(
                    text = "÷",
                    type = KeyType.OPERATOR,
                    onClick = { triggerHaptic(); onOperator("÷") },
                    testTag = "key_divide",
                    enableAnimations = enableAnimations,
                    shapeStructureStyle = shapeStructureStyle,
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 2: 7, 8, 9, ×
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LiquidGlassKey(
                    text = "7",
                    type = KeyType.NUMBER,
                    onClick = { triggerHaptic(); onDigit("7") },
                    testTag = "key_7",
                    enableAnimations = enableAnimations,
                    shapeStructureStyle = shapeStructureStyle,
                    modifier = Modifier.weight(1f)
                )
                LiquidGlassKey(
                    text = "8",
                    type = KeyType.NUMBER,
                    onClick = { triggerHaptic(); onDigit("8") },
                    testTag = "key_8",
                    enableAnimations = enableAnimations,
                    shapeStructureStyle = shapeStructureStyle,
                    modifier = Modifier.weight(1f)
                )
                LiquidGlassKey(
                    text = "9",
                    type = KeyType.NUMBER,
                    onClick = { triggerHaptic(); onDigit("9") },
                    testTag = "key_9",
                    enableAnimations = enableAnimations,
                    shapeStructureStyle = shapeStructureStyle,
                    modifier = Modifier.weight(1f)
                )
                LiquidGlassKey(
                    text = "×",
                    type = KeyType.OPERATOR,
                    onClick = { triggerHaptic(); onOperator("×") },
                    testTag = "key_multiply",
                    enableAnimations = enableAnimations,
                    shapeStructureStyle = shapeStructureStyle,
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 3: 4, 5, 6, −
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LiquidGlassKey(
                    text = "4",
                    type = KeyType.NUMBER,
                    onClick = { triggerHaptic(); onDigit("4") },
                    testTag = "key_4",
                    enableAnimations = enableAnimations,
                    shapeStructureStyle = shapeStructureStyle,
                    modifier = Modifier.weight(1f)
                )
                LiquidGlassKey(
                    text = "5",
                    type = KeyType.NUMBER,
                    onClick = { triggerHaptic(); onDigit("5") },
                    testTag = "key_5",
                    enableAnimations = enableAnimations,
                    shapeStructureStyle = shapeStructureStyle,
                    modifier = Modifier.weight(1f)
                )
                LiquidGlassKey(
                    text = "6",
                    type = KeyType.NUMBER,
                    onClick = { triggerHaptic(); onDigit("6") },
                    testTag = "key_6",
                    enableAnimations = enableAnimations,
                    shapeStructureStyle = shapeStructureStyle,
                    modifier = Modifier.weight(1f)
                )
                LiquidGlassKey(
                    text = "−",
                    type = KeyType.OPERATOR,
                    onClick = { triggerHaptic(); onOperator("−") },
                    testTag = "key_subtract",
                    enableAnimations = enableAnimations,
                    shapeStructureStyle = shapeStructureStyle,
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 4: 1, 2, 3, +
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LiquidGlassKey(
                    text = "1",
                    type = KeyType.NUMBER,
                    onClick = { triggerHaptic(); onDigit("1") },
                    testTag = "key_1",
                    enableAnimations = enableAnimations,
                    shapeStructureStyle = shapeStructureStyle,
                    modifier = Modifier.weight(1f)
                )
                LiquidGlassKey(
                    text = "2",
                    type = KeyType.NUMBER,
                    onClick = { triggerHaptic(); onDigit("2") },
                    testTag = "key_2",
                    enableAnimations = enableAnimations,
                    shapeStructureStyle = shapeStructureStyle,
                    modifier = Modifier.weight(1f)
                )
                LiquidGlassKey(
                    text = "3",
                    type = KeyType.NUMBER,
                    onClick = { triggerHaptic(); onDigit("3") },
                    testTag = "key_3",
                    enableAnimations = enableAnimations,
                    shapeStructureStyle = shapeStructureStyle,
                    modifier = Modifier.weight(1f)
                )
                LiquidGlassKey(
                    text = "+",
                    type = KeyType.OPERATOR,
                    onClick = { triggerHaptic(); onOperator("+") },
                    testTag = "key_add",
                    enableAnimations = enableAnimations,
                    shapeStructureStyle = shapeStructureStyle,
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 5: 0, 00 / +/-, ., =
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LiquidGlassKey(
                    text = "0",
                    type = KeyType.NUMBER,
                    onClick = { triggerHaptic(); onDigit("0") },
                    testTag = "key_0",
                    enableAnimations = enableAnimations,
                    shapeStructureStyle = shapeStructureStyle,
                    modifier = Modifier.weight(1f)
                )
                if (showDoubleZeroKey) {
                    LiquidGlassKey(
                        text = "00",
                        type = KeyType.DOUBLE_ZERO,
                        onClick = { triggerHaptic(); onDoubleZero() },
                        testTag = "key_double_zero",
                        enableAnimations = enableAnimations,
                        shapeStructureStyle = shapeStructureStyle,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    LiquidGlassKey(
                        text = "+/-",
                        type = KeyType.ACTION,
                        onClick = { triggerHaptic(); onNegate() },
                        testTag = "key_negate",
                        enableAnimations = enableAnimations,
                        shapeStructureStyle = shapeStructureStyle,
                        modifier = Modifier.weight(1f)
                    )
                }
                LiquidGlassKey(
                    text = ".",
                    type = KeyType.NUMBER,
                    onClick = { triggerHaptic(); onDigit(".") },
                    testTag = "key_decimal",
                    enableAnimations = enableAnimations,
                    shapeStructureStyle = shapeStructureStyle,
                    modifier = Modifier.weight(1f)
                )
                LiquidGlassKey(
                    text = "=",
                    type = KeyType.EQUALS,
                    onClick = { triggerHaptic(); onEquals() },
                    testTag = "key_equals",
                    enableAnimations = enableAnimations,
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
    enableAnimations: Boolean,
    shapeStructureStyle: ShapeStructureStyle = ShapeStructureStyle.ROUNDED,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.2f
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scope = rememberCoroutineScope()
    val bounceAnim = remember { Animatable(1f) }

    // Fluid responsive press scale & translation physics
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed && enableAnimations) 0.88f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "glass_press_scale"
    )

    val translationY by animateFloatAsState(
        targetValue = if (isPressed && enableAnimations) 3.5f else 0f,
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
                    if (enableAnimations) {
                        scope.launch {
                            bounceAnim.snapTo(0.88f)
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
                modifier = Modifier.size(24.dp)
            )
        } else if (text != null) {
            Text(
                text = text,
                color = contentColor,
                fontSize = when {
                    type == KeyType.EQUALS || type == KeyType.OPERATOR -> 28.sp
                    type == KeyType.DOUBLE_ZERO -> 22.sp
                    text.length > 2 -> 16.sp
                    else -> 24.sp
                },
                fontWeight = if (type == KeyType.EQUALS || type == KeyType.OPERATOR || type == KeyType.DOUBLE_ZERO) FontWeight.Bold else FontWeight.Medium,
                fontFamily = FontFamily.SansSerif
            )
        }
    }
}

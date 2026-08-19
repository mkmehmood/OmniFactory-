package com.example.ui.components

import android.view.HapticFeedbackConstants
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
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PureDarkGlassBorder
import com.example.ui.theme.PureDarkGlassSurface
import com.example.ui.theme.PureDarkGlassSurfacePressed
import com.example.ui.theme.PureDarkTextPrimary
import com.example.ui.theme.PureLightGlassBorder
import com.example.ui.theme.PureLightGlassSurface
import com.example.ui.theme.PureLightGlassSurfacePressed
import com.example.ui.theme.PureLightTextPrimary

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
    modifier: Modifier = Modifier
) {
    val view = LocalView.current

    val performHaptic = {
        if (hapticEnabled) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }

    val isDark = MaterialTheme.colorScheme.background.red < 0.2f

    // Completely transparent frosted glass keypad container with no background shadow
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
                    onClick = { performHaptic(); onClear() },
                    testTag = "key_clear",
                    enableAnimations = enableAnimations,
                    modifier = Modifier.weight(1f)
                )
                LiquidGlassKey(
                    icon = Icons.AutoMirrored.Filled.Backspace,
                    iconContentDescription = "Delete",
                    type = KeyType.ACTION,
                    onClick = { performHaptic(); onBackspace() },
                    testTag = "key_backspace",
                    enableAnimations = enableAnimations,
                    modifier = Modifier.weight(1f)
                )
                LiquidGlassKey(
                    icon = Icons.Default.Percent,
                    iconContentDescription = "Percent",
                    type = KeyType.ACTION,
                    onClick = { performHaptic(); onPercent() },
                    testTag = "key_percent",
                    enableAnimations = enableAnimations,
                    modifier = Modifier.weight(1f)
                )
                LiquidGlassKey(
                    text = "÷",
                    type = KeyType.OPERATOR,
                    onClick = { performHaptic(); onOperator("÷") },
                    testTag = "key_divide",
                    enableAnimations = enableAnimations,
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
                    onClick = { performHaptic(); onDigit("7") },
                    testTag = "key_7",
                    enableAnimations = enableAnimations,
                    modifier = Modifier.weight(1f)
                )
                LiquidGlassKey(
                    text = "8",
                    type = KeyType.NUMBER,
                    onClick = { performHaptic(); onDigit("8") },
                    testTag = "key_8",
                    enableAnimations = enableAnimations,
                    modifier = Modifier.weight(1f)
                )
                LiquidGlassKey(
                    text = "9",
                    type = KeyType.NUMBER,
                    onClick = { performHaptic(); onDigit("9") },
                    testTag = "key_9",
                    enableAnimations = enableAnimations,
                    modifier = Modifier.weight(1f)
                )
                LiquidGlassKey(
                    text = "×",
                    type = KeyType.OPERATOR,
                    onClick = { performHaptic(); onOperator("×") },
                    testTag = "key_multiply",
                    enableAnimations = enableAnimations,
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
                    onClick = { performHaptic(); onDigit("4") },
                    testTag = "key_4",
                    enableAnimations = enableAnimations,
                    modifier = Modifier.weight(1f)
                )
                LiquidGlassKey(
                    text = "5",
                    type = KeyType.NUMBER,
                    onClick = { performHaptic(); onDigit("5") },
                    testTag = "key_5",
                    enableAnimations = enableAnimations,
                    modifier = Modifier.weight(1f)
                )
                LiquidGlassKey(
                    text = "6",
                    type = KeyType.NUMBER,
                    onClick = { performHaptic(); onDigit("6") },
                    testTag = "key_6",
                    enableAnimations = enableAnimations,
                    modifier = Modifier.weight(1f)
                )
                LiquidGlassKey(
                    text = "−",
                    type = KeyType.OPERATOR,
                    onClick = { performHaptic(); onOperator("−") },
                    testTag = "key_subtract",
                    enableAnimations = enableAnimations,
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
                    onClick = { performHaptic(); onDigit("1") },
                    testTag = "key_1",
                    enableAnimations = enableAnimations,
                    modifier = Modifier.weight(1f)
                )
                LiquidGlassKey(
                    text = "2",
                    type = KeyType.NUMBER,
                    onClick = { performHaptic(); onDigit("2") },
                    testTag = "key_2",
                    enableAnimations = enableAnimations,
                    modifier = Modifier.weight(1f)
                )
                LiquidGlassKey(
                    text = "3",
                    type = KeyType.NUMBER,
                    onClick = { performHaptic(); onDigit("3") },
                    testTag = "key_3",
                    enableAnimations = enableAnimations,
                    modifier = Modifier.weight(1f)
                )
                LiquidGlassKey(
                    text = "+",
                    type = KeyType.OPERATOR,
                    onClick = { performHaptic(); onOperator("+") },
                    testTag = "key_add",
                    enableAnimations = enableAnimations,
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
                    onClick = { performHaptic(); onDigit("0") },
                    testTag = "key_0",
                    enableAnimations = enableAnimations,
                    modifier = Modifier.weight(1f)
                )
                if (showDoubleZeroKey) {
                    LiquidGlassKey(
                        text = "00",
                        type = KeyType.DOUBLE_ZERO,
                        onClick = { performHaptic(); onDoubleZero() },
                        testTag = "key_double_zero",
                        enableAnimations = enableAnimations,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    LiquidGlassKey(
                        text = "+/-",
                        type = KeyType.ACTION,
                        onClick = { performHaptic(); onNegate() },
                        testTag = "key_negate",
                        enableAnimations = enableAnimations,
                        modifier = Modifier.weight(1f)
                    )
                }
                LiquidGlassKey(
                    text = ".",
                    type = KeyType.NUMBER,
                    onClick = { performHaptic(); onDigit(".") },
                    testTag = "key_decimal",
                    enableAnimations = enableAnimations,
                    modifier = Modifier.weight(1f)
                )
                LiquidGlassKey(
                    text = "=",
                    type = KeyType.EQUALS,
                    onClick = { performHaptic(); onEquals() },
                    testTag = "key_equals",
                    enableAnimations = enableAnimations,
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
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.2f
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Smooth subtle glass press animation
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enableAnimations) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "glass_press_scale"
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
            val border = if (isDark) PureDarkGlassBorder else PureLightGlassBorder
            Triple(bg, textCol, border)
        }
        KeyType.DOUBLE_ZERO -> {
            val bg = if (isDark) {
                if (isPressed) primaryColor.copy(alpha = 0.35f) else primaryColor.copy(alpha = 0.15f)
            } else {
                if (isPressed) primaryColor.copy(alpha = 0.25f) else primaryColor.copy(alpha = 0.12f)
            }
            val textCol = primaryColor
            val border = primaryColor.copy(alpha = if (isPressed) 0.8f else 0.45f)
            Triple(bg, textCol, border)
        }
        KeyType.OPERATOR -> {
            val bg = if (isDark) {
                if (isPressed) primaryColor.copy(alpha = 0.45f) else primaryColor.copy(alpha = 0.25f)
            } else {
                if (isPressed) primaryColor.copy(alpha = 0.30f) else primaryColor.copy(alpha = 0.18f)
            }
            val textCol = primaryColor
            val border = primaryColor.copy(alpha = if (isPressed) 0.8f else 0.5f)
            Triple(bg, textCol, border)
        }
        KeyType.EQUALS -> {
            val bg = if (isPressed) primaryColor.copy(alpha = 0.9f) else primaryColor
            val textCol = if (isDark) Color.Black else Color.White
            val border = if (isDark) Color(0x80FFFFFF) else Color(0x33000000)
            Triple(bg, textCol, border)
        }
        KeyType.ACTION -> {
            val bg = if (isDark) {
                if (isPressed) Color(0x40FFFFFF) else Color(0x24FFFFFF)
            } else {
                if (isPressed) Color(0x66CBD5E1) else Color(0x4DCBD5E1)
            }
            val textCol = if (isDark) PureDarkTextPrimary else PureLightTextPrimary
            val border = if (isDark) PureDarkGlassBorder else PureLightGlassBorder
            Triple(bg, textCol, border)
        }
    }

    val shape = RoundedCornerShape(20.dp)

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .aspectRatio(1.15f)
            .clip(shape)
            .background(glassBg)
            .border(
                width = 1.dp,
                color = glassBorder,
                shape = shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(
                    bounded = true,
                    color = if (type == KeyType.EQUALS) Color.White else primaryColor
                ),
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

package com.example.ui.screens

import androidx.compose.ui.layout.onSizeChanged
import com.example.ui.background.CalculatorDisplayPicture
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import com.example.ui.components.skeuoInset
import androidx.compose.ui.graphics.luminance
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.ui.components.CalculatorDisplay
import com.example.ui.components.CalculatorKeypad
import com.example.ui.theme.AnimationIntensity
import com.example.ui.theme.ComponentSizeStyle
import com.example.ui.theme.HapticIntensity
import com.example.ui.theme.ShapeStructureStyle
import com.example.ui.theme.SoundIntensity

@Composable
fun BasicCalculatorScreen(
    expression: String,
    liveResult: String?,
    isAllClear: Boolean,
    showDoubleZeroKey: Boolean = true,
    animationIntensity: AnimationIntensity = AnimationIntensity.BALANCED,
    hapticIntensity: HapticIntensity = HapticIntensity.MEDIUM,
    soundIntensity: SoundIntensity = SoundIntensity.STANDARD,
    componentSizeStyle: ComponentSizeStyle = ComponentSizeStyle.STANDARD,
    shapeStructureStyle: ShapeStructureStyle = ShapeStructureStyle.ROUNDED,
    onDigit: (String) -> Unit = {},
    onDoubleZero: () -> Unit = {},
    onOperator: (String) -> Unit = {},
    onPercent: () -> Unit = {},
    onClear: () -> Unit = {},
    onBackspace: () -> Unit = {},
    onEquals: () -> Unit = {},
    onNegate: () -> Unit = {},
    onCopyExpression: () -> Unit = {},
    onCopyResult: () -> Unit = {},
    onPaste: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("basic_calculator_screen"),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Transparent Frosted Glass Display Area with tap-to-show Copy/Paste Bubble
        val skin = com.example.ui.theme.LocalAppThemeSkin.current
        val bgState = com.example.ui.background.LocalAppBackground.current
        val isDark = androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .onSizeChanged { size ->
                    if (size.height > 0) bgState?.reportDisplayAspect(size.width.toFloat() / size.height)
                }
                .skeuoInset(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                    base = skin.surface,
                    isDark = isDark,
                    accent = androidx.compose.material3.MaterialTheme.colorScheme.primary
                )
        ) {
            CalculatorDisplayPicture()
            CalculatorDisplay(
                expression = expression,
                liveResult = liveResult,
                onCopyExpression = onCopyExpression,
                onCopyResult = onCopyResult,
                onPaste = onPaste,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp)
            )
        }

        // Transparent Liquid Glass Keypad
        CalculatorKeypad(
            isAllClear = isAllClear,
            onDigit = onDigit,
            onDoubleZero = onDoubleZero,
            onOperator = onOperator,
            onPercent = onPercent,
            onClear = onClear,
            onBackspace = onBackspace,
            onEquals = onEquals,
            onNegate = onNegate,
            showDoubleZeroKey = showDoubleZeroKey,
            animationIntensity = animationIntensity,
            hapticIntensity = hapticIntensity,
            soundIntensity = soundIntensity,
            componentSizeStyle = componentSizeStyle,
            shapeStructureStyle = shapeStructureStyle,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

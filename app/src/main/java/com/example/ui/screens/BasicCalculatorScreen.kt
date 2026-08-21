package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.ui.components.CalculatorDisplay
import com.example.ui.components.CalculatorKeypad
import com.example.ui.theme.ShapeStructureStyle

@Composable
fun BasicCalculatorScreen(
    expression: String,
    liveResult: String?,
    isAllClear: Boolean,
    showDoubleZeroKey: Boolean = true,
    enableAnimations: Boolean = true,
    hapticEnabled: Boolean = true,
    soundEnabled: Boolean = true,
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
        CalculatorDisplay(
            expression = expression,
            liveResult = liveResult,
            onCopyExpression = onCopyExpression,
            onCopyResult = onCopyResult,
            onPaste = onPaste,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

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
            enableAnimations = enableAnimations,
            hapticEnabled = hapticEnabled,
            soundEnabled = soundEnabled,
            shapeStructureStyle = shapeStructureStyle,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

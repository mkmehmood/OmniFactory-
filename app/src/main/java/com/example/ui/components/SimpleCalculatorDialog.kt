package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.domain.calculator.CalculationResult
import com.example.domain.calculator.CalculatorEngine
import com.example.ui.theme.DigitalMonospaceFont

@Composable
fun SimpleCalculatorDialog(
    onDismiss: () -> Unit,
    onResultSelected: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var expression by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf<String?>(null) }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    fun evaluateLive(expr: String) {
        if (expr.isBlank()) {
            resultText = null
            return
        }
        val res = CalculatorEngine.evaluate(expr)
        resultText = when (res) {
            is CalculationResult.Success -> CalculatorEngine.formatNumber(res.value)
            is CalculationResult.Error -> null
            CalculationResult.Empty -> null
        }
    }

    fun appendChar(c: String) {
        val newExpr = expression + c
        expression = newExpr
        evaluateLive(newExpr)
    }

    fun onBackspace() {
        if (expression.isNotEmpty()) {
            val newExpr = expression.dropLast(1)
            expression = newExpr
            evaluateLive(newExpr)
        }
    }

    fun onClear() {
        expression = ""
        resultText = null
    }

    fun onEquals() {
        if (expression.isNotBlank()) {
            val res = CalculatorEngine.evaluate(expression)
            if (res is CalculationResult.Success) {
                val formatted = CalculatorEngine.formatNumber(res.value)
                expression = formatted.replace(",", "")
                resultText = null
            }
        }
    }

    val isDark = MaterialTheme.colorScheme.background.red < 0.2f
    val primaryColor = MaterialTheme.colorScheme.primary
    val dialogBg = if (isDark) Color(0xFF1E222B) else Color(0xFFF9FAFB)
    val cardBorder = if (isDark) Color(0x33FFFFFF) else Color(0x1F000000)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 380.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, cardBorder, RoundedCornerShape(24.dp))
                .testTag("simple_calculator_dialog"),
            color = dialogBg,
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(primaryColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = "QUICK CALCULATOR",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = DigitalMonospaceFont,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = 0.5.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Calculator",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Display Area
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(
                            1.dp,
                            if (isDark) Color(0x30FFFFFF) else Color(0x18000000),
                            RoundedCornerShape(14.dp)
                        )
                        .clickable {
                            val activeVal = resultText ?: expression
                            if (activeVal.isNotBlank()) {
                                clipboardManager.setText(AnnotatedString(activeVal.replace(",", "")))
                                Toast.makeText(context, "Copied $activeVal", Toast.LENGTH_SHORT).show()
                            }
                        },
                    color = if (isDark) Color(0xFF13161C) else Color(0xFFEAEFF5),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = if (expression.isEmpty()) "0" else expression,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = DigitalMonospaceFont,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            textAlign = TextAlign.End,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TAP TO COPY",
                                fontSize = 9.sp,
                                fontFamily = DigitalMonospaceFont,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )

                            Text(
                                text = if (resultText != null) "= $resultText" else "",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = DigitalMonospaceFont,
                                color = primaryColor,
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Keypad Matrix
                val buttonRows = listOf(
                    listOf("C" to DialogCalcKeyType.ACTION, "⌫" to DialogCalcKeyType.ACTION, "%" to DialogCalcKeyType.OPERATOR, "÷" to DialogCalcKeyType.OPERATOR),
                    listOf("7" to DialogCalcKeyType.DIGIT, "8" to DialogCalcKeyType.DIGIT, "9" to DialogCalcKeyType.DIGIT, "×" to DialogCalcKeyType.OPERATOR),
                    listOf("4" to DialogCalcKeyType.DIGIT, "5" to DialogCalcKeyType.DIGIT, "6" to DialogCalcKeyType.DIGIT, "−" to DialogCalcKeyType.OPERATOR),
                    listOf("1" to DialogCalcKeyType.DIGIT, "2" to DialogCalcKeyType.DIGIT, "3" to DialogCalcKeyType.DIGIT, "+" to DialogCalcKeyType.OPERATOR),
                    listOf("00" to DialogCalcKeyType.DIGIT, "0" to DialogCalcKeyType.DIGIT, "." to DialogCalcKeyType.DIGIT, "=" to DialogCalcKeyType.EQUALS)
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    buttonRows.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { (label, type) ->
                                MiniCalcButton(
                                    label = label,
                                    type = type,
                                    primaryColor = primaryColor,
                                    isDark = isDark,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        when (label) {
                                            "C" -> onClear()
                                            "⌫" -> onBackspace()
                                            "=" -> onEquals()
                                            "÷" -> appendChar("÷")
                                            "×" -> appendChar("×")
                                            "−" -> appendChar("-")
                                            "+" -> appendChar("+")
                                            "%" -> appendChar("%")
                                            else -> appendChar(label)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                                1.dp,
                                if (isDark) Color(0x28FFFFFF) else Color(0x1F000000),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                val currentVal = (resultText ?: expression).replace(",", "").trim()
                                if (currentVal.isNotEmpty()) {
                                    clipboardManager.setText(AnnotatedString(currentVal))
                                    Toast.makeText(context, "Copied: $currentVal", Toast.LENGTH_SHORT).show()
                                }
                            },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isDark) Color(0x15FFFFFF) else Color(0x0A000000)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Copy Result",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = DigitalMonospaceFont,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val activeVal = (resultText ?: expression).replace(",", "").trim()
                            if (activeVal.isNotEmpty()) {
                                onResultSelected?.invoke(activeVal)
                            }
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(
                            text = "Done",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = DigitalMonospaceFont
                        )
                    }
                }
            }
        }
    }
}

private enum class DialogCalcKeyType {
    DIGIT, OPERATOR, ACTION, EQUALS
}

@Composable
private fun MiniCalcButton(
    label: String,
    type: DialogCalcKeyType,
    primaryColor: Color,
    isDark: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val (bg, textColor, borderC) = when (type) {
        DialogCalcKeyType.DIGIT -> Triple(
            if (isDark) Color(0x1CFFFFFF) else Color(0x0D000000),
            MaterialTheme.colorScheme.onSurface,
            if (isDark) Color(0x22FFFFFF) else Color(0x14000000)
        )
        DialogCalcKeyType.OPERATOR -> Triple(
            primaryColor.copy(alpha = 0.15f),
            primaryColor,
            primaryColor.copy(alpha = 0.35f)
        )
        DialogCalcKeyType.ACTION -> Triple(
            if (isDark) Color(0x28EF4444).copy(alpha = 0.15f) else Color(0x18EF4444),
            Color(0xFFEF4444),
            Color(0xFFEF4444).copy(alpha = 0.3f)
        )
        DialogCalcKeyType.EQUALS -> Triple(
            primaryColor,
            Color.White,
            primaryColor
        )
    }

    Surface(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, borderC, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .testTag("mini_calc_key_$label"),
        shape = RoundedCornerShape(10.dp),
        color = bg
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (label == "⌫") {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Backspace",
                    tint = textColor,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Text(
                    text = label,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont,
                    color = textColor
                )
            }
        }
    }
}

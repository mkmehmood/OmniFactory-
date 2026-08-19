package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DigitalMonospaceFont

@Composable
fun CalculatorDisplay(
    expression: String,
    liveResult: String?,
    onCopyExpression: () -> Unit,
    onCopyResult: () -> Unit,
    onPaste: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val isDark = MaterialTheme.colorScheme.background.red < 0.2f
    val primaryColor = MaterialTheme.colorScheme.primary
    var isClipboardActionsVisible by remember { mutableStateOf(false) }

    LaunchedEffect(expression) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // Tapping anywhere on the display reveals / hides the 3 clipboard action sections
                isClipboardActionsVisible = !isClipboardActionsVisible
            }
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("calculator_display"),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.End
    ) {
        // Floating 3 Clipboard Action Sections (Copy Expr, Copy Result, Paste) revealed directly on tap
        AnimatedVisibility(
            visible = isClipboardActionsVisible,
            enter = fadeIn() + scaleIn(initialScale = 0.92f),
            exit = fadeOut() + scaleOut(targetScale = 0.92f)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            listOf(
                                primaryColor.copy(alpha = 0.6f),
                                primaryColor.copy(alpha = 0.2f)
                            )
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .testTag("clipboard_popup_bubble"),
                shape = RoundedCornerShape(20.dp),
                color = if (isDark) Color(0xDD0E1726) else Color(0xEEFFFFFF)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Section 1: Copy Expression
                    ClipboardSectionButton(
                        label = "Copy Expr",
                        icon = Icons.Default.ContentCopy,
                        onClick = {
                            onCopyExpression()
                            isClipboardActionsVisible = false
                        },
                        testTag = "popup_copy_expression_button",
                        primaryColor = primaryColor
                    )

                    // Section 2: Copy Result
                    ClipboardSectionButton(
                        label = "Copy Result",
                        icon = Icons.Default.Calculate,
                        onClick = {
                            onCopyResult()
                            isClipboardActionsVisible = false
                        },
                        testTag = "popup_copy_result_button",
                        primaryColor = primaryColor
                    )

                    // Section 3: Paste
                    ClipboardSectionButton(
                        label = "Paste",
                        icon = Icons.Default.ContentPaste,
                        onClick = {
                            onPaste()
                            isClipboardActionsVisible = false
                        },
                        testTag = "popup_paste_button",
                        primaryColor = primaryColor
                    )
                }
            }
        }

        // Secondary / Expression Input Line (Monospace Digital Style)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (expression.isEmpty()) "0" else expression,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = if (expression.length > 18) 20.sp else 24.sp,
                fontFamily = DigitalMonospaceFont,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp,
                textAlign = TextAlign.End,
                maxLines = 1,
                modifier = Modifier.testTag("expression_text")
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Large Primary Evaluation / Monospaced Result Display
        val displayText = liveResult ?: if (expression.isEmpty()) "0" else expression
        val parts = displayText.split(".")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = parts[0],
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = if (displayText.length > 10) 40.sp else 54.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 1.5.sp,
                fontFamily = DigitalMonospaceFont,
                textAlign = TextAlign.End
            )
            if (parts.size > 1) {
                Text(
                    text = "." + parts[1],
                    color = primaryColor,
                    fontSize = if (displayText.length > 10) 40.sp else 54.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 1.5.sp,
                    fontFamily = DigitalMonospaceFont,
                    textAlign = TextAlign.End
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Clean Hairline Glass Divider
        HorizontalDivider(
            thickness = 1.dp,
            color = if (isDark) Color(0x26FFFFFF) else Color(0x26000000),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ClipboardSectionButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    testTag: String,
    primaryColor: Color
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = primaryColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

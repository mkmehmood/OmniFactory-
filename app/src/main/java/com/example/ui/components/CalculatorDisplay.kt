package com.example.ui.components

import androidx.compose.ui.graphics.compositeOver
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.luminance
import com.example.ui.theme.DigitalMonospaceFont
import com.example.ui.theme.LocalAppFontStyle
import com.example.ui.theme.LocalAppThemeSkin

@Composable
fun CalculatorDisplay(
    expression: String,
    liveResult: String?,
    onCopyExpression: () -> Unit,
    onCopyResult: () -> Unit,
    onPaste: () -> Unit,
    modifier: Modifier = Modifier
) {
    val exprScrollState = rememberScrollState()
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val currentSkin = LocalAppThemeSkin.current
    val primaryColor = MaterialTheme.colorScheme.primary
    var isClipboardActionsVisible by remember { mutableStateOf(false) }

    LaunchedEffect(expression) {
        exprScrollState.animateScrollTo(exprScrollState.maxValue)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // Tapping anywhere on the display reveals / hides clipboard actions
                isClipboardActionsVisible = !isClipboardActionsVisible
            }
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .testTag("calculator_display"),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.End
    ) {
        // Floating Clipboard Action Sections (Copy Expr, Copy Result, Paste)
        AnimatedVisibility(
            visible = isClipboardActionsVisible,
            enter = fadeIn() + scaleIn(initialScale = 0.92f),
            exit = fadeOut() + scaleOut(targetScale = 0.92f)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .skeuoRaised(RoundedCornerShape(20.dp), currentSkin.surface.compositeOver(currentSkin.background), isDark, elevation = 8.dp)
                    .testTag("clipboard_popup_bubble"),
                shape = RoundedCornerShape(20.dp),
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
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

        // Multi-Line Expression Box (Shows line-by-line wrapping for long expressions instead of disappearing)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 28.dp, max = 110.dp)
                .verticalScroll(exprScrollState),
            contentAlignment = Alignment.BottomEnd
        ) {
            Text(
                text = if (expression.isEmpty()) "0" else expression,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = when {
                    expression.length > 40 -> 16.sp
                    expression.length > 20 -> 19.sp
                    else -> 23.sp
                },
                fontFamily = DigitalMonospaceFont,
                fontStyle = LocalAppFontStyle.current.defaultFontStyle,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.8.sp,
                lineHeight = when {
                    expression.length > 40 -> 21.sp
                    expression.length > 20 -> 25.sp
                    else -> 30.sp
                },
                softWrap = true,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("expression_text")
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

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
                fontSize = when {
                    displayText.length > 14 -> 32.sp
                    displayText.length > 9 -> 40.sp
                    else -> 52.sp
                },
                fontWeight = LocalAppFontStyle.current.defaultFontWeight,
                fontStyle = LocalAppFontStyle.current.defaultFontStyle,
                letterSpacing = 1.2.sp,
                fontFamily = DigitalMonospaceFont,
                textAlign = TextAlign.End
            )
            if (parts.size > 1) {
                Text(
                    text = "." + parts[1],
                    color = primaryColor,
                    fontSize = when {
                        displayText.length > 14 -> 32.sp
                        displayText.length > 9 -> 40.sp
                        else -> 52.sp
                    },
                    fontWeight = FontWeight.Normal,
                    fontStyle = LocalAppFontStyle.current.defaultFontStyle,
                    letterSpacing = 1.2.sp,
                    fontFamily = DigitalMonospaceFont,
                    textAlign = TextAlign.End
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Clean Hairline Glass Divider
        SoftDivider(
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
    val skin = LocalAppThemeSkin.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    Surface(
        modifier = Modifier
            .skeuoRaised(RoundedCornerShape(10.dp), skin.plate(), isDark, elevation = 3.dp)
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(10.dp),
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

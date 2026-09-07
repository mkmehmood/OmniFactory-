package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DigitalMonospaceFont
import com.example.ui.theme.LocalAppThemeSkin

/**
 * Applies the Liquid Glass theme visualization directly to input containers:
 * - Ultra-smooth glass capsule or rounded contour
 * - Translucent frosted glass refraction gradient
 * - Specular curved liquid highlight across the top edge (as seen in the mockup reference)
 * - Luminous glass rim with top specular glint and glowing accent transition
 */
fun Modifier.liquidGlassField(
    isFocused: Boolean = false,
    shape: Shape = RoundedCornerShape(16.dp),
    accentColor: Color = Color(0xFF00E5FF),
    isDark: Boolean = true,
    elevation: Dp = 3.dp
): Modifier = this
    .shadow(
        elevation = if (isFocused) (elevation + 3.dp) else elevation,
        shape = shape,
        ambientColor = if (isFocused) accentColor.copy(alpha = 0.22f) else if (isDark) Color(0x22000000) else Color(0x12000000),
        spotColor = if (isFocused) accentColor.copy(alpha = 0.32f) else if (isDark) Color(0x33000000) else Color(0x18000000)
    )
    .clip(shape)
    .drawBehind {
        // Specular curved liquid glass sheen along top edge
        val highlightHeight = size.height * 0.45f
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    if (isDark) Color.White.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.45f),
                    if (isDark) Color.White.copy(alpha = 0.04f) else Color.White.copy(alpha = 0.10f),
                    Color.Transparent
                ),
                startY = 0f,
                endY = highlightHeight
            ),
            size = size.copy(height = highlightHeight)
        )
    }
    .border(
        width = if (isFocused) 1.5.dp else 1.1.dp,
        brush = if (isFocused) {
            Brush.linearGradient(
                colors = listOf(
                    if (isDark) Color.White.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.95f),
                    accentColor,
                    accentColor.copy(alpha = 0.80f),
                    accentColor.copy(alpha = 0.40f)
                ),
                start = Offset(0f, 0f),
                end = Offset(400f, 400f)
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    if (isDark) Color.White.copy(alpha = 0.32f) else Color.White.copy(alpha = 0.70f),
                    if (isDark) Color.White.copy(alpha = 0.10f) else Color.White.copy(alpha = 0.30f),
                    if (isDark) Color.White.copy(alpha = 0.04f) else Color.Black.copy(alpha = 0.06f)
                ),
                start = Offset(0f, 0f),
                end = Offset(250f, 250f)
            )
        },
        shape = shape
    )

/**
 * Standardized Liquid Glass Text Field across the entire application.
 * Emulates the floating liquid glass capsule aesthetic with authentic top sheen and glowing rim.
 */
@Composable
fun LiquidGlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    label: String? = null,
    leadingIcon: ImageVector? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    prefix: (@Composable () -> Unit)? = null,
    suffix: (@Composable () -> Unit)? = null,
    isError: Boolean = false,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    shape: Shape = RoundedCornerShape(16.dp),
    accentColor: Color = MaterialTheme.colorScheme.primary,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    textStyle: TextStyle? = null,
    testTag: String = ""
) {
    val skin = LocalAppThemeSkin.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val interactionSource = remember { MutableInteractionSource() }
    val isSystemFocused by interactionSource.collectIsFocusedAsState()
    val isFocused = isSystemFocused

    val effectiveAccent = if (isError) Color(0xFFEF4444) else accentColor

    val containerBgBrush = Brush.verticalGradient(
        colors = if (isFocused) {
            listOf(
                skin.surfaceVariant.copy(alpha = if (isDark) 0.65f else 0.75f),
                skin.surface.copy(alpha = if (isDark) 0.85f else 0.90f)
            )
        } else {
            listOf(
                skin.surfaceVariant.copy(alpha = if (isDark) 0.40f else 0.55f),
                skin.surface.copy(alpha = if (isDark) 0.60f else 0.75f)
            )
        }
    )

    val computedTextStyle = (textStyle ?: MaterialTheme.typography.bodyLarge.copy(
        fontSize = 15.sp,
        fontFamily = DigitalMonospaceFont
    )).copy(
        color = MaterialTheme.colorScheme.onSurface
    )

    Column(modifier = modifier) {
        if (label != null) {
            Text(
                text = label.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                letterSpacing = 0.6.sp,
                color = if (isFocused) effectiveAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, bottom = 5.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlassField(
                    isFocused = isFocused,
                    shape = shape,
                    accentColor = effectiveAccent,
                    isDark = isDark,
                    elevation = if (isFocused) 4.dp else 2.dp
                )
                .background(containerBgBrush)
                .defaultMinSize(minHeight = 50.dp)
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (leadingContent != null) {
                    leadingContent()
                    Spacer(modifier = Modifier.width(8.dp))
                } else if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = if (isFocused) effectiveAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }

                if (prefix != null) {
                    prefix()
                    Spacer(modifier = Modifier.width(4.dp))
                }

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty() && placeholder != null) {
                        Text(
                            text = placeholder,
                            style = computedTextStyle.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                                fontWeight = FontWeight.Normal
                            ),
                            maxLines = maxLines,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = enabled,
                        readOnly = readOnly,
                        textStyle = computedTextStyle,
                        cursorBrush = SolidColor(effectiveAccent),
                        keyboardOptions = keyboardOptions,
                        keyboardActions = keyboardActions,
                        singleLine = singleLine,
                        maxLines = maxLines,
                        interactionSource = interactionSource
                    )
                }

                if (suffix != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    suffix()
                }

                if (trailingContent != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    trailingContent()
                }
            }
        }
    }
}

/**
 * Standardized Liquid Glass Dialpad Input field for financial/mathematical inputs:
 * Tapping sets focus, opens the custom keypad/dialpad, and reveals an active blinking cursor.
 */
@Composable
fun LiquidGlassDialpadInput(
    label: String,
    value: String,
    isFocused: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "0.00",
    leadingIcon: ImageVector? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    suffixText: String? = null,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    shape: Shape = RoundedCornerShape(16.dp),
    testTag: String = ""
) {
    val skin = LocalAppThemeSkin.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val containerBgBrush = Brush.verticalGradient(
        colors = if (isFocused) {
            listOf(
                accentColor.copy(alpha = if (isDark) 0.16f else 0.12f),
                skin.surfaceVariant.copy(alpha = if (isDark) 0.60f else 0.70f)
            )
        } else {
            listOf(
                skin.surfaceVariant.copy(alpha = if (isDark) 0.40f else 0.55f),
                skin.surface.copy(alpha = if (isDark) 0.60f else 0.75f)
            )
        }
    )

    Column(modifier = modifier) {
        if (label.isNotEmpty()) {
            Text(
                text = label.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                letterSpacing = 0.6.sp,
                color = if (isFocused) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, bottom = 5.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlassField(
                    isFocused = isFocused,
                    shape = shape,
                    accentColor = accentColor,
                    isDark = isDark,
                    elevation = if (isFocused) 4.dp else 2.dp
                )
                .background(containerBgBrush)
                .clickable { onClick() }
                .defaultMinSize(minHeight = 52.dp)
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    if (leadingIcon != null) {
                        Icon(
                            imageVector = leadingIcon,
                            contentDescription = null,
                            tint = if (isFocused) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(19.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    }

                    val displayValue = if (value.isEmpty()) placeholder else value
                    val isPlaceholder = value.isEmpty()

                    Text(
                        text = displayValue,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DigitalMonospaceFont,
                        color = if (isPlaceholder) {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.50f)
                        } else if (isFocused) {
                            accentColor
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (isFocused) {
                        Spacer(modifier = Modifier.width(3.dp))
                        BlinkingCursorPointer(color = accentColor, cursorHeight = 20.dp)
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    if (suffixText != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(accentColor.copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = suffixText,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = DigitalMonospaceFont,
                                color = accentColor
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    if (trailingContent != null) {
                        trailingContent()
                    }
                }
            }
        }
    }
}

/**
 * Standardized Liquid Glass Search Bar with capsule pill geometry (as in the reference mockups).
 */
@Composable
fun LiquidGlassSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    onClear: (() -> Unit)? = null,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    testTag: String = "liquid_glass_search_bar"
) {
    LiquidGlassTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = placeholder,
        leadingIcon = Icons.Default.Search,
        trailingContent = if (query.isNotEmpty()) {
            {
                IconButton(
                    onClick = {
                        onQueryChange("")
                        onClear?.invoke()
                    },
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        } else null,
        shape = CircleShape, // Capsule pill as shown in uploaded Liquid Glass mockups
        singleLine = true,
        accentColor = accentColor,
        modifier = modifier.testTag(testTag)
    )
}

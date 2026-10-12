package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.util.HapticFeedbackHelper
import com.example.domain.util.SoundFeedbackHelper
import com.example.ui.theme.DigitalMonospaceFont
import com.example.ui.theme.HapticIntensity
import com.example.ui.theme.LocalAppThemeSkin
import com.example.ui.theme.LocalShapeStructure
import com.example.ui.theme.SoundIntensity
import com.example.ui.theme.toInputShape
import com.example.ui.theme.toChipShape
import androidx.compose.ui.graphics.compositeOver
import kotlinx.coroutines.launch

/**
 * Skeuomorphic raised plate (name kept for call-site compatibility).
 * A physical panel lit from above: bevelled rim, moulded sheen and a real cast shadow.
 * When focused it picks up an accent lamp tint.
 */
fun Modifier.liquidGlassSurface(
    shape: Shape = RoundedCornerShape(18.dp),
    elevation: Dp = 4.dp,
    isDark: Boolean = true,
    isFocused: Boolean = false,
    accentColor: Color = Color(0xFFFF9F0A),
    containerBaseColor: Color? = null,
    highlightIntensity: Float = 1.0f
): Modifier {
    val base = (containerBaseColor ?: if (isDark) Color(0xFF332B26) else Color(0xFFEDE7DA)).copy(alpha = 1f)
    return this.skeuoRaised(
        shape = shape,
        base = base,
        isDark = isDark,
        elevation = if (isFocused) elevation + 2.dp else elevation,
        accent = if (isFocused) accentColor else null,
        sheen = highlightIntensity
    )
}

/**
 * Modifier that draws a crisp, straight dotted line at the bottom of the component.
 */
fun Modifier.straightDottedLine(
    color: Color,
    strokeWidth: Dp = 1.5.dp,
    dotLength: Dp = 4.dp,
    spaceLength: Dp = 3.dp
): Modifier = this.drawBehind {
    val strokePx = strokeWidth.toPx()
    val y = size.height - (strokePx / 2f)
    drawLine(
        color = color,
        start = Offset(0f, y),
        end = Offset(size.width, y),
        strokeWidth = strokePx,
        pathEffect = PathEffect.dashPathEffect(
            floatArrayOf(dotLength.toPx(), spaceLength.toPx()),
            0f
        )
    )
}

/**
 * Standardized input field: a recessed well cut into the panel, with an engraved lip.
 * Focus lights the rim in the accent colour.
 */
@Composable
fun LiquidGlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape? = null,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    containerBaseColor: Color? = null,
    forceFocused: Boolean = false,
    testTag: String? = null
) {
    val skin = LocalAppThemeSkin.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val isFocusedInternal by interactionSource.collectIsFocusedAsState()
    val isFieldFocused = forceFocused || isFocusedInternal

    val wellShape: Shape = shape ?: LocalShapeStructure.current.toInputShape()
    val fieldModifier = modifier
        .skeuoInset(
            shape = wellShape,
            base = containerBaseColor ?: skin.surface,
            isDark = isDark,
            focused = isFieldFocused,
            accent = accentColor,
            error = isError,
            errorColor = MaterialTheme.colorScheme.error
        )
        .let { if (testTag != null) it.testTag(testTag) else it }

    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = fieldModifier,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        prefix = prefix,
        suffix = suffix,
        supportingText = supportingText,
        isError = isError,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        interactionSource = interactionSource,
        shape = wellShape,
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            errorContainerColor = Color.Transparent,
            cursorColor = accentColor,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedLabelColor = accentColor,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.50f),
            focusedLeadingIconColor = accentColor,
            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

/**
 * Skeuomorphic push button (name kept for call-site compatibility).
 * A raised key that physically travels down when pressed. [isSelected] latches it
 * in the pressed position with a lit accent rim, like a mechanical toggle key.
 */
@Composable
fun DottedLineButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isSelected: Boolean = false,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    testTag: String? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
    content: @Composable RowScope.() -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val skin = LocalAppThemeSkin.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val down = isPressed || isSelected
    val shape = LocalShapeStructure.current.toInputShape()

    Box(
        modifier = modifier
            .graphicsLayer {
                translationY = if (down) 1.5.dp.toPx() else 0f
                alpha = if (enabled) 1f else 0.5f
            }
            .skeuoRaised(
                shape = shape,
                base = skin.plate(),
                isDark = isDark,
                elevation = 4.dp,
                pressed = down,
                accent = if (isSelected) accentColor else null
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .let { if (testTag != null) it.testTag(testTag) else it },
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.padding(contentPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            content = content
        )
    }
}

/**
 * Filter chip styled as a small rocker key: raised when off, latched down with a lit rim when on.
 */
@Composable
fun DottedFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    testTag: String? = null
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val skin = LocalAppThemeSkin.current
    val shape = LocalShapeStructure.current.toChipShape()

    Box(
        modifier = modifier
            .graphicsLayer { translationY = if (selected) 1.dp.toPx() else 0f }
            .skeuoRaised(
                shape = shape,
                base = skin.plate(),
                isDark = isDark,
                elevation = 3.dp,
                pressed = selected,
                accent = if (selected) accentColor else null
            )
            .clickable(onClick = onClick)
            .let { if (testTag != null) it.testTag(testTag) else it }
            .padding(horizontal = 12.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = if (selected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = label,
                fontSize = 11.5.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                fontFamily = DigitalMonospaceFont,
                color = if (selected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Standardized Search Field with straight dotted line underline.
 */
@Composable
fun LiquidGlassSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholderText: String = "Search...",
    leadingIcon: ImageVector = Icons.Default.Search,
    onClear: (() -> Unit)? = null,
    testTag: String = "liquid_glass_search_field"
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    LiquidGlassTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = placeholderText,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = {
                        if (onClear != null) onClear() else onQueryChange("")
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        },
        singleLine = true,
        modifier = modifier.fillMaxWidth(),
        accentColor = primaryColor,
        testTag = testTag
    )
}

/**
 * Raised panel modifier (name kept for call-site compatibility).
 */
fun Modifier.spatialGlass(
    shape: Shape = RoundedCornerShape(20.dp),
    elevation: Dp = 8.dp,
    borderAlpha: Float = 0.35f,
    specularHighlight: Boolean = true,
    isDark: Boolean = true,
    accentColor: Color = Color(0xFF00E5FF)
): Modifier = this.skeuoRaised(
    shape = shape,
    base = if (isDark) Color(0xFF332B26) else Color(0xFFEDE7DA),
    isDark = isDark,
    elevation = elevation,
    sheen = if (specularHighlight) 1f else 0f
)

/**
 * Spatial Glass Card container with layered optical depth and floating elevation.
 */
@Composable
fun SpatialGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    elevation: Dp = 8.dp,
    containerColor: Color? = null,
    testTag: String = "spatial_glass_card",
    content: @Composable BoxScope.() -> Unit
) {
    val skin = LocalAppThemeSkin.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val accent = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .skeuoRaised(
                shape = shape,
                base = (containerColor ?: skin.surface).compositeOver(skin.surface),
                isDark = isDark,
                elevation = if (elevation > 8.dp) 8.dp else elevation
            )
            .testTag(testTag),
        content = content
    )
}

/**
 * Spatial Floating Pill for interactive toggles, quick filters, or tags.
 */
@Composable
fun SpatialFloatingPill(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    testTag: String = "spatial_floating_pill"
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val primaryColor = MaterialTheme.colorScheme.primary
    val skin = LocalAppThemeSkin.current
    val context = LocalContext.current
    val view = LocalView.current

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "spatial_pill_scale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationY = if (isSelected) 1.dp.toPx() else 0f
            }
            .skeuoRaised(
                shape = CircleShape,
                base = skin.plate(),
                isDark = isDark,
                elevation = 3.dp,
                pressed = isSelected || isPressed,
                accent = if (isSelected) primaryColor else null
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                HapticFeedbackHelper.performKeypadTap(context, view, HapticIntensity.LIGHT)
                SoundFeedbackHelper.playClickSound(context, view, SoundIntensity.STANDARD)
                onClick()
            }
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontFamily = DigitalMonospaceFont,
                color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Spatial Glass Stat Card with glowing accent rim and prominent monetary / mathematical display.
 */
@Composable
fun SpatialGlassStatCard(
    title: String,
    value: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    testTag: String = "spatial_stat_card"
) {
    val skin = LocalAppThemeSkin.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    SpatialGlassCard(
        modifier = modifier.testTag(testTag),
        elevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (icon != null) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .skeuoInset(CircleShape, skin.surface, isDark, accent = accentColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = title.uppercase(),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        fontFamily = DigitalMonospaceFont,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Jewel lamp
                SkeuoLed(color = accentColor, size = 10.dp)
            }

            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Modern Vision-inspired Floating Spatial Dock Bar for rapid single-tap switching between modules.
 */
@Composable
fun SpatialFloatingDock(
    currentDestination: DrawerDestination,
    onSelectDestination: (DrawerDestination) -> Unit,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val skin = LocalAppThemeSkin.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val primaryColor = MaterialTheme.colorScheme.primary
    val context = LocalContext.current
    val view = LocalView.current

    val dockItems = listOf(
        Triple(DrawerDestination.CALCULATOR, Icons.Default.Calculate, "Calc"),
        Triple(DrawerDestination.LEDGER, Icons.Default.AccountBalanceWallet, "Ledger"),
        Triple(DrawerDestination.EXPENSES, Icons.Default.Receipt, "Expenses"),
        Triple(DrawerDestination.CONVERTER, Icons.Default.SwapHoriz, "Units"),
        Triple(DrawerDestination.MIXTURE_CALCULATOR, Icons.Default.Science, "Mixture"),
        Triple(DrawerDestination.SETTINGS, Icons.Default.Settings, "Config")
    )

    Box(
        modifier = modifier
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .liquidGlassSurface(
                    shape = CircleShape,
                    elevation = 10.dp,
                    isDark = isDark,
                    isFocused = false,
                    accentColor = primaryColor,
                    containerBaseColor = skin.surface
                )
                .padding(horizontal = 8.dp, vertical = 5.dp)
                .testTag("spatial_floating_dock"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Menu / All tools trigger
            IconButton(
                onClick = {
                    HapticFeedbackHelper.performKeypadTap(context, view, HapticIntensity.LIGHT)
                    SoundFeedbackHelper.playClickSound(context, view, SoundIntensity.STANDARD)
                    onOpenDrawer()
                },
                modifier = Modifier
                    .size(38.dp)
                    .skeuoRaised(CircleShape, skin.plate(), isDark, elevation = 3.dp)
                    .testTag("spatial_dock_menu_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "All Tools Menu",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(18.dp)
                )
            }

            dockItems.forEach { (dest, icon, label) ->
                val isSelected = currentDestination == dest
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()

                val scale by animateFloatAsState(
                    targetValue = if (isPressed) 0.90f else 1.0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                    label = "dock_item_scale"
                )

                val itemModifier = if (isSelected) {
                    Modifier.skeuoInset(
                        shape = CircleShape,
                        base = skin.surface,
                        isDark = isDark,
                        focused = true,
                        accent = primaryColor
                    )
                } else {
                    Modifier.clip(CircleShape)
                }

                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .then(itemModifier)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            HapticFeedbackHelper.performKeypadTap(context, view, HapticIntensity.LIGHT)
                            SoundFeedbackHelper.playClickSound(context, view, SoundIntensity.STANDARD)
                            onSelectDestination(dest)
                        }
                        .padding(horizontal = 11.dp, vertical = 7.dp)
                        .testTag("dock_item_${dest.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = label,
                            fontSize = 9.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontFamily = DigitalMonospaceFont,
                            color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

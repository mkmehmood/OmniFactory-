package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardColors
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalAppThemeSkin

/**
 * The single soft-UI container for the whole app. Drop-in replacement for Material `Surface`
 * and `Card`: screens no longer decide fills, borders or shadows themselves.
 *
 * The [color] argument is read as a *role*, not a paint colour:
 *  - fully transparent      -> layout wrapper, nothing is drawn
 *  - tint (saturated, faint) -> a pressed-in well lit with that tint (selected / status chips)
 *  - saturated and solid     -> a raised plate in that colour (accent buttons)
 *  - anything neutral        -> a raised plate in the skin's surface colour
 *
 * [border] is accepted for source compatibility and ignored: soft UI uses light and shadow,
 * never outlines.
 */
@Composable
fun SoftSurface(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    shape: Shape = RectangleShape,
    color: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = Color.Unspecified,
    tonalElevation: Dp = 0.dp,
    shadowElevation: Dp = 0.dp,
    border: BorderStroke? = null,
    content: @Composable () -> Unit
) {
    val skin = LocalAppThemeSkin.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val interaction = remember { MutableInteractionSource() }
    val pressedByTouch by interaction.collectIsPressedAsState()

    val role = softRoleOf(color)
    val textOnBase = skin.textPrimary
    val base = when (role) {
        SoftRole.Wrapper -> Color.Transparent
        SoftRole.Tint, SoftRole.Neutral -> skin.surface.compositeOver(skin.background)
        SoftRole.Solid -> color.compositeOver(skin.surface)
    }
    val resolvedContent = when {
        contentColor != Color.Unspecified -> contentColor
        role == SoftRole.Solid -> if (base.luminance() > 0.55f) Color(0xFF2A3350) else Color.White
        else -> textOnBase
    }

    val drawn = if (role == SoftRole.Wrapper) {
        modifier
    } else {
        modifier.skeuoRaised(
            shape = shape,
            base = base,
            isDark = isDark,
            elevation = when {
                shadowElevation <= 0.dp -> 6.dp
                shadowElevation < 3.dp -> 3.dp
                shadowElevation > 10.dp -> 10.dp
                else -> shadowElevation
            },
            pressed = role == SoftRole.Tint || (onClick != null && pressedByTouch),
            accent = if (role == SoftRole.Tint) color.copy(alpha = 1f) else null
        )
    }
    val clickable = if (onClick != null) {
        drawn.clickable(
            interactionSource = interaction,
            indication = null,
            enabled = enabled,
            onClick = onClick
        )
    } else drawn

    CompositionLocalProvider(LocalContentColor provides resolvedContent) {
        Box(modifier = clickable.clip(shape), propagateMinConstraints = true) {
            content()
        }
    }
}

/** Soft-UI replacement for Material `Card`. */
@Composable
fun SoftCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    colors: CardColors? = null,
    border: BorderStroke? = null,
    elevation: androidx.compose.material3.CardElevation? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    SoftSurface(
        modifier = modifier,
        shape = shape,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(content = content)
    }
}

private enum class SoftRole { Wrapper, Tint, Solid, Neutral }

private fun softRoleOf(color: Color): SoftRole {
    if (color == Color.Unspecified) return SoftRole.Neutral
    if (color.alpha < 0.02f) return SoftRole.Wrapper
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(color.copy(alpha = 1f).toArgb(), hsv)
    val saturated = hsv[1] > 0.38f
    return when {
        saturated && color.alpha < 0.6f -> SoftRole.Tint
        saturated -> SoftRole.Solid
        else -> SoftRole.Neutral
    }
}

/** Engraved groove: a dark line with a light line beneath it. Replaces every divider and rule. */
@Composable
fun SoftDivider(modifier: Modifier = Modifier) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(2.dp)
            .drawBehind {
                val h = 1.dp.toPx()
                drawRect(softDark(isDark), size = Size(size.width, h))
                drawRect(softLight(isDark), topLeft = Offset(0f, h), size = Size(size.width, h))
            }
    )
}

/** Track colour for progress bars and sliders. */
@Composable
fun softTrackColor(): Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)

/** Recessed well for grouped rows, chips and icon holders. [tint] lights the well when selected. */
@Composable
fun Modifier.softWell(shape: Shape = RoundedCornerShape(8.dp), tint: Color? = null): Modifier {
    val skin = LocalAppThemeSkin.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val base = skin.surface.compositeOver(skin.background)
    val well = this.skeuoInset(shape = shape, base = base, isDark = isDark)
    return if (tint != null) well.background(tint.copy(alpha = 0.18f), shape) else well
}

/** Raised plate for icon buttons and small controls. */
@Composable
fun Modifier.softPlate(shape: Shape = RoundedCornerShape(12.dp), pressed: Boolean = false): Modifier {
    val skin = LocalAppThemeSkin.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return this.skeuoRaised(
        shape = shape,
        base = skin.surface.compositeOver(skin.background),
        isDark = isDark,
        elevation = 4.dp,
        pressed = pressed
    )
}

/** Segmented-control segment: pressed into a lit well when selected, flat otherwise. */
@Composable
fun Modifier.softToggle(shape: Shape, selected: Boolean, tint: Color): Modifier =
    if (selected) this.softWell(shape, tint) else this.clip(shape)

// ---------------------------------------------------------------------------------------------
// Controls: buttons, icon buttons, switch, checkbox, radio, dialog.
// Signatures mirror the Material widgets they replace so screens only change the call name.
// Colour/border/elevation arguments are not accepted: the look comes from the skin.
// ---------------------------------------------------------------------------------------------

/**
 * Push button. [filled] gives a solid accent key (primary actions); otherwise a neutral raised key
 * whose label is tinted with [accentColor].
 */
@Composable
fun SoftButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(14.dp),
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
    accentColor: Color? = null,
    filled: Boolean = false,
    content: @Composable RowScope.() -> Unit
) {
    val labelColor = when {
        filled && accentColor != null -> if (accentColor.luminance() > 0.55f) Color(0xFF2A3350) else Color.White
        accentColor != null -> accentColor
        else -> LocalContentColor.current
    }
    SoftSurface(
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        color = if (filled && accentColor != null) accentColor else MaterialTheme.colorScheme.surface,
        modifier = modifier
            .defaultMinSize(minHeight = 40.dp)
            .graphicsLayer { alpha = if (enabled) 1f else 0.5f }
    ) {
        CompositionLocalProvider(LocalContentColor provides labelColor) {
            Row(
                modifier = Modifier.padding(contentPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                content = content
            )
        }
    }
}

/** Round raised icon key. */
@Composable
fun SoftIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    SoftSurface(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        modifier = modifier
            .defaultMinSize(minWidth = 40.dp, minHeight = 40.dp)
            .graphicsLayer { alpha = if (enabled) 1f else 0.5f }
    ) {
        Box(contentAlignment = Alignment.Center) { content() }
    }
}

/** Soft switch: a recessed track with a raised knob that slides across. */
@Composable
fun SoftSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val primary = MaterialTheme.colorScheme.primary
    val knobX by animateDpAsState(if (checked) 22.dp else 2.dp, label = "soft_switch_knob")
    Box(
        modifier = modifier
            .size(width = 50.dp, height = 28.dp)
            .graphicsLayer { alpha = if (enabled) 1f else 0.5f }
            .softWell(CircleShape, tint = if (checked) primary else null)
            .toggleable(
                value = checked,
                enabled = enabled && onCheckedChange != null,
                role = Role.Switch,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onValueChange = { onCheckedChange?.invoke(it) }
            )
    ) {
        Box(
            modifier = Modifier
                .offset(x = knobX, y = 2.dp)
                .size(24.dp)
                .softPlate(CircleShape)
        )
    }
}

/** Soft checkbox: a recessed square that lights up with a tick. */
@Composable
fun SoftCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val primary = MaterialTheme.colorScheme.primary
    Box(
        modifier = modifier
            .size(24.dp)
            .graphicsLayer { alpha = if (enabled) 1f else 0.5f }
            .softWell(RoundedCornerShape(7.dp), tint = if (checked) primary else null)
            .toggleable(
                value = checked,
                enabled = enabled && onCheckedChange != null,
                role = Role.Checkbox,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onValueChange = { onCheckedChange?.invoke(it) }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(Icons.Default.Check, contentDescription = null, tint = primary, modifier = Modifier.size(16.dp))
        }
    }
}

/** Soft radio: a recessed circle with a glowing lamp when selected. */
@Composable
fun SoftRadioButton(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .size(24.dp)
            .graphicsLayer { alpha = if (enabled) 1f else 0.5f }
            .softWell(CircleShape)
            .selectable(
                selected = selected,
                enabled = enabled && onClick != null,
                role = Role.RadioButton,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onClick?.invoke() }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (selected) SkeuoLed(color = MaterialTheme.colorScheme.primary, size = 10.dp)
    }
}

/** Dialog panel: a raised plate floating over the dimmed screen. */
@Composable
fun SoftAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(24.dp),
    containerColor: Color = Color.Unspecified,
    tonalElevation: Dp = 0.dp,
    iconContentColor: Color = Color.Unspecified,
    titleContentColor: Color = Color.Unspecified,
    textContentColor: Color = Color.Unspecified,
    properties: DialogProperties = DialogProperties()
) {
    Dialog(onDismissRequest = onDismissRequest, properties = properties) {
        SoftSurface(
            modifier = modifier.fillMaxWidth().padding(8.dp),
            shape = shape,
            shadowElevation = 10.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 22.dp, vertical = 20.dp)) {
                if (icon != null) {
                    Box(modifier = Modifier.align(Alignment.CenterHorizontally)) { icon() }
                    Spacer(modifier = Modifier.height(10.dp))
                }
                if (title != null) {
                    ProvideTextStyle(MaterialTheme.typography.titleLarge) { title() }
                    Spacer(modifier = Modifier.height(12.dp))
                }
                if (text != null) {
                    ProvideTextStyle(MaterialTheme.typography.bodyMedium) { text() }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    dismissButton?.invoke()
                    confirmButton()
                }
            }
        }
    }
}

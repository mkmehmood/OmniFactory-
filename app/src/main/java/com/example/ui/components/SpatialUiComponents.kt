package com.example.ui.components

import androidx.compose.animation.core.Animatable
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
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.util.HapticFeedbackHelper
import com.example.domain.util.SoundFeedbackHelper
import com.example.ui.theme.DigitalMonospaceFont
import com.example.ui.theme.HapticIntensity
import com.example.ui.theme.LocalAppThemeSkin
import com.example.ui.theme.SoundIntensity
import kotlinx.coroutines.launch

/**
 * Modifier that applies Vision-inspired 3D spatial glass depth,
 * dynamic drop shadow elevation, and subtle specular rim highlighting.
 */
fun Modifier.spatialGlass(
    shape: Shape = RoundedCornerShape(20.dp),
    elevation: Dp = 8.dp,
    borderAlpha: Float = 0.35f,
    specularHighlight: Boolean = true,
    isDark: Boolean = true,
    accentColor: Color = Color(0xFF00E5FF)
): Modifier = this
    .shadow(
        elevation = elevation,
        shape = shape,
        ambientColor = if (isDark) accentColor.copy(alpha = 0.25f) else Color(0x20000000),
        spotColor = if (isDark) accentColor.copy(alpha = 0.35f) else Color(0x30000000)
    )
    .clip(shape)
    .border(
        width = 1.2.dp,
        brush = if (specularHighlight) {
            Brush.linearGradient(
                colors = listOf(
                    if (isDark) Color.White.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.85f),
                    accentColor.copy(alpha = borderAlpha),
                    accentColor.copy(alpha = borderAlpha * 0.4f),
                    if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.05f)
                ),
                start = Offset(0f, 0f),
                end = Offset(400f, 400f)
            )
        } else {
            Brush.linearGradient(
                listOf(accentColor.copy(alpha = borderAlpha), accentColor.copy(alpha = borderAlpha * 0.5f))
            )
        },
        shape = shape
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

    val backgroundBrush = containerColor?.let {
        Brush.linearGradient(listOf(it, it))
    } ?: Brush.verticalGradient(
        colors = if (isDark) {
            listOf(
                skin.surfaceVariant.copy(alpha = 0.55f),
                skin.surface.copy(alpha = 0.85f)
            )
        } else {
            listOf(
                skin.surface.copy(alpha = 0.90f),
                skin.surfaceVariant.copy(alpha = 0.65f)
            )
        }
    )

    Box(
        modifier = modifier
            .spatialGlass(
                shape = shape,
                elevation = elevation,
                borderAlpha = if (isDark) 0.40f else 0.25f,
                specularHighlight = true,
                isDark = isDark,
                accentColor = accent
            )
            .background(backgroundBrush)
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

    val shape = CircleShape

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .spatialGlass(
                shape = shape,
                elevation = if (isSelected) 6.dp else 2.dp,
                borderAlpha = if (isSelected) 0.65f else 0.25f,
                specularHighlight = true,
                isDark = isDark,
                accentColor = if (isSelected) primaryColor else skin.outline
            )
            .background(
                if (isSelected) {
                    Brush.linearGradient(
                        listOf(primaryColor.copy(alpha = 0.28f), primaryColor.copy(alpha = 0.12f))
                    )
                } else {
                    Brush.linearGradient(
                        listOf(
                            skin.surfaceVariant.copy(alpha = if (isDark) 0.40f else 0.50f),
                            skin.surface.copy(alpha = if (isDark) 0.60f else 0.70f)
                        )
                    )
                }
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
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(accentColor.copy(alpha = 0.18f)),
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

                // Glowing indicator dot
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                        .shadow(4.dp, CircleShape, ambientColor = accentColor, spotColor = accentColor)
                )
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
        Triple(DrawerDestination.BATCH_SCALING, Icons.Default.Scale, "Batch"),
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
                .spatialGlass(
                    shape = RoundedCornerShape(26.dp),
                    elevation = 12.dp,
                    borderAlpha = if (isDark) 0.45f else 0.30f,
                    specularHighlight = true,
                    isDark = isDark,
                    accentColor = primaryColor
                )
                .background(
                    Brush.verticalGradient(
                        listOf(
                            skin.surface.copy(alpha = if (isDark) 0.88f else 0.94f),
                            skin.surfaceVariant.copy(alpha = if (isDark) 0.75f else 0.85f)
                        )
                    )
                )
                .padding(horizontal = 8.dp, vertical = 6.dp)
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
                    .clip(CircleShape)
                    .background(skin.surfaceVariant.copy(alpha = 0.4f))
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

                val itemBgBrush = if (isSelected) {
                    Brush.linearGradient(
                        listOf(primaryColor.copy(alpha = 0.28f), primaryColor.copy(alpha = 0.12f))
                    )
                } else {
                    Brush.linearGradient(
                        listOf(Color.Transparent, Color.Transparent)
                    )
                }

                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .clip(RoundedCornerShape(18.dp))
                        .background(itemBgBrush)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            HapticFeedbackHelper.performKeypadTap(context, view, HapticIntensity.LIGHT)
                            SoundFeedbackHelper.playClickSound(context, view, SoundIntensity.STANDARD)
                            onSelectDestination(dest)
                        }
                        .padding(horizontal = 10.dp, vertical = 7.dp)
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

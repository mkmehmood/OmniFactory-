package com.example.ui.components

import com.example.ui.theme.SoftColors
import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.util.HapticFeedbackHelper
import com.example.domain.util.SoundFeedbackHelper
import com.example.ui.theme.DigitalMonospaceFont
import com.example.ui.theme.HapticIntensity
import com.example.ui.theme.LocalAnimationIntensity
import com.example.ui.theme.LocalAppThemeSkin
import com.example.ui.theme.SoundIntensity

/**
 * Metadata representation of an app section item for the capsule navigation.
 */
data class SectionCapsuleItem(
    val destination: DrawerDestination,
    val title: String,
    val shortTitle: String,
    val subtitle: String,
    val categoryTag: String,
    val suiteAccent: Color,
    val icon: ImageVector,
    val testTag: String
)

val APP_SECTION_ITEMS = listOf(
    SectionCapsuleItem(
        destination = DrawerDestination.CALCULATOR,
        title = "Standard Calculator",
        shortTitle = "Calculator",
        subtitle = "Sequential math & tape history",
        categoryTag = "CORE MATH",
        suiteAccent = SoftColors.warning,
        icon = Icons.Default.Calculate,
        testTag = "drawer_item_calculator"
    ),
    SectionCapsuleItem(
        destination = DrawerDestination.LEDGER,
        title = "Daily Ledger / Cashbook",
        shortTitle = "Daily Ledger",
        subtitle = "Cash in hand, cash in & out audit",
        categoryTag = "FINANCE",
        suiteAccent = SoftColors.warning,
        icon = Icons.Default.AccountBalanceWallet,
        testTag = "drawer_item_ledger"
    ),
    SectionCapsuleItem(
        destination = DrawerDestination.EXPENSES,
        title = "Expenses (Business & Home)",
        shortTitle = "Expenses",
        subtitle = "Everyday business & home expense manager",
        categoryTag = "BUDGET",
        suiteAccent = SoftColors.warning,
        icon = Icons.Default.Receipt,
        testTag = "drawer_item_expenses"
    ),
    SectionCapsuleItem(
        destination = DrawerDestination.CONVERTER,
        title = "Units Converter",
        shortTitle = "Units Converter",
        subtitle = "11 dedicated conversion suites",
        categoryTag = "UNITS",
        suiteAccent = SoftColors.warning,
        icon = Icons.Default.SwapHoriz,
        testTag = "drawer_item_converter"
    ),
    SectionCapsuleItem(
        destination = DrawerDestination.SALES,
        title = "Sales Reconciliation",
        shortTitle = "Sales Audit",
        subtitle = "Reconciliation & commission audit",
        categoryTag = "AUDIT",
        suiteAccent = SoftColors.warning,
        icon = Icons.Default.ReceiptLong,
        testTag = "drawer_item_sales"
    ),
    SectionCapsuleItem(
        destination = DrawerDestination.MIXTURE_CALCULATOR,
        title = "Mixture Calculator",
        shortTitle = "Mixture Calc",
        subtitle = "Recalibrate, scale & batch",
        categoryTag = "CHEM/LAB",
        suiteAccent = SoftColors.warning,
        icon = Icons.Default.Science,
        testTag = "drawer_item_mixture_calculator"
    ),
    SectionCapsuleItem(
        destination = DrawerDestination.SETTINGS,
        title = "Settings & Customization",
        shortTitle = "Settings",
        subtitle = "Palettes, shapes, sounds & haptics",
        categoryTag = "SYSTEM",
        suiteAccent = SoftColors.warning,
        icon = Icons.Default.Settings,
        testTag = "drawer_item_settings"
    )
)

/**
 * Filter groups for rapid scanning in the up-to-date special UI navigation hub.
 */
enum class CapsuleFilterGroup(val label: String) {
    ALL("ALL (8)"),
    MATH("MATH (3)"),
    BUSINESS("BUSINESS (2)"),
    TOOLS("TOOLS (3)")
}

/**
 * High-fidelity interactive Top Capsule in the app bar.
 * Features spatial glass depth, glowing rim highlight, live breathing status LED,
 * tactile spring compression, and a rotating chevron indicator.
 */
@Composable
fun TopSectionCapsule(
    title: String,
    icon: ImageVector,
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val currentSkin = LocalAppThemeSkin.current

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Smooth tactile bounce scale on press
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "TopCapsulePressScale"
    )

    // Smooth 180° rotation of the chevron
    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 420f),
        label = "ChevronRotation"
    )

    val specularBorderBrush = Brush.linearGradient(
        colors = if (isExpanded) {
            listOf(
                Color.White.copy(alpha = 0.85f),
                primaryColor,
                primaryColor.copy(alpha = 0.70f),
                primaryColor.copy(alpha = 0.30f)
            )
        } else {
            listOf(
                if (isDark) Color.White.copy(alpha = 0.55f) else Color.White.copy(alpha = 0.95f),
                primaryColor.copy(alpha = 0.45f),
                if (isDark) currentSkin.outline.copy(alpha = 0.30f) else Color.White.copy(alpha = 0.60f),
                primaryColor.copy(alpha = 0.15f)
            )
        },
        start = Offset(0f, 0f),
        end = Offset(300f, 300f)
    )

    SoftSurface(
        onClick = {
            HapticFeedbackHelper.performKeypadTap(context, view, HapticIntensity.LIGHT)
            SoundFeedbackHelper.playClickSound(context, view, SoundIntensity.SOFT)
            onClick()
        },
        interactionSource = interactionSource,
        shape = RoundedCornerShape(percent = 50),
        color = if (isExpanded) {
            primaryColor.copy(alpha = if (isDark) 0.32f else 0.22f)
        } else if (isDark) {
            currentSkin.surface.copy(alpha = 0.75f)
        } else {
            currentSkin.surface.copy(alpha = 0.90f)
        },
        shadowElevation = if (isExpanded) 10.dp else 5.dp,
        modifier = modifier
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .testTag("top_section_capsule")
            .testTag("open_sidebar_button")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Glowing Orb Icon with micro breathing LED
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .softWell(CircleShape, tint = primaryColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(15.dp)
                )
            }

            Spacer(modifier = Modifier.width(7.dp))

            // Section title in crisp digital typography
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                fontSize = 14.5.sp,
                letterSpacing = 0.4.sp,
                color = if (isExpanded) primaryColor else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Mini live badge pill "HUB"
            SoftSurface(
                shape = RoundedCornerShape(percent = 50),
                color = primaryColor.copy(alpha = if (isExpanded) 0.35f else 0.16f)) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Micro glowing LED dot
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .softPlate(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "HUB",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = DigitalMonospaceFont,
                        color = primaryColor
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Rotating dropdown chevron
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse sections" else "Expand sections",
                tint = if (isExpanded) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(18.dp)
                    .rotate(chevronRotation)
            )
        }
    }
}

/**
 * Dropdown overlay revealing all navigation suites in special spatial capsule format.
 * Features:
 * - Ambient radial frosted backdrop
 * - Up-to-date spatial glass floating container with dual specular rim highlights
 * - Interactive filter chips (ALL, MATH, BUSINESS, TOOLS)
 * - Capsules dynamically sized progressively larger down the app
 * - Rich metadata: category tag, glowing suite orb, active state badge, micro-interactions
 * - VIP Signature capsule at the foundation ("Mahmood khan")
 */
@Composable
fun SectionCapsuleDropdownMenu(
    isOpen: Boolean,
    currentDestination: DrawerDestination,
    onSelectDestination: (DrawerDestination) -> Unit,
    onOpenConverterGrid: () -> Unit = {},
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val currentSkin = LocalAppThemeSkin.current
    val primaryColor = MaterialTheme.colorScheme.primary

    var selectedFilterGroup by remember { mutableStateOf(CapsuleFilterGroup.ALL) }

    val filteredItems = remember(selectedFilterGroup) {
        when (selectedFilterGroup) {
            CapsuleFilterGroup.ALL -> APP_SECTION_ITEMS
            CapsuleFilterGroup.MATH -> APP_SECTION_ITEMS.filter {
                it.destination in listOf(
                    DrawerDestination.CALCULATOR,
                    DrawerDestination.MIXTURE_CALCULATOR
                )
            }
            CapsuleFilterGroup.BUSINESS -> APP_SECTION_ITEMS.filter {
                it.destination in listOf(
                    DrawerDestination.LEDGER,
                    DrawerDestination.EXPENSES,
                    DrawerDestination.SALES
                )
            }
            CapsuleFilterGroup.TOOLS -> APP_SECTION_ITEMS.filter {
                it.destination in listOf(
                    DrawerDestination.CONVERTER,
                    DrawerDestination.SETTINGS
                )
            }
        }
    }

    AnimatedVisibility(
        visible = isOpen,
        enter = fadeIn(animationSpec = tween(200)) + expandVertically(
            animationSpec = spring(dampingRatio = 0.85f, stiffness = 360f),
            expandFrom = Alignment.Top
        ) + scaleIn(
            initialScale = 0.94f,
            animationSpec = spring(dampingRatio = 0.82f, stiffness = 380f)
        ),
        exit = fadeOut(animationSpec = tween(150)) + shrinkVertically(
            animationSpec = tween(180),
            shrinkTowards = Alignment.Top
        ) + scaleOut(
            targetScale = 0.95f,
            animationSpec = tween(150)
        ),
        modifier = modifier.fillMaxSize()
    ) {
        // Scrim backdrop with luminous ambient radial aura
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = if (isDark) 0.22f else 0.12f),
                            Color.Black.copy(alpha = if (isDark) 0.72f else 0.55f),
                            Color.Black.copy(alpha = if (isDark) 0.85f else 0.70f)
                        ),
                        center = Offset(500f, 100f),
                        radius = 1200f
                    )
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            // Spatial Glass Floating Hub Panel
            SoftSurface(
                shape = RoundedCornerShape(32.dp),
                shadowElevation = 24.dp,
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .fillMaxWidth()
                    .heightIn(max = 610.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { /* consume taps inside panel */ }
                    )
                    .testTag("section_capsule_dropdown_panel")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    // Special Header with Pulsing Beacon & Quick Close
                    SpatialNavigationHubHeader(
                        primaryColor = primaryColor,
                        onDismiss = onDismiss
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Rapid Category Filter Chips Row
                    FilterChipsRow(
                        selectedGroup = selectedFilterGroup,
                        onSelectGroup = { selectedFilterGroup = it },
                        primaryColor = primaryColor,
                        isDark = isDark
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Scrollable column containing capsules increasing in size down the app
                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        filteredItems.forEachIndexed { index, item ->
                            val isSelected = currentDestination == item.destination

                            // Dynamic progressive sizing: size INCREASES down the app!
                            // Height scales smoothly from 50dp up to 78dp
                            val capsuleHeight = (50 + (index * 3.8f)).dp
                            // Width fraction expands from 0.84f to 1.0f
                            val widthFraction = (0.84f + (index * 0.024f)).coerceAtMost(1.0f)
                            // Icon container scales from 32dp to 46dp
                            val iconBoxSize = (32 + (index * 1.8f)).dp
                            // Icon vector scales from 17dp to 24dp
                            val iconSize = (17 + (index * 1.0f)).dp
                            // Title typography scales from 13.5sp to 16.8sp
                            val titleFontSize = (13.5f + (index * 0.42f)).sp
                            val subtitleFontSize = (10.0f + (index * 0.30f)).sp
                            val horizontalPadding = (12 + (index * 1.2f)).dp
                            val elevation = (3 + (index * 0.8f)).dp

                            SpecialUiCapsuleItem(
                                item = item,
                                index = index,
                                isSelected = isSelected,
                                height = capsuleHeight,
                                widthFraction = widthFraction,
                                iconBoxSize = iconBoxSize,
                                iconSize = iconSize,
                                titleFontSize = titleFontSize,
                                subtitleFontSize = subtitleFontSize,
                                horizontalPadding = horizontalPadding,
                                elevation = elevation,
                                primaryColor = primaryColor,
                                isDark = isDark,
                                onClick = {
                                    if (item.destination == DrawerDestination.CONVERTER) {
                                        onOpenConverterGrid()
                                    }
                                    onSelectDestination(item.destination)
                                    onDismiss()
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // Bottom VIP Foundation Capsule: "Mahmood khan" Edition
                        SpecialUiMahmoodKhanFooterCapsule(
                            primaryColor = primaryColor,
                            isDark = isDark
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

/**
 * Top header of the Navigation Hub with pulsating beacon, suite status, and close action.
 */
@Composable
private fun SpatialNavigationHubHeader(
    primaryColor: Color,
    onDismiss: () -> Unit
) {
    val currentSkin = LocalAppThemeSkin.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Navigation Hub Title Pill
        SoftSurface(
            shape = RoundedCornerShape(percent = 50),
            color = primaryColor.copy(alpha = 0.14f)) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Glowing Emerald/Cyan Beacon Dot
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .softPlate(CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "SPATIAL NAVIGATION HUB",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont,
                    letterSpacing = 1.2.sp,
                    color = primaryColor
                )
            }
        }

        // Close button with circular frosted glass
        SoftIconButton(
            onClick = onDismiss,
            modifier = Modifier
                .size(32.dp)
                .testTag("close_dropdown_button")
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close Section Menu",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Filter chips row enabling swift scanning of sections.
 */
@Composable
private fun FilterChipsRow(
    selectedGroup: CapsuleFilterGroup,
    onSelectGroup: (CapsuleFilterGroup) -> Unit,
    primaryColor: Color,
    isDark: Boolean
) {
    val currentSkin = LocalAppThemeSkin.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CapsuleFilterGroup.values().forEach { group ->
            val isSelected = selectedGroup == group
            val dottedColor = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isDark) 0.40f else 0.25f)
            SoftSurface(
                onClick = { onSelectGroup(group) },
                shape = RectangleShape,
                color = Color.Transparent,
                modifier = Modifier
                    .weight(1f)
            ) {
                Box(
                    modifier = Modifier.padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = group.label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontFamily = DigitalMonospaceFont,
                        color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * High-fidelity spatial capsule item whose visual scale increases progressively down the app.
 * Incorporates:
 * - 100% capsule/pill stadium silhouette
 * - Dual specular light reflection rim
 * - Multi-stop ambient glass gradient fill
 * - Suite-specific neon icon orb
 * - Category pill & descriptive subtitle
 * - Active glowing pulse badge
 * - Tactile micro-compression spring physics
 */
@Composable
private fun SpecialUiCapsuleItem(
    item: SectionCapsuleItem,
    index: Int,
    isSelected: Boolean,
    height: Dp,
    widthFraction: Float,
    iconBoxSize: Dp,
    iconSize: Dp,
    titleFontSize: TextUnit,
    subtitleFontSize: TextUnit,
    horizontalPadding: Dp,
    elevation: Dp,
    primaryColor: Color,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val currentSkin = LocalAppThemeSkin.current

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Tactile micro-compression spring on press
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.965f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "CapsulePressScale"
    )

    // Specular border gradient with light reflection
    val borderBrush = if (isSelected) {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.90f),
                item.suiteAccent,
                item.suiteAccent.copy(alpha = 0.75f),
                item.suiteAccent.copy(alpha = 0.35f)
            ),
            start = Offset(0f, 0f),
            end = Offset(400f, 200f)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                if (isDark) Color.White.copy(alpha = 0.40f) else Color.White.copy(alpha = 0.85f),
                item.suiteAccent.copy(alpha = 0.45f),
                currentSkin.outline.copy(alpha = if (isDark) 0.25f else 0.18f),
                Color.Transparent
            ),
            start = Offset(0f, 0f),
            end = Offset(400f, 200f)
        )
    }

    // Layered glass background fill
    val backgroundBrush = if (isSelected) {
        Brush.horizontalGradient(
            listOf(
                item.suiteAccent.copy(alpha = if (isDark) 0.32f else 0.22f),
                primaryColor.copy(alpha = if (isDark) 0.20f else 0.14f),
                item.suiteAccent.copy(alpha = if (isDark) 0.10f else 0.06f)
            )
        )
    } else {
        Brush.horizontalGradient(
            listOf(
                currentSkin.surfaceVariant.copy(alpha = if (isDark) 0.55f else 0.65f),
                currentSkin.surface.copy(alpha = if (isDark) 0.40f else 0.50f)
            )
        )
    }

    val capsuleShape = RoundedCornerShape(percent = 50)

    SoftSurface(
        onClick = {
            HapticFeedbackHelper.performKeypadTap(context, view, HapticIntensity.MEDIUM)
            SoundFeedbackHelper.playClickSound(context, view, SoundIntensity.STANDARD)
            onClick()
        },
        interactionSource = interactionSource,
        shape = capsuleShape,
        color = Color.Transparent,
        shadowElevation = elevation,
        modifier = Modifier
            .fillMaxWidth(fraction = widthFraction)
            .height(height)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .testTag(item.testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left block: Suite Neon Orb + Title & Subtitle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Circular Glowing Orb Icon Box
                Box(
                    modifier = Modifier
                        .size(iconBoxSize)
                        .softWell(CircleShape, tint = item.suiteAccent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = if (isSelected) item.suiteAccent else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(iconSize)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Title + Category Tag & Subtitle
                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = item.title,
                            fontSize = titleFontSize,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            fontFamily = DigitalMonospaceFont,
                            color = if (isSelected) item.suiteAccent else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Mini Category Pill Tag
                        SoftSurface(
                            shape = RoundedCornerShape(percent = 50),
                            color = item.suiteAccent.copy(alpha = 0.15f)) {
                            Text(
                                text = item.categoryTag,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = DigitalMonospaceFont,
                                color = item.suiteAccent,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Text(
                        text = item.subtitle,
                        fontSize = subtitleFontSize,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.88f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right block: Dynamic Status Pill (ACTIVE vs Arrow Jump)
            if (isSelected) {
                SoftSurface(
                    shape = RoundedCornerShape(percent = 50),
                    color = item.suiteAccent,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Active section",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.5.dp))
                        Text(
                            text = "ACTIVE",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = DigitalMonospaceFont,
                            color = Color.White
                        )
                    }
                }
            } else {
                // Subtle glass circular forward arrow
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .softPlate(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Navigate",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f),
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}

/**
 * Bottom-most foundation capsule: "Mahmood khan" Special UI Masterpiece.
 * Styled with iridescent metallic borders, sparkling crown/star badge, and high-fidelity branding.
 */
@Composable
private fun SpecialUiMahmoodKhanFooterCapsule(
    primaryColor: Color,
    isDark: Boolean
) {
    val currentSkin = LocalAppThemeSkin.current
    val capsuleShape = RoundedCornerShape(percent = 50)

    val iridescentBrush = Brush.linearGradient(
        colors = listOf(
            SoftColors.gold, // Gold
            SoftColors.sky,
            SoftColors.violet,
            SoftColors.gold  // Gold
        ),
        start = Offset(0f, 0f),
        end = Offset(600f, 200f)
    )

    SoftSurface(
        shape = capsuleShape,
        shadowElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth(1.0f)
            .height(82.dp)
            .testTag("capsule_footer_mahmood_khan")
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ornate Crest Box with Star & Palette
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .softWell(CircleShape, tint = SoftColors.gold),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Diamond,
                    contentDescription = null,
                    tint = SoftColors.gold,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Mahmood khan",
                        fontFamily = DigitalMonospaceFont,
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // PRO Tag
                    SoftSurface(
                        shape = RoundedCornerShape(percent = 50),
                        color = SoftColors.gold.copy(alpha = 0.20f)) {
                        Text(
                            text = "MASTER EDITION",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = DigitalMonospaceFont,
                            color = SoftColors.gold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = "Spatial Capsule Navigation • Continuous Scale Architecture",
                    fontSize = 10.5.sp,
                    fontFamily = DigitalMonospaceFont,
                    color = primaryColor.copy(alpha = 0.90f)
                )
            }
        }
    }
}

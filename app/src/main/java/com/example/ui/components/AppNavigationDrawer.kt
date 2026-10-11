package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.luminance
import com.example.domain.converter.ConverterCategory
import com.example.ui.theme.DigitalMonospaceFont
import com.example.ui.theme.LocalAppThemeSkin

enum class DrawerDestination {
    CALCULATOR,
    LEDGER,
    EXPENSES,
    SALES,
    MIXTURE_CALCULATOR,
    MIXTURE_HISTORY,
    CONVERTER,
    SETTINGS
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppNavigationDrawer(
    currentDestination: DrawerDestination,
    activeConverterCategory: ConverterCategory? = null,
    onSelectDestination: (DrawerDestination) -> Unit,
    onOpenConverterGrid: () -> Unit = {},
    onSelectConverterCategory: (ConverterCategory) -> Unit = {},
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val currentSkin = LocalAppThemeSkin.current
    val primaryColor = MaterialTheme.colorScheme.primary

    ModalDrawerSheet(
        drawerContainerColor = currentSkin.surface,
        drawerShape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp),
        modifier = modifier
            .width(320.dp)
            .fillMaxHeight()
            .testTag("app_navigation_drawer")
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 16.dp, vertical = 18.dp)
        ) {
            // Header with App Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(primaryColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "CALCULATOR",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        fontFamily = DigitalMonospaceFont,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Precision, Conversions & Audit",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = primaryColor
                    )
                }
            }

            HorizontalDivider(
                thickness = 1.dp,
                color = currentSkin.outline.copy(alpha = if (isDark) 0.30f else 0.20f),
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // Scrollable List for Destinations
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "CORE TOOLS & CALCULATORS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        fontFamily = DigitalMonospaceFont,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }

                // 1. Standard Calculator Option
                item {
                    DrawerNavItem(
                        label = "Standard Calculator",
                        subtitle = "Standard math & history tape",
                        icon = Icons.Default.Calculate,
                        isSelected = currentDestination == DrawerDestination.CALCULATOR,
                        onClick = { onSelectDestination(DrawerDestination.CALCULATOR) },
                        testTag = "drawer_item_calculator",
                        primaryColor = primaryColor,
                        isDark = isDark
                    )
                }

                // 2. Daily Ledger / Cashbook Option
                item {
                    DrawerNavItem(
                        label = "Daily Ledger / Cashbook",
                        subtitle = "Cash in hand, cash in & out audit",
                        icon = Icons.Default.AccountBalanceWallet,
                        isSelected = currentDestination == DrawerDestination.LEDGER,
                        onClick = { onSelectDestination(DrawerDestination.LEDGER) },
                        testTag = "drawer_item_ledger",
                        primaryColor = primaryColor,
                        isDark = isDark
                    )
                }

                // 3. Expenses (Business & Home)
                item {
                    DrawerNavItem(
                        label = "Expenses",
                        subtitle = "Everyday business & home expense manager",
                        icon = Icons.Default.Receipt,
                        isSelected = currentDestination == DrawerDestination.EXPENSES,
                        onClick = { onSelectDestination(DrawerDestination.EXPENSES) },
                        testTag = "drawer_item_expenses",
                        primaryColor = primaryColor,
                        isDark = isDark
                    )
                }

                // 4. Units Converter Option (Opens Categories Hub)
                item {
                    DrawerNavItem(
                        label = "Units Converter",
                        subtitle = "11 dedicated conversion suites",
                        icon = Icons.Default.SwapHoriz,
                        isSelected = currentDestination == DrawerDestination.CONVERTER,
                        onClick = {
                            onOpenConverterGrid()
                            onSelectDestination(DrawerDestination.CONVERTER)
                        },
                        testTag = "drawer_item_converter",
                        primaryColor = primaryColor,
                        isDark = isDark
                    )
                }

                // 5. Sales Audit Option
                item {
                    DrawerNavItem(
                        label = "Sales Audit",
                        subtitle = "Reconciliation & commission audit",
                        icon = Icons.Default.ReceiptLong,
                        isSelected = currentDestination == DrawerDestination.SALES,
                        onClick = { onSelectDestination(DrawerDestination.SALES) },
                        testTag = "drawer_item_sales",
                        primaryColor = primaryColor,
                        isDark = isDark
                    )
                }

                // 6. Mixture Calculator
                item {
                    DrawerNavItem(
                        label = "Mixture Calculator",
                        subtitle = "Recalibrate, scale & batch",
                        icon = Icons.Default.Science,
                        isSelected = currentDestination == DrawerDestination.MIXTURE_CALCULATOR,
                        onClick = { onSelectDestination(DrawerDestination.MIXTURE_CALCULATOR) },
                        testTag = "drawer_item_mixture_calculator",
                        primaryColor = primaryColor,
                        isDark = isDark
                    )
                }

                item {
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = if (isDark) Color(0x22FFFFFF) else Color(0x22000000),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                // Settings Option
                item {
                    DrawerNavItem(
                        label = "Settings",
                        subtitle = "Palettes, shapes, sounds & haptics",
                        icon = Icons.Default.Settings,
                        isSelected = currentDestination == DrawerDestination.SETTINGS,
                        onClick = { onSelectDestination(DrawerDestination.SETTINGS) },
                        testTag = "drawer_item_settings",
                        primaryColor = primaryColor,
                        isDark = isDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer Panel - Mahmood khan anchored at the bottom of the sidebar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                .border(
                    1.dp,
                    currentSkin.outline.copy(alpha = if (isDark) 0.35f else 0.20f),
                    RoundedCornerShape(16.dp)
                )
                .testTag("sidebar_footer_mahmood_khan"),
                color = currentSkin.surfaceVariant.copy(alpha = if (isDark) 0.5f else 0.6f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Mahmood khan",
                        fontFamily = DigitalMonospaceFont,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DrawerNavItem(
    label: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    primaryColor: Color,
    isDark: Boolean
) {
    val currentSkin = LocalAppThemeSkin.current
    val containerBg = if (isSelected) {
        primaryColor.copy(alpha = 0.16f)
    } else {
        currentSkin.surfaceVariant.copy(alpha = if (isDark) 0.4f else 0.5f)
    }
    val borderColor = if (isSelected) {
        primaryColor.copy(alpha = 0.55f)
    } else {
        currentSkin.outline.copy(alpha = if (isDark) 0.25f else 0.15f)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        color = containerBg
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) primaryColor.copy(alpha = 0.25f) else (if (isDark) Color(0x18FFFFFF) else Color(0x12000000))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = label,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

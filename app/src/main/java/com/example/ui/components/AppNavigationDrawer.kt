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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.converter.ConverterCategory
import com.example.ui.theme.DigitalMonospaceFont

enum class DrawerDestination {
    CALCULATOR,
    SALES,
    MIXTURE_CALCULATOR,
    BATCH_SCALING,
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
    val isDark = MaterialTheme.colorScheme.background.red < 0.2f
    val primaryColor = MaterialTheme.colorScheme.primary

    ModalDrawerSheet(
        drawerContainerColor = if (isDark) Color(0xFF090E17) else Color(0xFFF1F5F9),
        drawerShape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp),
        modifier = modifier
            .width(320.dp)
            .fillMaxHeight()
            .testTag("app_navigation_drawer")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header with App Title and Close button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
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

                    IconButton(
                        onClick = onCloseDrawer,
                        modifier = Modifier.testTag("close_drawer_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Menu",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                HorizontalDivider(
                    thickness = 1.dp,
                    color = if (isDark) Color(0x22FFFFFF) else Color(0x22000000),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            // PRIMARY CALCULATORS & CONVERTER TOOLS SECTION
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
                    subtitle = "Sequential math & history tape",
                    icon = Icons.Default.Calculate,
                    isSelected = currentDestination == DrawerDestination.CALCULATOR,
                    onClick = { onSelectDestination(DrawerDestination.CALCULATOR) },
                    testTag = "drawer_item_calculator",
                    primaryColor = primaryColor,
                    isDark = isDark
                )
            }

            // 2. Units Converter Option (Opens Categories Hub)
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

            // 3. Sales Audit Option
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

            // 4. Mixture Calculator
            item {
                DrawerNavItem(
                    label = "Mixture Calculator",
                    subtitle = "6-step ratio recalibration wizard",
                    icon = Icons.Default.Science,
                    isSelected = currentDestination == DrawerDestination.MIXTURE_CALCULATOR,
                    onClick = { onSelectDestination(DrawerDestination.MIXTURE_CALCULATOR) },
                    testTag = "drawer_item_mixture_calculator",
                    primaryColor = primaryColor,
                    isDark = isDark
                )
            }

            // 5. Batch Scaling
            item {
                DrawerNavItem(
                    label = "Batch & Ratio Scaler",
                    subtitle = "Proportional recipes & locking",
                    icon = Icons.Default.Scale,
                    isSelected = currentDestination == DrawerDestination.BATCH_SCALING,
                    onClick = { onSelectDestination(DrawerDestination.BATCH_SCALING) },
                    testTag = "drawer_item_batch_scaling",
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

            // Footer Panel
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            1.dp,
                            if (isDark) Color(0x22FFFFFF) else Color(0x22000000),
                            RoundedCornerShape(16.dp)
                        ),
                    color = if (isDark) Color(0x14FFFFFF) else Color(0x10000000),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Mahmood khan",
                            fontFamily = DigitalMonospaceFont,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
    val containerBg = if (isSelected) {
        primaryColor.copy(alpha = 0.16f)
    } else {
        if (isDark) Color(0x0EFFFFFF) else Color(0x08000000)
    }
    val borderColor = if (isSelected) {
        primaryColor.copy(alpha = 0.45f)
    } else {
        if (isDark) Color(0x14FFFFFF) else Color(0x10000000)
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

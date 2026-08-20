package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DigitalMonospaceFont

enum class DrawerDestination {
    CALCULATOR,
    SALES,
    MIXTURE_CALCULATOR,
    BATCH_SCALING,
    HISTORY,
    SETTINGS
}

@Composable
fun AppNavigationDrawer(
    currentDestination: DrawerDestination,
    historyCount: Int,
    onSelectDestination: (DrawerDestination) -> Unit,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.2f
    val primaryColor = MaterialTheme.colorScheme.primary

    ModalDrawerSheet(
        drawerContainerColor = if (isDark) Color(0xFF090E17) else Color(0xFFF1F5F9),
        drawerShape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp),
        modifier = modifier
            .width(300.dp)
            .fillMaxHeight()
            .testTag("app_navigation_drawer")
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // Header with App Title and Close button
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
                                text = "Precision & Audit Suite",
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

                Spacer(modifier = Modifier.height(20.dp))

                HorizontalDivider(
                    thickness = 1.dp,
                    color = if (isDark) Color(0x22FFFFFF) else Color(0x22000000)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "CALCULATORS & TOOLS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    fontFamily = DigitalMonospaceFont,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // 1. Calculator Option
                DrawerNavItem(
                    label = "Standard Calculator",
                    subtitle = "Sequential series math",
                    icon = Icons.Default.Calculate,
                    isSelected = currentDestination == DrawerDestination.CALCULATOR,
                    onClick = { onSelectDestination(DrawerDestination.CALCULATOR) },
                    testTag = "drawer_item_calculator",
                    primaryColor = primaryColor,
                    isDark = isDark
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 2. Sales Reconciliation Option
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

                Spacer(modifier = Modifier.height(8.dp))

                // 3. Mixture Calculator (Ratio Recalibration Tool)
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

                Spacer(modifier = Modifier.height(8.dp))

                // 4. Batch Scaling & Proportional Ratio Scaler
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

                Spacer(modifier = Modifier.height(8.dp))

                // 5. History Option
                DrawerNavItem(
                    label = "History Tape",
                    subtitle = if (historyCount > 0) "$historyCount saved calculations" else "View past calculations",
                    icon = Icons.Default.History,
                    badgeCount = historyCount,
                    isSelected = currentDestination == DrawerDestination.HISTORY,
                    onClick = { onSelectDestination(DrawerDestination.HISTORY) },
                    testTag = "drawer_item_history",
                    primaryColor = primaryColor,
                    isDark = isDark
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 6. Settings Option
                DrawerNavItem(
                    label = "Settings",
                    subtitle = "Theme, accents & haptics",
                    icon = Icons.Default.Settings,
                    isSelected = currentDestination == DrawerDestination.SETTINGS,
                    onClick = { onSelectDestination(DrawerDestination.SETTINGS) },
                    testTag = "drawer_item_settings",
                    primaryColor = primaryColor,
                    isDark = isDark
                )
            }

            // Footer Panel
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
                        text = "Calculator • Offline & Secure",
                        fontFamily = DigitalMonospaceFont,
                        fontSize = 12.sp,
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
    isDark: Boolean,
    badgeCount: Int = 0
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
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        color = containerBg
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) primaryColor.copy(alpha = 0.25f) else (if (isDark) Color(0x18FFFFFF) else Color(0x12000000))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = label,
                        fontSize = 14.sp,
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

            if (badgeCount > 0) {
                Surface(
                    shape = CircleShape,
                    color = primaryColor.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "$badgeCount",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DigitalMonospaceFont,
                        color = primaryColor,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

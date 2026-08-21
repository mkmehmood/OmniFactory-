package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PanoramaFishEye
import androidx.compose.material.icons.filled.RoundedCorner
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentTheme
import com.example.ui.theme.DigitalMonospaceFont
import com.example.ui.theme.LocalShapeStructure
import com.example.ui.theme.ShapeStructureStyle
import com.example.ui.theme.ThemeMode
import com.example.ui.theme.toButtonShape
import com.example.ui.theme.toCardShape

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    currentThemeMode: ThemeMode,
    currentAccent: AccentTheme,
    currentShapeStructure: ShapeStructureStyle = ShapeStructureStyle.ROUNDED,
    showDoubleZeroKey: Boolean,
    enableAnimations: Boolean,
    hapticEnabled: Boolean,
    soundEnabled: Boolean = true,
    onThemeModeChange: (ThemeMode) -> Unit,
    onAccentChange: (AccentTheme) -> Unit,
    onShapeStructureChange: (ShapeStructureStyle) -> Unit = {},
    onToggleDoubleZero: (Boolean) -> Unit,
    onToggleAnimations: (Boolean) -> Unit,
    onHapticToggle: (Boolean) -> Unit,
    onSoundToggle: (Boolean) -> Unit = {},
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.2f
    val primaryColor = MaterialTheme.colorScheme.primary
    val shapeStructure = LocalShapeStructure.current

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("settings_screen"),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(primaryColor.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Settings",
                            fontFamily = DigitalMonospaceFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Section 1: Interactive Color Palette Selector
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Color Palette Theme",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Select from 10 distinct high-contrast neon glass accents",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                maxItemsInEachRow = 2
            ) {
                AccentTheme.entries.forEach { accent ->
                    val isSelected = currentAccent == accent
                    val accentColor = if (isDark) accent.darkPrimary else accent.lightPrimary

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(shapeStructure.toCardShape())
                            .clickable { onAccentChange(accent) }
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) accentColor else (if (isDark) Color(0x33FFFFFF) else Color(0x18000000)),
                                shape = shapeStructure.toCardShape()
                            )
                            .testTag("palette_option_${accent.name.lowercase()}"),
                        shape = shapeStructure.toCardShape(),
                        color = if (isSelected) accentColor.copy(alpha = 0.18f) else if (isDark) Color(0x1AFFFFFF) else Color(0x0C000000)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Color gradient circle
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Brush.linearGradient(accent.previewGradient))
                                        .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                                )

                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(accentColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = if (isDark) Color.Black else Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }

                            Column {
                                Text(
                                    text = accent.displayName,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = accent.description,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 13.sp
                                )
                            }

                            // Live miniature keypad preview bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(20.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isDark) Color(0x28FFFFFF) else Color(0x18000000)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("7", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(20.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(accentColor.copy(alpha = 0.25f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("+", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = accentColor)
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(20.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(accentColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("=", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.Black else Color.White)
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = primaryColor.copy(alpha = 0.2f))

            // Section 2: Appearance & Theme Mode (Dark Liquid / Light Frosted / System)
            Text(
                text = "Appearance & Surface Mode",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LiquidThemeCard(
                    title = "Dark Glass",
                    icon = Icons.Default.DarkMode,
                    isSelected = currentThemeMode == ThemeMode.DARK,
                    onClick = { onThemeModeChange(ThemeMode.DARK) },
                    modifier = Modifier.weight(1f)
                )
                LiquidThemeCard(
                    title = "Light Glass",
                    icon = Icons.Default.LightMode,
                    isSelected = currentThemeMode == ThemeMode.LIGHT,
                    onClick = { onThemeModeChange(ThemeMode.LIGHT) },
                    modifier = Modifier.weight(1f)
                )
                LiquidThemeCard(
                    title = "System",
                    icon = Icons.Default.Smartphone,
                    isSelected = currentThemeMode == ThemeMode.SYSTEM,
                    onClick = { onThemeModeChange(ThemeMode.SYSTEM) },
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider(color = primaryColor.copy(alpha = 0.2f))

            // Section 3: Button & Input Fields Geometry / Structure
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Keypad, Buttons & Input Geometry",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Choose the corner structure for calculator keys, action buttons, cards, and input fields across the app",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ShapeStructureCard(
                    title = "Rounded Squircle",
                    subtitle = "Smooth 18dp rounded keys & 12dp input fields",
                    icon = Icons.Default.RoundedCorner,
                    isSelected = currentShapeStructure == ShapeStructureStyle.ROUNDED,
                    onClick = { onShapeStructureChange(ShapeStructureStyle.ROUNDED) }
                )
                ShapeStructureCard(
                    title = "Pill / Capsule",
                    subtitle = "Continuous pill oval geometry on buttons & inputs",
                    icon = Icons.Default.PanoramaFishEye,
                    isSelected = currentShapeStructure == ShapeStructureStyle.PILL,
                    onClick = { onShapeStructureChange(ShapeStructureStyle.PILL) }
                )
                ShapeStructureCard(
                    title = "Line / Minimal Border",
                    subtitle = "Architectural line borders with subtle radius",
                    icon = Icons.Default.LinearScale,
                    isSelected = currentShapeStructure == ShapeStructureStyle.LINE_BORDER,
                    onClick = { onShapeStructureChange(ShapeStructureStyle.LINE_BORDER) }
                )
                ShapeStructureCard(
                    title = "Geometric Modern",
                    subtitle = "Sharp crisp beveled rectangular keys & inputs",
                    icon = Icons.Default.CropSquare,
                    isSelected = currentShapeStructure == ShapeStructureStyle.SHARP,
                    onClick = { onShapeStructureChange(ShapeStructureStyle.SHARP) }
                )
            }

            HorizontalDivider(color = primaryColor.copy(alpha = 0.2f))

            // Section 4: Keypad & Sound / Haptic Controls
            Text(
                text = "Keypad, Sound & Feedback Controls",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = shapeStructure.toCardShape(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0x18FFFFFF) else Color(0x0A000000)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isDark) Color(0x28FFFFFF) else Color(0x18000000)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Click Sound Option
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Keypad Click Sound", fontWeight = FontWeight.Medium)
                                Text(
                                    "Play crisp tactile audio clicks on calculator key presses",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = soundEnabled,
                            onCheckedChange = onSoundToggle,
                            colors = SwitchDefaults.colors(checkedThumbColor = primaryColor),
                            modifier = Modifier.testTag("sound_switch")
                        )
                    }

                    HorizontalDivider(color = if (isDark) Color(0x1AFFFFFF) else Color(0x10000000))

                    // Haptic Vibration Feedback
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Vibration,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Tactile Haptic Vibration", fontWeight = FontWeight.Medium)
                                Text(
                                    "Direct hardware vibrator click and touch feedback on every tap",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = hapticEnabled,
                            onCheckedChange = onHapticToggle,
                            colors = SwitchDefaults.colors(checkedThumbColor = primaryColor),
                            modifier = Modifier.testTag("haptic_switch")
                        )
                    }

                    HorizontalDivider(color = if (isDark) Color(0x1AFFFFFF) else Color(0x10000000))

                    // Fluid Tap Animations
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Fluid Tap Physics & Movement", fontWeight = FontWeight.Medium)
                            Text(
                                "Responsive spring compression and bouncy physics on key press",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = enableAnimations,
                            onCheckedChange = onToggleAnimations,
                            colors = SwitchDefaults.colors(checkedThumbColor = primaryColor),
                            modifier = Modifier.testTag("animations_switch")
                        )
                    }

                    HorizontalDivider(color = if (isDark) Color(0x1AFFFFFF) else Color(0x10000000))

                    // Dedicated '00' Key
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Dedicated '00' Key", fontWeight = FontWeight.Medium)
                            Text(
                                "Fast double-zero entry for currency & calculations",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = showDoubleZeroKey,
                            onCheckedChange = onToggleDoubleZero,
                            colors = SwitchDefaults.colors(checkedThumbColor = primaryColor),
                            modifier = Modifier.testTag("double_zero_switch")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ShapeStructureCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val isDark = MaterialTheme.colorScheme.background.red < 0.2f
    val shape = when (title) {
        "Rounded Squircle" -> RoundedCornerShape(16.dp)
        "Pill / Capsule" -> RoundedCornerShape(24.dp)
        "Line / Minimal Border" -> RoundedCornerShape(8.dp)
        "Geometric Modern" -> RoundedCornerShape(4.dp)
        else -> RoundedCornerShape(14.dp)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable { onClick() }
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) primaryColor else (if (isDark) Color(0x33FFFFFF) else Color(0x20000000)),
                shape = shape
            )
            .testTag("shape_option_${title.lowercase().replace(" ", "_").replace("/", "_")}"),
        shape = shape,
        color = if (isSelected) primaryColor.copy(alpha = 0.18f) else if (isDark) Color(0x1AFFFFFF) else Color(0x10000000)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 14.sp,
                    color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = primaryColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun LiquidThemeCard(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val isDark = MaterialTheme.colorScheme.background.red < 0.2f
    val shape = LocalShapeStructure.current.toCardShape()

    Surface(
        modifier = modifier
            .clip(shape)
            .clickable { onClick() }
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        if (isSelected) primaryColor else (if (isDark) Color.White else Color.Black).copy(alpha = 0.2f),
                        Color.Transparent
                    )
                ),
                shape = shape
            )
            .testTag("theme_card_${title.lowercase().replace(" ", "_")}"),
        shape = shape,
        color = if (isSelected) primaryColor.copy(alpha = 0.2f) else if (isDark) Color(0x26FFFFFF) else Color(0x33000000)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DensityMedium
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PanoramaFishEye
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RoundedCorner
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.util.HapticFeedbackHelper
import com.example.domain.util.SoundFeedbackHelper
import com.example.ui.theme.AccentTheme
import com.example.ui.theme.AnimationIntensity
import com.example.ui.theme.ComponentSizeStyle
import com.example.ui.theme.DigitalMonospaceFont
import com.example.ui.theme.HapticIntensity
import com.example.ui.theme.LocalShapeStructure
import com.example.ui.theme.ShapeStructureStyle
import com.example.ui.theme.SoundIntensity
import com.example.ui.theme.ThemeMode
import com.example.ui.theme.toCardShape
import com.example.ui.theme.toInputShape
import com.example.ui.theme.toSmallShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentThemeMode: ThemeMode,
    currentAccent: AccentTheme,
    currentShapeStructure: ShapeStructureStyle = ShapeStructureStyle.ROUNDED,
    currentComponentSize: ComponentSizeStyle = ComponentSizeStyle.STANDARD,
    currentAnimationIntensity: AnimationIntensity = AnimationIntensity.BALANCED,
    currentHapticIntensity: HapticIntensity = HapticIntensity.MEDIUM,
    currentSoundIntensity: SoundIntensity = SoundIntensity.STANDARD,
    showDoubleZeroKey: Boolean,
    onThemeModeChange: (ThemeMode) -> Unit,
    onAccentChange: (AccentTheme) -> Unit,
    onShapeStructureChange: (ShapeStructureStyle) -> Unit = {},
    onComponentSizeChange: (ComponentSizeStyle) -> Unit = {},
    onAnimationIntensityChange: (AnimationIntensity) -> Unit = {},
    onHapticIntensityChange: (HapticIntensity) -> Unit = {},
    onSoundIntensityChange: (SoundIntensity) -> Unit = {},
    onToggleDoubleZero: (Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val isDark = MaterialTheme.colorScheme.background.red < 0.2f
    val primaryColor = MaterialTheme.colorScheme.primary
    val shapeStructure = LocalShapeStructure.current

    var colorDropdownExpanded by remember { mutableStateOf(false) }
    var shapeDropdownExpanded by remember { mutableStateOf(false) }
    var sizeDropdownExpanded by remember { mutableStateOf(false) }
    var animDropdownExpanded by remember { mutableStateOf(false) }
    var hapticDropdownExpanded by remember { mutableStateOf(false) }
    var soundDropdownExpanded by remember { mutableStateOf(false) }

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
            // ==========================================
            // SECTION 1: Color Palette Scheme Dropdown
            // ==========================================
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Color Palette Scheme",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Select accent glowing glass colors from the dropdown",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Box(modifier = Modifier.fillMaxWidth().testTag("color_scheme_dropdown_box")) {
                    GlassDropdownTrigger(
                        isExpanded = colorDropdownExpanded,
                        onClick = { colorDropdownExpanded = !colorDropdownExpanded },
                        testTag = "color_scheme_dropdown_trigger",
                        leadingContent = {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(currentAccent.previewGradient))
                                    .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                            )
                        },
                        title = currentAccent.displayName,
                        subtitle = currentAccent.description
                    )

                    DropdownMenu(
                        expanded = colorDropdownExpanded,
                        onDismissRequest = { colorDropdownExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .background(if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC))
                            .border(1.dp, primaryColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .testTag("color_scheme_dropdown_menu")
                    ) {
                        AccentTheme.entries.forEach { accent ->
                            val isSelected = currentAccent == accent
                            val accentColor = if (isDark) accent.darkPrimary else accent.lightPrimary

                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(Brush.linearGradient(accent.previewGradient))
                                                .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = accent.displayName,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 13.sp,
                                                color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = accent.description,
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = accentColor,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    onAccentChange(accent)
                                    colorDropdownExpanded = false
                                },
                                modifier = Modifier.testTag("color_item_${accent.name.lowercase()}")
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = primaryColor.copy(alpha = 0.2f))

            // ==========================================
            // SECTION 2: Keypad & Shapes Dropdown
            // ==========================================
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Keypad & Component Shapes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Choose from 8 distinct geometric shapes for buttons, cards, and input fields",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Box(modifier = Modifier.fillMaxWidth().testTag("shape_structure_dropdown_box")) {
                    val currentShapeIcon = getShapeIcon(currentShapeStructure)

                    GlassDropdownTrigger(
                        isExpanded = shapeDropdownExpanded,
                        onClick = { shapeDropdownExpanded = !shapeDropdownExpanded },
                        testTag = "shape_structure_dropdown_trigger",
                        leadingContent = {
                            Icon(
                                imageVector = currentShapeIcon,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        title = currentShapeStructure.displayName,
                        subtitle = currentShapeStructure.description
                    )

                    DropdownMenu(
                        expanded = shapeDropdownExpanded,
                        onDismissRequest = { shapeDropdownExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .background(if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC))
                            .border(1.dp, primaryColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .testTag("shape_structure_dropdown_menu")
                    ) {
                        ShapeStructureStyle.entries.forEach { shapeStyle ->
                            val isSelected = currentShapeStructure == shapeStyle
                            val icon = getShapeIcon(shapeStyle)

                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = shapeStyle.displayName,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 13.sp,
                                                color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = shapeStyle.description,
                                                fontSize = 10.sp,
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
                                },
                                onClick = {
                                    onShapeStructureChange(shapeStyle)
                                    shapeDropdownExpanded = false
                                },
                                modifier = Modifier.testTag("shape_item_${shapeStyle.name.lowercase()}")
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = primaryColor.copy(alpha = 0.2f))

            // ==========================================
            // SECTION 3: Component & Button Sizing Dropdown
            // ==========================================
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "App Sizing & Touch Target Scale",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Scale keypad buttons, cards, spacing, and input fields to your comfort",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Box(modifier = Modifier.fillMaxWidth().testTag("size_dropdown_box")) {
                    GlassDropdownTrigger(
                        isExpanded = sizeDropdownExpanded,
                        onClick = { sizeDropdownExpanded = !sizeDropdownExpanded },
                        testTag = "size_dropdown_trigger",
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Default.DensityMedium,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        title = currentComponentSize.displayName,
                        subtitle = currentComponentSize.description
                    )

                    DropdownMenu(
                        expanded = sizeDropdownExpanded,
                        onDismissRequest = { sizeDropdownExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .background(if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC))
                            .border(1.dp, primaryColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .testTag("size_dropdown_menu")
                    ) {
                        ComponentSizeStyle.entries.forEach { sizeStyle ->
                            val isSelected = currentComponentSize == sizeStyle

                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AspectRatio,
                                            contentDescription = null,
                                            tint = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = sizeStyle.displayName,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 13.sp,
                                                color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = sizeStyle.description,
                                                fontSize = 10.sp,
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
                                },
                                onClick = {
                                    onComponentSizeChange(sizeStyle)
                                    sizeDropdownExpanded = false
                                },
                                modifier = Modifier.testTag("size_item_${sizeStyle.name.lowercase()}")
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = primaryColor.copy(alpha = 0.2f))

            // ==========================================
            // SECTION 4: Appearance & Theme Mode
            // ==========================================
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

            // ==========================================
            // SECTION 5: Intensity Controllers (Physics, Haptics, Audio)
            // ==========================================
            Text(
                text = "Intensity & Feedback Controllers",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            // Controller 1: Fluid Tap Physics Intensity Dropdown
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Fluid Tap Physics Intensity", fontWeight = FontWeight.Medium)
                        Text(
                            "Spring elasticity & compression recoil on keypress",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box(modifier = Modifier.fillMaxWidth().testTag("anim_dropdown_box")) {
                    GlassDropdownTrigger(
                        isExpanded = animDropdownExpanded,
                        onClick = { animDropdownExpanded = !animDropdownExpanded },
                        testTag = "anim_dropdown_trigger",
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Default.Animation,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        title = currentAnimationIntensity.displayName,
                        subtitle = currentAnimationIntensity.description
                    )

                    DropdownMenu(
                        expanded = animDropdownExpanded,
                        onDismissRequest = { animDropdownExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .background(if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC))
                            .border(1.dp, primaryColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .testTag("anim_dropdown_menu")
                    ) {
                        AnimationIntensity.entries.forEach { anim ->
                            val isSelected = currentAnimationIntensity == anim

                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = anim.displayName,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 13.sp,
                                                color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = anim.description,
                                                fontSize = 10.sp,
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
                                },
                                onClick = {
                                    onAnimationIntensityChange(anim)
                                    animDropdownExpanded = false
                                },
                                modifier = Modifier.testTag("anim_item_${anim.name.lowercase()}")
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Controller 2: Haptic Feedback Intensity Dropdown + Test Pulse Button
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Haptic Feedback Intensity", fontWeight = FontWeight.Medium)
                        Text(
                            "Hardware vibrator pulse strength and crisp tactile clicks",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            HapticFeedbackHelper.performKeypadTap(context, view, currentHapticIntensity)
                        },
                        shape = shapeStructure.toSmallShape(),
                        border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryColor),
                        modifier = Modifier.testTag("test_haptic_button")
                    ) {
                        Icon(imageVector = Icons.Default.TouchApp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Test", fontSize = 12.sp)
                    }
                }

                Box(modifier = Modifier.fillMaxWidth().testTag("haptic_dropdown_box")) {
                    GlassDropdownTrigger(
                        isExpanded = hapticDropdownExpanded,
                        onClick = { hapticDropdownExpanded = !hapticDropdownExpanded },
                        testTag = "haptic_dropdown_trigger",
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Default.Vibration,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        title = currentHapticIntensity.displayName,
                        subtitle = currentHapticIntensity.description
                    )

                    DropdownMenu(
                        expanded = hapticDropdownExpanded,
                        onDismissRequest = { hapticDropdownExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .background(if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC))
                            .border(1.dp, primaryColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .testTag("haptic_dropdown_menu")
                    ) {
                        HapticIntensity.entries.forEach { haptic ->
                            val isSelected = currentHapticIntensity == haptic

                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = haptic.displayName,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 13.sp,
                                                color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = haptic.description,
                                                fontSize = 10.sp,
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
                                },
                                onClick = {
                                    onHapticIntensityChange(haptic)
                                    hapticDropdownExpanded = false
                                    // Trigger immediate sample feedback
                                    HapticFeedbackHelper.performKeypadTap(context, view, haptic)
                                },
                                modifier = Modifier.testTag("haptic_item_${haptic.name.lowercase()}")
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Controller 3: Sound Feedback Intensity Dropdown + Test Play Button (with Extreme Boost)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Sound Feedback Intensity", fontWeight = FontWeight.Medium)
                        Text(
                            "Audible acoustic key click volume (Extreme Boost clears freely & loudly)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            SoundFeedbackHelper.playClickSound(context, view, currentSoundIntensity)
                        },
                        shape = shapeStructure.toSmallShape(),
                        border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryColor),
                        modifier = Modifier.testTag("test_sound_button")
                    ) {
                        Icon(imageVector = Icons.Default.GraphicEq, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Play", fontSize = 12.sp)
                    }
                }

                Box(modifier = Modifier.fillMaxWidth().testTag("sound_dropdown_box")) {
                    GlassDropdownTrigger(
                        isExpanded = soundDropdownExpanded,
                        onClick = { soundDropdownExpanded = !soundDropdownExpanded },
                        testTag = "sound_dropdown_trigger",
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        title = currentSoundIntensity.displayName,
                        subtitle = currentSoundIntensity.description
                    )

                    DropdownMenu(
                        expanded = soundDropdownExpanded,
                        onDismissRequest = { soundDropdownExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .background(if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC))
                            .border(1.dp, primaryColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .testTag("sound_dropdown_menu")
                    ) {
                        SoundIntensity.entries.forEach { sound ->
                            val isSelected = currentSoundIntensity == sound

                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = sound.displayName,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 13.sp,
                                                color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = sound.description,
                                                fontSize = 10.sp,
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
                                },
                                onClick = {
                                    onSoundIntensityChange(sound)
                                    soundDropdownExpanded = false
                                    // Trigger immediate sample audio burst
                                    SoundFeedbackHelper.playClickSound(context, view, sound)
                                },
                                modifier = Modifier.testTag("sound_item_${sound.name.lowercase()}")
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = primaryColor.copy(alpha = 0.2f))

            // ==========================================
            // SECTION 6: Keypad Extra Options
            // ==========================================
            Text(
                text = "Keypad Extra Options",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = shapeStructure.toCardShape(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0x18FFFFFF) else Color(0x0A000000)
                ),
                border = BorderStroke(
                    1.dp,
                    if (isDark) Color(0x28FFFFFF) else Color(0x18000000)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Dedicated '00' Key
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Dedicated '00' Double Zero Key", fontWeight = FontWeight.Medium)
                            Text(
                                "Fast double-zero entry for currency & calculations (replaces +/- key)",
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

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun GlassDropdownTrigger(
    isExpanded: Boolean,
    onClick: () -> Unit,
    testTag: String,
    leadingContent: @Composable () -> Unit,
    title: String,
    subtitle: String
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.2f
    val primaryColor = MaterialTheme.colorScheme.primary
    val shapeStructure = LocalShapeStructure.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shapeStructure.toInputShape())
            .border(
                width = if (isExpanded) 1.5.dp else 1.dp,
                color = if (isExpanded) primaryColor else (if (isDark) Color(0x33FFFFFF) else Color(0x20000000)),
                shape = shapeStructure.toInputShape()
            )
            .clickable { onClick() }
            .testTag(testTag),
        shape = shapeStructure.toInputShape(),
        color = if (isDark) Color(0x1CFFFFFF) else Color(0x0C000000)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                leadingContent()
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                imageVector = if (isExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                contentDescription = "Toggle Dropdown",
                tint = primaryColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

private fun getShapeIcon(shape: ShapeStructureStyle): ImageVector {
    return when (shape) {
        ShapeStructureStyle.ROUNDED -> Icons.Default.RoundedCorner
        ShapeStructureStyle.PILL -> Icons.Default.PanoramaFishEye
        ShapeStructureStyle.LINE_BORDER -> Icons.Default.LinearScale
        ShapeStructureStyle.SHARP -> Icons.Default.CropSquare
        ShapeStructureStyle.CHAMFER_CUT -> Icons.Default.CropSquare
        ShapeStructureStyle.TEARDROP -> Icons.Default.PanoramaFishEye
        ShapeStructureStyle.OVAL_STADIUM -> Icons.Default.PanoramaFishEye
        ShapeStructureStyle.SOFT_BUBBLE -> Icons.Default.RoundedCorner
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

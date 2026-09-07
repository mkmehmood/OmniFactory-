package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Abc
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DensityLarge
import androidx.compose.material.icons.filled.DensityMedium
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PanoramaFishEye
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.RoundedCorner
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.ViewCompact
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.db.BackupValidationResult
import com.example.data.db.RestoreMode
import com.example.domain.util.HapticFeedbackHelper
import com.example.domain.util.SoundFeedbackHelper
import com.example.ui.BackupDialogType
import com.example.ui.SettingsViewModel
import com.example.ui.theme.AnimationIntensity
import com.example.ui.theme.AppFontStyle
import com.example.ui.theme.AppThemeSkin
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
    currentAppThemeSkin: AppThemeSkin = AppThemeSkin.DARK_CHARCOAL,
    currentThemeMode: ThemeMode,
    currentShapeStructure: ShapeStructureStyle = ShapeStructureStyle.ROUNDED,
    currentComponentSize: ComponentSizeStyle = ComponentSizeStyle.STANDARD,
    currentAnimationIntensity: AnimationIntensity = AnimationIntensity.BALANCED,
    currentFontStyle: AppFontStyle = AppFontStyle.SYSTEM,
    currentHapticIntensity: HapticIntensity = HapticIntensity.MEDIUM,
    currentSoundIntensity: SoundIntensity = SoundIntensity.STANDARD,
    showDoubleZeroKey: Boolean,
    onAppThemeSkinChange: (AppThemeSkin) -> Unit = {},
    onThemeModeChange: (ThemeMode) -> Unit,
    onShapeStructureChange: (ShapeStructureStyle) -> Unit = {},
    onComponentSizeChange: (ComponentSizeStyle) -> Unit = {},
    onAnimationIntensityChange: (AnimationIntensity) -> Unit = {},
    onFontStyleChange: (AppFontStyle) -> Unit = {},
    onHapticIntensityChange: (HapticIntensity) -> Unit = {},
    onSoundIntensityChange: (SoundIntensity) -> Unit = {},
    onToggleDoubleZero: (Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    settingsViewModel: SettingsViewModel = viewModel(),
    onPreferencesReload: () -> Unit = {},
    onCopyToClipboard: (String, String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val primaryColor = MaterialTheme.colorScheme.primary
    val shapeStructure = LocalShapeStructure.current
    val clipboardManager = LocalClipboardManager.current

    val settingsUiState by settingsViewModel.uiState.collectAsStateWithLifecycle()

    val saveFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            settingsViewModel.saveBackupToUri(context, uri)
        }
    }

    val pickFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            settingsViewModel.loadBackupFromUri(context, uri)
        }
    }

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
        containerColor = Color.Transparent
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
            // SECTION 1: Photo Theme Mode (Pure Light & Dark Charcoal)
            // ==========================================
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Photo Theme Appearance",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Pure Light & Dark Charcoal from the design mockup",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = primaryColor.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "PHOTO THEME",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LiquidThemeCard(
                        title = "Dark Charcoal",
                        icon = Icons.Default.DarkMode,
                        isSelected = currentThemeMode == ThemeMode.DARK,
                        onClick = {
                            onThemeModeChange(ThemeMode.DARK)
                            onAppThemeSkinChange(AppThemeSkin.DARK_CHARCOAL)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    LiquidThemeCard(
                        title = "Pure Light",
                        icon = Icons.Default.LightMode,
                        isSelected = currentThemeMode == ThemeMode.LIGHT,
                        onClick = {
                            onThemeModeChange(ThemeMode.LIGHT)
                            onAppThemeSkinChange(AppThemeSkin.PURE_LIGHT)
                        },
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

                Column(modifier = Modifier.fillMaxWidth().testTag("shape_structure_dropdown_box")) {
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

                    GlassExpandablePanel(
                        expanded = shapeDropdownExpanded,
                        maxHeight = 360.dp,
                        modifier = Modifier.testTag("shape_structure_dropdown_menu")
                    ) {
                        ShapeStructureStyle.entries.forEach { shapeStyle ->
                            val isSelected = currentShapeStructure == shapeStyle
                            val icon = getShapeIcon(shapeStyle)

                            GlassDropdownItem(
                                onClick = {
                                    onShapeStructureChange(shapeStyle)
                                    shapeDropdownExpanded = false
                                },
                                modifier = Modifier.testTag("shape_item_${shapeStyle.name.lowercase()}")
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
                        }
                    }
                }
            }

            HorizontalDivider(color = primaryColor.copy(alpha = 0.2f))

            // ==========================================
            // SECTION 4: Component & Button Sizing Dropdown
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

                Column(modifier = Modifier.fillMaxWidth().testTag("size_dropdown_box")) {
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

                    GlassExpandablePanel(
                        expanded = sizeDropdownExpanded,
                        maxHeight = 360.dp,
                        modifier = Modifier.testTag("size_dropdown_menu")
                    ) {
                        ComponentSizeStyle.entries.forEach { sizeStyle ->
                            val isSelected = currentComponentSize == sizeStyle

                            GlassDropdownItem(
                                onClick = {
                                    onComponentSizeChange(sizeStyle)
                                    sizeDropdownExpanded = false
                                },
                                modifier = Modifier.testTag("size_item_${sizeStyle.name.lowercase()}")
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
                        }
                    }
                }
            }

            HorizontalDivider(color = primaryColor.copy(alpha = 0.2f))

            // ==========================================
            // SECTION 4: Custom Begonia Font (Applied Everywhere)
            // ==========================================
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "App Typography",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Begonia custom font applied across all calculations, keypad & stations",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = primaryColor.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "BEGONIA FONT",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Surface(
                    shape = shapeStructure.toCardShape(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth().testTag("begonia_font_card")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(primaryColor.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TextFields,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Begonia Custom Font",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    fontFamily = DigitalMonospaceFont,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Zero system font fallback • 100% custom typography",
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "176 • 123,456 + 75% = 216,048",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = DigitalMonospaceFont,
                                color = primaryColor,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
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

                Column(modifier = Modifier.fillMaxWidth().testTag("anim_dropdown_box")) {
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

                    GlassExpandablePanel(
                        expanded = animDropdownExpanded,
                        maxHeight = 300.dp,
                        modifier = Modifier.testTag("anim_dropdown_menu")
                    ) {
                        AnimationIntensity.entries.forEach { anim ->
                            val isSelected = currentAnimationIntensity == anim

                            GlassDropdownItem(
                                onClick = {
                                    onAnimationIntensityChange(anim)
                                    animDropdownExpanded = false
                                },
                                modifier = Modifier.testTag("anim_item_${anim.name.lowercase()}")
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

                Column(modifier = Modifier.fillMaxWidth().testTag("haptic_dropdown_box")) {
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

                    GlassExpandablePanel(
                        expanded = hapticDropdownExpanded,
                        maxHeight = 300.dp,
                        modifier = Modifier.testTag("haptic_dropdown_menu")
                    ) {
                        HapticIntensity.entries.forEach { haptic ->
                            val isSelected = currentHapticIntensity == haptic

                            GlassDropdownItem(
                                onClick = {
                                    onHapticIntensityChange(haptic)
                                    hapticDropdownExpanded = false
                                    // Trigger immediate sample feedback
                                    HapticFeedbackHelper.performKeypadTap(context, view, haptic)
                                },
                                modifier = Modifier.testTag("haptic_item_${haptic.name.lowercase()}")
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

                Column(modifier = Modifier.fillMaxWidth().testTag("sound_dropdown_box")) {
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

                    GlassExpandablePanel(
                        expanded = soundDropdownExpanded,
                        maxHeight = 300.dp,
                        modifier = Modifier.testTag("sound_dropdown_menu")
                    ) {
                        SoundIntensity.entries.forEach { sound ->
                            val isSelected = currentSoundIntensity == sound

                            GlassDropdownItem(
                                onClick = {
                                    onSoundIntensityChange(sound)
                                    soundDropdownExpanded = false
                                    // Trigger immediate sample audio burst
                                    SoundFeedbackHelper.playClickSound(context, view, sound)
                                },
                                modifier = Modifier.testTag("sound_item_${sound.name.lowercase()}")
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

            HorizontalDivider(color = primaryColor.copy(alpha = 0.2f))

            // ==========================================
            // SECTION 7: Complete App Database Backup & Restore
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Complete App Database Backup & Restore",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Download backup files (.json) and restore database anytime",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = { settingsViewModel.refreshStats() },
                    modifier = Modifier.size(36.dp).testTag("refresh_db_stats_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Database Statistics",
                        tint = primaryColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = shapeStructure.toCardShape(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0x1CFFFFFF) else Color(0x0C000000)
                ),
                border = BorderStroke(
                    1.dp,
                    if (isDark) Color(0x2EFFFFFF) else Color(0x1C000000)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Database Stats Matrix (2x2 Grid)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DbStatPill(
                                title = "Calculations",
                                value = "${settingsUiState.stats.calculationsCount}",
                                icon = Icons.Default.Calculate,
                                modifier = Modifier.weight(1f)
                            )
                            DbStatPill(
                                title = "Cashbook",
                                value = "${settingsUiState.stats.ledgerTransactionsCount}",
                                icon = Icons.Default.AccountBalanceWallet,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DbStatPill(
                                title = "Sales Audits",
                                value = "${settingsUiState.stats.salesTransactionsCount}",
                                icon = Icons.Default.ReceiptLong,
                                modifier = Modifier.weight(1f)
                            )
                            DbStatPill(
                                title = "Mixtures",
                                value = "${settingsUiState.stats.mixtureRecipesCount}",
                                icon = Icons.Default.Science,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Metadata details row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Last Backup: ${settingsUiState.stats.formattedLastBackup}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontFamily = DigitalMonospaceFont
                            )
                            Text(
                                text = "Export and restore entire app dataset",
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = primaryColor.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = "${settingsUiState.stats.totalRecords} Total Records",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor,
                                fontFamily = DigitalMonospaceFont,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = primaryColor.copy(alpha = 0.15f))

                    // Action buttons
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // 1. Direct Download & Pick File Row (Primary Action Pair)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    saveFileLauncher.launch(settingsViewModel.getDefaultBackupFileName())
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("download_backup_button"),
                                shape = shapeStructure.toInputShape(),
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "Download Backup",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        lineHeight = 14.sp
                                    )
                                    Text(
                                        text = "Save .json file",
                                        fontSize = 9.5.sp,
                                        color = Color.White.copy(alpha = 0.85f),
                                        lineHeight = 11.sp
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    pickFileLauncher.launch(arrayOf("application/json", "application/octet-stream", "text/*", "*/*"))
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("pick_file_restore_button"),
                                shape = shapeStructure.toInputShape(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Upload,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "Pick File & Restore",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color.White,
                                        lineHeight = 14.sp
                                    )
                                    Text(
                                        text = "From device storage",
                                        fontSize = 9.5.sp,
                                        color = Color.White.copy(alpha = 0.85f),
                                        lineHeight = 11.sp
                                    )
                                }
                            }
                        }

                        // 2. Reset / Wipe Database Option
                        OutlinedButton(
                            onClick = { settingsViewModel.openDialog(BackupDialogType.RESET_DATABASE) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                                .testTag("reset_db_button"),
                            shape = shapeStructure.toInputShape(),
                            border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFEF4444)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Database Maintenance & Reset Tables...",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFEF4444)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }

    // ==========================================
    // BACKUP & RESTORE DIALOGS
    // ==========================================

    // 1. Import & Restore Dialog
    if (settingsUiState.activeDialog == BackupDialogType.IMPORT_JSON || settingsUiState.activeDialog == BackupDialogType.RESTORE_CONFIRM) {
        var inputJson by remember { mutableStateOf(settingsUiState.pendingJson) }
        val validation = settingsUiState.validationResult

        AlertDialog(
            onDismissRequest = { settingsViewModel.closeDialog() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Restore,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Restore Database", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // HERO ACTION: Pick File from Device
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFF10B981).copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .clickable {
                                pickFileLauncher.launch(arrayOf("application/json", "application/octet-stream", "text/*", "*/*"))
                            },
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.14f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Upload,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Pick Backup File from Device",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    color = Color(0xFF10B981)
                                )
                                Text(
                                    text = "Choose any .json file from Downloads, Drive or Storage",
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // JSON Input Area
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Or Paste Backup JSON:",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        TextButton(
                            onClick = {
                                val clipText = clipboardManager.getText()?.text ?: ""
                                if (clipText.isNotBlank()) {
                                    inputJson = clipText
                                    settingsViewModel.setPendingJson(clipText)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Paste Clipboard", fontSize = 11.sp)
                        }
                    }

                    OutlinedTextField(
                        value = inputJson,
                        onValueChange = {
                            inputJson = it
                            settingsViewModel.setPendingJson(it)
                        },
                        placeholder = { Text("Paste {\"metadata\": ...} JSON here", fontSize = 11.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(95.dp)
                            .testTag("backup_json_input"),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = DigitalMonospaceFont,
                            fontSize = 10.sp
                        ),
                        maxLines = 5
                    )

                    // Validation Card
                    if (validation != null) {
                        if (validation.isValid) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.14f),
                                border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.6f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Valid Backup Payload Detected",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color(0xFF10B981)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Backup Date: ${validation.formattedBackupDate}", fontSize = 11.sp, fontFamily = DigitalMonospaceFont)
                                    Text("Calculations: ${validation.calculationsCount} items", fontSize = 11.sp)
                                    Text("Cashbook Ledger: ${validation.ledgerTransactionsCount} entries", fontSize = 11.sp)
                                    Text("Sales Audits: ${validation.salesTransactionsCount} transactions", fontSize = 11.sp)
                                    Text("Mixture Recipes: ${validation.mixtureRecipesCount} formulas", fontSize = 11.sp)
                                    Text("App Settings: ${if (validation.hasPreferences) "Included" else "None"}", fontSize = 11.sp)
                                }
                            }
                        } else {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFEF4444).copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = validation.errorMessage ?: "Invalid backup JSON format.",
                                        color = Color(0xFFEF4444),
                                        fontSize = 11.5.sp
                                    )
                                }
                            }
                        }
                    }

                    // Restore Strategy
                    Text(
                        text = "Restore Strategy:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { settingsViewModel.setRestoreMode(RestoreMode.OVERWRITE) }
                        ) {
                            RadioButton(
                                selected = settingsUiState.restoreMode == RestoreMode.OVERWRITE,
                                onClick = { settingsViewModel.setRestoreMode(RestoreMode.OVERWRITE) },
                                colors = RadioButtonDefaults.colors(selectedColor = primaryColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("Clean Overwrite (Recommended)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text("Wipes current database and replaces with backup data", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { settingsViewModel.setRestoreMode(RestoreMode.MERGE) }
                        ) {
                            RadioButton(
                                selected = settingsUiState.restoreMode == RestoreMode.MERGE,
                                onClick = { settingsViewModel.setRestoreMode(RestoreMode.MERGE) },
                                colors = RadioButtonDefaults.colors(selectedColor = primaryColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("Merge & Append", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text("Keeps existing items and adds backup records", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // Settings restore checkbox
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { settingsViewModel.setRestoreSettingsOption(!settingsUiState.restoreSettingsOption) }
                    ) {
                        Checkbox(
                            checked = settingsUiState.restoreSettingsOption,
                            onCheckedChange = { settingsViewModel.setRestoreSettingsOption(it) },
                            colors = CheckboxDefaults.colors(checkedColor = primaryColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Apply themes, styles & sound settings from backup",
                            fontSize = 11.5.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        settingsViewModel.performRestore(onPreferencesReload)
                    },
                    enabled = validation?.isValid == true && !settingsUiState.isBusy,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    if (settingsUiState.isBusy) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text("Restore Database", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { settingsViewModel.closeDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 3. Reset Database Confirmation Dialog
    if (settingsUiState.activeDialog == BackupDialogType.RESET_DATABASE) {
        AlertDialog(
            onDismissRequest = { settingsViewModel.closeDialog() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reset Database Tables", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Select which database tables you want to wipe. This action cannot be undone unless you have a backup.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFEF4444).copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { settingsViewModel.setClearCalculationsOption(!settingsUiState.clearCalculationsOption) }
                            ) {
                                Checkbox(
                                    checked = settingsUiState.clearCalculationsOption,
                                    onCheckedChange = { settingsViewModel.setClearCalculationsOption(it) },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFFEF4444))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Calculation History (${settingsUiState.stats.calculationsCount} items)", fontSize = 12.sp)
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { settingsViewModel.setClearLedgerOption(!settingsUiState.clearLedgerOption) }
                            ) {
                                Checkbox(
                                    checked = settingsUiState.clearLedgerOption,
                                    onCheckedChange = { settingsViewModel.setClearLedgerOption(it) },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFFEF4444))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Cashbook / Ledger (${settingsUiState.stats.ledgerTransactionsCount} records)", fontSize = 12.sp)
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { settingsViewModel.setClearSalesOption(!settingsUiState.clearSalesOption) }
                            ) {
                                Checkbox(
                                    checked = settingsUiState.clearSalesOption,
                                    onCheckedChange = { settingsViewModel.setClearSalesOption(it) },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFFEF4444))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sales Reconciliation Audits (${settingsUiState.stats.salesTransactionsCount} audits)", fontSize = 12.sp)
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { settingsViewModel.setClearMixturesOption(!settingsUiState.clearMixturesOption) }
                            ) {
                                Checkbox(
                                    checked = settingsUiState.clearMixturesOption,
                                    onCheckedChange = { settingsViewModel.setClearMixturesOption(it) },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFFEF4444))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Mixture Wizard Recipes (${settingsUiState.stats.mixtureRecipesCount} recipes)", fontSize = 12.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { settingsViewModel.performResetDatabase(onPreferencesReload) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Purge Selected Tables", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { settingsViewModel.closeDialog() }) {
                    Text("Cancel")
                }
            }
        )
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

@Composable
private fun GlassExpandablePanel(
    expanded: Boolean,
    modifier: Modifier = Modifier,
    maxHeight: Dp = 380.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.2f
    val primaryColor = MaterialTheme.colorScheme.primary
    val shapeStructure = LocalShapeStructure.current

    AnimatedVisibility(
        visible = expanded,
        enter = expandVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
        exit = shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeOut()
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .clip(shapeStructure.toCardShape())
                .border(1.dp, primaryColor.copy(alpha = 0.35f), shapeStructure.toCardShape()),
            shape = shapeStructure.toCardShape(),
            color = if (isDark) Color(0xFF161F30).copy(alpha = 0.95f) else Color(0xFFF8FAFC).copy(alpha = 0.98f),
            shadowElevation = 3.dp
        ) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxHeight)
                    .verticalScroll(scrollState)
                    .padding(vertical = 4.dp),
                content = content
            )
        }
    }
}

@Composable
private fun GlassDropdownItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
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

private fun getFontIcon(fontStyle: AppFontStyle): ImageVector {
    return when (fontStyle) {
        AppFontStyle.MODERN_CALLIGRAPHY_SIGNATURE -> Icons.Default.Draw
        AppFontStyle.ROMANTIC_FLOURISH_CALLIGRAPHY -> Icons.Default.Palette
        AppFontStyle.CHIC_BRUSH_CALLIGRAPHY -> Icons.Default.Edit
        AppFontStyle.PARISIAN_HAUTE_CALLIGRAPHY -> Icons.Default.FormatItalic
        AppFontStyle.MINIMALIST_MONOLINE_SCRIPT -> Icons.Default.LinearScale
        AppFontStyle.ROYAL_MAJESTIC_CALLIGRAPHY -> Icons.Default.MenuBook
        AppFontStyle.DANCING_BOUNCY_SCRIPT -> Icons.Default.PlayArrow
        AppFontStyle.CASUAL_HANDWRITING -> Icons.Default.Draw
        AppFontStyle.BOLD_MARKER_SCRIPT -> Icons.Default.Edit
        AppFontStyle.CLEAN_MONOSPACE -> Icons.Default.Calculate
        AppFontStyle.LCD_DIGITAL_BOLD -> Icons.Default.Tune
        AppFontStyle.CYBER_CONDENSED -> Icons.Default.GraphicEq
        AppFontStyle.TERMINAL_MONOSPACE -> Icons.Default.Terminal
        AppFontStyle.CONDENSED_DIGITAL -> Icons.Default.DensityMedium
        AppFontStyle.TYPEWRITER_DIGITAL -> Icons.Default.History
        AppFontStyle.INDUSTRIAL_DIGITAL -> Icons.Default.DensityLarge
        AppFontStyle.SYSTEM -> Icons.Default.Smartphone
        AppFontStyle.CLASSIC_SERIF -> Icons.Default.MenuBook
        AppFontStyle.LUXURY_SERIF_ITALIC -> Icons.Default.FormatItalic
        AppFontStyle.VINTAGE_TYPEWRITER -> Icons.Default.History
        AppFontStyle.MINIMALIST_LIGHT -> Icons.Default.LightMode
        AppFontStyle.INDUSTRIAL_BLACK -> Icons.Default.DensityLarge
        AppFontStyle.SMALL_CAPS_LEDGER -> Icons.Default.Abc
        AppFontStyle.GEOMETRIC_TECHNO -> Icons.Default.TextFields
        AppFontStyle.BEGONIA -> Icons.Default.TextFields
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

@Composable
private fun DbStatPill(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.2f
    val primaryColor = MaterialTheme.colorScheme.primary

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = if (isDark) Color(0x18FFFFFF) else Color(0x0C000000),
        border = BorderStroke(1.dp, if (isDark) Color(0x22FFFFFF) else Color(0x15000000))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = primaryColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                fontSize = 9.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

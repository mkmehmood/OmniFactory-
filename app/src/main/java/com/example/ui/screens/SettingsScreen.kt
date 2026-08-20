package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.PanoramaFishEye
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.RoundedCorner
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.CalculationEntity
import com.example.data.db.SalesTransactionEntity
import com.example.domain.export.CsvExporter
import com.example.ui.theme.AccentTheme
import com.example.ui.theme.DigitalMonospaceFont
import com.example.ui.theme.ShapeStructureStyle
import com.example.ui.theme.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentThemeMode: ThemeMode,
    currentAccent: AccentTheme,
    currentShapeStructure: ShapeStructureStyle = ShapeStructureStyle.ROUNDED,
    showDoubleZeroKey: Boolean,
    enableAnimations: Boolean,
    hapticEnabled: Boolean,
    historyList: List<CalculationEntity> = emptyList(),
    salesTransactions: List<SalesTransactionEntity> = emptyList(),
    onThemeModeChange: (ThemeMode) -> Unit,
    onAccentChange: (AccentTheme) -> Unit,
    onShapeStructureChange: (ShapeStructureStyle) -> Unit = {},
    onToggleDoubleZero: (Boolean) -> Unit,
    onToggleAnimations: (Boolean) -> Unit,
    onHapticToggle: (Boolean) -> Unit,
    onExportTapeCsv: () -> Unit = {},
    onExportSalesCsv: () -> Unit = {},
    onExportAllCsv: () -> Unit = {},
    onCopyTapeCsv: () -> Unit = {},
    onCopySalesCsv: () -> Unit = {},
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.2f
    val primaryColor = MaterialTheme.colorScheme.primary

    var previewTitle by remember { mutableStateOf<String?>(null) }
    var previewContent by remember { mutableStateOf<String?>(null) }

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
                                imageVector = Icons.Default.WaterDrop,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Settings & Preferences",
                            fontWeight = FontWeight.Bold,
                            fontFamily = DigitalMonospaceFont,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
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
                            contentDescription = "Back to Calculator",
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
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .widthIn(max = 640.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Section 1: Data Export & External CSV
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Data Export & External CSV",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
                Text(
                    text = "Export Room database calculations and sales reconciliation audits to external RFC 4180 CSV files using Pakistani Rupees (Rs.) in Indian number grouping format.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 1. Tape History CSV Export Card
            DataExportCard(
                title = "Calculation Tape History",
                subtitle = "Historical tape calculations, notes & pins",
                recordCount = historyList.size,
                icon = Icons.Default.History,
                primaryColor = primaryColor,
                isDark = isDark,
                onExportFile = onExportTapeCsv,
                onCopyCsv = onCopyTapeCsv,
                onPreview = {
                    val csv = CsvExporter.exportCalculationHistoryToCsv(historyList)
                    previewTitle = "Tape History CSV Preview (${historyList.size} records)"
                    previewContent = csv
                },
                exportTag = "export_tape_csv_button",
                copyTag = "copy_tape_csv_button",
                previewTag = "preview_tape_csv_button"
            )

            // 2. Sales Reconciliation CSV Export Card
            DataExportCard(
                title = "Sales Reconciliation Data",
                subtitle = "Full audit logs, recovered credit, commissions & discrepancies",
                recordCount = salesTransactions.size,
                icon = Icons.Default.ReceiptLong,
                primaryColor = Color(0xFF10B981),
                isDark = isDark,
                onExportFile = onExportSalesCsv,
                onCopyCsv = onCopySalesCsv,
                onPreview = {
                    val csv = CsvExporter.exportSalesTransactionsToCsv(salesTransactions)
                    previewTitle = "Sales Reconciliation CSV Preview (${salesTransactions.size} records)"
                    previewContent = csv
                },
                exportTag = "export_sales_csv_button",
                copyTag = "copy_sales_csv_button",
                previewTag = "preview_sales_csv_button"
            )

            // 3. Combined One-Tap Export Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        1.dp,
                        primaryColor.copy(alpha = 0.35f),
                        RoundedCornerShape(16.dp)
                    )
                    .clickable { onExportAllCsv() }
                    .testTag("export_all_data_button"),
                shape = RoundedCornerShape(16.dp),
                color = primaryColor.copy(alpha = 0.10f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FolderZip,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Export All App Data (Combined CSV)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                            Text(
                                text = "${historyList.size} tape items + ${salesTransactions.size} sales audits",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share All",
                        tint = primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            HorizontalDivider(color = primaryColor.copy(alpha = 0.2f))

            // Section 2: Theme Mode (Dark Liquid / Light Frosted / System)
            Text(
                text = "Appearance & Surface",
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
                    text = "Button & Input Field Structure",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Choose the shape style for keypad keys, cards, and input fields",
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
                    subtitle = "Smooth 18dp rounded corners",
                    icon = Icons.Default.RoundedCorner,
                    isSelected = currentShapeStructure == ShapeStructureStyle.ROUNDED,
                    onClick = { onShapeStructureChange(ShapeStructureStyle.ROUNDED) }
                )
                ShapeStructureCard(
                    title = "Pill / Capsule",
                    subtitle = "Continuous pill oval geometry",
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
                    subtitle = "Sharp crisp beveled rectangular keys",
                    icon = Icons.Default.CropSquare,
                    isSelected = currentShapeStructure == ShapeStructureStyle.SHARP,
                    onClick = { onShapeStructureChange(ShapeStructureStyle.SHARP) }
                )
            }

            HorizontalDivider(color = primaryColor.copy(alpha = 0.2f))

            // Section 4: Liquid Accent Themes
            Text(
                text = "Accent Color Theme",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                AccentTheme.values().forEach { accent ->
                    val accentColor = if (isDark) accent.darkPrimary else accent.lightPrimary
                    LiquidColorDot(
                        color = accentColor,
                        isSelected = currentAccent == accent,
                        onClick = { onAccentChange(accent) }
                    )
                }
            }

            HorizontalDivider(color = primaryColor.copy(alpha = 0.2f))

            // Section 5: Keypad & Feedback Controls
            Text(
                text = "Keypad & Feedback Controls",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
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

                    // Haptic Feedback
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Tactile Haptic Vibration Feedback", fontWeight = FontWeight.Medium)
                            Text(
                                "Direct hardware vibrator click and touch feedback on every tap",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = hapticEnabled,
                            onCheckedChange = onHapticToggle,
                            colors = SwitchDefaults.colors(checkedThumbColor = primaryColor),
                            modifier = Modifier.testTag("haptic_switch")
                        )
                    }
                }
            }

            HorizontalDivider(color = primaryColor.copy(alpha = 0.2f))

            // Section 6: About & Developer Info
            Text(
                text = "About & Developer",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0x18FFFFFF) else Color(0x0A000000)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    primaryColor.copy(alpha = 0.25f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(primaryColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WaterDrop,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Sarim Calculator",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Package: com.sarim.app • v1.0",
                                fontSize = 11.sp,
                                fontFamily = DigitalMonospaceFont,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider(color = if (isDark) Color(0x1AFFFFFF) else Color(0x10000000))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Developer",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Mahmood Khan (محمود خان)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Architecture",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Kotlin • Jetpack Compose • Room M3",
                            fontSize = 11.sp,
                            fontFamily = DigitalMonospaceFont,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // CSV Text Preview Dialog
    if (previewContent != null && previewTitle != null) {
        CsvPreviewDialog(
            title = previewTitle!!,
            content = previewContent!!,
            primaryColor = primaryColor,
            isDark = isDark,
            onDismiss = {
                previewTitle = null
                previewContent = null
            }
        )
    }
}

@Composable
private fun DataExportCard(
    title: String,
    subtitle: String,
    recordCount: Int,
    icon: ImageVector,
    primaryColor: Color,
    isDark: Boolean,
    onExportFile: () -> Unit,
    onCopyCsv: () -> Unit,
    onPreview: () -> Unit,
    exportTag: String,
    copyTag: String,
    previewTag: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0x18FFFFFF) else Color(0x0A000000)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isDark) Color(0x28FFFFFF) else Color(0x20000000)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(primaryColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = subtitle,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 14.sp
                        )
                    }
                }

                // Record count badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = primaryColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "$recordCount rows",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontFamily = DigitalMonospaceFont
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons (Export File / Copy / Preview)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onExportFile,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag(exportTag)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Export File",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share CSV", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                OutlinedButton(
                    onClick = onCopyCsv,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag(copyTag)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy CSV",
                        tint = primaryColor,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = primaryColor)
                }

                OutlinedButton(
                    onClick = onPreview,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag(previewTag)
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "Preview CSV",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("View", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun CsvPreviewDialog(
    title: String,
    content: String,
    primaryColor: Color,
    isDark: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = remember {
        context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = if (isDark) Color(0xFF0F172A) else Color(0xFFFFFFFF),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TableChart,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            1.dp,
                            if (isDark) Color(0x28FFFFFF) else Color(0x20000000),
                            RoundedCornerShape(12.dp)
                        ),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDark) Color(0xFF080D1A) else Color(0xFFF1F5F9)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                            .verticalScroll(rememberScrollState())
                            .horizontalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = content.ifBlank { "No records available." },
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {
                        val clip = android.content.ClipData.newPlainText("CSV Export", content)
                        clipboard.setPrimaryClip(clip)
                        android.widget.Toast.makeText(context, "CSV copied to clipboard", android.widget.Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy CSV")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Close")
                }
            }
        }
    )
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

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) primaryColor else (if (isDark) Color(0x33FFFFFF) else Color(0x20000000)),
                shape = RoundedCornerShape(14.dp)
            )
            .testTag("shape_option_${title.lowercase().replace(" ", "_").replace("/", "_")}"),
        shape = RoundedCornerShape(14.dp),
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

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        if (isSelected) primaryColor else (if (isDark) Color.White else Color.Black).copy(alpha = 0.2f),
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("theme_card_${title.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(16.dp),
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
private fun LiquidColorDot(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (isSelected) 2.5.dp else 1.dp,
                color = if (isSelected) Color.White else Color.Transparent,
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

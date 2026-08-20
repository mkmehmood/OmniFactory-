package com.example.ui.components

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FileDownload
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

@Composable
fun SettingsDialog(
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
    onDismiss: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.2f
    val primaryColor = MaterialTheme.colorScheme.primary

    var previewTitle by remember { mutableStateOf<String?>(null) }
    var previewContent by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = if (isDark) Color(0xFF0F172A) else Color(0xFFFFFFFF),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(primaryColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text("Settings & Export", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: CSV Export & External Data Integration
                Text(
                    text = "Data Export & External CSV",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
                Text(
                    text = "Export Room database calculations and sales audits to external RFC 4180 CSV files using Pakistani Rupees (Rs.) in Indian number grouping format.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

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

                // 3. Combined One-Tap Export Button
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(
                            1.dp,
                            primaryColor.copy(alpha = 0.35f),
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { onExportAllCsv() }
                        .testTag("export_all_data_button"),
                    shape = RoundedCornerShape(14.dp),
                    color = primaryColor.copy(alpha = 0.10f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FolderZip,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Export All App Data (Combined CSV)",
                                    fontSize = 13.sp,
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
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                HorizontalDivider(color = primaryColor.copy(alpha = 0.2f))

                // Theme Mode (Dark Liquid / Light Crystal / System)
                Text(
                    text = "Appearance & Surface",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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

                // Button & Input Fields Geometry / Structure
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

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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

                // Liquid Accent Themes
                Text(
                    text = "Accent Color",
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

                // Keypad & Feedback Controls
                Text(
                    text = "Keypad & Feedback",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                // Dedicated '00' Key
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Dedicated '00' Key", fontWeight = FontWeight.Medium)
                        Text(
                            "Fast double-zero entry",
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

                // Fluid Tap Animations
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Fluid Tap Physics", fontWeight = FontWeight.Medium)
                        Text(
                            "Subtle spring compression on touch",
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

                // Haptic Feedback
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Haptic Feedback", fontWeight = FontWeight.Medium)
                        Text(
                            "Tactile click vibration",
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
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("settings_done_button")
            ) {
                Text("Done", fontWeight = FontWeight.Bold, color = primaryColor)
            }
        }
    )

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
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(primaryColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
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

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons (Export File / Copy / Preview)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Export CSV File Button
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

                // Copy CSV Button
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

                // Preview Button
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

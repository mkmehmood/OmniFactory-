package com.example.ui.components

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.BatchScalingEntity
import com.example.data.db.RestoreMode
import com.example.ui.theme.DigitalMonospaceFont
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BatchScalingHistorySheet(
    historyList: List<BatchScalingEntity>,
    onDismiss: () -> Unit,
    onLoadItem: (BatchScalingEntity) -> Unit,
    onEditItem: (BatchScalingEntity, String, String, String) -> Unit,
    onDeleteItem: (BatchScalingEntity) -> Unit,
    onTogglePin: (BatchScalingEntity) -> Unit,
    onClearAll: () -> Unit,
    onExportBackupJson: suspend () -> String,
    onRestoreBackupJson: (String, RestoreMode) -> Unit,
    onSaveToUri: (android.net.Uri) -> Unit,
    onLoadFromUri: (android.net.Uri, RestoreMode) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceBg = if (isDark) Color(0x18FFFFFF) else Color(0x0C000000)
    val borderColor = if (isDark) Color(0x28FFFFFF) else Color(0x1E000000)

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, TARGET_TOTAL, BATCH_SIZE, PINNED
    var editingEntity by remember { mutableStateOf<BatchScalingEntity?>(null) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var showBackupRestoreModal by remember { mutableStateOf(false) }

    val filterOptions = listOf("ALL", "TARGET_TOTAL", "BATCH_SIZE", "PINNED")

    val filteredList = remember(historyList, searchQuery, selectedFilter) {
        historyList.filter { item ->
            val matchesFilter = when (selectedFilter) {
                "ALL" -> true
                "PINNED" -> item.isPinned
                else -> item.scalingMode.equals(selectedFilter, ignoreCase = true)
            }
            val matchesQuery = searchQuery.isBlank() ||
                    item.title.contains(searchQuery, ignoreCase = true) ||
                    item.description.contains(searchQuery, ignoreCase = true) ||
                    item.note.contains(searchQuery, ignoreCase = true) ||
                    item.componentsJson.contains(searchQuery, ignoreCase = true)
            matchesFilter && matchesQuery
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.testTag("batch_scaling_history_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Scale,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Batch Scaler History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DigitalMonospaceFont
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = primaryColor.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = "${historyList.size}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            fontFamily = DigitalMonospaceFont,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { showBackupRestoreModal = true },
                        modifier = Modifier.size(36.dp).testTag("batch_backup_restore_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Backup & Restore Scaler History",
                            tint = primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (historyList.isNotEmpty()) {
                        IconButton(
                            onClick = { showClearConfirm = true },
                            modifier = Modifier.size(36.dp).testTag("batch_clear_all_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Clear all",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp).testTag("close_batch_history_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            LiquidGlassTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search scaling recipes, components, notes...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                singleLine = true,
                accentColor = primaryColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("batch_history_search_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                filterOptions.forEach { opt ->
                    val isSelected = selectedFilter == opt
                    val label = when (opt) {
                        "ALL" -> "All"
                        "TARGET_TOTAL" -> "Target Total"
                        "BATCH_SIZE" -> "Batch Mode"
                        "PINNED" -> "Pinned ★"
                        else -> opt
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = opt },
                        label = { Text(label, fontSize = 11.sp, fontFamily = DigitalMonospaceFont) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = primaryColor.copy(alpha = 0.2f),
                            selectedLabelColor = primaryColor
                        ),
                        modifier = Modifier.height(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // History List
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Scale,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (historyList.isEmpty()) "No saved batch calculations yet" else "No matching scaling entries found",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (historyList.isEmpty()) "Scale a batch and tap 'Save to History'" else "Try clearing your search query or filter",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .heightIn(max = 480.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredList, key = { it.id }) { item ->
                        BatchScalingHistoryCard(
                            item = item,
                            onLoad = {
                                onLoadItem(item)
                                onDismiss()
                            },
                            onEdit = { editingEntity = item },
                            onDelete = { onDeleteItem(item) },
                            onTogglePin = { onTogglePin(item) },
                            onCopy = {
                                val summary = formatScalingSummary(item)
                                clipboardManager.setText(AnnotatedString(summary))
                                Toast.makeText(context, "Batch scaling copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            surfaceBg = surfaceBg,
                            borderColor = borderColor,
                            primaryColor = primaryColor
                        )
                    }
                }
            }
        }
    }

    // Edit Dialog
    if (editingEntity != null) {
        val entity = editingEntity!!
        var editTitle by remember(entity) { mutableStateOf(entity.title) }
        var editDescription by remember(entity) { mutableStateOf(entity.description) }
        var editNote by remember(entity) { mutableStateOf(entity.note) }

        AlertDialog(
            onDismissRequest = { editingEntity = null },
            title = {
                Text(
                    text = "Edit Batch Scaling Details",
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    LiquidGlassTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Title") },
                        singleLine = true,
                        accentColor = primaryColor,
                        modifier = Modifier.fillMaxWidth().testTag("edit_batch_title")
                    )

                    LiquidGlassTextField(
                        value = editDescription,
                        onValueChange = { editDescription = it },
                        label = { Text("Description") },
                        singleLine = true,
                        accentColor = primaryColor,
                        modifier = Modifier.fillMaxWidth().testTag("edit_batch_desc")
                    )

                    LiquidGlassTextField(
                        value = editNote,
                        onValueChange = { editNote = it },
                        label = { Text("Notes (Optional)") },
                        singleLine = false,
                        accentColor = primaryColor,
                        modifier = Modifier.fillMaxWidth().testTag("edit_batch_note")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onEditItem(entity, editTitle, editDescription, editNote)
                        editingEntity = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingEntity = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear All Confirmation
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear All Batch History?") },
            text = { Text("This will permanently remove all saved batch scaling records from the database. This action cannot be undone unless you have a backup.") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAll()
                        showClearConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Standalone Backup & Restore Modal for Batch Scaler
    if (showBackupRestoreModal) {
        BatchScalingBackupRestoreDialog(
            onDismiss = { showBackupRestoreModal = false },
            onExportJson = onExportBackupJson,
            onRestoreJson = { json, mode ->
                onRestoreBackupJson(json, mode)
                showBackupRestoreModal = false
            },
            onSaveToUri = onSaveToUri,
            onLoadFromUri = { uri, mode ->
                onLoadFromUri(uri, mode)
                showBackupRestoreModal = false
            }
        )
    }
}

@Composable
private fun BatchScalingHistoryCard(
    item: BatchScalingEntity,
    onLoad: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTogglePin: () -> Unit,
    onCopy: () -> Unit,
    surfaceBg: Color,
    borderColor: Color,
    primaryColor: Color
) {
    var expanded by remember { mutableStateOf(false) }
    val dateStr = remember(item.timestamp) {
        val sdf = SimpleDateFormat("MMM dd, yyyy · hh:mm a", Locale.getDefault())
        sdf.format(Date(item.timestamp))
    }

    val isTargetMode = item.scalingMode == "TARGET_TOTAL"
    val modeLabel = if (isTargetMode) "Target Total" else "Batch Mode"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                1.dp,
                if (item.isPinned) primaryColor.copy(alpha = 0.5f) else borderColor,
                RoundedCornerShape(16.dp)
            )
            .testTag("batch_card_${item.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceBg)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Title, Pin, Mode Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (item.isPinned) {
                            Icon(
                                imageVector = Icons.Filled.PushPin,
                                contentDescription = "Pinned",
                                tint = primaryColor,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = item.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = dateStr,
                        fontSize = 10.5.sp,
                        fontFamily = DigitalMonospaceFont,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = primaryColor.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = modeLabel.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = DigitalMonospaceFont,
                            color = primaryColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    IconButton(
                        onClick = onTogglePin,
                        modifier = Modifier.size(32.dp).testTag("pin_btn_${item.id}")
                    ) {
                        Icon(
                            imageVector = if (item.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = if (item.isPinned) "Unpin" else "Pin",
                            tint = if (item.isPinned) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Scaling Stats Matrix: Original -> Scaled (Scale factor)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x0A000000), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Original Total", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = formatNum(item.originalGrandTotal),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = DigitalMonospaceFont,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Factor", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = primaryColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = String.format(Locale.US, "%.2fx", item.scalingFactor),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = DigitalMonospaceFont,
                            color = primaryColor,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Scaled Total", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = formatNum(item.scaledGrandTotal),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DigitalMonospaceFont,
                        color = primaryColor
                    )
                }
            }

            if (item.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = item.description,
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = if (expanded) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (item.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Note: ${item.note}",
                    fontSize = 11.sp,
                    fontFamily = DigitalMonospaceFont,
                    color = primaryColor.copy(alpha = 0.9f)
                )
            }

            // Expandable Components Breakdown
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    HorizontalDivider(color = borderColor.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "COMPONENTS LIST (${item.componentsCount}):",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DigitalMonospaceFont,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    val components = parseComponentsJson(item.componentsJson)
                    components.forEach { (name, amount, isLocked) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "• $name",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (isLocked) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("(Locked)", fontSize = 9.sp, color = primaryColor)
                                }
                            }
                            Text(
                                text = amount,
                                fontSize = 11.sp,
                                fontFamily = DigitalMonospaceFont,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                        }
                    }

                    if (!isTargetMode || item.batchCountText != "1") {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Batch Count: ${item.batchCountText} | Single Batch: ${formatNum(item.singleBatchGrandTotal)}",
                            fontSize = 10.5.sp,
                            fontFamily = DigitalMonospaceFont,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clickable { expanded = !expanded }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (expanded) "Hide Details" else "View Details",
                        fontSize = 11.sp,
                        fontFamily = DigitalMonospaceFont,
                        color = primaryColor
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier.size(32.dp).testTag("copy_batch_${item.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Recipe",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp).testTag("edit_batch_${item.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Details",
                            tint = primaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp).testTag("delete_batch_${item.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Button(
                        onClick = onLoad,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp).testTag("load_batch_${item.id}")
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Load", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun BatchScalingSaveDialog(
    initialTitle: String,
    onDismiss: () -> Unit,
    onSave: (title: String, description: String, note: String) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle.ifBlank { "Batch Recipe" }) }
    var description by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    val primaryColor = MaterialTheme.colorScheme.primary

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Scale, contentDescription = null, tint = primaryColor, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Batch Scaling Recipe", fontWeight = FontWeight.Bold, fontFamily = DigitalMonospaceFont)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Save current components, target total, batch size, and scaling factors to history.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LiquidGlassTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Recipe Title") },
                    singleLine = true,
                    accentColor = primaryColor,
                    modifier = Modifier.fillMaxWidth().testTag("save_batch_title_input")
                )

                LiquidGlassTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description / Category (Optional)") },
                    singleLine = true,
                    accentColor = primaryColor,
                    modifier = Modifier.fillMaxWidth().testTag("save_batch_desc_input")
                )

                LiquidGlassTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Production Notes / Remarks (Optional)") },
                    singleLine = false,
                    accentColor = primaryColor,
                    modifier = Modifier.fillMaxWidth().testTag("save_batch_note_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title, description, note) },
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                modifier = Modifier.testTag("confirm_save_batch_btn")
            ) {
                Text("Save to History", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun BatchScalingBackupRestoreDialog(
    onDismiss: () -> Unit,
    onExportJson: suspend () -> String,
    onRestoreJson: (String, RestoreMode) -> Unit,
    onSaveToUri: (android.net.Uri) -> Unit,
    onLoadFromUri: (android.net.Uri, RestoreMode) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val primaryColor = MaterialTheme.colorScheme.primary
    var activeTab by remember { mutableStateOf(0) } // 0: Export, 1: Import
    var restoreMode by remember { mutableStateOf(RestoreMode.MERGE) }
    var pastedJson by remember { mutableStateOf("") }

    val fileSaveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) onSaveToUri(uri)
    }

    val filePickLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) onLoadFromUri(uri, restoreMode)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (activeTab == 0) Icons.Default.Download else Icons.Default.Upload,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Batch Scaler Backup & Restore", fontWeight = FontWeight.Bold, fontFamily = DigitalMonospaceFont)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Tab switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x12FFFFFF), RoundedCornerShape(10.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { activeTab = 0 },
                        shape = RoundedCornerShape(8.dp),
                        color = if (activeTab == 0) primaryColor else Color.Transparent
                    ) {
                        Text(
                            text = "Export Backup",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = DigitalMonospaceFont,
                            color = if (activeTab == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { activeTab = 1 },
                        shape = RoundedCornerShape(8.dp),
                        color = if (activeTab == 1) primaryColor else Color.Transparent
                    ) {
                        Text(
                            text = "Restore / Import",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = DigitalMonospaceFont,
                            color = if (activeTab == 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                if (activeTab == 0) {
                    Text(
                        text = "Export your saved batch scaling calculations as a standalone JSON backup file.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = {
                            val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                            fileSaveLauncher.launch("batch_scaler_backup_$dateStr.json")
                        },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Download Backup JSON File", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                val json = onExportJson()
                                clipboardManager.setText(AnnotatedString(json))
                                Toast.makeText(context, "Batch backup JSON copied to clipboard", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Backup JSON to Clipboard", fontSize = 12.sp)
                    }
                } else {
                    Text(
                        text = "Restore batch scaling recipes from a backup file or paste JSON.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { restoreMode = RestoreMode.MERGE }
                        ) {
                            RadioButton(
                                selected = restoreMode == RestoreMode.MERGE,
                                onClick = { restoreMode = RestoreMode.MERGE }
                            )
                            Text("Merge", fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { restoreMode = RestoreMode.OVERWRITE }
                        ) {
                            RadioButton(
                                selected = restoreMode == RestoreMode.OVERWRITE,
                                onClick = { restoreMode = RestoreMode.OVERWRITE }
                            )
                            Text("Overwrite Existing", fontSize = 12.sp)
                        }
                    }

                    Button(
                        onClick = { filePickLauncher.launch(arrayOf("application/json", "*/*")) },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Select Backup File (.json)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    LiquidGlassTextField(
                        value = pastedJson,
                        onValueChange = { pastedJson = it },
                        placeholder = { Text("Or paste backup JSON here...") },
                        singleLine = false,
                        accentColor = primaryColor,
                        modifier = Modifier.fillMaxWidth().height(100.dp)
                    )

                    if (pastedJson.isNotBlank()) {
                        Button(
                            onClick = { onRestoreJson(pastedJson, restoreMode) },
                            modifier = Modifier.fillMaxWidth().height(42.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Restore from Pasted JSON", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

private fun formatNum(v: Double): String {
    return if (v % 1.0 == 0.0) String.format(Locale.US, "%,.0f", v) else String.format(Locale.US, "%,.2f", v)
}

private fun formatScalingSummary(item: BatchScalingEntity): String {
    val components = parseComponentsJson(item.componentsJson)
    return buildString {
        appendLine("=== BATCH SCALING RECIPE: ${item.title} ===")
        appendLine("Mode: ${if (item.scalingMode == "TARGET_TOTAL") "Target Total" else "Batch Mode"}")
        appendLine("Original Total: ${formatNum(item.originalGrandTotal)}")
        appendLine("Scaled Total: ${formatNum(item.scaledGrandTotal)}")
        appendLine("Scaling Factor: ${String.format(Locale.US, "%.2fx", item.scalingFactor)}")
        if (item.batchCountText != "1") appendLine("Batch Count: ${item.batchCountText}")
        if (item.description.isNotBlank()) appendLine("Description: ${item.description}")
        if (item.note.isNotBlank()) appendLine("Note: ${item.note}")
        appendLine("\n--- COMPONENTS ---")
        components.forEach { (name, amount, isLocked) ->
            appendLine("• $name: $amount ${if (isLocked) "(Locked)" else ""}")
        }
    }
}

private fun parseComponentsJson(json: String): List<Triple<String, String, Boolean>> {
    val list = mutableListOf<Triple<String, String, Boolean>>()
    try {
        val array = JSONArray(json)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val name = obj.optString("name", "Component")
            val amount = obj.optString("amountText", "0")
            val isLocked = obj.optBoolean("isLocked", false)
            list.add(Triple(name, amount, isLocked))
        }
    } catch (e: Exception) {
        // fallback
    }
    return list
}

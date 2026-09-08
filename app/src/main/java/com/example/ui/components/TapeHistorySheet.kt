package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.LiquidGlassButtonStyle
import com.example.ui.components.LiquidGlassTextField
import com.example.ui.components.liquidGlassSurface
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.luminance
import com.example.data.db.CalculationEntity
import com.example.ui.theme.DigitalMonospaceFont
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class HistoryTimeSplit(val title: String) {
    ALL_TIME("All Time"),
    TODAY("Today"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TapeHistorySheet(
    historyList: List<CalculationEntity>,
    selectedTimeSplit: HistoryTimeSplit,
    onTimeSplitChange: (HistoryTimeSplit) -> Unit,
    onDismiss: () -> Unit,
    onRestoreItem: (CalculationEntity) -> Unit,
    onReuseExpression: (String) -> Unit = {},
    onDeleteItem: (CalculationEntity) -> Unit,
    onTogglePin: (CalculationEntity) -> Unit,
    onUpdateNote: (CalculationEntity, String) -> Unit,
    onClearAll: () -> Unit,
    onCopyTape: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedIds by remember { mutableStateOf(setOf<Long>()) }
    var editingEntity by remember { mutableStateOf<CalculationEntity?>(null) }
    var showClearDialog by remember { mutableStateOf(false) }

    // Clear time separation filter (All Time, Today, Weekly, Monthly, Yearly)
    val now = remember { System.currentTimeMillis() }
    val filteredList = remember(historyList, selectedTimeSplit, now) {
        val cal = Calendar.getInstance()
        when (selectedTimeSplit) {
            HistoryTimeSplit.ALL_TIME -> historyList
            HistoryTimeSplit.TODAY -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val startOfToday = cal.timeInMillis
                historyList.filter { it.timestamp >= startOfToday }
            }
            HistoryTimeSplit.WEEKLY -> {
                val sevenDaysAgo = now - (7L * 24 * 60 * 60 * 1000)
                historyList.filter { it.timestamp >= sevenDaysAgo }
            }
            HistoryTimeSplit.MONTHLY -> {
                val thirtyDaysAgo = now - (30L * 24 * 60 * 60 * 1000)
                historyList.filter { it.timestamp >= thirtyDaysAgo }
            }
            HistoryTimeSplit.YEARLY -> {
                val oneYearAgo = now - (365L * 24 * 60 * 60 * 1000)
                historyList.filter { it.timestamp >= oneYearAgo }
            }
        }
    }

    // Period Total Sum
    val periodTotalSum = remember(filteredList) {
        filteredList.mapNotNull { it.result.replace(",", "").toDoubleOrNull() }.sum()
    }

    val selectedSum = remember(selectedIds, historyList) {
        historyList
            .filter { it.id in selectedIds }
            .mapNotNull { it.result.replace(",", "").toDoubleOrNull() }
            .sum()
    }

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val primaryColor = MaterialTheme.colorScheme.primary

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (isDark) Color(0xD9101726) else Color(0xEEF8FAFC),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        modifier = Modifier.testTag("tape_history_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            // Top Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(primaryColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "History",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${filteredList.size} calculations • Total: ${com.example.domain.util.PkrNumberFormatter.formatPkrAmount(periodTotalSum, decimals = 2)}",
                            fontFamily = DigitalMonospaceFont,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (filteredList.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                val tapeText = filteredList.joinToString("\n") {
                                    "${it.expression} = ${it.result} ${if (it.note.isNotBlank()) "(${it.note})" else ""}"
                                }
                                onCopyTape(tapeText)
                            },
                            modifier = Modifier.testTag("export_tape_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy History",
                                tint = primaryColor
                            )
                        }

                        IconButton(
                            onClick = { showClearDialog = true },
                            modifier = Modifier.testTag("clear_tape_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Clear History",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Clear Time Separation Tabs: All Time, Today, Weekly, Monthly, Yearly
            ScrollableTabRow(
                selectedTabIndex = selectedTimeSplit.ordinal,
                edgePadding = 0.dp,
                containerColor = Color.Transparent,
                divider = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("history_time_split_tabs")
            ) {
                HistoryTimeSplit.values().forEach { split ->
                    val isSelected = selectedTimeSplit == split
                    Tab(
                        selected = isSelected,
                        onClick = { onTimeSplitChange(split) },
                        text = {
                            Text(
                                text = split.title,
                                fontFamily = DigitalMonospaceFont,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Running Sum of Multi-Selected Calculations
            AnimatedVisibility(visible = selectedIds.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, primaryColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
                    color = primaryColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Selected (${selectedIds.size}) Total:",
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = com.example.domain.util.PkrNumberFormatter.formatPkrAmount(selectedSum, decimals = 2),
                                fontWeight = FontWeight.Bold,
                                fontFamily = DigitalMonospaceFont,
                                fontSize = 15.sp,
                                color = primaryColor
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { selectedIds = emptySet() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear Selection",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // History Calculation Cards List (Unlimited and Persistent)
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No saved calculations for ${selectedTimeSplit.title.lowercase()}.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredList, key = { it.id }) { item ->
                        val isSelected = item.id in selectedIds
                        val dateFormat = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
                        val timeStr = dateFormat.format(Date(item.timestamp))

                        TapeItemCard(
                            entity = item,
                            timeStr = timeStr,
                            isSelected = isSelected,
                            primaryColor = primaryColor,
                            isDark = isDark,
                            onToggleSelect = {
                                selectedIds = if (isSelected) selectedIds - item.id else selectedIds + item.id
                            },
                            onRestore = {
                                onRestoreItem(item)
                                onDismiss()
                            },
                            onReuseExpr = {
                                onReuseExpression(item.expression)
                                onDismiss()
                            },
                            onDelete = { onDeleteItem(item) },
                            onTogglePin = { onTogglePin(item) },
                            onEditNote = { editingEntity = item }
                        )
                    }
                }
            }
        }
    }

    // Edit Note Dialog
    editingEntity?.let { entity ->
        var noteText by remember { mutableStateOf(entity.note) }

        AlertDialog(
            onDismissRequest = { editingEntity = null },
            shape = RoundedCornerShape(24.dp),
            title = { Text("Edit Note", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "${entity.expression} = ${entity.result}",
                        fontFamily = DigitalMonospaceFont,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LiquidGlassTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text("Note / Description") },
                        placeholder = { Text("Enter calculation note...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                LiquidGlassButton(
                    onClick = {
                        onUpdateNote(entity, noteText)
                        editingEntity = null
                    },
                    style = LiquidGlassButtonStyle.PRIMARY
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                LiquidGlassButton(
                    onClick = { editingEntity = null },
                    style = LiquidGlassButtonStyle.SECONDARY
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear All Confirmation
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            shape = RoundedCornerShape(24.dp),
            title = { Text("Clear Entire History?") },
            text = { Text("All saved calculations will be permanently deleted.") },
            confirmButton = {
                LiquidGlassButton(
                    onClick = {
                        onClearAll()
                        showClearDialog = false
                    },
                    style = LiquidGlassButtonStyle.DESTRUCTIVE
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                LiquidGlassButton(
                    onClick = { showClearDialog = false },
                    style = LiquidGlassButtonStyle.SECONDARY
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun TapeItemCard(
    entity: CalculationEntity,
    timeStr: String,
    isSelected: Boolean,
    primaryColor: Color,
    isDark: Boolean,
    onToggleSelect: () -> Unit,
    onRestore: () -> Unit,
    onReuseExpr: () -> Unit = {},
    onDelete: () -> Unit,
    onTogglePin: () -> Unit,
    onEditNote: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlassSurface(
                shape = RoundedCornerShape(18.dp),
                isDark = isDark,
                accentColor = if (isSelected) primaryColor else (if (isDark) Color.White else primaryColor),
                isFocused = isSelected,
                elevation = if (isSelected) 8.dp else 2.dp
            )
            .clickable { onToggleSelect() }
            .testTag("tape_item_${entity.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = timeStr,
                    fontFamily = DigitalMonospaceFont,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onTogglePin,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (entity.isPinned) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Pin",
                            tint = if (entity.isPinned) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onEditNote,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Note",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Expression with Digital Monospace Font
            Text(
                text = entity.expression,
                fontSize = 14.sp,
                fontFamily = DigitalMonospaceFont,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "= ${entity.result}",
                    fontSize = 20.sp,
                    fontFamily = DigitalMonospaceFont,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { onReuseExpr() }
                            .border(1.dp, primaryColor.copy(alpha = 0.5f), CircleShape)
                            .testTag("reuse_expr_btn_${entity.id}"),
                        shape = CircleShape,
                        color = primaryColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            "+ Expr",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = primaryColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    Button(
                        onClick = onRestore,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("reuse_result_btn_${entity.id}")
                    ) {
                        Text(
                            "+ Result",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.Black else Color.White
                        )
                    }
                }
            }

            if (entity.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Note: ${entity.note}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

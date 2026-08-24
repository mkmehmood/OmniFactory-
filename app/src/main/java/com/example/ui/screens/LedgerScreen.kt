package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.LedgerTransactionEntity
import com.example.domain.ledger.LedgerCategoryIcons
import com.example.domain.ledger.LedgerSummary
import com.example.domain.ledger.LedgerType
import com.example.domain.util.IndianNumberFormatter
import com.example.ui.LedgerUiState
import com.example.ui.LedgerViewModel
import com.example.ui.theme.DigitalMonospaceFont
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerScreen(
    viewModel: LedgerViewModel,
    onOpenCalculator: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val isDark = MaterialTheme.colorScheme.background.red < 0.2f
    val primaryColor = MaterialTheme.colorScheme.primary

    var transactionToDelete by remember { mutableStateOf<LedgerTransactionEntity?>(null) }

    LaunchedEffect(Unit) {
        viewModel.userMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("ledger_screen_container")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Grand Hero Card: Cash In Hand with Cash, Bank & Others breakdown
            LedgerHeroBalanceCard(
                summary = uiState.summary,
                onModePillClick = { modeFilter ->
                    viewModel.openHistoryForMode(modeFilter)
                },
                isDark = isDark
            )

            // 2. Metric Stat Cards (Total Cash In, Total Cash Out, Net Flow)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LedgerMetricCard(
                    title = "Total Cash In",
                    amount = uiState.summary.formattedCashIn,
                    count = "${uiState.summary.cashInCount} entries",
                    icon = Icons.Default.ArrowDownward,
                    accentColor = Color(0xFF10B981),
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )

                LedgerMetricCard(
                    title = "Total Cash Out",
                    amount = uiState.summary.formattedCashOut,
                    count = "${uiState.summary.cashOutCount} entries",
                    icon = Icons.Default.ArrowUpward,
                    accentColor = Color(0xFFEF4444),
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )

                val isSurplus = uiState.summary.totalCashInHand >= 0
                val netAccent = if (isSurplus) Color(0xFF0284C7) else Color(0xFFF59E0B)
                LedgerMetricCard(
                    title = "Net Balance",
                    amount = uiState.summary.formattedNetCashFlow,
                    count = if (isSurplus) "Surplus" else "Deficit",
                    icon = if (isSurplus) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                    accentColor = netAccent,
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
            }

            // 3. Fast Action Buttons: Only + Cash In and - Cash Out
            LedgerFastActionBar(
                onAddCashIn = { viewModel.openAddTransactionSheet(LedgerType.CASH_IN) },
                onAddCashOut = { viewModel.openAddTransactionSheet(LedgerType.CASH_OUT) }
            )

            // 4. Payment Channel & Mode Distribution Card
            if (uiState.summary.cashByMode.isNotEmpty()) {
                LedgerPaymentModeBreakdownCard(
                    modes = uiState.summary.cashByMode,
                    onModeClick = { modeName ->
                        viewModel.openHistoryForMode(modeName)
                    },
                    isDark = isDark
                )
            }

            // 5. Category Accounts Summary Card
            if (uiState.summary.categoryBreakdown.isNotEmpty()) {
                LedgerCategoryBreakdownCard(
                    categories = uiState.summary.categoryBreakdown,
                    primaryColor = primaryColor,
                    isDark = isDark
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // ==========================================
        // ADD / EDIT TRANSACTION BOTTOM SHEET
        // ==========================================
        if (uiState.isAddSheetOpen) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.closeAddSheet() },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = if (isDark) Color(0xFF101726) else Color(0xFFF8FAFC),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                LedgerAddEditSheetContent(
                    uiState = uiState,
                    onTypeChange = { viewModel.setFormType(it) },
                    onAmountChange = { viewModel.setFormAmountText(it) },
                    onCategoryChange = { viewModel.setFormCategory(it) },
                    onPaymentModeChange = { viewModel.setFormPaymentMode(it) },
                    onPartyNameChange = { viewModel.setFormPartyName(it) },
                    onRemarksChange = { viewModel.setFormRemarks(it) },
                    onRefNoChange = { viewModel.setFormReferenceNo(it) },
                    onDateChange = { viewModel.setFormDateMillis(it) },
                    onSave = { viewModel.saveFormTransaction() },
                    onCancel = { viewModel.closeAddSheet() },
                    primaryColor = primaryColor,
                    isDark = isDark
                )
            }
        }

        // ==========================================
        // DEDICATED HISTORY PANEL SHEET (TOPBAR LAUNCHER & PILL DRILLDOWN)
        // ==========================================
        if (uiState.isHistoryPanelOpen) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.setHistoryPanelOpen(false) },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                LedgerHistoryPanelSheetContent(
                    uiState = uiState,
                    viewModel = viewModel,
                    onClose = { viewModel.setHistoryPanelOpen(false) },
                    onEditTx = { tx ->
                        viewModel.setHistoryPanelOpen(false)
                        viewModel.openEditTransaction(tx)
                    },
                    onDeleteTx = { tx ->
                        transactionToDelete = tx
                    },
                    primaryColor = primaryColor,
                    isDark = isDark
                )
            }
        }

        // ==========================================
        // TRANSACTION DETAIL MODAL
        // ==========================================
        if (uiState.isDetailDialogOpen && uiState.selectedTransactionForDetail != null) {
            val tx = uiState.selectedTransactionForDetail!!
            LedgerDetailDialog(
                tx = tx,
                onDismiss = { viewModel.closeTransactionDetail() },
                onEdit = {
                    viewModel.closeTransactionDetail()
                    viewModel.openEditTransaction(tx)
                },
                onDelete = {
                    viewModel.closeTransactionDetail()
                    transactionToDelete = tx
                },
                onCopy = {
                    val voucher = viewModel.buildSingleTransactionReport(tx)
                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Voucher", voucher))
                    Toast.makeText(context, "Voucher copied", Toast.LENGTH_SHORT).show()
                },
                primaryColor = primaryColor,
                isDark = isDark
            )
        }

        // ==========================================
        // DELETE CONFIRMATION DIALOG
        // ==========================================
        if (transactionToDelete != null) {
            val tx = transactionToDelete!!
            AlertDialog(
                onDismissRequest = { transactionToDelete = null },
                title = { Text("Delete Transaction Record?") },
                text = {
                    Text("Are you sure you want to delete this ${tx.type.replace('_', ' ')} entry of Rs. ${IndianNumberFormatter.formatIndianDouble(tx.amount, 2)} (${tx.category})?")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteTransaction(tx)
                            transactionToDelete = null
                        }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { transactionToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

// =========================================================================
// 1. HERO CASH IN HAND CARD WITH CASH, BANK & OTHERS
// =========================================================================

@Composable
private fun LedgerHeroBalanceCard(
    summary: LedgerSummary,
    onModePillClick: (String) -> Unit,
    isDark: Boolean
) {
    val isPositive = summary.totalCashInHand >= 0
    val cardColor = if (isDark) Color(0xFF0D253A) else Color(0xFF0F172A)
    val accentColor = if (isPositive) Color(0xFF10B981) else Color(0xFFEF4444)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(
                BorderStroke(1.dp, if (isDark) Color(0x3338BDF8) else Color(0x22000000)),
                RoundedCornerShape(18.dp)
            )
            .testTag("ledger_hero_balance_card"),
        shape = RoundedCornerShape(18.dp),
        color = cardColor,
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TOTAL CASH IN HAND",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontFamily = DigitalMonospaceFont,
                        color = Color(0xFF94A3B8)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0x25FFFFFF)
                ) {
                    Text(
                        text = "${summary.transactionCount} Entries",
                        fontSize = 10.sp,
                        fontFamily = DigitalMonospaceFont,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFE2E8F0),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = summary.formattedCashInHand,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = DigitalMonospaceFont,
                color = if (isPositive) Color(0xFF34D399) else Color(0xFFF87171)
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(thickness = 0.8.dp, color = Color(0x22FFFFFF))
            Spacer(modifier = Modifier.height(10.dp))

            // Payment Mode Breakdown Pills: Cash, Bank, and OTHERS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ModeMiniPill(
                    label = "Cash",
                    amount = summary.cashBalance,
                    icon = Icons.Default.Payments,
                    onClick = { onModePillClick("CASH") },
                    modifier = Modifier.weight(1f)
                )
                ModeMiniPill(
                    label = "Bank",
                    amount = summary.bankBalance,
                    icon = Icons.Default.AccountBalance,
                    onClick = { onModePillClick("BANK") },
                    modifier = Modifier.weight(1f)
                )
                ModeMiniPill(
                    label = "Others",
                    amount = summary.otherModesBalance,
                    countSubtitle = "${summary.otherModesCount} txns",
                    icon = Icons.Default.MoreHoriz,
                    onClick = { onModePillClick("OTHERS") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ModeMiniPill(
    label: String,
    amount: Double,
    countSubtitle: String? = null,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .testTag("mode_pill_${label.lowercase()}"),
        shape = RoundedCornerShape(8.dp),
        color = Color(0x1FFFFFFF)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontFamily = DigitalMonospaceFont,
                    color = Color(0xFFCBD5E1),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Rs. ${IndianNumberFormatter.formatIndianDouble(amount, 0)}",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                color = if (amount >= 0) Color.White else Color(0xFFF87171),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (countSubtitle != null) {
                Text(
                    text = countSubtitle,
                    fontSize = 8.5.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}

// =========================================================================
// 2. METRIC STAT CARDS
// =========================================================================

@Composable
private fun LedgerMetricCard(
    title: String,
    amount: String,
    count: String,
    icon: ImageVector,
    accentColor: Color,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        color = if (isDark) Color(0xFF141E33) else Color(0xFFFFFFFF)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(13.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = amount,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                color = accentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = count,
                fontSize = 9.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

// =========================================================================
// 3. FAST ACTION BUTTONS (ONLY CASH IN & CASH OUT)
// =========================================================================

@Composable
private fun LedgerFastActionBar(
    onAddCashIn: () -> Unit,
    onAddCashOut: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // + Cash In
        Button(
            onClick = onAddCashIn,
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("ledger_add_cash_in_btn"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF10B981),
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(horizontal = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowDownward,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "+ Cash In",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // - Cash Out
        Button(
            onClick = onAddCashOut,
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("ledger_add_cash_out_btn"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFEF4444),
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(horizontal = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowUpward,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "- Cash Out",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// =========================================================================
// 4. PAYMENT CHANNEL DISTRIBUTION CARD
// =========================================================================

@Composable
private fun LedgerPaymentModeBreakdownCard(
    modes: Map<String, Double>,
    onModeClick: (String) -> Unit,
    isDark: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, if (isDark) Color(0x22FFFFFF) else Color(0x18000000), RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        color = if (isDark) Color(0xFF141E33) else Color(0xFFFFFFFF)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PAYMENT CHANNELS SUMMARY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        fontFamily = DigitalMonospaceFont,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                modes.forEach { (modeName, netAmount) ->
                    val isPositive = netAmount >= 0
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onModeClick(modeName) }
                            .padding(vertical = 4.dp, horizontal = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = modeName,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${if (isPositive) "+ " else "- "}Rs. ${IndianNumberFormatter.formatIndianDouble(kotlin.math.abs(netAmount), 2)}",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = DigitalMonospaceFont,
                            color = if (isPositive) Color(0xFF10B981) else Color(0xFFEF4444)
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// 5. CATEGORY BREAKDOWN CARD
// =========================================================================

@Composable
private fun LedgerCategoryBreakdownCard(
    categories: Map<String, Double>,
    primaryColor: Color,
    isDark: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, if (isDark) Color(0x22FFFFFF) else Color(0x18000000), RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        color = if (isDark) Color(0xFF141E33) else Color(0xFFFFFFFF)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Category,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "CATEGORIES & ACCOUNTS SUMMARY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    fontFamily = DigitalMonospaceFont,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                categories.entries.sortedByDescending { it.value }.take(8).forEach { (catName, amount) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = catName,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Rs. ${IndianNumberFormatter.formatIndianDouble(amount, 2)}",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = DigitalMonospaceFont,
                            color = primaryColor
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// 6. ADD / EDIT TRANSACTION BOTTOM SHEET CONTENT
// (With seamless Auto-suggest & Auto-save for Categories and Payment Modes)
// =========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LedgerAddEditSheetContent(
    uiState: LedgerUiState,
    onTypeChange: (LedgerType) -> Unit,
    onAmountChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onPaymentModeChange: (String) -> Unit,
    onPartyNameChange: (String) -> Unit,
    onRemarksChange: (String) -> Unit,
    onRefNoChange: (String) -> Unit,
    onDateChange: (Long) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    primaryColor: Color,
    isDark: Boolean
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }
    val isEditing = uiState.editingTransaction != null

    // Track focus for category and payment mode
    var isCategoryFocused by remember { mutableStateOf(false) }
    var isModeFocused by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("ledger_add_edit_sheet"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isEditing) "Edit Cashbook Entry" else "New Cashbook Transaction",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                color = MaterialTheme.colorScheme.onSurface
            )

            IconButton(onClick = onCancel, modifier = Modifier.size(32.dp)) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
            }
        }

        // 1. Transaction Type Toggle: Only Cash In and Cash Out
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, if (isDark) Color(0x28FFFFFF) else Color(0x18000000), RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            color = if (isDark) Color(0x18FFFFFF) else Color(0x0A000000)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Cash In Tab
                val isCashIn = uiState.formType == LedgerType.CASH_IN
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isCashIn) Color(0xFF10B981) else Color.Transparent)
                        .clickable { onTypeChange(LedgerType.CASH_IN) }
                        .testTag("form_type_cash_in"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = if (isCashIn) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Cash In",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCashIn) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Cash Out Tab
                val isCashOut = uiState.formType == LedgerType.CASH_OUT
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isCashOut) Color(0xFFEF4444) else Color.Transparent)
                        .clickable { onTypeChange(LedgerType.CASH_OUT) }
                        .testTag("form_type_cash_out"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = if (isCashOut) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Cash Out",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCashOut) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 2. Transaction Amount (Clean Input)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "TRANSACTION AMOUNT (RS.) *",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = uiState.formAmountText,
                onValueChange = onAmountChange,
                placeholder = { Text("0.00", fontFamily = DigitalMonospaceFont) },
                leadingIcon = {
                    Text(
                        text = "Rs.",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DigitalMonospaceFont,
                        color = uiState.formType.color,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                },
                trailingIcon = {
                    if (uiState.formAmountText.isNotEmpty()) {
                        IconButton(onClick = { onAmountChange("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = uiState.formType.color,
                    unfocusedBorderColor = if (isDark) Color(0x33FFFFFF) else Color(0x28000000)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("form_amount_input")
            )
        }

        // 3. Category Field (Auto-suggests as user types, auto-saves on save, hidden until typed)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "CATEGORY / HEAD OF ACCOUNT *",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = uiState.formCategory,
                onValueChange = onCategoryChange,
                placeholder = { Text("Type category (e.g. Sales, Home, Noran Shah...)") },
                leadingIcon = {
                    Icon(
                        imageVector = LedgerCategoryIcons.getCategoryIcon(uiState.formCategory),
                        contentDescription = null,
                        tint = uiState.formType.color,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (uiState.formCategory.isNotEmpty()) {
                        IconButton(onClick = { onCategoryChange("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = if (isDark) Color(0x33FFFFFF) else Color(0x28000000)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { isCategoryFocused = it.isFocused }
                    .testTag("form_category_input")
            )

            // Dynamic Auto-suggestions: Shown ONLY when matching items exist as user types
            val matchingCategories = remember(uiState.formCategory, uiState.availableCategories, isCategoryFocused) {
                val input = uiState.formCategory.trim()
                if (input.isEmpty()) {
                    emptyList()
                } else {
                    uiState.availableCategories.filter {
                        it.contains(input, ignoreCase = true) && !it.equals(input, ignoreCase = true)
                    }
                }
            }

            if (matchingCategories.isNotEmpty()) {
                Text(
                    text = "Matching suggestions:",
                    fontSize = 10.5.sp,
                    color = primaryColor,
                    fontWeight = FontWeight.SemiBold
                )
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(matchingCategories) { cat ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = primaryColor.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.35f)),
                            modifier = Modifier.clickable { onCategoryChange(cat) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = LedgerCategoryIcons.getCategoryIcon(cat),
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = cat,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = primaryColor
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Payment Mode Field (Auto-suggests as user types, auto-saves on save, hidden until typed)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "PAYMENT MODE / CHANNEL *",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = uiState.formPaymentMode,
                onValueChange = onPaymentModeChange,
                placeholder = { Text("Type payment mode (e.g. Cash, Bank Transfer, Easypaisa...)") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (uiState.formPaymentMode.isNotEmpty()) {
                        IconButton(onClick = { onPaymentModeChange("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = if (isDark) Color(0x33FFFFFF) else Color(0x28000000)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { isModeFocused = it.isFocused }
                    .testTag("form_payment_mode_input")
            )

            // Dynamic Auto-suggestions for Payment Mode: Shown ONLY when matching items exist as user types
            val matchingModes = remember(uiState.formPaymentMode, uiState.availablePaymentModes, isModeFocused) {
                val input = uiState.formPaymentMode.trim()
                if (input.isEmpty()) {
                    emptyList()
                } else {
                    uiState.availablePaymentModes.filter {
                        it.contains(input, ignoreCase = true) && !it.equals(input, ignoreCase = true)
                    }
                }
            }

            if (matchingModes.isNotEmpty()) {
                Text(
                    text = "Matching payment modes:",
                    fontSize = 10.5.sp,
                    color = primaryColor,
                    fontWeight = FontWeight.SemiBold
                )
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(matchingModes) { mode ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = primaryColor.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.35f)),
                            modifier = Modifier.clickable { onPaymentModeChange(mode) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Payments,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = mode,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = primaryColor
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Date Selector
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "TRANSACTION DATE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, if (isDark) Color(0x33FFFFFF) else Color(0x28000000), RoundedCornerShape(10.dp))
                    .clickable { showDatePicker = true },
                color = if (isDark) Color(0x18FFFFFF) else Color(0x0A000000)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = dateFormat.format(Date(uiState.formDateMillis)),
                        fontSize = 13.sp,
                        fontFamily = DigitalMonospaceFont,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // 6. Party / Contact Name
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "PARTY / CONTACT NAME (OPTIONAL)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = uiState.formPartyName,
                onValueChange = onPartyNameChange,
                placeholder = { Text("Customer, Vendor, Employee name...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("form_party_name_input")
            )

            if (uiState.distinctParties.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(uiState.distinctParties) { party ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isDark) Color(0x1AFFFFFF) else Color(0x0E000000),
                            modifier = Modifier.clickable { onPartyNameChange(party) }
                        ) {
                            Text(
                                text = party,
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // 7. Reference No / Bill No
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "REFERENCE / BILL / INVOICE NO. (OPTIONAL)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = uiState.formReferenceNo,
                onValueChange = onRefNoChange,
                placeholder = { Text("Inv #, Bill #, Cheque #, Transaction ID...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("form_ref_no_input")
            )
        }

        // 8. Remarks / Note
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "REMARKS / NOTES (OPTIONAL)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = uiState.formRemarks,
                onValueChange = onRemarksChange,
                placeholder = { Text("Add transaction description, item details...") },
                maxLines = 3,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("form_remarks_input")
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Save and Cancel Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDark) Color(0x22FFFFFF) else Color(0x18000000),
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text("Cancel", fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = onSave,
                enabled = !uiState.isFormSubmitting && uiState.formAmountText.isNotBlank(),
                modifier = Modifier
                    .weight(1.5f)
                    .height(50.dp)
                    .testTag("form_save_transaction_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = uiState.formType.color,
                    contentColor = Color.White
                )
            ) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEditing) "Update Entry" else "Save Transaction",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    // Material 3 Date Picker
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = uiState.formDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { onDateChange(it) }
                    showDatePicker = false
                }) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

// =========================================================================
// 7. HISTORY PANEL SHEET CONTENT WITH MODE FILTERS (ALL, CASH, BANK, OTHERS)
// =========================================================================

@Composable
private fun LedgerHistoryPanelSheetContent(
    uiState: LedgerUiState,
    viewModel: LedgerViewModel,
    onClose: () -> Unit,
    onEditTx: (LedgerTransactionEntity) -> Unit,
    onDeleteTx: (LedgerTransactionEntity) -> Unit,
    primaryColor: Color,
    isDark: Boolean
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 680.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("ledger_history_panel_sheet")
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Transaction History",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DigitalMonospaceFont,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${uiState.filteredTransactions.size} records matched",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Copy Summary Button
                IconButton(
                    onClick = {
                        val report = viewModel.buildPeriodAuditSummary()
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Audit Summary", report))
                        Toast.makeText(context, "Summary copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Summary",
                        tint = primaryColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(onClick = onClose, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Balanced Search Bar
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("Search party, category, mode, amount...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = primaryColor,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (uiState.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryColor,
                unfocusedBorderColor = if (isDark) Color(0x33FFFFFF) else Color(0x28000000)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("history_search_input")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Filter Pills: Type (All, Cash In, Cash Out)
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                TypeFilterChip(
                    label = "All Types",
                    isSelected = uiState.selectedTypeFilter == "ALL",
                    onClick = { viewModel.setTypeFilter("ALL") }
                )
            }
            item {
                TypeFilterChip(
                    label = "Cash In",
                    isSelected = uiState.selectedTypeFilter == "CASH_IN",
                    color = Color(0xFF10B981),
                    onClick = { viewModel.setTypeFilter("CASH_IN") }
                )
            }
            item {
                TypeFilterChip(
                    label = "Cash Out",
                    isSelected = uiState.selectedTypeFilter == "CASH_OUT",
                    color = Color(0xFFEF4444),
                    onClick = { viewModel.setTypeFilter("CASH_OUT") }
                )
            }
            item {
                Box(
                    modifier = Modifier
                        .height(26.dp)
                        .width(1.dp)
                        .background(if (isDark) Color(0x33FFFFFF) else Color(0x22000000))
                )
            }
            // Mode Filters: All, Cash, Bank, Others
            item {
                TypeFilterChip(
                    label = "All Channels",
                    isSelected = uiState.selectedModeFilter == "ALL",
                    onClick = { viewModel.setModeFilter("ALL") }
                )
            }
            item {
                TypeFilterChip(
                    label = "Cash",
                    isSelected = uiState.selectedModeFilter == "CASH",
                    onClick = { viewModel.setModeFilter("CASH") }
                )
            }
            item {
                TypeFilterChip(
                    label = "Bank",
                    isSelected = uiState.selectedModeFilter == "BANK",
                    onClick = { viewModel.setModeFilter("BANK") }
                )
            }
            item {
                TypeFilterChip(
                    label = "Others",
                    isSelected = uiState.selectedModeFilter == "OTHERS",
                    color = Color(0xFF8B5CF6),
                    onClick = { viewModel.setModeFilter("OTHERS") }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Transaction List
        if (uiState.filteredTransactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (uiState.searchQuery.isNotBlank() || uiState.selectedModeFilter != "ALL" || uiState.selectedTypeFilter != "ALL")
                            "No transactions match the selected filters"
                        else
                            "No cashbook entries yet",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.filteredTransactions, key = { it.id }) { tx ->
                    LedgerTransactionRowItem(
                        tx = tx,
                        onClick = { viewModel.openTransactionDetail(tx) },
                        onEdit = { onEditTx(tx) },
                        onDelete = { onDeleteTx(tx) },
                        isDark = isDark
                    )
                }
            }
        }
    }
}

@Composable
private fun TypeFilterChip(
    label: String,
    isSelected: Boolean,
    color: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) color.copy(alpha = 0.2f) else Color(0x10000000),
        border = BorderStroke(
            1.dp,
            if (isSelected) color else Color(0x22000000)
        )
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) color else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun LedgerTransactionRowItem(
    tx: LedgerTransactionEntity,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    isDark: Boolean
) {
    val isExpense = tx.type == "CASH_OUT"
    val accentColor = if (isExpense) Color(0xFFEF4444) else Color(0xFF10B981)
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.US) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                1.dp,
                if (isDark) Color(0x22FFFFFF) else Color(0x18000000),
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .testTag("tx_item_${tx.id}"),
        shape = RoundedCornerShape(12.dp),
        color = if (isDark) Color(0xFF141E33) else Color(0xFFFFFFFF)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Direction Icon Badge
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentColor.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isExpense) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = tx.category,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (tx.partyName.isNotBlank()) {
                            Text(
                                text = " • ${tx.partyName}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = dateFormat.format(Date(tx.dateMillis)),
                            fontSize = 10.5.sp,
                            fontFamily = DigitalMonospaceFont,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = " | ${tx.paymentMode}",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (tx.referenceNo.isNotBlank()) {
                            Text(
                                text = " #${tx.referenceNo}",
                                fontSize = 10.5.sp,
                                fontFamily = DigitalMonospaceFont,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Amount Column
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isExpense) "-" else "+"}Rs. ${IndianNumberFormatter.formatIndianDouble(tx.amount, 2)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont,
                    color = accentColor
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(26.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(26.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// 8. TRANSACTION DETAIL DIALOG
// =========================================================================

@Composable
private fun LedgerDetailDialog(
    tx: LedgerTransactionEntity,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCopy: () -> Unit,
    primaryColor: Color,
    isDark: Boolean
) {
    val isExpense = tx.type == "CASH_OUT"
    val accentColor = if (isExpense) Color(0xFFEF4444) else Color(0xFF10B981)
    val dateFormat = remember { SimpleDateFormat("dd MMMM yyyy, hh:mm:ss a", Locale.US) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isExpense) "Cash Out Voucher" else "Cash In Voucher",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DigitalMonospaceFont
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = accentColor.copy(alpha = 0.12f)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Rs. ${IndianNumberFormatter.formatIndianDouble(tx.amount, 2)}",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = DigitalMonospaceFont,
                            color = accentColor
                        )
                        Text(
                            text = if (isExpense) "CASH OUTFLOW" else "CASH INFLOW",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = DigitalMonospaceFont,
                            color = accentColor
                        )
                    }
                }

                DetailRow(label = "Category", value = tx.category)
                DetailRow(label = "Payment Mode", value = tx.paymentMode)
                if (tx.partyName.isNotBlank()) DetailRow(label = "Party / Name", value = tx.partyName)
                if (tx.referenceNo.isNotBlank()) DetailRow(label = "Reference #", value = tx.referenceNo)
                if (tx.remarks.isNotBlank()) DetailRow(label = "Remarks", value = tx.remarks)
                DetailRow(label = "Date & Time", value = dateFormat.format(Date(tx.dateMillis)))
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onCopy) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy")
                }
                TextButton(onClick = onEdit) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit")
                }
                TextButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End
        )
    }
}

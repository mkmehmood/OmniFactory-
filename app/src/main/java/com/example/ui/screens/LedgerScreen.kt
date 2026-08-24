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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Store
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.LedgerTransactionEntity
import com.example.domain.ledger.LedgerSummary
import com.example.domain.ledger.LedgerType
import com.example.domain.util.IndianNumberFormatter
import com.example.ui.LedgerUiState
import com.example.ui.LedgerViewModel
import com.example.ui.components.SimpleCalculatorDialog
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
    val clipboardManager = LocalClipboardManager.current

    val isDark = MaterialTheme.colorScheme.background.red < 0.2f
    val primaryColor = MaterialTheme.colorScheme.primary

    var showQuickCalcDialog by remember { mutableStateOf(false) }
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
            // 1. Grand Hero Card: Cash In Hand (Net Closing Balance)
            LedgerHeroBalanceCard(
                summary = uiState.summary,
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
                    title = "Net Cashflow",
                    amount = uiState.summary.formattedNetCashFlow,
                    count = if (isSurplus) "Surplus" else "Deficit",
                    icon = if (isSurplus) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                    accentColor = netAccent,
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
            }

            // 3. Quick Action Buttons (+ Cash In, - Cash Out, + Opening / Capital)
            LedgerFastActionBar(
                onAddCashIn = { viewModel.openAddTransactionSheet(LedgerType.CASH_IN) },
                onAddCashOut = { viewModel.openAddTransactionSheet(LedgerType.CASH_OUT) },
                onAddOpening = { viewModel.openAddTransactionSheet(LedgerType.OPENING_BALANCE) },
                isDark = isDark
            )

            // 4. Prominent Transaction History Launcher Card (History is opened here or via top bar icon)
            LedgerHistoryLauncherCard(
                totalCount = uiState.allTransactions.size,
                onClick = { viewModel.setHistoryPanelOpen(true) },
                primaryColor = primaryColor,
                isDark = isDark
            )

            // 5. Payment Channel & Mode Distribution Card
            if (uiState.summary.cashByMode.isNotEmpty()) {
                LedgerPaymentModeBreakdownCard(
                    modes = uiState.summary.cashByMode,
                    isDark = isDark
                )
            }

            // 6. Category Accounts Summary Card
            if (uiState.summary.categoryBreakdown.isNotEmpty()) {
                LedgerCategoryBreakdownCard(
                    categories = uiState.summary.categoryBreakdown,
                    primaryColor = primaryColor,
                    isDark = isDark
                )
            }

            // 7. Quick Tools & Calculator Launcher Card
            LedgerQuickToolsCard(
                onOpenCalculator = onOpenCalculator,
                onCopySummary = {
                    val report = viewModel.buildPeriodAuditSummary()
                    clipboardManager.setText(AnnotatedString(report))
                    Toast.makeText(context, "Ledger summary copied to clipboard", Toast.LENGTH_SHORT).show()
                },
                primaryColor = primaryColor,
                isDark = isDark
            )

            Spacer(modifier = Modifier.height(20.dp))
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
                    onAppendPreset = { viewModel.appendPresetAmount(it) },
                    onCategoryChange = { viewModel.setFormCategory(it) },
                    onPaymentModeChange = { viewModel.setFormPaymentMode(it) },
                    onPartyNameChange = { viewModel.setFormPartyName(it) },
                    onRemarksChange = { viewModel.setFormRemarks(it) },
                    onRefNoChange = { viewModel.setFormReferenceNo(it) },
                    onDateChange = { viewModel.setFormDateMillis(it) },
                    onOpenCalculator = { showQuickCalcDialog = true },
                    onSave = { viewModel.saveFormTransaction() },
                    onCancel = { viewModel.closeAddSheet() },
                    primaryColor = primaryColor,
                    isDark = isDark
                )
            }
        }

        // ==========================================
        // DEDICATED HISTORY PANEL / AUDIT MODAL SHEET
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
                    transactionToDelete = tx
                },
                onCopy = {
                    val voucher = viewModel.buildSingleTransactionReport(tx)
                    clipboardManager.setText(AnnotatedString(voucher))
                    Toast.makeText(context, "Voucher copied to clipboard", Toast.LENGTH_SHORT).show()
                },
                primaryColor = primaryColor,
                isDark = isDark
            )
        }

        // ==========================================
        // QUICK CALCULATOR DIALOG
        // ==========================================
        if (showQuickCalcDialog) {
            SimpleCalculatorDialog(
                onDismiss = { showQuickCalcDialog = false },
                onResultSelected = { calculatedValue ->
                    viewModel.setFormAmountText(calculatedValue)
                    showQuickCalcDialog = false
                }
            )
        }

        // ==========================================
        // DELETE CONFIRMATION DIALOG
        // ==========================================
        if (transactionToDelete != null) {
            val tx = transactionToDelete!!
            AlertDialog(
                onDismissRequest = { transactionToDelete = null },
                title = { Text("Delete Transaction Entry", fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "Are you sure you want to permanently delete this ${tx.category} record of Rs. ${IndianNumberFormatter.formatIndianDouble(tx.amount, 2)}?"
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteTransaction(tx)
                            transactionToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete", fontWeight = FontWeight.Bold)
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
// 1. HERO BALANCE CARD
// =========================================================================

@Composable
private fun LedgerHeroBalanceCard(
    summary: LedgerSummary,
    isDark: Boolean
) {
    val isBalancePositive = summary.totalCashInHand >= 0
    val balanceColor = if (isBalancePositive) Color(0xFF10B981) else Color(0xFFEF4444)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(
                BorderStroke(1.5.dp, balanceColor.copy(alpha = 0.5f)),
                RoundedCornerShape(18.dp)
            )
            .testTag("cash_in_hand_card"),
        shape = RoundedCornerShape(18.dp),
        color = if (isDark) Color(0xFF111A2E) else Color(0xFFFFFFFF),
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(balanceColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = balanceColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "TOTAL CASH IN HAND",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontFamily = DigitalMonospaceFont,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = balanceColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (isBalancePositive) "POSITIVE SURPLUS" else "DEFICIT SHORTFALL",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DigitalMonospaceFont,
                        color = balanceColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Big Balance Digits
            Text(
                text = summary.formattedCashInHand,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = DigitalMonospaceFont,
                color = balanceColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(thickness = 0.8.dp, color = if (isDark) Color(0x22FFFFFF) else Color(0x18000000))
            Spacer(modifier = Modifier.height(10.dp))

            // Payment Mode Breakdown Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val cashNet = summary.cashByMode["Cash"] ?: summary.cashByMode["CASH"] ?: 0.0
                val bankNet = summary.cashByMode["Bank"] ?: summary.cashByMode["Bank Transfer"] ?: summary.cashByMode["BANK_TRANSFER"] ?: 0.0
                val onlineNet = summary.cashByMode["Online"] ?: summary.cashByMode["UPI"] ?: summary.cashByMode["UPI / Digital"] ?: summary.cashByMode["UPI_DIGITAL"] ?: 0.0

                ModeMiniPill(
                    label = "Cash Balance",
                    amount = cashNet,
                    icon = Icons.Default.Payments,
                    modifier = Modifier.weight(1f),
                    isDark = isDark
                )
                ModeMiniPill(
                    label = "Bank / Cheque",
                    amount = bankNet,
                    icon = Icons.Default.AccountBalance,
                    modifier = Modifier.weight(1f),
                    isDark = isDark
                )
                ModeMiniPill(
                    label = "Online / Other",
                    amount = onlineNet,
                    icon = Icons.Default.AccountBalanceWallet,
                    modifier = Modifier.weight(1f),
                    isDark = isDark
                )
            }
        }
    }
}

@Composable
private fun ModeMiniPill(
    label: String,
    amount: Double,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    isDark: Boolean
) {
    Surface(
        modifier = modifier.clip(RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        color = if (isDark) Color(0x18FFFFFF) else Color(0x0C000000)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    fontSize = 9.sp,
                    fontFamily = DigitalMonospaceFont,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Rs. ${IndianNumberFormatter.formatIndianDouble(amount, 0)}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                color = if (amount >= 0) MaterialTheme.colorScheme.onSurface else Color(0xFFEF4444),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
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
                fontSize = 13.5.sp,
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
// 3. FAST ACTION BUTTONS
// =========================================================================

@Composable
private fun LedgerFastActionBar(
    onAddCashIn: () -> Unit,
    onAddCashOut: () -> Unit,
    onAddOpening: () -> Unit,
    isDark: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // + Cash In
        Button(
            onClick = onAddCashIn,
            modifier = Modifier
                .weight(1.1f)
                .height(46.dp)
                .testTag("ledger_add_cash_in_btn"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF10B981),
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowDownward,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Cash In",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // - Cash Out
        Button(
            onClick = onAddCashOut,
            modifier = Modifier
                .weight(1.1f)
                .height(46.dp)
                .testTag("ledger_add_cash_out_btn"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFEF4444),
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowUpward,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Cash Out",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // + Opening / Transfer
        Button(
            onClick = onAddOpening,
            modifier = Modifier
                .weight(1f)
                .height(46.dp)
                .testTag("ledger_add_opening_btn"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0),
                contentColor = if (isDark) Color(0xFF93C5FD) else Color(0xFF1E40AF)
            ),
            contentPadding = PaddingValues(horizontal = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AccountBalanceWallet,
                contentDescription = null,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Opening",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// =========================================================================
// 4. TRANSACTION HISTORY LAUNCHER CARD
// =========================================================================

@Composable
private fun LedgerHistoryLauncherCard(
    totalCount: Int,
    onClick: () -> Unit,
    primaryColor: Color,
    isDark: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
                BorderStroke(1.dp, if (isDark) Color(0x33FFFFFF) else Color(0x20000000)),
                RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .testTag("ledger_history_launcher_card"),
        shape = RoundedCornerShape(14.dp),
        color = if (isDark) Color(0xFF151F33) else Color(0xFFFFFFFF),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(primaryColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Transaction History",
                        tint = primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Transaction History & Vouchers",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = primaryColor.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "$totalCount",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(
                        text = "Tap to search, filter, edit & copy vouchers",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// =========================================================================
// 5. PAYMENT CHANNEL DISTRIBUTION CARD
// =========================================================================

@Composable
private fun LedgerPaymentModeBreakdownCard(
    modes: Map<String, Double>,
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
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = modeName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${if (isPositive) "+ " else "- "}Rs. ${IndianNumberFormatter.formatIndianDouble(kotlin.math.abs(netAmount), 2)}",
                            fontSize = 12.sp,
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
// 6. CATEGORY BREAKDOWN CARD
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
                    text = "ACCOUNTS & CATEGORIES BREAKDOWN",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    fontFamily = DigitalMonospaceFont,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                categories.entries.sortedByDescending { it.value }.take(6).forEach { (catName, amount) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = catName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Rs. ${IndianNumberFormatter.formatIndianDouble(amount, 2)}",
                            fontSize = 12.sp,
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
// 7. QUICK TOOLS & SUMMARY CARD
// =========================================================================

@Composable
private fun LedgerQuickToolsCard(
    onOpenCalculator: () -> Unit,
    onCopySummary: () -> Unit,
    primaryColor: Color,
    isDark: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Quick Calculator
        Surface(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, if (isDark) Color(0x22FFFFFF) else Color(0x18000000), RoundedCornerShape(12.dp))
                .clickable { onOpenCalculator() },
            shape = RoundedCornerShape(12.dp),
            color = if (isDark) Color(0x14FFFFFF) else Color(0x0A000000)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Calculate,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Calculator",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Copy Ledger Summary
        Surface(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, if (isDark) Color(0x22FFFFFF) else Color(0x18000000), RoundedCornerShape(12.dp))
                .clickable { onCopySummary() },
            shape = RoundedCornerShape(12.dp),
            color = if (isDark) Color(0x14FFFFFF) else Color(0x0A000000)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Copy Summary",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// =========================================================================
// 8. ADD / EDIT TRANSACTION BOTTOM SHEET CONTENT (CUSTOM INPUTS)
// =========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LedgerAddEditSheetContent(
    uiState: LedgerUiState,
    onTypeChange: (LedgerType) -> Unit,
    onAmountChange: (String) -> Unit,
    onAppendPreset: (Double) -> Unit,
    onCategoryChange: (String) -> Unit,
    onPaymentModeChange: (String) -> Unit,
    onPartyNameChange: (String) -> Unit,
    onRemarksChange: (String) -> Unit,
    onRefNoChange: (String) -> Unit,
    onDateChange: (Long) -> Unit,
    onOpenCalculator: () -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    primaryColor: Color,
    isDark: Boolean
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }
    val isEditing = uiState.editingTransaction != null

    // Standard baseline suggestion presets
    val standardCategories = when (uiState.formType) {
        LedgerType.CASH_OUT -> listOf("Expense", "Purchase", "Salary", "Rent", "Utility", "Vendor Payment", "Transport", "Maintenance")
        LedgerType.OPENING_BALANCE -> listOf("Opening Balance", "Capital Deposit", "Cash Transfer", "Bank Withdrawal")
        else -> listOf("Sales", "Customer Payment", "Recovery", "Commission", "Service Fee", "Advance")
    }
    val combinedCategorySuggestions = (uiState.distinctCategories + standardCategories).distinct().take(10)

    val standardModes = listOf("Cash", "Bank Transfer", "Online / UPI", "Cheque", "Credit Card")
    val combinedModeSuggestions = (uiState.distinctPaymentModes + standardModes).distinct().take(8)

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

        // 1. Transaction Type Selector (Cash In, Cash Out, Opening)
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
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                LedgerType.entries.forEach { type ->
                    val isSelected = uiState.formType == type
                    val bg = if (isSelected) type.color else Color.Transparent
                    val fg = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(bg)
                            .clickable { onTypeChange(type) }
                            .testTag("form_type_${type.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = type.icon,
                                contentDescription = null,
                                tint = fg,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = type.title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = fg
                            )
                        }
                    }
                }
            }
        }

        // 2. Amount Input with Quick Calculator Launcher & Quick Increment Chips
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "TRANSACTION AMOUNT (RS.) *",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
                        IconButton(
                            onClick = onOpenCalculator,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("form_calculator_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = "Quick Calculator",
                                tint = primaryColor
                            )
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
                        .weight(1f)
                        .testTag("form_amount_input")
                )
            }

            // Quick Preset Increment Buttons (+500, +1000, +2000, +5000, +10000, +50000)
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val presets = listOf(500.0, 1000.0, 2000.0, 5000.0, 10000.0, 50000.0)
                items(presets) { preset ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isDark) Color(0x1AFFFFFF) else Color(0x0C000000),
                        border = BorderStroke(1.dp, if (isDark) Color(0x28FFFFFF) else Color(0x18000000)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onAppendPreset(preset) }
                    ) {
                        Text(
                            text = "+${IndianNumberFormatter.formatIndianDouble(preset, 0)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = DigitalMonospaceFont,
                            color = primaryColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // 3. Custom Category / Account Input Field with Quick Suggestion Chips
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
                placeholder = { Text("e.g. Sales, Rent, Raw Materials, Salaries...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        tint = uiState.formType.color,
                        modifier = Modifier.size(16.dp)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("form_category_input")
            )

            // Auto-complete suggestion chips for Category
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(combinedCategorySuggestions) { cat ->
                    val isSelected = uiState.formCategory.equals(cat, ignoreCase = true)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSelected) uiState.formType.color.copy(alpha = 0.2f) else if (isDark) Color(0x18FFFFFF) else Color(0x0A000000),
                        border = BorderStroke(1.dp, if (isSelected) uiState.formType.color else if (isDark) Color(0x28FFFFFF) else Color(0x18000000)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onCategoryChange(cat) }
                    ) {
                        Text(
                            text = cat,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) uiState.formType.color else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // 4. Custom Payment Mode / Channel Input Field with Quick Suggestion Chips
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
                placeholder = { Text("e.g. Cash, Bank Transfer, UPI, Cheque...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("form_payment_mode_input")
            )

            // Auto-complete suggestion chips for Payment Mode
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(combinedModeSuggestions) { mode ->
                    val isSelected = uiState.formPaymentMode.equals(mode, ignoreCase = true)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSelected) primaryColor.copy(alpha = 0.2f) else if (isDark) Color(0x18FFFFFF) else Color(0x0A000000),
                        border = BorderStroke(1.dp, if (isSelected) primaryColor else if (isDark) Color(0x28FFFFFF) else Color(0x18000000)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onPaymentModeChange(mode) }
                    ) {
                        Text(
                            text = mode,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
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
                    .height(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, if (isDark) Color(0x33FFFFFF) else Color(0x28000000), RoundedCornerShape(10.dp))
                    .clickable { showDatePicker = true },
                color = if (isDark) Color(0x18FFFFFF) else Color(0x0A000000)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
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

        // 6. Party / Customer / Vendor Name & Suggestions
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

            // Auto-complete suggestion chips from previous contacts
            if (uiState.distinctParties.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(uiState.distinctParties) { party ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isDark) Color(0x1AFFFFFF) else Color(0x0E000000),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onPartyNameChange(party) }
                        ) {
                            Text(
                                text = party,
                                fontSize = 10.sp,
                                color = primaryColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }

        // 7. Reference # & Remarks
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = uiState.formReferenceNo,
                onValueChange = onRefNoChange,
                label = { Text("Bill / Ref #", fontSize = 11.sp) },
                placeholder = { Text("e.g. INV-104") },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("form_ref_no_input")
            )

            OutlinedTextField(
                value = uiState.formRemarks,
                onValueChange = onRemarksChange,
                label = { Text("Remarks / Notes", fontSize = 11.sp) },
                placeholder = { Text("Notes...") },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1.5f)
                    .testTag("form_remarks_input")
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Save & Submit Button
        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("form_save_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = uiState.formType.color,
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isEditing) "Update Transaction" else "Save ${uiState.formType.title}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    if (showDatePicker) {
        val dateState = rememberDatePickerState(initialSelectedDateMillis = uiState.formDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let { onDateChange(it) }
                    showDatePicker = false
                }) {
                    Text("Done")
                }
            }
        ) {
            DatePicker(state = dateState)
        }
    }
}

// =========================================================================
// 9. DEDICATED HISTORY PANEL / AUDIT MODAL SHEET (VISIBLE ON HISTORY CLICK)
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
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(620.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("ledger_history_panel_sheet")
    ) {
        // Header Row
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
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Transaction History & Vouchers",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar & Copy Summary Action
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("ledger_history_search_input")
            )

            // Export / Copy Full Audit Summary
            IconButton(
                onClick = {
                    val report = viewModel.buildPeriodAuditSummary()
                    clipboardManager.setText(AnnotatedString(report))
                    Toast.makeText(context, "Full ledger summary copied to clipboard", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isDark) Color(0x22FFFFFF) else Color(0x10000000))
                    .testTag("ledger_history_share_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy Summary",
                    tint = primaryColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Type Filter Chips (All, Cash In, Cash Out, Opening)
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val typeFilters = listOf(
                "ALL" to "All (${uiState.allTransactions.size})",
                "CASH_IN" to "Cash In",
                "CASH_OUT" to "Cash Out",
                "OPENING_BALANCE" to "Opening"
            )
            items(typeFilters) { (typeKey, label) ->
                val isSelected = uiState.selectedTypeFilter == typeKey
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) primaryColor else if (isDark) Color(0x18FFFFFF) else Color(0x0C000000),
                    border = BorderStroke(1.dp, if (isSelected) primaryColor else if (isDark) Color(0x28FFFFFF) else Color(0x18000000)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { viewModel.setTypeFilter(typeKey) }
                ) {
                    Text(
                        text = label,
                        fontSize = 11.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Transaction List Grouped by Date
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (uiState.filteredTransactions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (uiState.searchQuery.isNotEmpty() || uiState.selectedTypeFilter != "ALL")
                                "No transactions match your search filter"
                            else "No transaction records found in ledger",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                val grouped = remember(uiState.filteredTransactions) {
                    groupTransactionsByDate(uiState.filteredTransactions)
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("ledger_transaction_history_list"),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    grouped.forEach { (dateHeader, txList) ->
                        item(key = "header_$dateHeader") {
                            LedgerDateGroupHeader(
                                dateHeader = dateHeader,
                                transactions = txList,
                                primaryColor = primaryColor,
                                isDark = isDark
                            )
                        }

                        items(txList, key = { it.id }) { tx ->
                            LedgerTransactionCard(
                                tx = tx,
                                onEdit = { onEditTx(tx) },
                                onDelete = { onDeleteTx(tx) },
                                onCopy = {
                                    val voucher = viewModel.buildSingleTransactionReport(tx)
                                    clipboardManager.setText(AnnotatedString(voucher))
                                    Toast.makeText(context, "Voucher copied", Toast.LENGTH_SHORT).show()
                                },
                                onTapDetail = { viewModel.openTransactionDetail(tx) },
                                primaryColor = primaryColor,
                                isDark = isDark
                            )
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// 10. DATE GROUP HEADER & TRANSACTION CARD
// =========================================================================

@Composable
private fun LedgerDateGroupHeader(
    dateHeader: String,
    transactions: List<LedgerTransactionEntity>,
    primaryColor: Color,
    isDark: Boolean
) {
    var dayIn = 0.0
    var dayOut = 0.0
    for (tx in transactions) {
        if (tx.type == "CASH_IN" || tx.type == "OPENING_BALANCE") dayIn += tx.amount
        else if (tx.type == "CASH_OUT") dayOut += tx.amount
    }
    val netDay = dayIn - dayOut

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        color = if (isDark) Color(0x1F223652) else Color(0x14E2E8F0)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = dateHeader,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "(${transactions.size})",
                    fontSize = 10.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "Net: ${if (netDay >= 0) "+ " else "- "}Rs. ${IndianNumberFormatter.formatIndianDouble(kotlin.math.abs(netDay), 2)}",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                color = if (netDay >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
            )
        }
    }
}

@Composable
private fun LedgerTransactionCard(
    tx: LedgerTransactionEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCopy: () -> Unit,
    onTapDetail: () -> Unit,
    primaryColor: Color,
    isDark: Boolean
) {
    val isIncome = tx.type == "CASH_IN" || tx.type == "OPENING_BALANCE"
    val accentColor = if (tx.type == "CASH_IN") Color(0xFF10B981)
    else if (tx.type == "CASH_OUT") Color(0xFFEF4444)
    else Color(0xFF3B82F6)

    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.US) }
    val timeStr = timeFormat.format(Date(tx.dateMillis))

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, if (isDark) Color(0x22FFFFFF) else Color(0x18000000), RoundedCornerShape(12.dp))
            .clickable { onTapDetail() }
            .testTag("ledger_card_${tx.id}"),
        shape = RoundedCornerShape(12.dp),
        color = if (isDark) Color(0xFF141E33) else Color(0xFFFFFFFF),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
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
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (tx.type == "CASH_IN") Icons.Default.ArrowDownward
                            else if (tx.type == "CASH_OUT") Icons.Default.ArrowUpward
                            else Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = tx.category,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (tx.partyName.isNotBlank()) {
                            Text(
                                text = tx.partyName,
                                fontSize = 11.sp,
                                color = primaryColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Amount
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${if (isIncome) "+" else "-"} Rs. ${IndianNumberFormatter.formatIndianDouble(tx.amount, 2)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DigitalMonospaceFont,
                        color = accentColor,
                        maxLines = 1
                    )
                    Text(
                        text = timeStr,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (tx.remarks.isNotBlank() || tx.referenceNo.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (tx.remarks.isNotBlank()) {
                        Text(
                            text = tx.remarks,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (tx.referenceNo.isNotBlank()) {
                        Text(
                            text = "Ref: ${tx.referenceNo}",
                            fontSize = 10.sp,
                            fontFamily = DigitalMonospaceFont,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(thickness = 0.5.dp, color = if (isDark) Color(0x18FFFFFF) else Color(0x12000000))
            Spacer(modifier = Modifier.height(6.dp))

            // Bottom row: Payment Mode Pill + Quick Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (isDark) Color(0x18FFFFFF) else Color(0x0C000000)
                ) {
                    Text(
                        text = tx.paymentMode,
                        fontSize = 10.sp,
                        fontFamily = DigitalMonospaceFont,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onCopy, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = primaryColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// 11. TRANSACTION DETAIL DIALOG
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
    val dateFormat = remember { SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.US) }
    val isIncome = tx.type == "CASH_IN" || tx.type == "OPENING_BALANCE"
    val accentColor = if (tx.type == "CASH_IN") Color(0xFF10B981)
    else if (tx.type == "CASH_OUT") Color(0xFFEF4444)
    else Color(0xFF3B82F6)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Receipt,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Voucher Details", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${if (isIncome) "+ " else "- "}Rs. ${IndianNumberFormatter.formatIndianDouble(tx.amount, 2)}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = DigitalMonospaceFont,
                    color = accentColor
                )

                HorizontalDivider()

                DetailItem(label = "Type", value = tx.type)
                DetailItem(label = "Category", value = tx.category)
                DetailItem(label = "Payment Mode", value = tx.paymentMode)
                if (tx.partyName.isNotBlank()) DetailItem(label = "Party / Contact", value = tx.partyName)
                if (tx.referenceNo.isNotBlank()) DetailItem(label = "Reference #", value = tx.referenceNo)
                if (tx.remarks.isNotBlank()) DetailItem(label = "Remarks", value = tx.remarks)
                DetailItem(label = "Date & Time", value = dateFormat.format(Date(tx.dateMillis)))
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun DetailItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}

// =========================================================================
// HELPER: GROUP TRANSACTIONS BY DATE
// =========================================================================

private fun groupTransactionsByDate(list: List<LedgerTransactionEntity>): Map<String, List<LedgerTransactionEntity>> {
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
    val todayStr = dateFormat.format(Date())
    val yesterdayStr = dateFormat.format(Date(System.currentTimeMillis() - 86400000L))

    return list.groupBy { tx ->
        val formatted = dateFormat.format(Date(tx.dateMillis))
        when (formatted) {
            todayStr -> "Today, $formatted"
            yesterdayStr -> "Yesterday, $formatted"
            else -> formatted
        }
    }
}

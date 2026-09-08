package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardHide
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.SyncAlt
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
import com.example.ui.components.LiquidGlassSearchField
import com.example.ui.components.LiquidGlassTextField
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.LedgerTransactionEntity
import com.example.domain.ledger.LedgerCategoryIcons
import com.example.domain.ledger.LedgerPaymentUtils
import com.example.domain.ledger.LedgerSummary
import com.example.domain.ledger.LedgerType
import com.example.domain.ledger.LedgerUnifiedDataManager
import com.example.domain.util.IndianNumberFormatter
import com.example.domain.util.PkrNumberFormatter
import com.example.ui.LedgerUiState
import com.example.ui.LedgerViewModel
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import com.example.ui.components.BlinkingCursorPointer
import com.example.ui.components.InputFieldCopyPasteMenu
import com.example.ui.components.SimpleCalculatorDialog
import com.example.ui.components.SmartStationKeypad
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

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val primaryColor = MaterialTheme.colorScheme.primary

    var transactionToDelete by remember { mutableStateOf<LedgerTransactionEntity?>(null) }
    var showQuickCalculatorDialog by remember { mutableStateOf(false) }

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

            // 2. Metric Stat Cards (Total Cash In, Total Cash Out, Net Balance)
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

            // 3. Fast Action Buttons: + Cash In, - Cash Out, ⇆ Cash Transfer
            LedgerFastActionBar(
                onAddCashIn = { viewModel.openAddTransactionSheet(LedgerType.CASH_IN) },
                onAddCashOut = { viewModel.openAddTransactionSheet(LedgerType.CASH_OUT, "BUSINESS") },
                onAddTransfer = { viewModel.openAddTransactionSheet(LedgerType.CASH_TRANSFER) }
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
                containerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                LedgerAddEditSheetContent(
                    uiState = uiState,
                    onTypeChange = { viewModel.setFormType(it) },
                    onExpenseScopeChange = { viewModel.setFormExpenseScope(it) },
                    onAmountChange = { viewModel.setFormAmountText(it) },
                    onCategoryChange = { viewModel.setFormCategory(it) },
                    onPaymentModeChange = { viewModel.setFormPaymentMode(it) },
                    onTransferToModeChange = { viewModel.setFormTransferToMode(it) },
                    onSwapTransferModes = { viewModel.swapTransferModes() },
                    onPartyNameChange = { viewModel.setFormPartyName(it) },
                    onRemarksChange = { viewModel.setFormRemarks(it) },
                    onRefNoChange = { viewModel.setFormReferenceNo(it) },
                    onDateChange = { viewModel.setFormDateMillis(it) },
                    onOpenCalculator = { showQuickCalculatorDialog = true },
                    onTokenAppend = { viewModel.appendTokenToAmount(it) },
                    onBackspaceAmount = { viewModel.backspaceAmount() },
                    onClearAmount = { viewModel.clearAmount() },
                    onSave = { viewModel.saveFormTransaction() },
                    onCancel = { viewModel.closeAddSheet() },
                    primaryColor = primaryColor,
                    isDark = isDark
                )
            }
        }

        // ==========================================
        // QUICK CALCULATOR DIALOG
        // ==========================================
        if (showQuickCalculatorDialog) {
            SimpleCalculatorDialog(
                onDismiss = { showQuickCalculatorDialog = false },
                onResultSelected = { result ->
                    viewModel.setFormAmountText(result)
                    showQuickCalculatorDialog = false
                }
            )
        }

        // ==========================================
        // DEDICATED HISTORY PANEL SHEET (TOPBAR LAUNCHER & PILL DRILLDOWN)
        // ==========================================
        if (uiState.isHistoryPanelOpen) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.setHistoryPanelOpen(false) },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
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
                statement = uiState.statementMap[tx.id],
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
            val isTransfer = tx.type == "TRANSFER" || tx.type == "CASH_TRANSFER"
            val titleText = if (isTransfer) "Delete Transfer Record?" else "Delete Transaction Record?"
            val detailText = if (isTransfer) {
                "Are you sure you want to delete this transfer of ${PkrNumberFormatter.formatPkrAmount(tx.amount)} (${tx.paymentMode} ➔ ${tx.transferToMode})?"
            } else {
                "Are you sure you want to delete this ${tx.type.replace('_', ' ')} entry of ${PkrNumberFormatter.formatPkrAmount(tx.amount)} (${tx.category})?"
            }

            AlertDialog(
                onDismissRequest = { transactionToDelete = null },
                title = { Text(titleText) },
                text = { Text(detailText) },
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
    val cardColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface
    val accentColor = if (isPositive) Color(0xFF10B981) else Color(0xFFEF4444)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = if (isDark) 0.35f else 0.18f)),
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "${summary.transactionCount} Entries",
                        fontSize = 10.sp,
                        fontFamily = DigitalMonospaceFont,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
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
                color = if (isPositive) Color(0xFF10B981) else Color(0xFFEF4444)
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(thickness = 0.8.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = if (isDark) 0.25f else 0.15f))
            Spacer(modifier = Modifier.height(10.dp))

            // Payment Mode Breakdown Pills: Cash, Bank, and Others
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
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
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
                    fontSize = 10.sp,
                    fontFamily = DigitalMonospaceFont,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = PkrNumberFormatter.formatPkrDouble(amount, decimals = 0),
                fontSize = 11.5.sp,
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
        color = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
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
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = DigitalMonospaceFont,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// =========================================================================
// 3. FAST ACTION BUTTONS (+ CASH IN, - CASH OUT, ⇆ CASH TRANSFER)
// =========================================================================

@Composable
private fun LedgerFastActionBar(
    onAddCashIn: () -> Unit,
    onAddCashOut: () -> Unit,
    onAddTransfer: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Cash In Button
        Button(
            onClick = onAddCashIn,
            modifier = Modifier
                .weight(1f)
                .height(46.dp)
                .testTag("action_add_cash_in"),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF10B981),
                contentColor = Color.White
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "+ In",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        // Cash Out Button
        Button(
            onClick = onAddCashOut,
            modifier = Modifier
                .weight(1f)
                .height(46.dp)
                .testTag("action_add_cash_out"),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFEF4444),
                contentColor = Color.White
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "- Out",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        // Cash Transfer Button
        Button(
            onClick = onAddTransfer,
            modifier = Modifier
                .weight(1f)
                .height(46.dp)
                .testTag("action_add_transfer"),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF3B82F6),
                contentColor = Color.White
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(17.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Transfer",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

// =========================================================================
// 4. PAYMENT MODE BREAKDOWN CARD
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
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, if (isDark) Color(0x22FFFFFF) else Color(0x18000000), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "PAYMENT CHANNELS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        fontFamily = DigitalMonospaceFont,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "Tap to filter",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(modes.entries.toList()) { entry ->
                    val isPositive = entry.value >= 0
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onModeClick(entry.key) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isDark) Color(0x18FFFFFF) else Color(0x0A000000),
                        border = BorderStroke(1.dp, if (isDark) Color(0x22FFFFFF) else Color(0x18000000))
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                            Text(
                                text = entry.key,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${if (isPositive) "+" else "-"}${PkrNumberFormatter.formatPkrDouble(kotlin.math.abs(entry.value), decimals = 0)}",
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
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, if (isDark) Color(0x22FFFFFF) else Color(0x18000000), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Category,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "CATEGORY ACCOUNTS SUMMARY",
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
                            text = PkrNumberFormatter.formatPkrAmount(amount),
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
// (Supports Cash In, Cash Out with Scope Toggle, and Mode-to-Mode Cash Transfer)
// =========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LedgerAddEditSheetContent(
    uiState: LedgerUiState,
    onTypeChange: (LedgerType) -> Unit,
    onExpenseScopeChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onPaymentModeChange: (String) -> Unit,
    onTransferToModeChange: (String) -> Unit,
    onSwapTransferModes: () -> Unit,
    onPartyNameChange: (String) -> Unit,
    onRemarksChange: (String) -> Unit,
    onRefNoChange: (String) -> Unit,
    onDateChange: (Long) -> Unit,
    onOpenCalculator: () -> Unit,
    onTokenAppend: (String) -> Unit,
    onBackspaceAmount: () -> Unit,
    onClearAmount: () -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    primaryColor: Color,
    isDark: Boolean
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var isKeypadOpen by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }
    val isEditing = uiState.editingTransaction != null

    var isCategoryFocused by remember { mutableStateOf(false) }
    var isFromModeFocused by remember { mutableStateOf(false) }
    var isToModeFocused by remember { mutableStateOf(false) }

    val isCashIn = uiState.formType == LedgerType.CASH_IN
    val isCashOut = uiState.formType == LedgerType.CASH_OUT
    val isTransfer = uiState.formType == LedgerType.CASH_TRANSFER

    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val isImeOpen = WindowInsets.ime.getBottom(density) > 0
    var showAmountContextMenu by remember { mutableStateOf(false) }

    // Dismiss keypad automatically whenever user scrolls the form or opens system keyboard for text
    LaunchedEffect(scrollState.isScrollInProgress, isImeOpen) {
        if (scrollState.isScrollInProgress || isImeOpen) {
            isKeypadOpen = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ledger_add_edit_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .padding(bottom = if (isKeypadOpen) 290.dp else 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isEditing) "Edit Record" else when {
                    isTransfer -> "New Cash Transfer"
                    isCashOut -> "New Cash Out (Expense)"
                    else -> "New Cash In (Income)"
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                color = MaterialTheme.colorScheme.onSurface
            )

            IconButton(onClick = onCancel, modifier = Modifier.size(32.dp)) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
            }
        }

        // 1. Transaction Type Toggle: 3 Tabs (Cash In, Cash Out, Transfer)
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
                // Cash In Tab
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = if (isCashIn) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "+ In",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCashIn) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Cash Out Tab
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = if (isCashOut) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "- Out",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCashOut) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Transfer Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isTransfer) Color(0xFF3B82F6) else Color.Transparent)
                        .clickable { onTypeChange(LedgerType.CASH_TRANSFER) }
                        .testTag("form_type_transfer"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = if (isTransfer) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Transfer",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isTransfer) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // =========================================================================
        // EXPENSE SCOPE SELECTOR (BUSINESS VS HOME) - FOR CASH OUT
        // =========================================================================
        if (isCashOut) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "EXPENSE CLASSIFICATION *",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DigitalMonospaceFont,
                        color = Color(0xFFEF4444)
                    )
                    Text(
                        text = "Syncs with Expenses Manager",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0x33EF4444), RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else Color(0xFFFEF2F2)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val isBizScope = uiState.formExpenseScope == "BUSINESS"
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isBizScope) Color(0xFFEF4444) else Color.Transparent)
                                .clickable { onExpenseScopeChange("BUSINESS") }
                                .testTag("expense_scope_biz_toggle"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Business,
                                    contentDescription = null,
                                    tint = if (isBizScope) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Business Expense",
                                    fontSize = 12.sp,
                                    fontWeight = if (isBizScope) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isBizScope) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        val isHomeScope = uiState.formExpenseScope == "HOME"
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isHomeScope) Color(0xFFF97316) else Color.Transparent)
                                .clickable { onExpenseScopeChange("HOME") }
                                .testTag("expense_scope_home_toggle"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = null,
                                    tint = if (isHomeScope) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Home / Personal",
                                    fontSize = 12.sp,
                                    fontWeight = if (isHomeScope) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isHomeScope) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // CASH TRANSFER CHANNEL SELECTORS (FROM ACCOUNT ➔ TO ACCOUNT)
        // =========================================================================
        if (isTransfer) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, Color(0x443B82F6), RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                color = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else Color(0xFFEFF6FF)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SyncAlt,
                                contentDescription = null,
                                tint = Color(0xFF3B82F6),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "TRANSFER BETWEEN CHANNELS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = DigitalMonospaceFont,
                                color = Color(0xFF3B82F6)
                            )
                        }

                        IconButton(
                            onClick = onSwapTransferModes,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Swap Accounts",
                                tint = Color(0xFF3B82F6),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // FROM CHANNEL
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "FROM CHANNEL (SOURCE) *",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = DigitalMonospaceFont,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LiquidGlassTextField(
                            value = uiState.formPaymentMode,
                            onValueChange = onPaymentModeChange,
                            placeholder = { Text("e.g. Cash, Bank Transfer...", fontSize = 12.5.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (LedgerPaymentUtils.isBankMode(uiState.formPaymentMode)) Icons.Default.AccountBalance else Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            singleLine = true,
                            accentColor = Color(0xFF3B82F6),
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged {
                                    isFromModeFocused = it.isFocused
                                    if (it.isFocused) isKeypadOpen = false
                                }
                                .testTag("transfer_from_mode_input")
                        )

                        // Mode Chips for From Channel (Shows suggestions when typing)
                        val typedFrom = uiState.formPaymentMode.trim()
                        val isCustomFrom = typedFrom.isNotEmpty() && uiState.availablePaymentModes.none { it.equals(typedFrom, ignoreCase = true) }
                        val matchingFromModes = remember(uiState.formPaymentMode, uiState.availablePaymentModes) {
                            if (typedFrom.isEmpty()) emptyList() else uiState.availablePaymentModes.filter { it.contains(typedFrom, ignoreCase = true) && !it.equals(typedFrom, ignoreCase = true) }
                        }

                        AnimatedVisibility(
                            visible = typedFrom.isNotEmpty() && (isCustomFrom || matchingFromModes.isNotEmpty()),
                            enter = fadeIn() + slideInVertically(),
                            exit = fadeOut() + slideOutVertically()
                        ) {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (isCustomFrom) {
                                    item {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFF3B82F6).copy(alpha = 0.2f),
                                            border = BorderStroke(1.dp, Color(0xFF3B82F6)),
                                            modifier = Modifier.clickable { onPaymentModeChange(typedFrom) }
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(text = "Use '$typedFrom'", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3B82F6))
                                            }
                                        }
                                    }
                                }
                                items(matchingFromModes) { mode ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF3B82F6).copy(alpha = 0.15f),
                                        border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.35f)),
                                        modifier = Modifier.clickable { onPaymentModeChange(mode) }
                                    ) {
                                        Text(
                                            text = mode,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF3B82F6),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // TO CHANNEL
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "TO CHANNEL (DESTINATION) *",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = DigitalMonospaceFont,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LiquidGlassTextField(
                            value = uiState.formTransferToMode,
                            onValueChange = onTransferToModeChange,
                            placeholder = { Text("e.g. Bank Transfer, Easypaisa...", fontSize = 12.5.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (LedgerPaymentUtils.isBankMode(uiState.formTransferToMode)) Icons.Default.AccountBalance else Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            singleLine = true,
                            accentColor = Color(0xFF3B82F6),
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged {
                                    isToModeFocused = it.isFocused
                                    if (it.isFocused) isKeypadOpen = false
                                }
                                .testTag("transfer_to_mode_input")
                        )

                        // Mode Chips for To Channel (Shows suggestions when typing)
                        val typedTo = uiState.formTransferToMode.trim()
                        val isCustomTo = typedTo.isNotEmpty() && uiState.availablePaymentModes.none { it.equals(typedTo, ignoreCase = true) }
                        val matchingToModes = remember(uiState.formTransferToMode, uiState.availablePaymentModes) {
                            if (typedTo.isEmpty()) emptyList() else uiState.availablePaymentModes.filter { it.contains(typedTo, ignoreCase = true) && !it.equals(typedTo, ignoreCase = true) }
                        }

                        AnimatedVisibility(
                            visible = typedTo.isNotEmpty() && (isCustomTo || matchingToModes.isNotEmpty()),
                            enter = fadeIn() + slideInVertically(),
                            exit = fadeOut() + slideOutVertically()
                        ) {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (isCustomTo) {
                                    item {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFF10B981).copy(alpha = 0.2f),
                                            border = BorderStroke(1.dp, Color(0xFF10B981)),
                                            modifier = Modifier.clickable { onTransferToModeChange(typedTo) }
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(text = "Use '$typedTo'", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                                            }
                                        }
                                    }
                                }
                                items(matchingToModes) { mode ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF10B981).copy(alpha = 0.15f),
                                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.35f)),
                                        modifier = Modifier.clickable { onTransferToModeChange(mode) }
                                    ) {
                                        Text(
                                            text = mode,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF10B981),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Transaction Amount
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "TRANSACTION AMOUNT (RS.) *",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                LiquidGlassTextField(
                    value = uiState.formAmountText,
                    onValueChange = onAmountChange,
                    readOnly = true,
                    placeholder = { Text("0.00", fontFamily = DigitalMonospaceFont) },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (uiState.formAmountText.isNotEmpty()) {
                                IconButton(
                                    onClick = onClearAmount,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                }
                            }
                            IconButton(
                                onClick = onOpenCalculator,
                                modifier = Modifier.size(28.dp).testTag("amount_calc_icon_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Calculate,
                                    contentDescription = "Quick Calculator",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    },
                    suffix = {
                        if (isKeypadOpen) {
                            BlinkingCursorPointer(color = uiState.formType.color)
                        }
                    },
                    singleLine = true,
                    forceFocused = isKeypadOpen,
                    accentColor = uiState.formType.color,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("form_amount_input")
                )

                // Click and Long-Press overlay for input area (tap opens keypad, long-press opens Copy/Paste)
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(end = 64.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = {
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                    isKeypadOpen = true
                                },
                                onLongPress = { showAmountContextMenu = true }
                            )
                        }
                )

                InputFieldCopyPasteMenu(
                    expanded = showAmountContextMenu,
                    onDismissRequest = { showAmountContextMenu = false },
                    value = uiState.formAmountText,
                    onPaste = { onAmountChange(it) }
                )
            }
        }

        // 3. Category Field (Only for Cash In and Cash Out)
        if (!isTransfer) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                val categoryContextLabel = if (isCashOut) {
                    if (uiState.formExpenseScope == "HOME") "HOME EXPENSE CATEGORY *" else "BUSINESS EXPENSE CATEGORY *"
                } else {
                    "INCOME / SALES CATEGORY *"
                }

                Text(
                    text = categoryContextLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val placeholderText = if (isCashOut) {
                    if (uiState.formExpenseScope == "HOME") "e.g. Groceries & Food, Home Rent, Electricity..." else "e.g. Raw Materials, Salaries, Rent, Fuel..."
                } else {
                    "e.g. Sales, Customer Payment, Recovery, Noran Shah..."
                }

                LiquidGlassTextField(
                    value = uiState.formCategory,
                    onValueChange = onCategoryChange,
                    placeholder = { Text(placeholderText, fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = if (isCashOut && uiState.formExpenseScope == "HOME") Icons.Default.Home else LedgerCategoryIcons.getCategoryIcon(uiState.formCategory),
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged {
                            isCategoryFocused = it.isFocused
                            if (it.isFocused) isKeypadOpen = false
                        }
                        .testTag("form_category_input")
                )

                // Dynamic Auto-suggestions & Automated Creation (Only shows when user types letters)
                val activeCategoryPool = if (isCashOut) {
                    if (uiState.formExpenseScope == "HOME") uiState.homeCategories else uiState.businessCategories
                } else {
                    uiState.incomeCategories
                }

                val trimmedInputCategory = uiState.formCategory.trim()
                val isCustomNewCategory = trimmedInputCategory.isNotEmpty() &&
                    activeCategoryPool.none { it.equals(trimmedInputCategory, ignoreCase = true) }

                val matchingCategories = remember(uiState.formCategory, activeCategoryPool, isCategoryFocused) {
                    if (trimmedInputCategory.isEmpty()) {
                        emptyList()
                    } else {
                        activeCategoryPool.filter {
                            it.contains(trimmedInputCategory, ignoreCase = true) && !it.equals(trimmedInputCategory, ignoreCase = true)
                        }
                    }
                }

                // Show suggestion row or create new category chip when user types
                AnimatedVisibility(
                    visible = trimmedInputCategory.isNotEmpty() && (isCustomNewCategory || matchingCategories.isNotEmpty()),
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = if (isCustomNewCategory) "Automated Category Creation & Suggestions:" else "Matching suggestions (tap to pick):",
                            fontSize = 10.5.sp,
                            color = primaryColor,
                            fontWeight = FontWeight.SemiBold
                        )
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (isCustomNewCategory) {
                                item {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = primaryColor.copy(alpha = 0.2f),
                                        border = BorderStroke(1.dp, primaryColor),
                                        modifier = Modifier.clickable { onCategoryChange(trimmedInputCategory) }
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = primaryColor, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Create '$trimmedInputCategory'",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = primaryColor
                                            )
                                        }
                                    }
                                }
                            }

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
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = cat,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Payment Mode Field (For Cash In and Cash Out)
        if (!isTransfer) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "PAYMENT CHANNEL / MODE *",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LiquidGlassTextField(
                    value = uiState.formPaymentMode,
                    onValueChange = onPaymentModeChange,
                    placeholder = { Text("e.g. Cash, Bank Transfer, Easypaisa...") },
                    leadingIcon = {
                        Icon(
                            imageVector = if (LedgerPaymentUtils.isBankMode(uiState.formPaymentMode)) Icons.Default.AccountBalance else Icons.Default.Payments,
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { if (it.isFocused) isKeypadOpen = false }
                        .testTag("form_payment_mode_input")
                )

                // Dynamic Mode Suggestions & Automated Creation (Only shows when user types letters)
                val typedPaymentMode = uiState.formPaymentMode.trim()
                val isCustomNewMode = typedPaymentMode.isNotEmpty() &&
                    uiState.availablePaymentModes.none { it.equals(typedPaymentMode, ignoreCase = true) }

                val matchingModes = remember(uiState.formPaymentMode, uiState.availablePaymentModes) {
                    if (typedPaymentMode.isEmpty()) {
                        emptyList()
                    } else {
                        uiState.availablePaymentModes.filter {
                            it.contains(typedPaymentMode, ignoreCase = true) && !it.equals(typedPaymentMode, ignoreCase = true)
                        }
                    }
                }

                AnimatedVisibility(
                    visible = typedPaymentMode.isNotEmpty() && (isCustomNewMode || matchingModes.isNotEmpty()),
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = if (isCustomNewMode) "Automated Payment Mode Creation:" else "Matching channels:",
                            fontSize = 10.5.sp,
                            color = primaryColor,
                            fontWeight = FontWeight.SemiBold
                        )
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (isCustomNewMode) {
                                item {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = primaryColor.copy(alpha = 0.2f),
                                        border = BorderStroke(1.dp, primaryColor),
                                        modifier = Modifier.clickable { onPaymentModeChange(typedPaymentMode) }
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = primaryColor, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Create Mode '$typedPaymentMode'",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = primaryColor
                                            )
                                        }
                                    }
                                }
                            }

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
                                            imageVector = if (LedgerPaymentUtils.isBankMode(mode)) Icons.Default.AccountBalance else Icons.Default.Payments,
                                            contentDescription = null,
                                            tint = primaryColor,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = mode,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Party / Customer / Vendor Name (Optional)
        if (!isTransfer) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "PARTY / CUSTOMER / VENDOR NAME (OPTIONAL)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LiquidGlassTextField(
                    value = uiState.formPartyName,
                    onValueChange = onPartyNameChange,
                    placeholder = { Text("e.g. Noran Shah, ABC Traders, Ali Store...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = primaryColor, modifier = Modifier.size(20.dp))
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { if (it.isFocused) isKeypadOpen = false }
                        .testTag("form_party_name_input")
                )

                if (uiState.distinctParties.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(uiState.distinctParties) { party ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isDark) Color(0x18FFFFFF) else Color(0x0A000000),
                                border = BorderStroke(1.dp, if (isDark) Color(0x22FFFFFF) else Color(0x18000000)),
                                modifier = Modifier.clickable { onPartyNameChange(party) }
                            ) {
                                Text(
                                    text = party,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 6. Transaction Date Picker
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "TRANSACTION DATE *",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, if (isDark) Color(0x33FFFFFF) else Color(0x28000000), RoundedCornerShape(12.dp))
                    .clickable { showDatePicker = true }
                    .testTag("form_date_picker_button"),
                shape = RoundedCornerShape(12.dp),
                color = if (isDark) Color(0x12FFFFFF) else Color(0x06000000)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, tint = primaryColor, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = dateFormat.format(Date(uiState.formDateMillis)),
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = DigitalMonospaceFont,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "Change Date",
                        fontSize = 11.sp,
                        color = primaryColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 7. Reference Number & Remarks
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LiquidGlassTextField(
                value = uiState.formReferenceNo,
                onValueChange = onRefNoChange,
                label = { Text("Bill / Ref #", fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { if (it.isFocused) isKeypadOpen = false }
                    .testTag("form_ref_no_input")
            )

            LiquidGlassTextField(
                value = uiState.formRemarks,
                onValueChange = onRemarksChange,
                label = { Text("Remarks / Note", fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { if (it.isFocused) isKeypadOpen = false }
                    .testTag("form_remarks_input")
            )
        }

        // Action Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDark) Color(0x22FFFFFF) else Color(0x15000000),
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text("Cancel")
            }

            Button(
                onClick = onSave,
                enabled = !uiState.isFormSubmitting,
                modifier = Modifier
                    .weight(1.5f)
                    .height(48.dp)
                    .testTag("form_save_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = uiState.formType.color,
                    contentColor = Color.White
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isEditing) "Update Entry" else when {
                            isTransfer -> "Save Transfer"
                            isCashOut -> "Save Cash Out"
                            else -> "Save Cash In"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }

    // Bottom-docked Smart Dialpad (slides up from the bottom of the screen, auto-dismisses on scroll)
    AnimatedVisibility(
        visible = isKeypadOpen,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = Modifier.align(Alignment.BottomCenter)
    ) {
        Surface(
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            color = if (isDark) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = if (isDark) 0.35f else 0.18f)),
            shadowElevation = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            SmartStationKeypad(
                activeFieldLabel = "Amount",
                onDigit = onTokenAppend,
                onDoubleZero = { onTokenAppend("00") },
                onDecimal = { onTokenAppend(".") },
                onBackspace = onBackspaceAmount,
                onClear = onClearAmount,
                onDone = { isKeypadOpen = false },
                showDoubleZeroKey = true,
                showDecimalKey = true,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = uiState.formDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { onDateChange(it) }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
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
// 7. DEDICATED HISTORY PANEL SHEET (With SVG Filter Chips)
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
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("ledger_history_panel_sheet")
    ) {
        // Header
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
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Cashbook Log & History",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Copy Full Audit Log
                IconButton(
                    onClick = {
                        val audit = viewModel.buildPeriodAuditSummary()
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Cashbook Audit", audit))
                        Toast.makeText(context, "Full audit log copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy Audit", tint = primaryColor)
                }

                IconButton(onClick = onClose, modifier = Modifier.size(34.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar
        LiquidGlassSearchField(
            query = uiState.searchQuery,
            onQueryChange = { viewModel.setSearchQuery(it) },
            placeholderText = "Search by party, category, mode, amount...",
            modifier = Modifier
                .fillMaxWidth()
                .testTag("history_search_input")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Filter Chips with Clean SVG Icons (NO EMOJIS)
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                HistoryFilterSvgChip(
                    label = "All Entries",
                    icon = null,
                    isSelected = uiState.selectedTypeFilter == "ALL" && uiState.selectedModeFilter == "ALL",
                    onClick = {
                        viewModel.setTypeFilter("ALL")
                        viewModel.setModeFilter("ALL")
                    },
                    primaryColor = primaryColor,
                    isDark = isDark
                )
            }

            item {
                HistoryFilterSvgChip(
                    label = "Cash In (+)",
                    icon = Icons.Default.ArrowDownward,
                    isSelected = uiState.selectedTypeFilter == "CASH_IN",
                    onClick = { viewModel.setTypeFilter("CASH_IN") },
                    primaryColor = Color(0xFF10B981),
                    isDark = isDark
                )
            }

            item {
                HistoryFilterSvgChip(
                    label = "Cash Out (-)",
                    icon = Icons.Default.ArrowUpward,
                    isSelected = uiState.selectedTypeFilter == "CASH_OUT",
                    onClick = { viewModel.setTypeFilter("CASH_OUT") },
                    primaryColor = Color(0xFFEF4444),
                    isDark = isDark
                )
            }

            item {
                HistoryFilterSvgChip(
                    label = "Transfers",
                    icon = Icons.Default.SwapHoriz,
                    isSelected = uiState.selectedTypeFilter == "TRANSFER",
                    onClick = { viewModel.setTypeFilter("TRANSFER") },
                    primaryColor = Color(0xFF3B82F6),
                    isDark = isDark
                )
            }

            item {
                HistoryFilterSvgChip(
                    label = "Business Exp",
                    icon = Icons.Default.Business,
                    isSelected = uiState.selectedTypeFilter == "BUSINESS_EXPENSE",
                    onClick = { viewModel.setTypeFilter("BUSINESS_EXPENSE") },
                    primaryColor = Color(0xFFEF4444),
                    isDark = isDark
                )
            }

            item {
                HistoryFilterSvgChip(
                    label = "Home Exp",
                    icon = Icons.Default.Home,
                    isSelected = uiState.selectedTypeFilter == "HOME_EXPENSE",
                    onClick = { viewModel.setTypeFilter("HOME_EXPENSE") },
                    primaryColor = Color(0xFFF97316),
                    isDark = isDark
                )
            }

            // Mode Filters
            item {
                HistoryFilterSvgChip(
                    label = "Cash Mode",
                    icon = Icons.Default.Payments,
                    isSelected = uiState.selectedModeFilter == "CASH",
                    onClick = { viewModel.setModeFilter(if (uiState.selectedModeFilter == "CASH") "ALL" else "CASH") },
                    primaryColor = primaryColor,
                    isDark = isDark
                )
            }

            item {
                HistoryFilterSvgChip(
                    label = "Bank Mode",
                    icon = Icons.Default.AccountBalance,
                    isSelected = uiState.selectedModeFilter == "BANK",
                    onClick = { viewModel.setModeFilter(if (uiState.selectedModeFilter == "BANK") "ALL" else "BANK") },
                    primaryColor = primaryColor,
                    isDark = isDark
                )
            }

            item {
                HistoryFilterSvgChip(
                    label = "Others",
                    icon = Icons.Default.MoreHoriz,
                    isSelected = uiState.selectedModeFilter == "OTHERS",
                    onClick = { viewModel.setModeFilter(if (uiState.selectedModeFilter == "OTHERS") "ALL" else "OTHERS") },
                    primaryColor = primaryColor,
                    isDark = isDark
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
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No matching transactions found",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("history_transaction_list"),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(uiState.filteredTransactions, key = { it.id }) { tx ->
                    LedgerTransactionRow(
                        tx = tx,
                        statement = uiState.statementMap[tx.id],
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
private fun HistoryFilterSvgChip(
    label: String,
    icon: ImageVector?,
    isSelected: Boolean,
    onClick: () -> Unit,
    primaryColor: Color,
    isDark: Boolean
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) primaryColor else if (isDark) Color(0x18FFFFFF) else Color(0x0A000000),
        border = BorderStroke(1.dp, if (isSelected) primaryColor else if (isDark) Color(0x22FFFFFF) else Color(0x18000000))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = label,
                fontSize = 11.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// =========================================================================
// 8. TRANSACTION ROW COMPONENT (WITH BANK STATEMENT RUNNING BALANCE)
// =========================================================================

@Composable
private fun LedgerTransactionRow(
    tx: LedgerTransactionEntity,
    statement: com.example.domain.ledger.LedgerStatementItem? = null,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    isDark: Boolean
) {
    val isTransfer = tx.type == "TRANSFER" || tx.type == "CASH_TRANSFER"
    val isCashOut = tx.type == "CASH_OUT"

    val accentColor = when {
        isTransfer -> Color(0xFF3B82F6)
        isCashOut -> Color(0xFFEF4444)
        else -> Color(0xFF10B981)
    }

    val icon = when {
        isTransfer -> Icons.Default.SwapHoriz
        isCashOut -> Icons.Default.ArrowUpward
        else -> Icons.Default.ArrowDownward
    }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, if (isDark) Color(0x1EFFFFFF) else Color(0x12000000), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("transaction_row_${tx.id}"),
        shape = RoundedCornerShape(12.dp),
        color = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Icon + Info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isTransfer) "Account Transfer" else tx.category,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (isCashOut) {
                                val isHome = tx.expenseScope == "HOME" || LedgerUnifiedDataManager.isHomeExpense(tx.category)
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (isHome) Color(0x25F97316) else Color(0x25EF4444)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (isHome) Icons.Default.Home else Icons.Default.Business,
                                            contentDescription = null,
                                            tint = if (isHome) Color(0xFFF97316) else Color(0xFFEF4444),
                                            modifier = Modifier.size(9.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = if (isHome) "Home" else "Biz",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isHome) Color(0xFFF97316) else Color(0xFFEF4444)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        val subtitle = if (isTransfer) {
                            "${tx.paymentMode} ➔ ${tx.transferToMode}"
                        } else {
                            val party = if (tx.partyName.isNotBlank()) "${tx.partyName} • " else ""
                            "$party${tx.paymentMode} • ${dateFormat.format(Date(tx.dateMillis))}"
                        }

                        Text(
                            text = subtitle,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Right: Amount + Actions
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        val sign = if (isTransfer) "⇆" else if (isCashOut) "-" else "+"
                        Text(
                            text = "$sign ${PkrNumberFormatter.formatPkrAmount(tx.amount)}",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = DigitalMonospaceFont,
                            color = accentColor
                        )
                        if (isTransfer) {
                            Text(
                                text = dateFormat.format(Date(tx.dateMillis)),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(15.dp))
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(15.dp))
                    }
                }
            }

            // Bank Statement Running Balance Effect Strip
            if (statement != null) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(thickness = 0.7.dp, color = if (isDark) Color(0x18FFFFFF) else Color(0x10000000))
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Balance: ",
                            fontSize = 10.5.sp,
                            fontFamily = DigitalMonospaceFont,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = "${statement.formattedBalanceBefore} ➔",
                            fontSize = 10.5.sp,
                            fontFamily = DigitalMonospaceFont,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(5.dp),
                        color = if (statement.balanceAfter >= 0) Color(0x1810B981) else Color(0x18EF4444)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Bal: ${statement.formattedBalanceAfter}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = DigitalMonospaceFont,
                                color = if (statement.balanceAfter >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                            )
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// 9. TRANSACTION DETAIL DIALOG (WITH BANK STATEMENT PROGRESSION EFFECT)
// =========================================================================

@Composable
private fun LedgerDetailDialog(
    tx: LedgerTransactionEntity,
    statement: com.example.domain.ledger.LedgerStatementItem? = null,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCopy: () -> Unit,
    primaryColor: Color,
    isDark: Boolean
) {
    val isTransfer = tx.type == "TRANSFER" || tx.type == "CASH_TRANSFER"
    val isCashOut = tx.type == "CASH_OUT"
    val accentColor = when {
        isTransfer -> Color(0xFF3B82F6)
        isCashOut -> Color(0xFFEF4444)
        else -> Color(0xFF10B981)
    }
    val dateFormat = remember { SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.US) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isTransfer) "Transfer Statement Voucher" else "Statement Voucher",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont
                )
                IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy Voucher", tint = primaryColor)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Big Amount Display
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = accentColor.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isTransfer) "AMOUNT TRANSFERRED" else if (isCashOut) "TOTAL CASH OUT" else "TOTAL CASH IN",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = DigitalMonospaceFont,
                            color = accentColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = PkrNumberFormatter.formatPkrAmount(tx.amount),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = DigitalMonospaceFont,
                            color = accentColor
                        )
                    }
                }

                // Bank Statement Running Progression Box
                if (statement != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isDark) Color(0x22FFFFFF) else Color(0x0A000000),
                        border = BorderStroke(1.dp, if (isDark) Color(0x33FFFFFF) else Color(0x18000000)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "BANK STATEMENT EFFECT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = DigitalMonospaceFont,
                                    color = primaryColor
                                )
                                Text(
                                    text = statement.formattedImpact,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = DigitalMonospaceFont,
                                    color = accentColor
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            DetailItemRow("Balance Before:", statement.formattedBalanceBefore)
                            DetailItemRow("Balance After:", statement.formattedBalanceAfter)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                if (isTransfer) {
                    DetailItemRow("From Channel:", tx.paymentMode)
                    DetailItemRow("To Channel:", tx.transferToMode)
                } else {
                    DetailItemRow("Category:", tx.category)
                    DetailItemRow("Payment Mode:", tx.paymentMode)
                    if (isCashOut) {
                        val isHome = tx.expenseScope == "HOME" || LedgerUnifiedDataManager.isHomeExpense(tx.category)
                        DetailItemRow("Scope:", if (isHome) "Home / Personal Expense" else "Business Expense")
                    }
                    if (tx.partyName.isNotBlank()) DetailItemRow("Party / Name:", tx.partyName)
                }

                if (tx.referenceNo.isNotBlank()) DetailItemRow("Ref / Bill #:", tx.referenceNo)
                if (tx.remarks.isNotBlank()) DetailItemRow("Remarks:", tx.remarks)
                DetailItemRow("Date:", dateFormat.format(Date(tx.dateMillis)))
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TextButton(onClick = onEdit) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit")
                }
                TextButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete", color = Color(0xFFEF4444))
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
private fun DetailItemRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.4f)
        )
    }
}

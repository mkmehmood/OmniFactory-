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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardHide
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.TrendingDown
import com.example.ui.ExpenseStatementItem
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.LedgerTransactionEntity
import com.example.domain.ledger.LedgerCategoryIcons
import com.example.domain.util.IndianNumberFormatter
import com.example.domain.util.PkrNumberFormatter
import com.example.ui.ExpenseScope
import com.example.ui.ExpenseTimeFilter
import com.example.ui.ExpensesSummary
import com.example.ui.ExpensesUiState
import com.example.ui.ExpensesViewModel
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.ui.focus.onFocusChanged
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
fun ExpensesScreen(
    viewModel: ExpensesViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val primaryColor = MaterialTheme.colorScheme.primary
    val expenseAccent = Color(0xFFEF4444)

    var expenseToDelete by remember { mutableStateOf<LedgerTransactionEntity?>(null) }

    LaunchedEffect(Unit) {
        viewModel.userMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("expenses_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. SCOPE FILTER TAB BAR (ALL / BUSINESS / HOME)
            item {
                ExpenseScopeTabBar(
                    selectedScope = uiState.selectedScope,
                    onSelectScope = { viewModel.setScope(it) },
                    summary = uiState.summary,
                    primaryColor = primaryColor,
                    isDark = isDark
                )
            }

            // 2. HERO SUMMARY CARD WITH METRICS
            item {
                ExpenseHeroSummaryCard(
                    selectedScope = uiState.selectedScope,
                    summary = uiState.summary,
                    selectedTimeFilter = uiState.selectedTimeFilter,
                    onSelectTimeFilter = { viewModel.setTimeFilter(it) },
                    onCopyAuditReport = {
                        val report = viewModel.buildExpenseSummaryReport()
                        clipboardManager.setText(AnnotatedString(report))
                        Toast.makeText(context, "Expense audit report copied!", Toast.LENGTH_SHORT).show()
                    },
                    primaryColor = primaryColor,
                    expenseAccent = expenseAccent,
                    isDark = isDark
                )
            }

            // 3. SEARCH BAR & QUICK ACTION
            item {
                ExpenseSearchBarAndAction(
                    searchQuery = uiState.searchQuery,
                    onQueryChange = { viewModel.setSearchQuery(it) },
                    onOpenAddSheet = { viewModel.openAddExpense(uiState.selectedScope) },
                    onOpenCalc = { viewModel.openCalculator() },
                    primaryColor = primaryColor,
                    isDark = isDark
                )
            }

            // 4. TOP EXPENSE CATEGORIES SUMMARY (IF AVAILABLE)
            if (uiState.summary.topCategories.isNotEmpty()) {
                item {
                    ExpenseCategorySummaryCard(
                        categories = uiState.summary.topCategories,
                        primaryColor = primaryColor,
                        isDark = isDark
                    )
                }
            }

            // 5. EXPENSES LIST HEADER
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "EXPENSES LOG (${uiState.filteredExpenses.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        fontFamily = DigitalMonospaceFont,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "Synchronized with Ledger",
                        fontSize = 10.5.sp,
                        color = primaryColor,
                        fontFamily = DigitalMonospaceFont
                    )
                }
            }

            // 6. EXPENSE LIST ITEMS
            if (uiState.filteredExpenses.isEmpty()) {
                item {
                    ExpenseEmptyStateCard(
                        scope = uiState.selectedScope,
                        onAddExpense = { viewModel.openAddExpense(uiState.selectedScope) },
                        primaryColor = primaryColor,
                        isDark = isDark
                    )
                }
            } else {
                items(uiState.filteredExpenses, key = { it.id }) { expense ->
                    ExpenseRowItem(
                        expense = expense,
                        statement = uiState.statementMap[expense.id],
                        onClick = { viewModel.openDetail(expense) },
                        onEdit = { viewModel.openEditExpense(expense) },
                        onDelete = { expenseToDelete = expense },
                        isDark = isDark
                    )
                }
            }
        }

        // ==========================================
        // FLOATING ACTION BUTTON - LOG EXPENSE
        // ==========================================
        FloatingActionButton(
            onClick = { viewModel.openAddExpense(uiState.selectedScope) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_expense_fab"),
            containerColor = expenseAccent,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Expense")
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Log Expense",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        // ==========================================
        // ADD / EDIT EXPENSE BOTTOM SHEET
        // ==========================================
        if (uiState.isAddSheetOpen) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.closeAddSheet() },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = if (isDark) Color(0xFF101726) else Color(0xFFF8FAFC),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                ExpenseAddEditSheetContent(
                    uiState = uiState,
                    onScopeChange = { viewModel.setFormScope(it) },
                    onAmountChange = { viewModel.setFormAmount(it) },
                    onTokenAppend = { viewModel.appendTokenToAmount(it) },
                    onBackspaceAmount = { viewModel.backspaceAmount() },
                    onClearAmount = { viewModel.clearAmount() },
                    onOpenCalculator = { viewModel.openCalculator() },
                    onCategoryChange = { viewModel.setFormCategory(it) },
                    onPaymentModeChange = { viewModel.setFormPaymentMode(it) },
                    onPaidToChange = { viewModel.setFormPaidTo(it) },
                    onRemarksChange = { viewModel.setFormRemarks(it) },
                    onRefNoChange = { viewModel.setFormReferenceNo(it) },
                    onDateChange = { viewModel.setFormDateMillis(it) },
                    onSave = { viewModel.saveExpense() },
                    onCancel = { viewModel.closeAddSheet() },
                    primaryColor = primaryColor,
                    isDark = isDark
                )
            }
        }

        // ==========================================
        // QUICK CALCULATOR DIALOG
        // ==========================================
        if (uiState.isCalculatorOpen) {
            SimpleCalculatorDialog(
                onDismiss = { viewModel.closeCalculator() },
                onResultSelected = { result ->
                    viewModel.setFormAmount(result)
                }
            )
        }

        // ==========================================
        // EXPENSE DETAIL MODAL (WITH STATEMENT PROGRESSION)
        // ==========================================
        if (uiState.isDetailDialogOpen && uiState.selectedExpenseForDetail != null) {
            val exp = uiState.selectedExpenseForDetail!!
            val statement = uiState.statementMap[exp.id]
            val dateFormat = remember { SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.US) }
            AlertDialog(
                onDismissRequest = { viewModel.closeDetail() },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Expense Statement Voucher",
                            fontWeight = FontWeight.Bold,
                            fontFamily = DigitalMonospaceFont
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = {
                                val voucher = viewModel.buildSingleExpenseReport(exp)
                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Expense Voucher", voucher))
                                Toast.makeText(context, "Voucher copied", Toast.LENGTH_SHORT).show()
                            }, modifier = Modifier.size(28.dp)) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy Voucher", tint = primaryColor, modifier = Modifier.size(16.dp))
                            }
                            IconButton(onClick = { viewModel.closeDetail() }, modifier = Modifier.size(28.dp)) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = expenseAccent.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, expenseAccent.copy(alpha = 0.35f))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "-${PkrNumberFormatter.formatPkrAmount(exp.amount)}",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = DigitalMonospaceFont,
                                    color = expenseAccent
                                )
                                Text(
                                    text = if (exp.expenseScope == "HOME") "HOME / PERSONAL EXPENSE" else "BUSINESS EXPENSE",
                                    fontSize = 11.sp,
                                    fontFamily = DigitalMonospaceFont,
                                    color = expenseAccent
                                )
                            }
                        }

                        // Statement Effect Box
                        if (statement != null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isDark) Color(0x22FFFFFF) else Color(0x0A000000),
                                border = BorderStroke(1.dp, if (isDark) Color(0x33FFFFFF) else Color(0x18000000)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "EXPENSE STATEMENT PROGRESSION",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = DigitalMonospaceFont,
                                        color = primaryColor
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    DetailRow(label = "Cumulative Spent Before:", value = statement.formattedCumulativeBefore)
                                    DetailRow(label = "Expense Impact:", value = statement.formattedAmount)
                                    DetailRow(label = "Cumulative Spent After:", value = statement.formattedCumulativeAfter)
                                }
                            }
                        }

                        DetailRow(label = "Category", value = exp.category)
                        DetailRow(label = "Payment Mode", value = exp.paymentMode)
                        if (exp.partyName.isNotBlank()) DetailRow(label = "Paid To", value = exp.partyName)
                        if (exp.referenceNo.isNotBlank()) DetailRow(label = "Bill / Ref #", value = exp.referenceNo)
                        if (exp.remarks.isNotBlank()) DetailRow(label = "Remarks", value = exp.remarks)
                        DetailRow(label = "Date", value = dateFormat.format(Date(exp.dateMillis)))
                    }
                },
                confirmButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = {
                            viewModel.closeDetail()
                            viewModel.openEditExpense(exp)
                        }) {
                            Text("Edit")
                        }
                        TextButton(onClick = {
                            viewModel.closeDetail()
                            expenseToDelete = exp
                        }) {
                            Text("Delete", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }

        // ==========================================
        // DELETE CONFIRMATION DIALOG
        // ==========================================
        if (expenseToDelete != null) {
            val exp = expenseToDelete!!
            AlertDialog(
                onDismissRequest = { expenseToDelete = null },
                title = { Text("Delete Expense Record?") },
                text = {
                    Text("Are you sure you want to delete this expense of ${PkrNumberFormatter.formatPkrAmount(exp.amount, decimals = 2)} (${exp.category})?")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteExpense(exp)
                            expenseToDelete = null
                        }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { expenseToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

// =========================================================================
// SCOPE TAB BAR
// =========================================================================

@Composable
private fun ExpenseScopeTabBar(
    selectedScope: ExpenseScope,
    onSelectScope: (ExpenseScope) -> Unit,
    summary: ExpensesSummary,
    primaryColor: Color,
    isDark: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, if (isDark) Color(0x22FFFFFF) else Color(0x18000000), RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        color = if (isDark) Color(0xFF141E33) else Color(0xFFF1F5F9)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ExpenseScopeTabItem(
                scope = ExpenseScope.ALL,
                title = "All Expenses",
                count = "${summary.count}",
                isSelected = selectedScope == ExpenseScope.ALL,
                onClick = { onSelectScope(ExpenseScope.ALL) },
                icon = Icons.Default.Receipt,
                primaryColor = primaryColor,
                modifier = Modifier.weight(1f)
            )

            ExpenseScopeTabItem(
                scope = ExpenseScope.BUSINESS,
                title = "Business",
                count = "${summary.businessCount}",
                isSelected = selectedScope == ExpenseScope.BUSINESS,
                onClick = { onSelectScope(ExpenseScope.BUSINESS) },
                icon = Icons.Default.Business,
                primaryColor = primaryColor,
                modifier = Modifier.weight(1f)
            )

            ExpenseScopeTabItem(
                scope = ExpenseScope.HOME,
                title = "Home",
                count = "${summary.homeCount}",
                isSelected = selectedScope == ExpenseScope.HOME,
                onClick = { onSelectScope(ExpenseScope.HOME) },
                icon = Icons.Default.Home,
                primaryColor = primaryColor,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ExpenseScopeTabItem(
    scope: ExpenseScope,
    title: String,
    count: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) primaryColor else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// =========================================================================
// EXPENSE HERO SUMMARY CARD
// =========================================================================

@Composable
private fun ExpenseHeroSummaryCard(
    selectedScope: ExpenseScope,
    summary: ExpensesSummary,
    selectedTimeFilter: ExpenseTimeFilter,
    onSelectTimeFilter: (ExpenseTimeFilter) -> Unit,
    onCopyAuditReport: () -> Unit,
    primaryColor: Color,
    expenseAccent: Color,
    isDark: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, if (isDark) Color(0x22FFFFFF) else Color(0x18000000), RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = if (isDark) Color(0xFF141E33) else Color(0xFFFFFFFF),
        shadowElevation = if (isDark) 0.dp else 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Scope Title + Time Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(expenseAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = expenseAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${selectedScope.label.uppercase()} SUMMARY",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DigitalMonospaceFont,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = onCopyAuditReport,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Audit",
                        tint = primaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Total Amount Display
            Column {
                val totalDisplay = when (selectedScope) {
                    ExpenseScope.ALL -> summary.formattedTotal
                    ExpenseScope.BUSINESS -> summary.formattedBusiness
                    ExpenseScope.HOME -> summary.formattedHome
                }
                Text(
                    text = totalDisplay,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont,
                    color = expenseAccent
                )
                Text(
                    text = "${summary.count} Total Expense Entries Recorded",
                    fontSize = 11.sp,
                    fontFamily = DigitalMonospaceFont,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Time Filter Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ExpenseTimeFilter.values().forEach { filter ->
                    val isSelected = selectedTimeFilter == filter
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelectTimeFilter(filter) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) primaryColor else if (isDark) Color(0x18FFFFFF) else Color(0x0C000000),
                        border = BorderStroke(1.dp, if (isSelected) primaryColor else Color.Transparent)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = filter.label,
                                fontSize = 10.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Metrics Grid: Today vs This Month
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricSubCard(
                    title = "TODAY'S EXPENSE",
                    amount = summary.formattedToday,
                    icon = Icons.Default.Today,
                    color = Color(0xFFF59E0B),
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )

                MetricSubCard(
                    title = "THIS MONTH",
                    amount = summary.formattedMonth,
                    icon = Icons.Default.CalendarMonth,
                    color = Color(0xFF3B82F6),
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MetricSubCard(
    title: String,
    amount: String,
    icon: ImageVector,
    color: Color,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, if (isDark) Color(0x18FFFFFF) else Color(0x10000000), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        color = if (isDark) Color(0xFF0F1726) else Color(0xFFF8FAFC)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = title,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = amount,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                color = color
            )
        }
    }
}

// =========================================================================
// SEARCH BAR & QUICK ACTION
// =========================================================================

@Composable
private fun ExpenseSearchBarAndAction(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onOpenAddSheet: () -> Unit,
    onOpenCalc: () -> Unit,
    primaryColor: Color,
    isDark: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onQueryChange,
            placeholder = { Text("Search expense, party, mode...", fontSize = 13.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
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
                .weight(1f)
                .testTag("expense_search_input")
        )

        // Quick Calculator Button
        Surface(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, if (isDark) Color(0x33FFFFFF) else Color(0x28000000), RoundedCornerShape(12.dp))
                .clickable { onOpenCalc() }
                .testTag("expense_quick_calc_button"),
            shape = RoundedCornerShape(12.dp),
            color = if (isDark) Color(0xFF141E33) else Color(0xFFFFFFFF)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Calculate,
                    contentDescription = "Quick Calculator",
                    tint = primaryColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

// =========================================================================
// TOP CATEGORY BREAKDOWN CARD
// =========================================================================

@Composable
private fun ExpenseCategorySummaryCard(
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
                    text = "TOP EXPENSE CATEGORIES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    fontFamily = DigitalMonospaceFont,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                categories.entries.sortedByDescending { it.value }.take(6).forEach { (catName, amount) ->
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
                            color = Color(0xFFEF4444)
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// EXPENSE ROW ITEM (WITH BANK STATEMENT RUNNING CUMULATIVE BALANCE)
// =========================================================================

@Composable
private fun ExpenseRowItem(
    expense: LedgerTransactionEntity,
    statement: ExpenseStatementItem? = null,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    isDark: Boolean
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.US) }
    val accentColor = Color(0xFFEF4444)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, if (isDark) Color(0x22FFFFFF) else Color(0x18000000), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("expense_row_${expense.id}"),
        shape = RoundedCornerShape(12.dp),
        color = if (isDark) Color(0xFF141E33) else Color(0xFFFFFFFF)
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(accentColor.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = LedgerCategoryIcons.getCategoryIcon(expense.category),
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = expense.category,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (expense.partyName.isNotBlank()) {
                                Text(
                                    text = " • ${expense.partyName}",
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
                                text = dateFormat.format(Date(expense.dateMillis)),
                                fontSize = 10.5.sp,
                                fontFamily = DigitalMonospaceFont,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = " | ${expense.paymentMode}",
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (expense.remarks.isNotBlank()) {
                                Text(
                                    text = " (${expense.remarks})",
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "-${PkrNumberFormatter.formatPkrAmount(expense.amount)}",
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

            // Bank Statement Cumulative Running Expense Strip
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
                            imageVector = Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Cumulative: ",
                            fontSize = 10.5.sp,
                            fontFamily = DigitalMonospaceFont,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = "${statement.formattedCumulativeBefore} ➔",
                            fontSize = 10.5.sp,
                            fontFamily = DigitalMonospaceFont,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(5.dp),
                        color = Color(0x18EF4444)
                    ) {
                        Text(
                            text = "Total Spent: ${statement.formattedCumulativeAfter}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = DigitalMonospaceFont,
                            color = Color(0xFFEF4444),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// EMPTY STATE CARD
// =========================================================================

@Composable
private fun ExpenseEmptyStateCard(
    scope: ExpenseScope,
    onAddExpense: () -> Unit,
    primaryColor: Color,
    isDark: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, if (isDark) Color(0x22FFFFFF) else Color(0x18000000), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = if (isDark) Color(0xFF141E33) else Color(0xFFFFFFFF)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFEF4444).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(28.dp)
                )
            }

            Text(
                text = "No ${scope.shortName} Expenses Found",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Track your daily ${scope.label.lowercase()} expenses with automated categories, instant keypad calculations, and seamless ledger synchronization.",
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = onAddExpense,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEF4444),
                    contentColor = Color.White
                )
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Log ${scope.shortName} Expense", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// =========================================================================
// ADD / EDIT EXPENSE SHEET CONTENT WITH KEYPAD & AUTO-PICKUP/CREATE
// =========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseAddEditSheetContent(
    uiState: ExpensesUiState,
    onScopeChange: (ExpenseScope) -> Unit,
    onAmountChange: (String) -> Unit,
    onTokenAppend: (String) -> Unit,
    onBackspaceAmount: () -> Unit,
    onClearAmount: () -> Unit,
    onOpenCalculator: () -> Unit,
    onCategoryChange: (String) -> Unit,
    onPaymentModeChange: (String) -> Unit,
    onPaidToChange: (String) -> Unit,
    onRemarksChange: (String) -> Unit,
    onRefNoChange: (String) -> Unit,
    onDateChange: (Long) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    primaryColor: Color,
    isDark: Boolean
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var isDialpadOpen by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }
    val isEditing = uiState.editingExpense != null

    val currentScopeCategories = if (uiState.formScope == ExpenseScope.HOME) {
        uiState.homeCategories
    } else {
        uiState.businessCategories
    }

    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val isImeOpen = WindowInsets.ime.getBottom(density) > 0
    var showAmountContextMenu by remember { mutableStateOf(false) }

    // Dismiss keypad automatically whenever user scrolls the form or opens system keyboard for text
    LaunchedEffect(scrollState.isScrollInProgress, isImeOpen) {
        if (scrollState.isScrollInProgress || isImeOpen) {
            isDialpadOpen = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("expense_add_edit_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .padding(bottom = if (isDialpadOpen) 290.dp else 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isEditing) "Edit Expense" else "Log Everyday Expense",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                color = MaterialTheme.colorScheme.onSurface
            )

            IconButton(onClick = onCancel, modifier = Modifier.size(32.dp)) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
            }
        }

        // Scope Switcher: Business vs Home
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
                // Business Scope
                val isBiz = uiState.formScope == ExpenseScope.BUSINESS
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isBiz) primaryColor else Color.Transparent)
                        .clickable { onScopeChange(ExpenseScope.BUSINESS) }
                        .testTag("form_scope_business"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = null,
                            tint = if (isBiz) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Business Expense",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isBiz) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Home Scope
                val isHome = uiState.formScope == ExpenseScope.HOME
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isHome) Color(0xFFF97316) else Color.Transparent)
                        .clickable { onScopeChange(ExpenseScope.HOME) }
                        .testTag("form_scope_home"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            tint = if (isHome) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Home Expense",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isHome) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Amount Input with Smart Keypad Integration
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "EXPENSE AMOUNT (RS.) *",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = uiState.formAmountText,
                    onValueChange = onAmountChange,
                    readOnly = true,
                    placeholder = { Text("0.00", fontFamily = DigitalMonospaceFont) },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (uiState.formAmountText.isNotEmpty()) {
                                IconButton(onClick = onClearAmount, modifier = Modifier.size(28.dp)) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                }
                            }
                            IconButton(
                                onClick = onOpenCalculator,
                                modifier = Modifier.size(28.dp).testTag("form_calc_icon_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Calculate,
                                    contentDescription = "Quick Calculator",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    },
                    suffix = {
                        if (isDialpadOpen) {
                            BlinkingCursorPointer(color = Color(0xFFEF4444))
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFEF4444),
                        unfocusedBorderColor = if (isDialpadOpen) Color(0xFFEF4444) else if (isDark) Color(0x33FFFFFF) else Color(0x28000000),
                        focusedContainerColor = if (isDialpadOpen) Color(0xFFEF4444).copy(alpha = 0.08f) else Color.Transparent,
                        unfocusedContainerColor = if (isDialpadOpen) Color(0xFFEF4444).copy(alpha = 0.05f) else Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("form_expense_amount_input")
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
                                    isDialpadOpen = true
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

        // Category Input with Dynamic Auto-pickup & Create (Shows suggestions when typing)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "EXPENSE CATEGORY *",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = uiState.formCategory,
                onValueChange = onCategoryChange,
                placeholder = { Text(if (uiState.formScope == ExpenseScope.HOME) "e.g. Groceries, Electricity, School Fees..." else "e.g. Raw Material, Salary, Rent, Fuel...") },
                leadingIcon = {
                    Icon(
                        imageVector = LedgerCategoryIcons.getCategoryIcon(uiState.formCategory),
                        contentDescription = null,
                        tint = primaryColor,
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
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { if (it.isFocused) isDialpadOpen = false }
                    .testTag("form_expense_category_input")
            )

            // Dynamic Auto-suggestions & "➕ Add '[typed text]'" - Only shows when user types letters
            val inputCat = uiState.formCategory.trim()
            val isCustomCat = inputCat.isNotEmpty() && currentScopeCategories.none { it.equals(inputCat, ignoreCase = true) }
            val matchingCategories = remember(uiState.formCategory, currentScopeCategories) {
                if (inputCat.isEmpty()) {
                    emptyList()
                } else {
                    currentScopeCategories.filter {
                        it.contains(inputCat, ignoreCase = true) && !it.equals(inputCat, ignoreCase = true)
                    }
                }
            }

            AnimatedVisibility(
                visible = inputCat.isNotEmpty() && (isCustomCat || matchingCategories.isNotEmpty()),
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isCustomCat) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, Color(0xFF10B981)),
                                modifier = Modifier.clickable { onCategoryChange(inputCat) }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Create \"$inputCat\"",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF10B981)
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

        // Payment Mode Input with Automated Pickup & Create (Shows suggestions when typing)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "PAID VIA / PAYMENT CHANNEL *",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = uiState.formPaymentMode,
                onValueChange = onPaymentModeChange,
                placeholder = { Text("e.g. Cash, Bank Transfer, Easypaisa, JazzCash...") },
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
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { if (it.isFocused) isDialpadOpen = false }
                    .testTag("form_expense_mode_input")
            )

            // Dynamic Auto-suggestions & "➕ Add '[typed text]'" - Only shows when user types letters
            val inputMode = uiState.formPaymentMode.trim()
            val isCustomMode = inputMode.isNotEmpty() && uiState.availableModes.none { it.equals(inputMode, ignoreCase = true) }
            val matchingModes = remember(uiState.formPaymentMode, uiState.availableModes) {
                if (inputMode.isEmpty()) {
                    emptyList()
                } else {
                    uiState.availableModes.filter {
                        it.contains(inputMode, ignoreCase = true) && !it.equals(inputMode, ignoreCase = true)
                    }
                }
            }

            AnimatedVisibility(
                visible = inputMode.isNotEmpty() && (isCustomMode || matchingModes.isNotEmpty()),
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isCustomMode) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, Color(0xFF10B981)),
                                modifier = Modifier.clickable { onPaymentModeChange(inputMode) }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Create Mode \"$inputMode\"",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF10B981)
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

        // Date Picker
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "EXPENSE DATE",
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

        // Paid To (Vendor / Person / Store)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "PAID TO / VENDOR / STORE (OPTIONAL)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = uiState.formPaidTo,
                onValueChange = onPaidToChange,
                placeholder = { Text("Vendor name, store name, employee...") },
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
                    .onFocusChanged { if (it.isFocused) isDialpadOpen = false }
            )
        }

        // Bill / Invoice Ref #
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "BILL / INVOICE / RECEIPT # (OPTIONAL)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = uiState.formReferenceNo,
                onValueChange = onRefNoChange,
                placeholder = { Text("Receipt #, Bill #, Token #...") },
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
                    .onFocusChanged { if (it.isFocused) isDialpadOpen = false }
            )
        }

        // Remarks / Notes
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "REMARKS / ITEMS (OPTIONAL)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = uiState.formRemarks,
                onValueChange = onRemarksChange,
                placeholder = { Text("Add purchased items or notes...") },
                maxLines = 3,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { if (it.isFocused) isDialpadOpen = false }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Save and Cancel Buttons
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
                enabled = !uiState.isSubmitting && uiState.formAmountText.isNotBlank(),
                modifier = Modifier
                    .weight(1.5f)
                    .height(50.dp)
                    .testTag("form_save_expense_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEF4444),
                    contentColor = Color.White
                )
            ) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEditing) "Update Expense" else "Save Expense",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    // Bottom-docked Smart Dialpad (slides up from the bottom of the screen, auto-dismisses on scroll)
    AnimatedVisibility(
        visible = isDialpadOpen,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = Modifier.align(Alignment.BottomCenter)
    ) {
        Surface(
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            color = if (isDark) Color(0xFF0F172A) else Color(0xFFFFFFFF),
            border = BorderStroke(1.dp, if (isDark) Color(0x33EF4444) else Color(0x22000000)),
            shadowElevation = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            SmartStationKeypad(
                activeFieldLabel = "Expense Amount",
                onDigit = { onTokenAppend(it) },
                onDoubleZero = { onTokenAppend("00") },
                onDecimal = { onTokenAppend(".") },
                onBackspace = { onBackspaceAmount() },
                onClear = { onClearAmount() },
                onDone = { isDialpadOpen = false },
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

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.TrendingDown
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
import com.example.domain.util.IndianNumberFormatter
import com.example.ui.ExpenseScope
import com.example.ui.ExpenseTimeFilter
import com.example.ui.ExpensesSummary
import com.example.ui.ExpensesUiState
import com.example.ui.ExpensesViewModel
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

    val isDark = MaterialTheme.colorScheme.background.red < 0.2f
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
            .testTag("expenses_screen_container")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // 1. Scope Tabs (All, Business, Home)
            ExpenseScopeTabBar(
                selectedScope = uiState.selectedScope,
                onSelectScope = { viewModel.setScope(it) },
                summary = uiState.summary,
                primaryColor = primaryColor,
                isDark = isDark
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Main Scrollable Content
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Top Metrics Cards
                item {
                    ExpenseHeroSummaryCard(
                        summary = uiState.summary,
                        currentScope = uiState.selectedScope,
                        onCopyReport = {
                            val report = viewModel.buildExpenseSummaryReport()
                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Expenses Report", report))
                            Toast.makeText(context, "Expenses report copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        isDark = isDark
                    )
                }

                // Time Period Cards (Today, Month, Breakdown)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ExpenseStatPill(
                            title = "Today's Expense",
                            amount = uiState.summary.formattedToday,
                            icon = Icons.Default.Today,
                            accentColor = Color(0xFFF59E0B),
                            isDark = isDark,
                            modifier = Modifier.weight(1f)
                        )

                        ExpenseStatPill(
                            title = "This Month",
                            amount = uiState.summary.formattedMonth,
                            icon = Icons.Default.CalendarMonth,
                            accentColor = Color(0xFF38BDF8),
                            isDark = isDark,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Search and Time Filters
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("Search expense, category, vendor, mode...") },
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
                                .height(52.dp)
                                .testTag("expense_search_input")
                        )

                        // Time Filter Chips
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(ExpenseTimeFilter.values()) { timeFilter ->
                                val isSelected = uiState.selectedTimeFilter == timeFilter
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { viewModel.setTimeFilter(timeFilter) },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) primaryColor.copy(alpha = 0.2f) else if (isDark) Color(0x18FFFFFF) else Color(0x0C000000),
                                    border = BorderStroke(1.dp, if (isSelected) primaryColor else if (isDark) Color(0x22FFFFFF) else Color(0x18000000))
                                ) {
                                    Text(
                                        text = timeFilter.label,
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Category Summary Chips if available
                if (uiState.summary.topCategories.isNotEmpty()) {
                    item {
                        ExpenseCategorySummaryCard(
                            categories = uiState.summary.topCategories,
                            primaryColor = primaryColor,
                            isDark = isDark
                        )
                    }
                }

                // Section Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "EXPENSE LOG (${uiState.filteredExpenses.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontFamily = DigitalMonospaceFont,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Expense List Items
                if (uiState.filteredExpenses.isEmpty()) {
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isDark) Color(0x12FFFFFF) else Color(0x0A000000)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Receipt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(42.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No expenses recorded for this filter",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(uiState.filteredExpenses, key = { it.id }) { expense ->
                        ExpenseRowItem(
                            expense = expense,
                            onClick = { viewModel.openDetail(expense) },
                            onEdit = { viewModel.openEditExpense(expense) },
                            onDelete = { expenseToDelete = expense },
                            isDark = isDark
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(70.dp))
                }
            }
        }

        // Floating Action Button to Add Expense
        FloatingActionButton(
            onClick = {
                viewModel.openAddExpense(
                    initialScope = if (uiState.selectedScope == ExpenseScope.HOME) ExpenseScope.HOME else ExpenseScope.BUSINESS
                )
            },
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
        // EXPENSE DETAIL MODAL
        // ==========================================
        if (uiState.isDetailDialogOpen && uiState.selectedExpenseForDetail != null) {
            val exp = uiState.selectedExpenseForDetail!!
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
                            text = "Expense Voucher",
                            fontWeight = FontWeight.Bold,
                            fontFamily = DigitalMonospaceFont
                        )
                        IconButton(onClick = { viewModel.closeDetail() }, modifier = Modifier.size(28.dp)) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
                        }
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = expenseAccent.copy(alpha = 0.12f)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Rs. ${IndianNumberFormatter.formatIndianDouble(exp.amount, 2)}",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = DigitalMonospaceFont,
                                    color = expenseAccent
                                )
                                Text(
                                    text = "EVERYDAY EXPENSE",
                                    fontSize = 11.sp,
                                    fontFamily = DigitalMonospaceFont,
                                    color = expenseAccent
                                )
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
                    Text("Are you sure you want to delete this expense of Rs. ${IndianNumberFormatter.formatIndianDouble(exp.amount, 2)} (${exp.category})?")
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
    summary: ExpensesSummary,
    currentScope: ExpenseScope,
    onCopyReport: () -> Unit,
    isDark: Boolean
) {
    val cardColor = if (isDark) Color(0xFF1E1010) else Color(0xFF1A1A24)
    val accentColor = Color(0xFFEF4444)

    val activeTotal = when (currentScope) {
        ExpenseScope.ALL -> summary.formattedTotal
        ExpenseScope.BUSINESS -> summary.formattedBusiness
        ExpenseScope.HOME -> summary.formattedHome
    }

    val activeLabel = when (currentScope) {
        ExpenseScope.ALL -> "TOTAL EVERYDAY EXPENSES"
        ExpenseScope.BUSINESS -> "TOTAL BUSINESS EXPENSES"
        ExpenseScope.HOME -> "TOTAL HOME EXPENSES"
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(BorderStroke(1.dp, Color(0x33EF4444)), RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        color = cardColor,
        shadowElevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                        text = activeLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontFamily = DigitalMonospaceFont,
                        color = Color(0xFFFCA5A5)
                    )
                }

                IconButton(onClick = onCopyReport, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Report",
                        tint = Color(0xFFFCA5A5),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = activeTotal,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = DigitalMonospaceFont,
                color = Color(0xFFF87171)
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(thickness = 0.8.dp, color = Color(0x22FFFFFF))
            Spacer(modifier = Modifier.height(10.dp))

            // Business vs Home Split Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ScopeMiniPill(
                    title = "Business Expenses",
                    amount = summary.formattedBusiness,
                    icon = Icons.Default.Business,
                    modifier = Modifier.weight(1f)
                )

                ScopeMiniPill(
                    title = "Home Expenses",
                    amount = summary.formattedHome,
                    icon = Icons.Default.Home,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ScopeMiniPill(
    title: String,
    amount: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clip(RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        color = Color(0x18FFFFFF)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFFFCA5A5),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = title,
                    fontSize = 10.sp,
                    fontFamily = DigitalMonospaceFont,
                    color = Color(0xFFCBD5E1),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = amount,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ExpenseStatPill(
    title: String,
    amount: String,
    icon: ImageVector,
    accentColor: Color,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        color = if (isDark) Color(0xFF141E33) else Color(0xFFFFFFFF)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DigitalMonospaceFont,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(14.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = amount,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DigitalMonospaceFont,
                color = accentColor
            )
        }
    }
}

// =========================================================================
// CATEGORY BREAKDOWN CARD
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
                            text = "Rs. ${IndianNumberFormatter.formatIndianDouble(amount, 2)}",
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
// EXPENSE ROW ITEM
// =========================================================================

@Composable
private fun ExpenseRowItem(
    expense: LedgerTransactionEntity,
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
                    text = "-Rs. ${IndianNumberFormatter.formatIndianDouble(expense.amount, 2)}",
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
// ADD / EDIT EXPENSE SHEET CONTENT WITH AUTO-SUGGEST & AUTO-SAVE
// =========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseAddEditSheetContent(
    uiState: ExpensesUiState,
    onScopeChange: (ExpenseScope) -> Unit,
    onAmountChange: (String) -> Unit,
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
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }
    val isEditing = uiState.editingExpense != null

    val currentScopeCategories = if (uiState.formScope == ExpenseScope.HOME) {
        uiState.homeCategories
    } else {
        uiState.businessCategories
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("expense_add_edit_sheet"),
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
                        .background(if (isHome) Color(0xFF0284C7) else Color.Transparent)
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

        // Amount Input
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "EXPENSE AMOUNT (RS.) *",
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
                        color = Color(0xFFEF4444),
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
                    focusedBorderColor = Color(0xFFEF4444),
                    unfocusedBorderColor = if (isDark) Color(0x33FFFFFF) else Color(0x28000000)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("form_expense_amount_input")
            )
        }

        // Category Input with Auto-suggestions (hidden initially, suggested as user types, auto-saved)
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
                    .testTag("form_expense_category_input")
            )

            // Dynamic Auto-suggestions: Shown ONLY when matching items exist
            val matchingCategories = remember(uiState.formCategory, currentScopeCategories) {
                val input = uiState.formCategory.trim()
                if (input.isEmpty()) {
                    emptyList()
                } else {
                    currentScopeCategories.filter {
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

        // Payment Mode Input with Auto-suggestions
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
                    .testTag("form_expense_mode_input")
            )

            // Dynamic Auto-suggestions for Payment Mode
            val matchingModes = remember(uiState.formPaymentMode, uiState.availableModes) {
                val input = uiState.formPaymentMode.trim()
                if (input.isEmpty()) {
                    emptyList()
                } else {
                    uiState.availableModes.filter {
                        it.contains(input, ignoreCase = true) && !it.equals(input, ignoreCase = true)
                    }
                }
            }

            if (matchingModes.isNotEmpty()) {
                Text(
                    text = "Matching payment channels:",
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
                modifier = Modifier.fillMaxWidth()
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
                modifier = Modifier.fillMaxWidth()
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
                modifier = Modifier.fillMaxWidth()
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

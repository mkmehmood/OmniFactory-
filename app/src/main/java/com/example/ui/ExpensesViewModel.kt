package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.LedgerTransactionDao
import com.example.data.db.LedgerTransactionEntity
import com.example.domain.ledger.LedgerUnifiedDataManager
import com.example.domain.util.IndianNumberFormatter
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class ExpenseScope(val label: String, val shortName: String) {
    ALL("All Expenses", "All"),
    BUSINESS("Business", "Business"),
    HOME("Home / Personal", "Home")
}

enum class ExpenseTimeFilter(val label: String) {
    ALL_TIME("All Time"),
    THIS_MONTH("This Month"),
    THIS_WEEK("This Week"),
    TODAY("Today")
}

data class ExpensesSummary(
    val totalExpense: Double = 0.0,
    val businessExpense: Double = 0.0,
    val homeExpense: Double = 0.0,
    val todayExpense: Double = 0.0,
    val monthExpense: Double = 0.0,
    val count: Int = 0,
    val businessCount: Int = 0,
    val homeCount: Int = 0,
    val topCategories: Map<String, Double> = emptyMap(),
    val byMode: Map<String, Double> = emptyMap()
) {
    val formattedTotal: String get() = "Rs. ${IndianNumberFormatter.formatIndianDouble(totalExpense, 2)}"
    val formattedBusiness: String get() = "Rs. ${IndianNumberFormatter.formatIndianDouble(businessExpense, 2)}"
    val formattedHome: String get() = "Rs. ${IndianNumberFormatter.formatIndianDouble(homeExpense, 2)}"
    val formattedToday: String get() = "Rs. ${IndianNumberFormatter.formatIndianDouble(todayExpense, 2)}"
    val formattedMonth: String get() = "Rs. ${IndianNumberFormatter.formatIndianDouble(monthExpense, 2)}"
}

data class ExpensesUiState(
    val selectedScope: ExpenseScope = ExpenseScope.ALL,
    val selectedTimeFilter: ExpenseTimeFilter = ExpenseTimeFilter.ALL_TIME,
    val searchQuery: String = "",
    val isAddSheetOpen: Boolean = false,
    val isDetailDialogOpen: Boolean = false,
    val isCalculatorOpen: Boolean = false,
    val selectedExpenseForDetail: LedgerTransactionEntity? = null,
    val editingExpense: LedgerTransactionEntity? = null,
    // Add/Edit Form state
    val formScope: ExpenseScope = ExpenseScope.BUSINESS,
    val formAmountText: String = "",
    val formCategory: String = "Raw Materials",
    val formPaymentMode: String = "Cash",
    val formPaidTo: String = "",
    val formRemarks: String = "",
    val formReferenceNo: String = "",
    val formDateMillis: Long = System.currentTimeMillis(),
    val isSubmitting: Boolean = false,
    // Data lists
    val allExpenses: List<LedgerTransactionEntity> = emptyList(),
    val filteredExpenses: List<LedgerTransactionEntity> = emptyList(),
    val summary: ExpensesSummary = ExpensesSummary(),
    val businessCategories: List<String> = emptyList(),
    val homeCategories: List<String> = emptyList(),
    val availableModes: List<String> = emptyList()
)

class ExpensesViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val appContext: Context = application.applicationContext
    private val ledgerDao: LedgerTransactionDao = AppDatabase.getDatabase(application).ledgerTransactionDao()

    private val _uiState = MutableStateFlow(ExpensesUiState())
    val uiState: StateFlow<ExpensesUiState> = _uiState

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    init {
        loadSynchronizedCategories()

        viewModelScope.launch {
            ledgerDao.getAllTransactions().collect { allList ->
                val expensesOnly = allList.filter {
                    it.type.equals("CASH_OUT", ignoreCase = true) ||
                    it.type.equals("EXPENSE", ignoreCase = true) ||
                    it.type.equals("HOME_EXPENSE", ignoreCase = true) ||
                    it.type.equals("BUSINESS_EXPENSE", ignoreCase = true) ||
                    it.type.contains("OUT", ignoreCase = true) ||
                    it.type.contains("EXPENSE", ignoreCase = true)
                }

                _uiState.update { current ->
                    val summary = computeSummary(expensesOnly)
                    val filtered = filterExpenses(
                        expensesOnly,
                        current.selectedScope,
                        current.selectedTimeFilter,
                        current.searchQuery
                    )

                    val dbBizCategories = expensesOnly
                        .filter { !LedgerUnifiedDataManager.isHomeExpense(it.category, it.expenseScope) }
                        .map { it.category.trim() }
                        .filter { it.isNotEmpty() }

                    val dbHomeCategories = expensesOnly
                        .filter { LedgerUnifiedDataManager.isHomeExpense(it.category, it.expenseScope) }
                        .map { it.category.trim() }
                        .filter { it.isNotEmpty() }

                    val dbModes = expensesOnly.map { it.paymentMode.trim() }.filter { it.isNotEmpty() }

                    val bizList = (LedgerUnifiedDataManager.getBusinessCategories(appContext) + dbBizCategories).distinct()
                    val homeList = (LedgerUnifiedDataManager.getHomeCategories(appContext) + dbHomeCategories).distinct()
                    val modesList = (LedgerUnifiedDataManager.getPaymentModes(appContext) + dbModes).distinct()

                    current.copy(
                        allExpenses = expensesOnly,
                        filteredExpenses = filtered,
                        summary = summary,
                        businessCategories = bizList,
                        homeCategories = homeList,
                        availableModes = modesList
                    )
                }
            }
        }
    }

    private fun loadSynchronizedCategories() {
        val bizList = LedgerUnifiedDataManager.getBusinessCategories(appContext)
        val homeList = LedgerUnifiedDataManager.getHomeCategories(appContext)
        val modesList = LedgerUnifiedDataManager.getPaymentModes(appContext)

        _uiState.update {
            it.copy(
                businessCategories = bizList,
                homeCategories = homeList,
                availableModes = modesList
            )
        }
    }

    private fun isHomeExpenseItem(category: String, scope: String): Boolean {
        return LedgerUnifiedDataManager.isHomeExpense(category, scope)
    }

    private fun computeSummary(expenses: List<LedgerTransactionEntity>): ExpensesSummary {
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val monthStart = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        var total = 0.0
        var bizTotal = 0.0
        var homeTotal = 0.0
        var todayTotal = 0.0
        var monthTotal = 0.0
        var bizCount = 0
        var homeCount = 0

        val byCategory = mutableMapOf<String, Double>()
        val byMode = mutableMapOf<String, Double>()

        for (item in expenses) {
            total += item.amount
            if (item.dateMillis >= todayStart) todayTotal += item.amount
            if (item.dateMillis >= monthStart) monthTotal += item.amount

            val isHome = isHomeExpenseItem(item.category, item.expenseScope)
            if (isHome) {
                homeTotal += item.amount
                homeCount++
            } else {
                bizTotal += item.amount
                bizCount++
            }

            byCategory[item.category] = (byCategory[item.category] ?: 0.0) + item.amount
            val mode = item.paymentMode.ifBlank { "Cash" }
            byMode[mode] = (byMode[mode] ?: 0.0) + item.amount
        }

        return ExpensesSummary(
            totalExpense = total,
            businessExpense = bizTotal,
            homeExpense = homeTotal,
            todayExpense = todayTotal,
            monthExpense = monthTotal,
            count = expenses.size,
            businessCount = bizCount,
            homeCount = homeCount,
            topCategories = byCategory,
            byMode = byMode
        )
    }

    private fun filterExpenses(
        list: List<LedgerTransactionEntity>,
        scope: ExpenseScope,
        timeFilter: ExpenseTimeFilter,
        query: String
    ): List<LedgerTransactionEntity> {
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val weekStart = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val monthStart = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val trimmedQuery = query.trim()

        return list.filter { item ->
            val isHome = isHomeExpenseItem(item.category, item.expenseScope)
            val matchScope = when (scope) {
                ExpenseScope.ALL -> true
                ExpenseScope.BUSINESS -> !isHome
                ExpenseScope.HOME -> isHome
            }

            val matchTime = when (timeFilter) {
                ExpenseTimeFilter.ALL_TIME -> true
                ExpenseTimeFilter.TODAY -> item.dateMillis >= todayStart
                ExpenseTimeFilter.THIS_WEEK -> item.dateMillis >= weekStart
                ExpenseTimeFilter.THIS_MONTH -> item.dateMillis >= monthStart
            }

            val matchQuery = trimmedQuery.isEmpty() ||
                    item.category.contains(trimmedQuery, ignoreCase = true) ||
                    item.partyName.contains(trimmedQuery, ignoreCase = true) ||
                    item.remarks.contains(trimmedQuery, ignoreCase = true) ||
                    item.referenceNo.contains(trimmedQuery, ignoreCase = true) ||
                    item.paymentMode.contains(trimmedQuery, ignoreCase = true) ||
                    item.amount.toString().contains(trimmedQuery)

            matchScope && matchTime && matchQuery
        }
    }

    private fun recomputeState() {
        val current = _uiState.value
        val filtered = filterExpenses(
            current.allExpenses,
            current.selectedScope,
            current.selectedTimeFilter,
            current.searchQuery
        )
        _uiState.update { it.copy(filteredExpenses = filtered) }
    }

    // --- Controls ---
    fun setScope(scope: ExpenseScope) {
        _uiState.update { it.copy(selectedScope = scope) }
        recomputeState()
    }

    fun setTimeFilter(filter: ExpenseTimeFilter) {
        _uiState.update { it.copy(selectedTimeFilter = filter) }
        recomputeState()
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        recomputeState()
    }

    // --- Calculator Dialog ---
    fun openCalculator() {
        _uiState.update { it.copy(isCalculatorOpen = true) }
    }

    fun closeCalculator() {
        _uiState.update { it.copy(isCalculatorOpen = false) }
    }

    // --- Add / Edit Expense Sheet ---
    fun openAddExpense(initialScope: ExpenseScope = ExpenseScope.BUSINESS) {
        val defaultScope = if (initialScope == ExpenseScope.ALL) ExpenseScope.BUSINESS else initialScope
        val defaultCat = if (defaultScope == ExpenseScope.HOME) "Groceries & Food" else "Raw Materials"

        _uiState.update {
            it.copy(
                isAddSheetOpen = true,
                editingExpense = null,
                formScope = defaultScope,
                formAmountText = "",
                formCategory = defaultCat,
                formPaymentMode = "Cash",
                formPaidTo = "",
                formRemarks = "",
                formReferenceNo = "",
                formDateMillis = System.currentTimeMillis()
            )
        }
    }

    fun openEditExpense(expense: LedgerTransactionEntity) {
        val isHome = isHomeExpenseItem(expense.category, expense.expenseScope)
        _uiState.update {
            it.copy(
                isAddSheetOpen = true,
                editingExpense = expense,
                formScope = if (isHome) ExpenseScope.HOME else ExpenseScope.BUSINESS,
                formAmountText = if (expense.amount % 1.0 == 0.0) expense.amount.toLong().toString() else expense.amount.toString(),
                formCategory = expense.category,
                formPaymentMode = expense.paymentMode,
                formPaidTo = expense.partyName,
                formRemarks = expense.remarks,
                formReferenceNo = expense.referenceNo,
                formDateMillis = expense.dateMillis
            )
        }
    }

    fun closeAddSheet() {
        _uiState.update { it.copy(isAddSheetOpen = false, editingExpense = null) }
    }

    fun setFormScope(scope: ExpenseScope) {
        val currentCat = _uiState.value.formCategory
        val isHome = isHomeExpenseItem(currentCat, "")
        val newCat = if (scope == ExpenseScope.HOME && !isHome) {
            "Groceries & Food"
        } else if (scope == ExpenseScope.BUSINESS && isHome) {
            "Raw Materials"
        } else {
            currentCat
        }

        _uiState.update {
            it.copy(
                formScope = scope,
                formCategory = newCat
            )
        }
    }

    fun setFormAmount(amount: String) {
        _uiState.update { it.copy(formAmountText = amount) }
    }

    fun appendTokenToAmount(token: String) {
        val current = _uiState.value.formAmountText
        if (token == ".") {
            if (!current.contains(".")) {
                val next = if (current.isEmpty()) "0." else "$current."
                _uiState.update { it.copy(formAmountText = next) }
            }
        } else if (token == "00") {
            if (current.isNotEmpty() && current != "0") {
                _uiState.update { it.copy(formAmountText = "$current$token") }
            }
        } else {
            val next = if (current == "0") token else "$current$token"
            _uiState.update { it.copy(formAmountText = next) }
        }
    }

    fun backspaceAmount() {
        val current = _uiState.value.formAmountText
        if (current.isNotEmpty()) {
            _uiState.update { it.copy(formAmountText = current.dropLast(1)) }
        }
    }

    fun clearAmount() {
        _uiState.update { it.copy(formAmountText = "") }
    }

    fun setFormCategory(cat: String) {
        _uiState.update { it.copy(formCategory = cat) }
    }

    fun setFormPaymentMode(mode: String) {
        _uiState.update { it.copy(formPaymentMode = mode) }
    }

    fun setFormPaidTo(paidTo: String) {
        _uiState.update { it.copy(formPaidTo = paidTo) }
    }

    fun setFormRemarks(remarks: String) {
        _uiState.update { it.copy(formRemarks = remarks) }
    }

    fun setFormReferenceNo(refNo: String) {
        _uiState.update { it.copy(formReferenceNo = refNo) }
    }

    fun setFormDateMillis(millis: Long) {
        _uiState.update { it.copy(formDateMillis = millis) }
    }

    fun saveExpense() {
        val current = _uiState.value
        val amount = current.formAmountText.toDoubleOrNull()
        if (amount == null || amount <= 0.0) {
            viewModelScope.launch { _userMessage.emit("Please enter a valid expense amount.") }
            return
        }

        val isBiz = current.formScope == ExpenseScope.BUSINESS
        val expenseScopeStr = if (isBiz) "BUSINESS" else "HOME"
        val category = current.formCategory.trim().ifBlank { if (isBiz) "Raw Materials" else "Groceries & Food" }
        val mode = current.formPaymentMode.trim().ifBlank { "Cash" }
        val paidTo = current.formPaidTo.trim()
        val remarks = current.formRemarks.trim()
        val refNo = current.formReferenceNo.trim()
        val dateMillis = current.formDateMillis

        // Auto-save category & mode into synchronized master manager
        LedgerUnifiedDataManager.saveCategoryIfNew(appContext, category, expenseScopeStr)
        LedgerUnifiedDataManager.savePaymentModeIfNew(appContext, mode)

        loadSynchronizedCategories()

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val existing = current.editingExpense
            if (existing != null) {
                val updated = existing.copy(
                    type = "CASH_OUT",
                    amount = amount,
                    category = category,
                    paymentMode = mode,
                    expenseScope = expenseScopeStr,
                    partyName = paidTo,
                    remarks = remarks,
                    referenceNo = refNo,
                    dateMillis = dateMillis
                )
                ledgerDao.updateTransaction(updated)
                _userMessage.emit("Expense updated & synced with Daily Ledger.")
            } else {
                val newExpense = LedgerTransactionEntity(
                    type = "CASH_OUT",
                    amount = amount,
                    category = category,
                    paymentMode = mode,
                    expenseScope = expenseScopeStr,
                    partyName = paidTo,
                    remarks = remarks,
                    referenceNo = refNo,
                    dateMillis = dateMillis
                )
                ledgerDao.insertTransaction(newExpense)
                _userMessage.emit("${current.formScope.shortName} Expense of Rs. ${IndianNumberFormatter.formatIndianDouble(amount, 0)} logged to Ledger.")
            }
            _uiState.update {
                it.copy(
                    isAddSheetOpen = false,
                    isSubmitting = false,
                    editingExpense = null
                )
            }
        }
    }

    fun deleteExpense(expense: LedgerTransactionEntity) {
        viewModelScope.launch {
            ledgerDao.deleteTransaction(expense)
            _userMessage.emit("Expense deleted.")
            if (_uiState.value.selectedExpenseForDetail?.id == expense.id) {
                _uiState.update { it.copy(isDetailDialogOpen = false, selectedExpenseForDetail = null) }
            }
        }
    }

    fun openDetail(expense: LedgerTransactionEntity) {
        _uiState.update { it.copy(isDetailDialogOpen = true, selectedExpenseForDetail = expense) }
    }

    fun closeDetail() {
        _uiState.update { it.copy(isDetailDialogOpen = false, selectedExpenseForDetail = null) }
    }

    fun buildExpenseSummaryReport(): String {
        val state = _uiState.value
        val summary = state.summary
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)

        return buildString {
            appendLine("=== EVERYDAY EXPENSES AUDIT REPORT ===")
            appendLine("Filter: ${state.selectedScope.label} | ${state.selectedTimeFilter.label}")
            appendLine("--------------------------------------")
            appendLine("TOTAL EXPENSES:     ${summary.formattedTotal} (${summary.count} entries)")
            appendLine("Business Expenses:  ${summary.formattedBusiness} (${summary.businessCount} entries)")
            appendLine("Home / Personal:    ${summary.formattedHome} (${summary.homeCount} entries)")
            appendLine("Today's Expenses:   ${summary.formattedToday}")
            appendLine("This Month:         ${summary.formattedMonth}")
            appendLine("--------------------------------------")
            if (summary.topCategories.isNotEmpty()) {
                appendLine("EXPENSES BY CATEGORY:")
                summary.topCategories.entries.sortedByDescending { it.value }.take(10).forEach { (cat, amt) ->
                    appendLine("• $cat: Rs. ${IndianNumberFormatter.formatIndianDouble(amt, 2)}")
                }
                appendLine("--------------------------------------")
            }
            appendLine("EXPENSES LOG (${state.filteredExpenses.size} items):")
            state.filteredExpenses.forEachIndexed { i, exp ->
                val scopeTag = if (exp.expenseScope == "HOME" || isHomeExpenseItem(exp.category, exp.expenseScope)) "[Home]" else "[Business]"
                val party = if (exp.partyName.isNotBlank()) " | Paid to: ${exp.partyName}" else ""
                val rem = if (exp.remarks.isNotBlank()) " (${exp.remarks})" else ""
                appendLine("${i + 1}. [${dateFormat.format(Date(exp.dateMillis))}] $scopeTag ${exp.category}$party : Rs. ${IndianNumberFormatter.formatIndianDouble(exp.amount, 2)} [${exp.paymentMode}]$rem")
            }
            appendLine("======================================")
        }
    }
}

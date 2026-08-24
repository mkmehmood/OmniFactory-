package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.LedgerTransactionDao
import com.example.data.db.LedgerTransactionEntity
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

private const val EXPENSES_PREFS = "expenses_preferences"
private const val KEY_SAVED_BIZ_CATEGORIES = "saved_biz_categories"
private const val KEY_SAVED_HOME_CATEGORIES = "saved_home_categories"
private const val KEY_SAVED_EXPENSE_MODES = "saved_expense_modes"

val DEFAULT_BUSINESS_CATEGORIES = listOf(
    "Raw Materials",
    "Salaries & Labor",
    "Rent & Lease",
    "Electricity / Power",
    "Maintenance & Repairs",
    "Transport & Fuel",
    "Tea & Refreshment",
    "Packaging Supplies",
    "Office Supplies",
    "Taxes & Govt Fees",
    "Marketing & Ads",
    "Miscellaneous Business"
)

val DEFAULT_HOME_CATEGORIES = listOf(
    "Groceries & Food",
    "Home Electricity & Gas",
    "Water & Maintenance",
    "Home Rent",
    "School / Tuition Fees",
    "Medical & Healthcare",
    "House Help & Maid",
    "Clothing & Shopping",
    "Mobile & Internet",
    "Home Repairs",
    "Gifts & Charity",
    "Miscellaneous Home"
)

val DEFAULT_EXPENSE_MODES = listOf("Cash", "Bank Transfer", "Easypaisa", "JazzCash", "Cheque")

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
    val businessCategories: List<String> = DEFAULT_BUSINESS_CATEGORIES,
    val homeCategories: List<String> = DEFAULT_HOME_CATEGORIES,
    val availableModes: List<String> = DEFAULT_EXPENSE_MODES
)

class ExpensesViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences(EXPENSES_PREFS, Context.MODE_PRIVATE)
    private val ledgerDao: LedgerTransactionDao = AppDatabase.getDatabase(application).ledgerTransactionDao()

    private val _uiState = MutableStateFlow(ExpensesUiState())
    val uiState: StateFlow<ExpensesUiState> = _uiState

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    init {
        loadPersistedCategories()

        viewModelScope.launch {
            ledgerDao.getAllTransactions().collect { allList ->
                val expensesOnly = allList.filter { it.type == "CASH_OUT" }
                _uiState.update { current ->
                    val summary = computeSummary(expensesOnly)
                    val filtered = filterExpenses(
                        expensesOnly,
                        current.selectedScope,
                        current.selectedTimeFilter,
                        current.searchQuery
                    )
                    current.copy(
                        allExpenses = expensesOnly,
                        filteredExpenses = filtered,
                        summary = summary
                    )
                }
            }
        }
    }

    private fun loadPersistedCategories() {
        val savedBiz = prefs.getStringSet(KEY_SAVED_BIZ_CATEGORIES, null)
        val bizList = if (savedBiz != null && savedBiz.isNotEmpty()) {
            (DEFAULT_BUSINESS_CATEGORIES + savedBiz).distinct()
        } else {
            DEFAULT_BUSINESS_CATEGORIES
        }

        val savedHome = prefs.getStringSet(KEY_SAVED_HOME_CATEGORIES, null)
        val homeList = if (savedHome != null && savedHome.isNotEmpty()) {
            (DEFAULT_HOME_CATEGORIES + savedHome).distinct()
        } else {
            DEFAULT_HOME_CATEGORIES
        }

        val savedModes = prefs.getStringSet(KEY_SAVED_EXPENSE_MODES, null)
        val modesList = if (savedModes != null && savedModes.isNotEmpty()) {
            (DEFAULT_EXPENSE_MODES + savedModes).distinct()
        } else {
            DEFAULT_EXPENSE_MODES
        }

        _uiState.update {
            it.copy(
                businessCategories = bizList,
                homeCategories = homeList,
                availableModes = modesList
            )
        }
    }

    private fun autoSaveCategoryIfNew(category: String, isBusiness: Boolean) {
        val trimmed = category.trim()
        if (trimmed.isEmpty()) return
        if (isBusiness) {
            val current = _uiState.value.businessCategories
            if (current.none { it.equals(trimmed, ignoreCase = true) }) {
                val updated = current + trimmed
                prefs.edit().putStringSet(KEY_SAVED_BIZ_CATEGORIES, updated.toSet()).apply()
                _uiState.update { it.copy(businessCategories = updated) }
            }
        } else {
            val current = _uiState.value.homeCategories
            if (current.none { it.equals(trimmed, ignoreCase = true) }) {
                val updated = current + trimmed
                prefs.edit().putStringSet(KEY_SAVED_HOME_CATEGORIES, updated.toSet()).apply()
                _uiState.update { it.copy(homeCategories = updated) }
            }
        }
    }

    private fun autoSaveModeIfNew(mode: String) {
        val trimmed = mode.trim()
        if (trimmed.isEmpty()) return
        val current = _uiState.value.availableModes
        if (current.none { it.equals(trimmed, ignoreCase = true) }) {
            val updated = current + trimmed
            prefs.edit().putStringSet(KEY_SAVED_EXPENSE_MODES, updated.toSet()).apply()
            _uiState.update { it.copy(availableModes = updated) }
        }
    }

    private fun computeSummary(expenses: List<LedgerTransactionEntity>): ExpensesSummary {
        val now = Calendar.getInstance()
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

            val isHome = isHomeCategory(item.category)
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

    private fun isHomeCategory(cat: String): Boolean {
        val lower = cat.lowercase()
        return lower.contains("home") || lower.contains("grocer") || lower.contains("school") ||
                lower.contains("tuition") || lower.contains("maid") || lower.contains("doctor") ||
                lower.contains("medical") || lower.contains("clothing") || lower.contains("personal") ||
                lower.contains("family") || lower.contains("kitchen")
    }

    private fun filterExpenses(
        list: List<LedgerTransactionEntity>,
        scope: ExpenseScope,
        timeFilter: ExpenseTimeFilter,
        query: String
    ): List<LedgerTransactionEntity> {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()

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
            // Scope match
            val matchScope = when (scope) {
                ExpenseScope.ALL -> true
                ExpenseScope.BUSINESS -> !isHomeCategory(item.category)
                ExpenseScope.HOME -> isHomeCategory(item.category)
            }

            // Time match
            val matchTime = when (timeFilter) {
                ExpenseTimeFilter.ALL_TIME -> true
                ExpenseTimeFilter.TODAY -> item.dateMillis >= todayStart
                ExpenseTimeFilter.THIS_WEEK -> item.dateMillis >= weekStart
                ExpenseTimeFilter.THIS_MONTH -> item.dateMillis >= monthStart
            }

            // Search match
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
        val isHome = isHomeCategory(expense.category)
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
        val newCat = if (scope == ExpenseScope.HOME && !isHomeCategory(currentCat)) {
            "Groceries & Food"
        } else if (scope == ExpenseScope.BUSINESS && isHomeCategory(currentCat)) {
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
        val category = current.formCategory.trim().ifBlank { if (isBiz) "Raw Materials" else "Groceries & Food" }
        val mode = current.formPaymentMode.trim().ifBlank { "Cash" }
        val paidTo = current.formPaidTo.trim()
        val remarks = current.formRemarks.trim()
        val refNo = current.formReferenceNo.trim()
        val dateMillis = current.formDateMillis

        // Auto-save category & mode without extra prompt
        autoSaveCategoryIfNew(category, isBiz)
        autoSaveModeIfNew(mode)

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val existing = current.editingExpense
            if (existing != null) {
                val updated = existing.copy(
                    type = "CASH_OUT",
                    amount = amount,
                    category = category,
                    paymentMode = mode,
                    partyName = paidTo,
                    remarks = remarks,
                    referenceNo = refNo,
                    dateMillis = dateMillis
                )
                ledgerDao.updateTransaction(updated)
                _userMessage.emit("Expense updated successfully.")
            } else {
                val newExpense = LedgerTransactionEntity(
                    type = "CASH_OUT",
                    amount = amount,
                    category = category,
                    paymentMode = mode,
                    partyName = paidTo,
                    remarks = remarks,
                    referenceNo = refNo,
                    dateMillis = dateMillis
                )
                ledgerDao.insertTransaction(newExpense)
                _userMessage.emit("Expense of Rs. ${IndianNumberFormatter.formatIndianDouble(amount, 0)} recorded.")
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
                val party = if (exp.partyName.isNotBlank()) " | Paid to: ${exp.partyName}" else ""
                val rem = if (exp.remarks.isNotBlank()) " (${exp.remarks})" else ""
                appendLine("${i + 1}. [${dateFormat.format(Date(exp.dateMillis))}] ${exp.category}$party : Rs. ${IndianNumberFormatter.formatIndianDouble(exp.amount, 2)} [${exp.paymentMode}]$rem")
            }
            appendLine("======================================")
        }
    }
}

package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.LedgerTransactionDao
import com.example.data.db.LedgerTransactionEntity
import com.example.domain.ledger.LedgerPaymentUtils
import com.example.domain.ledger.LedgerSummary
import com.example.domain.ledger.LedgerType
import com.example.domain.util.IndianNumberFormatter
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val PREFS_NAME = "ledger_preferences"
private const val KEY_SAVED_CATEGORIES = "saved_categories_set"
private const val KEY_SAVED_MODES = "saved_modes_set"

val DEFAULT_CATEGORIES = listOf("Sales", "Noran Shah", "Noman Shah", "Home")
val DEFAULT_PAYMENT_MODES = listOf("Cash", "Bank Transfer")

data class LedgerUiState(
    val searchQuery: String = "",
    val selectedTypeFilter: String = "ALL",          // "ALL", "CASH_IN", "CASH_OUT"
    val selectedModeFilter: String = "ALL",          // "ALL", "CASH", "BANK", "OTHERS"
    val isAddSheetOpen: Boolean = false,
    val isHistoryPanelOpen: Boolean = false,
    val isDetailDialogOpen: Boolean = false,
    val selectedTransactionForDetail: LedgerTransactionEntity? = null,
    val editingTransaction: LedgerTransactionEntity? = null,
    // Custom Form Inputs
    val formType: LedgerType = LedgerType.CASH_IN,
    val formAmountText: String = "",
    val formCategory: String = "Sales",
    val formPaymentMode: String = "Cash",
    val formPartyName: String = "",
    val formRemarks: String = "",
    val formReferenceNo: String = "",
    val formDateMillis: Long = System.currentTimeMillis(),
    val isFormSubmitting: Boolean = false,
    // Computed Data
    val summary: LedgerSummary = LedgerSummary(),
    val filteredTransactions: List<LedgerTransactionEntity> = emptyList(),
    val allTransactions: List<LedgerTransactionEntity> = emptyList(),
    val distinctParties: List<String> = emptyList(),
    val availableCategories: List<String> = DEFAULT_CATEGORIES,
    val availablePaymentModes: List<String> = DEFAULT_PAYMENT_MODES
)

class LedgerViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val ledgerDao: LedgerTransactionDao = AppDatabase.getDatabase(application).ledgerTransactionDao()

    private val _uiState = MutableStateFlow(LedgerUiState())
    val uiState: StateFlow<LedgerUiState> = _uiState

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    init {
        loadPersistedLists()

        viewModelScope.launch {
            ledgerDao.getAllTransactions().collect { allList ->
                _uiState.update { current ->
                    val summary = computeSummary(allList)
                    val filtered = filterTransactions(
                        allList,
                        current.searchQuery,
                        current.selectedTypeFilter,
                        current.selectedModeFilter
                    )
                    val distinctParties = allList.map { it.partyName.trim() }
                        .filter { it.isNotEmpty() }
                        .distinct()
                        .take(20)

                    // Merge DB categories and payment modes with current available
                    val dbCategories = allList.map { it.category.trim() }.filter { it.isNotEmpty() }
                    val dbModes = allList.map { it.paymentMode.trim() }.filter { it.isNotEmpty() }

                    val mergedCategories = (current.availableCategories + dbCategories).distinct()
                    val mergedModes = (current.availablePaymentModes + dbModes).distinct()

                    current.copy(
                        allTransactions = allList,
                        summary = summary,
                        filteredTransactions = filtered,
                        distinctParties = distinctParties,
                        availableCategories = mergedCategories,
                        availablePaymentModes = mergedModes
                    )
                }
            }
        }
    }

    private fun loadPersistedLists() {
        val savedCatSet = prefs.getStringSet(KEY_SAVED_CATEGORIES, null)
        val loadedCategories = if (savedCatSet != null && savedCatSet.isNotEmpty()) {
            (DEFAULT_CATEGORIES + savedCatSet).distinct()
        } else {
            DEFAULT_CATEGORIES
        }

        val savedModeSet = prefs.getStringSet(KEY_SAVED_MODES, null)
        val loadedModes = if (savedModeSet != null && savedModeSet.isNotEmpty()) {
            (DEFAULT_PAYMENT_MODES + savedModeSet).distinct()
        } else {
            DEFAULT_PAYMENT_MODES
        }

        _uiState.update {
            it.copy(
                availableCategories = loadedCategories,
                availablePaymentModes = loadedModes
            )
        }
    }

    private fun persistCategories(list: List<String>) {
        prefs.edit().putStringSet(KEY_SAVED_CATEGORIES, list.toSet()).apply()
    }

    private fun persistPaymentModes(list: List<String>) {
        prefs.edit().putStringSet(KEY_SAVED_MODES, list.toSet()).apply()
    }

    private fun autoSaveCategoryIfNew(category: String) {
        val trimmed = category.trim()
        if (trimmed.isEmpty()) return
        val currentCategories = _uiState.value.availableCategories
        if (currentCategories.none { it.equals(trimmed, ignoreCase = true) }) {
            val updated = currentCategories + trimmed
            persistCategories(updated)
            _uiState.update { it.copy(availableCategories = updated) }
        }
    }

    private fun autoSavePaymentModeIfNew(mode: String) {
        val trimmed = mode.trim()
        if (trimmed.isEmpty()) return
        val currentModes = _uiState.value.availablePaymentModes
        if (currentModes.none { it.equals(trimmed, ignoreCase = true) }) {
            val updated = currentModes + trimmed
            persistPaymentModes(updated)
            _uiState.update { it.copy(availablePaymentModes = updated) }
        }
    }

    private fun recomputeState() {
        val current = _uiState.value
        val allList = current.allTransactions
        val summary = computeSummary(allList)
        val filtered = filterTransactions(
            allList,
            current.searchQuery,
            current.selectedTypeFilter,
            current.selectedModeFilter
        )

        _uiState.update {
            it.copy(
                summary = summary,
                filteredTransactions = filtered
            )
        }
    }

    private fun computeSummary(allList: List<LedgerTransactionEntity>): LedgerSummary {
        var totalCashIn = 0.0
        var totalCashOut = 0.0
        var cashInCount = 0
        var cashOutCount = 0

        var cashBalance = 0.0
        var bankBalance = 0.0
        var otherModesBalance = 0.0
        var otherModesCount = 0

        val byMode = mutableMapOf<String, Double>()
        val byCategory = mutableMapOf<String, Double>()

        for (tx in allList) {
            val isExpense = tx.type == "CASH_OUT"
            if (isExpense) {
                totalCashOut += tx.amount
                cashOutCount++
            } else {
                totalCashIn += tx.amount
                cashInCount++
            }

            val netDelta = if (isExpense) -tx.amount else tx.amount
            val mode = tx.paymentMode.trim().ifEmpty { "Cash" }

            // Group by Cash, Bank Transfer, or Others
            if (LedgerPaymentUtils.isCashMode(mode)) {
                cashBalance += netDelta
            } else if (LedgerPaymentUtils.isBankMode(mode)) {
                bankBalance += netDelta
            } else {
                otherModesBalance += netDelta
                otherModesCount++
            }

            byMode[mode] = (byMode[mode] ?: 0.0) + netDelta

            // By Category
            val cat = tx.category.trim().ifEmpty { "Sales" }
            byCategory[cat] = (byCategory[cat] ?: 0.0) + tx.amount
        }

        val totalCashInHand = totalCashIn - totalCashOut

        return LedgerSummary(
            totalCashIn = totalCashIn,
            totalCashOut = totalCashOut,
            netCashFlow = totalCashInHand,
            totalCashInHand = totalCashInHand,
            totalOpeningBalance = 0.0,
            transactionCount = allList.size,
            cashInCount = cashInCount,
            cashOutCount = cashOutCount,
            cashBalance = cashBalance,
            bankBalance = bankBalance,
            otherModesBalance = otherModesBalance,
            otherModesCount = otherModesCount,
            cashByMode = byMode,
            categoryBreakdown = byCategory
        )
    }

    private fun filterTransactions(
        allList: List<LedgerTransactionEntity>,
        searchQuery: String,
        typeFilter: String,
        modeFilter: String
    ): List<LedgerTransactionEntity> {
        val query = searchQuery.trim()
        return allList.filter { tx ->
            val matchSearch = query.isBlank() ||
                    tx.partyName.contains(query, ignoreCase = true) ||
                    tx.remarks.contains(query, ignoreCase = true) ||
                    tx.referenceNo.contains(query, ignoreCase = true) ||
                    tx.category.contains(query, ignoreCase = true) ||
                    tx.paymentMode.contains(query, ignoreCase = true) ||
                    tx.amount.toString().contains(query)

            val matchType = when (typeFilter) {
                "ALL" -> true
                "CASH_IN" -> tx.type != "CASH_OUT"
                "CASH_OUT" -> tx.type == "CASH_OUT"
                else -> true
            }

            val matchMode = when (modeFilter) {
                "ALL" -> true
                "CASH" -> LedgerPaymentUtils.isCashMode(tx.paymentMode)
                "BANK" -> LedgerPaymentUtils.isBankMode(tx.paymentMode)
                "OTHERS" -> !LedgerPaymentUtils.isDefaultMode(tx.paymentMode)
                else -> tx.paymentMode.equals(modeFilter, ignoreCase = true)
            }

            matchSearch && matchType && matchMode
        }
    }

    // --- History Search & Filter Controls ---
    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        recomputeState()
    }

    fun setTypeFilter(filter: String) {
        _uiState.update { it.copy(selectedTypeFilter = filter) }
        recomputeState()
    }

    fun setModeFilter(filter: String) {
        _uiState.update { it.copy(selectedModeFilter = filter) }
        recomputeState()
    }

    fun openHistoryForMode(modeFilter: String) {
        _uiState.update {
            it.copy(
                isHistoryPanelOpen = true,
                selectedModeFilter = modeFilter,
                selectedTypeFilter = "ALL",
                searchQuery = ""
            )
        }
        recomputeState()
    }

    // --- Form Inputs ---
    fun setFormCategory(category: String) {
        _uiState.update { it.copy(formCategory = category) }
    }

    fun setFormPaymentMode(mode: String) {
        _uiState.update { it.copy(formPaymentMode = mode) }
    }

    // --- Add / Edit Transaction Sheet & Form ---
    fun openAddTransactionSheet(type: LedgerType = LedgerType.CASH_IN) {
        val defaultCategory = if (type == LedgerType.CASH_OUT) "Home" else "Sales"
        val defaultPaymentMode = "Cash"

        _uiState.update {
            it.copy(
                isAddSheetOpen = true,
                editingTransaction = null,
                formType = type,
                formAmountText = "",
                formCategory = defaultCategory,
                formPaymentMode = defaultPaymentMode,
                formPartyName = "",
                formRemarks = "",
                formReferenceNo = "",
                formDateMillis = System.currentTimeMillis()
            )
        }
    }

    fun openEditTransaction(tx: LedgerTransactionEntity) {
        val type = if (tx.type == "CASH_OUT") LedgerType.CASH_OUT else LedgerType.CASH_IN

        _uiState.update {
            it.copy(
                isAddSheetOpen = true,
                editingTransaction = tx,
                formType = type,
                formAmountText = if (tx.amount % 1.0 == 0.0) tx.amount.toLong().toString() else tx.amount.toString(),
                formCategory = tx.category.ifBlank { "Sales" },
                formPaymentMode = tx.paymentMode.ifBlank { "Cash" },
                formPartyName = tx.partyName,
                formRemarks = tx.remarks,
                formReferenceNo = tx.referenceNo,
                formDateMillis = tx.dateMillis
            )
        }
    }

    fun closeAddSheet() {
        _uiState.update {
            it.copy(
                isAddSheetOpen = false,
                editingTransaction = null
            )
        }
    }

    fun setFormType(type: LedgerType) {
        _uiState.update { it.copy(formType = type) }
    }

    fun setFormAmountText(amount: String) {
        _uiState.update { it.copy(formAmountText = amount) }
    }

    fun setFormPartyName(partyName: String) {
        _uiState.update { it.copy(formPartyName = partyName) }
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

    fun saveFormTransaction() {
        val current = _uiState.value
        val amount = current.formAmountText.toDoubleOrNull()
        if (amount == null || amount <= 0.0) {
            viewModelScope.launch { _userMessage.emit("Please enter a valid amount greater than 0.") }
            return
        }

        val typeStr = current.formType.name
        val category = current.formCategory.trim().ifBlank { if (current.formType == LedgerType.CASH_OUT) "Home" else "Sales" }
        val mode = current.formPaymentMode.trim().ifBlank { "Cash" }
        val party = current.formPartyName.trim()
        val remarks = current.formRemarks.trim()
        val refNo = current.formReferenceNo.trim()
        val dateMillis = current.formDateMillis

        // Auto-save new category and payment mode automatically without extra UI
        autoSaveCategoryIfNew(category)
        autoSavePaymentModeIfNew(mode)

        viewModelScope.launch {
            _uiState.update { it.copy(isFormSubmitting = true) }
            val existing = current.editingTransaction
            if (existing != null) {
                val updated = existing.copy(
                    type = typeStr,
                    amount = amount,
                    category = category,
                    paymentMode = mode,
                    partyName = party,
                    remarks = remarks,
                    referenceNo = refNo,
                    dateMillis = dateMillis
                )
                ledgerDao.updateTransaction(updated)
                _userMessage.emit("Transaction updated successfully.")
            } else {
                val newTx = LedgerTransactionEntity(
                    type = typeStr,
                    amount = amount,
                    category = category,
                    paymentMode = mode,
                    partyName = party,
                    remarks = remarks,
                    referenceNo = refNo,
                    dateMillis = dateMillis
                )
                ledgerDao.insertTransaction(newTx)
                _userMessage.emit("${current.formType.title} entry saved.")
            }
            _uiState.update {
                it.copy(
                    isAddSheetOpen = false,
                    isFormSubmitting = false,
                    editingTransaction = null
                )
            }
        }
    }

    fun deleteTransaction(tx: LedgerTransactionEntity) {
        viewModelScope.launch {
            ledgerDao.deleteTransaction(tx)
            _userMessage.emit("Transaction deleted.")
            if (_uiState.value.selectedTransactionForDetail?.id == tx.id) {
                _uiState.update { it.copy(isDetailDialogOpen = false, selectedTransactionForDetail = null) }
            }
        }
    }

    // --- History Panel & Dialogs ---
    fun setHistoryPanelOpen(open: Boolean) {
        _uiState.update { it.copy(isHistoryPanelOpen = open) }
    }

    fun openTransactionDetail(tx: LedgerTransactionEntity) {
        _uiState.update { it.copy(isDetailDialogOpen = true, selectedTransactionForDetail = tx) }
    }

    fun closeTransactionDetail() {
        _uiState.update { it.copy(isDetailDialogOpen = false, selectedTransactionForDetail = null) }
    }

    // --- Formatted Exports ---
    fun buildSingleTransactionReport(tx: LedgerTransactionEntity): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
        val typeLabel = if (tx.type == "CASH_OUT") "CASH OUT (EXPENSE)" else "CASH IN (INCOME)"
        return buildString {
            appendLine("=== CASHBOOK TRANSACTION VOUCHER ===")
            appendLine("Type: $typeLabel")
            appendLine("Amount: Rs. ${IndianNumberFormatter.formatIndianDouble(tx.amount, 2)}")
            appendLine("Category: ${tx.category}")
            appendLine("Payment Mode: ${tx.paymentMode}")
            if (tx.partyName.isNotBlank()) appendLine("Party / Name: ${tx.partyName}")
            if (tx.referenceNo.isNotBlank()) appendLine("Ref / Bill No: ${tx.referenceNo}")
            if (tx.remarks.isNotBlank()) appendLine("Remarks: ${tx.remarks}")
            appendLine("Date: ${dateFormat.format(Date(tx.dateMillis))}")
            appendLine("====================================")
        }
    }

    fun buildPeriodAuditSummary(): String {
        val state = _uiState.value
        val summary = state.summary
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)

        return buildString {
            appendLine("=== DAILY LEDGER & CASHBOOK SUMMARY ===")
            appendLine("Total Entries: ${summary.transactionCount}")
            appendLine("--------------------------------------")
            appendLine("TOTAL CASH IN HAND: ${summary.formattedCashInHand}")
            appendLine("Cash Mode Balance:  ${summary.formattedCashBalance}")
            appendLine("Bank Mode Balance:  ${summary.formattedBankBalance}")
            appendLine("Others Balance:     ${summary.formattedOthersBalance} (${summary.otherModesCount} txns)")
            appendLine("--------------------------------------")
            appendLine("TOTAL CASH IN:      ${summary.formattedCashIn} (${summary.cashInCount} txns)")
            appendLine("TOTAL CASH OUT:     ${summary.formattedCashOut} (${summary.cashOutCount} txns)")
            appendLine("NET BALANCE:        ${summary.formattedNetCashFlow}")
            appendLine("--------------------------------------")
            if (summary.cashByMode.isNotEmpty()) {
                appendLine("PAYMENT CHANNEL BREAKDOWN:")
                summary.cashByMode.forEach { (mode, net) ->
                    val prefix = if (net >= 0) "+Rs. " else "-Rs. "
                    appendLine("• $mode: $prefix${IndianNumberFormatter.formatIndianDouble(kotlin.math.abs(net), 2)}")
                }
                appendLine("--------------------------------------")
            }
            if (summary.categoryBreakdown.isNotEmpty()) {
                appendLine("CATEGORY BREAKDOWN:")
                summary.categoryBreakdown.forEach { (cat, amt) ->
                    appendLine("• $cat: Rs. ${IndianNumberFormatter.formatIndianDouble(amt, 2)}")
                }
                appendLine("--------------------------------------")
            }
            appendLine("TRANSACTION LOG (${state.filteredTransactions.size} records):")
            state.filteredTransactions.forEachIndexed { i, tx ->
                val sign = if (tx.type == "CASH_OUT") "-" else "+"
                val party = if (tx.partyName.isNotBlank()) " | ${tx.partyName}" else ""
                appendLine("${i + 1}. [${dateFormat.format(Date(tx.dateMillis))}] ${tx.category}$party : $sign Rs. ${IndianNumberFormatter.formatIndianDouble(tx.amount, 2)} (${tx.paymentMode})")
            }
            appendLine("======================================")
        }
    }
}

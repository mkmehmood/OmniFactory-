package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.LedgerTransactionDao
import com.example.data.db.LedgerTransactionEntity
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

data class LedgerUiState(
    val searchQuery: String = "",
    val selectedTypeFilter: String = "ALL",          // "ALL", "CASH_IN", "CASH_OUT", "OPENING_BALANCE"
    val selectedCategoryFilter: String = "ALL",
    val selectedPaymentModeFilter: String = "ALL",
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
    val distinctCategories: List<String> = emptyList(),
    val distinctPaymentModes: List<String> = emptyList()
)

class LedgerViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val ledgerDao: LedgerTransactionDao = AppDatabase.getDatabase(application).ledgerTransactionDao()

    private val _uiState = MutableStateFlow(LedgerUiState())
    val uiState: StateFlow<LedgerUiState> = _uiState

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    init {
        viewModelScope.launch {
            ledgerDao.getAllTransactions().collect { allList ->
                _uiState.update { current ->
                    val summary = computeSummary(allList)
                    val filtered = filterTransactions(
                        allList,
                        current.searchQuery,
                        current.selectedTypeFilter,
                        current.selectedCategoryFilter,
                        current.selectedPaymentModeFilter
                    )
                    val distinctParties = allList.map { it.partyName.trim() }
                        .filter { it.isNotEmpty() }
                        .distinct()
                        .take(20)
                    val distinctCategories = allList.map { it.category.trim() }
                        .filter { it.isNotEmpty() }
                        .distinct()
                        .take(20)
                    val distinctPaymentModes = allList.map { it.paymentMode.trim() }
                        .filter { it.isNotEmpty() }
                        .distinct()
                        .take(15)

                    current.copy(
                        allTransactions = allList,
                        summary = summary,
                        filteredTransactions = filtered,
                        distinctParties = distinctParties,
                        distinctCategories = distinctCategories,
                        distinctPaymentModes = distinctPaymentModes
                    )
                }
            }
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
            current.selectedCategoryFilter,
            current.selectedPaymentModeFilter
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
        var totalOpening = 0.0
        var cashInCount = 0
        var cashOutCount = 0
        val byMode = mutableMapOf<String, Double>()
        val byCategory = mutableMapOf<String, Double>()

        for (tx in allList) {
            when (tx.type) {
                "CASH_IN" -> {
                    totalCashIn += tx.amount
                    cashInCount++
                }
                "CASH_OUT" -> {
                    totalCashOut += tx.amount
                    cashOutCount++
                }
                "OPENING_BALANCE" -> {
                    totalOpening += tx.amount
                    totalCashIn += tx.amount
                    cashInCount++
                }
            }
            // By Mode
            val mode = tx.paymentMode.trim().ifEmpty { "Cash" }
            val netDelta = if (tx.type == "CASH_OUT") -tx.amount else tx.amount
            byMode[mode] = (byMode[mode] ?: 0.0) + netDelta

            // By Category
            val cat = tx.category.trim().ifEmpty { "General" }
            byCategory[cat] = (byCategory[cat] ?: 0.0) + tx.amount
        }

        val totalCashInHand = totalCashIn - totalCashOut

        return LedgerSummary(
            totalCashIn = totalCashIn,
            totalCashOut = totalCashOut,
            netCashFlow = totalCashInHand,
            totalCashInHand = totalCashInHand,
            totalOpeningBalance = totalOpening,
            transactionCount = allList.size,
            cashInCount = cashInCount,
            cashOutCount = cashOutCount,
            cashByMode = byMode,
            categoryBreakdown = byCategory
        )
    }

    private fun filterTransactions(
        allList: List<LedgerTransactionEntity>,
        searchQuery: String,
        typeFilter: String,
        categoryFilter: String,
        paymentModeFilter: String
    ): List<LedgerTransactionEntity> {
        return allList.filter { tx ->
            val matchSearch = searchQuery.isBlank() ||
                    tx.partyName.contains(searchQuery, ignoreCase = true) ||
                    tx.remarks.contains(searchQuery, ignoreCase = true) ||
                    tx.referenceNo.contains(searchQuery, ignoreCase = true) ||
                    tx.category.contains(searchQuery, ignoreCase = true) ||
                    tx.paymentMode.contains(searchQuery, ignoreCase = true) ||
                    tx.amount.toString().contains(searchQuery)

            val matchType = typeFilter == "ALL" || tx.type.equals(typeFilter, ignoreCase = true)
            val matchCategory = categoryFilter == "ALL" || tx.category.equals(categoryFilter, ignoreCase = true)
            val matchMode = paymentModeFilter == "ALL" || tx.paymentMode.equals(paymentModeFilter, ignoreCase = true)

            matchSearch && matchType && matchCategory && matchMode
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

    fun setCategoryFilter(category: String) {
        _uiState.update { it.copy(selectedCategoryFilter = category) }
        recomputeState()
    }

    fun setPaymentModeFilter(mode: String) {
        _uiState.update { it.copy(selectedPaymentModeFilter = mode) }
        recomputeState()
    }

    // --- Add / Edit Transaction Sheet & Form ---
    fun openAddTransactionSheet(type: LedgerType = LedgerType.CASH_IN) {
        val defaultCategory = when (type) {
            LedgerType.CASH_OUT -> "Expense"
            LedgerType.OPENING_BALANCE -> "Opening Balance"
            else -> "Sales"
        }

        _uiState.update {
            it.copy(
                isAddSheetOpen = true,
                editingTransaction = null,
                formType = type,
                formAmountText = "",
                formCategory = defaultCategory,
                formPaymentMode = "Cash",
                formPartyName = "",
                formRemarks = "",
                formReferenceNo = "",
                formDateMillis = System.currentTimeMillis()
            )
        }
    }

    fun openEditTransaction(tx: LedgerTransactionEntity) {
        val type = when (tx.type) {
            "CASH_OUT" -> LedgerType.CASH_OUT
            "OPENING_BALANCE" -> LedgerType.OPENING_BALANCE
            else -> LedgerType.CASH_IN
        }

        _uiState.update {
            it.copy(
                isAddSheetOpen = true,
                editingTransaction = tx,
                formType = type,
                formAmountText = if (tx.amount % 1.0 == 0.0) tx.amount.toLong().toString() else tx.amount.toString(),
                formCategory = tx.category,
                formPaymentMode = tx.paymentMode,
                formPartyName = tx.partyName,
                formRemarks = tx.remarks,
                formReferenceNo = tx.referenceNo,
                formDateMillis = tx.dateMillis
            )
        }
    }

    fun closeAddSheet() {
        _uiState.update { it.copy(isAddSheetOpen = false, editingTransaction = null) }
    }

    fun setFormType(type: LedgerType) {
        val currentCategory = _uiState.value.formCategory
        val defaultCategory = when (type) {
            LedgerType.CASH_OUT -> if (currentCategory == "Sales" || currentCategory.isBlank()) "Expense" else currentCategory
            LedgerType.OPENING_BALANCE -> "Opening Balance"
            else -> if (currentCategory == "Expense" || currentCategory.isBlank()) "Sales" else currentCategory
        }

        _uiState.update { it.copy(formType = type, formCategory = defaultCategory) }
    }

    fun setFormAmountText(amount: String) {
        _uiState.update { it.copy(formAmountText = amount) }
    }

    fun appendPresetAmount(preset: Double) {
        val current = _uiState.value.formAmountText.toDoubleOrNull() ?: 0.0
        val sum = current + preset
        val text = if (sum % 1.0 == 0.0) sum.toLong().toString() else sum.toString()
        _uiState.update { it.copy(formAmountText = text) }
    }

    fun setFormCategory(category: String) {
        _uiState.update { it.copy(formCategory = category) }
    }

    fun setFormPaymentMode(mode: String) {
        _uiState.update { it.copy(formPaymentMode = mode) }
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
            viewModelScope.launch { _userMessage.emit("Please enter a valid transaction amount greater than 0.") }
            return
        }

        val typeStr = current.formType.name
        val category = current.formCategory.trim().ifBlank { "General" }
        val mode = current.formPaymentMode.trim().ifBlank { "Cash" }
        val party = current.formPartyName.trim()
        val remarks = current.formRemarks.trim()
        val refNo = current.formReferenceNo.trim()
        val dateMillis = current.formDateMillis

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
            _uiState.update { it.copy(isAddSheetOpen = false, isFormSubmitting = false, editingTransaction = null) }
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
        return buildString {
            appendLine("=== CASHBOOK TRANSACTION VOUCHER ===")
            appendLine("Type: ${if (tx.type == "CASH_IN") "CASH IN (INCOME)" else if (tx.type == "CASH_OUT") "CASH OUT (EXPENSE)" else "OPENING BALANCE"}")
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

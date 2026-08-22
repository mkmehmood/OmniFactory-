package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.SalesTransactionEntity
import com.example.data.repository.SalesRepository
import com.example.domain.sales.ReconciliationStatus
import com.example.domain.sales.SalesReconciliationCalculator
import com.example.domain.sales.SalesReconciliationInput
import com.example.domain.sales.SalesReconciliationResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SalesUiState(
    val salesRep: String = "",
    val availableReps: List<String> = emptyList(),
    val dateMillis: Long = System.currentTimeMillis(),
    val totalSoldQtyText: String = "",
    val returnedQtyText: String = "",
    val expiredQtyText: String = "",
    val sharedQtyText: String = "",
    val creditSalesQtyText: String = "",
    val recoveredCreditText: String = "",
    val physicalCashText: String = "",
    val expenseText: String = "",
    val unitSalePriceText: String = "",
    val commissionPerUnitText: String = "",
    val commissionPaidText: String = "",
    val reconciliationResult: SalesReconciliationResult = SalesReconciliationCalculator.calculate(
        SalesReconciliationInput(
            salesRep = "",
            dateMillis = System.currentTimeMillis(),
            totalSoldQty = 0.0,
            returnedQty = 0.0,
            expiredQty = 0.0,
            sharedQty = 0.0,
            creditSalesQty = 0.0,
            recoveredCredit = 0.0,
            physicalCashInHand = 0.0,
            expense = 0.0,
            unitSalePrice = 0.0,
            commissionPerUnit = 0.0,
            commissionPaid = 0.0
        )
    ),
    val isSavedSuccess: Boolean = false,
    val showSavedHistorySheet: Boolean = false
)

class SalesViewModel(application: Application) : AndroidViewModel(application) {
    private val salesRepository: SalesRepository

    private val _uiState = MutableStateFlow(SalesUiState())
    val uiState: StateFlow<SalesUiState> = _uiState.asStateFlow()

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    val savedTransactions: StateFlow<List<SalesTransactionEntity>>

    init {
        val database = AppDatabase.getDatabase(application)
        salesRepository = SalesRepository(database.salesTransactionDao())
        savedTransactions = salesRepository.allTransactions
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        viewModelScope.launch {
            savedTransactions.collect { list ->
                val distinctReps = list.map { it.salesRep }.filter { it.isNotBlank() }.distinct()
                _uiState.update { current ->
                    val merged = (current.availableReps + distinctReps).distinct()
                    current.copy(
                        availableReps = merged,
                        salesRep = if (current.salesRep.isEmpty() && merged.isNotEmpty()) merged.first() else current.salesRep
                    )
                }
            }
        }
    }

    fun setSalesRep(rep: String) {
        _uiState.update { current ->
            val updated = current.copy(salesRep = rep)
            recalculate(updated)
        }
    }

    fun addCustomSalesRep(newRep: String) {
        val trimmed = newRep.trim()
        if (trimmed.isEmpty()) return
        _uiState.update { current ->
            val updatedList = if (!current.availableReps.contains(trimmed)) {
                current.availableReps + trimmed
            } else current.availableReps
            val updated = current.copy(salesRep = trimmed, availableReps = updatedList)
            recalculate(updated)
        }
    }

    fun setDateMillis(dateMillis: Long) {
        _uiState.update { current ->
            val updated = current.copy(dateMillis = dateMillis)
            recalculate(updated)
        }
    }

    fun setTotalSoldQty(text: String) {
        _uiState.update { current ->
            val updated = current.copy(totalSoldQtyText = text, isSavedSuccess = false)
            recalculate(updated)
        }
    }

    fun setReturnedQty(text: String) {
        _uiState.update { current ->
            val updated = current.copy(returnedQtyText = text, isSavedSuccess = false)
            recalculate(updated)
        }
    }

    fun setExpiredQty(text: String) {
        _uiState.update { current ->
            val updated = current.copy(expiredQtyText = text, isSavedSuccess = false)
            recalculate(updated)
        }
    }

    fun setSharedQty(text: String) {
        _uiState.update { current ->
            val updated = current.copy(sharedQtyText = text, isSavedSuccess = false)
            recalculate(updated)
        }
    }

    fun setCreditSalesQty(text: String) {
        _uiState.update { current ->
            val updated = current.copy(creditSalesQtyText = text, isSavedSuccess = false)
            recalculate(updated)
        }
    }

    fun setRecoveredCredit(text: String) {
        _uiState.update { current ->
            val updated = current.copy(recoveredCreditText = text, isSavedSuccess = false)
            recalculate(updated)
        }
    }

    fun setPhysicalCash(text: String) {
        _uiState.update { current ->
            val updated = current.copy(physicalCashText = text, isSavedSuccess = false)
            recalculate(updated)
        }
    }

    fun setExpense(text: String) {
        _uiState.update { current ->
            val updated = current.copy(expenseText = text, isSavedSuccess = false)
            recalculate(updated)
        }
    }

    fun setUnitSalePrice(text: String) {
        _uiState.update { current ->
            val updated = current.copy(unitSalePriceText = text, isSavedSuccess = false)
            recalculate(updated)
        }
    }

    fun setCommissionPerUnit(text: String) {
        _uiState.update { current ->
            val updated = current.copy(commissionPerUnitText = text, isSavedSuccess = false)
            recalculate(updated)
        }
    }

    fun setCommissionPaid(text: String) {
        _uiState.update { current ->
            val updated = current.copy(commissionPaidText = text, isSavedSuccess = false)
            recalculate(updated)
        }
    }

    fun setSavedHistorySheetOpen(isOpen: Boolean) {
        _uiState.update { it.copy(showSavedHistorySheet = isOpen) }
    }

    fun clearInputs() {
        _uiState.update { current ->
            val cleared = current.copy(
                totalSoldQtyText = "",
                returnedQtyText = "",
                expiredQtyText = "",
                sharedQtyText = "",
                creditSalesQtyText = "",
                recoveredCreditText = "",
                physicalCashText = "",
                expenseText = "",
                unitSalePriceText = "",
                commissionPerUnitText = "",
                commissionPaidText = "",
                isSavedSuccess = false
            )
            recalculate(cleared)
        }
    }

    fun saveTransaction() {
        val state = _uiState.value
        val repName = state.salesRep.trim().ifEmpty { "General Sales" }
        val result = state.reconciliationResult

        val entity = SalesTransactionEntity(
            salesRep = repName,
            dateMillis = state.dateMillis,
            totalSoldQty = state.totalSoldQtyText.parseCleanDouble(),
            returnedQty = state.returnedQtyText.parseCleanDouble(),
            expiredQty = state.expiredQtyText.parseCleanDouble(),
            sharedQty = state.sharedQtyText.parseCleanDouble(),
            creditSalesQty = state.creditSalesQtyText.parseCleanDouble(),
            recoveredCredit = state.recoveredCreditText.parseCleanDouble(),
            physicalCashInHand = state.physicalCashText.parseCleanDouble(),
            expense = state.expenseText.parseCleanDouble(),
            unitSalePrice = state.unitSalePriceText.parseCleanDouble(),
            commissionPerUnit = state.commissionPerUnitText.parseCleanDouble(),
            grossCommission = result.grossCommission,
            commissionPaid = state.commissionPaidText.parseCleanDouble(),
            totalCommission = result.totalCommission,
            netSoldUnits = result.netSoldUnits,
            cashQtyUnits = result.cashQtyUnits,
            expectedCash = result.expectedCash,
            reconciledCash = result.reconciledCash,
            discrepancy = result.discrepancy,
            status = result.status.name,
            timestamp = System.currentTimeMillis()
        )

        viewModelScope.launch {
            salesRepository.insertTransaction(entity)
            _uiState.update { it.copy(isSavedSuccess = true) }
            val statusDescription = when (result.status) {
                ReconciliationStatus.BALANCED -> "Balanced (₹ 0.00 discrepancy)"
                ReconciliationStatus.OVER -> "Surplus of ${result.formattedDiscrepancy}"
                ReconciliationStatus.SHORT -> "Deficit of ${result.formattedDiscrepancy}"
            }
            _userMessage.emit("Transaction saved for $repName! Status: $statusDescription")
        }
    }

    fun deleteTransaction(entity: SalesTransactionEntity) {
        viewModelScope.launch {
            salesRepository.deleteTransaction(entity)
            _userMessage.emit("Transaction deleted")
        }
    }

    fun loadTransactionIntoForm(entity: SalesTransactionEntity) {
        _uiState.update { current ->
            val loaded = current.copy(
                salesRep = entity.salesRep,
                dateMillis = entity.dateMillis,
                totalSoldQtyText = formatNumberInput(entity.totalSoldQty),
                returnedQtyText = formatNumberInput(entity.returnedQty),
                expiredQtyText = formatNumberInput(entity.expiredQty),
                sharedQtyText = formatNumberInput(entity.sharedQty),
                creditSalesQtyText = formatNumberInput(entity.creditSalesQty),
                recoveredCreditText = formatNumberInput(entity.recoveredCredit),
                physicalCashText = formatNumberInput(entity.physicalCashInHand),
                expenseText = formatNumberInput(entity.expense),
                unitSalePriceText = formatNumberInput(entity.unitSalePrice),
                commissionPerUnitText = formatNumberInput(entity.commissionPerUnit),
                commissionPaidText = formatNumberInput(entity.commissionPaid),
                showSavedHistorySheet = false,
                isSavedSuccess = false
            )
            recalculate(loaded)
        }
    }

    private fun recalculate(state: SalesUiState): SalesUiState {
        val input = SalesReconciliationInput(
            salesRep = state.salesRep,
            dateMillis = state.dateMillis,
            totalSoldQty = state.totalSoldQtyText.parseCleanDouble(),
            returnedQty = state.returnedQtyText.parseCleanDouble(),
            expiredQty = state.expiredQtyText.parseCleanDouble(),
            sharedQty = state.sharedQtyText.parseCleanDouble(),
            creditSalesQty = state.creditSalesQtyText.parseCleanDouble(),
            recoveredCredit = state.recoveredCreditText.parseCleanDouble(),
            physicalCashInHand = state.physicalCashText.parseCleanDouble(),
            expense = state.expenseText.parseCleanDouble(),
            unitSalePrice = state.unitSalePriceText.parseCleanDouble(),
            commissionPerUnit = state.commissionPerUnitText.parseCleanDouble(),
            commissionPaid = state.commissionPaidText.parseCleanDouble()
        )
        val result = SalesReconciliationCalculator.calculate(input)
        return state.copy(reconciliationResult = result)
    }

    private fun String.parseCleanDouble(): Double {
        val clean = this.replace(",", "").replace(" ", "").trim()
        return clean.toDoubleOrNull() ?: 0.0
    }

    private fun formatNumberInput(value: Double): String {
        return if (value == 0.0) {
            ""
        } else if (value % 1.0 == 0.0) {
            String.format(java.util.Locale.US, "%.0f", value)
        } else {
            String.format(java.util.Locale.US, "%.2f", value)
        }
    }
}

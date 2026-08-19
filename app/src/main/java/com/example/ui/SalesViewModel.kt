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
    val salesRep: String = "Alex Morgan",
    val availableReps: List<String> = listOf(
        "Alex Morgan",
        "Sarah Chen",
        "Marcus Vance",
        "Elena Rostova",
        "David Kim",
        "Jordan Lee"
    ),
    val dateMillis: Long = System.currentTimeMillis(),
    val totalSoldQtyText: String = "150",
    val returnedQtyText: String = "10",
    val expiredQtyText: String = "5",
    val creditSalesQtyText: String = "20",
    val recoveredCreditText: String = "150.00",
    val physicalCashText: String = "2975.00",
    val expenseText: String = "50.00",
    val unitSalePriceText: String = "25.00",
    val reconciliationResult: SalesReconciliationResult = SalesReconciliationCalculator.calculate(
        SalesReconciliationInput(
            salesRep = "Alex Morgan",
            dateMillis = System.currentTimeMillis(),
            totalSoldQty = 150.0,
            returnedQty = 10.0,
            expiredQty = 5.0,
            creditSalesQty = 20.0,
            recoveredCredit = 150.0,
            physicalCashInHand = 2975.0,
            expense = 50.0,
            unitSalePrice = 25.0
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

    fun setSavedHistorySheetOpen(isOpen: Boolean) {
        _uiState.update { it.copy(showSavedHistorySheet = isOpen) }
    }

    fun clearInputs() {
        _uiState.update { current ->
            val cleared = current.copy(
                totalSoldQtyText = "",
                returnedQtyText = "",
                expiredQtyText = "",
                creditSalesQtyText = "",
                recoveredCreditText = "",
                physicalCashText = "",
                expenseText = "",
                unitSalePriceText = "",
                isSavedSuccess = false
            )
            recalculate(cleared)
        }
    }

    fun populateSampleData() {
        _uiState.update { current ->
            val populated = current.copy(
                totalSoldQtyText = "150",
                returnedQtyText = "10",
                expiredQtyText = "5",
                creditSalesQtyText = "20",
                recoveredCreditText = "150.00",
                physicalCashText = "2975.00",
                expenseText = "50.00",
                unitSalePriceText = "25.00",
                isSavedSuccess = false
            )
            recalculate(populated)
        }
    }

    fun saveTransaction() {
        val state = _uiState.value
        val result = state.reconciliationResult

        val entity = SalesTransactionEntity(
            salesRep = state.salesRep,
            dateMillis = state.dateMillis,
            totalSoldQty = state.totalSoldQtyText.toDoubleOrNull() ?: 0.0,
            returnedQty = state.returnedQtyText.toDoubleOrNull() ?: 0.0,
            expiredQty = state.expiredQtyText.toDoubleOrNull() ?: 0.0,
            creditSalesQty = state.creditSalesQtyText.toDoubleOrNull() ?: 0.0,
            recoveredCredit = state.recoveredCreditText.toDoubleOrNull() ?: 0.0,
            physicalCashInHand = state.physicalCashText.toDoubleOrNull() ?: 0.0,
            expense = state.expenseText.toDoubleOrNull() ?: 0.0,
            unitSalePrice = state.unitSalePriceText.toDoubleOrNull() ?: 0.0,
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
                ReconciliationStatus.BALANCED -> "Balanced ($0.00 discrepancy)"
                ReconciliationStatus.OVER -> "Surplus of ${result.formattedDiscrepancy}"
                ReconciliationStatus.SHORT -> "Deficit of ${result.formattedDiscrepancy}"
            }
            _userMessage.emit("Transaction saved for ${state.salesRep}! Status: $statusDescription")
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
                creditSalesQtyText = formatNumberInput(entity.creditSalesQty),
                recoveredCreditText = formatNumberInput(entity.recoveredCredit),
                physicalCashText = formatNumberInput(entity.physicalCashInHand),
                expenseText = formatNumberInput(entity.expense),
                unitSalePriceText = formatNumberInput(entity.unitSalePrice),
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
            totalSoldQty = state.totalSoldQtyText.toDoubleOrNull() ?: 0.0,
            returnedQty = state.returnedQtyText.toDoubleOrNull() ?: 0.0,
            expiredQty = state.expiredQtyText.toDoubleOrNull() ?: 0.0,
            creditSalesQty = state.creditSalesQtyText.toDoubleOrNull() ?: 0.0,
            recoveredCredit = state.recoveredCreditText.toDoubleOrNull() ?: 0.0,
            physicalCashInHand = state.physicalCashText.toDoubleOrNull() ?: 0.0,
            expense = state.expenseText.toDoubleOrNull() ?: 0.0,
            unitSalePrice = state.unitSalePriceText.toDoubleOrNull() ?: 0.0
        )
        val result = SalesReconciliationCalculator.calculate(input)
        return state.copy(reconciliationResult = result)
    }

    private fun formatNumberInput(value: Double): String {
        return if (value % 1.0 == 0.0) {
            String.format(java.util.Locale.US, "%.0f", value)
        } else {
            String.format(java.util.Locale.US, "%.2f", value)
        }
    }
}

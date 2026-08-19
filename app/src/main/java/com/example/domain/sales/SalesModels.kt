package com.example.domain.sales

import java.util.Locale

enum class ReconciliationStatus {
    BALANCED,
    OVER,   // Surplus: Reconciled cash > Expected cash
    SHORT   // Deficit: Reconciled cash < Expected cash
}

data class SalesReconciliationInput(
    val salesRep: String = "Alex Morgan",
    val dateMillis: Long = System.currentTimeMillis(),
    val totalSoldQty: Double = 0.0,
    val returnedQty: Double = 0.0,
    val expiredQty: Double = 0.0,
    val creditSalesQty: Double = 0.0,
    val recoveredCredit: Double = 0.0,
    val physicalCashInHand: Double = 0.0,
    val expense: Double = 0.0,
    val unitSalePrice: Double = 0.0
)

data class SalesReconciliationResult(
    val netSoldUnits: Double,
    val cashQtyUnits: Double,
    val unitSalePrice: Double,
    val cashFromSales: Double,
    val recoveredCredit: Double,
    val expense: Double,
    val expectedCash: Double,
    val physicalCashInHand: Double,
    val reconciledCash: Double,
    val discrepancy: Double,
    val status: ReconciliationStatus,
    val formattedNetSold: String,
    val formattedCashQty: String,
    val formattedUnitPrice: String,
    val formattedExpectedCash: String,
    val formattedPhysicalCash: String,
    val formattedReconciledCash: String,
    val formattedDiscrepancy: String,
    val formattedExpense: String,
    val formattedRecoveredCredit: String,
    val formattedCashFromSales: String
)

object SalesReconciliationCalculator {

    fun calculate(input: SalesReconciliationInput): SalesReconciliationResult {
        val totalSold = maxOf(0.0, input.totalSoldQty)
        val returned = maxOf(0.0, input.returnedQty)
        val expired = maxOf(0.0, input.expiredQty)
        val creditSales = maxOf(0.0, input.creditSalesQty)
        val recoveredCredit = maxOf(0.0, input.recoveredCredit)
        val physicalCash = maxOf(0.0, input.physicalCashInHand)
        val expense = maxOf(0.0, input.expense)
        val unitPrice = maxOf(0.0, input.unitSalePrice)

        // NetSoldUnits = max(0, TotalSoldQty − ReturnedQty − ExpiredQty)
        val netSoldUnits = maxOf(0.0, totalSold - returned - expired)

        // CashQtyUnits = max(0, NetSoldUnits − CreditSalesQty)
        val cashQtyUnits = maxOf(0.0, netSoldUnits - creditSales)

        // Cash from sales = CashQtyUnits * UnitSalePrice
        val cashFromSales = cashQtyUnits * unitPrice

        // ExpectedCash = (CashQtyUnits × UnitSalePrice) + RecoveredCredit
        val expectedCash = cashFromSales + recoveredCredit

        // ReconciledCash = PhysicalCashInHand + Expense
        val reconciledCash = physicalCash + expense

        // Status / Discrepancy = ReconciledCash − ExpectedCash
        val discrepancy = reconciledCash - expectedCash

        val status = when {
            kotlin.math.abs(discrepancy) < 0.005 -> ReconciliationStatus.BALANCED
            discrepancy > 0.005 -> ReconciliationStatus.OVER
            else -> ReconciliationStatus.SHORT
        }

        return SalesReconciliationResult(
            netSoldUnits = netSoldUnits,
            cashQtyUnits = cashQtyUnits,
            unitSalePrice = unitPrice,
            cashFromSales = cashFromSales,
            recoveredCredit = recoveredCredit,
            expense = expense,
            expectedCash = expectedCash,
            physicalCashInHand = physicalCash,
            reconciledCash = reconciledCash,
            discrepancy = discrepancy,
            status = status,
            formattedNetSold = formatNumber(netSoldUnits),
            formattedCashQty = formatNumber(cashQtyUnits),
            formattedUnitPrice = formatCurrency(unitPrice),
            formattedExpectedCash = formatCurrency(expectedCash),
            formattedPhysicalCash = formatCurrency(physicalCash),
            formattedReconciledCash = formatCurrency(reconciledCash),
            formattedDiscrepancy = formatCurrencyWithSign(discrepancy),
            formattedExpense = formatCurrency(expense),
            formattedRecoveredCredit = formatCurrency(recoveredCredit),
            formattedCashFromSales = formatCurrency(cashFromSales)
        )
    }

    private fun formatNumber(value: Double): String {
        return if (value % 1.0 == 0.0) {
            String.format(Locale.US, "%,.0f", value)
        } else {
            String.format(Locale.US, "%,.2f", value)
        }
    }

    private fun formatCurrency(value: Double): String {
        return String.format(Locale.US, "PKR %,.2f", value)
    }

    private fun formatCurrencyWithSign(value: Double): String {
        return when {
            kotlin.math.abs(value) < 0.005 -> "PKR 0.00"
            value > 0 -> String.format(Locale.US, "+PKR %,.2f", value)
            else -> String.format(Locale.US, "-PKR %,.2f", kotlin.math.abs(value))
        }
    }
}

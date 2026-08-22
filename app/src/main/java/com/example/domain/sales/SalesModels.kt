package com.example.domain.sales

import com.example.domain.util.IndianNumberFormatter

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
    val sharedQty: Double = 0.0,
    val creditSalesQty: Double = 0.0,
    val recoveredCredit: Double = 0.0,
    val physicalCashInHand: Double = 0.0,
    val expense: Double = 0.0,
    val unitSalePrice: Double = 0.0,
    val commissionPerUnit: Double = 0.0,
    val commissionPaid: Double = 0.0
)

data class SalesReconciliationResult(
    val netSoldUnits: Double,
    val cashQtyUnits: Double,
    val unitSalePrice: Double,
    val commissionPerUnit: Double,
    val grossCommission: Double,
    val commissionPaid: Double,
    val commissionPayable: Double,
    val totalCommission: Double, // Same as commissionPayable for backwards compatibility
    val cashFromSales: Double,
    val recoveredCredit: Double,
    val expense: Double,
    val expectedCash: Double,
    val physicalCashInHand: Double,
    val reconciledCash: Double,
    val discrepancy: Double,
    val deficitAmount: Double,
    val status: ReconciliationStatus,
    val sharedQty: Double = 0.0,
    val formattedNetSold: String,
    val formattedCashQty: String,
    val formattedSharedQty: String,
    val formattedUnitPrice: String,
    val formattedCommissionPerUnit: String,
    val formattedGrossCommission: String,
    val formattedCommissionPaid: String,
    val formattedCommissionPayable: String,
    val formattedTotalCommission: String,
    val formattedExpectedCash: String,
    val formattedPhysicalCash: String,
    val formattedReconciledCash: String,
    val formattedDiscrepancy: String,
    val formattedDeficitAmount: String,
    val formattedExpense: String,
    val formattedRecoveredCredit: String,
    val formattedCashFromSales: String
)

object SalesReconciliationCalculator {

    fun calculate(input: SalesReconciliationInput): SalesReconciliationResult {
        val totalSold = maxOf(0.0, input.totalSoldQty)
        val returned = maxOf(0.0, input.returnedQty)
        val expired = maxOf(0.0, input.expiredQty)
        val shared = maxOf(0.0, input.sharedQty)
        val creditSales = maxOf(0.0, input.creditSalesQty)
        val recoveredCredit = maxOf(0.0, input.recoveredCredit)
        val physicalCash = maxOf(0.0, input.physicalCashInHand)
        val expense = maxOf(0.0, input.expense)
        val unitPrice = maxOf(0.0, input.unitSalePrice)
        val commissionPerUnit = maxOf(0.0, input.commissionPerUnit)
        val commissionPaid = maxOf(0.0, input.commissionPaid)

        // NetSoldUnits = max(0, TotalSoldQty − ReturnedQty − ExpiredQty − SharedQty)
        val netSoldUnits = maxOf(0.0, totalSold - returned - expired - shared)

        // CashQtyUnits = max(0, NetSoldUnits − CreditSalesQty)
        val cashQtyUnits = maxOf(0.0, netSoldUnits - creditSales)

        // Cash from product sales = CashQtyUnits * UnitSalePrice (Strictly quantity & prices)
        val cashFromSales = cashQtyUnits * unitPrice

        // Gross commission earned = NetSoldUnits * CommissionPerUnit
        val grossCommission = netSoldUnits * commissionPerUnit

        // Commission Payable (Net Commission to be paid) = max(0, Gross Commission - Commission Paid)
        val commissionPayable = maxOf(0.0, grossCommission - commissionPaid)

        // Expected Cash to receive from Sales Rep / Manager:
        // Relates ONLY to product quantity and prices (+ recovered credit - field expenses).
        // Commission does NOT affect or reduce expected cash.
        val expectedCash = cashFromSales + recoveredCredit - expense

        // Reconciled Cash accounted for (Physical Cash received)
        val reconciledCash = physicalCash

        // Status / Discrepancy = Physical Cash Received − Expected Cash
        val discrepancy = physicalCash - expectedCash
        val deficitAmount = if (discrepancy < -0.005) kotlin.math.abs(discrepancy) else 0.0

        val status = when {
            kotlin.math.abs(discrepancy) < 0.005 -> ReconciliationStatus.BALANCED
            discrepancy > 0.005 -> ReconciliationStatus.OVER
            else -> ReconciliationStatus.SHORT
        }

        return SalesReconciliationResult(
            netSoldUnits = netSoldUnits,
            cashQtyUnits = cashQtyUnits,
            unitSalePrice = unitPrice,
            commissionPerUnit = commissionPerUnit,
            grossCommission = grossCommission,
            commissionPaid = commissionPaid,
            commissionPayable = commissionPayable,
            totalCommission = commissionPayable,
            cashFromSales = cashFromSales,
            recoveredCredit = recoveredCredit,
            expense = expense,
            expectedCash = expectedCash,
            physicalCashInHand = physicalCash,
            reconciledCash = reconciledCash,
            discrepancy = discrepancy,
            deficitAmount = deficitAmount,
            status = status,
            sharedQty = shared,
            formattedNetSold = formatIndianNumber(netSoldUnits),
            formattedCashQty = formatIndianNumber(cashQtyUnits),
            formattedSharedQty = formatIndianNumber(shared),
            formattedUnitPrice = IndianNumberFormatter.formatIndianCurrency(unitPrice),
            formattedCommissionPerUnit = IndianNumberFormatter.formatIndianCurrency(commissionPerUnit),
            formattedGrossCommission = IndianNumberFormatter.formatIndianCurrency(grossCommission),
            formattedCommissionPaid = IndianNumberFormatter.formatIndianCurrency(commissionPaid),
            formattedCommissionPayable = IndianNumberFormatter.formatIndianCurrency(commissionPayable),
            formattedTotalCommission = IndianNumberFormatter.formatIndianCurrency(commissionPayable),
            formattedExpectedCash = IndianNumberFormatter.formatIndianCurrency(expectedCash),
            formattedPhysicalCash = IndianNumberFormatter.formatIndianCurrency(physicalCash),
            formattedReconciledCash = IndianNumberFormatter.formatIndianCurrency(reconciledCash),
            formattedDiscrepancy = IndianNumberFormatter.formatIndianCurrency(discrepancy, showSign = true),
            formattedDeficitAmount = IndianNumberFormatter.formatIndianCurrency(deficitAmount),
            formattedExpense = IndianNumberFormatter.formatIndianCurrency(expense),
            formattedRecoveredCredit = IndianNumberFormatter.formatIndianCurrency(recoveredCredit),
            formattedCashFromSales = IndianNumberFormatter.formatIndianCurrency(cashFromSales)
        )
    }

    private fun formatIndianNumber(value: Double): String {
        return if (value % 1.0 == 0.0) {
            IndianNumberFormatter.formatIndianDouble(value, 0)
        } else {
            IndianNumberFormatter.formatIndianDouble(value, 2)
        }
    }
}

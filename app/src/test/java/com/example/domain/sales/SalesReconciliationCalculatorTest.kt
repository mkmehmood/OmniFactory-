package com.example.domain.sales

import org.junit.Assert.assertEquals
import org.junit.Test

class SalesReconciliationCalculatorTest {

    @Test
    fun testBalancedReconciliation() {
        // Sold = 150, Returned = 10, Expired = 5 -> Net Sold = 135
        // Credit Sales = 20 -> Cash Qty = 115
        // Unit Price = PKR 25.00 -> Sales Cash = 115 * 25 = PKR 2875.00
        // Recovered Credit = PKR 150.00
        // Expected Cash = (115 * 25) + 150 = PKR 3025.00
        // Physical Cash = PKR 2975.00, Expense = PKR 50.00 -> Reconciled Cash = 2975 + 50 = PKR 3025.00
        // Status / Discrepancy = 3025 - 3025 = PKR 0.00 (BALANCED)
        val input = SalesReconciliationInput(
            salesRep = "Alex Morgan",
            dateMillis = 1000L,
            totalSoldQty = 150.0,
            returnedQty = 10.0,
            expiredQty = 5.0,
            creditSalesQty = 20.0,
            recoveredCredit = 150.0,
            physicalCashInHand = 2975.0,
            expense = 50.0,
            unitSalePrice = 25.0
        )

        val result = SalesReconciliationCalculator.calculate(input)

        assertEquals(135.0, result.netSoldUnits, 0.001)
        assertEquals(115.0, result.cashQtyUnits, 0.001)
        assertEquals(2875.0, result.cashFromSales, 0.001)
        assertEquals(3025.0, result.expectedCash, 0.001)
        assertEquals(3025.0, result.reconciledCash, 0.001)
        assertEquals(0.0, result.discrepancy, 0.001)
        assertEquals(ReconciliationStatus.BALANCED, result.status)
        assertEquals("135", result.formattedNetSold)
        assertEquals("115", result.formattedCashQty)
        assertEquals("PKR 25.00", result.formattedUnitPrice)
        assertEquals("PKR 3,025.00", result.formattedExpectedCash)
        assertEquals("PKR 3,025.00", result.formattedReconciledCash)
        assertEquals("PKR 0.00", result.formattedDiscrepancy)
    }

    @Test
    fun testSurplusReconciliation() {
        // Expected Cash = PKR 3025.00
        // Physical Cash = PKR 3050.00, Expense = PKR 50.00 -> Reconciled Cash = PKR 3100.00
        // Discrepancy = 3100 - 3025 = +PKR 75.00 (OVER)
        val input = SalesReconciliationInput(
            salesRep = "Sarah Chen",
            dateMillis = 1000L,
            totalSoldQty = 150.0,
            returnedQty = 10.0,
            expiredQty = 5.0,
            creditSalesQty = 20.0,
            recoveredCredit = 150.0,
            physicalCashInHand = 3050.0,
            expense = 50.0,
            unitSalePrice = 25.0
        )

        val result = SalesReconciliationCalculator.calculate(input)

        assertEquals(3025.0, result.expectedCash, 0.001)
        assertEquals(3100.0, result.reconciledCash, 0.001)
        assertEquals(75.0, result.discrepancy, 0.001)
        assertEquals(ReconciliationStatus.OVER, result.status)
        assertEquals("+PKR 75.00", result.formattedDiscrepancy)
    }

    @Test
    fun testDeficitReconciliation() {
        // Expected Cash = PKR 3025.00
        // Physical Cash = PKR 2850.00, Expense = PKR 50.00 -> Reconciled Cash = PKR 2900.00
        // Discrepancy = 2900 - 3025 = -PKR 125.00 (SHORT)
        val input = SalesReconciliationInput(
            salesRep = "Marcus Vance",
            dateMillis = 1000L,
            totalSoldQty = 150.0,
            returnedQty = 10.0,
            expiredQty = 5.0,
            creditSalesQty = 20.0,
            recoveredCredit = 150.0,
            physicalCashInHand = 2850.0,
            expense = 50.0,
            unitSalePrice = 25.0
        )

        val result = SalesReconciliationCalculator.calculate(input)

        assertEquals(3025.0, result.expectedCash, 0.001)
        assertEquals(2900.0, result.reconciledCash, 0.001)
        assertEquals(-125.0, result.discrepancy, 0.001)
        assertEquals(ReconciliationStatus.SHORT, result.status)
        assertEquals("-PKR 125.00", result.formattedDiscrepancy)
    }

    @Test
    fun testZeroAndNegativeClamping() {
        val input = SalesReconciliationInput(
            salesRep = "David Kim",
            totalSoldQty = 10.0,
            returnedQty = 20.0, // Returned > Sold -> Net Sold clamps to 0
            expiredQty = 5.0,
            creditSalesQty = 5.0,
            recoveredCredit = 0.0,
            physicalCashInHand = 0.0,
            expense = 0.0,
            unitSalePrice = 10.0
        )

        val result = SalesReconciliationCalculator.calculate(input)

        assertEquals(0.0, result.netSoldUnits, 0.001)
        assertEquals(0.0, result.cashQtyUnits, 0.001)
        assertEquals(0.0, result.expectedCash, 0.001)
        assertEquals(0.0, result.reconciledCash, 0.001)
        assertEquals(0.0, result.discrepancy, 0.001)
        assertEquals(ReconciliationStatus.BALANCED, result.status)
    }
}

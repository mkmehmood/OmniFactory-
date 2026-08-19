package com.example.domain.sales

import org.junit.Assert.assertEquals
import org.junit.Test

class SalesReconciliationCalculatorTest {

    @Test
    fun testBalancedReconciliation() {
        // Sold = 150, Returned = 10, Expired = 5 -> Net Sold = 135
        // Credit Sales = 20 -> Cash Qty = 115
        // Unit Price = $25.00 -> Sales Cash = 115 * 25 = $2875.00
        // Recovered Credit = $150.00 -> Total Gross Cash = $2875 + $150 = $3025.00
        // Expense = $50.00 -> Expected Cash = $3025 - $50 = $2975.00
        // Physical Cash = $2975.00 -> Discrepancy = $0.00 (Balanced)
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
        assertEquals(2975.0, result.expectedCash, 0.001)
        assertEquals(0.0, result.discrepancy, 0.001)
        assertEquals(ReconciliationStatus.BALANCED, result.status)
        assertEquals("135", result.formattedNetSold)
        assertEquals("115", result.formattedCashQty)
        assertEquals("$25.00", result.formattedUnitPrice)
        assertEquals("$2,975.00", result.formattedExpectedCash)
        assertEquals("$0.00", result.formattedDiscrepancy)
    }

    @Test
    fun testSurplusReconciliation() {
        // Expected Cash = $2975.00, Physical Cash = $3050.00 -> Discrepancy = +$75.00 (OVER)
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

        assertEquals(2975.0, result.expectedCash, 0.001)
        assertEquals(75.0, result.discrepancy, 0.001)
        assertEquals(ReconciliationStatus.OVER, result.status)
        assertEquals("+$75.00", result.formattedDiscrepancy)
    }

    @Test
    fun testDeficitReconciliation() {
        // Expected Cash = $2975.00, Physical Cash = $2850.00 -> Discrepancy = -$125.00 (SHORT)
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

        assertEquals(2975.0, result.expectedCash, 0.001)
        assertEquals(-125.0, result.discrepancy, 0.001)
        assertEquals(ReconciliationStatus.SHORT, result.status)
        assertEquals("-$125.00", result.formattedDiscrepancy)
    }

    @Test
    fun testZeroAndNegativeClamping() {
        val input = SalesReconciliationInput(
            salesRep = "David Kim",
            totalSoldQty = 10.0,
            returnedQty = 20.0, // Returned > Sold -> Net Sold should clamp to 0
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
        assertEquals(0.0, result.discrepancy, 0.001)
        assertEquals(ReconciliationStatus.BALANCED, result.status)
    }
}

package com.example.domain.sales

import org.junit.Assert.assertEquals
import org.junit.Test

class SalesReconciliationCalculatorTest {

    @Test
    fun testBalancedReconciliationWithCommission() {
        // Sold = 150, Returned = 10, Expired = 5 -> Net Sold = 135
        // Credit Sales = 20 -> Cash Qty = 115
        // Unit Price = Rs. 25.00 -> Sales Cash = 115 * 25 = Rs. 2,875.00
        // Commission Per Unit = Rs. 2.50 -> Total Commission = 135 * 2.50 = Rs. 337.50
        // Recovered Credit = Rs. 150.00
        // Expected Cash = (115 * 25) + 150 = Rs. 3,025.00
        // Physical Cash = Rs. 2,975.00, Expense = Rs. 50.00 -> Reconciled Cash = 2975 + 50 = Rs. 3,025.00
        // Status / Discrepancy = 3025 - 3025 = Rs. 0.00 (BALANCED)
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
            unitSalePrice = 25.0,
            commissionPerUnit = 2.5
        )

        val result = SalesReconciliationCalculator.calculate(input)

        assertEquals(135.0, result.netSoldUnits, 0.001)
        assertEquals(115.0, result.cashQtyUnits, 0.001)
        assertEquals(337.5, result.totalCommission, 0.001)
        assertEquals(2875.0, result.cashFromSales, 0.001)
        assertEquals(3025.0, result.expectedCash, 0.001)
        assertEquals(3025.0, result.reconciledCash, 0.001)
        assertEquals(0.0, result.discrepancy, 0.001)
        assertEquals(ReconciliationStatus.BALANCED, result.status)
        assertEquals("135", result.formattedNetSold)
        assertEquals("115", result.formattedCashQty)
        assertEquals("Rs. 25.00", result.formattedUnitPrice)
        assertEquals("Rs. 2.50", result.formattedCommissionPerUnit)
        assertEquals("Rs. 337.50", result.formattedTotalCommission)
        assertEquals("Rs. 3,025.00", result.formattedExpectedCash)
        assertEquals("Rs. 3,025.00", result.formattedReconciledCash)
        assertEquals("Rs. 0.00", result.formattedDiscrepancy)
    }

    @Test
    fun testSurplusReconciliation() {
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
            unitSalePrice = 25.0,
            commissionPerUnit = 1.0
        )

        val result = SalesReconciliationCalculator.calculate(input)

        assertEquals(3025.0, result.expectedCash, 0.001)
        assertEquals(3100.0, result.reconciledCash, 0.001)
        assertEquals(75.0, result.discrepancy, 0.001)
        assertEquals(ReconciliationStatus.OVER, result.status)
        assertEquals("+Rs. 75.00", result.formattedDiscrepancy)
    }

    @Test
    fun testDeficitReconciliation() {
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
            unitSalePrice = 25.0,
            commissionPerUnit = 0.0
        )

        val result = SalesReconciliationCalculator.calculate(input)

        assertEquals(3025.0, result.expectedCash, 0.001)
        assertEquals(2900.0, result.reconciledCash, 0.001)
        assertEquals(-125.0, result.discrepancy, 0.001)
        assertEquals(ReconciliationStatus.SHORT, result.status)
        assertEquals("-Rs. 125.00", result.formattedDiscrepancy)
    }

    @Test
    fun testLargeIndianCurrencyFormat() {
        val input = SalesReconciliationInput(
            totalSoldQty = 1050345.0,
            returnedQty = 0.0,
            expiredQty = 0.0,
            creditSalesQty = 0.0,
            recoveredCredit = 0.0,
            physicalCashInHand = 10503450.0,
            expense = 0.0,
            unitSalePrice = 10.0,
            commissionPerUnit = 1.5
        )

        val result = SalesReconciliationCalculator.calculate(input)
        assertEquals("10,50,345", result.formattedNetSold)
        assertEquals("Rs. 1,05,03,450.00", result.formattedExpectedCash)
        assertEquals("Rs. 15,75,517.50", result.formattedTotalCommission)
    }
}

package com.example.domain.sales

import org.junit.Assert.assertEquals
import org.junit.Test

class SalesReconciliationCalculatorTest {

    @Test
    fun testBalancedReconciliationWithCommissionAndDeductions() {
        // Sold = 150, Returned = 10, Expired = 5 -> Net Sold = 135
        // Credit Sales = 20 -> Cash Qty = 115
        // Unit Price = Rs. 25.00 -> Product Cash Sales = 115 * 25 = Rs. 2,875.00
        // Commission Per Unit = Rs. 2.50 -> Gross Commission = 135 * 2.50 = Rs. 337.50
        // Commission Paid = Rs. 100.00 -> Commission Payable = Rs. 237.50
        // Recovered Credit = Rs. 150.00
        // Field Expense = Rs. 50.00
        // Expected Cash to receive from rep = 2875 + 150 - 50 = Rs. 2,975.00 (Commission does NOT deduct from expected cash)
        // Physical Cash handed over = Rs. 2,975.00
        // Discrepancy = 2975.00 - 2975.00 = Rs. 0.00 (BALANCED)
        val input = SalesReconciliationInput(
            salesRep = "Alex Morgan",
            dateMillis = 1000L,
            totalSoldQty = 150.0,
            returnedQty = 10.0,
            expiredQty = 5.0,
            creditSalesQty = 20.0,
            recoveredCredit = 150.0,
            physicalCashInHand = 2975.00,
            expense = 50.0,
            unitSalePrice = 25.0,
            commissionPerUnit = 2.5,
            commissionPaid = 100.0
        )

        val result = SalesReconciliationCalculator.calculate(input)

        assertEquals(135.0, result.netSoldUnits, 0.001)
        assertEquals(115.0, result.cashQtyUnits, 0.001)
        assertEquals(337.5, result.grossCommission, 0.001)
        assertEquals(100.0, result.commissionPaid, 0.001)
        assertEquals(237.5, result.commissionPayable, 0.001)
        assertEquals(2875.0, result.cashFromSales, 0.001)
        assertEquals(2975.0, result.expectedCash, 0.001)
        assertEquals(2975.0, result.physicalCashInHand, 0.001)
        assertEquals(0.0, result.discrepancy, 0.001)
        assertEquals(ReconciliationStatus.BALANCED, result.status)
        assertEquals("135", result.formattedNetSold)
        assertEquals("115", result.formattedCashQty)
        assertEquals("Rs. 25.00", result.formattedUnitPrice)
        assertEquals("Rs. 2.50", result.formattedCommissionPerUnit)
        assertEquals("Rs. 337.50", result.formattedGrossCommission)
        assertEquals("Rs. 100.00", result.formattedCommissionPaid)
        assertEquals("Rs. 237.50", result.formattedCommissionPayable)
        assertEquals("Rs. 2,975.00", result.formattedExpectedCash)
        assertEquals("Rs. 0.00", result.formattedDiscrepancy)
    }

    @Test
    fun testSurplusReconciliation() {
        // Expected Cash = (2875 + 150) - 50 = Rs. 2,975.00
        // Physical cash received = Rs. 3,035.00 -> Surplus = +Rs. 60.00
        val input = SalesReconciliationInput(
            salesRep = "Sarah Chen",
            dateMillis = 1000L,
            totalSoldQty = 150.0,
            returnedQty = 10.0,
            expiredQty = 5.0,
            creditSalesQty = 20.0,
            recoveredCredit = 150.0,
            physicalCashInHand = 3035.0,
            expense = 50.0,
            unitSalePrice = 25.0,
            commissionPerUnit = 1.0
        )

        val result = SalesReconciliationCalculator.calculate(input)

        assertEquals(2975.0, result.expectedCash, 0.001)
        assertEquals(3035.0, result.physicalCashInHand, 0.001)
        assertEquals(60.0, result.discrepancy, 0.001)
        assertEquals(ReconciliationStatus.OVER, result.status)
        assertEquals("+Rs. 60.00", result.formattedDiscrepancy)
    }

    @Test
    fun testDeficitReconciliation() {
        // Expected Cash = (2875 + 150) - 50 = Rs. 2,975.00
        // Physical Cash = Rs. 2,850.00 -> Shortage = -Rs. 125.00
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

        assertEquals(2975.0, result.expectedCash, 0.001)
        assertEquals(2850.0, result.physicalCashInHand, 0.001)
        assertEquals(-125.0, result.discrepancy, 0.001)
        assertEquals(125.0, result.deficitAmount, 0.001)
        assertEquals(ReconciliationStatus.SHORT, result.status)
        assertEquals("-Rs. 125.00", result.formattedDiscrepancy)
    }

    @Test
    fun testLargeIndianCurrencyFormat() {
        // Sold: 1,050,345 @ 10 = 10,503,450
        // Commission: 1,050,345 * 1.5 = 1,575,517.50
        // Expected Cash = 10,503,450 (unaffected by commission)
        val input = SalesReconciliationInput(
            totalSoldQty = 1050345.0,
            returnedQty = 0.0,
            expiredQty = 0.0,
            creditSalesQty = 0.0,
            recoveredCredit = 0.0,
            physicalCashInHand = 10503450.00,
            expense = 0.0,
            unitSalePrice = 10.0,
            commissionPerUnit = 1.5
        )

        val result = SalesReconciliationCalculator.calculate(input)
        assertEquals("10,50,345", result.formattedNetSold)
        assertEquals("Rs. 1,05,03,450.00", result.formattedExpectedCash)
        assertEquals("Rs. 15,75,517.50", result.formattedTotalCommission)
        assertEquals(ReconciliationStatus.BALANCED, result.status)
    }

    @Test
    fun testSalesMatrixSummaryCalculations() {
        val repA = com.example.ui.components.RepMatrixSummary(
            repName = "Alex Morgan",
            transactionCount = 3,
            totalNetSoldUnits = 450.0,
            totalCashQtyUnits = 400.0,
            totalCreditSalesQty = 50.0,
            totalProductCashSales = 10000.0,
            totalRecoveredCredit = 500.0,
            totalCommission = 1125.0,
            totalExpense = 150.0,
            totalExpectedCash = 9225.0,
            totalPhysicalCash = 9225.0,
            netDiscrepancy = 0.0,
            collectionRate = 100.0,
            balancedCount = 3,
            latestTimestamp = 2000L,
            transactions = emptyList()
        )

        assertEquals(ReconciliationStatus.BALANCED, repA.status)
        assertEquals("450", repA.formattedNetSold)
        assertEquals("Rs. 10,000.00", repA.formattedProductCashSales)
        assertEquals("Rs. 1,125.00", repA.formattedTotalCommission)
        assertEquals("Rs. 9,225.00", repA.formattedExpectedCash)
        assertEquals("Rs. 0.00", repA.formattedDiscrepancy)
    }

    @Test
    fun testUserScenarioWithExpenseAndExpired() {
        // Total sold: 375, Expired: 19.45, Returned: 0, Unit Price: 400
        // Net sold = 355.55
        // Cash from sales = 355.55 * 400 = 142,220.00
        // Recoveries: 0, Commission: 0, Expense: 14,300.00
        // Expected Cash = 142,220 - 14,300 = 127,920.00
        // Physical Cash = 128,500.00
        // Discrepancy = 128,500 - 127,920 = +580.00 (Surplus)
        val input = SalesReconciliationInput(
            totalSoldQty = 375.0,
            returnedQty = 0.0,
            expiredQty = 19.45,
            creditSalesQty = 0.0,
            recoveredCredit = 0.0,
            physicalCashInHand = 128500.0,
            expense = 14300.0,
            unitSalePrice = 400.0,
            commissionPerUnit = 0.0
        )

        val result = SalesReconciliationCalculator.calculate(input)
        assertEquals(355.55, result.netSoldUnits, 0.001)
        assertEquals(142220.0, result.cashFromSales, 0.001)
        assertEquals(127920.0, result.expectedCash, 0.001)
        assertEquals(128500.0, result.physicalCashInHand, 0.001)
        assertEquals(580.0, result.discrepancy, 0.001)
        assertEquals(ReconciliationStatus.OVER, result.status)
        assertEquals("+Rs. 580.00", result.formattedDiscrepancy)
    }
}

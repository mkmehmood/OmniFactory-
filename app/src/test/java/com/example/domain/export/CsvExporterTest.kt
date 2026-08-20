package com.example.domain.export

import com.example.data.db.CalculationEntity
import com.example.data.db.SalesTransactionEntity
import org.junit.Assert.assertTrue
import org.junit.Test

class CsvExporterTest {

    @Test
    fun testCalculationHistoryCsvExport() {
        val list = listOf(
            CalculationEntity(
                id = 1L,
                expression = "1050345 + 500",
                result = "10,50,845",
                timestamp = 1700000000000L,
                note = "Daily inventory total",
                isPinned = true
            ),
            CalculationEntity(
                id = 2L,
                expression = "5000 * 2",
                result = "10,000",
                timestamp = 1700000000000L,
                note = "",
                isPinned = false
            )
        )

        val csv = CsvExporter.exportCalculationHistoryToCsv(list)

        assertTrue(csv.contains("\"ID\",\"Date Time\",\"Expression\",\"Result\",\"Tag\",\"Note\",\"Is Pinned\",\"Raw Timestamp\""))
        assertTrue(csv.contains("\"1050345 + 500\""))
        assertTrue(csv.contains("\"10,50,845\""))
        assertTrue(csv.contains("\"Daily inventory total\""))
        assertTrue(csv.contains("\"Yes\""))
    }

    @Test
    fun testSalesTransactionsCsvExport() {
        val list = listOf(
            SalesTransactionEntity(
                id = 1L,
                salesRep = "Zubair Khan",
                dateMillis = 1700000000000L,
                totalSoldQty = 150.0,
                returnedQty = 10.0,
                expiredQty = 5.0,
                creditSalesQty = 20.0,
                recoveredCredit = 150.0,
                physicalCashInHand = 2975.0,
                expense = 50.0,
                unitSalePrice = 25.0,
                commissionPerUnit = 2.5,
                totalCommission = 337.5,
                netSoldUnits = 135.0,
                cashQtyUnits = 115.0,
                expectedCash = 3025.0,
                reconciledCash = 3025.0,
                discrepancy = 0.0,
                status = "BALANCED",
                timestamp = 1700000000000L,
                note = ""
            )
        )

        val csv = CsvExporter.exportSalesTransactionsToCsv(list)

        assertTrue(csv.contains("\"Sales Representative\""))
        assertTrue(csv.contains("\"Zubair Khan\""))
        assertTrue(csv.contains("\"Rs. 3,025.00\""))
        assertTrue(csv.contains("\"Rs. 337.50\""))
        assertTrue(csv.contains("\"BALANCED\""))
    }
}

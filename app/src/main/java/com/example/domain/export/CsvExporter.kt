package com.example.domain.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.db.CalculationEntity
import com.example.data.db.SalesTransactionEntity
import com.example.domain.util.IndianNumberFormatter
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExporter {

    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    /**
     * Converts a list of CalculationEntity records into RFC 4180 standard CSV.
     */
    fun exportCalculationHistoryToCsv(calculations: List<CalculationEntity>): String {
        val sb = StringBuilder()
        // Header
        sb.append(
            listOf(
                "ID",
                "Date Time",
                "Expression",
                "Result",
                "Tag",
                "Note",
                "Is Pinned",
                "Raw Timestamp"
            ).joinToString(",") { escapeCsvCell(it) }
        ).append("\r\n")

        for (calc in calculations) {
            val formattedDateTime = dateTimeFormat.format(Date(calc.timestamp))
            val isPinnedStr = if (calc.isPinned) "Yes" else "No"
            val row = listOf(
                calc.id.toString(),
                formattedDateTime,
                calc.expression,
                calc.result,
                calc.tag,
                calc.note,
                isPinnedStr,
                calc.timestamp.toString()
            )
            sb.append(row.joinToString(",") { escapeCsvCell(it) }).append("\r\n")
        }

        return sb.toString()
    }

    /**
     * Converts a list of SalesTransactionEntity records into RFC 4180 standard CSV.
     */
    fun exportSalesTransactionsToCsv(transactions: List<SalesTransactionEntity>): String {
        val sb = StringBuilder()
        // Header
        sb.append(
            listOf(
                "ID",
                "Transaction Date",
                "Sales Representative",
                "Total Sold Qty",
                "Returned Qty",
                "Expired Qty",
                "Net Sold Units",
                "Credit Sales Qty",
                "Cash Qty Units",
                "Unit Sale Price (Rs.)",
                "Commission Per Unit (Rs.)",
                "Total Commission (Rs.)",
                "Recovered Credit (Rs.)",
                "Expected Cash (Rs.)",
                "Physical Cash In Hand (Rs.)",
                "Expense (Rs.)",
                "Reconciled Cash (Rs.)",
                "Discrepancy (Rs.)",
                "Status",
                "Note",
                "Created At"
            ).joinToString(",") { escapeCsvCell(it) }
        ).append("\r\n")

        for (item in transactions) {
            val formattedDate = dateFormat.format(Date(item.dateMillis))
            val formattedCreatedAt = dateTimeFormat.format(Date(item.timestamp))

            val formattedNetSold = formatIndianNumber(item.netSoldUnits)
            val formattedCashQty = formatIndianNumber(item.cashQtyUnits)
            val formattedSoldQty = formatIndianNumber(item.totalSoldQty)
            val formattedReturnedQty = formatIndianNumber(item.returnedQty)
            val formattedExpiredQty = formatIndianNumber(item.expiredQty)
            val formattedCreditQty = formatIndianNumber(item.creditSalesQty)

            val formattedUnitPrice = IndianNumberFormatter.formatPakistaniRupees(item.unitSalePrice)
            val formattedCommissionPerUnit = IndianNumberFormatter.formatPakistaniRupees(item.commissionPerUnit)
            val formattedTotalCommission = IndianNumberFormatter.formatPakistaniRupees(item.totalCommission)
            val formattedRecoveredCredit = IndianNumberFormatter.formatPakistaniRupees(item.recoveredCredit)
            val formattedExpectedCash = IndianNumberFormatter.formatPakistaniRupees(item.expectedCash)
            val formattedPhysicalCash = IndianNumberFormatter.formatPakistaniRupees(item.physicalCashInHand)
            val formattedExpense = IndianNumberFormatter.formatPakistaniRupees(item.expense)
            val formattedReconciledCash = IndianNumberFormatter.formatPakistaniRupees(item.reconciledCash)
            val formattedDiscrepancy = IndianNumberFormatter.formatPakistaniRupees(item.discrepancy, showSign = true)

            val row = listOf(
                item.id.toString(),
                formattedDate,
                item.salesRep,
                formattedSoldQty,
                formattedReturnedQty,
                formattedExpiredQty,
                formattedNetSold,
                formattedCreditQty,
                formattedCashQty,
                formattedUnitPrice,
                formattedCommissionPerUnit,
                formattedTotalCommission,
                formattedRecoveredCredit,
                formattedExpectedCash,
                formattedPhysicalCash,
                formattedExpense,
                formattedReconciledCash,
                formattedDiscrepancy,
                item.status,
                item.note,
                formattedCreatedAt
            )
            sb.append(row.joinToString(",") { escapeCsvCell(it) }).append("\r\n")
        }

        return sb.toString()
    }

    /**
     * Saves CSV string to a local application cache file and returns the File reference.
     */
    fun saveCsvToCacheFile(context: Context, fileName: String, csvContent: String): File {
        val exportDir = File(context.cacheDir, "exports")
        if (!exportDir.exists()) {
            exportDir.mkdirs()
        }
        val file = File(exportDir, fileName)
        FileWriter(file).use { writer ->
            writer.write(csvContent)
        }
        return file
    }

    /**
     * Prepares an Intent to share or export the CSV file using Android's Share Chooser.
     */
    fun createShareCsvIntent(context: Context, file: File, subject: String): Intent {
        val authority = "${context.packageName}.fileprovider"
        val contentUri: Uri = FileProvider.getUriForFile(context, authority, file)

        return Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    /**
     * Direct plain text share fallback.
     */
    fun createPlainTextShareIntent(subject: String, textContent: String): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, textContent)
        }
    }

    /**
     * Escapes standard CSV special characters (commas, quotes, newlines).
     */
    private fun escapeCsvCell(cell: String): String {
        val containsSpecial = cell.contains(",") || cell.contains("\"") || cell.contains("\n") || cell.contains("\r")
        return if (containsSpecial) {
            "\"" + cell.replace("\"", "\"\"") + "\""
        } else {
            "\"$cell\""
        }
    }

    private fun formatIndianNumber(value: Double): String {
        return if (value % 1.0 == 0.0) {
            IndianNumberFormatter.formatIndianDouble(value, 0)
        } else {
            IndianNumberFormatter.formatIndianDouble(value, 2)
        }
    }
}

package com.example.domain.ledger

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.data.db.LedgerTransactionEntity
import com.example.domain.util.IndianNumberFormatter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class LedgerType(
    val title: String,
    val description: String,
    val color: Color,
    val icon: ImageVector
) {
    CASH_IN(
        title = "Cash In",
        description = "Money received / Income / Sales",
        color = Color(0xFF10B981),
        icon = Icons.Default.ArrowDownward
    ),
    CASH_OUT(
        title = "Cash Out",
        description = "Money paid / Expense / Purchase",
        color = Color(0xFFEF4444),
        icon = Icons.Default.ArrowUpward
    ),
    OPENING_BALANCE(
        title = "Opening / Transfer",
        description = "Starting cash in hand or capital transfer",
        color = Color(0xFF3B82F6),
        icon = Icons.Default.AccountBalanceWallet
    )
}

enum class LedgerPeriod(val label: String, val shortName: String) {
    DAILY("Daily", "Day"),
    WEEKLY("Weekly", "Week"),
    MONTHLY("Monthly", "Month"),
    YEARLY("Yearly", "Year"),
    ALL_TIME("All Time", "All")
}

enum class PaymentMode(val displayName: String, val code: String, val icon: ImageVector) {
    CASH("Cash", "CASH", Icons.Default.Payments),
    UPI_DIGITAL("UPI / Digital", "UPI_DIGITAL", Icons.Default.AccountBalanceWallet),
    BANK_TRANSFER("Bank Transfer", "BANK_TRANSFER", Icons.Default.AccountBalance),
    CHEQUE("Cheque", "CHEQUE", Icons.Default.Receipt),
    OTHER("Other", "OTHER", Icons.Default.Money);

    companion object {
        fun fromCode(code: String): PaymentMode {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: CASH
        }
    }
}

data class LedgerCategoryInfo(
    val name: String,
    val type: LedgerType,
    val icon: ImageVector
)

object LedgerCategories {
    val INFLOW_CATEGORIES = listOf(
        LedgerCategoryInfo("Sales Revenue", LedgerType.CASH_IN, Icons.Default.Store),
        LedgerCategoryInfo("Customer Payment", LedgerType.CASH_IN, Icons.Default.People),
        LedgerCategoryInfo("Recovered Credit", LedgerType.CASH_IN, Icons.Default.Payments),
        LedgerCategoryInfo("Commission Earned", LedgerType.CASH_IN, Icons.Default.TrendingUp),
        LedgerCategoryInfo("Interest / Dividend", LedgerType.CASH_IN, Icons.Default.AccountBalance),
        LedgerCategoryInfo("Capital Deposit", LedgerType.CASH_IN, Icons.Default.AccountBalanceWallet),
        LedgerCategoryInfo("Opening Balance", LedgerType.OPENING_BALANCE, Icons.Default.AccountBalanceWallet),
        LedgerCategoryInfo("Other Income", LedgerType.CASH_IN, Icons.Default.Category)
    )

    val OUTFLOW_CATEGORIES = listOf(
        LedgerCategoryInfo("Expense / Utility", LedgerType.CASH_OUT, Icons.Default.Receipt),
        LedgerCategoryInfo("Inventory / Purchase", LedgerType.CASH_OUT, Icons.Default.ShoppingBag),
        LedgerCategoryInfo("Salary & Wages", LedgerType.CASH_OUT, Icons.Default.People),
        LedgerCategoryInfo("Rent & Maintenance", LedgerType.CASH_OUT, Icons.Default.Store),
        LedgerCategoryInfo("Logistics & Freight", LedgerType.CASH_OUT, Icons.Default.LocalShipping),
        LedgerCategoryInfo("Vendor Payment", LedgerType.CASH_OUT, Icons.Default.Payments),
        LedgerCategoryInfo("Commission Paid", LedgerType.CASH_OUT, Icons.Default.TrendingDown),
        LedgerCategoryInfo("Personal Drawing", LedgerType.CASH_OUT, Icons.Default.AccountBalanceWallet),
        LedgerCategoryInfo("Other Expense", LedgerType.CASH_OUT, Icons.Default.Category)
    )

    fun getCategoryIcon(categoryName: String): ImageVector {
        val all = INFLOW_CATEGORIES + OUTFLOW_CATEGORIES
        return all.firstOrNull { it.name.equals(categoryName, ignoreCase = true) }?.icon ?: Icons.Default.Category
    }
}

data class LedgerSummary(
    val totalCashIn: Double = 0.0,
    val totalCashOut: Double = 0.0,
    val netCashFlow: Double = 0.0,            // Inflow - Outflow in period
    val totalCashInHand: Double = 0.0,        // All-time accumulated cash in hand
    val totalOpeningBalance: Double = 0.0,
    val transactionCount: Int = 0,
    val cashInCount: Int = 0,
    val cashOutCount: Int = 0,
    val cashByMode: Map<String, Double> = emptyMap(),
    val categoryBreakdown: Map<String, Double> = emptyMap()
) {
    val formattedCashIn: String get() = "Rs. ${IndianNumberFormatter.formatIndianDouble(totalCashIn, 2)}"
    val formattedCashOut: String get() = "Rs. ${IndianNumberFormatter.formatIndianDouble(totalCashOut, 2)}"
    val formattedNetCashFlow: String
        get() {
            val prefix = if (netCashFlow > 0) "+Rs. " else if (netCashFlow < 0) "-Rs. " else "Rs. "
            return "$prefix${IndianNumberFormatter.formatIndianDouble(kotlin.math.abs(netCashFlow), 2)}"
        }
    val formattedCashInHand: String get() = "Rs. ${IndianNumberFormatter.formatIndianDouble(totalCashInHand, 2)}"
}

data class DateRangeBoundary(
    val startMillis: Long,
    val endMillis: Long,
    val title: String,
    val subtitle: String
)

object LedgerDateUtils {

    fun getPeriodBoundary(period: LedgerPeriod, referenceDateMillis: Long): DateRangeBoundary {
        val cal = Calendar.getInstance().apply {
            timeInMillis = referenceDateMillis
        }

        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.US)
        val yearFormat = SimpleDateFormat("yyyy", Locale.US)

        return when (period) {
            LedgerPeriod.DAILY -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis

                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis

                val isToday = isSameDay(referenceDateMillis, System.currentTimeMillis())
                val isYesterday = isSameDay(referenceDateMillis, System.currentTimeMillis() - 86400000L)
                val title = when {
                    isToday -> "Today"
                    isYesterday -> "Yesterday"
                    else -> dateFormat.format(Date(referenceDateMillis))
                }

                DateRangeBoundary(start, end, title, dateFormat.format(Date(referenceDateMillis)))
            }

            LedgerPeriod.WEEKLY -> {
                // Calendar Week (Monday to Sunday)
                cal.firstDayOfWeek = Calendar.MONDAY
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis

                cal.add(Calendar.DAY_OF_WEEK, 6)
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis

                val startStr = SimpleDateFormat("dd MMM", Locale.US).format(Date(start))
                val endStr = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(end))
                DateRangeBoundary(start, end, "$startStr - $endStr", "7-Day Settlement Cycle")
            }

            LedgerPeriod.MONTHLY -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis

                val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                cal.set(Calendar.DAY_OF_MONTH, maxDay)
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis

                DateRangeBoundary(start, end, monthFormat.format(Date(start)), "1 - $maxDay ${monthFormat.format(Date(start))}")
            }

            LedgerPeriod.YEARLY -> {
                cal.set(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis

                val maxDay = cal.getActualMaximum(Calendar.DAY_OF_YEAR)
                cal.set(Calendar.DAY_OF_YEAR, maxDay)
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis

                DateRangeBoundary(start, end, "Year ${yearFormat.format(Date(start))}", "Jan 1 - Dec 31, ${yearFormat.format(Date(start))}")
            }

            LedgerPeriod.ALL_TIME -> {
                DateRangeBoundary(0L, Long.MAX_VALUE, "All-Time Records", "Complete Lifetime Cashbook")
            }
        }
    }

    fun isSameDay(millis1: Long, millis2: Long): Boolean {
        val c1 = Calendar.getInstance().apply { timeInMillis = millis1 }
        val c2 = Calendar.getInstance().apply { timeInMillis = millis2 }
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
    }

    fun shiftPeriod(period: LedgerPeriod, referenceDateMillis: Long, delta: Int): Long {
        if (period == LedgerPeriod.ALL_TIME) return referenceDateMillis
        val cal = Calendar.getInstance().apply {
            timeInMillis = referenceDateMillis
        }
        when (period) {
            LedgerPeriod.DAILY -> cal.add(Calendar.DAY_OF_YEAR, delta)
            LedgerPeriod.WEEKLY -> cal.add(Calendar.WEEK_OF_YEAR, delta)
            LedgerPeriod.MONTHLY -> cal.add(Calendar.MONTH, delta)
            LedgerPeriod.YEARLY -> cal.add(Calendar.YEAR, delta)
            LedgerPeriod.ALL_TIME -> {}
        }
        return cal.timeInMillis
    }
}

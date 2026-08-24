package com.example.domain.ledger

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.domain.util.IndianNumberFormatter

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
    CASH_TRANSFER(
        title = "Cash Transfer",
        description = "Transfer between payment modes",
        color = Color(0xFF3B82F6),
        icon = Icons.Default.SwapHoriz
    )
}

enum class LedgerPeriod(val label: String, val shortName: String) {
    DAILY("Daily", "Day"),
    WEEKLY("Weekly", "Week"),
    MONTHLY("Monthly", "Month"),
    YEARLY("Yearly", "Year"),
    ALL_TIME("All Time", "All")
}

data class LedgerSummary(
    val totalCashIn: Double = 0.0,
    val totalCashOut: Double = 0.0,
    val totalTransferred: Double = 0.0,
    val netCashFlow: Double = 0.0,            // Inflow - Outflow in period
    val totalCashInHand: Double = 0.0,        // All-time accumulated cash in hand
    val totalOpeningBalance: Double = 0.0,
    val transactionCount: Int = 0,
    val cashInCount: Int = 0,
    val cashOutCount: Int = 0,
    val transferCount: Int = 0,
    val cashBalance: Double = 0.0,            // Cash mode net balance
    val bankBalance: Double = 0.0,            // Bank Transfer mode net balance
    val otherModesBalance: Double = 0.0,      // All other modes combined net balance
    val otherModesCount: Int = 0,             // Number of transactions outside default modes
    val cashByMode: Map<String, Double> = emptyMap(),
    val categoryBreakdown: Map<String, Double> = emptyMap()
) {
    val formattedCashIn: String get() = "Rs. ${IndianNumberFormatter.formatIndianDouble(totalCashIn, 2)}"
    val formattedCashOut: String get() = "Rs. ${IndianNumberFormatter.formatIndianDouble(totalCashOut, 2)}"
    val formattedTransferred: String get() = "Rs. ${IndianNumberFormatter.formatIndianDouble(totalTransferred, 2)}"
    val formattedNetCashFlow: String
        get() {
            val prefix = if (netCashFlow > 0) "+Rs. " else if (netCashFlow < 0) "-Rs. " else "Rs. "
            return "$prefix${IndianNumberFormatter.formatIndianDouble(kotlin.math.abs(netCashFlow), 2)}"
        }
    val formattedCashInHand: String get() = "Rs. ${IndianNumberFormatter.formatIndianDouble(totalCashInHand, 2)}"
    val formattedCashBalance: String get() = "Rs. ${IndianNumberFormatter.formatIndianDouble(cashBalance, 0)}"
    val formattedBankBalance: String get() = "Rs. ${IndianNumberFormatter.formatIndianDouble(bankBalance, 0)}"
    val formattedOthersBalance: String get() = "Rs. ${IndianNumberFormatter.formatIndianDouble(otherModesBalance, 0)}"
}

object LedgerPaymentUtils {
    fun isCashMode(mode: String): Boolean {
        val lower = mode.trim().lowercase()
        return lower == "cash" || lower == "cash in hand" || lower == "petty cash"
    }

    fun isBankMode(mode: String): Boolean {
        val lower = mode.trim().lowercase()
        return lower == "bank transfer" || lower == "bank" || lower == "bank_transfer" || lower == "cheque" || lower == "online"
    }

    fun isDefaultMode(mode: String): Boolean {
        return isCashMode(mode) || isBankMode(mode)
    }
}

object LedgerCategoryIcons {
    fun getCategoryIcon(name: String): ImageVector {
        val lower = name.lowercase()
        return when {
            lower.contains("sale") || lower.contains("store") -> Icons.Default.Store
            lower.contains("home") || lower.contains("house") -> Icons.Default.Home
            lower.contains("shah") || lower.contains("person") || lower.contains("party") || lower.contains("customer") || lower.contains("vendor") -> Icons.Default.Person
            lower.contains("bank") || lower.contains("transfer") -> Icons.Default.AccountBalance
            lower.contains("cash") || lower.contains("wallet") -> Icons.Default.AccountBalanceWallet
            lower.contains("other") -> Icons.Default.MoreHoriz
            else -> Icons.Default.Category
        }
    }
}

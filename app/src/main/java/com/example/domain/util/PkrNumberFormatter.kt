package com.example.domain.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * PKR (Pakistani) Number and Amount Formatter.
 * Formats numbers in Pakistani Lakhs & Crores grouping (e.g., 10,70,500.00, 1,00,000)
 * strictly WITHOUT currency symbols (Rs. / ₹ / PKR) for clean visual representation of amounts.
 */
object PkrNumberFormatter {

    /**
     * Formats a BigDecimal number using the Pakistani (PKR) numbering system.
     * Groups: Last 3 digits, then groups of 2 digits (e.g. 10,70,500 or 1,00,00,000).
     * Does NOT include any currency symbol.
     */
    fun formatPkr(value: BigDecimal, maxDecimals: Int = 8, useGrouping: Boolean = true): String {
        val absVal = value.abs()
        // For extremely large (> 100 Trillion) or microscopic numbers, use scientific notation
        if (absVal > BigDecimal("100000000000000") || (absVal > BigDecimal.ZERO && absVal < BigDecimal("0.000001"))) {
            val symbols = DecimalFormatSymbols(Locale.US).apply {
                decimalSeparator = '.'
            }
            val sciDf = DecimalFormat("0.######E0", symbols)
            return sciDf.format(value)
        }

        val isNegative = value.signum() < 0
        // Strip trailing zeros after decimal point
        val plainStr = absVal.stripTrailingZeros().toPlainString()
        val parts = plainStr.split(".")
        val integerPart = parts[0]
        val fractionalPart = if (parts.size > 1) {
            val frac = parts[1]
            if (frac.length > maxDecimals) frac.substring(0, maxDecimals).trimEnd('0') else frac
        } else ""

        val formattedInt = if (useGrouping) formatIntegerPkr(integerPart) else integerPart

        val result = if (fractionalPart.isNotEmpty()) {
            "$formattedInt.$fractionalPart"
        } else {
            formattedInt
        }

        return if (isNegative) "-$result" else result
    }

    /**
     * Formats a Double value to PKR numbering format (e.g. 10,70,500 or 10,70,500.00).
     * No currency symbol is prefixed.
     */
    fun formatPkrDouble(value: Double, decimals: Int = 2, stripTrailingZeros: Boolean = true): String {
        if (value.isNaN() || value.isInfinite()) return "0"
        val isNegative = value < 0
        val absVal = kotlin.math.abs(value)

        val rounded = BigDecimal.valueOf(absVal).setScale(decimals, RoundingMode.HALF_UP)
        val plainStr = rounded.toPlainString()
        val parts = plainStr.split(".")
        val integerPart = parts[0]
        val fracPart = if (parts.size > 1) parts[1] else ""

        val formattedInt = formatIntegerPkr(integerPart)
        val result = if (decimals > 0) {
            val processedFrac = if (stripTrailingZeros) fracPart.trimEnd('0') else fracPart.padEnd(decimals, '0')
            if (processedFrac.isNotEmpty()) "$formattedInt.$processedFrac" else formattedInt
        } else {
            formattedInt
        }

        return if (isNegative) "-$result" else result
    }

    /**
     * Formats a Double amount for visual representation strictly WITHOUT currency symbols.
     * Whole numbers format cleanly without decimals (e.g., 123.00 -> "123", 1070500.0 -> "10,70,500").
     * Numbers with fractional parts retain their decimals (e.g., 123.45 -> "123.45").
     */
    fun formatPkrAmount(
        value: Double,
        showSign: Boolean = false,
        decimals: Int = 2,
        stripTrailingZeros: Boolean = true
    ): String {
        if (value.isNaN() || value.isInfinite()) return "0"
        val absVal = kotlin.math.abs(value)
        val absFormatted = formatPkrDouble(absVal, decimals = decimals, stripTrailingZeros = stripTrailingZeros)
        return when {
            value < -0.005 -> "-$absFormatted"
            showSign && value > 0.005 -> "+$absFormatted"
            else -> absFormatted
        }
    }

    fun formatPkrLong(value: Long): String {
        return formatIntegerPkr(value.toString())
    }

    /**
     * Splits integer string from right to left using South Asian / Pakistani grouping:
     * - First group of 3 digits
     * - Then groups of 2 digits (Lakhs, Crores, etc.)
     * Example: 1070500 -> 10,70,500
     */
    fun formatIntegerPkr(intStr: String): String {
        if (intStr.length <= 3) return intStr
        val last3 = intStr.substring(intStr.length - 3)
        var remaining = intStr.substring(0, intStr.length - 3)
        val groups = mutableListOf<String>()

        while (remaining.length > 2) {
            groups.add(0, remaining.substring(remaining.length - 2))
            remaining = remaining.substring(0, remaining.length - 2)
        }
        if (remaining.isNotEmpty()) {
            groups.add(0, remaining)
        }

        return groups.joinToString(",") + "," + last3
    }
}

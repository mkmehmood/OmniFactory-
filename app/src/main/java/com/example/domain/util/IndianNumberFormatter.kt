package com.example.domain.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object IndianNumberFormatter {

    /**
     * Formats a BigDecimal number using the Indian numbering system.
     * Groups: Last 3 digits, then groups of 2 digits (e.g. 10,70,500 or 1,00,00,000).
     */
    fun formatIndian(value: BigDecimal, maxDecimals: Int = 8, useGrouping: Boolean = true): String {
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

        val formattedInt = if (useGrouping) formatIntegerIndian(integerPart) else integerPart

        val result = if (fractionalPart.isNotEmpty()) {
            "$formattedInt.$fractionalPart"
        } else {
            formattedInt
        }

        return if (isNegative) "-$result" else result
    }

    /**
     * Formats a Double value to Indian numbering format with clean representation (e.g. 10,70,500 instead of 10,70,500.00).
     * If decimals exist (e.g. 123.45), formats with up to specified decimals without redundant trailing zeros.
     */
    fun formatIndianDouble(value: Double, decimals: Int = 2, stripTrailingZeros: Boolean = true): String {
        if (value.isNaN() || value.isInfinite()) return "0"
        val isNegative = value < 0
        val absVal = kotlin.math.abs(value)

        val rounded = BigDecimal(absVal.toString()).setScale(decimals, RoundingMode.HALF_UP)
        val plainStr = rounded.toPlainString()
        val parts = plainStr.split(".")
        val integerPart = parts[0]
        val fracPart = if (parts.size > 1) parts[1] else ""

        val formattedInt = formatIntegerIndian(integerPart)
        val result = if (decimals > 0 && fracPart.isNotEmpty()) {
            val processedFrac = if (stripTrailingZeros) fracPart.trimEnd('0') else fracPart.padEnd(decimals, '0')
            if (processedFrac.isNotEmpty()) "$formattedInt.$processedFrac" else formattedInt
        } else {
            formattedInt
        }

        return if (isNegative) "-$result" else result
    }

    /**
     * Formats a Double as Pakistani Rupee in Indian number grouping format (e.g. Rs. 10,70,500 instead of Rs. 10,70,500.00).
     */
    fun formatIndianCurrency(
        value: Double,
        showSign: Boolean = false,
        symbol: String = "Rs."
    ): String {
        if (value.isNaN() || value.isInfinite()) return "$symbol 0"
        val absVal = kotlin.math.abs(value)
        if (absVal < 0.005) return "$symbol 0"
        val absFormatted = formatIndianDouble(absVal, decimals = 2, stripTrailingZeros = true)
        return when {
            value < 0 -> "-$symbol $absFormatted"
            showSign -> "+$symbol $absFormatted"
            else -> "$symbol $absFormatted"
        }
    }

    /**
     * Alias for Pakistani Rupee formatting with Indian grouping format.
     */
    fun formatPakistaniRupees(value: Double, showSign: Boolean = false): String {
        return formatIndianCurrency(value, showSign = showSign, symbol = "Rs.")
    }

    fun formatIndianLong(value: Long): String {
        return formatIntegerIndian(value.toString())
    }

    /**
     * Splits integer string from right to left:
     * - First group of 3 digits
     * - Then groups of 2 digits (Lakhs, Crores, etc.)
     * Example: 1070500 -> 10,70,500
     */
    fun formatIntegerIndian(intStr: String): String {
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

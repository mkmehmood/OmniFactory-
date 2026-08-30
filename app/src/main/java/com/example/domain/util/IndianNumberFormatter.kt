package com.example.domain.util

import java.math.BigDecimal

/**
 * Compatibility wrapper delegating to PkrNumberFormatter without currency symbols.
 */
object IndianNumberFormatter {

    fun formatIndian(value: BigDecimal, maxDecimals: Int = 8, useGrouping: Boolean = true): String {
        return PkrNumberFormatter.formatPkr(value, maxDecimals = maxDecimals, useGrouping = useGrouping)
    }

    fun formatIndianDouble(value: Double, decimals: Int = 2, stripTrailingZeros: Boolean = true): String {
        return PkrNumberFormatter.formatPkrDouble(value, decimals = decimals, stripTrailingZeros = stripTrailingZeros)
    }

    /**
     * Formats amount strictly without currency symbol by default for clean visual display.
     */
    fun formatIndianCurrency(
        value: Double,
        showSign: Boolean = false,
        symbol: String = ""
    ): String {
        val formatted = PkrNumberFormatter.formatPkrAmount(value, showSign = showSign, decimals = 2, stripTrailingZeros = true)
        return if (symbol.isEmpty()) formatted else {
            if (formatted.startsWith("-")) "-$symbol ${formatted.substring(1)}"
            else if (formatted.startsWith("+")) "+$symbol ${formatted.substring(1)}"
            else "$symbol $formatted"
        }
    }

    fun formatPakistaniRupees(value: Double, showSign: Boolean = false): String {
        return formatIndianCurrency(value, showSign = showSign, symbol = "")
    }

    fun formatIndianLong(value: Long): String {
        return PkrNumberFormatter.formatPkrLong(value)
    }

    fun formatIntegerIndian(intStr: String): String {
        return PkrNumberFormatter.formatIntegerPkr(intStr)
    }
}


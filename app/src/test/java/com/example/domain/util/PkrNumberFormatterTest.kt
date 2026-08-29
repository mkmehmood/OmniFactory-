package com.example.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class PkrNumberFormatterTest {

    @Test
    fun testPkrNumberFormatting() {
        assertEquals("10,50,345", PkrNumberFormatter.formatPkr(BigDecimal("1050345")))
        assertEquals("1,00,00,000", PkrNumberFormatter.formatPkr(BigDecimal("10000000")))
        assertEquals("1,00,000", PkrNumberFormatter.formatPkr(BigDecimal("100000")))
        assertEquals("50,345", PkrNumberFormatter.formatPkr(BigDecimal("50345")))
        assertEquals("1,234", PkrNumberFormatter.formatPkr(BigDecimal("1234")))
        assertEquals("500", PkrNumberFormatter.formatPkr(BigDecimal("500")))
        assertEquals("0", PkrNumberFormatter.formatPkr(BigDecimal("0")))
        assertEquals("10,50,345.75", PkrNumberFormatter.formatPkr(BigDecimal("1050345.75")))
        assertEquals("-10,50,345.5", PkrNumberFormatter.formatPkr(BigDecimal("-1050345.5")))
    }

    @Test
    fun testPkrAmountFormattingWithoutSymbols() {
        assertEquals("10,70,500.00", PkrNumberFormatter.formatPkrAmount(1070500.0))
        assertEquals("10,50,345.00", PkrNumberFormatter.formatPkrAmount(1050345.0))
        assertEquals("0.00", PkrNumberFormatter.formatPkrAmount(0.0))
        assertEquals("-5,000.50", PkrNumberFormatter.formatPkrAmount(-5000.50))
        assertEquals("+10,000.00", PkrNumberFormatter.formatPkrAmount(10000.0, showSign = true))
    }
}

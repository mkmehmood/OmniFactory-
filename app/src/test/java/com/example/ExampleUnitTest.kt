package com.example

import com.example.domain.util.PkrNumberFormatter
import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testPkrAmountFormatting_noTrailingZerosOnIntegers() {
        // 123.00 formats to 123
        assertEquals("123", PkrNumberFormatter.formatPkrAmount(123.00))
        assertEquals("0", PkrNumberFormatter.formatPkrAmount(0.00))
        assertEquals("10,70,500", PkrNumberFormatter.formatPkrAmount(1070500.00))
        assertEquals("1,00,000", PkrNumberFormatter.formatPkrAmount(100000.00))
        assertEquals("1,00,00,000", PkrNumberFormatter.formatPkrAmount(10000000.00))

        // Fractional amounts preserve decimals
        assertEquals("123.45", PkrNumberFormatter.formatPkrAmount(123.45))
        assertEquals("10,70,500.5", PkrNumberFormatter.formatPkrAmount(1070500.50))
    }

    @Test
    fun testPkrBigDecimalFormatting() {
        assertEquals("123", PkrNumberFormatter.formatPkr(BigDecimal("123.00")))
        assertEquals("10,70,500", PkrNumberFormatter.formatPkr(BigDecimal("1070500")))
        assertEquals("123.456", PkrNumberFormatter.formatPkr(BigDecimal("123.456000")))
    }
}


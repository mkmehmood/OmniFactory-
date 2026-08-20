package com.example.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class IndianNumberFormatterTest {

    @Test
    fun testIndianNumberFormatting() {
        assertEquals("10,50,345", IndianNumberFormatter.formatIndian(BigDecimal("1050345")))
        assertEquals("1,00,00,000", IndianNumberFormatter.formatIndian(BigDecimal("10000000")))
        assertEquals("1,00,000", IndianNumberFormatter.formatIndian(BigDecimal("100000")))
        assertEquals("50,345", IndianNumberFormatter.formatIndian(BigDecimal("50345")))
        assertEquals("1,234", IndianNumberFormatter.formatIndian(BigDecimal("1234")))
        assertEquals("500", IndianNumberFormatter.formatIndian(BigDecimal("500")))
        assertEquals("0", IndianNumberFormatter.formatIndian(BigDecimal("0")))
        assertEquals("10,50,345.75", IndianNumberFormatter.formatIndian(BigDecimal("1050345.75")))
        assertEquals("-10,50,345.5", IndianNumberFormatter.formatIndian(BigDecimal("-1050345.5")))
    }

    @Test
    fun testPakistaniRupeesInIndianFormatting() {
        assertEquals("Rs. 10,70,500.00", IndianNumberFormatter.formatPakistaniRupees(1070500.0))
        assertEquals("Rs. 10,50,345.00", IndianNumberFormatter.formatIndianCurrency(1050345.0))
        assertEquals("Rs. 0.00", IndianNumberFormatter.formatIndianCurrency(0.0))
        assertEquals("-Rs. 5,000.50", IndianNumberFormatter.formatIndianCurrency(-5000.50))
        assertEquals("+Rs. 10,000.00", IndianNumberFormatter.formatIndianCurrency(10000.0, showSign = true))
    }
}

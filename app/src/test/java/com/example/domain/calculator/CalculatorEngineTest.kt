package com.example.domain.calculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class CalculatorEngineTest {

    @Test
    fun testSequentialCalculationsFromUserPrompt() {
        // 2+5=7
        val r1 = CalculatorEngine.evaluate("2+5")
        assertTrue(r1 is CalculationResult.Success)
        assertEquals("7", CalculatorEngine.formatNumber((r1 as CalculationResult.Success).value))

        // 2+3-2=3
        val r2 = CalculatorEngine.evaluate("2+3-2")
        assertTrue(r2 is CalculationResult.Success)
        assertEquals("3", CalculatorEngine.formatNumber((r2 as CalculationResult.Success).value))

        // 10+5÷2=7.5
        val r3 = CalculatorEngine.evaluate("10+5÷2")
        assertTrue(r3 is CalculationResult.Success)
        assertEquals("7.5", CalculatorEngine.formatNumber((r3 as CalculationResult.Success).value))

        // 10+5×3÷5: (10+5)=15, 15×3=45, 45÷5=9
        val r4 = CalculatorEngine.evaluate("10+5×3÷5")
        assertTrue(r4 is CalculationResult.Success)
        assertEquals("9", CalculatorEngine.formatNumber((r4 as CalculationResult.Success).value))
    }

    @Test
    fun testIndianNumberFormattingInResults() {
        val r = CalculatorEngine.evaluate("1000000 + 50345")
        assertTrue(r is CalculationResult.Success)
        assertEquals("10,50,345", CalculatorEngine.formatNumber((r as CalculationResult.Success).value))
    }

    @Test
    fun testChainedCalculationsWithCommas() {
        val r = CalculatorEngine.evaluate("10,50,345 + 5")
        assertTrue(r is CalculationResult.Success)
        assertEquals("10,50,350", CalculatorEngine.formatNumber((r as CalculationResult.Success).value))
    }

    @Test
    fun testPercentCommercialCalculations() {
        // 199 + 15% = 228.85 (199 + 29.85)
        val r0 = CalculatorEngine.evaluate("199 + 15%")
        assertTrue(r0 is CalculationResult.Success)
        assertEquals("228.85", CalculatorEngine.formatNumber((r0 as CalculationResult.Success).value))

        // 199 - 15% = 169.15
        val r0b = CalculatorEngine.evaluate("199 - 15%")
        assertTrue(r0b is CalculationResult.Success)
        assertEquals("169.15", CalculatorEngine.formatNumber((r0b as CalculationResult.Success).value))

        // 100 + 10% = 110
        val r1 = CalculatorEngine.evaluate("100 + 10%")
        assertTrue(r1 is CalculationResult.Success)
        assertEquals("110", CalculatorEngine.formatNumber((r1 as CalculationResult.Success).value))

        // 100 - 10% = 90
        val r2 = CalculatorEngine.evaluate("100 - 10%")
        assertTrue(r2 is CalculationResult.Success)
        assertEquals("90", CalculatorEngine.formatNumber((r2 as CalculationResult.Success).value))

        // 500 × 15% = 75
        val r3 = CalculatorEngine.evaluate("500 * 15%")
        assertTrue(r3 is CalculationResult.Success)
        assertEquals("75", CalculatorEngine.formatNumber((r3 as CalculationResult.Success).value))

        // 50% alone = 0.5
        val r4 = CalculatorEngine.evaluate("50%")
        assertTrue(r4 is CalculationResult.Success)
        assertEquals("0.5", CalculatorEngine.formatNumber((r4 as CalculationResult.Success).value))
    }

    @Test
    fun testDivideByZeroHandling() {
        val r = CalculatorEngine.evaluate("50 ÷ 0")
        assertTrue(r is CalculationResult.Error)
        assertEquals("Cannot divide by 0", (r as CalculationResult.Error).message)
    }

    @Test
    fun testQuickEvaluatePreview() {
        val preview = CalculatorEngine.quickEvaluatePreview("10+5×3")
        assertEquals("45", preview)
    }

    @Test
    fun testDecimalAndParentheses() {
        val res = CalculatorEngine.evaluate("(2+3)×4")
        assertTrue(res is CalculationResult.Success)
        assertEquals("20", CalculatorEngine.formatNumber((res as CalculationResult.Success).value))
    }
}

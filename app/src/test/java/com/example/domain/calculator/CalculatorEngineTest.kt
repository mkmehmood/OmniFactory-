package com.example.domain.calculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class CalculatorEngineTest {

    @Test
    fun testStandardMathematicalOrderOfOperations() {
        // Standard addition
        val r1 = CalculatorEngine.evaluate("2+5")
        assertTrue(r1 is CalculationResult.Success)
        assertEquals("7", CalculatorEngine.formatNumber((r1 as CalculationResult.Success).value))

        // Standard addition and subtraction
        val r2 = CalculatorEngine.evaluate("2+3-2")
        assertTrue(r2 is CalculationResult.Success)
        assertEquals("3", CalculatorEngine.formatNumber((r2 as CalculationResult.Success).value))

        // Original Series operations: (10 + 5) ÷ 2 = 15 ÷ 2 = 7.5
        val r3 = CalculatorEngine.evaluate("10+5÷2")
        assertTrue(r3 is CalculationResult.Success)
        assertEquals("7.5", CalculatorEngine.formatNumber((r3 as CalculationResult.Success).value))

        // Parentheses explicitly: (10 + 5) ÷ 2 = 7.5
        val r3b = CalculatorEngine.evaluate("(10+5)÷2")
        assertTrue(r3b is CalculationResult.Success)
        assertEquals("7.5", CalculatorEngine.formatNumber((r3b as CalculationResult.Success).value))

        // Original Series operations: (10 + 5) × 3 ÷ 5 = 45 ÷ 5 = 9
        val r4 = CalculatorEngine.evaluate("10+5×3÷5")
        assertTrue(r4 is CalculationResult.Success)
        assertEquals("9", CalculatorEngine.formatNumber((r4 as CalculationResult.Success).value))

        // Original Series operations: (2 + 3) × 4 = 5 × 4 = 20
        val r5 = CalculatorEngine.evaluate("2+3×4")
        assertTrue(r5 is CalculationResult.Success)
        assertEquals("20", CalculatorEngine.formatNumber((r5 as CalculationResult.Success).value))

        // Parentheses allow grouping: 2 + (3 × 4) = 14
        val r5b = CalculatorEngine.evaluate("2+(3×4)")
        assertTrue(r5b is CalculationResult.Success)
        assertEquals("14", CalculatorEngine.formatNumber((r5b as CalculationResult.Success).value))

        // Original Series operations: (50 - 10) × 2 = 40 × 2 = 80
        val r6 = CalculatorEngine.evaluate("50-10×2")
        assertTrue(r6 is CalculationResult.Success)
        assertEquals("80", CalculatorEngine.formatNumber((r6 as CalculationResult.Success).value))

        // Parentheses allow grouping: 50 - (10 × 2) = 30
        val r6b = CalculatorEngine.evaluate("50-(10×2)")
        assertTrue(r6b is CalculationResult.Success)
        assertEquals("30", CalculatorEngine.formatNumber((r6b as CalculationResult.Success).value))
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
        // Preview respects series calculation: (10 + 5) × 3 = 45
        val preview = CalculatorEngine.quickEvaluatePreview("10+5×3")
        assertEquals("45", preview)
    }

    @Test
    fun testDecimalAndParentheses() {
        val res = CalculatorEngine.evaluate("(2+3)×4")
        assertTrue(res is CalculationResult.Success)
        assertEquals("20", CalculatorEngine.formatNumber((res as CalculationResult.Success).value))

        // Implicit multiplication
        val resImplicit = CalculatorEngine.evaluate("2(3+4)")
        assertTrue(resImplicit is CalculationResult.Success)
        assertEquals("14", CalculatorEngine.formatNumber((resImplicit as CalculationResult.Success).value))
    }

    @Test
    fun testUnaryMinusAndPowers() {
        val r1 = CalculatorEngine.evaluate("-5 + 3")
        assertTrue(r1 is CalculationResult.Success)
        assertEquals("-2", CalculatorEngine.formatNumber((r1 as CalculationResult.Success).value))

        val r2 = CalculatorEngine.evaluate("-(2+3)")
        assertTrue(r2 is CalculationResult.Success)
        assertEquals("-5", CalculatorEngine.formatNumber((r2 as CalculationResult.Success).value))

        val r3 = CalculatorEngine.evaluate("2^3*4")
        assertTrue(r3 is CalculationResult.Success)
        assertEquals("32", CalculatorEngine.formatNumber((r3 as CalculationResult.Success).value))
    }
}

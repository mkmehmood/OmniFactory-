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

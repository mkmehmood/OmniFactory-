package com.example.domain.calculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BatchScalingEngineTest {

    @Test
    fun testTargetTotalScalingWithLockedComponent() {
        // Flour: 1000g (unlocked), Water: 700g (unlocked), Salt: 20g (locked)
        // Original Total: 1720g
        // Scaled to Target Total: 3420g
        // Locked Salt stays at 20g.
        // Remaining to distribute = 3420 - 20 = 3400g
        // Original unlocked sum = 1000 + 700 = 1700g
        // Scaling factor k = 3400 / 1700 = 2.0
        // Expected: Flour = 2000g, Water = 1400g, Salt = 20g, Total = 3420g
        val components = listOf(
            ScalingComponent(name = "Flour", originalAmount = 1000.0, isLocked = false),
            ScalingComponent(name = "Water", originalAmount = 700.0, isLocked = false),
            ScalingComponent(name = "Salt", originalAmount = 20.0, isLocked = true)
        )

        val output = BatchScalingEngine.calculate(
            components = components,
            scalingMode = ScalingMode.TARGET_TOTAL,
            targetTotalInput = 3420.0,
            batchSizeInput = 500.0,
            batchSubMode = BatchSubMode.BY_BATCH_COUNT,
            batchCountInput = 1.0,
            availableAmountInput = 0.0,
            roundingOption = RoundingOption.EXACT
        )

        assertTrue(output.isValid)
        assertEquals(1720.0, output.originalGrandTotal, 0.001)
        assertEquals(3420.0, output.scaledGrandTotal, 0.001)
        assertEquals(2.0, output.scalingFactor, 0.001)

        val flour = output.results.find { it.name == "Flour" }
        val water = output.results.find { it.name == "Water" }
        val salt = output.results.find { it.name == "Salt" }

        assertNotNull(flour)
        assertNotNull(water)
        assertNotNull(salt)

        assertEquals(2000.0, flour!!.scaledAmount, 0.001)
        assertEquals(1400.0, water!!.scaledAmount, 0.001)
        assertEquals(20.0, salt!!.scaledAmount, 0.001)
    }

    @Test
    fun testBatchSizeModeByBatchCount() {
        // Recipe: 100g A, 200g B (Total = 300g)
        // Batch size = 300g per batch
        // Desired Batches = 4
        // Grand Total = 1200g
        // Scaled: A = 400g, B = 800g
        // Per Batch: A = 100g, B = 200g
        val components = listOf(
            ScalingComponent(name = "A", originalAmount = 100.0),
            ScalingComponent(name = "B", originalAmount = 200.0)
        )

        val output = BatchScalingEngine.calculate(
            components = components,
            scalingMode = ScalingMode.BATCH_SIZE,
            targetTotalInput = 0.0,
            batchSizeInput = 300.0,
            batchSubMode = BatchSubMode.BY_BATCH_COUNT,
            batchCountInput = 4.0,
            availableAmountInput = 0.0,
            roundingOption = RoundingOption.EXACT
        )

        assertTrue(output.isValid)
        assertEquals(1200.0, output.scaledGrandTotal, 0.001)
        assertEquals(4, output.fullBatchesCount)
        assertEquals(300.0, output.singleBatchGrandTotal, 0.001)

        val a = output.results.find { it.name == "A" }
        val b = output.results.find { it.name == "B" }

        assertEquals(400.0, a!!.scaledAmount, 0.001)
        assertEquals(100.0, a.perBatchAmount, 0.001)
        assertEquals(800.0, b!!.scaledAmount, 0.001)
        assertEquals(200.0, b.perBatchAmount, 0.001)
    }

    @Test
    fun testBatchSizeModeByAvailableAmountWithRemainder() {
        // Recipe: 100g A, 150g B (Total = 250g)
        // Batch size = 250g
        // Available amount = 1100g
        // Full batches = floor(1100 / 250) = 4 batches (Total utilized = 1000g)
        // Remainder = 100g left over
        val components = listOf(
            ScalingComponent(name = "A", originalAmount = 100.0),
            ScalingComponent(name = "B", originalAmount = 150.0)
        )

        val output = BatchScalingEngine.calculate(
            components = components,
            scalingMode = ScalingMode.BATCH_SIZE,
            targetTotalInput = 0.0,
            batchSizeInput = 250.0,
            batchSubMode = BatchSubMode.BY_AVAILABLE_AMOUNT,
            batchCountInput = 1.0,
            availableAmountInput = 1100.0,
            roundingOption = RoundingOption.EXACT
        )

        assertTrue(output.isValid)
        assertEquals(4, output.fullBatchesCount)
        assertEquals(1000.0, output.scaledGrandTotal, 0.001)
        assertEquals(100.0, output.remainderAmount!!, 0.001)
        assertEquals(250.0, output.singleBatchGrandTotal, 0.001)
    }

    @Test
    fun testFormulaTransparencyStepsGenerated() {
        val components = listOf(
            ScalingComponent(name = "Flour", originalAmount = 500.0),
            ScalingComponent(name = "Sugar", originalAmount = 250.0)
        )

        val output = BatchScalingEngine.calculate(
            components = components,
            scalingMode = ScalingMode.TARGET_TOTAL,
            targetTotalInput = 1500.0,
            batchSizeInput = 500.0,
            batchSubMode = BatchSubMode.BY_BATCH_COUNT,
            batchCountInput = 1.0,
            availableAmountInput = 0.0,
            roundingOption = RoundingOption.TWO_DECIMALS
        )

        assertTrue(output.formulaSteps.isNotEmpty())
        assertTrue(output.formulaSteps.any { it.contains("Scaling Factor") })
        assertTrue(output.formulaSteps.any { it.contains("Flour") })
        assertTrue(output.formulaSteps.any { it.contains("Sugar") })
    }
}

package com.example.domain.mixture

import com.example.domain.calculator.RoundingOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MixtureScaleEngineTest {

    private val ingredients = listOf(
        TargetIngredient("a", "Cement", 100.0, "kg"),
        TargetIngredient("b", "Sand", 200.0, "kg"),
        TargetIngredient("c", "Water", 50.0, "kg")
    )

    private val recipe = MixtureCalculatorEngine.recalibrate(
        ingredients, "a", mapOf("a" to 100.0, "b" to 0.0, "c" to 0.0),
        MixtureAdjustmentMode.CONSTANT_TOTAL_WEIGHT
    )
    private val base = recipe.totalNewMixtureWeight

    @Test
    fun multiplyAndSplit_matchLegacyBatchMath() {
        for (n in listOf(1.0, 2.0, 3.0, 2.5)) {
            val legacyMul = MixtureCalculatorEngine.calculateBatches(recipe, n, BatchOperationMode.MULTIPLY)
            val mul = MixtureScaleEngine.scale(recipe, ScaleMode.MULTIPLY, n, emptySet(), RoundingOption.EXACT).division
            assertEquals(legacyMul.totalResultWeight, mul.totalResultWeight, 1e-6)
            assertEquals(legacyMul.totalWeightPerBatch, mul.totalWeightPerBatch, 1e-6)

            val legacyDiv = MixtureCalculatorEngine.calculateBatches(recipe, n, BatchOperationMode.DIVIDE)
            val div = MixtureScaleEngine.scale(recipe, ScaleMode.DIVIDE, n, emptySet(), RoundingOption.EXACT).division
            assertEquals(legacyDiv.totalResultWeight, div.totalResultWeight, 1e-6)
            legacyDiv.items.indices.forEach {
                assertEquals(legacyDiv.items[it].amountPerBatch, div.items[it].amountPerBatch, 1e-6)
            }
        }
    }

    @Test
    fun targetTotal_keepsLockedIngredientFixed() {
        val out = MixtureScaleEngine.scale(recipe, ScaleMode.TARGET_TOTAL, 700.0, setOf("c"), RoundingOption.TWO_DECIMALS)
        assertNull(out.message)
        val result = out.output!!
        assertEquals(700.0, result.scaledGrandTotal, 1e-6)
        val water = result.results.first { it.id == "c" }
        assertEquals(recipe.proportions.first { it.ingredientId == "c" }.newRequiredTotal, water.scaledAmount, 1e-6)
    }

    @Test
    fun targetTotal_belowLockedSum_reportsMessage() {
        val out = MixtureScaleEngine.scale(recipe, ScaleMode.TARGET_TOTAL, 10.0, setOf("b"), RoundingOption.EXACT)
        assertNotNull(out.message)
        assertNull(out.output)
    }

    @Test
    fun stock_returnsFullBatchesAndLeftover() {
        val out = MixtureScaleEngine.scale(recipe, ScaleMode.AVAILABLE, base * 2.5, emptySet(), RoundingOption.EXACT)
        assertEquals(2, out.output!!.fullBatchesCount)
        assertEquals(base * 0.5, out.output!!.remainderAmount!!, 1e-6)
        assertEquals(base * 2, out.division.totalResultWeight, 1e-6)
    }

    @Test
    fun stock_tooSmall_orMissingValue_explainsWhy() {
        assertNotNull(MixtureScaleEngine.scale(recipe, ScaleMode.AVAILABLE, base / 2, emptySet(), RoundingOption.EXACT).message)
        assertNotNull(MixtureScaleEngine.scale(recipe, ScaleMode.AVAILABLE, 0.0, emptySet(), RoundingOption.EXACT).message)
        assertNotNull(MixtureScaleEngine.scale(recipe, ScaleMode.TARGET_TOTAL, 0.0, emptySet(), RoundingOption.EXACT).message)
    }

    @Test
    fun emptyRecipe_neverCrashes() {
        val empty = MixtureCalculatorEngine.recalibrate(emptyList(), "", emptyMap(), MixtureAdjustmentMode.CONSTANT_TOTAL_WEIGHT)
        ScaleMode.values().forEach {
            assertTrue(MixtureScaleEngine.scale(empty, it, 2.0, emptySet(), RoundingOption.EXACT).message != null)
        }
    }
}

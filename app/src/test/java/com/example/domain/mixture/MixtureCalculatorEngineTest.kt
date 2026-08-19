package com.example.domain.mixture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MixtureCalculatorEngineTest {

    @Test
    fun testSimplifiedRatio_WholeNumbers() {
        val ingredients = listOf(
            TargetIngredient(id = "1", name = "Flour", targetQuantity = 100.0, unit = "kg"),
            TargetIngredient(id = "2", name = "Water", targetQuantity = 10.0, unit = "kg")
        )
        val result = MixtureCalculatorEngine.calculateSimplifiedRatio(ingredients)
        // 100 : 10 -> 10 : 1
        assertEquals(10L, result.ratioParts[0].second)
        assertEquals(1L, result.ratioParts[1].second)
        assertEquals(110.0, result.totalWeight, 0.001)
    }

    @Test
    fun testSimplifiedRatio_MultipleIngredients() {
        val ingredients = listOf(
            TargetIngredient(id = "1", name = "Resin", targetQuantity = 100.0, unit = "kg"),
            TargetIngredient(id = "2", name = "Hardener", targetQuantity = 50.0, unit = "kg"),
            TargetIngredient(id = "3", name = "Additive", targetQuantity = 10.0, unit = "kg")
        )
        val result = MixtureCalculatorEngine.calculateSimplifiedRatio(ingredients)
        // 100 : 50 : 10 -> 10 : 5 : 1
        assertEquals(10L, result.ratioParts[0].second)
        assertEquals(5L, result.ratioParts[1].second)
        assertEquals(1L, result.ratioParts[2].second)
    }

    @Test
    fun testRecalibrate_AnchorScaledUp() {
        val ingredients = listOf(
            TargetIngredient(id = "1", name = "Resin", targetQuantity = 100.0, unit = "kg"),
            TargetIngredient(id = "2", name = "Hardener", targetQuantity = 50.0, unit = "kg"),
            TargetIngredient(id = "3", name = "Additive", targetQuantity = 10.0, unit = "kg")
        )
        // Target was 100 kg Resin, but actual added is 120 kg (Scale Factor = 120 / 100 = 1.20x)
        // Hardener already added: 40 kg -> New Total = 50 * 1.2 = 60 kg -> Still Needed = 60 - 40 = 20 kg
        // Additive already added: 8 kg -> New Total = 10 * 1.2 = 12 kg -> Still Needed = 12 - 8 = 4 kg
        val actualMap = mapOf(
            "1" to 120.0,
            "2" to 40.0,
            "3" to 8.0
        )
        val result = MixtureCalculatorEngine.recalibrate(ingredients, "1", actualMap)

        assertEquals(1.20, result.scaleFactor, 0.001)
        assertEquals(192.0, result.totalNewMixtureWeight, 0.001) // 120 + 60 + 12 = 192

        val resinProp = result.proportions.find { it.ingredientId == "1" }!!
        assertEquals(120.0, resinProp.newRequiredTotal, 0.001)
        assertEquals(0.0, resinProp.stillNeeded, 0.001)
        assertTrue(resinProp.isAnchor)

        val hardenerProp = result.proportions.find { it.ingredientId == "2" }!!
        assertEquals(60.0, hardenerProp.newRequiredTotal, 0.001)
        assertEquals(40.0, hardenerProp.alreadyAdded, 0.001)
        assertEquals(20.0, hardenerProp.stillNeeded, 0.001)

        val additiveProp = result.proportions.find { it.ingredientId == "3" }!!
        assertEquals(12.0, additiveProp.newRequiredTotal, 0.001)
        assertEquals(8.0, additiveProp.alreadyAdded, 0.001)
        assertEquals(4.0, additiveProp.stillNeeded, 0.001)
    }

    @Test
    fun testDivideBatches() {
        val ingredients = listOf(
            TargetIngredient(id = "1", name = "Resin", targetQuantity = 100.0, unit = "kg"),
            TargetIngredient(id = "2", name = "Hardener", targetQuantity = 50.0, unit = "kg")
        )
        val actualMap = mapOf(
            "1" to 120.0, // Scale 1.2x -> Resin: 120kg, Hardener: 60kg -> Total: 180kg
            "2" to 60.0
        )
        val recal = MixtureCalculatorEngine.recalibrate(ingredients, "1", actualMap)
        val batchResult = MixtureCalculatorEngine.divideBatches(recal, batchCount = 3)

        assertEquals(3, batchResult.batchCount)
        assertEquals(60.0, batchResult.totalWeightPerBatch, 0.001) // 180 / 3 = 60 kg

        val resinBatch = batchResult.items.find { it.ingredientName == "Resin" }!!
        assertEquals(40.0, resinBatch.amountPerBatch, 0.001) // 120 / 3 = 40 kg

        val hardenerBatch = batchResult.items.find { it.ingredientName == "Hardener" }!!
        assertEquals(20.0, hardenerBatch.amountPerBatch, 0.001) // 60 / 3 = 20 kg
    }
}

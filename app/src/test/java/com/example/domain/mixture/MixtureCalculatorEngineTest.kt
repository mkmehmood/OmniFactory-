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

    @Test
    fun testRecalibrate_AnchorReduced_AdditionOption_CalculatesOtherComponentsToAdd() {
        val ingredients = listOf(
            TargetIngredient(id = "1", name = "Cement", targetQuantity = 100.0, unit = "kg"),
            TargetIngredient(id = "2", name = "Sand", targetQuantity = 200.0, unit = "kg")
        )
        // User reduced Cement (Anchor) to 50 kg (0.50x scale).
        // Sand has not been added yet (0 kg in tub).
        // Option 1 must NOT tell the user to add Cement! Cement is locked at 50 kg (0 to add).
        // Option 1 calculates that Sand needs +100 kg added to match the scaled anchor.
        val actualMap = mapOf(
            "1" to 50.0,
            "2" to 0.0
        )
        val result = MixtureCalculatorEngine.recalibrate(
            ingredients = ingredients,
            anchorId = "1",
            actualAddedMap = actualMap,
            mode = MixtureAdjustmentMode.ADD_TO_BALANCE
        )

        assertEquals(0.50, result.scaleFactor, 0.001)
        assertEquals(150.0, result.totalNewMixtureWeight, 0.001) // 50 + 100
        val cement = result.proportions.find { it.ingredientId == "1" }!!
        assertEquals(0.0, cement.stillNeeded, 0.001) // Anchor must NEVER be added!
        assertEquals(50.0, cement.newRequiredTotal, 0.001)

        val sand = result.proportions.find { it.ingredientId == "2" }!!
        assertEquals(100.0, sand.newRequiredTotal, 0.001)
        assertEquals(100.0, sand.stillNeeded, 0.001) // Need to ADD +100 kg sand
        assertEquals(100.0, result.totalStillNeededWeight, 0.001)
    }

    @Test
    fun testRecalibrate_AnchorReduced_WithPartialSandInTub() {
        val ingredients = listOf(
            TargetIngredient(id = "1", name = "Cement", targetQuantity = 100.0, unit = "kg"),
            TargetIngredient(id = "2", name = "Sand", targetQuantity = 200.0, unit = "kg")
        )
        // User reduced Cement to 50 kg (0.50x scale).
        // 40 kg of Sand is already in the tub.
        // Cement Still Needed = 0.0 kg.
        // Sand Scaled Target = 100 kg -> Still Needed = 100 - 40 = 60 kg!
        val actualMap = mapOf(
            "1" to 50.0,
            "2" to 40.0
        )
        val result = MixtureCalculatorEngine.recalibrate(
            ingredients = ingredients,
            anchorId = "1",
            actualAddedMap = actualMap,
            mode = MixtureAdjustmentMode.ADD_TO_BALANCE
        )

        val cement = result.proportions.find { it.ingredientId == "1" }!!
        assertEquals(0.0, cement.stillNeeded, 0.001)

        val sand = result.proportions.find { it.ingredientId == "2" }!!
        assertEquals(100.0, sand.newRequiredTotal, 0.001)
        assertEquals(60.0, sand.stillNeeded, 0.001)
        assertEquals(60.0, result.totalStillNeededWeight, 0.001)
    }

    @Test
    fun testRecalibrate_AnchorReduced_ReduceMode_RemoveExcess() {
        val ingredients = listOf(
            TargetIngredient(id = "1", name = "Cement", targetQuantity = 100.0, unit = "kg"),
            TargetIngredient(id = "2", name = "Sand", targetQuantity = 200.0, unit = "kg")
        )
        // User reduced Cement to 50 kg (0.50x), and 200 kg of Sand is in the tub.
        // In Option 2 (Reduce mode): Strictly scales to Cement = 50 kg (0.50x).
        // Sand target at 0.50x is 100 kg. Since 200 kg is in the tub, excess = 100 kg to REDUCE / REMOVE!
        val actualMap = mapOf(
            "1" to 50.0,
            "2" to 200.0
        )
        val result = MixtureCalculatorEngine.recalibrate(
            ingredients = ingredients,
            anchorId = "1",
            actualAddedMap = actualMap,
            mode = MixtureAdjustmentMode.REDUCE_TO_TARGET
        )

        assertEquals(0.50, result.scaleFactor, 0.001)
        assertEquals(150.0, result.totalNewMixtureWeight, 0.001) // 50 + 100
        assertEquals(100.0, result.totalExcessWeight, 0.001) // 100 kg excess sand
        assertTrue(result.hasExcessInTub)

        val sand = result.proportions.find { it.ingredientId == "2" }!!
        assertEquals(100.0, sand.newRequiredTotal, 0.001)
        assertEquals(100.0, sand.excessAmount, 0.001) // Must reduce 100 kg
        assertEquals(100.0, sand.reductionFromOriginal, 0.001) // 200 - 100
    }

    @Test
    fun testRecalibrate_AnchorAdded_AdditionOption_CalculatesOtherComponentsToAdd() {
        val ingredients = listOf(
            TargetIngredient(id = "1", name = "Cement", targetQuantity = 100.0, unit = "kg"),
            TargetIngredient(id = "2", name = "Sand", targetQuantity = 200.0, unit = "kg")
        )
        // User added 120 kg of Cement (1.20x scale).
        // Sand in tub is 150 kg.
        // Option 1: Cement is locked at 120 kg (0 to add).
        // Sand target is 240 kg. In tub = 150 kg -> ADD +90 kg Sand!
        val actualMap = mapOf(
            "1" to 120.0,
            "2" to 150.0
        )
        val addResult = MixtureCalculatorEngine.recalibrate(
            ingredients = ingredients,
            anchorId = "1",
            actualAddedMap = actualMap,
            mode = MixtureAdjustmentMode.ADD_TO_BALANCE
        )
        assertEquals(1.20, addResult.scaleFactor, 0.001)
        val cementAdd = addResult.proportions.find { it.ingredientId == "1" }!!
        assertEquals(0.0, cementAdd.stillNeeded, 0.001) // Anchor must NEVER be added!

        val sandAdd = addResult.proportions.find { it.ingredientId == "2" }!!
        assertEquals(240.0, sandAdd.newRequiredTotal, 0.001)
        assertEquals(90.0, sandAdd.stillNeeded, 0.001)
        assertEquals(90.0, addResult.totalStillNeededWeight, 0.001)
    }

    @Test
    fun testRecalibrate_ConstantTotalWeight_LockedTotalMixtureWeight() {
        // Initial targets: Cement = 100 kg, Sand = 200 kg, Gravel = 300 kg. Total Initial = 600 kg.
        val ingredients = listOf(
            TargetIngredient(id = "1", name = "Cement", targetQuantity = 100.0, unit = "kg"),
            TargetIngredient(id = "2", name = "Sand", targetQuantity = 200.0, unit = "kg"),
            TargetIngredient(id = "3", name = "Gravel", targetQuantity = 300.0, unit = "kg")
        )
        // User changed Anchor (Cement) actual added to 150 kg (+50 kg exact anchor update).
        // Total mixture weight must remain locked at 600.0 kg!
        // Remaining weight for others = 600 - 150 = 450 kg.
        // Initial sum of others = 200 + 300 = 500 kg.
        // Others scale factor = 450 / 500 = 0.90 (90%).
        // Sand new target = 200 * 0.90 = 180 kg (delta: -20 kg).
        // Gravel new target = 300 * 0.90 = 270 kg (delta: -30 kg).
        // Total new sum = 150 + 180 + 270 = 600 kg (EXACTLY CONSTANT!).
        val actualMap = mapOf(
            "1" to 150.0,
            "2" to 0.0,
            "3" to 0.0
        )
        val result = MixtureCalculatorEngine.recalibrate(
            ingredients = ingredients,
            anchorId = "1",
            actualAddedMap = actualMap,
            mode = MixtureAdjustmentMode.CONSTANT_TOTAL_WEIGHT
        )

        assertEquals(600.0, result.lockedInitialTotalWeight, 0.001)
        assertEquals(600.0, result.totalNewMixtureWeight, 0.001)

        val cement = result.constantWeightProportions.find { it.ingredientId == "1" }!!
        assertEquals(150.0, cement.newRequiredTotal, 0.001)
        assertTrue(cement.isAnchor)
        assertEquals(50.0, cement.suggestedAdjustmentDelta, 0.001)

        val sand = result.constantWeightProportions.find { it.ingredientId == "2" }!!
        assertEquals(180.0, sand.newRequiredTotal, 0.001)
        assertEquals(-20.0, sand.suggestedAdjustmentDelta, 0.001)

        val gravel = result.constantWeightProportions.find { it.ingredientId == "3" }!!
        assertEquals(270.0, gravel.newRequiredTotal, 0.001)
        assertEquals(-30.0, gravel.suggestedAdjustmentDelta, 0.001)

        // Sum of all new required totals must equal 600.0 kg
        val totalCalculated = result.constantWeightProportions.sumOf { it.newRequiredTotal }
        assertEquals(600.0, totalCalculated, 0.001)
    }

    @Test
    fun testBatchScaling_DivideAndMultiply() {
        val ingredients = listOf(
            TargetIngredient(id = "1", name = "Resin", targetQuantity = 100.0, unit = "kg"),
            TargetIngredient(id = "2", name = "Hardener", targetQuantity = 50.0, unit = "kg")
        )
        val actualMap = mapOf(
            "1" to 100.0,
            "2" to 50.0
        )
        val recal = MixtureCalculatorEngine.recalibrate(ingredients, "1", actualMap, MixtureAdjustmentMode.CONSTANT_TOTAL_WEIGHT)
        // Base mixture weight = 150 kg

        // 1. DIVIDE into 3 batches
        val divideResult = MixtureCalculatorEngine.calculateBatches(
            recalibrationResult = recal,
            batchCount = 3.0,
            mode = BatchOperationMode.DIVIDE
        )
        assertEquals(3, divideResult.batchCount)
        assertEquals(50.0, divideResult.totalWeightPerBatch, 0.001) // 150 / 3
        assertEquals(150.0, divideResult.totalResultWeight, 0.001)

        val resinDiv = divideResult.items.find { it.ingredientName == "Resin" }!!
        assertEquals(33.333, resinDiv.amountPerBatch, 0.01) // 100 / 3
        assertEquals(100.0, resinDiv.totalAmount, 0.001)

        // 2. MULTIPLY by 4 batches to get Mixture
        val multiplyResult = MixtureCalculatorEngine.calculateBatches(
            recalibrationResult = recal,
            batchCount = 4.0,
            mode = BatchOperationMode.MULTIPLY
        )
        assertEquals(4, multiplyResult.batchCount)
        assertEquals(150.0, multiplyResult.totalWeightPerBatch, 0.001) // single batch
        assertEquals(600.0, multiplyResult.totalResultWeight, 0.001) // 150 * 4

        val resinMul = multiplyResult.items.find { it.ingredientName == "Resin" }!!
        assertEquals(100.0, resinMul.amountPerBatch, 0.001) // single batch amount
        assertEquals(400.0, resinMul.totalAmount, 0.001) // 100 * 4
        val hardenerMul = multiplyResult.items.find { it.ingredientName == "Hardener" }!!
        assertEquals(50.0, hardenerMul.amountPerBatch, 0.001)
        assertEquals(200.0, hardenerMul.totalAmount, 0.001)
    }
}

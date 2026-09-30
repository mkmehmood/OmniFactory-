package com.example.domain.mixture

import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToLong

data class TargetIngredient(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val targetQuantity: Double,
    val unit: String = "kg"
)

data class RatioCalculationResult(
    val simplifiedRatioString: String,
    val ratioParts: List<Pair<String, Long>>,
    val totalWeight: Double,
    val unit: String,
    val formattedTotalWeight: String
)

data class ProportionRow(
    val ingredientId: String,
    val ingredientName: String,
    val unit: String,
    val targetQty: Double,
    val newRequiredTotal: Double,
    val alreadyAdded: Double,
    val stillNeeded: Double,
    val isAnchor: Boolean,
    val formattedTarget: String,
    val formattedNewTotal: String,
    val formattedAlreadyAdded: String,
    val formattedStillNeeded: String
)

data class RecalibrationResult(
    val anchorIngredientId: String,
    val anchorIngredientName: String,
    val anchorTargetQty: Double,
    val anchorActualAdded: Double,
    val scaleFactor: Double,
    val formattedScaleFactor: String,
    val scaleExplanation: String,
    val proportions: List<ProportionRow>,
    val totalNewMixtureWeight: Double,
    val formattedTotalNewWeight: String
)

data class BatchItem(
    val ingredientName: String,
    val amountPerBatch: Double,
    val unit: String,
    val totalAmount: Double,
    val formattedPerBatch: String,
    val formattedTotal: String
)

data class BatchDivisionResult(
    val batchCount: Int,
    val items: List<BatchItem>,
    val totalWeightPerBatch: Double,
    val formattedWeightPerBatch: String
)

object MixtureCalculatorEngine {

    /**
     * Finds Greatest Common Divisor of two positive long integers.
     */
    fun gcd(a: Long, b: Long): Long {
        var x = abs(a)
        var y = abs(b)
        while (y != 0L) {
            val temp = y
            y = x % y
            x = temp
        }
        return if (x == 0L) 1L else x
    }

    /**
     * Calculates simplified whole-number ratio (e.g. 10 : 1 or 5 : 2 : 1) from list of ingredients.
     */
    fun calculateSimplifiedRatio(ingredients: List<TargetIngredient>): RatioCalculationResult {
        val validIngredients = ingredients.filter { it.targetQuantity > 0.0 }
        if (validIngredients.isEmpty()) {
            return RatioCalculationResult(
                simplifiedRatioString = "No ingredients",
                ratioParts = emptyList(),
                totalWeight = 0.0,
                unit = "kg",
                formattedTotalWeight = "0.00 kg"
            )
        }

        val totalWeight = validIngredients.sumOf { it.targetQuantity }
        val primaryUnit = validIngredients.firstOrNull()?.unit ?: "kg"

        if (validIngredients.size == 1) {
            val item = validIngredients.first()
            return RatioCalculationResult(
                simplifiedRatioString = "${item.name} (1)",
                ratioParts = listOf(item.name to 1L),
                totalWeight = totalWeight,
                unit = primaryUnit,
                formattedTotalWeight = formatNumber(totalWeight) + " $primaryUnit"
            )
        }

        // To handle fractional quantities (e.g. 2.5, 0.5, 1.25), scale all up to integers (by factor of 100 or 1000)
        val scalingFactor = 1000.0
        val integerQuantities = validIngredients.map {
            max(1L, (it.targetQuantity * scalingFactor).roundToLong())
        }

        // Find GCD across all quantities
        var commonGcd = integerQuantities.first()
        for (i in 1 until integerQuantities.size) {
            commonGcd = gcd(commonGcd, integerQuantities[i])
        }

        val reducedParts = validIngredients.indices.map { i ->
            val reduced = max(1L, integerQuantities[i] / commonGcd)
            validIngredients[i].name to reduced
        }

        val ratioString = reducedParts.joinToString(" : ") { "${it.first} (${it.second})" }
        val simplifiedNumbersOnly = reducedParts.joinToString(" : ") { "${it.second}" }

        return RatioCalculationResult(
            simplifiedRatioString = "$ratioString ($simplifiedNumbersOnly)",
            ratioParts = reducedParts,
            totalWeight = totalWeight,
            unit = primaryUnit,
            formattedTotalWeight = formatNumber(totalWeight) + " $primaryUnit"
        )
    }

    /**
     * Recalibrates mixture based on the calibration anchor ingredient and actual amount added.
     */
    fun recalibrate(
        ingredients: List<TargetIngredient>,
        anchorId: String,
        actualAddedMap: Map<String, Double>
    ): RecalibrationResult {
        val anchor = ingredients.find { it.id == anchorId } ?: ingredients.firstOrNull()
        if (anchor == null || ingredients.isEmpty()) {
            return RecalibrationResult(
                anchorIngredientId = "",
                anchorIngredientName = "None",
                anchorTargetQty = 0.0,
                anchorActualAdded = 0.0,
                scaleFactor = 1.0,
                formattedScaleFactor = "1.00x",
                scaleExplanation = "No ingredients to recalibrate.",
                proportions = emptyList(),
                totalNewMixtureWeight = 0.0,
                formattedTotalNewWeight = "0.00 kg"
            )
        }

        val anchorTarget = max(0.0001, anchor.targetQuantity)
        val anchorActual = max(0.0, actualAddedMap[anchor.id] ?: anchor.targetQuantity)

        // ScaleFactor = ActualAdded(anchorIngredient) / TargetQty(anchorIngredient)
        val scaleFactor = anchorActual / anchorTarget

        val proportions = ingredients.map { item ->
            val isAnchor = item.id == anchor.id
            val target = item.targetQuantity
            val newTotal = target * scaleFactor
            val alreadyAdded = if (isAnchor) anchorActual else (actualAddedMap[item.id] ?: 0.0)
            val stillNeeded = max(0.0, newTotal - alreadyAdded)

            ProportionRow(
                ingredientId = item.id,
                ingredientName = item.name,
                unit = item.unit,
                targetQty = target,
                newRequiredTotal = newTotal,
                alreadyAdded = alreadyAdded,
                stillNeeded = stillNeeded,
                isAnchor = isAnchor,
                formattedTarget = "${formatNumber(target)} ${item.unit}",
                formattedNewTotal = "${formatNumber(newTotal)} ${item.unit}",
                formattedAlreadyAdded = "${formatNumber(alreadyAdded)} ${item.unit}",
                formattedStillNeeded = "${formatNumber(stillNeeded)} ${item.unit}"
            )
        }

        val totalNewWeight = proportions.sumOf { it.newRequiredTotal }
        val primaryUnit = anchor.unit

        val scaleFormatted = String.format(Locale.US, "%.2f", scaleFactor)
        val anchorTargetFormatted = formatNumber(anchor.targetQuantity)
        val anchorActualFormatted = formatNumber(anchorActual)

        val explanation = if (abs(scaleFactor - 1.0) < 0.001) {
            "Because you added exactly $anchorActualFormatted ${anchor.unit} of ${anchor.name} (matching Target $anchorTargetFormatted ${anchor.unit}), the scale factor is 1.00x."
        } else {
            "Because you added $anchorActualFormatted ${anchor.unit} of ${anchor.name} (Target was $anchorTargetFormatted ${anchor.unit}), the mixture scale factor is ${scaleFormatted}x to maintain your exact recipe ratio."
        }

        return RecalibrationResult(
            anchorIngredientId = anchor.id,
            anchorIngredientName = anchor.name,
            anchorTargetQty = anchor.targetQuantity,
            anchorActualAdded = anchorActual,
            scaleFactor = scaleFactor,
            formattedScaleFactor = "${scaleFormatted}x",
            scaleExplanation = explanation,
            proportions = proportions,
            totalNewMixtureWeight = totalNewWeight,
            formattedTotalNewWeight = "${formatNumber(totalNewWeight)} $primaryUnit"
        )
    }

    /**
     * Divides recalibrated mixture into equal batches (supports decimal batch counts e.g. 2.5).
     */
    fun divideBatches(
        recalibrationResult: RecalibrationResult,
        batchCount: Double
    ): BatchDivisionResult {
        val count = if (batchCount <= 0.0) 1.0 else batchCount
        val items = recalibrationResult.proportions.map { prop ->
            val perBatch = prop.newRequiredTotal / count
            BatchItem(
                ingredientName = prop.ingredientName,
                amountPerBatch = perBatch,
                unit = prop.unit,
                totalAmount = prop.newRequiredTotal,
                formattedPerBatch = "${formatNumber(perBatch)} ${prop.unit}",
                formattedTotal = "${formatNumber(prop.newRequiredTotal)} ${prop.unit}"
            )
        }

        val totalWeightPerBatch = recalibrationResult.totalNewMixtureWeight / count
        val primaryUnit = recalibrationResult.proportions.firstOrNull()?.unit ?: "kg"

        return BatchDivisionResult(
            batchCount = if (count % 1.0 == 0.0) count.toInt() else count.toInt().coerceAtLeast(1),
            items = items,
            totalWeightPerBatch = totalWeightPerBatch,
            formattedWeightPerBatch = "${formatNumber(totalWeightPerBatch)} $primaryUnit"
        )
    }

    fun divideBatches(
        recalibrationResult: RecalibrationResult,
        batchCount: Int
    ): BatchDivisionResult = divideBatches(recalibrationResult, batchCount.toDouble())

    private fun formatNumber(value: Double): String {
        return if (value % 1.0 == 0.0) {
            String.format(Locale.US, "%,.0f", value)
        } else {
            String.format(Locale.US, "%,.2f", value)
        }
    }
}

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

enum class MixtureAdjustmentMode(val title: String, val badge: String, val description: String) {
    ADD_TO_BALANCE(
        title = "Add Components (To Match Scaled Anchor)",
        badge = "Option 1: Add Other Components",
        description = "Anchor is fixed at your scaled quantity (never asks to add anchor). Calculates which OTHER components must be added (+) to reach the scaled formulation."
    ),
    REDUCE_TO_TARGET(
        title = "Reduce Components (Proportional Reductions)",
        badge = "Option 2: Reduce / Remove Excess",
        description = "Shows how each component is reduced from the original recipe target, and indicates any excess materials in the tub that must be reduced/removed."
    )
}

data class ProportionRow(
    val ingredientId: String,
    val ingredientName: String,
    val unit: String,
    val targetQty: Double,
    val newRequiredTotal: Double,
    val alreadyAdded: Double,
    val stillNeeded: Double,
    val excessAmount: Double = 0.0,
    val reductionFromOriginal: Double = 0.0,
    val isAnchor: Boolean,
    val isLimitingFactor: Boolean = false,
    val formattedTarget: String,
    val formattedNewTotal: String,
    val formattedAlreadyAdded: String,
    val formattedStillNeeded: String,
    val formattedExcess: String = "0.00 $unit",
    val formattedReductionFromOriginal: String = "0.00 $unit"
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
    val formattedTotalNewWeight: String,
    val adjustmentMode: MixtureAdjustmentMode = MixtureAdjustmentMode.ADD_TO_BALANCE,
    val inTubScaleFactor: Double = scaleFactor,
    val formattedInTubScaleFactor: String = formattedScaleFactor,
    val strictAnchorScaleFactor: Double = scaleFactor,
    val formattedStrictAnchorScaleFactor: String = formattedScaleFactor,
    val hasExcessInTub: Boolean = false,
    val totalExcessWeight: Double = 0.0,
    val formattedTotalExcessWeight: String = "0.00 kg",
    val totalStillNeededWeight: Double = 0.0,
    val formattedTotalStillNeededWeight: String = "0.00 kg",
    val totalReductionFromOriginal: Double = 0.0,
    val formattedTotalReduction: String = "0.00 kg",
    val limitingIngredientName: String = anchorIngredientName,
    val addModeProportions: List<ProportionRow> = emptyList(),
    val reduceModeProportions: List<ProportionRow> = emptyList(),
    val addModeTotalWeight: Double = totalNewMixtureWeight,
    val reduceModeTotalWeight: Double = totalNewMixtureWeight,
    val formattedAddModeTotalWeight: String = formattedTotalNewWeight,
    val formattedReduceModeTotalWeight: String = formattedTotalNewWeight
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
     * Supports both pathways:
     * 1) ADD_TO_BALANCE: For when the mixture is in the tub and materials cannot be removed.
     *    Scales up to accommodate the highest in-tub component, calculating what components must be added.
     * 2) REDUCE_TO_TARGET: Strict anchor scaling. Shows which components must be reduced or removed.
     */
    fun recalibrate(
        ingredients: List<TargetIngredient>,
        anchorId: String,
        actualAddedMap: Map<String, Double>,
        mode: MixtureAdjustmentMode = MixtureAdjustmentMode.ADD_TO_BALANCE
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
                formattedTotalNewWeight = "0.00 kg",
                adjustmentMode = mode
            )
        }

        val primaryUnit = anchor.unit
        val anchorTarget = max(0.0001, anchor.targetQuantity)
        val anchorActual = max(0.0, actualAddedMap[anchor.id] ?: anchor.targetQuantity)

        // 1. Strict Anchor Scale Factor (Scale is strictly determined by the chosen calibration anchor)
        val strictAnchorScaleFactor = anchorActual / anchorTarget

        // Option 1: ADD_TO_BALANCE (Add Other Components to Match Scaled Anchor)
        // The anchor is fixed at anchorActual (never asks to add anchor).
        // Calculates which OTHER components must be added (+) to reach the scaled formulation.
        val addModeProportions = ingredients.map { item ->
            val isAnchor = item.id == anchor.id
            val target = item.targetQuantity
            val newTotal = target * strictAnchorScaleFactor
            val alreadyAdded = if (isAnchor) anchorActual else max(0.0, actualAddedMap[item.id] ?: 0.0)
            // Anchor is already at anchorActual, so stillNeeded for anchor is strictly 0.0!
            val stillNeeded = if (isAnchor) 0.0 else max(0.0, newTotal - alreadyAdded)
            val excess = if (isAnchor) 0.0 else max(0.0, alreadyAdded - newTotal)
            val reductionFromOriginal = max(0.0, target - newTotal)

            ProportionRow(
                ingredientId = item.id,
                ingredientName = item.name,
                unit = item.unit,
                targetQty = target,
                newRequiredTotal = newTotal,
                alreadyAdded = alreadyAdded,
                stillNeeded = stillNeeded,
                excessAmount = excess,
                reductionFromOriginal = reductionFromOriginal,
                isAnchor = isAnchor,
                isLimitingFactor = false,
                formattedTarget = "${formatNumber(target)} ${item.unit}",
                formattedNewTotal = "${formatNumber(newTotal)} ${item.unit}",
                formattedAlreadyAdded = "${formatNumber(alreadyAdded)} ${item.unit}",
                formattedStillNeeded = "${formatNumber(stillNeeded)} ${item.unit}",
                formattedExcess = "${formatNumber(excess)} ${item.unit}",
                formattedReductionFromOriginal = "${formatNumber(reductionFromOriginal)} ${item.unit}"
            )
        }
        val addModeTotalWeight = addModeProportions.sumOf { it.newRequiredTotal }
        val addModeStillNeededTotal = addModeProportions.sumOf { it.stillNeeded }

        // Option 2: REDUCE_TO_TARGET (Proportional Reduction / Remove Excess)
        // Strictly scales by strictAnchorScaleFactor. Shows recipe reductions and tub excess to remove.
        val reduceModeProportions = ingredients.map { item ->
            val isAnchor = item.id == anchor.id
            val target = item.targetQuantity
            val newTotal = target * strictAnchorScaleFactor
            val alreadyAdded = if (isAnchor) anchorActual else max(0.0, actualAddedMap[item.id] ?: 0.0)
            val stillNeeded = if (isAnchor) 0.0 else max(0.0, newTotal - alreadyAdded)
            val excess = if (isAnchor) 0.0 else max(0.0, alreadyAdded - newTotal)
            val reductionFromOriginal = max(0.0, target - newTotal)

            ProportionRow(
                ingredientId = item.id,
                ingredientName = item.name,
                unit = item.unit,
                targetQty = target,
                newRequiredTotal = newTotal,
                alreadyAdded = alreadyAdded,
                stillNeeded = stillNeeded,
                excessAmount = excess,
                reductionFromOriginal = reductionFromOriginal,
                isAnchor = isAnchor,
                isLimitingFactor = isAnchor,
                formattedTarget = "${formatNumber(target)} ${item.unit}",
                formattedNewTotal = "${formatNumber(newTotal)} ${item.unit}",
                formattedAlreadyAdded = "${formatNumber(alreadyAdded)} ${item.unit}",
                formattedStillNeeded = "${formatNumber(stillNeeded)} ${item.unit}",
                formattedExcess = "${formatNumber(excess)} ${item.unit}",
                formattedReductionFromOriginal = "${formatNumber(reductionFromOriginal)} ${item.unit}"
            )
        }
        val reduceModeTotalWeight = reduceModeProportions.sumOf { it.newRequiredTotal }
        val totalExcessWeight = reduceModeProportions.sumOf { it.excessAmount }
        val totalReductionFromOriginal = reduceModeProportions.sumOf { it.reductionFromOriginal }
        val hasExcessInTub = totalExcessWeight > 0.0001

        val activeScaleFactor = strictAnchorScaleFactor
        val activeProportions = if (mode == MixtureAdjustmentMode.ADD_TO_BALANCE) addModeProportions else reduceModeProportions
        val activeTotalWeight = if (mode == MixtureAdjustmentMode.ADD_TO_BALANCE) addModeTotalWeight else reduceModeTotalWeight

        val scaleFormatted = String.format(Locale.US, "%.2f", activeScaleFactor)
        val strictScaleFormatted = scaleFormatted
        val anchorTargetFormatted = formatNumber(anchor.targetQuantity)
        val anchorActualFormatted = formatNumber(anchorActual)

        // Explanations tailored to each mode
        val explanation = when (mode) {
            MixtureAdjustmentMode.ADD_TO_BALANCE -> {
                if (strictAnchorScaleFactor < 0.999) {
                    val pct = formatNumber((1.0 - strictAnchorScaleFactor) * 100)
                    "Option 1 (Add to Scaled Anchor): Anchor '${anchor.name}' was reduced to $anchorActualFormatted ${anchor.unit} (${strictScaleFormatted}x, -$pct%). The anchor is locked at this scaled quantity (0 to add). Add the other components (+${formatNumber(addModeStillNeededTotal)} $primaryUnit total) to match this scaled anchor formulation."
                } else if (strictAnchorScaleFactor > 1.001) {
                    val pct = formatNumber((strictAnchorScaleFactor - 1.0) * 100)
                    "Option 1 (Add to Scaled Anchor): Anchor '${anchor.name}' was increased to $anchorActualFormatted ${anchor.unit} (${strictScaleFormatted}x, +$pct%). The anchor is locked at this scaled quantity (0 to add). Add the other components (+${formatNumber(addModeStillNeededTotal)} $primaryUnit total) to balance the batch."
                } else {
                    "Option 1 (Add to Scaled Anchor): Anchor '${anchor.name}' is at exact 1.00x recipe proportions. Add the other components (+${formatNumber(addModeStillNeededTotal)} $primaryUnit total) to complete the batch."
                }
            }
            MixtureAdjustmentMode.REDUCE_TO_TARGET -> {
                if (hasExcessInTub) {
                    "Option 2 (Proportional Reductions): Scaled to Anchor '${anchor.name}' ($anchorActualFormatted ${anchor.unit} = ${strictScaleFormatted}x). Attention: ${formatNumber(totalExcessWeight)} $primaryUnit of excess material is in the tub across ingredients exceeding their scaled target and must be reduced/removed."
                } else if (strictAnchorScaleFactor < 0.999) {
                    val pct = formatNumber((1.0 - strictAnchorScaleFactor) * 100)
                    "Option 2 (Proportional Reductions): Anchor '${anchor.name}' was reduced to $anchorActualFormatted ${anchor.unit} (${strictScaleFormatted}x, -$pct% reduction). All components are reduced proportionally from the original formulation (-${formatNumber(totalReductionFromOriginal)} $primaryUnit total reduction)."
                } else {
                    "Option 2 (Proportional Reductions): Scaled to Anchor '${anchor.name}' at ${strictScaleFormatted}x. Components are adjusted to match the target formulation."
                }
            }
        }

        return RecalibrationResult(
            anchorIngredientId = anchor.id,
            anchorIngredientName = anchor.name,
            anchorTargetQty = anchor.targetQuantity,
            anchorActualAdded = anchorActual,
            scaleFactor = activeScaleFactor,
            formattedScaleFactor = "${scaleFormatted}x",
            scaleExplanation = explanation,
            proportions = activeProportions,
            totalNewMixtureWeight = activeTotalWeight,
            formattedTotalNewWeight = "${formatNumber(activeTotalWeight)} $primaryUnit",
            adjustmentMode = mode,
            inTubScaleFactor = strictAnchorScaleFactor,
            formattedInTubScaleFactor = "${strictScaleFormatted}x",
            strictAnchorScaleFactor = strictAnchorScaleFactor,
            formattedStrictAnchorScaleFactor = "${strictScaleFormatted}x",
            hasExcessInTub = hasExcessInTub,
            totalExcessWeight = totalExcessWeight,
            formattedTotalExcessWeight = "${formatNumber(totalExcessWeight)} $primaryUnit",
            totalStillNeededWeight = addModeStillNeededTotal,
            formattedTotalStillNeededWeight = "${formatNumber(addModeStillNeededTotal)} $primaryUnit",
            totalReductionFromOriginal = totalReductionFromOriginal,
            formattedTotalReduction = "${formatNumber(totalReductionFromOriginal)} $primaryUnit",
            limitingIngredientName = anchor.name,
            addModeProportions = addModeProportions,
            reduceModeProportions = reduceModeProportions,
            addModeTotalWeight = addModeTotalWeight,
            reduceModeTotalWeight = reduceModeTotalWeight,
            formattedAddModeTotalWeight = "${formatNumber(addModeTotalWeight)} $primaryUnit",
            formattedReduceModeTotalWeight = "${formatNumber(reduceModeTotalWeight)} $primaryUnit"
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

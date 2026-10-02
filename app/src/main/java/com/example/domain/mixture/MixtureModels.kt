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

enum class BatchOperationMode(val label: String, val shortSymbol: String, val description: String) {
    DIVIDE(
        label = "Divide into Batches (÷)",
        shortSymbol = "÷",
        description = "Split the mixture into equal smaller batches or containers"
    ),
    MULTIPLY(
        label = "Multiply Batches (×)",
        shortSymbol = "×",
        description = "Multiply the mixture recipe to produce multiple batches"
    )
}

enum class MixtureAdjustmentMode(val title: String, val badge: String, val description: String) {
    CONSTANT_TOTAL_WEIGHT(
        title = "Lock Total Weight (Constant Mixture)",
        badge = "Lock Total Weight",
        description = "Total mixture weight is strictly locked to initial entry. Anchor is updated to actual, and other components adjust proportionally."
    ),
    ADD_TO_BALANCE(
        title = "Scale to Anchor (Add Components)",
        badge = "Add Other Components",
        description = "Anchor is fixed at your scaled quantity (never asks to add anchor). Calculates which OTHER components must be added (+) to reach the scaled formulation."
    ),
    REDUCE_TO_TARGET(
        title = "Scale to Anchor (Reduce Excess)",
        badge = "Reduce / Remove Excess",
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
    val formattedReductionFromOriginal: String = "0.00 $unit",
    val suggestedAdjustmentDelta: Double = 0.0,
    val formattedSuggestedAdjustmentDelta: String = "0.00 $unit"
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
    val adjustmentMode: MixtureAdjustmentMode = MixtureAdjustmentMode.CONSTANT_TOTAL_WEIGHT,
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
    val lockedInitialTotalWeight: Double = totalNewMixtureWeight,
    val formattedLockedInitialTotalWeight: String = formattedTotalNewWeight,
    val constantWeightProportions: List<ProportionRow> = emptyList(),
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
    val formattedWeightPerBatch: String,
    val operationMode: BatchOperationMode = BatchOperationMode.DIVIDE,
    val batchCountDouble: Double = batchCount.toDouble(),
    val baseMixtureWeight: Double = totalWeightPerBatch * batchCount,
    val formattedBaseMixtureWeight: String = formattedWeightPerBatch,
    val totalResultWeight: Double = totalWeightPerBatch,
    val formattedResultWeight: String = formattedWeightPerBatch,
    val explanation: String = ""
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

        // 1. LOCKED INITIAL TOTAL WEIGHT (Keep Total Mixture Weight Constant of initial entry)
        val lockedInitialTotalWeight = ingredients.sumOf { it.targetQuantity }
        val remainingWeightForOthers = max(0.0, lockedInitialTotalWeight - anchorActual)
        val initialOthersSum = max(0.0001, lockedInitialTotalWeight - anchor.targetQuantity)
        val othersLockScalingFactor = remainingWeightForOthers / initialOthersSum

        val constantWeightProportions = ingredients.map { item ->
            val isAnchor = item.id == anchor.id
            val target = item.targetQuantity
            val newTotal = if (isAnchor) anchorActual else (target * othersLockScalingFactor)
            val alreadyAdded = if (isAnchor) anchorActual else max(0.0, actualAddedMap[item.id] ?: 0.0)
            val stillNeeded = if (isAnchor) 0.0 else max(0.0, newTotal - alreadyAdded)
            val excess = if (isAnchor) 0.0 else max(0.0, alreadyAdded - newTotal)
            val reductionFromOriginal = target - newTotal
            val delta = if (isAnchor) (anchorActual - target) else (newTotal - target)
            val formattedDelta = when {
                delta > 0.0001 -> "+${formatNumber(delta)} ${item.unit}"
                delta < -0.0001 -> "-${formatNumber(abs(delta))} ${item.unit}"
                else -> "0.00 ${item.unit}"
            }

            ProportionRow(
                ingredientId = item.id,
                ingredientName = item.name,
                unit = item.unit,
                targetQty = target,
                newRequiredTotal = newTotal,
                alreadyAdded = alreadyAdded,
                stillNeeded = stillNeeded,
                excessAmount = excess,
                reductionFromOriginal = max(0.0, reductionFromOriginal),
                isAnchor = isAnchor,
                isLimitingFactor = false,
                formattedTarget = "${formatNumber(target)} ${item.unit}",
                formattedNewTotal = "${formatNumber(newTotal)} ${item.unit}",
                formattedAlreadyAdded = "${formatNumber(alreadyAdded)} ${item.unit}",
                formattedStillNeeded = "${formatNumber(stillNeeded)} ${item.unit}",
                formattedExcess = "${formatNumber(excess)} ${item.unit}",
                formattedReductionFromOriginal = "${formatNumber(max(0.0, reductionFromOriginal))} ${item.unit}",
                suggestedAdjustmentDelta = delta,
                formattedSuggestedAdjustmentDelta = formattedDelta
            )
        }
        val constantWeightTotal = constantWeightProportions.sumOf { it.newRequiredTotal }
        val constantWeightStillNeededTotal = constantWeightProportions.sumOf { it.stillNeeded }
        val constantWeightExcessTotal = constantWeightProportions.sumOf { it.excessAmount }

        // 2. Strict Anchor Scale Factor (Scale is strictly determined by chosen calibration anchor)
        val strictAnchorScaleFactor = anchorActual / anchorTarget

        // Option: ADD_TO_BALANCE (Add Other Components to Match Scaled Anchor)
        val addModeProportions = ingredients.map { item ->
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

        // Option: REDUCE_TO_TARGET (Proportional Reduction / Remove Excess)
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

        val activeScaleFactor = if (mode == MixtureAdjustmentMode.CONSTANT_TOTAL_WEIGHT) 1.0 else strictAnchorScaleFactor
        val activeProportions = when (mode) {
            MixtureAdjustmentMode.CONSTANT_TOTAL_WEIGHT -> constantWeightProportions
            MixtureAdjustmentMode.ADD_TO_BALANCE -> addModeProportions
            MixtureAdjustmentMode.REDUCE_TO_TARGET -> reduceModeProportions
        }
        val activeTotalWeight = when (mode) {
            MixtureAdjustmentMode.CONSTANT_TOTAL_WEIGHT -> constantWeightTotal
            MixtureAdjustmentMode.ADD_TO_BALANCE -> addModeTotalWeight
            MixtureAdjustmentMode.REDUCE_TO_TARGET -> reduceModeTotalWeight
        }

        val scaleFormatted = String.format(Locale.US, "%.2f", activeScaleFactor)
        val strictScaleFormatted = String.format(Locale.US, "%.2f", strictAnchorScaleFactor)
        val anchorTargetFormatted = formatNumber(anchor.targetQuantity)
        val anchorActualFormatted = formatNumber(anchorActual)

        // Explanations tailored to each mode
        val explanation = when (mode) {
            MixtureAdjustmentMode.CONSTANT_TOTAL_WEIGHT -> {
                val anchorDelta = anchorActual - anchor.targetQuantity
                val sign = if (anchorDelta >= 0) "+" else ""
                val deltaFormatted = "$sign${formatNumber(anchorDelta)} ${anchor.unit}"
                "Locked Total Weight Mode: Total mixture weight is strictly locked at ${formatNumber(lockedInitialTotalWeight)} $primaryUnit (initial entry). Anchor '${anchor.name}' is set to $anchorActualFormatted ${anchor.unit} ($deltaFormatted). Other components are adjusted proportionally to keep the total mixture weight constant."
            }
            MixtureAdjustmentMode.ADD_TO_BALANCE -> {
                if (strictAnchorScaleFactor < 0.999) {
                    val pct = formatNumber((1.0 - strictAnchorScaleFactor) * 100)
                    "Option (Add to Scaled Anchor): Anchor '${anchor.name}' was reduced to $anchorActualFormatted ${anchor.unit} (${strictScaleFormatted}x, -$pct%). The anchor is locked at this scaled quantity (0 to add). Add the other components (+${formatNumber(addModeStillNeededTotal)} $primaryUnit total) to match this scaled anchor formulation."
                } else if (strictAnchorScaleFactor > 1.001) {
                    val pct = formatNumber((strictAnchorScaleFactor - 1.0) * 100)
                    "Option (Add to Scaled Anchor): Anchor '${anchor.name}' was increased to $anchorActualFormatted ${anchor.unit} (${strictScaleFormatted}x, +$pct%). The anchor is locked at this scaled quantity (0 to add). Add the other components (+${formatNumber(addModeStillNeededTotal)} $primaryUnit total) to balance the batch."
                } else {
                    "Option (Add to Scaled Anchor): Anchor '${anchor.name}' is at exact 1.00x recipe proportions. Add the other components (+${formatNumber(addModeStillNeededTotal)} $primaryUnit total) to complete the batch."
                }
            }
            MixtureAdjustmentMode.REDUCE_TO_TARGET -> {
                if (hasExcessInTub) {
                    "Option (Proportional Reductions): Scaled to Anchor '${anchor.name}' ($anchorActualFormatted ${anchor.unit} = ${strictScaleFormatted}x). Attention: ${formatNumber(totalExcessWeight)} $primaryUnit of excess material is in the tub across ingredients exceeding their scaled target and must be reduced/removed."
                } else if (strictAnchorScaleFactor < 0.999) {
                    val pct = formatNumber((1.0 - strictAnchorScaleFactor) * 100)
                    "Option (Proportional Reductions): Anchor '${anchor.name}' was reduced to $anchorActualFormatted ${anchor.unit} (${strictScaleFormatted}x, -$pct% reduction). All components are reduced proportionally from the original formulation (-${formatNumber(totalReductionFromOriginal)} $primaryUnit total reduction)."
                } else {
                    "Option (Proportional Reductions): Scaled to Anchor '${anchor.name}' at ${strictScaleFormatted}x. Components are adjusted to match the target formulation."
                }
            }
        }

        return RecalibrationResult(
            anchorIngredientId = anchor.id,
            anchorIngredientName = anchor.name,
            anchorTargetQty = anchor.targetQuantity,
            anchorActualAdded = anchorActual,
            scaleFactor = activeScaleFactor,
            formattedScaleFactor = if (mode == MixtureAdjustmentMode.CONSTANT_TOTAL_WEIGHT) "1.00x (Locked)" else "${scaleFormatted}x",
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
            totalExcessWeight = if (mode == MixtureAdjustmentMode.CONSTANT_TOTAL_WEIGHT) constantWeightExcessTotal else totalExcessWeight,
            formattedTotalExcessWeight = "${formatNumber(if (mode == MixtureAdjustmentMode.CONSTANT_TOTAL_WEIGHT) constantWeightExcessTotal else totalExcessWeight)} $primaryUnit",
            totalStillNeededWeight = if (mode == MixtureAdjustmentMode.CONSTANT_TOTAL_WEIGHT) constantWeightStillNeededTotal else addModeStillNeededTotal,
            formattedTotalStillNeededWeight = "${formatNumber(if (mode == MixtureAdjustmentMode.CONSTANT_TOTAL_WEIGHT) constantWeightStillNeededTotal else addModeStillNeededTotal)} $primaryUnit",
            totalReductionFromOriginal = totalReductionFromOriginal,
            formattedTotalReduction = "${formatNumber(totalReductionFromOriginal)} $primaryUnit",
            limitingIngredientName = anchor.name,
            lockedInitialTotalWeight = lockedInitialTotalWeight,
            formattedLockedInitialTotalWeight = "${formatNumber(lockedInitialTotalWeight)} $primaryUnit",
            constantWeightProportions = constantWeightProportions,
            addModeProportions = addModeProportions,
            reduceModeProportions = reduceModeProportions,
            addModeTotalWeight = addModeTotalWeight,
            reduceModeTotalWeight = reduceModeTotalWeight,
            formattedAddModeTotalWeight = "${formatNumber(addModeTotalWeight)} $primaryUnit",
            formattedReduceModeTotalWeight = "${formatNumber(reduceModeTotalWeight)} $primaryUnit"
        )
    }

    /**
     * Calculates batches: supports both DIVIDE (equal smaller batches) and MULTIPLY (producing multiple batches).
     */
    fun calculateBatches(
        recalibrationResult: RecalibrationResult,
        batchCount: Double,
        mode: BatchOperationMode = BatchOperationMode.DIVIDE
    ): BatchDivisionResult {
        val count = if (batchCount <= 0.0) 1.0 else batchCount
        val primaryUnit = recalibrationResult.proportions.firstOrNull()?.unit ?: "kg"
        val baseMixtureWeight = recalibrationResult.totalNewMixtureWeight

        val items = recalibrationResult.proportions.map { prop ->
            val perBatchAmount = if (mode == BatchOperationMode.DIVIDE) {
                prop.newRequiredTotal / count
            } else {
                prop.newRequiredTotal
            }
            val totalAmount = if (mode == BatchOperationMode.DIVIDE) {
                prop.newRequiredTotal
            } else {
                prop.newRequiredTotal * count
            }
            BatchItem(
                ingredientName = prop.ingredientName,
                amountPerBatch = perBatchAmount,
                unit = prop.unit,
                totalAmount = totalAmount,
                formattedPerBatch = "${formatNumber(perBatchAmount)} ${prop.unit}",
                formattedTotal = "${formatNumber(totalAmount)} ${prop.unit}"
            )
        }

        val weightPerBatch = if (mode == BatchOperationMode.DIVIDE) {
            baseMixtureWeight / count
        } else {
            baseMixtureWeight
        }

        val totalResultWeight = if (mode == BatchOperationMode.DIVIDE) {
            baseMixtureWeight
        } else {
            baseMixtureWeight * count
        }

        val explanation = if (mode == BatchOperationMode.DIVIDE) {
            "Dividing ${formatNumber(baseMixtureWeight)} $primaryUnit mixture into ${formatNumber(count)} equal batches (${formatNumber(weightPerBatch)} $primaryUnit per batch)."
        } else {
            "Multiplying single batch recipe of ${formatNumber(baseMixtureWeight)} $primaryUnit by ${formatNumber(count)} batches to produce a total mixture of ${formatNumber(totalResultWeight)} $primaryUnit."
        }

        return BatchDivisionResult(
            batchCount = if (count % 1.0 == 0.0) count.toInt() else count.toInt().coerceAtLeast(1),
            items = items,
            totalWeightPerBatch = weightPerBatch,
            formattedWeightPerBatch = "${formatNumber(weightPerBatch)} $primaryUnit",
            operationMode = mode,
            batchCountDouble = count,
            baseMixtureWeight = baseMixtureWeight,
            formattedBaseMixtureWeight = "${formatNumber(baseMixtureWeight)} $primaryUnit",
            totalResultWeight = totalResultWeight,
            formattedResultWeight = "${formatNumber(totalResultWeight)} $primaryUnit",
            explanation = explanation
        )
    }

    /**
     * Divides recalibrated mixture into equal batches (supports decimal batch counts e.g. 2.5).
     */
    fun divideBatches(
        recalibrationResult: RecalibrationResult,
        batchCount: Double
    ): BatchDivisionResult = calculateBatches(recalibrationResult, batchCount, BatchOperationMode.DIVIDE)

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

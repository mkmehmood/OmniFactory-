package com.example.domain.mixture

import com.example.domain.calculator.BatchScalingEngine
import com.example.domain.calculator.BatchScalingOutput
import com.example.domain.calculator.BatchSubMode
import com.example.domain.calculator.RoundingOption
import com.example.domain.calculator.ScalingComponent
import com.example.domain.calculator.ScalingMode

/**
 * The four ways a finished formulation can be scaled. They replace the old Divide/Multiply pair
 * and the standalone Batch Scaler, and all run through [BatchScalingEngine].
 */
enum class ScaleMode(
    val label: String,
    val symbol: String,
    val prompt: String,
    val hint: String
) {
    MULTIPLY("Multiply", "×", "How many batches of this recipe?", "Make N full batches of the recipe"),
    DIVIDE("Split", "÷", "Split into how many equal batches?", "Split the mixture into N equal batches"),
    TARGET_TOTAL("Target", "=", "Target total mixture weight", "Scale the whole recipe to a total you choose"),
    AVAILABLE("Stock", "∑", "Material available", "See how many full batches your stock makes");

    /** Multiply and Split are driven by a batch count; the others by a weight. */
    val usesBatchCount: Boolean get() = this == MULTIPLY || this == DIVIDE
}

data class ScaleOutcome(
    val division: BatchDivisionResult,
    val output: BatchScalingOutput?,
    /** Set when the inputs cannot be scaled yet (nothing to scale, locks exceed target, stock too small...). */
    val message: String?
)

object MixtureScaleEngine {

    /**
     * Scales [recipe] (the final recalibrated formulation) according to [mode].
     *
     * @param value batch count for MULTIPLY/DIVIDE, otherwise a weight (target total or available stock).
     * @param lockedIds ingredients held at their recipe amount; the rest absorb the change.
     */
    fun scale(
        recipe: RecalibrationResult,
        mode: ScaleMode,
        value: Double,
        lockedIds: Set<String>,
        rounding: RoundingOption
    ): ScaleOutcome {
        val unit = recipe.proportions.firstOrNull()?.unit ?: "kg"
        val components = recipe.proportions
            .filter { it.newRequiredTotal > 0.0 }
            .map {
                ScalingComponent(
                    id = it.ingredientId,
                    name = it.ingredientName,
                    originalAmount = it.newRequiredTotal,
                    isLocked = it.ingredientId in lockedIds
                )
            }
        val base = components.sumOf { it.originalAmount }
        val count = if (value > 0.0) value else 1.0

        val output = when (mode) {
            ScaleMode.MULTIPLY -> BatchScalingEngine.calculate(
                components, ScalingMode.BATCH_SIZE, 0.0, base, BatchSubMode.BY_BATCH_COUNT, count, 0.0, rounding
            )
            ScaleMode.DIVIDE -> BatchScalingEngine.calculate(
                components, ScalingMode.BATCH_SIZE, 0.0, if (base > 0.0) base / count else 0.0,
                BatchSubMode.BY_BATCH_COUNT, count, 0.0, rounding
            )
            ScaleMode.TARGET_TOTAL -> BatchScalingEngine.calculate(
                components, ScalingMode.TARGET_TOTAL, value, 0.0, BatchSubMode.BY_BATCH_COUNT, 1.0, 0.0, rounding
            )
            ScaleMode.AVAILABLE -> BatchScalingEngine.calculate(
                components, ScalingMode.BATCH_SIZE, 0.0, base, BatchSubMode.BY_AVAILABLE_AMOUNT, 1.0, value, rounding
            )
        }

        val message: String? = when {
            !output.isValid -> output.errorMessage
            mode == ScaleMode.TARGET_TOTAL && value <= 0.0 -> "Enter a target total to scale to."
            mode == ScaleMode.AVAILABLE && value <= 0.0 -> "Enter the amount of material you have."
            mode == ScaleMode.AVAILABLE && output.fullBatchesCount < 1 ->
                "Not enough for one full batch (needs ${BatchScalingEngine.formatNumber(base, rounding)} $unit)."
            else -> null
        }

        return ScaleOutcome(
            division = toDivisionResult(recipe, mode, output, count, base, unit, rounding),
            output = output.takeIf { it.isValid },
            message = message
        )
    }

    private fun toDivisionResult(
        recipe: RecalibrationResult,
        mode: ScaleMode,
        out: BatchScalingOutput,
        count: Double,
        base: Double,
        unit: String,
        rounding: RoundingOption
    ): BatchDivisionResult {
        fun fmt(v: Double) = BatchScalingEngine.formatNumber(v, rounding)
        val batches = if (out.isValid) out.numberOfBatches.coerceAtLeast(1.0) else count

        val items = if (out.isValid) {
            out.results.map {
                BatchItem(
                    ingredientName = it.name,
                    amountPerBatch = it.perBatchAmount,
                    unit = unit,
                    totalAmount = it.scaledAmount,
                    formattedPerBatch = "${it.perBatchAmountFormatted} $unit",
                    formattedTotal = "${it.scaledAmountFormatted} $unit",
                    ingredientId = it.id
                )
            }
        } else {
            recipe.proportions.map {
                BatchItem(it.ingredientName, it.newRequiredTotal, it.unit, it.newRequiredTotal,
                    "${fmt(it.newRequiredTotal)} ${it.unit}", "${fmt(it.newRequiredTotal)} ${it.unit}", it.ingredientId)
            }
        }

        val perBatch = if (out.isValid) out.singleBatchGrandTotal else base
        val total = if (out.isValid) out.scaledGrandTotal else base

        val explanation = when {
            !out.isValid -> out.errorMessage ?: "Nothing to scale yet."
            mode == ScaleMode.DIVIDE -> "Splitting ${fmt(total)} $unit into ${fmt(batches)} equal batches (${fmt(perBatch)} $unit each)."
            mode == ScaleMode.MULTIPLY -> "Making ${fmt(batches)} batches of ${fmt(base)} $unit = ${fmt(total)} $unit total."
            mode == ScaleMode.TARGET_TOTAL -> "Scaling ${fmt(base)} $unit to ${fmt(total)} $unit (×${"%.4f".format(java.util.Locale.US, out.scalingFactor)} on unlocked items)."
            else -> "${fmt(out.totalAvailableAmount ?: 0.0)} $unit in stock = ${out.fullBatchesCount} full batches of ${fmt(base)} $unit, leftover ${fmt(out.remainderAmount ?: 0.0)} $unit."
        }

        return BatchDivisionResult(
            batchCount = batches.toInt().coerceAtLeast(1),
            items = items,
            totalWeightPerBatch = perBatch,
            formattedWeightPerBatch = "${fmt(perBatch)} $unit",
            operationMode = if (mode == ScaleMode.DIVIDE) BatchOperationMode.DIVIDE else BatchOperationMode.MULTIPLY,
            batchCountDouble = batches,
            baseMixtureWeight = base,
            formattedBaseMixtureWeight = "${fmt(base)} $unit",
            totalResultWeight = total,
            formattedResultWeight = "${fmt(total)} $unit",
            explanation = explanation
        )
    }
}

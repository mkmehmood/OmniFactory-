package com.example.domain.calculator

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import java.util.UUID

enum class RoundingOption(val label: String, val decimals: Int?) {
    EXACT("Exact", null),
    WHOLE("Whole (0)", 0),
    ONE_DECIMAL("1 Dec (0.0)", 1),
    TWO_DECIMALS("2 Dec (0.00)", 2),
    THREE_DECIMALS("3 Dec (0.000)", 3)
}

enum class ScalingMode {
    TARGET_TOTAL,
    BATCH_SIZE
}

enum class BatchSubMode {
    BY_BATCH_COUNT,
    BY_AVAILABLE_AMOUNT
}

data class ScalingComponent(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val originalAmount: Double,
    val isLocked: Boolean = false,
    val lockedValue: Double? = null // if null and isLocked is true, uses originalAmount
)

data class ScaledComponentResult(
    val id: String,
    val name: String,
    val originalAmount: Double,
    val originalSharePercent: Double,
    val isLocked: Boolean,
    val scaledAmount: Double,
    val scaledSharePercent: Double,
    val perBatchAmount: Double,
    val originalAmountFormatted: String,
    val scaledAmountFormatted: String,
    val perBatchAmountFormatted: String
)

data class BatchScalingOutput(
    val results: List<ScaledComponentResult>,
    val originalGrandTotal: Double,
    val targetGrandTotal: Double,
    val scaledGrandTotal: Double,
    val singleBatchGrandTotal: Double,
    val lockedTotal: Double,
    val unlockedOriginalTotal: Double,
    val unlockedScaledTotal: Double,
    val scalingFactor: Double,
    val isBatchMode: Boolean,
    val batchSubMode: BatchSubMode,
    val batchSize: Double,
    val numberOfBatches: Double,
    val fullBatchesCount: Int,
    val totalAvailableAmount: Double?,
    val remainderAmount: Double?,
    val roundingOption: RoundingOption,
    val formulaSteps: List<String>,
    val arithmeticSummary: String,
    val isValid: Boolean,
    val errorMessage: String? = null
)

object BatchScalingEngine {

    private val mathContext = MathContext(12, RoundingMode.HALF_UP)

    fun calculate(
        components: List<ScalingComponent>,
        scalingMode: ScalingMode,
        targetTotalInput: Double,
        batchSizeInput: Double,
        batchSubMode: BatchSubMode,
        batchCountInput: Double,
        availableAmountInput: Double,
        roundingOption: RoundingOption
    ): BatchScalingOutput {
        val validComponents = components.filter { it.originalAmount > 0 }

        if (validComponents.isEmpty()) {
            return BatchScalingOutput(
                results = emptyList(),
                originalGrandTotal = 0.0,
                targetGrandTotal = 0.0,
                scaledGrandTotal = 0.0,
                singleBatchGrandTotal = 0.0,
                lockedTotal = 0.0,
                unlockedOriginalTotal = 0.0,
                unlockedScaledTotal = 0.0,
                scalingFactor = 1.0,
                isBatchMode = scalingMode == ScalingMode.BATCH_SIZE,
                batchSubMode = batchSubMode,
                batchSize = batchSizeInput,
                numberOfBatches = 0.0,
                fullBatchesCount = 0,
                totalAvailableAmount = if (batchSubMode == BatchSubMode.BY_AVAILABLE_AMOUNT) availableAmountInput else null,
                remainderAmount = null,
                roundingOption = roundingOption,
                formulaSteps = listOf("Add at least one component with quantity > 0 to calculate."),
                arithmeticSummary = "No components configured.",
                isValid = false,
                errorMessage = "Please add one or more components with amounts greater than 0."
            )
        }

        val originalGrandTotal = validComponents.sumOf { it.originalAmount }
        if (originalGrandTotal <= 0.0) {
            return BatchScalingOutput(
                results = emptyList(),
                originalGrandTotal = 0.0,
                targetGrandTotal = 0.0,
                scaledGrandTotal = 0.0,
                singleBatchGrandTotal = 0.0,
                lockedTotal = 0.0,
                unlockedOriginalTotal = 0.0,
                unlockedScaledTotal = 0.0,
                scalingFactor = 1.0,
                isBatchMode = scalingMode == ScalingMode.BATCH_SIZE,
                batchSubMode = batchSubMode,
                batchSize = batchSizeInput,
                numberOfBatches = 0.0,
                fullBatchesCount = 0,
                totalAvailableAmount = null,
                remainderAmount = null,
                roundingOption = roundingOption,
                formulaSteps = listOf("Original total must be greater than 0."),
                arithmeticSummary = "Total component weight is 0.",
                isValid = false,
                errorMessage = "Component amounts must sum to more than 0."
            )
        }

        val lockedComponents = validComponents.filter { it.isLocked }
        val unlockedComponents = validComponents.filter { !it.isLocked }

        val lockedTotal = lockedComponents.sumOf { it.lockedValue ?: it.originalAmount }
        val unlockedOriginalTotal = unlockedComponents.sumOf { it.originalAmount }

        var targetGrandTotal = 0.0
        var batchCount = 1.0
        var fullBatches = 1
        var remainder: Double? = null
        val formulaSteps = mutableListOf<String>()

        val isBatchMode = scalingMode == ScalingMode.BATCH_SIZE

        if (isBatchMode) {
            val batchSize = if (batchSizeInput > 0) batchSizeInput else originalGrandTotal
            when (batchSubMode) {
                BatchSubMode.BY_BATCH_COUNT -> {
                    val count = if (batchCountInput > 0) batchCountInput else 1.0
                    batchCount = count
                    fullBatches = count.toInt()
                    targetGrandTotal = batchSize * count
                    formulaSteps.add("Mode: Batch Size ($batchSize) × Count ($count) = Grand Total Target: ${formatNumber(targetGrandTotal, roundingOption)}")
                }
                BatchSubMode.BY_AVAILABLE_AMOUNT -> {
                    val available = if (availableAmountInput > 0) availableAmountInput else batchSize
                    fullBatches = if (batchSize > 0) (available / batchSize).toInt() else 0
                    batchCount = fullBatches.toDouble()
                    if (fullBatches > 0) {
                        targetGrandTotal = fullBatches * batchSize
                        remainder = available - targetGrandTotal
                        formulaSteps.add("Mode: Available Amount ($available) ÷ Batch Size ($batchSize) = $fullBatches full batches")
                        formulaSteps.add("Utilized Total = $fullBatches × $batchSize = ${formatNumber(targetGrandTotal, roundingOption)}, Leftover Remainder = ${formatNumber(remainder, roundingOption)}")
                    } else {
                        targetGrandTotal = 0.0
                        remainder = available
                        formulaSteps.add("Available amount ($available) is less than 1 full batch ($batchSize). Leftover = $available")
                    }
                }
            }
        } else {
            targetGrandTotal = if (targetTotalInput > 0) targetTotalInput else originalGrandTotal
            batchCount = 1.0
            fullBatches = 1
            formulaSteps.add("Mode: Scale to Target Grand Total = ${formatNumber(targetGrandTotal, roundingOption)}")
        }

        // Validate target vs locked sum
        if (targetGrandTotal < lockedTotal) {
            val err = "Target total (${formatNumber(targetGrandTotal, roundingOption)}) cannot be less than locked components sum (${formatNumber(lockedTotal, roundingOption)})."
            return BatchScalingOutput(
                results = emptyList(),
                originalGrandTotal = originalGrandTotal,
                targetGrandTotal = targetGrandTotal,
                scaledGrandTotal = 0.0,
                singleBatchGrandTotal = 0.0,
                lockedTotal = lockedTotal,
                unlockedOriginalTotal = unlockedOriginalTotal,
                unlockedScaledTotal = 0.0,
                scalingFactor = 0.0,
                isBatchMode = isBatchMode,
                batchSubMode = batchSubMode,
                batchSize = batchSizeInput,
                numberOfBatches = batchCount,
                fullBatchesCount = fullBatches,
                totalAvailableAmount = if (batchSubMode == BatchSubMode.BY_AVAILABLE_AMOUNT) availableAmountInput else null,
                remainderAmount = remainder,
                roundingOption = roundingOption,
                formulaSteps = listOf(err),
                arithmeticSummary = "Constraint violation: Locked sum exceeds target.",
                isValid = false,
                errorMessage = err
            )
        }

        // Compute scaling factor for unlocked components
        val remainingForUnlocked = targetGrandTotal - lockedTotal
        val scalingFactor = if (unlockedOriginalTotal > 0.0) {
            remainingForUnlocked / unlockedOriginalTotal
        } else {
            1.0
        }

        if (unlockedComponents.isNotEmpty()) {
            formulaSteps.add(
                "Scaling Factor for unlocked components: ($targetGrandTotal target - $lockedTotal locked) ÷ $unlockedOriginalTotal original unlocked = ${formatFactor(scalingFactor)}"
            )
        } else {
            formulaSteps.add("All components are locked. Scaled amounts match locked values exactly.")
        }

        val results = validComponents.map { comp ->
            val origShare = if (originalGrandTotal > 0) (comp.originalAmount / originalGrandTotal) * 100.0 else 0.0

            val scaledVal = if (comp.isLocked) {
                comp.lockedValue ?: comp.originalAmount
            } else {
                comp.originalAmount * scalingFactor
            }

            val scaledShare = if (targetGrandTotal > 0) (scaledVal / targetGrandTotal) * 100.0 else 0.0
            val perBatchVal = if (batchCount > 0) scaledVal / batchCount else scaledVal

            formulaSteps.add(
                if (comp.isLocked) {
                    "• ${comp.name}: Locked at ${formatNumber(scaledVal, roundingOption)} (${formatPercent(scaledShare)})"
                } else {
                    "• ${comp.name}: ${formatNumber(comp.originalAmount, roundingOption)} × ${formatFactor(scalingFactor)} = ${formatNumber(scaledVal, roundingOption)} (${formatPercent(scaledShare)})"
                }
            )

            ScaledComponentResult(
                id = comp.id,
                name = comp.name,
                originalAmount = comp.originalAmount,
                originalSharePercent = origShare,
                isLocked = comp.isLocked,
                scaledAmount = scaledVal,
                scaledSharePercent = scaledShare,
                perBatchAmount = perBatchVal,
                originalAmountFormatted = formatNumber(comp.originalAmount, roundingOption),
                scaledAmountFormatted = formatNumber(scaledVal, roundingOption),
                perBatchAmountFormatted = formatNumber(perBatchVal, roundingOption)
            )
        }

        val scaledGrandTotal = results.sumOf { it.scaledAmount }
        val unlockedScaledTotal = results.filter { !it.isLocked }.sumOf { it.scaledAmount }
        val singleBatchGrandTotal = results.sumOf { it.perBatchAmount }

        if (isBatchMode && batchCount > 0) {
            formulaSteps.add("Per Batch Division: Each component scaled amount ÷ $batchCount batches (Total per batch = ${formatNumber(singleBatchGrandTotal, roundingOption)})")
        }

        val summaryText = buildString {
            append("Scaled from ${formatNumber(originalGrandTotal, roundingOption)} to ${formatNumber(scaledGrandTotal, roundingOption)} ")
            if (isBatchMode) {
                append("across $fullBatches batches (${formatNumber(singleBatchGrandTotal, roundingOption)} / batch)")
            } else {
                append("using scaling factor ${formatFactor(scalingFactor)}")
            }
        }

        return BatchScalingOutput(
            results = results,
            originalGrandTotal = originalGrandTotal,
            targetGrandTotal = targetGrandTotal,
            scaledGrandTotal = scaledGrandTotal,
            singleBatchGrandTotal = singleBatchGrandTotal,
            lockedTotal = lockedTotal,
            unlockedOriginalTotal = unlockedOriginalTotal,
            unlockedScaledTotal = unlockedScaledTotal,
            scalingFactor = scalingFactor,
            isBatchMode = isBatchMode,
            batchSubMode = batchSubMode,
            batchSize = batchSizeInput,
            numberOfBatches = batchCount,
            fullBatchesCount = fullBatches,
            totalAvailableAmount = if (batchSubMode == BatchSubMode.BY_AVAILABLE_AMOUNT) availableAmountInput else null,
            remainderAmount = remainder,
            roundingOption = roundingOption,
            formulaSteps = formulaSteps,
            arithmeticSummary = summaryText,
            isValid = true,
            errorMessage = null
        )
    }

    fun formatNumber(value: Double, rounding: RoundingOption): String {
        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = ','
            decimalSeparator = '.'
        }

        return when (rounding) {
            RoundingOption.EXACT -> {
                val df = DecimalFormat("#,##0.######", symbols)
                df.format(value)
            }
            RoundingOption.WHOLE -> {
                val df = DecimalFormat("#,##0", symbols)
                df.format(Math.round(value))
            }
            RoundingOption.ONE_DECIMAL -> {
                val df = DecimalFormat("#,##0.0", symbols)
                df.format(value)
            }
            RoundingOption.TWO_DECIMALS -> {
                val df = DecimalFormat("#,##0.00", symbols)
                df.format(value)
            }
            RoundingOption.THREE_DECIMALS -> {
                val df = DecimalFormat("#,##0.000", symbols)
                df.format(value)
            }
        }
    }

    private fun formatFactor(factor: Double): String {
        val symbols = DecimalFormatSymbols(Locale.US)
        val df = DecimalFormat("0.####", symbols)
        return df.format(factor)
    }

    private fun formatPercent(percent: Double): String {
        val symbols = DecimalFormatSymbols(Locale.US)
        val df = DecimalFormat("0.0%", symbols)
        return df.format(percent / 100.0)
    }
}

package com.example.domain.mixture

/**
 * Bridges the equation solver into the main Mixture Calculator pipeline.
 *
 * Every solver method (Target Total, Per Reference, Base + Additives) is converted into the same
 * [RecalibrationResult] the anchor method produces, so Step 4 (result), Step 5 (batch scaling),
 * copy/export and history all work on the solved formulation without special cases.
 */
data class FormulaLine(
    val id: String,
    val name: String,
    /** Short tag such as "Ratio", "Fixed", "Additive", "Scale". */
    val roleLabel: String,
    val unit: String,
    /** Starting amount: original recipe amount, per-reference amount or unscaled batch amount. */
    val baseQty: Double,
    /** Final amount to weigh out. */
    val finalQty: Double,
    /** Amount already in the tub, in [unit]. */
    val inTubQty: Double = 0.0
) {
    val stillNeeded: Double get() = (finalQty - inTubQty).coerceAtLeast(0.0)
    val excess: Double get() = (inTubQty - finalQty).coerceAtLeast(0.0)
}

data class FormulaSummary(
    val method: EquationMode,
    val lines: List<FormulaLine>,
    val unit: String,
    val baseHeader: String,
    val totalBase: Double,
    val totalFinal: Double,
    val scaleFactor: Double,
    val steps: List<String>,
    val warnings: List<String>
) {
    val basisLabel: String get() = "Basis: ${method.label} (${EquationFormat.withUnit(totalFinal, unit)})"

    val hasInTub: Boolean get() = lines.any { it.inTubQty > 1e-9 }
    val totalStillNeeded: Double get() = lines.sumOf { it.stillNeeded }
    val totalExcess: Double get() = lines.sumOf { it.excess }

    fun share(line: FormulaLine): Double = if (totalFinal > 1e-9) line.finalQty / totalFinal * 100.0 else 0.0

    fun reportText(): String = buildString {
        appendLine("========================================")
        appendLine("    MIXTURE FORMULATION (${method.label.uppercase()})")
        appendLine("========================================")
        lines.forEach { l ->
            appendLine(
                "• ${l.name} [${l.roleLabel}]: ${baseHeader} ${EquationFormat.withUnit(l.baseQty, unit)} -> " +
                    "${EquationFormat.withUnit(l.finalQty, unit)} (${EquationFormat.percent(share(l))})"
            )
        }
        appendLine("----------------------------------------")
        appendLine("Total: ${EquationFormat.withUnit(totalFinal, unit)}")
        if (hasInTub) {
            appendLine("")
            appendLine("IN TUB vs REQUIRED")
            lines.forEach { l ->
                val action = when {
                    l.excess > 1e-6 -> "remove ${EquationFormat.withUnit(l.excess, unit)}"
                    l.stillNeeded > 1e-6 -> "add ${EquationFormat.withUnit(l.stillNeeded, unit)}"
                    else -> "complete"
                }
                appendLine("• ${l.name}: in tub ${EquationFormat.withUnit(l.inTubQty, unit)} -> $action")
            }
        }
        if (steps.isNotEmpty()) {
            appendLine("")
            steps.forEach { appendLine(it) }
        }
    }
}

data class FormulaOutcome(
    val recalibration: RecalibrationResult,
    val summary: FormulaSummary
)

object MixtureFormulaBridge {

    fun fromTargetTotal(
        result: TargetTotalResult,
        ingredients: List<TargetIngredient>,
        inTub: Map<String, Double> = emptyMap()
    ): FormulaOutcome? {
        if (!result.isValid) return null
        val lines = result.rows.map { row ->
            FormulaLine(row.id, row.name, row.role.label, result.resultUnit, row.baseQty, row.resultQty)
        }
        return outcome(
            method = EquationMode.TARGET_TOTAL,
            lines = lines,
            ingredients = ingredients,
            inTub = inTub,
            unit = result.resultUnit,
            baseHeader = "was",
            scaleFactor = result.scaleFactor,
            steps = result.steps,
            warnings = result.warnings
        )
    }

    fun fromReference(
        result: ReferenceResult,
        ingredients: List<TargetIngredient>,
        inTub: Map<String, Double> = emptyMap()
    ): FormulaOutcome? {
        if (!result.isValid) return null
        val lines = result.rows.map { row ->
            FormulaLine(row.id, row.name, row.role.label, result.resultUnit, row.perReferenceQty, row.fullScaleQty)
        }
        return outcome(
            method = EquationMode.PER_REFERENCE,
            lines = lines,
            ingredients = ingredients,
            inTub = inTub,
            unit = result.resultUnit,
            baseHeader = "per ref.",
            scaleFactor = result.scaleFactor,
            steps = result.steps,
            warnings = emptyList()
        )
    }

    fun fromBaseBatch(
        result: BaseBatchResult,
        ingredients: List<TargetIngredient>,
        inTub: Map<String, Double> = emptyMap()
    ): FormulaOutcome? {
        if (!result.isValid) return null
        val lines = result.rows.map { row ->
            FormulaLine(row.id, row.name, row.role.label, result.resultUnit, row.unscaledQty, row.finalQty)
        }
        return outcome(
            method = EquationMode.BASE_BATCH,
            lines = lines,
            ingredients = ingredients,
            inTub = inTub,
            unit = result.resultUnit,
            baseHeader = "x1",
            scaleFactor = result.multiplier,
            steps = result.steps,
            warnings = result.warnings
        )
    }

    private fun outcome(
        method: EquationMode,
        lines: List<FormulaLine>,
        ingredients: List<TargetIngredient>,
        inTub: Map<String, Double>,
        unit: String,
        baseHeader: String,
        scaleFactor: Double,
        steps: List<String>,
        warnings: List<String>
    ): FormulaOutcome {
        val withTub = lines.map { l ->
            val ing = ingredients.find { it.id == l.id }
            val raw = inTub[l.id] ?: 0.0
            val converted = when {
                raw <= 0.0 || ing == null -> 0.0
                MixtureUnits.areCompatible(ing.unit, unit) -> MixtureUnits.convert(raw, ing.unit, unit)
                else -> 0.0
            }
            l.copy(inTubQty = converted)
        }
        return buildOutcome(method, withTub, ingredients, unit, baseHeader, scaleFactor, steps, warnings)
    }

    private fun buildOutcome(
        method: EquationMode,
        lines: List<FormulaLine>,
        ingredients: List<TargetIngredient>,
        unit: String,
        baseHeader: String,
        scaleFactor: Double,
        steps: List<String>,
        warnings: List<String>
    ): FormulaOutcome {
        val totalFinal = lines.sumOf { it.finalQty }
        val totalBase = lines.sumOf { it.baseQty }
        val summary = FormulaSummary(
            method = method,
            lines = lines,
            unit = unit,
            baseHeader = baseHeader,
            totalBase = totalBase,
            totalFinal = totalFinal,
            scaleFactor = scaleFactor,
            steps = steps,
            warnings = warnings
        )
        return FormulaOutcome(toRecalibration(summary, ingredients), summary)
    }

    /** Builds the calculator's result type from solved lines, including what is already in the tub. */
    fun toRecalibration(summary: FormulaSummary, ingredients: List<TargetIngredient>): RecalibrationResult {
        val unit = summary.unit
        val rows = summary.lines.map { l ->
            val delta = l.finalQty - l.baseQty
            ProportionRow(
                ingredientId = l.id,
                ingredientName = l.name,
                unit = unit,
                targetQty = l.baseQty,
                newRequiredTotal = l.finalQty,
                alreadyAdded = l.inTubQty,
                stillNeeded = l.stillNeeded,
                excessAmount = l.excess,
                reductionFromOriginal = (l.baseQty - l.finalQty).coerceAtLeast(0.0),
                isAnchor = false,
                isLimitingFactor = false,
                formattedTarget = EquationFormat.withUnit(l.baseQty, unit),
                formattedNewTotal = EquationFormat.withUnit(l.finalQty, unit),
                formattedAlreadyAdded = EquationFormat.withUnit(l.inTubQty, unit),
                formattedStillNeeded = EquationFormat.withUnit(l.stillNeeded, unit),
                formattedExcess = EquationFormat.withUnit(l.excess, unit),
                formattedReductionFromOriginal = EquationFormat.withUnit((l.baseQty - l.finalQty).coerceAtLeast(0.0), unit),
                suggestedAdjustmentDelta = delta,
                formattedSuggestedAdjustmentDelta = EquationFormat.withUnit(delta, unit)
            )
        }
        val biggest = rows.maxByOrNull { it.newRequiredTotal }
        val anchorId = biggest?.ingredientId ?: ingredients.firstOrNull()?.id ?: ""
        val marked = rows.map { it.copy(isAnchor = it.ingredientId == anchorId) }
        val total = marked.sumOf { it.newRequiredTotal }
        val formattedTotal = EquationFormat.withUnit(total, unit)
        val factorText = EquationFormat.factor(summary.scaleFactor)
        val explanation = "${summary.method.label}: ${marked.size} ingredients solved to a " +
            "total of $formattedTotal (scale $factorText)."

        return RecalibrationResult(
            anchorIngredientId = anchorId,
            anchorIngredientName = biggest?.ingredientName ?: "None",
            anchorTargetQty = biggest?.targetQty ?: 0.0,
            anchorActualAdded = biggest?.newRequiredTotal ?: 0.0,
            scaleFactor = summary.scaleFactor,
            formattedScaleFactor = factorText,
            scaleExplanation = explanation,
            proportions = marked,
            totalNewMixtureWeight = total,
            formattedTotalNewWeight = formattedTotal,
            adjustmentMode = MixtureAdjustmentMode.ADD_TO_BALANCE,
            strictAnchorScaleFactor = summary.scaleFactor,
            formattedStrictAnchorScaleFactor = factorText,
            hasExcessInTub = summary.totalExcess > 1e-6,
            totalExcessWeight = summary.totalExcess,
            formattedTotalExcessWeight = EquationFormat.withUnit(summary.totalExcess, unit),
            totalStillNeededWeight = summary.totalStillNeeded,
            formattedTotalStillNeededWeight = EquationFormat.withUnit(summary.totalStillNeeded, unit),
            limitingIngredientName = biggest?.ingredientName ?: "None",
            lockedInitialTotalWeight = total,
            formattedLockedInitialTotalWeight = formattedTotal,
            constantWeightProportions = marked,
            addModeProportions = marked,
            reduceModeProportions = marked,
            addModeTotalWeight = total,
            reduceModeTotalWeight = total,
            formattedAddModeTotalWeight = formattedTotal,
            formattedReduceModeTotalWeight = formattedTotal
        )
    }
}

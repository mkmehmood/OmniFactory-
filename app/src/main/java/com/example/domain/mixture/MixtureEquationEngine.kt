package com.example.domain.mixture

import java.util.Locale
import kotlin.math.abs

/**
 * Equation solver for the Mixture Calculator.
 *
 * Covers two families of problems that the anchor-based recalibration cannot express:
 *
 * 1. TARGET TOTAL  - "The whole batch must weigh T. Keep some components fixed, remove some,
 *    and split the remainder across the rest using their original ratio."
 *    e.g. total = 2.3 kg, C = 400 g and D = 150 g fixed, A:B = 80:10 share the rest.
 *
 * 2. PER REFERENCE - "C and D are specified per R of the main mix (A+B). The main mix is
 *    actually M, so scale C and D by M / R."
 *    e.g. A = 80, B = 10 (kg), C = 400 g and D = 150 g per 2.3 kg of A+B.
 */

enum class EquationMode(val label: String, val hint: String) {
    ANCHOR(
        "Anchor Scale",
        "Calibrate to what is already in the tub: pick an anchor ingredient and its actual quantity."
    ),
    TARGET_TOTAL(
        "Target Total",
        "The whole batch must weigh a set amount. Lock or remove ingredients; the rest share it by ratio."
    ),
    PER_REFERENCE(
        "Per Reference",
        "Some amounts are quoted per a reference weight of the main mix. Scale them to the real mix."
    ),
    BASE_BATCH(
        "Base + Additives",
        "Ratio items share a base weight, fixed items sit inside it, additives go on top, then a batch multiplier."
    )
}

/** Role of a component in BASE_BATCH mode. */
enum class BatchRole(val label: String) {
    /** Shares the remaining base weight with other proportional items by ratio. */
    PROPORTIONAL("Ratio"),
    /** Absolute weight that occupies space inside the base target weight. */
    FIXED_BASE("Fixed"),
    /** Absolute weight added on top of the base, outside the base target. */
    ADDITIVE("Additive")
}

/** Role of a component in TARGET_TOTAL mode. */
enum class ComponentRole(val label: String) {
    SCALE("Scale"),
    FIXED("Fixed"),
    REMOVED("Removed")
}

/** Role of a component in PER_REFERENCE mode. */
enum class ReferenceRole(val label: String) {
    MAIN("Main mix"),
    DEPENDENT("Per ref.")
}

enum class UnitFamily { MASS, VOLUME, OTHER }

object MixtureUnits {

    private data class UnitDef(val family: UnitFamily, val toBase: Double)

    // Base for MASS is grams, base for VOLUME is millilitres.
    private val table: Map<String, UnitDef> = mapOf(
        "mg" to UnitDef(UnitFamily.MASS, 0.001),
        "g" to UnitDef(UnitFamily.MASS, 1.0),
        "gm" to UnitDef(UnitFamily.MASS, 1.0),
        "gram" to UnitDef(UnitFamily.MASS, 1.0),
        "grams" to UnitDef(UnitFamily.MASS, 1.0),
        "kg" to UnitDef(UnitFamily.MASS, 1000.0),
        "kgs" to UnitDef(UnitFamily.MASS, 1000.0),
        "t" to UnitDef(UnitFamily.MASS, 1_000_000.0),
        "ton" to UnitDef(UnitFamily.MASS, 1_000_000.0),
        "tonne" to UnitDef(UnitFamily.MASS, 1_000_000.0),
        "lb" to UnitDef(UnitFamily.MASS, 453.59237),
        "lbs" to UnitDef(UnitFamily.MASS, 453.59237),
        "oz" to UnitDef(UnitFamily.MASS, 28.349523125),
        "ml" to UnitDef(UnitFamily.VOLUME, 1.0),
        "cl" to UnitDef(UnitFamily.VOLUME, 10.0),
        "l" to UnitDef(UnitFamily.VOLUME, 1000.0),
        "lt" to UnitDef(UnitFamily.VOLUME, 1000.0),
        "ltr" to UnitDef(UnitFamily.VOLUME, 1000.0),
        "liter" to UnitDef(UnitFamily.VOLUME, 1000.0),
        "litre" to UnitDef(UnitFamily.VOLUME, 1000.0)
    )

    private fun key(unit: String) = unit.trim().lowercase(Locale.US)

    fun familyOf(unit: String): UnitFamily = table[key(unit)]?.family ?: UnitFamily.OTHER

    /** True if both units can be converted into each other. Identical unknown units are compatible. */
    fun areCompatible(a: String, b: String): Boolean {
        val fa = familyOf(a)
        val fb = familyOf(b)
        return if (fa == UnitFamily.OTHER || fb == UnitFamily.OTHER) key(a) == key(b) else fa == fb
    }

    /** Converts [value] from [from] to [to]. Caller must check [areCompatible] first. */
    fun convert(value: Double, from: String, to: String): Double {
        if (key(from) == key(to)) return value
        val f = table[key(from)] ?: return value
        val t = table[key(to)] ?: return value
        return value * f.toBase / t.toBase
    }
}

// ---------------------------------------------------------------------------------------------
// TARGET TOTAL
// ---------------------------------------------------------------------------------------------

data class EquationComponent(
    val id: String,
    val name: String,
    val baseQty: Double,
    val unit: String,
    val role: ComponentRole = ComponentRole.SCALE,
    /** Used only when role == FIXED, expressed in [fixedUnit]. */
    val fixedQty: Double? = null,
    val fixedUnit: String = unit
)

data class EquationRow(
    val id: String,
    val name: String,
    val role: ComponentRole,
    /** Original recipe amount converted to the result unit. */
    val baseQty: Double,
    /** Final amount in the result unit. */
    val resultQty: Double,
    val sharePercent: Double,
    val formattedBase: String,
    val formattedResult: String,
    val formattedShare: String
)

data class TargetTotalResult(
    val isValid: Boolean,
    val errorMessage: String?,
    val warnings: List<String>,
    val rows: List<EquationRow>,
    val targetTotal: Double,
    val achievedTotal: Double,
    val resultUnit: String,
    val fixedTotal: Double,
    val scaleBaseTotal: Double,
    val scaleTargetTotal: Double,
    /** Multiplier applied to every SCALE component. */
    val scaleFactor: Double,
    val steps: List<String>
) {
    val formattedTarget: String get() = EquationFormat.withUnit(targetTotal, resultUnit)
    val formattedAchieved: String get() = EquationFormat.withUnit(achievedTotal, resultUnit)
}

// ---------------------------------------------------------------------------------------------
// PER REFERENCE
// ---------------------------------------------------------------------------------------------

data class ReferenceComponent(
    val id: String,
    val name: String,
    val qty: Double,
    val unit: String,
    val role: ReferenceRole
)

data class ReferenceRow(
    val id: String,
    val name: String,
    val role: ReferenceRole,
    /** Amount at the full main-mix size (e.g. 90 kg of A+B, 15.65 kg of C). */
    val fullScaleQty: Double,
    /** Amount inside one reference portion (e.g. 2,044.4 g of A, 400 g of C). */
    val perReferenceQty: Double,
    val formattedFullScale: String,
    val formattedPerReference: String
)

data class ReferenceResult(
    val isValid: Boolean,
    val errorMessage: String?,
    val rows: List<ReferenceRow>,
    val mainTotal: Double,
    val referenceWeight: Double,
    val scaleFactor: Double,
    val resultUnit: String,
    val steps: List<String>
) {
    /** One reference portion expressed as a complete recipe, ready to be reused as a base recipe. */
    fun perReferenceRecipe(): List<Pair<String, Double>> = rows.map { it.name to it.perReferenceQty }
}

// ---------------------------------------------------------------------------------------------
// BASE BATCH  (proportional + fixed base + additives, then a global multiplier)
// ---------------------------------------------------------------------------------------------

data class BatchComponent(
    val id: String,
    val name: String,
    /** Ratio coefficient for PROPORTIONAL, absolute weight for FIXED_BASE and ADDITIVE. */
    val qty: Double,
    val unit: String,
    val role: BatchRole
)

data class BatchRow(
    val id: String,
    val name: String,
    val role: BatchRole,
    /** Weight inside the unscaled batch (multiplier = 1). */
    val unscaledQty: Double,
    /** Weight after applying the multiplier. */
    val finalQty: Double,
    val sharePercent: Double,
    val formattedUnscaled: String,
    val formattedFinal: String,
    val formattedShare: String
)

data class BaseBatchResult(
    val isValid: Boolean,
    val errorMessage: String?,
    val warnings: List<String>,
    val rows: List<BatchRow>,
    val baseTarget: Double,
    val fixedTotal: Double,
    val remainingForRatio: Double,
    val additiveTotal: Double,
    val unscaledTotal: Double,
    val multiplier: Double,
    val finalTotal: Double,
    val resultUnit: String,
    val steps: List<String>
) {
    val formattedUnscaledTotal: String get() = EquationFormat.withUnit(unscaledTotal, resultUnit)
    val formattedFinalTotal: String get() = EquationFormat.withUnit(finalTotal, resultUnit)
}

object EquationFormat {
    fun number(value: Double): String {
        val v = if (abs(value) < 0.0000005) 0.0 else value
        return when {
            v % 1.0 == 0.0 -> String.format(Locale.US, "%,.0f", v)
            abs(v) >= 100 -> String.format(Locale.US, "%,.2f", v)
            else -> String.format(Locale.US, "%,.3f", v).trimEnd('0').trimEnd('.')
        }
    }

    fun withUnit(value: Double, unit: String): String = "${number(value)} $unit"

    fun percent(value: Double): String = String.format(Locale.US, "%.2f%%", value)

    fun factor(value: Double): String = String.format(Locale.US, "%.4fx", value)
}

object MixtureEquationEngine {

    private const val EPS = 1e-9

    // -----------------------------------------------------------------------------------------
    // TARGET TOTAL
    // -----------------------------------------------------------------------------------------

    /**
     * Solves for a batch of exactly [targetTotal] (expressed in [targetUnit]).
     *
     * - REMOVED components contribute 0.
     * - FIXED components keep their [EquationComponent.fixedQty] (falls back to base amount).
     * - SCALE components share (targetTotal - fixedTotal) in proportion to their base amounts.
     *
     * Everything is converted to [targetUnit] first, so mixed units (kg and g) are handled.
     */
    fun solveTargetTotal(
        components: List<EquationComponent>,
        targetTotal: Double,
        targetUnit: String
    ): TargetTotalResult {
        val unit = targetUnit.trim().ifEmpty { "kg" }

        fun invalid(message: String, steps: List<String> = emptyList()) = TargetTotalResult(
            isValid = false,
            errorMessage = message,
            warnings = emptyList(),
            rows = emptyList(),
            targetTotal = targetTotal,
            achievedTotal = 0.0,
            resultUnit = unit,
            fixedTotal = 0.0,
            scaleBaseTotal = 0.0,
            scaleTargetTotal = 0.0,
            scaleFactor = 0.0,
            steps = steps
        )

        if (components.isEmpty()) return invalid("Add at least one ingredient first.")
        if (targetTotal.isNaN() || targetTotal <= 0.0) return invalid("Enter a target total greater than 0.")

        for (c in components) {
            if (!MixtureUnits.areCompatible(c.unit, unit)) {
                return invalid("'${c.name}' is in ${c.unit}, which cannot be converted to $unit.")
            }
            if (c.role == ComponentRole.FIXED && !MixtureUnits.areCompatible(c.fixedUnit, unit)) {
                return invalid("Fixed amount of '${c.name}' is in ${c.fixedUnit}, which cannot be converted to $unit.")
            }
        }

        val bases = components.associate { it.id to MixtureUnits.convert(it.baseQty, it.unit, unit) }
        val fixedAmounts = components
            .filter { it.role == ComponentRole.FIXED }
            .associate { c ->
                val raw = c.fixedQty
                val converted = if (raw != null) MixtureUnits.convert(raw, c.fixedUnit, unit) else bases.getValue(c.id)
                c.id to converted.coerceAtLeast(0.0)
            }

        val fixedTotal = fixedAmounts.values.sum()
        val scaleComponents = components.filter { it.role == ComponentRole.SCALE }
        val scaleBaseTotal = scaleComponents.sumOf { bases.getValue(it.id) }
        val remaining = targetTotal - fixedTotal

        val steps = mutableListOf<String>()
        steps += "Target total = ${EquationFormat.withUnit(targetTotal, unit)}"
        steps += "Fixed total = ${EquationFormat.withUnit(fixedTotal, unit)}"
        steps += "Left for scaled components = ${EquationFormat.number(targetTotal)} - ${EquationFormat.number(fixedTotal)} = ${EquationFormat.withUnit(remaining, unit)}"

        if (remaining < -EPS) {
            return invalid(
                "Fixed amounts (${EquationFormat.withUnit(fixedTotal, unit)}) exceed the target total " +
                    "by ${EquationFormat.withUnit(-remaining, unit)}.",
                steps
            )
        }

        if (scaleComponents.isEmpty()) {
            if (abs(remaining) > 1e-6) {
                return invalid(
                    "Nothing is set to Scale, so ${EquationFormat.withUnit(remaining, unit)} of the target " +
                        "cannot be allocated. Set at least one ingredient to Scale or adjust the target.",
                    steps
                )
            }
        } else if (scaleBaseTotal <= EPS) {
            return invalid("Scaled ingredients have no base amount to split the remainder by.", steps)
        }

        val factor = if (scaleComponents.isEmpty()) 0.0 else remaining / scaleBaseTotal
        if (scaleComponents.isNotEmpty()) {
            steps += "Scale factor = ${EquationFormat.number(remaining)} / ${EquationFormat.number(scaleBaseTotal)} = ${EquationFormat.factor(factor)}"
        }

        val results = components.associate { c ->
            val value = when (c.role) {
                ComponentRole.REMOVED -> 0.0
                ComponentRole.FIXED -> fixedAmounts.getValue(c.id)
                ComponentRole.SCALE -> bases.getValue(c.id) * factor
            }
            c.id to value
        }
        val achieved = results.values.sum()

        val rows = components.map { c ->
            val result = results.getValue(c.id)
            val share = if (achieved > EPS) result / achieved * 100.0 else 0.0
            EquationRow(
                id = c.id,
                name = c.name,
                role = c.role,
                baseQty = bases.getValue(c.id),
                resultQty = result,
                sharePercent = share,
                formattedBase = EquationFormat.withUnit(bases.getValue(c.id), unit),
                formattedResult = EquationFormat.withUnit(result, unit),
                formattedShare = EquationFormat.percent(share)
            )
        }

        val warnings = mutableListOf<String>()
        val scaledOnly = scaleComponents.size
        if (scaledOnly >= 1 && components.any { it.role == ComponentRole.REMOVED }) {
            warnings += "A removed ingredient's share was redistributed to: " +
                scaleComponents.joinToString { it.name } + "."
        }
        if (scaledOnly == 1 && components.size - scaledOnly > 1) {
            warnings += "'${scaleComponents.first().name}' absorbs the entire remainder because it is the only Scale ingredient."
        }
        if (abs(achieved - targetTotal) > 1e-6) {
            warnings += "Total ${EquationFormat.withUnit(achieved, unit)} differs from target ${EquationFormat.withUnit(targetTotal, unit)}."
        }

        return TargetTotalResult(
            isValid = true,
            errorMessage = null,
            warnings = warnings,
            rows = rows,
            targetTotal = targetTotal,
            achievedTotal = achieved,
            resultUnit = unit,
            fixedTotal = fixedTotal,
            scaleBaseTotal = scaleBaseTotal,
            scaleTargetTotal = remaining.coerceAtLeast(0.0),
            scaleFactor = factor,
            steps = steps
        )
    }

    // -----------------------------------------------------------------------------------------
    // PER REFERENCE
    // -----------------------------------------------------------------------------------------

    /**
     * MAIN components are the actual mix (e.g. A = 80 kg, B = 10 kg).
     * DEPENDENT components are quoted per [referenceWeight] of the main mix
     * (e.g. C = 400 g and D = 150 g per 2.3 kg of A+B).
     *
     * Dependents scale by (main total / reference weight). The result also contains the
     * per-reference split of the main mix, which is the recipe for one reference portion.
     */
    fun solvePerReference(
        components: List<ReferenceComponent>,
        referenceWeight: Double,
        referenceUnit: String,
        resultUnit: String = referenceUnit
    ): ReferenceResult {
        val rUnit = resultUnit.trim().ifEmpty { referenceUnit.trim().ifEmpty { "kg" } }
        val refUnit = referenceUnit.trim().ifEmpty { "kg" }

        fun invalid(message: String) = ReferenceResult(
            isValid = false,
            errorMessage = message,
            rows = emptyList(),
            mainTotal = 0.0,
            referenceWeight = referenceWeight,
            scaleFactor = 0.0,
            resultUnit = rUnit,
            steps = emptyList()
        )

        val main = components.filter { it.role == ReferenceRole.MAIN && it.qty > 0.0 }
        val dependents = components.filter { it.role == ReferenceRole.DEPENDENT }

        if (main.isEmpty()) return invalid("Mark at least one ingredient as Main mix.")
        if (referenceWeight.isNaN() || referenceWeight <= 0.0) return invalid("Enter a reference weight greater than 0.")
        if (!MixtureUnits.areCompatible(refUnit, rUnit)) return invalid("Reference unit $refUnit cannot be converted to $rUnit.")
        for (c in components) {
            if (!MixtureUnits.areCompatible(c.unit, rUnit)) {
                return invalid("'${c.name}' is in ${c.unit}, which cannot be converted to $rUnit.")
            }
        }

        val mainTotal = main.sumOf { MixtureUnits.convert(it.qty, it.unit, rUnit) }
        val refInResult = MixtureUnits.convert(referenceWeight, refUnit, rUnit)
        val factor = mainTotal / refInResult

        val steps = mutableListOf<String>()
        steps += "Main mix total = ${EquationFormat.withUnit(mainTotal, rUnit)}"
        steps += "Reference portion = ${EquationFormat.withUnit(refInResult, rUnit)}"
        steps += "Scale factor = ${EquationFormat.number(mainTotal)} / ${EquationFormat.number(refInResult)} = ${EquationFormat.factor(factor)}"

        val rows = components.map { c ->
            val qty = MixtureUnits.convert(c.qty, c.unit, rUnit)
            val full: Double
            val perRef: Double
            if (c.role == ReferenceRole.MAIN) {
                full = qty
                perRef = if (mainTotal > EPS) qty / mainTotal * refInResult else 0.0
            } else {
                perRef = qty
                full = qty * factor
            }
            ReferenceRow(
                id = c.id,
                name = c.name,
                role = c.role,
                fullScaleQty = full,
                perReferenceQty = perRef,
                formattedFullScale = EquationFormat.withUnit(full, rUnit),
                formattedPerReference = EquationFormat.withUnit(perRef, rUnit)
            )
        }

        if (dependents.isEmpty()) {
            steps += "No per-reference components set; showing the main mix split only."
        }

        return ReferenceResult(
            isValid = true,
            errorMessage = null,
            rows = rows,
            mainTotal = mainTotal,
            referenceWeight = refInResult,
            scaleFactor = factor,
            resultUnit = rUnit,
            steps = steps
        )
    }

    // -----------------------------------------------------------------------------------------
    // BASE BATCH
    // -----------------------------------------------------------------------------------------

    /**
     *   Fixed_Total   = SUM(FIXED_BASE)
     *   W_remain      = baseTarget - Fixed_Total
     *   W_j           = W_remain * ratio_j / SUM(ratios)        (PROPORTIONAL)
     *   Unscaled      = baseTarget + SUM(ADDITIVE)
     *   Final         = item * multiplier
     *
     * All amounts are converted to [resultUnit] first, so mixed kg / g input works.
     */
    fun solveBaseBatch(
        components: List<BatchComponent>,
        baseTarget: Double,
        baseUnit: String,
        multiplier: Double = 1.0
    ): BaseBatchResult {
        val unit = baseUnit.trim().ifEmpty { "kg" }

        fun invalid(message: String, steps: List<String> = emptyList()) = BaseBatchResult(
            isValid = false, errorMessage = message, warnings = emptyList(), rows = emptyList(),
            baseTarget = baseTarget, fixedTotal = 0.0, remainingForRatio = 0.0, additiveTotal = 0.0,
            unscaledTotal = 0.0, multiplier = multiplier, finalTotal = 0.0, resultUnit = unit, steps = steps
        )

        if (components.isEmpty()) return invalid("Add at least one ingredient first.")
        if (baseTarget.isNaN() || baseTarget <= 0.0) return invalid("Enter a base target weight greater than 0.")
        if (multiplier.isNaN() || multiplier <= 0.0) return invalid("Enter a multiplier greater than 0.")
        for (c in components) {
            if (!MixtureUnits.areCompatible(c.unit, unit)) {
                return invalid("'${c.name}' is in ${c.unit}, which cannot be converted to $unit.")
            }
            if (c.qty < 0.0 || c.qty.isNaN()) return invalid("'${c.name}' has an invalid amount.")
        }

        val amounts = components.associate { it.id to MixtureUnits.convert(it.qty, it.unit, unit) }
        val fixed = components.filter { it.role == BatchRole.FIXED_BASE }
        val ratio = components.filter { it.role == BatchRole.PROPORTIONAL }
        val additive = components.filter { it.role == BatchRole.ADDITIVE }

        val fixedTotal = fixed.sumOf { amounts.getValue(it.id) }
        val remaining = baseTarget - fixedTotal
        val ratioSum = ratio.sumOf { c -> c.qty }
        val additiveTotal = additive.sumOf { amounts.getValue(it.id) }

        val steps = mutableListOf<String>()
        steps += "Fixed total = ${EquationFormat.withUnit(fixedTotal, unit)}"
        steps += "Remaining for ratio items = ${EquationFormat.number(baseTarget)} - ${EquationFormat.number(fixedTotal)} = ${EquationFormat.withUnit(remaining, unit)}"

        if (remaining < -EPS) {
            return invalid(
                "Fixed items (${EquationFormat.withUnit(fixedTotal, unit)}) exceed the base target " +
                    "by ${EquationFormat.withUnit(-remaining, unit)}.",
                steps
            )
        }
        if (ratio.isEmpty()) {
            if (abs(remaining) > 1e-6) {
                return invalid(
                    "No Ratio items, so ${EquationFormat.withUnit(remaining, unit)} of the base cannot be allocated. " +
                        "Mark an item as Ratio or change the base target.",
                    steps
                )
            }
        } else if (ratioSum <= EPS) {
            return invalid("Ratio items need coefficients greater than 0.", steps)
        }

        val unscaled = components.associate { c ->
            val v = when (c.role) {
                BatchRole.PROPORTIONAL -> remaining * (c.qty / ratioSum)
                BatchRole.FIXED_BASE, BatchRole.ADDITIVE -> amounts.getValue(c.id)
            }
            c.id to v
        }
        val unscaledTotal = baseTarget + additiveTotal
        val finalTotal = unscaledTotal * multiplier

        if (ratio.isNotEmpty()) {
            steps += "Ratio parts = ${ratio.joinToString(" : ") { EquationFormat.number(it.qty) }}"
        }
        steps += "Unscaled total = ${EquationFormat.number(baseTarget)} + ${EquationFormat.number(additiveTotal)} = ${EquationFormat.withUnit(unscaledTotal, unit)}"
        steps += "Final total = ${EquationFormat.number(unscaledTotal)} x ${EquationFormat.number(multiplier)} = ${EquationFormat.withUnit(finalTotal, unit)}"

        val rows = components.map { c ->
            val u = unscaled.getValue(c.id)
            val f = u * multiplier
            val share = if (unscaledTotal > EPS) u / unscaledTotal * 100.0 else 0.0
            BatchRow(
                id = c.id, name = c.name, role = c.role,
                unscaledQty = u, finalQty = f, sharePercent = share,
                formattedUnscaled = EquationFormat.withUnit(u, unit),
                formattedFinal = EquationFormat.withUnit(f, unit),
                formattedShare = EquationFormat.percent(share)
            )
        }

        val warnings = mutableListOf<String>()
        val sum = rows.sumOf { it.unscaledQty }
        if (abs(sum - unscaledTotal) > 1e-6) {
            warnings += "Item weights add up to ${EquationFormat.withUnit(sum, unit)}, not ${EquationFormat.withUnit(unscaledTotal, unit)}."
        }

        return BaseBatchResult(
            isValid = true, errorMessage = null, warnings = warnings, rows = rows,
            baseTarget = baseTarget, fixedTotal = fixedTotal, remainingForRatio = remaining,
            additiveTotal = additiveTotal, unscaledTotal = unscaledTotal, multiplier = multiplier,
            finalTotal = finalTotal, resultUnit = unit, steps = steps
        )
    }
}

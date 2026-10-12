package com.example.domain.mixture

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.max

/**
 * Native engine for the Mixture Calculator (Formula & Mixture -> Re-balance -> Report).
 *
 * Pure Kotlin, no Android dependencies. Every stored weight is in kg; text the person types is in
 * the currently selected [CalcUnit] and converted with [CalcUnit.factor].
 */

enum class CalcUnit(val label: String, val factor: Double) {
    KG("kg", 1.0),
    G("g", 0.001);

    val other: CalcUnit get() = if (this == KG) G else KG

    companion object {
        fun fromKey(key: String?): CalcUnit = if (key == "g") G else KG
    }
}

enum class CalcMode(val key: String) {
    /** Entered amounts describe ONE base unit; scale up to a large batch. */
    UNIT_BASED("UNIT_BASED"),

    /** Entered amounts describe the WHOLE batch; derive the per-unit formula. */
    BATCH_EXTRACT("BATCH_EXTRACT");

    companion object {
        fun fromKey(key: String?): CalcMode = if (key == "BATCH_EXTRACT") BATCH_EXTRACT else UNIT_BASED
    }
}

enum class CalcIngType(val key: String, val label: String) {
    FIXED_BASE("FIXED_BASE", "Fixed"),
    ADDITIVE("ADDITIVE", "Additive");

    companion object {
        /** Older saved data may contain PROPORTIONAL; it is read as FIXED_BASE, as before. */
        fun fromKey(key: String?): CalcIngType = if (key == "ADDITIVE") ADDITIVE else FIXED_BASE
    }
}

/** One editable ingredient row. [valueText] is typed in the current unit. */
data class CalcRow(
    val id: Int,
    val name: String = "",
    val type: CalcIngType = CalcIngType.FIXED_BASE,
    val valueText: String = ""
)

/** Everything the person typed. The derived numbers come from [FormulaCalcEngine.derive]. */
data class CalcInputs(
    val unit: CalcUnit = CalcUnit.KG,
    val mode: CalcMode = CalcMode.UNIT_BASED,
    val baseText: String = "",
    val capText: String = "",
    val multText: String = "",
    val origText: String = "",
    val usedText: String = "",
    val rows: List<CalcRow> = listOf(CalcRow(1)),
    /** True once the person edits the Re-balance target formula; it then stops following the formula. */
    val targetDirty: Boolean = false,
    /** Target amounts (current unit text) by ingredient id; only used while [targetDirty]. */
    val targetTexts: Map<Int, String> = emptyMap()
)

/** An ingredient with its amount resolved to kg (after auto-fill). */
data class CalcIngredient(
    val id: Int,
    val name: String,
    val type: CalcIngType,
    val valueKg: Double
) {
    val isBlank: Boolean get() = name.isBlank()
}

data class CalcAutoFill(
    val id: Int,
    val name: String,
    val kg: Double,
    val totalKg: Double,
    val othersKg: Double
)

data class CalcBatchRow(
    val id: Int,
    val name: String,
    val type: CalcIngType,
    val unitKg: Double,
    val finalKg: Double
)

data class CalcBatch(
    val rows: List<CalcBatchRow> = emptyList(),
    val grandTotalKg: Double = 0.0,
    val multiplier: Double = 1.0,
    val unitTotalKg: Double = 0.0,
    val baseKg: Double = 0.0,
    /** True when the multiplier was derived from a target batch weight. */
    val multiplierFromCap: Boolean = false
) {
    val unitsCount: Double get() = if (baseKg > 0) grandTotalKg / baseKg else 0.0
}

data class CalcCorrectionRow(
    val id: Int,
    val name: String,
    val remKg: Double,
    val targetKg: Double,
    val addKg: Double,
    val editable: Boolean,
    /** Target amount per base unit; used to rescale when an Add box is edited. */
    val targetUnitKg: Double
)

data class CalcCorrection(
    val origKg: Double = 0.0,
    val usedKg: Double = 0.0,
    val remKg: Double = 0.0,
    val newTotalKg: Double = 0.0,
    val addTotalKg: Double = 0.0,
    val remTotalKg: Double = 0.0,
    val manual: Boolean = false,
    val rows: List<CalcCorrectionRow> = emptyList(),
    /** False when there is no remaining batch / no formula yet, so nothing can be corrected. */
    val active: Boolean = false
) {
    fun addFor(id: Int): Double = rows.firstOrNull { it.id == id }?.addKg ?: 0.0
}

data class CalcDerived(
    val auto: CalcAutoFill? = null,
    val effective: List<CalcIngredient> = emptyList(),
    val target: List<CalcIngredient> = emptyList(),
    val batch: CalcBatch = CalcBatch(),
    val correction: CalcCorrection = CalcCorrection(),
    /** Fingerprint of the inputs a manual correction scale belongs to. */
    val signature: String? = null,
    /** The manual scale that is still valid for [signature], or null once the inputs changed. */
    val manualScale: Double? = null
)

object FormulaCalcEngine {

    val DEFAULT_NAMES: List<String> = listOf(
        "کیمیکل", "برلنزہ", "تمن", "چونا", "پوڈر", "مالٹے رنگ", "اجمل سفید", "سفید", "سبز", "فرمان"
    )

    private const val LINE = "------------------------------"

    // ---------------------------------------------------------------- number helpers

    /** Lenient parse: blank / invalid -> 0. Accepts a comma as the decimal separator. */
    fun parseNum(text: String): Double {
        val v = text.trim().replace(',', '.').toDoubleOrNull() ?: return 0.0
        return if (v.isNaN() || v.isInfinite()) 0.0 else v
    }

    /** Keeps digits and a single dot so a number field can never hold junk. */
    fun sanitizeNumeric(raw: String): String {
        val sb = StringBuilder()
        var dot = false
        for (c in raw) {
            when {
                c.isDigit() -> sb.append(c)
                (c == '.' || c == ',') && !dot -> { sb.append('.'); dot = true }
            }
        }
        return sb.toString()
    }

    private fun plain(bd: BigDecimal): String {
        if (bd.signum() == 0) return "0"
        return bd.stripTrailingZeros().toPlainString()
    }

    /** kg -> text for an input box in [unit] (10 significant digits, no trailing zeros, "" for 0). */
    fun toDisp(kg: Double, unit: CalcUnit): String {
        if (!(kg > 0) || kg.isNaN() || kg.isInfinite()) return ""
        return plain(BigDecimal(kg / unit.factor).round(MathContext(10)))
    }

    /** Re-express typed text in another unit; blank / invalid text is left alone. */
    fun convertText(text: String, from: CalcUnit, to: CalcUnit): String {
        val v = text.trim().replace(',', '.').toDoubleOrNull() ?: return text
        val kg = v * from.factor
        if (kg == 0.0) return text
        return plain(BigDecimal(kg / to.factor).round(MathContext(10)))
    }

    /** Text for an "Add" box: kg -> 3 decimals, g -> 1 decimal, trailing zeros removed. */
    fun addInputText(kg: Double, unit: CalcUnit): String {
        val scale = if (unit == CalcUnit.KG) 3 else 1
        val v = BigDecimal(kg / unit.factor).setScale(scale, RoundingMode.HALF_UP)
        return plain(v)
    }

    private val usSymbols = DecimalFormatSymbols(Locale.US)

    private fun grams(kg: Double, maxFraction: Int): String {
        val pattern = if (maxFraction == 0) "#,##0" else "#,##0." + "#".repeat(maxFraction)
        return DecimalFormat(pattern, usSymbols).format(kg * 1000)
    }

    /** "1.250 kg" or "1,250 g" depending on the unit. */
    fun fmt(kg: Double, unit: CalcUnit): String =
        if (unit == CalcUnit.KG) String.format(Locale.US, "%.3f kg", kg) else "${grams(kg, 1)} g"

    /** The same weight in the OTHER unit, compact: "1,250g" when kg is selected, "1.250kg" for g. */
    fun fmtAlt(kg: Double, unit: CalcUnit): String =
        if (unit == CalcUnit.KG) "${grams(kg, 0)}g" else String.format(Locale.US, "%.3fkg", kg)

    fun fmtBoth(kg: Double): String =
        String.format(Locale.US, "%.3f kg (%s g)", kg, grams(kg, 0))

    fun fmtMultiplier(m: Double): String = String.format(Locale.US, "%.3fx", m)

    // ---------------------------------------------------------------- ingredient resolution

    private fun rowKg(row: CalcRow, unit: CalcUnit): Double = parseNum(row.valueText) * unit.factor

    /**
     * When exactly one named ingredient is left at zero it is filled so the total matches the base
     * unit (unit mode) or the target batch (batch mode).
     */
    fun computeAutoFill(inputs: CalcInputs): CalcAutoFill? {
        val f = inputs.unit.factor
        val active = inputs.rows.filter { it.name.isNotBlank() }
        val zeros = active.filter { !(rowKg(it, inputs.unit) > 0) }
        if (zeros.size != 1) return null
        val total = (if (inputs.mode == CalcMode.UNIT_BASED) parseNum(inputs.baseText) else parseNum(inputs.capText)) * f
        if (!(total > 0)) return null
        val sum = active.sumOf { val v = rowKg(it, inputs.unit); if (v > 0) v else 0.0 }
        val z = zeros.first()
        return CalcAutoFill(z.id, z.name, max(0.0, total - sum), total, sum)
    }

    fun effectiveIngredients(inputs: CalcInputs, auto: CalcAutoFill?): List<CalcIngredient> =
        inputs.rows.map { r ->
            val kg = if (auto != null && auto.id == r.id) auto.kg else rowKg(r, inputs.unit)
            CalcIngredient(r.id, r.name, r.type, if (kg > 0) kg else 0.0)
        }

    private fun targetIngredients(inputs: CalcInputs, effective: List<CalcIngredient>): List<CalcIngredient> {
        if (!inputs.targetDirty) return effective
        return inputs.rows.map { r ->
            val kg = parseNum(inputs.targetTexts[r.id] ?: "") * inputs.unit.factor
            CalcIngredient(r.id, r.name, r.type, if (kg > 0) kg else 0.0)
        }
    }

    // ---------------------------------------------------------------- batch

    fun computeBatch(inputs: CalcInputs, effective: List<CalcIngredient>): CalcBatch {
        val f = inputs.unit.factor
        val baseKg = parseNum(inputs.baseText) * f
        val capKg = parseNum(inputs.capText) * f
        var multiplier = parseNum(inputs.multText)
        if (!(multiplier > 0)) multiplier = 1.0

        val active = effective.filter { !it.isBlank }
        val unitWeights: List<Double> = if (inputs.mode == CalcMode.UNIT_BASED) {
            active.map { it.valueKg }
        } else {
            val totalInput = active.sumOf { it.valueKg }
            val scale = if (totalInput > 0) baseKg / totalInput else 1.0
            active.map { it.valueKg * scale }
        }
        val unscaledTotal = unitWeights.sum()

        var fromCap = false
        if (capKg > 0) {
            if (unscaledTotal > 0) {
                multiplier = capKg / unscaledTotal
                fromCap = true
            } else {
                multiplier = 1.0
            }
        }

        val rows = active.mapIndexed { i, ing ->
            CalcBatchRow(ing.id, ing.name, ing.type, unitWeights[i], unitWeights[i] * multiplier)
        }
        return CalcBatch(
            rows = rows,
            grandTotalKg = rows.sumOf { it.finalKg },
            multiplier = multiplier,
            unitTotalKg = unscaledTotal,
            baseKg = baseKg,
            multiplierFromCap = fromCap
        )
    }

    // ---------------------------------------------------------------- correction

    /** Scale a manual "Add" edit implies: (remaining + typed add) / per-unit target. */
    fun scaleForAdd(remKg: Double, targetUnitKg: Double, addKg: Double): Double? =
        if (targetUnitKg > 0) (remKg + addKg) / targetUnitKg else null

    private fun correctionSignature(rem: List<Pair<Int, Double>>, tgt: List<Pair<Int, Double>>): String {
        val r = rem.joinToString(",") { "${it.first}:${String.format(Locale.US, "%.9f", it.second)}" }
        val t = tgt.joinToString(",") { "${it.first}:${it.second}" }
        return "$r|$t"
    }

    private data class CorrectionOutcome(val correction: CalcCorrection, val signature: String?, val manual: Double?)

    private fun computeCorrection(
        inputs: CalcInputs,
        batch: CalcBatch,
        target: List<CalcIngredient>,
        manualScale: Double?,
        knownSignature: String?
    ): CorrectionOutcome {
        val f = inputs.unit.factor
        val origKg = parseNum(inputs.origText) * f
        val usedKg = parseNum(inputs.usedText) * f
        val remKg = max(0.0, origKg - usedKg)
        val inactive = CalcCorrection(origKg = origKg, usedKg = usedKg, remKg = remKg)

        if (remKg <= 0 || batch.grandTotalKg <= 0) {
            return CorrectionOutcome(inactive, knownSignature, manualScale)
        }

        data class Rem(val id: Int, val name: String, val kg: Double)
        val rem = batch.rows.map { Rem(it.id, it.name, (it.finalKg / batch.grandTotalKg) * remKg) }
        val targetUnit = target.filter { !it.isBlank }

        var maxScale = 0.0
        rem.forEach { r ->
            val t = targetUnit.firstOrNull { it.id == r.id }
            if (t != null && t.valueKg > 0) maxScale = max(maxScale, r.kg / t.valueKg)
        }

        val sig = correctionSignature(rem.map { it.id to it.kg }, targetUnit.map { it.id to it.valueKg })
        val validManual = if (sig == knownSignature) manualScale else null

        var scale = maxScale
        var manual = false
        if (validManual != null && validManual > maxScale * (1 + 1e-9)) {
            scale = validManual
            manual = true
        }

        var newTotal = 0.0
        var addTotal = 0.0
        var remTotal = 0.0
        val rows = rem.map { r ->
            val tUnit = targetUnit.firstOrNull { it.id == r.id }?.valueKg ?: 0.0
            val targetKg = tUnit * scale
            val addKg = max(0.0, targetKg - r.kg)
            newTotal += targetKg
            addTotal += addKg
            remTotal += r.kg
            CalcCorrectionRow(r.id, r.name, r.kg, targetKg, addKg, tUnit > 0, tUnit)
        }
        val c = CalcCorrection(origKg, usedKg, remKg, newTotal, addTotal, remTotal, manual, rows, active = true)
        return CorrectionOutcome(c, sig, validManual)
    }

    // ---------------------------------------------------------------- everything at once

    /**
     * @param manualScale scale chosen by editing an Add box (null = automatic minimum)
     * @param knownSignature signature the manual scale was chosen for
     */
    fun derive(inputs: CalcInputs, manualScale: Double? = null, knownSignature: String? = null): CalcDerived {
        val auto = computeAutoFill(inputs)
        val effective = effectiveIngredients(inputs, auto)
        val target = targetIngredients(inputs, effective)
        val batch = computeBatch(inputs, effective)
        val out = computeCorrection(inputs, batch, target, manualScale, knownSignature)
        return CalcDerived(
            auto = auto,
            effective = effective,
            target = target,
            batch = batch,
            correction = out.correction,
            signature = out.signature,
            manualScale = out.manual
        )
    }

    // ---------------------------------------------------------------- reports

    private fun reportHeader(title: String, inputs: CalcInputs, batch: CalcBatch, dateText: String): MutableList<String> {
        val modeText = if (inputs.mode == CalcMode.UNIT_BASED) "Unit -> Large Batch" else "Batch -> Per-Unit Extract"
        val base = inputs.baseText.ifBlank { "0" }
        return mutableListOf(
            title,
            dateText,
            LINE,
            "Mode: $modeText",
            "Base unit size: $base ${inputs.unit.label}",
            "Batch multiplier: ${fmtMultiplier(batch.multiplier)}",
            "Total batch: ${fmtBoth(batch.grandTotalKg)}"
        )
    }

    fun buildBatchReport(inputs: CalcInputs, derived: CalcDerived, dateText: String): String {
        val u = inputs.unit
        val b = derived.batch
        val l = reportHeader("MIXTURE CALCULATOR - BATCH REPORT", inputs, b, dateText)
        l.add(""); l.add("INGREDIENTS"); l.add(LINE)
        if (b.rows.isEmpty()) l.add("No ingredients entered.")
        b.rows.forEachIndexed { i, r ->
            val share = if (b.grandTotalKg > 0) String.format(Locale.US, "%.1f", r.finalKg / b.grandTotalKg * 100) else "0.0"
            l.add("${i + 1}. ${r.name} (${r.type.label})")
            l.add("    Per unit: ${fmt(r.unitKg, u)}")
            l.add("    Full batch: ${fmt(r.finalKg, u)}  [$share%]")
        }
        l.add(LINE)
        l.add("TOTAL: ${fmtBoth(b.grandTotalKg)}")
        return l.joinToString("\n")
    }

    fun buildComparisonReport(inputs: CalcInputs, derived: CalcDerived, dateText: String): String {
        val u = inputs.unit
        val b = derived.batch
        val c = derived.correction
        val l = reportHeader("MIXTURE CALCULATOR - COMPARISON & CORRECTION REPORT", inputs, b, dateText)
        l.add(""); l.add("BATCH vs CORRECTION"); l.add(LINE)
        if (b.rows.isEmpty()) l.add("No ingredients entered.")
        b.rows.forEachIndexed { i, r ->
            val add = c.addFor(r.id)
            l.add("${i + 1}. ${r.name}")
            l.add("    Per unit: ${fmt(r.unitKg, u)}")
            l.add("    Scaled batch: ${fmt(r.finalKg, u)}")
            l.add("    Add to correct: ${if (add > 0) fmt(add, u) else "nothing"}")
        }
        if (c.active) {
            l.add(""); l.add("BATCH CORRECTION"); l.add(LINE)
            l.add("Original batch: ${fmt(c.origKg, u)}")
            l.add("Used / lost: ${fmt(c.usedKg, u)}")
            l.add("Remaining: ${fmt(c.remKg, u)}")
            l.add(""); l.add("WHAT TO ADD:")
            val toAdd = c.rows.filter { it.addKg > 0 }
            if (toAdd.isEmpty()) l.add("Nothing - remaining batch already matches the formula.")
            toAdd.forEach { r ->
                l.add("  + ${r.name}: ${fmt(r.addKg, u)}   (now ${fmt(r.remKg, u)} -> ${fmt(r.targetKg, u)})")
            }
            l.add(LINE)
            l.add("Total to add: ${fmt(c.addTotalKg, u)}")
            l.add("New batch total: ${fmt(c.newTotalKg, u)}")
            if (c.manual) l.add("(Amounts adjusted manually; ratio kept)")
        } else {
            l.add(""); l.add("(No batch correction entered.)")
        }
        return l.joinToString("\n")
    }
}

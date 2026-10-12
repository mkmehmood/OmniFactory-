package com.example.domain.mixture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FormulaCalcEngineTest {

    private val eps = 1e-9

    private fun base() = CalcInputs(
        baseText = "10", capText = "100", origText = "50", usedText = "20",
        rows = listOf(
            CalcRow(1, "A", valueText = "4"),
            CalcRow(2, "B", valueText = "3"),
            CalcRow(3, "C", valueText = ""),
            CalcRow(4, "", valueText = "9")
        )
    )

    @Test
    fun autoFillScaleAndHiddenRows() {
        val d = FormulaCalcEngine.derive(base())
        assertEquals(3, d.auto!!.id)
        assertEquals(3.0, d.auto!!.kg, eps)
        assertEquals(3, d.batch.rows.size) // blank-name row is hidden
        assertEquals(10.0, d.batch.multiplier, eps)
        assertEquals(100.0, d.batch.grandTotalKg, eps)
        assertEquals(30.0, d.batch.rows[2].finalKg, eps)
    }

    @Test
    fun correctionNeedsNothingWhenFormulaUnchanged() {
        val c = FormulaCalcEngine.derive(base()).correction
        assertTrue(c.active)
        assertEquals(30.0, c.remKg, eps)
        assertEquals(0.0, c.addTotalKg, eps)
    }

    @Test
    fun editedTargetAndManualScale() {
        val inp = base().copy(targetDirty = true, targetTexts = mapOf(1 to "6", 2 to "3", 3 to "3"))
        val d = FormulaCalcEngine.derive(inp)
        assertEquals(6.0, d.correction.addFor(1), eps)
        assertEquals(36.0, d.correction.newTotalKg, eps)

        val row = d.correction.rows.first { it.id == 1 }
        val scale = FormulaCalcEngine.scaleForAdd(row.remKg, row.targetUnitKg, 12.0)!!
        val manual = FormulaCalcEngine.derive(inp, scale, d.signature)
        assertTrue(manual.correction.manual)
        assertEquals(3.0, manual.correction.addFor(2), eps)

        // changing the inputs drops the manual scale
        val changed = FormulaCalcEngine.derive(inp.copy(usedText = "10"), scale, d.signature)
        assertFalse(changed.correction.manual)
        assertNull(changed.manualScale)
    }

    @Test
    fun batchExtractAndMultiplier() {
        val extract = FormulaCalcEngine.derive(
            CalcInputs(
                mode = CalcMode.BATCH_EXTRACT, baseText = "2",
                rows = listOf(CalcRow(1, "X", valueText = "4"), CalcRow(2, "Y", valueText = "6"))
            )
        )
        assertEquals(0.8, extract.batch.rows[0].unitKg, eps)
        assertEquals(1.2, extract.batch.rows[1].unitKg, eps)

        val typed = FormulaCalcEngine.derive(
            CalcInputs(baseText = "1", multText = "2.5", rows = listOf(CalcRow(1, "X", valueText = "1"), CalcRow(2, "Y", valueText = "2")))
        )
        assertEquals(7.5, typed.batch.grandTotalKg, eps)
    }

    @Test
    fun gramsAndFormatting() {
        val g = FormulaCalcEngine.derive(
            CalcInputs(unit = CalcUnit.G, baseText = "500", rows = listOf(CalcRow(1, "X", valueText = "200"), CalcRow(2, "Y", valueText = "")))
        )
        assertNotNull(g.auto)
        assertEquals(0.3, g.auto!!.kg, eps)
        assertEquals("1.235 kg", FormulaCalcEngine.fmt(1.2345, CalcUnit.KG))
        assertEquals("1,234.5 g", FormulaCalcEngine.fmt(1.2345, CalcUnit.G))
        assertEquals("1500", FormulaCalcEngine.convertText("1.5", CalcUnit.KG, CalcUnit.G))
        assertEquals("0.25", FormulaCalcEngine.convertText("250", CalcUnit.G, CalcUnit.KG))
        assertEquals("1.23", FormulaCalcEngine.sanitizeNumeric("1,2.3a"))
    }
}

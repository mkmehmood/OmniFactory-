package com.example.domain.mixture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MixtureEquationEngineTest {

    // ---- Scenario 1 & 2: A = 80, B = 10 (kg); C = 400 g, D = 150 g per 2.3 kg of A+B ----------

    private fun referenceComponents() = listOf(
        ReferenceComponent("A", "A", 80.0, "kg", ReferenceRole.MAIN),
        ReferenceComponent("B", "B", 10.0, "kg", ReferenceRole.MAIN),
        ReferenceComponent("C", "C", 400.0, "g", ReferenceRole.DEPENDENT),
        ReferenceComponent("D", "D", 150.0, "g", ReferenceRole.DEPENDENT)
    )

    @Test
    fun perReference_fullScaleDependents() {
        val r = MixtureEquationEngine.solvePerReference(referenceComponents(), 2.3, "kg", "kg")
        assertTrue(r.isValid)
        assertEquals(90.0, r.mainTotal, 1e-9)
        assertEquals(90.0 / 2.3, r.scaleFactor, 1e-9)
        // C = 400 g x 39.13 = 15.652 kg, D = 150 g x 39.13 = 5.870 kg
        assertEquals(15.6522, r.rows.first { it.id == "C" }.fullScaleQty, 1e-3)
        assertEquals(5.8696, r.rows.first { it.id == "D" }.fullScaleQty, 1e-3)
    }

    @Test
    fun perReference_splitInsideOneReferencePortion_inGrams() {
        val r = MixtureEquationEngine.solvePerReference(referenceComponents(), 2.3, "kg", "g")
        assertTrue(r.isValid)
        assertEquals(2044.444, r.rows.first { it.id == "A" }.perReferenceQty, 1e-3)
        assertEquals(255.556, r.rows.first { it.id == "B" }.perReferenceQty, 1e-3)
        assertEquals(400.0, r.rows.first { it.id == "C" }.perReferenceQty, 1e-9)
        assertEquals(150.0, r.rows.first { it.id == "D" }.perReferenceQty, 1e-9)
        // One reference portion recipe weighs 2,300 + 400 + 150
        assertEquals(2850.0, r.rows.sumOf { it.perReferenceQty }, 1e-6)
    }

    @Test
    fun perReference_requiresMainMixAndReference() {
        val noMain = MixtureEquationEngine.solvePerReference(
            listOf(ReferenceComponent("C", "C", 400.0, "g", ReferenceRole.DEPENDENT)), 2.3, "kg"
        )
        assertFalse(noMain.isValid)
        val badRef = MixtureEquationEngine.solvePerReference(referenceComponents(), 0.0, "kg")
        assertFalse(badRef.isValid)
        assertNotNull(badRef.errorMessage)
    }

    // ---- Scenarios 3-5 use the one-portion recipe (grams) as the base ------------------------

    private fun baseRecipe(
        aRole: ComponentRole = ComponentRole.SCALE,
        bRole: ComponentRole = ComponentRole.SCALE,
        cRole: ComponentRole = ComponentRole.SCALE,
        dRole: ComponentRole = ComponentRole.SCALE,
        cFixed: Double? = null,
        dFixed: Double? = null
    ) = listOf(
        EquationComponent("A", "A", 2044.4444444, "g", aRole),
        EquationComponent("B", "B", 255.5555556, "g", bRole),
        EquationComponent("C", "C", 400.0, "g", cRole, cFixed, "g"),
        EquationComponent("D", "D", 150.0, "g", dRole, dFixed, "g")
    )

    @Test
    fun targetTotal_scenario3_scaleEverythingProportionally() {
        val r = MixtureEquationEngine.solveTargetTotal(baseRecipe(), 2.3, "kg")
        assertTrue(r.isValid)
        assertEquals(2300.0 / 2850.0, r.scaleFactor, 1e-9)
        // result unit is kg
        assertEquals(1.6499, r.rows.first { it.id == "A" }.resultQty, 1e-3)
        assertEquals(0.2062, r.rows.first { it.id == "B" }.resultQty, 1e-3)
        assertEquals(0.3228, r.rows.first { it.id == "C" }.resultQty, 1e-3)
        assertEquals(0.1211, r.rows.first { it.id == "D" }.resultQty, 1e-3)
        assertEquals(2.3, r.achievedTotal, 1e-9)
    }

    @Test
    fun targetTotal_scenario3_inGrams() {
        val r = MixtureEquationEngine.solveTargetTotal(baseRecipe(), 2300.0, "g")
        assertTrue(r.isValid)
        assertEquals(1649.9, r.rows.first { it.id == "A" }.resultQty, 0.1)
        assertEquals(206.2, r.rows.first { it.id == "B" }.resultQty, 0.1)
        assertEquals(322.8, r.rows.first { it.id == "C" }.resultQty, 0.1)
        assertEquals(121.1, r.rows.first { it.id == "D" }.resultQty, 0.1)
        assertEquals(2300.0, r.rows.sumOf { it.resultQty }, 1e-6)
    }

    @Test
    fun targetTotal_scenario4_cAndDFixed_abSplitByRatio() {
        val r = MixtureEquationEngine.solveTargetTotal(
            baseRecipe(cRole = ComponentRole.FIXED, dRole = ComponentRole.FIXED, cFixed = 400.0, dFixed = 150.0),
            2300.0, "g"
        )
        assertTrue(r.isValid)
        assertEquals(1555.556, r.rows.first { it.id == "A" }.resultQty, 1e-2)
        assertEquals(194.444, r.rows.first { it.id == "B" }.resultQty, 1e-2)
        assertEquals(400.0, r.rows.first { it.id == "C" }.resultQty, 1e-9)
        assertEquals(150.0, r.rows.first { it.id == "D" }.resultQty, 1e-9)
        assertEquals(2300.0, r.achievedTotal, 1e-6)
        // A:B ratio is preserved at 8:1
        assertEquals(8.0, r.rows.first { it.id == "A" }.resultQty / r.rows.first { it.id == "B" }.resultQty, 1e-6)
    }

    @Test
    fun targetTotal_scenario5_bRemoved_cUpdated_aTakesRemainder() {
        val r = MixtureEquationEngine.solveTargetTotal(
            baseRecipe(
                bRole = ComponentRole.REMOVED,
                cRole = ComponentRole.FIXED, dRole = ComponentRole.FIXED,
                cFixed = 500.0, dFixed = 150.0
            ),
            2300.0, "g"
        )
        assertTrue(r.isValid)
        assertEquals(1650.0, r.rows.first { it.id == "A" }.resultQty, 1e-6)
        assertEquals(0.0, r.rows.first { it.id == "B" }.resultQty, 1e-9)
        assertEquals(500.0, r.rows.first { it.id == "C" }.resultQty, 1e-9)
        assertEquals(150.0, r.rows.first { it.id == "D" }.resultQty, 1e-9)
        assertTrue(r.warnings.isNotEmpty())
    }

    @Test
    fun targetTotal_mixedUnits_fixedInGramsTargetInKg() {
        val comps = listOf(
            EquationComponent("A", "A", 80.0, "kg", ComponentRole.SCALE),
            EquationComponent("C", "C", 1.0, "kg", ComponentRole.FIXED, 400.0, "g")
        )
        val r = MixtureEquationEngine.solveTargetTotal(comps, 2.3, "kg")
        assertTrue(r.isValid)
        assertEquals(0.4, r.rows.first { it.id == "C" }.resultQty, 1e-9)
        assertEquals(1.9, r.rows.first { it.id == "A" }.resultQty, 1e-9)
    }

    @Test
    fun targetTotal_fixedExceedsTarget_isInvalid() {
        val r = MixtureEquationEngine.solveTargetTotal(
            baseRecipe(cRole = ComponentRole.FIXED, cFixed = 3000.0), 2300.0, "g"
        )
        assertFalse(r.isValid)
        assertTrue(r.errorMessage!!.contains("exceed"))
    }

    @Test
    fun targetTotal_noScaleComponentAndMismatch_isInvalid() {
        val comps = listOf(
            EquationComponent("C", "C", 400.0, "g", ComponentRole.FIXED, 400.0, "g"),
            EquationComponent("D", "D", 150.0, "g", ComponentRole.FIXED, 150.0, "g")
        )
        assertFalse(MixtureEquationEngine.solveTargetTotal(comps, 2300.0, "g").isValid)
        // exact match is fine
        assertTrue(MixtureEquationEngine.solveTargetTotal(comps, 550.0, "g").isValid)
    }

    @Test
    fun targetTotal_incompatibleUnits_isInvalid() {
        val comps = listOf(
            EquationComponent("A", "A", 10.0, "kg", ComponentRole.SCALE),
            EquationComponent("W", "W", 5.0, "L", ComponentRole.SCALE)
        )
        val r = MixtureEquationEngine.solveTargetTotal(comps, 20.0, "kg")
        assertFalse(r.isValid)
    }

    @Test
    fun targetTotal_invalidTarget() {
        assertFalse(MixtureEquationEngine.solveTargetTotal(baseRecipe(), 0.0, "kg").isValid)
        assertFalse(MixtureEquationEngine.solveTargetTotal(emptyList(), 5.0, "kg").isValid)
    }

    // ---- Base + Additives + Multiplier (spec: Mixture Calculator Summary) ----------------------

    private fun specComponents() = listOf(
        BatchComponent("a", "Chemical", 80.0, "kg", BatchRole.PROPORTIONAL),
        BatchComponent("b", "Berlinza", 10.0, "kg", BatchRole.PROPORTIONAL),
        BatchComponent("c", "Taman", 0.5, "kg", BatchRole.FIXED_BASE),
        BatchComponent("d", "Lime", 0.15, "kg", BatchRole.FIXED_BASE),
        BatchComponent("e", "Powder", 0.7, "kg", BatchRole.FIXED_BASE),
        BatchComponent("f", "Orange", 2.1, "kg", BatchRole.ADDITIVE),
        BatchComponent("g", "Ajmal White", 1.6, "kg", BatchRole.ADDITIVE),
        BatchComponent("h", "White", 0.53, "kg", BatchRole.ADDITIVE),
        BatchComponent("i", "Green", 0.53, "kg", BatchRole.ADDITIVE),
        BatchComponent("j", "Furman", 0.26, "kg", BatchRole.ADDITIVE)
    )

    @Test
    fun baseBatch_unscaledMatchesSpec() {
        val r = MixtureEquationEngine.solveBaseBatch(specComponents(), 3.0, "kg", 1.0)
        assertTrue(r.isValid)
        assertEquals(1.35, r.fixedTotal, 1e-9)
        assertEquals(1.65, r.remainingForRatio, 1e-9)
        assertEquals(1.4667, r.rows.first { it.id == "a" }.unscaledQty, 1e-3)
        assertEquals(0.1833, r.rows.first { it.id == "b" }.unscaledQty, 1e-3)
        assertEquals(5.02, r.additiveTotal, 1e-9)
        assertEquals(8.02, r.unscaledTotal, 1e-9)
        assertEquals(8.02, r.rows.sumOf { it.unscaledQty }, 1e-9)
    }

    @Test
    fun baseBatch_scaledByTenMatchesSpec() {
        val r = MixtureEquationEngine.solveBaseBatch(specComponents(), 3.0, "kg", 10.0)
        assertTrue(r.isValid)
        assertEquals(14.667, r.rows.first { it.id == "a" }.finalQty, 1e-3)
        assertEquals(1.833, r.rows.first { it.id == "b" }.finalQty, 1e-3)
        assertEquals(21.0, r.rows.first { it.id == "f" }.finalQty, 1e-9)
        assertEquals(80.2, r.finalTotal, 1e-9)
        assertEquals(80.2, r.rows.sumOf { it.finalQty }, 1e-9)
    }

    @Test
    fun baseBatch_mixedUnitsAreConverted() {
        val comps = listOf(
            BatchComponent("a", "A", 1.0, "kg", BatchRole.PROPORTIONAL),
            BatchComponent("c", "C", 500.0, "g", BatchRole.FIXED_BASE),
            BatchComponent("f", "F", 250.0, "g", BatchRole.ADDITIVE)
        )
        val r = MixtureEquationEngine.solveBaseBatch(comps, 2.0, "kg", 2.0)
        assertTrue(r.isValid)
        assertEquals(1.5, r.rows.first { it.id == "a" }.unscaledQty, 1e-9)
        assertEquals(2.25, r.unscaledTotal, 1e-9)
        assertEquals(4.5, r.finalTotal, 1e-9)
    }

    @Test
    fun baseBatch_rejectsBadInput() {
        assertFalse(MixtureEquationEngine.solveBaseBatch(specComponents(), 1.0, "kg", 1.0).isValid) // fixed > base
        assertFalse(MixtureEquationEngine.solveBaseBatch(specComponents(), 3.0, "kg", 0.0).isValid)
        assertFalse(MixtureEquationEngine.solveBaseBatch(specComponents(), 0.0, "kg", 1.0).isValid)
        val noRatio = specComponents().filter { it.role != BatchRole.PROPORTIONAL }
        assertFalse(MixtureEquationEngine.solveBaseBatch(noRatio, 3.0, "kg", 1.0).isValid)
    }

    // ---- Bridge: solver output feeds the shared pipeline ---------------------------------------

    private fun specIngredients() = specComponents().map {
        TargetIngredient(id = it.id, name = it.name, targetQuantity = it.qty, unit = it.unit)
    }

    @Test
    fun bridge_baseBatchFeedsBatchScaling() {
        val solved = MixtureEquationEngine.solveBaseBatch(specComponents(), 3.0, "kg", 10.0)
        val outcome = MixtureFormulaBridge.fromBaseBatch(solved, specIngredients())!!
        val recal = outcome.recalibration
        assertEquals(80.2, recal.totalNewMixtureWeight, 1e-9)
        assertEquals(10, recal.proportions.size)
        assertEquals(10.0, recal.scaleFactor, 1e-9)
        // Same rows regardless of which legacy list the UI reads.
        assertEquals(recal.proportions, recal.addModeProportions)
        // Step 5 multiply x2 doubles the solved formulation.
        val batches = MixtureCalculatorEngine.calculateBatches(recal, 2.0, BatchOperationMode.MULTIPLY)
        assertEquals(160.4, batches.totalResultWeight, 1e-9)
        // Divide into 4 containers.
        val divided = MixtureCalculatorEngine.calculateBatches(recal, 4.0, BatchOperationMode.DIVIDE)
        assertEquals(20.05, divided.totalWeightPerBatch, 1e-9)
    }

    @Test
    fun bridge_targetTotalKeepsTargetAndMarksLargestAsAnchor() {
        val comps = listOf(
            EquationComponent("a", "A", 80.0, "kg"),
            EquationComponent("b", "B", 10.0, "kg"),
            EquationComponent("c", "C", 400.0, "g", ComponentRole.FIXED, 400.0, "g")
        )
        val ings = comps.map { TargetIngredient(it.id, it.name, it.baseQty, it.unit) }
        val solved = MixtureEquationEngine.solveTargetTotal(comps, 2.3, "kg")
        val recal = MixtureFormulaBridge.fromTargetTotal(solved, ings)!!.recalibration
        assertEquals(2.3, recal.totalNewMixtureWeight, 1e-9)
        assertEquals("A", recal.anchorIngredientName)
        assertEquals(2.3, recal.proportions.sumOf { it.newRequiredTotal }, 1e-9)
    }

    @Test
    fun bridge_returnsNullForInvalidResult() {
        val bad = MixtureEquationEngine.solveBaseBatch(specComponents(), 1.0, "kg", 1.0)
        assertFalse(bad.isValid)
        assertEquals(null, MixtureFormulaBridge.fromBaseBatch(bad, specIngredients()))
    }

    // ---- In-tub amounts for solver methods -----------------------------------------------------

    @Test
    fun bridge_inTubGivesAddAndRemoveAmounts() {
        val solved = MixtureEquationEngine.solveBaseBatch(specComponents(), 3.0, "kg", 10.0)
        // 10 kg of Chemical already in the tub (needs 14.667) and 6 kg of Powder (needs 7.0 -> over? no, under).
        // Orange needs 21 kg; 25 kg in tub is 4 kg over. Powder is given in g to test conversion: 8000 g = 8 kg (1 kg over).
        val ings = specIngredients().map { if (it.id == "e") it.copy(unit = "g", targetQuantity = 700.0) else it }
        val comps = specComponents().map { if (it.id == "e") it.copy(unit = "g", qty = 700.0) else it }
        val s2 = MixtureEquationEngine.solveBaseBatch(comps, 3.0, "kg", 10.0)
        val inTub = mapOf("a" to 10.0, "f" to 25.0, "e" to 8000.0)
        val summary = MixtureFormulaBridge.fromBaseBatch(s2, ings, inTub)!!.summary
        val a = summary.lines.first { it.id == "a" }
        val f = summary.lines.first { it.id == "f" }
        val e = summary.lines.first { it.id == "e" }
        assertEquals(10.0, a.inTubQty, 1e-9)
        assertEquals(14.6667 - 10.0, a.stillNeeded, 1e-3)
        assertEquals(4.0, f.excess, 1e-9)
        assertEquals(8.0, e.inTubQty, 1e-9)   // 8000 g converted to kg
        assertEquals(1.0, e.excess, 1e-9)
        assertTrue(summary.hasInTub)
        assertTrue(summary.totalExcess >= 5.0 - 1e-9)
        // The same figures reach the calculator's result used by Step 5 and history.
        val recal = MixtureFormulaBridge.fromBaseBatch(s2, ings, inTub)!!.recalibration
        assertTrue(recal.hasExcessInTub)
        assertEquals(summary.totalStillNeeded, recal.totalStillNeededWeight, 1e-9)
        assertEquals(80.2, recal.totalNewMixtureWeight, 1e-9)
        assertNotNull(solved)
    }

    @Test
    fun bridge_noInTubMeansEverythingStillNeeded() {
        val solved = MixtureEquationEngine.solveBaseBatch(specComponents(), 3.0, "kg", 1.0)
        val summary = MixtureFormulaBridge.fromBaseBatch(solved, specIngredients())!!.summary
        assertFalse(summary.hasInTub)
        assertEquals(8.02, summary.totalStillNeeded, 1e-9)
        assertEquals(0.0, summary.totalExcess, 1e-9)
    }
}

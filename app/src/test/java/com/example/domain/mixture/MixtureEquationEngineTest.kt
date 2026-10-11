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
}

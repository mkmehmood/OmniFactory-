package com.example.domain

import com.example.data.currency.supportedCurrencies
import com.example.domain.converter.ConverterCategory
import com.example.domain.converter.UnitRegistry
import com.example.ui.BmiCategory
import com.example.ui.BmiUnitSystem
import com.example.ui.ConverterViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ConverterTest {

    @Test
    fun testAll11CategoriesExist() {
        val categories = ConverterCategory.values()
        assertEquals(11, categories.size)
        val expectedShortNames = listOf(
            "Currency", "BMI", "Length", "Area", "Volume",
            "Weight", "Temperature", "Speed", "Pressure", "Power", "Number Base"
        )
        expectedShortNames.forEach { expected ->
            assertTrue("Category $expected should be present", categories.any { it.shortName == expected })
        }
    }

    @Test
    fun testLengthConversion() {
        val km = UnitRegistry.lengthUnits.first { it.id == "km" }
        val m = UnitRegistry.lengthUnits.first { it.id == "m" }
        val mi = UnitRegistry.lengthUnits.first { it.id == "mi" }

        val convertedM = UnitRegistry.convertStandard(2.5, km, m)
        assertEquals(2500.0, convertedM, 0.001)

        val convertedKm = UnitRegistry.convertStandard(1.0, mi, km)
        assertEquals(1.609344, convertedKm, 0.001)
    }

    @Test
    fun testAreaConversion() {
        val ha = UnitRegistry.areaUnits.first { it.id == "ha" }
        val sqM = UnitRegistry.areaUnits.first { it.id == "sq_m" }
        val ac = UnitRegistry.areaUnits.first { it.id == "ac" }

        val convertedSqM = UnitRegistry.convertStandard(1.0, ha, sqM)
        assertEquals(10000.0, convertedSqM, 0.001)

        val convertedAcToHa = UnitRegistry.convertStandard(10.0, ac, ha)
        assertTrue(convertedAcToHa > 4.0 && convertedAcToHa < 4.1)
    }

    @Test
    fun testVolumeConversion() {
        val l = UnitRegistry.volumeUnits.first { it.id == "l" }
        val ml = UnitRegistry.volumeUnits.first { it.id == "ml" }
        val gal = UnitRegistry.volumeUnits.first { it.id == "us_gal" }

        val convertedMl = UnitRegistry.convertStandard(1.5, l, ml)
        assertEquals(1500.0, convertedMl, 0.001)

        val convertedGalToL = UnitRegistry.convertStandard(1.0, gal, l)
        assertEquals(3.7854, convertedGalToL, 0.01)
    }

    @Test
    fun testWeightConversion() {
        val kg = UnitRegistry.weightUnits.first { it.id == "kg" }
        val g = UnitRegistry.weightUnits.first { it.id == "g" }
        val lb = UnitRegistry.weightUnits.first { it.id == "lb" }

        val convertedG = UnitRegistry.convertStandard(2.0, kg, g)
        assertEquals(2000.0, convertedG, 0.001)

        val convertedLbToKg = UnitRegistry.convertStandard(10.0, lb, kg)
        assertEquals(4.5359, convertedLbToKg, 0.01)
    }

    @Test
    fun testTemperatureConversion() {
        val c = UnitRegistry.TempUnit.CELSIUS
        val f = UnitRegistry.TempUnit.FAHRENHEIT
        val k = UnitRegistry.TempUnit.KELVIN

        assertEquals(32.0, UnitRegistry.convertTemperature(0.0, c, f), 0.001)
        assertEquals(212.0, UnitRegistry.convertTemperature(100.0, c, f), 0.001)
        assertEquals(273.15, UnitRegistry.convertTemperature(0.0, c, k), 0.001)
        assertEquals(0.0, UnitRegistry.convertTemperature(32.0, f, c), 0.001)
    }

    @Test
    fun testSpeedConversion() {
        val kmh = UnitRegistry.speedUnits.first { it.id == "kmh" }
        val mps = UnitRegistry.speedUnits.first { it.id == "mps" }
        val mph = UnitRegistry.speedUnits.first { it.id == "mph" }

        val convertedMps = UnitRegistry.convertStandard(36.0, kmh, mps)
        assertEquals(10.0, convertedMps, 0.01)

        val convertedMph = UnitRegistry.convertStandard(100.0, kmh, mph)
        assertEquals(62.137, convertedMph, 0.1)
    }

    @Test
    fun testPressureConversion() {
        val bar = UnitRegistry.pressureUnits.first { it.id == "bar" }
        val pa = UnitRegistry.pressureUnits.first { it.id == "pa" }
        val psi = UnitRegistry.pressureUnits.first { it.id == "psi" }

        val convertedPa = UnitRegistry.convertStandard(1.0, bar, pa)
        assertEquals(100000.0, convertedPa, 0.01)

        val convertedPsi = UnitRegistry.convertStandard(1.0, bar, psi)
        assertEquals(14.5038, convertedPsi, 0.01)
    }

    @Test
    fun testPowerConversion() {
        val kw = UnitRegistry.powerUnits.first { it.id == "kw" }
        val w = UnitRegistry.powerUnits.first { it.id == "w" }
        val hp = UnitRegistry.powerUnits.first { it.id == "hp" }

        val convertedW = UnitRegistry.convertStandard(1.5, kw, w)
        assertEquals(1500.0, convertedW, 0.01)

        val convertedHp = UnitRegistry.convertStandard(1.0, hp, w)
        assertEquals(745.7, convertedHp, 0.5)
    }

    @Test
    fun testNumberSystemConversions() {
        val dec = "42"
        val bin = UnitRegistry.formatNumberBase(dec, 10, 2)
        val hex = UnitRegistry.formatNumberBase(dec, 10, 16)
        val oct = UnitRegistry.formatNumberBase(dec, 10, 8)

        assertEquals("0010 1010", bin)
        assertEquals("2A", hex)
        assertEquals("52", oct)

        val backDec = UnitRegistry.formatNumberBase("2A", 16, 10)
        assertEquals("42", backDec)
    }

    @Test
    fun testBmiCalculation() {
        val vm = ConverterViewModel()
        vm.setBmiUnitSystem(BmiUnitSystem.METRIC)
        vm.setBmiMetricWeight("70")
        vm.setBmiMetricHeight("175")

        val result = vm.calculateBmi()
        assertEquals(22.9, result.bmiValue, 0.2)
        assertEquals(BmiCategory.NORMAL, result.category)
        assertNotNull(result.idealWeightRangeText)
    }

    @Test
    fun testConverterNavigationAndCategoriesHubFlow() {
        val vm = ConverterViewModel()
        // Default starts in categories hub overview
        assertTrue(vm.uiState.value.isCategoryGridActive)

        // Select category and open dedicated screen
        vm.selectCategoryAndOpenDedicatedScreen(ConverterCategory.SPEED)
        assertEquals(ConverterCategory.SPEED, vm.uiState.value.activeCategory)
        assertEquals(false, vm.uiState.value.isCategoryGridActive)

        // Return to categories hub
        vm.showCategoryGrid()
        assertEquals(true, vm.uiState.value.isCategoryGridActive)

        // Open another dedicated category screen
        vm.selectCategoryAndOpenDedicatedScreen(ConverterCategory.TEMPERATURE)
        assertEquals(ConverterCategory.TEMPERATURE, vm.uiState.value.activeCategory)
        assertEquals(false, vm.uiState.value.isCategoryGridActive)
    }

    @Test
    fun testSmartKeypadFieldAwareRouting() {
        val vm = ConverterViewModel()

        // 1. Currency amount routing
        vm.clearField("currency_amount")
        assertEquals("0", vm.uiState.value.currencyAmount)
        vm.appendTokenToField("currency_amount", "7")
        vm.appendTokenToField("currency_amount", "00")
        assertEquals("700", vm.uiState.value.currencyAmount)
        vm.backspaceField("currency_amount")
        assertEquals("70", vm.uiState.value.currencyAmount)

        // 2. BMI Metric Weight routing
        vm.clearField("bmi_weight_metric")
        assertEquals("0", vm.uiState.value.bmiWeightMetricKg)
        vm.appendTokenToField("bmi_weight_metric", "6")
        vm.appendTokenToField("bmi_weight_metric", "8")
        vm.appendTokenToField("bmi_weight_metric", ".")
        vm.appendTokenToField("bmi_weight_metric", "5")
        assertEquals("68.5", vm.uiState.value.bmiWeightMetricKg)

        // 3. Temperature input routing with Negate
        vm.clearField("temp_input")
        assertEquals("0", vm.uiState.value.tempInput)
        vm.appendTokenToField("temp_input", "4")
        vm.appendTokenToField("temp_input", "0")
        assertEquals("40", vm.uiState.value.tempInput)
        vm.negateField("temp_input")
        assertEquals("-40", vm.uiState.value.tempInput)
        vm.negateField("temp_input")
        assertEquals("40", vm.uiState.value.tempInput)

        // 4. Standard Input routing
        vm.clearField("standard_input")
        assertEquals("0", vm.uiState.value.standardInput)
        vm.appendTokenToField("standard_input", "1")
        vm.appendTokenToField("standard_input", "00")
        assertEquals("100", vm.uiState.value.standardInput)
    }

    @Test
    fun testCurrenciesSearchData() {
        val usd = supportedCurrencies.find { it.code == "USD" }
        assertNotNull(usd)
        assertTrue(usd!!.country.contains("America") || usd.country.contains("United States"))

        val jpy = supportedCurrencies.find { it.code == "JPY" }
        assertNotNull(jpy)
        assertEquals("Japan", jpy!!.country)

        val eur = supportedCurrencies.find { it.code == "EUR" }
        assertNotNull(eur)
        assertEquals("Europe", eur!!.region)
    }
}

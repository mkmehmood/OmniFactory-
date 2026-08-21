package com.example.domain

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
    fun testConverterNavigationAnd3x4GridFlow() {
        val vm = ConverterViewModel()
        // Default starts in grid overview
        assertTrue(vm.uiState.value.isCategoryGridActive)

        // Select category and open dedicated screen
        vm.selectCategoryAndOpenDedicatedScreen(ConverterCategory.SPEED)
        assertEquals(ConverterCategory.SPEED, vm.uiState.value.activeCategory)
        assertEquals(false, vm.uiState.value.isCategoryGridActive)

        // Return to 3x4 categories grid
        vm.showCategoryGrid()
        assertEquals(true, vm.uiState.value.isCategoryGridActive)

        // Open another dedicated category screen
        vm.selectCategoryAndOpenDedicatedScreen(ConverterCategory.TEMPERATURE)
        assertEquals(ConverterCategory.TEMPERATURE, vm.uiState.value.activeCategory)
        assertEquals(false, vm.uiState.value.isCategoryGridActive)
    }

    @Test
    fun testDialpadWithDoubleZero() {
        val vm = ConverterViewModel()
        vm.selectCategoryAndOpenDedicatedScreen(ConverterCategory.CURRENCY)
        vm.clearActiveValue()
        assertEquals("0", vm.uiState.value.currencyAmount)

        // Type '5'
        vm.appendActiveValue("5")
        assertEquals("5", vm.uiState.value.currencyAmount)

        // Type '00'
        vm.appendActiveValue("00")
        assertEquals("500", vm.uiState.value.currencyAmount)

        // Type another '00'
        vm.appendActiveValue("00")
        assertEquals("50000", vm.uiState.value.currencyAmount)

        // Backspace
        vm.backspaceActiveValue()
        assertEquals("5000", vm.uiState.value.currencyAmount)

        // Test Standard Units with 00
        vm.selectCategoryAndOpenDedicatedScreen(ConverterCategory.LENGTH)
        vm.clearActiveValue()
        assertEquals("0", vm.uiState.value.standardInput)
        vm.appendActiveValue("2")
        vm.appendActiveValue("00")
        assertEquals("200", vm.uiState.value.standardInput)
    }

    @Test
    fun testFiveCountriesSimultaneousCurrencyExchange() {
        val vm = ConverterViewModel()
        vm.selectCategoryAndOpenDedicatedScreen(ConverterCategory.CURRENCY)
        vm.setCurrencyAmount("100")
        vm.setCurrencyBase("USD")

        // Check 5 active countries
        val slots = vm.uiState.value.activeCurrencyCodes
        assertEquals(5, slots.size)
        assertEquals("USD", slots[0])

        // Verify conversion amounts for different countries
        val eurAmount = vm.getConvertedAmountFor("EUR")
        assertNotNull(eurAmount)
        assertTrue(eurAmount.isNotEmpty())

        val gbpAmount = vm.getConvertedAmountFor("GBP")
        assertNotNull(gbpAmount)
        assertTrue(gbpAmount.isNotEmpty())

        val jpyAmount = vm.getConvertedAmountFor("JPY")
        assertNotNull(jpyAmount)
        assertTrue(jpyAmount.isNotEmpty())

        // Change a slot via search & click
        vm.setCurrencySlot(3, "SAR") // Saudi Riyal in 4th slot
        assertEquals("SAR", vm.uiState.value.activeCurrencyCodes[3])
        val sarAmount = vm.getConvertedAmountFor("SAR")
        assertNotNull(sarAmount)

        // Set base country to SAR
        vm.setCurrencyBase("SAR")
        assertEquals("SAR", vm.uiState.value.currencyBase)
        val usdFromSar = vm.getConvertedAmountFor("USD")
        assertNotNull(usdFromSar)
    }
}

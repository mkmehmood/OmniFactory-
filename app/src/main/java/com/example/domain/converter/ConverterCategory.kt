package com.example.domain.converter

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AreaChart
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

enum class ConverterCategory(
    val title: String,
    val shortName: String,
    val description: String,
    val icon: ImageVector,
    val accentColor: Color,
    val testTagId: String
) {
    CURRENCY(
        title = "Live Currency Exchange Rate",
        shortName = "Currency",
        description = "Real-time world exchange rates & conversions",
        icon = Icons.Default.AttachMoney,
        accentColor = Color(0xFF10B981),
        testTagId = "category_currency"
    ),
    BMI(
        title = "BMI (Body Mass Index)",
        shortName = "BMI",
        description = "Healthy weight & body composition gauge",
        icon = Icons.Default.FitnessCenter,
        accentColor = Color(0xFF06B6D4),
        testTagId = "category_bmi"
    ),
    LENGTH(
        title = "Length",
        shortName = "Length",
        description = "Metric, Imperial, Astronomical & Nautical",
        icon = Icons.Default.Straighten,
        accentColor = Color(0xFF3B82F6),
        testTagId = "category_length"
    ),
    AREA(
        title = "Area",
        shortName = "Area",
        description = "Land, imperial, metric & square units",
        icon = Icons.Default.AreaChart,
        accentColor = Color(0xFF8B5CF6),
        testTagId = "category_area"
    ),
    VOLUME(
        title = "Volume & Capacity",
        shortName = "Volume",
        description = "Liters, gallons, fluid ounces & barrels",
        icon = Icons.Default.ViewInAr,
        accentColor = Color(0xFFEC4899),
        testTagId = "category_volume"
    ),
    WEIGHT(
        title = "Weight & Mass",
        shortName = "Weight",
        description = "Kilograms, pounds, stone, ounces & carats",
        icon = Icons.Default.Height,
        accentColor = Color(0xFFF59E0B),
        testTagId = "category_weight"
    ),
    TEMPERATURE(
        title = "Temperature",
        shortName = "Temperature",
        description = "Celsius, Fahrenheit, Kelvin & formulas",
        icon = Icons.Default.Thermostat,
        accentColor = Color(0xFFEF4444),
        testTagId = "category_temperature"
    ),
    SPEED(
        title = "Speed & Velocity",
        shortName = "Speed",
        description = "km/h, mph, m/s, knots & Mach speeds",
        icon = Icons.Default.Speed,
        accentColor = Color(0xFF6366F1),
        testTagId = "category_speed"
    ),
    PRESSURE(
        title = "Pressure",
        shortName = "Pressure",
        description = "PSI, Bar, Pascals, atm & Torr",
        icon = Icons.Default.Compress,
        accentColor = Color(0xFF14B8A6),
        testTagId = "category_pressure"
    ),
    POWER(
        title = "Power",
        shortName = "Power",
        description = "Watts, Kilowatts, Horsepower & BTU/h",
        icon = Icons.Default.Bolt,
        accentColor = Color(0xFFEAB308),
        testTagId = "category_power"
    ),
    NUMBER_SYSTEM(
        title = "Number System",
        shortName = "Number Base",
        description = "Binary, Hex, Octal & Decimal bitwise",
        icon = Icons.Default.Numbers,
        accentColor = Color(0xFFA855F7),
        testTagId = "category_number_system"
    )
}

data class ConversionUnit(
    val id: String,
    val name: String,
    val symbol: String,
    val factorToBase: Double // Multiply by this factor to get Base unit
)

object UnitRegistry {
    // 1. Length (Base: Meter)
    val lengthUnits = listOf(
        ConversionUnit("km", "Kilometer", "km", 1000.0),
        ConversionUnit("m", "Meter", "m", 1.0),
        ConversionUnit("cm", "Centimeter", "cm", 0.01),
        ConversionUnit("mm", "Millimeter", "mm", 0.001),
        ConversionUnit("um", "Micrometer", "µm", 1e-6),
        ConversionUnit("nm", "Nanometer", "nm", 1e-9),
        ConversionUnit("mi", "Mile", "mi", 1609.344),
        ConversionUnit("yd", "Yard", "yd", 0.9144),
        ConversionUnit("ft", "Foot", "ft", 0.3048),
        ConversionUnit("in", "Inch", "in", 0.0254),
        ConversionUnit("nmi", "Nautical Mile", "NM", 1852.0)
    )

    // 2. Area (Base: Square Meter)
    val areaUnits = listOf(
        ConversionUnit("sq_km", "Square Kilometer", "km²", 1_000_000.0),
        ConversionUnit("sq_m", "Square Meter", "m²", 1.0),
        ConversionUnit("sq_cm", "Square Centimeter", "cm²", 0.0001),
        ConversionUnit("sq_mm", "Square Millimeter", "mm²", 0.000001),
        ConversionUnit("ha", "Hectare", "ha", 10000.0),
        ConversionUnit("ac", "Acre", "ac", 4046.8564224),
        ConversionUnit("sq_mi", "Square Mile", "mi²", 2589988.11),
        ConversionUnit("sq_yd", "Square Yard", "yd²", 0.83612736),
        ConversionUnit("sq_ft", "Square Foot", "ft²", 0.09290304),
        ConversionUnit("sq_in", "Square Inch", "in²", 0.00064516)
    )

    // 3. Volume (Base: Liter)
    val volumeUnits = listOf(
        ConversionUnit("l", "Liter", "L", 1.0),
        ConversionUnit("ml", "Milliliter", "mL", 0.001),
        ConversionUnit("cum", "Cubic Meter", "m³", 1000.0),
        ConversionUnit("cucm", "Cubic Centimeter", "cm³", 0.001),
        ConversionUnit("us_gal", "US Gallon", "gal", 3.785411784),
        ConversionUnit("us_qt", "US Quart", "qt", 0.946352946),
        ConversionUnit("us_pt", "US Pint", "pt", 0.473176473),
        ConversionUnit("us_cup", "US Cup", "cup", 0.2365882365),
        ConversionUnit("us_fl_oz", "US Fluid Ounce", "fl oz", 0.0295735295625),
        ConversionUnit("us_tbsp", "US Tablespoon", "tbsp", 0.01478676478125),
        ConversionUnit("us_tsp", "US Teaspoon", "tsp", 0.00492892159375),
        ConversionUnit("uk_gal", "Imperial Gallon", "imp gal", 4.54609),
        ConversionUnit("bbl", "Oil Barrel", "bbl", 158.987294928)
    )

    // 4. Weight (Base: Kilogram)
    val weightUnits = listOf(
        ConversionUnit("t", "Metric Ton", "t", 1000.0),
        ConversionUnit("kg", "Kilogram", "kg", 1.0),
        ConversionUnit("g", "Gram", "g", 0.001),
        ConversionUnit("mg", "Milligram", "mg", 0.000001),
        ConversionUnit("ug", "Microgram", "µg", 1e-9),
        ConversionUnit("lb", "Pound", "lb", 0.45359237),
        ConversionUnit("oz", "Ounce", "oz", 0.028349523125),
        ConversionUnit("st", "Stone", "st", 6.35029318),
        ConversionUnit("ct", "Carat", "ct", 0.0002),
        ConversionUnit("oz_t", "Troy Ounce", "oz t", 0.0311034768),
        ConversionUnit("gr", "Grain", "gr", 0.00006479891)
    )

    // 5. Speed (Base: m/s)
    val speedUnits = listOf(
        ConversionUnit("mps", "Meter per second", "m/s", 1.0),
        ConversionUnit("kmh", "Kilometer per hour", "km/h", 1.0 / 3.6),
        ConversionUnit("mph", "Mile per hour", "mph", 0.44704),
        ConversionUnit("knot", "Knot", "kn", 0.514444),
        ConversionUnit("fps", "Foot per second", "ft/s", 0.3048),
        ConversionUnit("mach", "Mach (Standard Sea Level)", "Mach", 340.29)
    )

    // 6. Pressure (Base: Pascal)
    val pressureUnits = listOf(
        ConversionUnit("pa", "Pascal", "Pa", 1.0),
        ConversionUnit("kpa", "Kilopascal", "kPa", 1000.0),
        ConversionUnit("mpa", "Megapascal", "MPa", 1_000_000.0),
        ConversionUnit("bar", "Bar", "bar", 100_000.0),
        ConversionUnit("mbar", "Millibar", "mbar", 100.0),
        ConversionUnit("psi", "Pound per Square Inch", "psi", 6894.757293),
        ConversionUnit("atm", "Standard Atmosphere", "atm", 101325.0),
        ConversionUnit("torr", "Torr / mmHg", "mmHg", 133.322368421),
        ConversionUnit("inhg", "Inch of Mercury", "inHg", 3386.388666)
    )

    // 7. Power (Base: Watt)
    val powerUnits = listOf(
        ConversionUnit("gw", "Gigawatt", "GW", 1e9),
        ConversionUnit("mw", "Megawatt", "MW", 1e6),
        ConversionUnit("kw", "Kilowatt", "kW", 1000.0),
        ConversionUnit("w", "Watt", "W", 1.0),
        ConversionUnit("hp", "Mechanical Horsepower", "hp", 745.699872),
        ConversionUnit("ps", "Metric Horsepower", "PS", 735.49875),
        ConversionUnit("btuh", "BTU per hour", "BTU/h", 0.29307107),
        ConversionUnit("ftlbps", "Foot-pound per second", "ft-lb/s", 1.3558179),
        ConversionUnit("kcalh", "Kilocalorie per hour", "kcal/h", 1.163)
    )

    fun getUnitsForCategory(category: ConverterCategory): List<ConversionUnit> {
        return when (category) {
            ConverterCategory.LENGTH -> lengthUnits
            ConverterCategory.AREA -> areaUnits
            ConverterCategory.VOLUME -> volumeUnits
            ConverterCategory.WEIGHT -> weightUnits
            ConverterCategory.SPEED -> speedUnits
            ConverterCategory.PRESSURE -> pressureUnits
            ConverterCategory.POWER -> powerUnits
            else -> emptyList()
        }
    }

    fun convertStandard(
        value: Double,
        fromUnit: ConversionUnit,
        toUnit: ConversionUnit
    ): Double {
        if (fromUnit.id == toUnit.id) return value
        val inBase = value * fromUnit.factorToBase
        return inBase / toUnit.factorToBase
    }

    // Temperature custom conversions
    enum class TempUnit(val id: String, val displayName: String, val symbol: String) {
        CELSIUS("c", "Celsius", "°C"),
        FAHRENHEIT("f", "Fahrenheit", "°F"),
        KELVIN("k", "Kelvin", "K"),
        RANKINE("r", "Rankine", "°R"),
        REAUMUR("re", "Réaumur", "°Ré")
    }

    fun convertTemperature(value: Double, from: TempUnit, to: TempUnit): Double {
        if (from == to) return value
        // Convert from source to Celsius first
        val inCelsius = when (from) {
            TempUnit.CELSIUS -> value
            TempUnit.FAHRENHEIT -> (value - 32.0) * (5.0 / 9.0)
            TempUnit.KELVIN -> value - 273.15
            TempUnit.RANKINE -> (value - 491.67) * (5.0 / 9.0)
            TempUnit.REAUMUR -> value * 1.25
        }
        // Convert Celsius to target
        return when (to) {
            TempUnit.CELSIUS -> inCelsius
            TempUnit.FAHRENHEIT -> (inCelsius * (9.0 / 5.0)) + 32.0
            TempUnit.KELVIN -> inCelsius + 273.15
            TempUnit.RANKINE -> (inCelsius + 273.15) * (9.0 / 5.0)
            TempUnit.REAUMUR -> inCelsius * 0.8
        }
    }

    fun getTemperatureFormulaDescription(from: TempUnit, to: TempUnit): String {
        return when (from to to) {
            TempUnit.CELSIUS to TempUnit.FAHRENHEIT -> "°F = (°C × 9/5) + 32"
            TempUnit.FAHRENHEIT to TempUnit.CELSIUS -> "°C = (°F − 32) × 5/9"
            TempUnit.CELSIUS to TempUnit.KELVIN -> "K = °C + 273.15"
            TempUnit.KELVIN to TempUnit.CELSIUS -> "°C = K − 273.15"
            TempUnit.FAHRENHEIT to TempUnit.KELVIN -> "K = (°F − 32) × 5/9 + 273.15"
            TempUnit.KELVIN to TempUnit.FAHRENHEIT -> "°F = (K − 273.15) × 9/5 + 32"
            else -> "Formula: ${from.symbol} → ${to.symbol}"
        }
    }

    // Number System conversion helpers
    fun formatNumberBase(valueStr: String, fromRadix: Int, toRadix: Int): String {
        val clean = valueStr.trim().replace(" ", "").replace("_", "")
        if (clean.isEmpty()) return "0"
        return try {
            val bigInt = clean.toBigInteger(fromRadix)
            val result = bigInt.toString(toRadix)
            when (toRadix) {
                16 -> result.uppercase()
                2 -> formatBinaryNibbles(result)
                else -> result
            }
        } catch (e: Exception) {
            "Invalid"
        }
    }

    private fun formatBinaryNibbles(binaryStr: String): String {
        val padLen = if (binaryStr.length % 4 == 0) binaryStr.length else ((binaryStr.length / 4) + 1) * 4
        val padded = binaryStr.padStart(padLen, '0')
        return padded.chunked(4).joinToString(" ")
    }
}

package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.currency.CurrencyInfo
import com.example.data.currency.CurrencyRateState
import com.example.data.currency.CurrencyRepository
import com.example.data.currency.defaultRates
import com.example.data.currency.supportedCurrencies
import com.example.domain.converter.ConversionUnit
import com.example.domain.converter.ConverterCategory
import com.example.domain.converter.UnitRegistry
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

enum class BmiUnitSystem {
    METRIC,    // kg, cm
    IMPERIAL   // lbs, ft & in
}

enum class BmiCategory(val label: String, val rangeDescription: String, val colorHex: Long) {
    SEVERELY_UNDERWEIGHT("Severely Underweight", "< 16.0", 0xFF3B82F6),
    UNDERWEIGHT("Underweight", "16.0 – 18.4", 0xFF60A5FA),
    NORMAL("Normal (Healthy)", "18.5 – 24.9", 0xFF10B981),
    OVERWEIGHT("Overweight", "25.0 – 29.9", 0xFFF59E0B),
    OBESE_CLASS_1("Obese (Class 1)", "30.0 – 34.9", 0xFFF97316),
    OBESE_CLASS_2("Obese (Class 2)", "35.0 – 39.9", 0xFFEF4444),
    OBESE_CLASS_3("Severe Obese (Class 3)", "≥ 40.0", 0xFFB91C1C)
}

data class ConverterUiState(
    val activeCategory: ConverterCategory = ConverterCategory.CURRENCY,
    val isCategoryGridActive: Boolean = true,
    // Currency: 5 active countries/currencies at a time
    val currencyBase: String = "USD",
    val activeCurrencyCodes: List<String> = listOf("USD", "EUR", "GBP", "JPY", "CAD"),
    val currencyTarget: String = "EUR",
    val currencyAmount: String = "100",
    val currencyRates: CurrencyRateState = CurrencyRateState(),
    val currencySearchQuery: String = "",

    // Standard Units Conversion
    val standardInput: String = "1",
    val standardFromUnitId: String = "",
    val standardToUnitId: String = "",

    // BMI
    val bmiUnitSystem: BmiUnitSystem = BmiUnitSystem.METRIC,
    val bmiWeightMetricKg: String = "70",
    val bmiHeightMetricCm: String = "175",
    val bmiWeightImperialLbs: String = "154",
    val bmiHeightImperialFeet: String = "5",
    val bmiHeightImperialInches: String = "9",

    // Temperature
    val tempInput: String = "25",
    val tempFromUnit: UnitRegistry.TempUnit = UnitRegistry.TempUnit.CELSIUS,
    val tempToUnit: UnitRegistry.TempUnit = UnitRegistry.TempUnit.FAHRENHEIT,

    // Number System
    val numDecimal: String = "42",
    val numBinary: String = "101010",
    val numHex: String = "2A",
    val numOctal: String = "52"
)

class ConverterViewModel(
    private val currencyRepository: CurrencyRepository = CurrencyRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConverterUiState())
    val uiState: StateFlow<ConverterUiState> = _uiState.asStateFlow()

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    private val standardFormatter = DecimalFormat("#,##0.######")

    init {
        // Initialize default units for standard conversion categories
        setCategory(ConverterCategory.CURRENCY)
        fetchLiveCurrencyRates()
    }

    fun setCategory(category: ConverterCategory) {
        val units = UnitRegistry.getUnitsForCategory(category)
        val defaultFrom = units.getOrNull(0)?.id ?: ""
        val defaultTo = units.getOrNull(1)?.id ?: units.getOrNull(0)?.id ?: ""

        _uiState.update { current ->
            current.copy(
                activeCategory = category,
                standardFromUnitId = defaultFrom,
                standardToUnitId = defaultTo
            )
        }
    }

    fun showCategoryGrid() {
        _uiState.update { it.copy(isCategoryGridActive = true) }
    }

    fun selectCategoryAndOpenDedicatedScreen(category: ConverterCategory) {
        setCategory(category)
        _uiState.update { it.copy(isCategoryGridActive = false) }
    }

    fun toggleCategoryGrid() {
        _uiState.update { it.copy(isCategoryGridActive = !it.isCategoryGridActive) }
    }

    // --- CURRENCY LOGIC (5 COUNTRIES AT A TIME + SEARCH & CLICK) ---
    fun setCurrencyBase(code: String) {
        _uiState.update { current ->
            val list = current.activeCurrencyCodes.toMutableList()
            if (!list.contains(code)) {
                if (list.isNotEmpty()) list[0] = code else list.add(code)
            }
            current.copy(currencyBase = code, activeCurrencyCodes = list)
        }
    }

    fun setCurrencyTarget(code: String) {
        _uiState.update { it.copy(currencyTarget = code) }
    }

    fun setCurrencySlot(slotIndex: Int, code: String) {
        _uiState.update { current ->
            val list = current.activeCurrencyCodes.toMutableList()
            if (slotIndex in list.indices) {
                list[slotIndex] = code
            } else if (list.size < 5) {
                list.add(code)
            }
            val newBase = if (slotIndex == 0) code else current.currencyBase
            current.copy(activeCurrencyCodes = list, currencyBase = newBase)
        }
    }

    fun setCurrencyAmount(amount: String) {
        val filtered = amount.filter { it.isDigit() || it == '.' }
        if (filtered.count { it == '.' } <= 1) {
            _uiState.update { it.copy(currencyAmount = filtered) }
        }
    }

    fun swapCurrency() {
        _uiState.update { current ->
            current.copy(
                currencyBase = current.currencyTarget,
                currencyTarget = current.currencyBase
            )
        }
    }

    fun swapCurrencySlots(indexA: Int, indexB: Int) {
        _uiState.update { current ->
            val list = current.activeCurrencyCodes.toMutableList()
            if (indexA in list.indices && indexB in list.indices) {
                val temp = list[indexA]
                list[indexA] = list[indexB]
                list[indexB] = temp
            }
            current.copy(activeCurrencyCodes = list)
        }
    }

    fun setCurrencySearchQuery(query: String) {
        _uiState.update { it.copy(currencySearchQuery = query) }
    }

    fun fetchLiveCurrencyRates() {
        viewModelScope.launch {
            _uiState.update { it.copy(currencyRates = it.currencyRates.copy(isLoading = true)) }
            val result = currencyRepository.fetchLiveRates("USD")
            result.onSuccess { (rates, updateTime) ->
                _uiState.update {
                    it.copy(
                        currencyRates = it.currencyRates.copy(
                            rates = rates,
                            lastUpdated = updateTime,
                            isLoading = false,
                            isLiveOnline = true,
                            errorMessage = null
                        )
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        currencyRates = it.currencyRates.copy(
                            isLoading = false,
                            isLiveOnline = false,
                            errorMessage = "Using offline rates (${err.message ?: "Network error"})"
                        )
                    )
                }
            }
        }
    }

    fun getCurrencyConvertedAmount(): String {
        return getConvertedAmountFor(_uiState.value.currencyTarget)
    }

    fun getConvertedAmountFor(targetCode: String): String {
        val state = _uiState.value
        val amount = state.currencyAmount.toDoubleOrNull() ?: 0.0
        val baseCode = state.currencyBase

        val rates = state.currencyRates.rates
        val baseRateToUsd = rates[baseCode] ?: defaultRates[baseCode] ?: 1.0
        val targetRateToUsd = rates[targetCode] ?: defaultRates[targetCode] ?: 1.0

        if (baseRateToUsd == 0.0) return "0.00"
        val converted = (amount / baseRateToUsd) * targetRateToUsd
        return DecimalFormat("#,##0.00##").format(converted)
    }

    fun getCurrencySingleRate(): String {
        return getSingleRateFor(_uiState.value.currencyTarget)
    }

    fun getSingleRateFor(targetCode: String): String {
        val state = _uiState.value
        val baseCode = state.currencyBase
        val rates = state.currencyRates.rates
        val baseRateToUsd = rates[baseCode] ?: defaultRates[baseCode] ?: 1.0
        val targetRateToUsd = rates[targetCode] ?: defaultRates[targetCode] ?: 1.0
        if (baseRateToUsd == 0.0) return "0.00"
        val singleRate = (1.0 / baseRateToUsd) * targetRateToUsd
        return DecimalFormat("#,##0.0000").format(singleRate)
    }

    // --- STANDARD UNIT CONVERSION LOGIC ---
    fun setStandardInput(input: String) {
        val filtered = input.filter { it.isDigit() || it == '.' || it == '-' }
        if (filtered.count { it == '.' } <= 1 && filtered.lastIndexOf('-') <= 0) {
            _uiState.update { it.copy(standardInput = filtered) }
        }
    }

    fun setStandardFromUnit(unitId: String) {
        _uiState.update { it.copy(standardFromUnitId = unitId) }
    }

    fun setStandardToUnit(unitId: String) {
        _uiState.update { it.copy(standardToUnitId = unitId) }
    }

    fun swapStandardUnits() {
        _uiState.update { current ->
            current.copy(
                standardFromUnitId = current.standardToUnitId,
                standardToUnitId = current.standardFromUnitId
            )
        }
    }

    fun getStandardConvertedResult(): String {
        val state = _uiState.value
        val value = state.standardInput.toDoubleOrNull() ?: 0.0
        val units = UnitRegistry.getUnitsForCategory(state.activeCategory)
        val fromUnit = units.find { it.id == state.standardFromUnitId } ?: units.firstOrNull() ?: return "0"
        val toUnit = units.find { it.id == state.standardToUnitId } ?: units.firstOrNull() ?: return "0"

        val converted = UnitRegistry.convertStandard(value, fromUnit, toUnit)
        return formatOutputNumber(converted)
    }

    fun getStandardAllUnitsMatrix(): List<Pair<ConversionUnit, String>> {
        val state = _uiState.value
        val value = state.standardInput.toDoubleOrNull() ?: 0.0
        val units = UnitRegistry.getUnitsForCategory(state.activeCategory)
        val fromUnit = units.find { it.id == state.standardFromUnitId } ?: units.firstOrNull() ?: return emptyList()

        return units.map { unit ->
            val result = UnitRegistry.convertStandard(value, fromUnit, unit)
            unit to formatOutputNumber(result)
        }
    }

    // --- TEMPERATURE LOGIC ---
    fun setTempInput(input: String) {
        val filtered = input.filter { it.isDigit() || it == '.' || it == '-' }
        if (filtered.count { it == '.' } <= 1 && filtered.lastIndexOf('-') <= 0) {
            _uiState.update { it.copy(tempInput = filtered) }
        }
    }

    fun setTempFrom(unit: UnitRegistry.TempUnit) {
        _uiState.update { it.copy(tempFromUnit = unit) }
    }

    fun setTempTo(unit: UnitRegistry.TempUnit) {
        _uiState.update { it.copy(tempToUnit = unit) }
    }

    fun swapTempUnits() {
        _uiState.update { current ->
            current.copy(
                tempFromUnit = current.tempToUnit,
                tempToUnit = current.tempFromUnit
            )
        }
    }

    fun getTempResult(): String {
        val state = _uiState.value
        val value = state.tempInput.toDoubleOrNull() ?: 0.0
        val res = UnitRegistry.convertTemperature(value, state.tempFromUnit, state.tempToUnit)
        return DecimalFormat("#,##0.##").format(res)
    }

    fun getTempAllMatrix(): List<Pair<UnitRegistry.TempUnit, String>> {
        val state = _uiState.value
        val value = state.tempInput.toDoubleOrNull() ?: 0.0
        return UnitRegistry.TempUnit.values().map { unit ->
            val res = UnitRegistry.convertTemperature(value, state.tempFromUnit, unit)
            unit to DecimalFormat("#,##0.##").format(res)
        }
    }

    // --- BMI LOGIC ---
    fun setBmiUnitSystem(system: BmiUnitSystem) {
        _uiState.update { it.copy(bmiUnitSystem = system) }
    }

    fun setBmiMetricWeight(kg: String) {
        _uiState.update { it.copy(bmiWeightMetricKg = kg.filter { c -> c.isDigit() || c == '.' }) }
    }

    fun setBmiMetricHeight(cm: String) {
        _uiState.update { it.copy(bmiHeightMetricCm = cm.filter { c -> c.isDigit() || c == '.' }) }
    }

    fun setBmiImperialWeight(lbs: String) {
        _uiState.update { it.copy(bmiWeightImperialLbs = lbs.filter { c -> c.isDigit() || c == '.' }) }
    }

    fun setBmiImperialFeet(ft: String) {
        _uiState.update { it.copy(bmiHeightImperialFeet = ft.filter { c -> c.isDigit() }) }
    }

    fun setBmiImperialInches(inch: String) {
        _uiState.update { it.copy(bmiHeightImperialInches = inch.filter { c -> c.isDigit() }) }
    }

    data class BmiCalculationResult(
        val bmiValue: Double,
        val formattedBmi: String,
        val category: BmiCategory,
        val idealWeightRangeText: String,
        val primeBmi: String,
        val heightInMeters: Double
    )

    fun calculateBmi(): BmiCalculationResult {
        val state = _uiState.value
        val (weightKg, heightMeters) = if (state.bmiUnitSystem == BmiUnitSystem.METRIC) {
            val w = state.bmiWeightMetricKg.toDoubleOrNull() ?: 70.0
            val h = (state.bmiHeightMetricCm.toDoubleOrNull() ?: 175.0) / 100.0
            w to h
        } else {
            val lbs = state.bmiWeightImperialLbs.toDoubleOrNull() ?: 154.0
            val ft = state.bmiHeightImperialFeet.toDoubleOrNull() ?: 5.0
            val inch = state.bmiHeightImperialInches.toDoubleOrNull() ?: 9.0
            val totalInches = (ft * 12.0) + inch
            val w = lbs * 0.45359237
            val h = totalInches * 0.0254
            w to h
        }

        val bmi = if (heightMeters > 0.3) weightKg / (heightMeters * heightMeters) else 0.0
        val category = when {
            bmi < 16.0 -> BmiCategory.SEVERELY_UNDERWEIGHT
            bmi < 18.5 -> BmiCategory.UNDERWEIGHT
            bmi < 25.0 -> BmiCategory.NORMAL
            bmi < 30.0 -> BmiCategory.OVERWEIGHT
            bmi < 35.0 -> BmiCategory.OBESE_CLASS_1
            bmi < 40.0 -> BmiCategory.OBESE_CLASS_2
            else -> BmiCategory.OBESE_CLASS_3
        }

        // Ideal weight for BMI 18.5 - 24.9
        val minIdealKg = 18.5 * (heightMeters * heightMeters)
        val maxIdealKg = 24.9 * (heightMeters * heightMeters)

        val idealText = if (state.bmiUnitSystem == BmiUnitSystem.METRIC) {
            "${DecimalFormat("0.0").format(minIdealKg)} kg – ${DecimalFormat("0.0").format(maxIdealKg)} kg"
        } else {
            val minLbs = minIdealKg / 0.45359237
            val maxLbs = maxIdealKg / 0.45359237
            "${DecimalFormat("0.0").format(minLbs)} lbs – ${DecimalFormat("0.0").format(maxLbs)} lbs"
        }

        val prime = if (bmi > 0) DecimalFormat("0.00").format(bmi / 25.0) else "0.00"

        return BmiCalculationResult(
            bmiValue = bmi,
            formattedBmi = DecimalFormat("0.0").format(bmi),
            category = category,
            idealWeightRangeText = idealText,
            primeBmi = prime,
            heightInMeters = heightMeters
        )
    }

    // --- NUMBER SYSTEM LOGIC ---
    fun onNumberSystemChanged(sourceBase: Int, value: String) {
        val clean = value.trim().replace(" ", "").replace("_", "")
        if (clean.isEmpty()) {
            _uiState.update {
                it.copy(
                    numDecimal = "",
                    numBinary = "",
                    numHex = "",
                    numOctal = ""
                )
            }
            return
        }

        try {
            val bigInt = clean.toBigInteger(sourceBase)
            val decStr = bigInt.toString(10)
            val binStr = UnitRegistry.formatNumberBase(decStr, 10, 2)
            val hexStr = UnitRegistry.formatNumberBase(decStr, 10, 16)
            val octStr = UnitRegistry.formatNumberBase(decStr, 10, 8)

            _uiState.update {
                it.copy(
                    numDecimal = decStr,
                    numBinary = binStr,
                    numHex = hexStr,
                    numOctal = octStr
                )
            }
        } catch (e: Exception) {
            // Keep the typed text in its own field
            when (sourceBase) {
                10 -> _uiState.update { it.copy(numDecimal = value) }
                2 -> _uiState.update { it.copy(numBinary = value) }
                16 -> _uiState.update { it.copy(numHex = value) }
                8 -> _uiState.update { it.copy(numOctal = value) }
            }
        }
    }

    private fun formatOutputNumber(value: Double): String {
        return if (value == 0.0) {
            "0"
        } else if (Math.abs(value) >= 1e9 || (Math.abs(value) <= 1e-5 && Math.abs(value) > 0)) {
            DecimalFormat("0.#####E0").format(value)
        } else {
            DecimalFormat("#,##0.######").format(value)
        }
    }

    fun showToast(message: String) {
        viewModelScope.launch {
            _userMessage.emit(message)
        }
    }

    // --- CONVERTER DIALPAD / KEYPAD INPUT HELPERS ---
    fun appendActiveValue(token: String) {
        val cat = _uiState.value.activeCategory
        when (cat) {
            ConverterCategory.CURRENCY -> {
                val cur = _uiState.value.currencyAmount
                if (token == "00") {
                    if (cur.isEmpty() || cur == "0") setCurrencyAmount("0")
                    else setCurrencyAmount(cur + "00")
                } else if (token == ".") {
                    if (!cur.contains('.')) setCurrencyAmount(if (cur.isEmpty()) "0." else "$cur.")
                } else {
                    if (cur == "0" && token != ".") setCurrencyAmount(token)
                    else setCurrencyAmount(cur + token)
                }
            }
            ConverterCategory.TEMPERATURE -> {
                val cur = _uiState.value.tempInput
                if (token == "00") {
                    if (cur.isEmpty() || cur == "0") setTempInput("0")
                    else setTempInput(cur + "00")
                } else if (token == ".") {
                    if (!cur.contains('.')) setTempInput(if (cur.isEmpty()) "0." else "$cur.")
                } else {
                    if (cur == "0" && token != ".") setTempInput(token)
                    else setTempInput(cur + token)
                }
            }
            ConverterCategory.BMI -> {
                if (_uiState.value.bmiUnitSystem == BmiUnitSystem.METRIC) {
                    val cur = _uiState.value.bmiWeightMetricKg
                    if (token == "00") {
                        if (cur.isNotEmpty() && cur != "0") setBmiMetricWeight(cur + "00")
                    } else if (token == ".") {
                        if (!cur.contains('.')) setBmiMetricWeight(if (cur.isEmpty()) "0." else "$cur.")
                    } else {
                        if (cur == "0" && token != ".") setBmiMetricWeight(token)
                        else setBmiMetricWeight(cur + token)
                    }
                } else {
                    val cur = _uiState.value.bmiWeightImperialLbs
                    if (token == "00") {
                        if (cur.isNotEmpty() && cur != "0") setBmiImperialWeight(cur + "00")
                    } else if (token == ".") {
                        if (!cur.contains('.')) setBmiImperialWeight(if (cur.isEmpty()) "0." else "$cur.")
                    } else {
                        if (cur == "0" && token != ".") setBmiImperialWeight(token)
                        else setBmiImperialWeight(cur + token)
                    }
                }
            }
            ConverterCategory.NUMBER_SYSTEM -> {
                val cur = _uiState.value.numDecimal
                if (token == "00") {
                    if (cur.isNotEmpty() && cur != "0") onNumberSystemChanged(10, cur + "00")
                } else if (token != ".") {
                    if (cur == "0") onNumberSystemChanged(10, token)
                    else onNumberSystemChanged(10, cur + token)
                }
            }
            else -> {
                // Standard Units (Length, Area, Volume, Weight, Speed, Pressure, Power)
                val cur = _uiState.value.standardInput
                if (token == "00") {
                    if (cur.isEmpty() || cur == "0") setStandardInput("0")
                    else setStandardInput(cur + "00")
                } else if (token == ".") {
                    if (!cur.contains('.')) setStandardInput(if (cur.isEmpty()) "0." else "$cur.")
                } else {
                    if (cur == "0" && token != ".") setStandardInput(token)
                    else setStandardInput(cur + token)
                }
            }
        }
    }

    fun backspaceActiveValue() {
        val cat = _uiState.value.activeCategory
        when (cat) {
            ConverterCategory.CURRENCY -> {
                val cur = _uiState.value.currencyAmount
                setCurrencyAmount(if (cur.length > 1) cur.dropLast(1) else "0")
            }
            ConverterCategory.TEMPERATURE -> {
                val cur = _uiState.value.tempInput
                setTempInput(if (cur.length > 1) cur.dropLast(1) else "0")
            }
            ConverterCategory.BMI -> {
                if (_uiState.value.bmiUnitSystem == BmiUnitSystem.METRIC) {
                    val cur = _uiState.value.bmiWeightMetricKg
                    setBmiMetricWeight(if (cur.length > 1) cur.dropLast(1) else "0")
                } else {
                    val cur = _uiState.value.bmiWeightImperialLbs
                    setBmiImperialWeight(if (cur.length > 1) cur.dropLast(1) else "0")
                }
            }
            ConverterCategory.NUMBER_SYSTEM -> {
                val cur = _uiState.value.numDecimal
                onNumberSystemChanged(10, if (cur.length > 1) cur.dropLast(1) else "0")
            }
            else -> {
                val cur = _uiState.value.standardInput
                setStandardInput(if (cur.length > 1) cur.dropLast(1) else "0")
            }
        }
    }

    fun clearActiveValue() {
        val cat = _uiState.value.activeCategory
        when (cat) {
            ConverterCategory.CURRENCY -> setCurrencyAmount("0")
            ConverterCategory.TEMPERATURE -> setTempInput("0")
            ConverterCategory.BMI -> {
                if (_uiState.value.bmiUnitSystem == BmiUnitSystem.METRIC) setBmiMetricWeight("0")
                else setBmiImperialWeight("0")
            }
            ConverterCategory.NUMBER_SYSTEM -> onNumberSystemChanged(10, "0")
            else -> setStandardInput("0")
        }
    }

    fun negateActiveValue() {
        val cat = _uiState.value.activeCategory
        when (cat) {
            ConverterCategory.TEMPERATURE -> {
                val cur = _uiState.value.tempInput
                if (cur.startsWith("-")) setTempInput(cur.removePrefix("-"))
                else if (cur.isNotEmpty() && cur != "0") setTempInput("-$cur")
            }
            else -> {
                val cur = _uiState.value.standardInput
                if (cur.startsWith("-")) setStandardInput(cur.removePrefix("-"))
                else if (cur.isNotEmpty() && cur != "0") setStandardInput("-$cur")
            }
        }
    }
}

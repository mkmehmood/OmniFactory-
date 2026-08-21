package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.CalculationEntity
import com.example.data.preferences.UserPreferencesManager
import com.example.data.repository.HistoryRepository
import com.example.domain.calculator.CalculationResult
import com.example.domain.calculator.CalculatorEngine
import com.example.ui.components.HistoryTimeSplit
import com.example.ui.theme.AccentTheme
import com.example.ui.theme.ShapeStructureStyle
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal

data class CalculatorUiState(
    val expression: String = "",
    val liveResult: String? = null,
    val isEvaluated: Boolean = false,
    val memory: BigDecimal = BigDecimal.ZERO,
    val showDoubleZeroKey: Boolean = true,
    val enableAnimations: Boolean = true,
    val hapticEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val shapeStructureStyle: ShapeStructureStyle = ShapeStructureStyle.ROUNDED,
    val themeMode: ThemeMode = ThemeMode.DARK,
    val accentTheme: AccentTheme = AccentTheme.AQUA,
    val historyTimeSplit: HistoryTimeSplit = HistoryTimeSplit.ALL_TIME,
    val isTapeOpen: Boolean = false,
    val isSettingsOpen: Boolean = false,
    val errorMessage: String? = null
)

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferencesManager = UserPreferencesManager(application)

    private val db = AppDatabase.getDatabase(application)
    private val repository: HistoryRepository = HistoryRepository(db.calculationDao())

    private val _uiState = MutableStateFlow(
        CalculatorUiState(
            shapeStructureStyle = userPreferencesManager.shapeStructureStyle,
            themeMode = userPreferencesManager.themeMode,
            accentTheme = userPreferencesManager.accentTheme,
            historyTimeSplit = userPreferencesManager.lastHistoryTimeSplit,
            showDoubleZeroKey = userPreferencesManager.showDoubleZeroKey,
            enableAnimations = userPreferencesManager.enableAnimations,
            hapticEnabled = userPreferencesManager.hapticEnabled,
            soundEnabled = userPreferencesManager.soundEnabled
        )
    )
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    private val _historyList = MutableStateFlow<List<CalculationEntity>>(emptyList())
    val historyList: StateFlow<List<CalculationEntity>> = _historyList.asStateFlow()

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.allHistory.collect { list ->
                _historyList.value = list
            }
        }
    }

    fun showToast(msg: String) {
        viewModelScope.launch {
            _userMessage.emit(msg)
        }
    }

    fun onDigit(digit: String) {
        val current = _uiState.value.expression
        val isNew = _uiState.value.isEvaluated

        if (digit == ".") {
            onDecimal()
            return
        }

        val newExpr = if (isNew || current == "0") {
            digit
        } else {
            // Check if current active token is just "0" and we are typing another digit -> replace the "0"
            val lastToken = current.split('+', '−', '-', '×', '*', '÷', '/', '(', ')').lastOrNull() ?: ""
            if (lastToken == "0") {
                current.dropLast(1) + digit
            } else {
                current + digit
            }
        }
        updateExpression(newExpr, isEvaluated = false)
    }

    private fun onDecimal() {
        val current = _uiState.value.expression
        val isNew = _uiState.value.isEvaluated

        if (isNew || current.isEmpty()) {
            updateExpression("0.", isEvaluated = false)
            return
        }

        val lastChar = current.last()
        if (lastChar in listOf('+', '−', '-', '×', '*', '÷', '/', '(')) {
            updateExpression(current + "0.", isEvaluated = false)
            return
        }

        // Check if the current number already contains a decimal point
        val lastToken = current.split('+', '−', '-', '×', '*', '÷', '/', '(', ')').lastOrNull() ?: ""
        if (lastToken.contains('.')) {
            // Already has decimal point in active token, ignore
            return
        }

        updateExpression(current + ".", isEvaluated = false)
    }

    fun onDoubleZero() {
        val current = _uiState.value.expression
        val isNew = _uiState.value.isEvaluated

        if (isNew || current.isEmpty() || current == "0") {
            updateExpression("0", isEvaluated = false)
            return
        }

        val lastChar = current.last()
        if (lastChar in listOf('+', '−', '-', '×', '*', '÷', '/', '(')) {
            updateExpression(current + "0", isEvaluated = false)
            return
        }

        val lastToken = current.split('+', '−', '-', '×', '*', '÷', '/', '(', ')').lastOrNull() ?: ""
        if (lastToken == "0") {
            return
        }

        updateExpression(current + "00", isEvaluated = false)
    }

    fun onOperator(op: String) {
        var current = _uiState.value.expression
        if (current.isEmpty()) {
            current = "0"
        }

        // If trailing decimal, drop it before appending operator
        if (current.endsWith('.')) {
            current = current.dropLast(1)
        }

        // If ends with another operator, replace it
        if (current.isNotEmpty() && current.last() in listOf('+', '−', '×', '÷')) {
            current = current.dropLast(1)
        }

        updateExpression(current + op, isEvaluated = false)
    }

    fun onParentheses() {
        val current = _uiState.value.expression
        val openCount = current.count { it == '(' }
        val closeCount = current.count { it == ')' }

        val newExpr = if (current.isEmpty() || current.last() in listOf('+', '−', '×', '÷', '(')) {
            current + "("
        } else if (openCount > closeCount) {
            current + ")"
        } else {
            current + "×("
        }

        updateExpression(newExpr, isEvaluated = false)
    }

    fun onPercent() {
        val current = _uiState.value.expression
        if (current.isEmpty() || current.last() in listOf('+', '−', '×', '÷', '(', '%', '.')) return

        updateExpression(current + "%", isEvaluated = false)
    }

    fun onClear() {
        updateExpression("", isEvaluated = false)
    }

    fun onBackspace() {
        val current = _uiState.value.expression
        val isNew = _uiState.value.isEvaluated
        if (isNew) {
            updateExpression("", isEvaluated = false)
        } else if (current.isNotEmpty()) {
            updateExpression(current.dropLast(1), isEvaluated = false)
        }
    }

    fun onNegate() {
        val current = _uiState.value.expression
        if (current.isEmpty() || current == "0") return

        val cleanCurrent = current.replace(",", "").trim()
        val num = cleanCurrent.toBigDecimalOrNull()
        if (num != null) {
            val negated = num.negate()
            updateExpression(CalculatorEngine.formatNumber(negated), isEvaluated = false)
        } else {
            updateExpression("-($current)", isEvaluated = false)
        }
    }

    fun onPaste(pasted: String) {
        if (pasted.isBlank()) {
            showToast("Clipboard is empty")
            return
        }
        val clean = pasted.trim()
            .replace(",", "")
            .replace("*", "×")
            .replace("/", "÷")
            .replace("-", "−")
            .filter { it.isDigit() || it in listOf('.', '+', '−', '×', '÷', '%', '(', ')') }

        if (clean.isEmpty()) {
            showToast("No valid numbers found in clipboard")
            return
        }

        val current = _uiState.value.expression
        val isNew = _uiState.value.isEvaluated

        val newExpr = if (isNew || current.isEmpty() || current == "0") {
            clean
        } else {
            current + clean
        }

        updateExpression(newExpr, isEvaluated = false)
        showToast("Pasted: $clean")
    }

    fun onEquals() {
        val current = _uiState.value.expression
        if (current.isBlank()) return

        val eval = CalculatorEngine.evaluate(current)
        when (eval) {
            is CalculationResult.Success -> {
                val formatted = CalculatorEngine.formatNumber(eval.value)
                viewModelScope.launch {
                    repository.insertCalculation(
                        expression = CalculatorEngine.formatExpressionForDisplay(current),
                        result = formatted,
                        tag = ""
                    )
                }
                _uiState.update {
                    it.copy(
                        expression = formatted,
                        liveResult = null,
                        isEvaluated = true,
                        errorMessage = null
                    )
                }
            }
            is CalculationResult.Error -> {
                _uiState.update {
                    it.copy(
                        errorMessage = eval.message,
                        liveResult = "Error"
                    )
                }
            }
            CalculationResult.Empty -> {}
        }
    }

    fun restoreCalculation(entity: CalculationEntity) {
        val current = _uiState.value.expression
        val toInsert = entity.result.replace(",", "").trim()
        val isNew = _uiState.value.isEvaluated

        val newExpr = if (isNew || current.isEmpty() || current == "0") {
            toInsert
        } else if (current.last() in listOf('+', '−', '-', '×', '*', '÷', '/', '(')) {
            current + toInsert
        } else {
            current + "+" + toInsert
        }
        updateExpression(newExpr, isEvaluated = false)
        showToast("Restored: $toInsert")
    }

    fun togglePin(entity: CalculationEntity) {
        viewModelScope.launch {
            repository.updateCalculation(entity.copy(isPinned = !entity.isPinned))
        }
    }

    fun reuseExpression(expression: String) {
        val clean = expression
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace(" ", "")
        updateExpression(clean, isEvaluated = false)
    }

    fun deleteCalculation(entity: CalculationEntity) {
        viewModelScope.launch {
            repository.deleteCalculation(entity)
        }
    }

    fun updateCalculationTag(entity: CalculationEntity, tag: String) {
        viewModelScope.launch {
            repository.updateCalculation(entity.copy(tag = tag))
        }
    }

    fun updateCalculationNote(entity: CalculationEntity, note: String) {
        viewModelScope.launch {
            repository.updateCalculation(entity.copy(note = note))
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAll()
            showToast("Calculation history cleared")
        }
    }

    fun setHistoryTimeSplit(split: HistoryTimeSplit) {
        userPreferencesManager.lastHistoryTimeSplit = split
        _uiState.update { it.copy(historyTimeSplit = split) }
    }

    // UI Configuration & Persistence Actions
    fun setShapeStructureStyle(style: ShapeStructureStyle) {
        userPreferencesManager.shapeStructureStyle = style
        _uiState.update { it.copy(shapeStructureStyle = style) }
    }

    fun setThemeMode(mode: ThemeMode) {
        userPreferencesManager.themeMode = mode
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun setAccentTheme(accent: AccentTheme) {
        userPreferencesManager.accentTheme = accent
        _uiState.update { it.copy(accentTheme = accent) }
    }

    fun setShowDoubleZeroKey(enabled: Boolean) {
        userPreferencesManager.showDoubleZeroKey = enabled
        _uiState.update { it.copy(showDoubleZeroKey = enabled) }
    }

    fun setEnableAnimations(enabled: Boolean) {
        userPreferencesManager.enableAnimations = enabled
        _uiState.update { it.copy(enableAnimations = enabled) }
    }

    fun setHapticEnabled(enabled: Boolean) {
        userPreferencesManager.hapticEnabled = enabled
        _uiState.update { it.copy(hapticEnabled = enabled) }
    }

    fun setSoundEnabled(enabled: Boolean) {
        userPreferencesManager.soundEnabled = enabled
        _uiState.update { it.copy(soundEnabled = enabled) }
    }

    fun setTapeOpen(open: Boolean) {
        _uiState.update { it.copy(isTapeOpen = open) }
    }

    fun setSettingsOpen(open: Boolean) {
        _uiState.update { it.copy(isSettingsOpen = open) }
    }

    private fun updateExpression(newExpr: String, isEvaluated: Boolean) {
        val live = if (newExpr.isNotEmpty() && !isEvaluated) {
            val eval = CalculatorEngine.evaluate(newExpr)
            if (eval is CalculationResult.Success) {
                CalculatorEngine.formatNumber(eval.value)
            } else null
        } else null

        _uiState.update {
            it.copy(
                expression = newExpr,
                liveResult = live,
                isEvaluated = isEvaluated,
                errorMessage = null
            )
        }
    }
}

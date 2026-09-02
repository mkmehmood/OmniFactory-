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
import com.example.ui.theme.AnimationIntensity
import com.example.ui.theme.AppFontStyle
import com.example.ui.theme.AppThemeSkin
import com.example.ui.theme.ComponentSizeStyle
import com.example.ui.theme.HapticIntensity
import com.example.ui.theme.ShapeStructureStyle
import com.example.ui.theme.SoundIntensity
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
    val appThemeSkin: AppThemeSkin = AppThemeSkin.DARK_OBSIDIAN,
    val fontStyle: AppFontStyle = AppFontStyle.SYSTEM,
    val animationIntensity: AnimationIntensity = AnimationIntensity.BALANCED,
    val hapticIntensity: HapticIntensity = HapticIntensity.MEDIUM,
    val soundIntensity: SoundIntensity = SoundIntensity.STANDARD,
    val componentSizeStyle: ComponentSizeStyle = ComponentSizeStyle.STANDARD,
    val shapeStructureStyle: ShapeStructureStyle = ShapeStructureStyle.ROUNDED,
    val themeMode: ThemeMode = ThemeMode.DARK,
    val historyTimeSplit: HistoryTimeSplit = HistoryTimeSplit.ALL_TIME,
    val isTapeOpen: Boolean = false,
    val isSettingsOpen: Boolean = false,
    val errorMessage: String? = null
) {
    val enableAnimations: Boolean get() = animationIntensity != AnimationIntensity.OFF
    val hapticEnabled: Boolean get() = hapticIntensity != HapticIntensity.OFF
    val soundEnabled: Boolean get() = soundIntensity != SoundIntensity.OFF
}

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferencesManager = UserPreferencesManager(application)

    private val db = AppDatabase.getDatabase(application)
    private val repository: HistoryRepository = HistoryRepository(db.calculationDao())

    private val _uiState = MutableStateFlow(
        CalculatorUiState(
            appThemeSkin = userPreferencesManager.appThemeSkin,
            fontStyle = userPreferencesManager.fontStyle,
            shapeStructureStyle = userPreferencesManager.shapeStructureStyle,
            componentSizeStyle = userPreferencesManager.componentSizeStyle,
            themeMode = userPreferencesManager.themeMode,
            animationIntensity = userPreferencesManager.animationIntensity,
            hapticIntensity = userPreferencesManager.hapticIntensity,
            soundIntensity = userPreferencesManager.soundIntensity,
            historyTimeSplit = userPreferencesManager.lastHistoryTimeSplit,
            showDoubleZeroKey = userPreferencesManager.showDoubleZeroKey
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
        } else if (current.endsWith("%")) {
            current + "×" + digit
        } else {
            val lastToken = current.split('+', '−', '-', '×', '*', '÷', '/', '(', ')', '%').lastOrNull() ?: ""
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

        if (current.endsWith("%")) {
            updateExpression(current + "×0.", isEvaluated = false)
            return
        }

        val lastChar = current.last()
        if (lastChar in listOf('+', '−', '-', '×', '*', '÷', '/', '(')) {
            updateExpression(current + "0.", isEvaluated = false)
            return
        }

        val lastToken = current.split('+', '−', '-', '×', '*', '÷', '/', '(', ')', '%').lastOrNull() ?: ""
        if (lastToken.contains('.')) {
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

        if (current.endsWith("%")) {
            return
        }

        val lastChar = current.last()
        if (lastChar in listOf('+', '−', '-', '×', '*', '÷', '/', '(')) {
            updateExpression(current + "0", isEvaluated = false)
            return
        }

        val lastToken = current.split('+', '−', '-', '×', '*', '÷', '/', '(', ')', '%').lastOrNull() ?: ""
        if (lastToken == "0") {
            return
        }

        updateExpression(current + "00", isEvaluated = false)
    }

    fun onOperator(op: String) {
        val current = _uiState.value.expression
        val normalizedOp = when (op) {
            "*" -> "×"
            "/" -> "÷"
            "-" -> "−"
            else -> op
        }

        if (current.isEmpty()) {
            if (normalizedOp == "−") {
                updateExpression("−", isEvaluated = false)
            }
            return
        }

        val lastChar = current.last().toString()
        if (lastChar in listOf("+", "−", "×", "÷")) {
            val replaced = current.dropLast(1) + normalizedOp
            updateExpression(replaced, isEvaluated = false)
            return
        }

        updateExpression(current + normalizedOp, isEvaluated = false)
    }

    fun onPercent() {
        val current = _uiState.value.expression
        if (current.isEmpty()) return

        val lastChar = current.last()
        if (lastChar in listOf('+', '−', '-', '×', '*', '÷', '/', '(', '%')) return

        updateExpression(current + "%", isEvaluated = false)
    }

    fun onNegate() {
        val current = _uiState.value.expression
        if (current.isEmpty()) return

        val tokens = current.split('+', '−', '×', '÷')
        val lastToken = tokens.lastOrNull() ?: return
        if (lastToken.isEmpty()) return

        val evalToken = CalculatorEngine.evaluate(lastToken)
        if (evalToken is CalculationResult.Success) {
            val negated = evalToken.value.negate()
            val formatted = CalculatorEngine.formatNumber(negated)
            val prefix = current.substring(0, current.length - lastToken.length)
            updateExpression(prefix + formatted, isEvaluated = false)
        }
    }

    fun onBackspace() {
        val current = _uiState.value.expression
        if (current.isNotEmpty()) {
            val newExpr = current.dropLast(1)
            updateExpression(newExpr, isEvaluated = false)
        }
    }

    fun onClear() {
        _uiState.update {
            it.copy(
                expression = "",
                liveResult = null,
                isEvaluated = false,
                errorMessage = null
            )
        }
    }

    fun onEquals() {
        val current = _uiState.value.expression
        if (current.isEmpty()) return

        val result = CalculatorEngine.evaluate(current)
        when (result) {
            is CalculationResult.Success -> {
                val formattedResult = CalculatorEngine.formatNumber(result.value)

                viewModelScope.launch {
                    repository.insertCalculation(
                        expression = current,
                        result = formattedResult,
                        tag = "General",
                        note = ""
                    )
                }

                _uiState.update {
                    it.copy(
                        expression = formattedResult,
                        liveResult = null,
                        isEvaluated = true,
                        errorMessage = null
                    )
                }
            }
            is CalculationResult.Error -> {
                _uiState.update {
                    it.copy(errorMessage = result.message)
                }
                showToast("Error: ${result.message}")
            }
            is CalculationResult.Empty -> {}
        }
    }

    fun onPaste(text: String) {
        val sanitized = text.filter { it.isDigit() || it in listOf('.', '+', '-', '*', '/', '%', '(', ')', '×', '÷', '−') }
            .replace("*", "×")
            .replace("/", "÷")
            .replace("-", "−")
        if (sanitized.isNotEmpty()) {
            updateExpression(sanitized, isEvaluated = false)
            showToast("Pasted expression")
        } else {
            showToast("Invalid clipboard content")
        }
    }

    fun restoreCalculation(entity: CalculationEntity) {
        val current = _uiState.value.expression
        val toInsert = entity.result
        val newExpr = if (current.isEmpty() || _uiState.value.isEvaluated) {
            toInsert
        } else if (current.last() in listOf('+', '−', '×', '÷', '(')) {
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

    fun setComponentSizeStyle(style: ComponentSizeStyle) {
        userPreferencesManager.componentSizeStyle = style
        _uiState.update { it.copy(componentSizeStyle = style) }
    }

    fun setAnimationIntensity(intensity: AnimationIntensity) {
        userPreferencesManager.animationIntensity = intensity
        _uiState.update { it.copy(animationIntensity = intensity) }
    }

    fun setHapticIntensity(intensity: HapticIntensity) {
        userPreferencesManager.hapticIntensity = intensity
        _uiState.update { it.copy(hapticIntensity = intensity) }
    }

    fun setSoundIntensity(intensity: SoundIntensity) {
        userPreferencesManager.soundIntensity = intensity
        _uiState.update { it.copy(soundIntensity = intensity) }
    }

    fun setAppThemeSkin(skin: AppThemeSkin) {
        userPreferencesManager.appThemeSkin = skin
        val targetMode = if (skin.isDark) ThemeMode.DARK else ThemeMode.LIGHT
        userPreferencesManager.themeMode = targetMode
        _uiState.update { it.copy(appThemeSkin = skin, themeMode = targetMode) }
    }

    fun setFontStyle(style: AppFontStyle) {
        userPreferencesManager.fontStyle = style
        _uiState.update { it.copy(fontStyle = style) }
    }

    fun setThemeMode(mode: ThemeMode) {
        userPreferencesManager.themeMode = mode
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun setShowDoubleZeroKey(enabled: Boolean) {
        userPreferencesManager.showDoubleZeroKey = enabled
        _uiState.update { it.copy(showDoubleZeroKey = enabled) }
    }

    fun reloadPreferences() {
        _uiState.update {
            it.copy(
                appThemeSkin = userPreferencesManager.appThemeSkin,
                fontStyle = userPreferencesManager.fontStyle,
                shapeStructureStyle = userPreferencesManager.shapeStructureStyle,
                componentSizeStyle = userPreferencesManager.componentSizeStyle,
                themeMode = userPreferencesManager.themeMode,
                animationIntensity = userPreferencesManager.animationIntensity,
                hapticIntensity = userPreferencesManager.hapticIntensity,
                soundIntensity = userPreferencesManager.soundIntensity,
                historyTimeSplit = userPreferencesManager.lastHistoryTimeSplit,
                showDoubleZeroKey = userPreferencesManager.showDoubleZeroKey
            )
        }
    }

    fun setEnableAnimations(enabled: Boolean) {
        userPreferencesManager.enableAnimations = enabled
        _uiState.update { it.copy(animationIntensity = userPreferencesManager.animationIntensity) }
    }

    fun setHapticEnabled(enabled: Boolean) {
        userPreferencesManager.hapticEnabled = enabled
        _uiState.update { it.copy(hapticIntensity = userPreferencesManager.hapticIntensity) }
    }

    fun setSoundEnabled(enabled: Boolean) {
        userPreferencesManager.soundEnabled = enabled
        _uiState.update { it.copy(soundIntensity = userPreferencesManager.soundIntensity) }
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

package com.example.ui

import androidx.lifecycle.ViewModel
import com.example.domain.calculator.BatchScalingEngine
import com.example.domain.calculator.BatchScalingOutput
import com.example.domain.calculator.BatchSubMode
import com.example.domain.calculator.RoundingOption
import com.example.domain.calculator.ScalingComponent
import com.example.domain.calculator.ScalingMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

data class ComponentInput(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val amountText: String,
    val isLocked: Boolean = false,
    val lockedValueText: String? = null
)

data class BatchScalingUiState(
    val components: List<ComponentInput> = defaultBreadComponents(),
    val scalingMode: ScalingMode = ScalingMode.TARGET_TOTAL,
    val batchSubMode: BatchSubMode = BatchSubMode.BY_BATCH_COUNT,
    val targetTotalText: String = "2000",
    val batchSizeText: String = "500",
    val batchCountText: String = "4",
    val availableAmountText: String = "2500",
    val roundingOption: RoundingOption = RoundingOption.ONE_DECIMAL,
    val calculationOutput: BatchScalingOutput = calculateState(
        components = defaultBreadComponents(),
        scalingMode = ScalingMode.TARGET_TOTAL,
        batchSubMode = BatchSubMode.BY_BATCH_COUNT,
        targetTotalText = "2000",
        batchSizeText = "500",
        batchCountText = "4",
        availableAmountText = "2500",
        roundingOption = RoundingOption.ONE_DECIMAL
    ),
    val isFormulaSectionExpanded: Boolean = true
)

private fun defaultBreadComponents() = listOf(
    ComponentInput(name = "Bread Flour", amountText = "1000", isLocked = false),
    ComponentInput(name = "Water (Hydration)", amountText = "720", isLocked = false),
    ComponentInput(name = "Sourdough Starter", amountText = "200", isLocked = false),
    ComponentInput(name = "Sea Salt", amountText = "20", isLocked = true)
)

data class ScalingPreset(
    val title: String,
    val description: String,
    val defaultTarget: String,
    val defaultBatchSize: String,
    val defaultBatchCount: String,
    val components: List<ComponentInput>
)

val BATCH_PRESETS = listOf(
    ScalingPreset(
        title = "Artisan Sourdough",
        description = "Flour, Water, Starter, Salt",
        defaultTarget = "2000",
        defaultBatchSize = "500",
        defaultBatchCount = "4",
        components = listOf(
            ComponentInput(name = "Bread Flour", amountText = "1000", isLocked = false),
            ComponentInput(name = "Water", amountText = "720", isLocked = false),
            ComponentInput(name = "Sourdough Starter", amountText = "200", isLocked = false),
            ComponentInput(name = "Sea Salt", amountText = "20", isLocked = true)
        )
    ),
    ScalingPreset(
        title = "Cookie Dough",
        description = "Flour, Butter, Sugar, Chips, Eggs",
        defaultTarget = "1500",
        defaultBatchSize = "300",
        defaultBatchCount = "5",
        components = listOf(
            ComponentInput(name = "All-Purpose Flour", amountText = "350", isLocked = false),
            ComponentInput(name = "Butter", amountText = "225", isLocked = false),
            ComponentInput(name = "Brown Sugar", amountText = "150", isLocked = false),
            ComponentInput(name = "White Sugar", amountText = "100", isLocked = false),
            ComponentInput(name = "Chocolate Chips", amountText = "200", isLocked = false),
            ComponentInput(name = "Eggs", amountText = "100", isLocked = true)
        )
    ),
    ScalingPreset(
        title = "Concrete Mix Ratio",
        description = "Cement, Sand, Gravel, Water",
        defaultTarget = "1000",
        defaultBatchSize = "250",
        defaultBatchCount = "4",
        components = listOf(
            ComponentInput(name = "Portland Cement", amountText = "150", isLocked = false),
            ComponentInput(name = "Fine Sand", amountText = "300", isLocked = false),
            ComponentInput(name = "Gravel Aggregate", amountText = "450", isLocked = false),
            ComponentInput(name = "Mixing Water", amountText = "75", isLocked = true)
        )
    ),
    ScalingPreset(
        title = "Fertilizer N-P-K Blend",
        description = "Nitrogen, Phosphate, Potash, Organic Filler",
        defaultTarget = "500",
        defaultBatchSize = "100",
        defaultBatchCount = "5",
        components = listOf(
            ComponentInput(name = "Nitrogen (N)", amountText = "30", isLocked = false),
            ComponentInput(name = "Phosphate (P2O5)", amountText = "15", isLocked = false),
            ComponentInput(name = "Potash (K2O)", amountText = "20", isLocked = false),
            ComponentInput(name = "Inert Filler", amountText = "35", isLocked = true)
        )
    ),
    ScalingPreset(
        title = "Craft Cocktail Punch",
        description = "Spirits, Citrus Juice, Syrup, Liqueur",
        defaultTarget = "3000",
        defaultBatchSize = "750",
        defaultBatchCount = "4",
        components = listOf(
            ComponentInput(name = "Bourbon Whiskey", amountText = "750", isLocked = false),
            ComponentInput(name = "Sweet Vermouth", amountText = "250", isLocked = false),
            ComponentInput(name = "Fresh Lemon Juice", amountText = "150", isLocked = false),
            ComponentInput(name = "Simple Syrup", amountText = "150", isLocked = false),
            ComponentInput(name = "Aromatic Bitters", amountText = "25", isLocked = true)
        )
    )
)

class BatchScalingViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(BatchScalingUiState())
    val uiState: StateFlow<BatchScalingUiState> = _uiState.asStateFlow()

    fun addComponent(name: String = "", amount: String = "100") {
        _uiState.update { current ->
            val updated = current.components + ComponentInput(
                name = name.ifBlank { "Component ${current.components.size + 1}" },
                amountText = amount,
                isLocked = false
            )
            recalculate(current.copy(components = updated))
        }
    }

    fun removeComponent(id: String) {
        _uiState.update { current ->
            val updated = current.components.filter { it.id != id }
            recalculate(current.copy(components = updated))
        }
    }

    fun updateComponentName(id: String, newName: String) {
        _uiState.update { current ->
            val updated = current.components.map {
                if (it.id == id) it.copy(name = newName) else it
            }
            recalculate(current.copy(components = updated))
        }
    }

    fun updateComponentAmount(id: String, newAmountText: String) {
        _uiState.update { current ->
            val updated = current.components.map {
                if (it.id == id) it.copy(amountText = newAmountText) else it
            }
            recalculate(current.copy(components = updated))
        }
    }

    fun toggleComponentLock(id: String) {
        _uiState.update { current ->
            val updated = current.components.map {
                if (it.id == id) it.copy(isLocked = !it.isLocked) else it
            }
            recalculate(current.copy(components = updated))
        }
    }

    fun setScalingMode(mode: ScalingMode) {
        _uiState.update { current ->
            recalculate(current.copy(scalingMode = mode))
        }
    }

    fun setBatchSubMode(subMode: BatchSubMode) {
        _uiState.update { current ->
            recalculate(current.copy(batchSubMode = subMode))
        }
    }

    fun setTargetTotal(targetText: String) {
        _uiState.update { current ->
            recalculate(current.copy(targetTotalText = targetText))
        }
    }

    fun setBatchSize(batchSizeText: String) {
        _uiState.update { current ->
            recalculate(current.copy(batchSizeText = batchSizeText))
        }
    }

    fun setBatchCount(batchCountText: String) {
        _uiState.update { current ->
            recalculate(current.copy(batchCountText = batchCountText))
        }
    }

    fun setAvailableAmount(availableText: String) {
        _uiState.update { current ->
            recalculate(current.copy(availableAmountText = availableText))
        }
    }

    fun setRoundingOption(option: RoundingOption) {
        _uiState.update { current ->
            recalculate(current.copy(roundingOption = option))
        }
    }

    fun toggleFormulaExpanded() {
        _uiState.update { it.copy(isFormulaSectionExpanded = !it.isFormulaSectionExpanded) }
    }

    fun applyMultiplier(factor: Double) {
        _uiState.update { current ->
            val origTotal = current.calculationOutput.originalGrandTotal
            val newTarget = if (origTotal > 0) (origTotal * factor).toString() else (1000 * factor).toString()
            recalculate(current.copy(targetTotalText = newTarget, scalingMode = ScalingMode.TARGET_TOTAL))
        }
    }

    fun loadPreset(preset: ScalingPreset) {
        _uiState.update { current ->
            val updated = current.copy(
                components = preset.components,
                targetTotalText = preset.defaultTarget,
                batchSizeText = preset.defaultBatchSize,
                batchCountText = preset.defaultBatchCount
            )
            recalculate(updated)
        }
    }

    fun clearAll() {
        _uiState.update { current ->
            val updated = current.copy(
                components = listOf(
                    ComponentInput(name = "Component A", amountText = "100", isLocked = false),
                    ComponentInput(name = "Component B", amountText = "100", isLocked = false)
                ),
                targetTotalText = "500",
                batchSizeText = "250",
                batchCountText = "2",
                availableAmountText = "1000"
            )
            recalculate(updated)
        }
    }

    private fun recalculate(state: BatchScalingUiState): BatchScalingUiState {
        val output = calculateState(
            components = state.components,
            scalingMode = state.scalingMode,
            batchSubMode = state.batchSubMode,
            targetTotalText = state.targetTotalText,
            batchSizeText = state.batchSizeText,
            batchCountText = state.batchCountText,
            availableAmountText = state.availableAmountText,
            roundingOption = state.roundingOption
        )
        return state.copy(calculationOutput = output)
    }
}

private fun calculateState(
    components: List<ComponentInput>,
    scalingMode: ScalingMode,
    batchSubMode: BatchSubMode,
    targetTotalText: String,
    batchSizeText: String,
    batchCountText: String,
    availableAmountText: String,
    roundingOption: RoundingOption
): BatchScalingOutput {
    val domainComponents = components.map {
        ScalingComponent(
            id = it.id,
            name = it.name.ifBlank { "Item" },
            originalAmount = it.amountText.toDoubleOrNull() ?: 0.0,
            isLocked = it.isLocked
        )
    }

    val targetTotal = targetTotalText.toDoubleOrNull() ?: 0.0
    val batchSize = batchSizeText.toDoubleOrNull() ?: 0.0
    val batchCount = batchCountText.toDoubleOrNull() ?: 1.0
    val availableAmount = availableAmountText.toDoubleOrNull() ?: 0.0

    return BatchScalingEngine.calculate(
        components = domainComponents,
        scalingMode = scalingMode,
        targetTotalInput = targetTotal,
        batchSizeInput = batchSize,
        batchSubMode = batchSubMode,
        batchCountInput = batchCount,
        availableAmountInput = availableAmount,
        roundingOption = roundingOption
    )
}

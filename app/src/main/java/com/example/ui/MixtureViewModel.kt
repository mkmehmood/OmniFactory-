package com.example.ui

import androidx.lifecycle.ViewModel
import com.example.domain.mixture.BatchDivisionResult
import com.example.domain.mixture.MixtureCalculatorEngine
import com.example.domain.mixture.RatioCalculationResult
import com.example.domain.mixture.RecalibrationResult
import com.example.domain.mixture.TargetIngredient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

data class MixtureUiState(
    val currentStep: Int = 1,
    val targetIngredients: List<TargetIngredient> = listOf(
        TargetIngredient(id = "ing_1", name = "Resin (Part A)", targetQuantity = 100.0, unit = "kg"),
        TargetIngredient(id = "ing_2", name = "Hardener (Part B)", targetQuantity = 50.0, unit = "kg"),
        TargetIngredient(id = "ing_3", name = "Pigment Paste", targetQuantity = 10.0, unit = "kg")
    ),
    val anchorIngredientId: String = "ing_1",
    val actualAddedTexts: Map<String, String> = mapOf(
        "ing_1" to "120",
        "ing_2" to "40",
        "ing_3" to "8"
    ),
    val newIngredientName: String = "",
    val newIngredientQty: String = "",
    val newIngredientUnit: String = "kg",
    val batchCountText: String = "3",
    val batchCount: Int = 3,
    val simplifiedRatio: RatioCalculationResult = MixtureCalculatorEngine.calculateSimplifiedRatio(
        listOf(
            TargetIngredient(id = "ing_1", name = "Resin (Part A)", targetQuantity = 100.0, unit = "kg"),
            TargetIngredient(id = "ing_2", name = "Hardener (Part B)", targetQuantity = 50.0, unit = "kg"),
            TargetIngredient(id = "ing_3", name = "Pigment Paste", targetQuantity = 10.0, unit = "kg")
        )
    ),
    val recalibrationResult: RecalibrationResult = MixtureCalculatorEngine.recalibrate(
        ingredients = listOf(
            TargetIngredient(id = "ing_1", name = "Resin (Part A)", targetQuantity = 100.0, unit = "kg"),
            TargetIngredient(id = "ing_2", name = "Hardener (Part B)", targetQuantity = 50.0, unit = "kg"),
            TargetIngredient(id = "ing_3", name = "Pigment Paste", targetQuantity = 10.0, unit = "kg")
        ),
        anchorId = "ing_1",
        actualAddedMap = mapOf("ing_1" to 120.0, "ing_2" to 40.0, "ing_3" to 8.0)
    ),
    val batchDivisionResult: BatchDivisionResult = MixtureCalculatorEngine.divideBatches(
        recalibrationResult = MixtureCalculatorEngine.recalibrate(
            ingredients = listOf(
                TargetIngredient(id = "ing_1", name = "Resin (Part A)", targetQuantity = 100.0, unit = "kg"),
                TargetIngredient(id = "ing_2", name = "Hardener (Part B)", targetQuantity = 50.0, unit = "kg"),
                TargetIngredient(id = "ing_3", name = "Pigment Paste", targetQuantity = 10.0, unit = "kg")
            ),
            anchorId = "ing_1",
            actualAddedMap = mapOf("ing_1" to 120.0, "ing_2" to 40.0, "ing_3" to 8.0)
        ),
        batchCount = 3
    )
)

class MixtureViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MixtureUiState())
    val uiState: StateFlow<MixtureUiState> = _uiState.asStateFlow()

    fun nextStep() {
        _uiState.update { current ->
            if (current.currentStep < 6) {
                val next = current.copy(currentStep = current.currentStep + 1)
                recalculate(next)
            } else {
                current
            }
        }
    }

    fun prevStep() {
        _uiState.update { current ->
            if (current.currentStep > 1) {
                val prev = current.copy(currentStep = current.currentStep - 1)
                recalculate(prev)
            } else {
                current
            }
        }
    }

    fun goToStep(step: Int) {
        val targetStep = step.coerceIn(1, 6)
        _uiState.update { current ->
            val updated = current.copy(currentStep = targetStep)
            recalculate(updated)
        }
    }

    fun resetAll() {
        _uiState.value = MixtureUiState(
            currentStep = 1,
            targetIngredients = emptyList(),
            anchorIngredientId = "",
            actualAddedTexts = emptyMap(),
            newIngredientName = "",
            newIngredientQty = "",
            newIngredientUnit = "kg",
            batchCountText = "2",
            batchCount = 2,
            simplifiedRatio = MixtureCalculatorEngine.calculateSimplifiedRatio(emptyList()),
            recalibrationResult = MixtureCalculatorEngine.recalibrate(emptyList(), "", emptyMap()),
            batchDivisionResult = MixtureCalculatorEngine.divideBatches(
                MixtureCalculatorEngine.recalibrate(emptyList(), "", emptyMap()),
                2
            )
        )
    }

    fun loadDefaultSample() {
        val sample = listOf(
            TargetIngredient(id = "ing_1", name = "Resin (Part A)", targetQuantity = 100.0, unit = "kg"),
            TargetIngredient(id = "ing_2", name = "Hardener (Part B)", targetQuantity = 50.0, unit = "kg"),
            TargetIngredient(id = "ing_3", name = "Pigment Paste", targetQuantity = 10.0, unit = "kg")
        )
        _uiState.update { current ->
            val updated = current.copy(
                targetIngredients = sample,
                anchorIngredientId = "ing_1",
                actualAddedTexts = mapOf("ing_1" to "120", "ing_2" to "40", "ing_3" to "8"),
                batchCountText = "3",
                batchCount = 3
            )
            recalculate(updated)
        }
    }

    fun setNewIngredientName(name: String) {
        _uiState.update { it.copy(newIngredientName = name) }
    }

    fun setNewIngredientQty(qty: String) {
        _uiState.update { it.copy(newIngredientQty = qty) }
    }

    fun setNewIngredientUnit(unit: String) {
        _uiState.update { it.copy(newIngredientUnit = unit) }
    }

    fun addTargetIngredient() {
        val state = _uiState.value
        val name = state.newIngredientName.trim()
        val qty = state.newIngredientQty.toDoubleOrNull() ?: 0.0
        val unit = state.newIngredientUnit.trim().ifEmpty { "kg" }

        if (name.isEmpty() || qty <= 0.0) return

        val newIngredient = TargetIngredient(
            id = UUID.randomUUID().toString(),
            name = name,
            targetQuantity = qty,
            unit = unit
        )

        _uiState.update { current ->
            val updatedList = current.targetIngredients + newIngredient
            val anchorId = if (current.anchorIngredientId.isEmpty()) newIngredient.id else current.anchorIngredientId
            val updatedActuals = current.actualAddedTexts + (newIngredient.id to qty.toString())
            val updated = current.copy(
                targetIngredients = updatedList,
                anchorIngredientId = anchorId,
                actualAddedTexts = updatedActuals,
                newIngredientName = "",
                newIngredientQty = ""
            )
            recalculate(updated)
        }
    }

    fun removeTargetIngredient(id: String) {
        _uiState.update { current ->
            val updatedList = current.targetIngredients.filter { it.id != id }
            val newAnchorId = if (current.anchorIngredientId == id) {
                updatedList.firstOrNull()?.id ?: ""
            } else {
                current.anchorIngredientId
            }
            val updatedActuals = current.actualAddedTexts - id
            val updated = current.copy(
                targetIngredients = updatedList,
                anchorIngredientId = newAnchorId,
                actualAddedTexts = updatedActuals
            )
            recalculate(updated)
        }
    }

    fun setAnchorIngredient(id: String) {
        _uiState.update { current ->
            val updated = current.copy(anchorIngredientId = id)
            recalculate(updated)
        }
    }

    fun setActualAdded(id: String, text: String) {
        _uiState.update { current ->
            val updatedMap = current.actualAddedTexts + (id to text)
            val updated = current.copy(actualAddedTexts = updatedMap)
            recalculate(updated)
        }
    }

    fun setBatchCountText(text: String) {
        val count = text.toIntOrNull()?.coerceAtLeast(1) ?: 1
        _uiState.update { current ->
            val updated = current.copy(batchCountText = text, batchCount = count)
            recalculate(updated)
        }
    }

    fun incrementBatchCount() {
        val newCount = _uiState.value.batchCount + 1
        setBatchCountText(newCount.toString())
    }

    fun decrementBatchCount() {
        val newCount = maxOf(1, _uiState.value.batchCount - 1)
        setBatchCountText(newCount.toString())
    }

    private fun recalculate(state: MixtureUiState): MixtureUiState {
        val ratioResult = MixtureCalculatorEngine.calculateSimplifiedRatio(state.targetIngredients)

        val actualMap = state.targetIngredients.associate { item ->
            val text = state.actualAddedTexts[item.id]
            val numeric = text?.toDoubleOrNull() ?: item.targetQuantity
            item.id to numeric
        }

        val anchorId = if (state.anchorIngredientId.isNotEmpty() && state.targetIngredients.any { it.id == state.anchorIngredientId }) {
            state.anchorIngredientId
        } else {
            state.targetIngredients.firstOrNull()?.id ?: ""
        }

        val recalResult = MixtureCalculatorEngine.recalibrate(
            ingredients = state.targetIngredients,
            anchorId = anchorId,
            actualAddedMap = actualMap
        )

        val batchResult = MixtureCalculatorEngine.divideBatches(
            recalibrationResult = recalResult,
            batchCount = state.batchCount
        )

        return state.copy(
            anchorIngredientId = anchorId,
            simplifiedRatio = ratioResult,
            recalibrationResult = recalResult,
            batchDivisionResult = batchResult
        )
    }
}

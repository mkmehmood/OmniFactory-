package com.example.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.DatabaseBackupManager
import com.example.data.db.MixtureRecipeEntity
import com.example.data.db.RestoreMode
import com.example.data.db.RestoreSummary
import com.example.domain.mixture.BatchDivisionResult
import com.example.domain.mixture.MixtureCalculatorEngine
import com.example.domain.mixture.RatioCalculationResult
import com.example.domain.mixture.RecalibrationResult
import com.example.domain.mixture.TargetIngredient
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import com.example.domain.mixture.MixtureAdjustmentMode
import java.util.UUID

data class MixtureUiState(
    val currentStep: Int = 1,
    val targetIngredients: List<TargetIngredient> = emptyList(),
    val anchorIngredientId: String = "",
    val actualAddedTexts: Map<String, String> = emptyMap(),
    val newIngredientName: String = "",
    val newIngredientQty: String = "",
    val newIngredientUnit: String = "kg",
    val batchCountText: String = "1",
    val batchCount: Int = 1,
    val currentRecipeTitle: String = "Custom Formulation",
    val showDatabaseBrowserSheet: Boolean = false,
    val showSaveRecipeDialog: Boolean = false,
    val showBackupRestoreDialog: Boolean = false,
    val historySearchQuery: String = "",
    val historyCategoryFilter: String = "ALL",
    val selectedAdjustmentMode: MixtureAdjustmentMode = MixtureAdjustmentMode.ADD_TO_BALANCE,
    val showCompareBothModes: Boolean = false,
    val simplifiedRatio: RatioCalculationResult = MixtureCalculatorEngine.calculateSimplifiedRatio(emptyList()),
    val recalibrationResult: RecalibrationResult = MixtureCalculatorEngine.recalibrate(emptyList(), "", emptyMap(), MixtureAdjustmentMode.ADD_TO_BALANCE),
    val batchDivisionResult: BatchDivisionResult = MixtureCalculatorEngine.divideBatches(
        MixtureCalculatorEngine.recalibrate(emptyList(), "", emptyMap(), MixtureAdjustmentMode.ADD_TO_BALANCE),
        1
    )
)

class MixtureViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val mixtureDao = database.mixtureRecipeDao()

    private val _uiState = MutableStateFlow(MixtureUiState())
    val uiState: StateFlow<MixtureUiState> = _uiState.asStateFlow()

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    val persistentRecipes: StateFlow<List<MixtureRecipeEntity>> = mixtureDao.getAllRecipes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    fun setDatabaseBrowserSheetOpen(isOpen: Boolean) {
        _uiState.update { it.copy(showDatabaseBrowserSheet = isOpen) }
    }

    fun setSaveRecipeDialogOpen(isOpen: Boolean) {
        _uiState.update { it.copy(showSaveRecipeDialog = isOpen) }
    }

    fun setBackupRestoreDialogOpen(isOpen: Boolean) {
        _uiState.update { it.copy(showBackupRestoreDialog = isOpen) }
    }

    fun setHistorySearchQuery(query: String) {
        _uiState.update { it.copy(historySearchQuery = query) }
    }

    fun setHistoryCategoryFilter(category: String) {
        _uiState.update { it.copy(historyCategoryFilter = category) }
    }

    fun loadDefaultSample() {
        // Kept for backward compatibility
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
            batchCountText = "1",
            batchCount = 1,
            currentRecipeTitle = "Custom Formulation",
            selectedAdjustmentMode = MixtureAdjustmentMode.ADD_TO_BALANCE,
            showCompareBothModes = false,
            simplifiedRatio = MixtureCalculatorEngine.calculateSimplifiedRatio(emptyList()),
            recalibrationResult = MixtureCalculatorEngine.recalibrate(emptyList(), "", emptyMap(), MixtureAdjustmentMode.ADD_TO_BALANCE),
            batchDivisionResult = MixtureCalculatorEngine.divideBatches(
                MixtureCalculatorEngine.recalibrate(emptyList(), "", emptyMap(), MixtureAdjustmentMode.ADD_TO_BALANCE),
                1
            )
        )
    }

    fun loadPreset(recipeType: String) {
        val (title, ingredients, anchorIdx, actuals, batchCount) = when (recipeType) {
            "concrete" -> {
                val items = listOf(
                    TargetIngredient(id = "ing_c35_1", name = "Portland Cement (CEM I 52.5N)", targetQuantity = 400.0, unit = "kg"),
                    TargetIngredient(id = "ing_c35_2", name = "Fine Silica Sand (0-4mm)", targetQuantity = 720.0, unit = "kg"),
                    TargetIngredient(id = "ing_c35_3", name = "Crushed Basalt Aggregate (10-20mm)", targetQuantity = 1150.0, unit = "kg"),
                    TargetIngredient(id = "ing_c35_4", name = "Batching Water", targetQuantity = 160.0, unit = "kg"),
                    TargetIngredient(id = "ing_c35_5", name = "Superplasticizer Admixture", targetQuantity = 20.0, unit = "kg")
                )
                Tuple5("High-Strength Structural Concrete (C35/45)", items, "ing_c35_1", mapOf(
                    "ing_c35_1" to "480",
                    "ing_c35_2" to "800",
                    "ing_c35_3" to "1300",
                    "ing_c35_4" to "180",
                    "ing_c35_5" to "22"
                ), 4)
            }
            "epoxy" -> {
                val items = listOf(
                    TargetIngredient(id = "ing_epx_1", name = "Epoxy Base Resin (Part A)", targetQuantity = 100.0, unit = "kg"),
                    TargetIngredient(id = "ing_epx_2", name = "Amine Hardener (Part B)", targetQuantity = 50.0, unit = "kg"),
                    TargetIngredient(id = "ing_epx_3", name = "Silica Fume Toughener", targetQuantity = 10.0, unit = "kg")
                )
                Tuple5("Aerospace Structural Epoxy Matrix (2:1)", items, "ing_epx_1", mapOf(
                    "ing_epx_1" to "120",
                    "ing_epx_2" to "50",
                    "ing_epx_3" to "8"
                ), 3)
            }
            "mortar" -> {
                val items = listOf(
                    TargetIngredient(id = "ing_mor_1", name = "Portland Cement", targetQuantity = 50.0, unit = "kg"),
                    TargetIngredient(id = "ing_mor_2", name = "Hydrated Lime Type S", targetQuantity = 15.0, unit = "kg"),
                    TargetIngredient(id = "ing_mor_3", name = "Washed River Sand", targetQuantity = 135.0, unit = "kg")
                )
                Tuple5("Structural Masonry Mortar (M10)", items, "ing_mor_1", mapOf(
                    "ing_mor_1" to "60",
                    "ing_mor_2" to "16",
                    "ing_mor_3" to "150"
                ), 2)
            }
            else -> {
                val items = listOf(
                    TargetIngredient(id = "ing_c35_1", name = "Portland Cement (CEM I 52.5N)", targetQuantity = 400.0, unit = "kg"),
                    TargetIngredient(id = "ing_c35_2", name = "Fine Silica Sand (0-4mm)", targetQuantity = 720.0, unit = "kg"),
                    TargetIngredient(id = "ing_c35_3", name = "Crushed Basalt Aggregate (10-20mm)", targetQuantity = 1150.0, unit = "kg"),
                    TargetIngredient(id = "ing_c35_4", name = "Batching Water", targetQuantity = 160.0, unit = "kg"),
                    TargetIngredient(id = "ing_c35_5", name = "Superplasticizer Admixture", targetQuantity = 20.0, unit = "kg")
                )
                Tuple5("High-Strength Structural Concrete (C35/45)", items, "ing_c35_1", mapOf(
                    "ing_c35_1" to "480",
                    "ing_c35_2" to "800",
                    "ing_c35_3" to "1300",
                    "ing_c35_4" to "180",
                    "ing_c35_5" to "22"
                ), 4)
            }
        }

        _uiState.update { current ->
            val updated = current.copy(
                currentRecipeTitle = title,
                targetIngredients = ingredients,
                anchorIngredientId = anchorIdx,
                actualAddedTexts = actuals,
                batchCountText = batchCount.toString(),
                batchCount = batchCount
            )
            recalculate(updated)
        }
    }

    fun loadRecipeFromDb(entity: MixtureRecipeEntity) {
        val parsedIngredients = mutableListOf<TargetIngredient>()
        val actualsMap = mutableMapOf<String, String>()
        try {
            val array = JSONArray(entity.ingredientsJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = "ing_db_${entity.id}_$i"
                val name = obj.optString("name", "Component ${i + 1}")
                val qty = obj.optDouble("quantity", 100.0)
                val unit = obj.optString("unit", entity.unit)
                val ingredient = TargetIngredient(id = id, name = name, targetQuantity = qty, unit = unit)
                parsedIngredients.add(ingredient)
                actualsMap[id] = qty.toString()
            }
        } catch (e: Exception) {
            // Fallback
        }

        if (parsedIngredients.isNotEmpty()) {
            val anchorId = parsedIngredients.find { it.name.contains(entity.anchorIngredientName, ignoreCase = true) }?.id
                ?: parsedIngredients.first().id

            _uiState.update { current ->
                val updated = current.copy(
                    currentRecipeTitle = entity.title,
                    targetIngredients = parsedIngredients,
                    anchorIngredientId = anchorId,
                    actualAddedTexts = actualsMap,
                    batchCountText = entity.batchCount.toString(),
                    batchCount = entity.batchCount,
                    showDatabaseBrowserSheet = false
                )
                recalculate(updated)
            }
            viewModelScope.launch {
                _userMessage.emit("Loaded '${entity.title}' from database")
            }
        }
    }

    fun saveCurrentRecipeToDb(title: String = "", category: String = "", description: String = "") {
        val state = _uiState.value
        if (state.targetIngredients.isEmpty()) return

        val array = JSONArray()
        for (item in state.targetIngredients) {
            val obj = JSONObject()
            obj.put("name", item.name)
            obj.put("quantity", item.targetQuantity)
            obj.put("unit", item.unit)
            array.put(obj)
        }

        val anchorName = state.targetIngredients.find { it.id == state.anchorIngredientId }?.name ?: "Primary"
        val totalWeight = state.targetIngredients.sumOf { it.targetQuantity }

        val recalibratedArray = JSONArray()
        for (prop in state.recalibrationResult.proportions) {
            val obj = JSONObject().apply {
                put("ingredientId", prop.ingredientId)
                put("ingredientName", prop.ingredientName)
                put("unit", prop.unit)
                put("targetQty", prop.targetQty)
                put("newRequiredTotal", prop.newRequiredTotal)
                put("alreadyAdded", prop.alreadyAdded)
                put("stillNeeded", prop.stillNeeded)
                put("isAnchor", prop.isAnchor)
                put("formattedTarget", prop.formattedTarget)
                put("formattedNewTotal", prop.formattedNewTotal)
                put("formattedAlreadyAdded", prop.formattedAlreadyAdded)
                put("formattedStillNeeded", prop.formattedStillNeeded)
            }
            recalibratedArray.put(obj)
        }

        val batchItemsArray = JSONArray()
        for (item in state.batchDivisionResult.items) {
            val obj = JSONObject().apply {
                put("ingredientName", item.ingredientName)
                put("amountPerBatch", item.amountPerBatch)
                put("unit", item.unit)
                put("totalAmount", item.totalAmount)
                put("formattedPerBatch", item.formattedPerBatch)
                put("formattedTotal", item.formattedTotal)
            }
            batchItemsArray.put(obj)
        }

        val entity = MixtureRecipeEntity(
            title = title.ifEmpty { state.currentRecipeTitle },
            category = category.ifEmpty { "Structural" },
            description = description.ifEmpty { "Persistent calibrated formulation" },
            anchorIngredientName = anchorName,
            scaleFactor = state.recalibrationResult.scaleFactor,
            totalTargetWeight = totalWeight,
            unit = state.targetIngredients.firstOrNull()?.unit ?: "kg",
            ingredientsJson = array.toString(),
            batchCount = state.batchCount,
            simplifiedRatioString = state.simplifiedRatio.simplifiedRatioString,
            anchorTargetQuantity = state.recalibrationResult.anchorTargetQty,
            anchorActualAdded = state.recalibrationResult.anchorActualAdded,
            scaleExplanation = state.recalibrationResult.scaleExplanation,
            recalibratedProportionsJson = recalibratedArray.toString(),
            totalNewMixtureWeight = state.recalibrationResult.totalNewMixtureWeight,
            batchDivisionJson = batchItemsArray.toString(),
            totalWeightPerBatch = state.batchDivisionResult.totalWeightPerBatch
        )

        viewModelScope.launch {
            mixtureDao.insert(entity)
            _uiState.update { it.copy(showSaveRecipeDialog = false) }
            _userMessage.emit("Formulation '$title' permanently saved to database")
        }
    }

    fun deleteRecipeFromDb(entity: MixtureRecipeEntity) {
        viewModelScope.launch {
            mixtureDao.delete(entity)
            _userMessage.emit("Removed formulation from database")
        }
    }

    fun updateRecipeInDb(entity: MixtureRecipeEntity, newTitle: String, newCategory: String, newDescription: String) {
        viewModelScope.launch {
            val updated = entity.copy(
                title = newTitle.ifBlank { entity.title },
                category = newCategory.ifBlank { entity.category },
                description = newDescription
            )
            mixtureDao.update(updated)
            _userMessage.emit("Formulation '${updated.title}' updated")
        }
    }

    fun toggleFavorite(entity: MixtureRecipeEntity) {
        viewModelScope.launch {
            val updated = entity.copy(isFavorite = !entity.isFavorite)
            mixtureDao.update(updated)
        }
    }

    fun clearAllRecipes() {
        viewModelScope.launch {
            mixtureDao.clearAll()
            _userMessage.emit("Cleared all mixture recipes")
        }
    }

    suspend fun exportBackupJson(): String {
        val backupManager = DatabaseBackupManager(getApplication())
        return backupManager.exportMixtureBackupJson()
    }

    fun restoreBackupFromJson(json: String, mode: RestoreMode) {
        viewModelScope.launch {
            val backupManager = DatabaseBackupManager(getApplication())
            val result = backupManager.restoreMixtureFromJson(json, mode)
            _userMessage.emit(result.message)
        }
    }

    fun saveBackupToUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            val backupManager = DatabaseBackupManager(context)
            val json = backupManager.exportMixtureBackupJson()
            val ok = backupManager.writeBackupToUri(context, uri, json)
            if (ok) {
                _userMessage.emit("Mixture formulation backup saved successfully.")
            } else {
                _userMessage.emit("Failed to save mixture backup file.")
            }
        }
    }

    fun loadBackupFromUri(context: Context, uri: Uri, mode: RestoreMode) {
        viewModelScope.launch {
            val backupManager = DatabaseBackupManager(context)
            val content = backupManager.readBackupFromUri(context, uri)
            if (!content.isNullOrBlank()) {
                val result = backupManager.restoreMixtureFromJson(content, mode)
                _userMessage.emit(result.message)
            } else {
                _userMessage.emit("Failed to read mixture backup file.")
            }
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
        val qty = state.newIngredientQty.replace(",", ".").replace(" ", "").trim().toDoubleOrNull() ?: 0.0
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
            val isFirst = current.targetIngredients.isEmpty()
            val initialActual = if (isFirst) qty.toString() else "0"
            val updatedActuals = current.actualAddedTexts + (newIngredient.id to initialActual)
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
        val clean = text.replace(",", ".").replace(" ", "").trim()
        val countDouble = clean.toDoubleOrNull()?.coerceAtLeast(0.01) ?: 1.0
        val countInt = countDouble.toInt().coerceAtLeast(1)
        _uiState.update { current ->
            val updated = current.copy(batchCountText = text, batchCount = countInt)
            recalculate(updated)
        }
    }

    fun incrementBatchCount() {
        val currentDouble = _uiState.value.batchCountText.replace(",", ".").toDoubleOrNull() ?: _uiState.value.batchCount.toDouble()
        val newCount = (currentDouble + 1.0)
        val formatted = if (newCount % 1.0 == 0.0) newCount.toInt().toString() else String.format(java.util.Locale.US, "%.1f", newCount)
        setBatchCountText(formatted)
    }

    fun decrementBatchCount() {
        val currentDouble = _uiState.value.batchCountText.replace(",", ".").toDoubleOrNull() ?: _uiState.value.batchCount.toDouble()
        val newCount = maxOf(0.5, currentDouble - 1.0)
        val formatted = if (newCount % 1.0 == 0.0) newCount.toInt().toString() else String.format(java.util.Locale.US, "%.1f", newCount)
        setBatchCountText(formatted)
    }

    fun setAdjustmentMode(mode: MixtureAdjustmentMode) {
        _uiState.update { current ->
            val updated = current.copy(selectedAdjustmentMode = mode)
            recalculate(updated)
        }
    }

    fun toggleCompareBothModes() {
        _uiState.update { it.copy(showCompareBothModes = !it.showCompareBothModes) }
    }

    fun setAllActualsToZeroExceptAnchor() {
        _uiState.update { current ->
            val anchor = current.targetIngredients.find { it.id == current.anchorIngredientId }
                ?: current.targetIngredients.firstOrNull()
            val anchorActual = current.actualAddedTexts[anchor?.id] ?: anchor?.targetQuantity?.toString() ?: "0"
            val updatedActuals = current.targetIngredients.associate { item ->
                if (item.id == anchor?.id) {
                    item.id to anchorActual
                } else {
                    item.id to "0"
                }
            }
            val updated = current.copy(actualAddedTexts = updatedActuals)
            recalculate(updated)
        }
    }

    fun setAllActualsToTarget() {
        _uiState.update { current ->
            val updatedActuals = current.targetIngredients.associate { item ->
                val text = if (item.targetQuantity % 1.0 == 0.0) item.targetQuantity.toLong().toString() else item.targetQuantity.toString()
                item.id to text
            }
            val updated = current.copy(actualAddedTexts = updatedActuals)
            recalculate(updated)
        }
    }

    private fun recalculate(state: MixtureUiState): MixtureUiState {
        val ratioResult = MixtureCalculatorEngine.calculateSimplifiedRatio(state.targetIngredients)

        val actualMap = state.targetIngredients.associate { item ->
            val text = state.actualAddedTexts[item.id]
            val numeric = if (text != null && text.isBlank()) {
                0.0
            } else if (text != null) {
                text.replace(",", ".").replace(" ", "").trim().toDoubleOrNull() ?: 0.0
            } else {
                if (item.id == state.anchorIngredientId) item.targetQuantity else 0.0
            }
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
            actualAddedMap = actualMap,
            mode = state.selectedAdjustmentMode
        )

        val countDouble = state.batchCountText.replace(",", ".").replace(" ", "").trim().toDoubleOrNull()?.coerceAtLeast(0.01)
            ?: state.batchCount.toDouble()

        val batchResult = MixtureCalculatorEngine.divideBatches(
            recalibrationResult = recalResult,
            batchCount = countDouble
        )

        return state.copy(
            anchorIngredientId = anchorId,
            simplifiedRatio = ratioResult,
            recalibrationResult = recalResult,
            batchDivisionResult = batchResult
        )
    }

    private data class Tuple5<A, B, C, D, E>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D,
        val fifth: E
    )
}

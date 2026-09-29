package com.example.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.BatchScalingEntity
import com.example.data.db.DatabaseBackupManager
import com.example.data.db.RestoreMode
import com.example.domain.calculator.BatchScalingEngine
import com.example.domain.calculator.BatchScalingOutput
import com.example.domain.calculator.BatchSubMode
import com.example.domain.calculator.RoundingOption
import com.example.domain.calculator.ScalingComponent
import com.example.domain.calculator.ScalingMode
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
import java.util.UUID

data class ComponentInput(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val amountText: String,
    val isLocked: Boolean = false,
    val lockedValueText: String? = null
)

data class BatchScalingUiState(
    val components: List<ComponentInput> = initialComponents(),
    val scalingMode: ScalingMode = ScalingMode.TARGET_TOTAL,
    val batchSubMode: BatchSubMode = BatchSubMode.BY_BATCH_COUNT,
    val targetTotalText: String = "",
    val batchSizeText: String = "",
    val batchCountText: String = "1",
    val availableAmountText: String = "",
    val roundingOption: RoundingOption = RoundingOption.ONE_DECIMAL,
    val calculationOutput: BatchScalingOutput = calculateState(
        components = initialComponents(),
        scalingMode = ScalingMode.TARGET_TOTAL,
        batchSubMode = BatchSubMode.BY_BATCH_COUNT,
        targetTotalText = "",
        batchSizeText = "",
        batchCountText = "1",
        availableAmountText = "",
        roundingOption = RoundingOption.ONE_DECIMAL
    ),
    val isFormulaSectionExpanded: Boolean = true,
    val showHistorySheet: Boolean = false,
    val showSaveDialog: Boolean = false,
    val showBackupRestoreDialog: Boolean = false,
    val currentTitle: String = "Batch Recipe"
)

private fun initialComponents() = listOf(
    ComponentInput(name = "Component 1", amountText = "", isLocked = false),
    ComponentInput(name = "Component 2", amountText = "", isLocked = false)
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

class BatchScalingViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val batchScalingDao = database.batchScalingDao()

    private val _uiState = MutableStateFlow(BatchScalingUiState())
    val uiState: StateFlow<BatchScalingUiState> = _uiState.asStateFlow()

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    val historyList: StateFlow<List<BatchScalingEntity>> = batchScalingDao.getAllHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setHistorySheetOpen(isOpen: Boolean) {
        _uiState.update { it.copy(showHistorySheet = isOpen) }
    }

    fun setSaveDialogOpen(isOpen: Boolean) {
        _uiState.update { it.copy(showSaveDialog = isOpen) }
    }

    fun setBackupRestoreDialogOpen(isOpen: Boolean) {
        _uiState.update { it.copy(showBackupRestoreDialog = isOpen) }
    }

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
                currentTitle = preset.title,
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
                    ComponentInput(name = "Component 1", amountText = "", isLocked = false),
                    ComponentInput(name = "Component 2", amountText = "", isLocked = false)
                ),
                targetTotalText = "",
                batchSizeText = "",
                batchCountText = "1",
                availableAmountText = ""
            )
            recalculate(updated)
        }
    }

    // -------------------------------------------------------------
    // History Persistence Operations (Save, View, Edit, Backup, Restore)
    // -------------------------------------------------------------

    fun saveCurrentToHistory(title: String = "", description: String = "", note: String = "") {
        val current = _uiState.value
        val output = current.calculationOutput

        val componentsArray = JSONArray()
        for (comp in current.components) {
            val obj = JSONObject().apply {
                put("id", comp.id)
                put("name", comp.name)
                put("amountText", comp.amountText)
                put("isLocked", comp.isLocked)
            }
            componentsArray.put(obj)
        }

        val entity = BatchScalingEntity(
            title = title.ifBlank { current.currentTitle },
            description = description,
            scalingMode = current.scalingMode.name,
            batchSubMode = current.batchSubMode.name,
            targetTotalText = current.targetTotalText,
            batchSizeText = current.batchSizeText,
            batchCountText = current.batchCountText,
            availableAmountText = current.availableAmountText,
            roundingOption = current.roundingOption.name,
            componentsJson = componentsArray.toString(),
            originalGrandTotal = output.originalGrandTotal,
            scaledGrandTotal = output.scaledGrandTotal,
            scalingFactor = output.scalingFactor,
            singleBatchGrandTotal = output.singleBatchGrandTotal,
            componentsCount = current.components.size,
            timestamp = System.currentTimeMillis(),
            isPinned = false,
            note = note
        )

        viewModelScope.launch {
            batchScalingDao.insert(entity)
            _uiState.update { it.copy(showSaveDialog = false, currentTitle = entity.title) }
            _userMessage.emit("Batch scaling '${entity.title}' saved to history")
        }
    }

    fun loadFromHistory(entity: BatchScalingEntity) {
        val parsedComponents = mutableListOf<ComponentInput>()
        try {
            val array = JSONArray(entity.componentsJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                parsedComponents.add(
                    ComponentInput(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        name = obj.optString("name", "Component ${i + 1}"),
                        amountText = obj.optString("amountText", "0"),
                        isLocked = obj.optBoolean("isLocked", false)
                    )
                )
            }
        } catch (e: Exception) {
            // fallback
        }

        val componentsToUse = if (parsedComponents.isNotEmpty()) parsedComponents else initialComponents()
        val mode = try { ScalingMode.valueOf(entity.scalingMode) } catch (e: Exception) { ScalingMode.TARGET_TOTAL }
        val subMode = try { BatchSubMode.valueOf(entity.batchSubMode) } catch (e: Exception) { BatchSubMode.BY_BATCH_COUNT }
        val rounding = try { RoundingOption.valueOf(entity.roundingOption) } catch (e: Exception) { RoundingOption.ONE_DECIMAL }

        _uiState.update { current ->
            val updated = current.copy(
                currentTitle = entity.title,
                components = componentsToUse,
                scalingMode = mode,
                batchSubMode = subMode,
                targetTotalText = entity.targetTotalText,
                batchSizeText = entity.batchSizeText,
                batchCountText = entity.batchCountText,
                availableAmountText = entity.availableAmountText,
                roundingOption = rounding,
                showHistorySheet = false
            )
            recalculate(updated)
        }

        viewModelScope.launch {
            _userMessage.emit("Loaded '${entity.title}' into Batch Scaler")
        }
    }

    fun updateHistoryItem(entity: BatchScalingEntity, newTitle: String, newDescription: String, newNote: String) {
        viewModelScope.launch {
            val updated = entity.copy(
                title = newTitle.ifBlank { entity.title },
                description = newDescription,
                note = newNote
            )
            batchScalingDao.update(updated)
            _userMessage.emit("Updated '${updated.title}'")
        }
    }

    fun togglePin(entity: BatchScalingEntity) {
        viewModelScope.launch {
            val updated = entity.copy(isPinned = !entity.isPinned)
            batchScalingDao.update(updated)
        }
    }

    fun deleteHistoryItem(entity: BatchScalingEntity) {
        viewModelScope.launch {
            batchScalingDao.delete(entity)
            _userMessage.emit("Deleted '${entity.title}' from history")
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            batchScalingDao.clearAll()
            _userMessage.emit("Cleared all batch scaler history")
        }
    }

    suspend fun exportBackupJson(): String {
        val backupManager = DatabaseBackupManager(getApplication())
        return backupManager.exportBatchScalingBackupJson()
    }

    fun restoreBackupFromJson(json: String, mode: RestoreMode) {
        viewModelScope.launch {
            val backupManager = DatabaseBackupManager(getApplication())
            val result = backupManager.restoreBatchScalingFromJson(json, mode)
            _userMessage.emit(result.message)
        }
    }

    fun saveBackupToUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            val backupManager = DatabaseBackupManager(context)
            val json = backupManager.exportBatchScalingBackupJson()
            val ok = backupManager.writeBackupToUri(context, uri, json)
            if (ok) {
                _userMessage.emit("Batch Scaler backup file saved successfully.")
            } else {
                _userMessage.emit("Failed to save Batch Scaler backup.")
            }
        }
    }

    fun loadBackupFromUri(context: Context, uri: Uri, mode: RestoreMode) {
        viewModelScope.launch {
            val backupManager = DatabaseBackupManager(context)
            val content = backupManager.readBackupFromUri(context, uri)
            if (!content.isNullOrBlank()) {
                val result = backupManager.restoreBatchScalingFromJson(content, mode)
                _userMessage.emit(result.message)
            } else {
                _userMessage.emit("Failed to read Batch Scaler backup file.")
            }
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

private fun String.parseCleanDouble(): Double =
    this.replace(",", ".").replace(" ", "").trim().toDoubleOrNull() ?: 0.0

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
            originalAmount = it.amountText.parseCleanDouble(),
            isLocked = it.isLocked
        )
    }

    val targetTotal = targetTotalText.parseCleanDouble()
    val batchSize = batchSizeText.parseCleanDouble()
    val batchCount = batchCountText.parseCleanDouble().let { if (it <= 0.0) 1.0 else it }
    val availableAmount = availableAmountText.parseCleanDouble()

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

package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.BackupValidationResult
import com.example.data.db.DatabaseBackupManager
import com.example.data.db.DatabaseStats
import com.example.data.db.RestoreMode
import com.example.data.db.RestoreSummary
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class BackupDialogType {
    NONE,
    EXPORT_PREVIEW,
    IMPORT_JSON,
    RESTORE_CONFIRM,
    RESET_DATABASE
}

data class SettingsUiState(
    val stats: DatabaseStats = DatabaseStats(),
    val isBusy: Boolean = false,
    val activeDialog: BackupDialogType = BackupDialogType.NONE,
    val pendingJson: String = "",
    val validationResult: BackupValidationResult? = null,
    val restoreMode: RestoreMode = RestoreMode.OVERWRITE,
    val restoreSettingsOption: Boolean = true,
    val lastRestoreSummary: RestoreSummary? = null,
    val clearCalculationsOption: Boolean = true,
    val clearSalesOption: Boolean = true,
    val clearMixturesOption: Boolean = true
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val backupManager = DatabaseBackupManager(application)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    init {
        refreshStats()
    }

    fun refreshStats() {
        viewModelScope.launch {
            val stats = backupManager.getStats()
            _uiState.update { it.copy(stats = stats) }
        }
    }

    fun openDialog(type: BackupDialogType) {
        _uiState.update { it.copy(activeDialog = type) }
        if (type == BackupDialogType.EXPORT_PREVIEW) {
            prepareExportJson()
        }
    }

    fun closeDialog() {
        _uiState.update {
            it.copy(
                activeDialog = BackupDialogType.NONE,
                pendingJson = "",
                validationResult = null
            )
        }
    }

    fun setRestoreMode(mode: RestoreMode) {
        _uiState.update { it.copy(restoreMode = mode) }
    }

    fun setRestoreSettingsOption(enabled: Boolean) {
        _uiState.update { it.copy(restoreSettingsOption = enabled) }
    }

    fun setClearCalculationsOption(enabled: Boolean) {
        _uiState.update { it.copy(clearCalculationsOption = enabled) }
    }

    fun setClearSalesOption(enabled: Boolean) {
        _uiState.update { it.copy(clearSalesOption = enabled) }
    }

    fun setClearMixturesOption(enabled: Boolean) {
        _uiState.update { it.copy(clearMixturesOption = enabled) }
    }

    fun setPendingJson(json: String) {
        val validation = backupManager.validateBackupJson(json)
        _uiState.update {
            it.copy(
                pendingJson = json,
                validationResult = validation
            )
        }
    }

    private fun prepareExportJson() {
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true) }
            val json = backupManager.createBackupJson()
            val stats = backupManager.getStats()
            _uiState.update {
                it.copy(
                    isBusy = false,
                    pendingJson = json,
                    stats = stats
                )
            }
        }
    }

    fun getDefaultBackupFileName(): String {
        return backupManager.generateBackupFileName()
    }

    fun saveBackupToUri(context: Context, uri: android.net.Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true) }
            val json = if (_uiState.value.pendingJson.isNotBlank()) {
                _uiState.value.pendingJson
            } else {
                backupManager.createBackupJson()
            }
            val success = backupManager.writeBackupToUri(context, uri, json)
            val stats = backupManager.getStats()
            _uiState.update { it.copy(isBusy = false, stats = stats) }
            if (success) {
                _userMessage.emit("Backup downloaded and saved successfully.")
            } else {
                _userMessage.emit("Failed to write backup file.")
            }
        }
    }

    fun loadBackupFromUri(context: Context, uri: android.net.Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true) }
            val content = backupManager.readBackupFromUri(context, uri)
            if (!content.isNullOrBlank()) {
                val validation = backupManager.validateBackupJson(content)
                _uiState.update {
                    it.copy(
                        isBusy = false,
                        pendingJson = content,
                        validationResult = validation,
                        activeDialog = BackupDialogType.RESTORE_CONFIRM
                    )
                }
                if (validation.isValid) {
                    _userMessage.emit("Backup file loaded successfully (${validation.totalItems} items).")
                } else {
                    _userMessage.emit("Invalid backup file format.")
                }
            } else {
                _uiState.update { it.copy(isBusy = false) }
                _userMessage.emit("Could not read the selected backup file.")
            }
        }
    }

    fun performRestore(onPreferencesReload: () -> Unit) {
        val current = _uiState.value
        val json = current.pendingJson
        if (json.isBlank()) {
            viewModelScope.launch {
                _userMessage.emit("No backup data to restore.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true) }
            val result = backupManager.restoreFromJson(
                rawJson = json,
                mode = current.restoreMode,
                restoreSettings = current.restoreSettingsOption
            )
            val stats = backupManager.getStats()

            _uiState.update {
                it.copy(
                    isBusy = false,
                    activeDialog = BackupDialogType.NONE,
                    pendingJson = "",
                    validationResult = null,
                    lastRestoreSummary = result,
                    stats = stats
                )
            }

            if (result.success) {
                if (current.restoreSettingsOption && result.restoredSettings) {
                    onPreferencesReload()
                }
                _userMessage.emit(result.message)
            } else {
                _userMessage.emit(result.message)
            }
        }
    }

    fun performResetDatabase(onPreferencesReload: () -> Unit) {
        val current = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true) }
            val ok = backupManager.clearAllData(
                clearCalculations = current.clearCalculationsOption,
                clearSales = current.clearSalesOption,
                clearMixtures = current.clearMixturesOption
            )
            val stats = backupManager.getStats()
            _uiState.update {
                it.copy(
                    isBusy = false,
                    activeDialog = BackupDialogType.NONE,
                    stats = stats
                )
            }
            if (ok) {
                _userMessage.emit("Selected database tables cleared successfully.")
            } else {
                _userMessage.emit("Error resetting database tables.")
            }
        }
    }
}

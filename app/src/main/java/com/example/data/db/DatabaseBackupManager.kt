package com.example.data.db

import android.content.Context
import android.content.Intent
import com.example.data.preferences.UserPreferencesManager
import com.example.ui.components.HistoryTimeSplit
import com.example.ui.theme.AccentTheme
import com.example.ui.theme.AnimationIntensity
import com.example.ui.theme.ComponentSizeStyle
import com.example.ui.theme.HapticIntensity
import com.example.ui.theme.ShapeStructureStyle
import com.example.ui.theme.SoundIntensity
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DatabaseStats(
    val calculationsCount: Int = 0,
    val salesTransactionsCount: Int = 0,
    val mixtureRecipesCount: Int = 0,
    val lastBackupTimestamp: Long = 0L,
    val hasLocalSnapshot: Boolean = false,
    val localSnapshotTimestamp: Long? = null
) {
    val totalRecords: Int get() = calculationsCount + salesTransactionsCount + mixtureRecipesCount

    val formattedLastBackup: String
        get() = if (lastBackupTimestamp > 0) {
            val sdf = SimpleDateFormat("MMM dd, yyyy · hh:mm a", Locale.getDefault())
            sdf.format(Date(lastBackupTimestamp))
        } else {
            "Never"
        }
}

data class BackupValidationResult(
    val isValid: Boolean,
    val version: Int = 1,
    val timestamp: Long = 0L,
    val calculationsCount: Int = 0,
    val salesTransactionsCount: Int = 0,
    val mixtureRecipesCount: Int = 0,
    val hasPreferences: Boolean = false,
    val errorMessage: String? = null
) {
    val totalItems: Int get() = calculationsCount + salesTransactionsCount + mixtureRecipesCount

    val formattedBackupDate: String
        get() = if (timestamp > 0) {
            val sdf = SimpleDateFormat("MMM dd, yyyy · hh:mm a", Locale.getDefault())
            sdf.format(Date(timestamp))
        } else {
            "Unknown Date"
        }
}

enum class RestoreMode {
    OVERWRITE, // Clear existing tables, replace with backup
    MERGE      // Keep existing data, append backup data
}

data class RestoreSummary(
    val success: Boolean,
    val restoredCalculations: Int = 0,
    val restoredSales: Int = 0,
    val restoredMixtures: Int = 0,
    val restoredSettings: Boolean = false,
    val message: String
)

class DatabaseBackupManager(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val calculationDao = db.calculationDao()
    private val salesDao = db.salesTransactionDao()
    private val mixtureDao = db.mixtureRecipeDao()
    private val prefs = UserPreferencesManager(context)

    private val snapshotFile: File
        get() = File(context.filesDir, "calculator_backup_snapshot.json")

    suspend fun getStats(): DatabaseStats = withContext(Dispatchers.IO) {
        try {
            val calcs = calculationDao.getHistoryCount()
            val sales = salesDao.getCount()
            val mixtures = mixtureDao.getCount()
            val lastBackup = prefs.lastBackupTimestamp
            val snapshotExists = snapshotFile.exists() && snapshotFile.length() > 10
            val snapshotTime = if (snapshotExists) snapshotFile.lastModified() else null

            DatabaseStats(
                calculationsCount = calcs,
                salesTransactionsCount = sales,
                mixtureRecipesCount = mixtures,
                lastBackupTimestamp = lastBackup,
                hasLocalSnapshot = snapshotExists,
                localSnapshotTimestamp = snapshotTime
            )
        } catch (e: Exception) {
            DatabaseStats()
        }
    }

    suspend fun createBackupJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        val now = System.currentTimeMillis()

        // 1. Metadata header
        val meta = JSONObject().apply {
            put("version", 1)
            put("app", "Precision Calculator Suite")
            put("exportedAt", now)
            put("schemaVersion", 7)
        }
        root.put("metadata", meta)

        // 2. Calculation History
        val calcs = calculationDao.getAllList()
        val calcArray = JSONArray()
        for (item in calcs) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("expression", item.expression)
                put("result", item.result)
                put("timestamp", item.timestamp)
                put("tag", item.tag)
                put("note", item.note)
                put("isPinned", item.isPinned)
            }
            calcArray.put(obj)
        }
        root.put("calculations", calcArray)

        // 3. Sales Transactions
        val sales = salesDao.getAllList()
        val salesArray = JSONArray()
        for (s in sales) {
            val obj = JSONObject().apply {
                put("id", s.id)
                put("salesRep", s.salesRep)
                put("dateMillis", s.dateMillis)
                put("totalSoldQty", s.totalSoldQty)
                put("returnedQty", s.returnedQty)
                put("expiredQty", s.expiredQty)
                put("sharedQty", s.sharedQty)
                put("creditSalesQty", s.creditSalesQty)
                put("recoveredCredit", s.recoveredCredit)
                put("physicalCashInHand", s.physicalCashInHand)
                put("expense", s.expense)
                put("unitSalePrice", s.unitSalePrice)
                put("commissionPerUnit", s.commissionPerUnit)
                put("grossCommission", s.grossCommission)
                put("commissionPaid", s.commissionPaid)
                put("totalCommission", s.totalCommission)
                put("netSoldUnits", s.netSoldUnits)
                put("cashQtyUnits", s.cashQtyUnits)
                put("expectedCash", s.expectedCash)
                put("reconciledCash", s.reconciledCash)
                put("discrepancy", s.discrepancy)
                put("status", s.status)
                put("timestamp", s.timestamp)
                put("note", s.note)
            }
            salesArray.put(obj)
        }
        root.put("salesTransactions", salesArray)

        // 4. Mixture Recipes
        val mixtures = mixtureDao.getAllList()
        val mixtureArray = JSONArray()
        for (m in mixtures) {
            val obj = JSONObject().apply {
                put("id", m.id)
                put("title", m.title)
                put("category", m.category)
                put("description", m.description)
                put("anchorIngredientName", m.anchorIngredientName)
                put("scaleFactor", m.scaleFactor)
                put("totalTargetWeight", m.totalTargetWeight)
                put("unit", m.unit)
                put("ingredientsJson", m.ingredientsJson)
                put("batchCount", m.batchCount)
                put("timestamp", m.timestamp)
                put("isFavorite", m.isFavorite)
            }
            mixtureArray.put(obj)
        }
        root.put("mixtureRecipes", mixtureArray)

        // 5. App Preferences & User Settings
        val settingsObj = JSONObject().apply {
            put("themeMode", prefs.themeMode.name)
            put("accentTheme", prefs.accentTheme.name)
            put("shapeStructureStyle", prefs.shapeStructureStyle.name)
            put("componentSizeStyle", prefs.componentSizeStyle.name)
            put("animationIntensity", prefs.animationIntensity.name)
            put("hapticIntensity", prefs.hapticIntensity.name)
            put("soundIntensity", prefs.soundIntensity.name)
            put("lastHistoryTimeSplit", prefs.lastHistoryTimeSplit.name)
            put("showDoubleZeroKey", prefs.showDoubleZeroKey)
        }
        root.put("settings", settingsObj)

        val jsonString = root.toString(2)

        // Save local snapshot to disk for instant on-device recovery
        try {
            snapshotFile.writeText(jsonString)
        } catch (e: Exception) {
            // non-fatal
        }

        // Update preference timestamp
        prefs.lastBackupTimestamp = now

        jsonString
    }

    fun validateBackupJson(rawJson: String): BackupValidationResult {
        if (rawJson.isBlank()) {
            return BackupValidationResult(isValid = false, errorMessage = "Backup JSON is empty.")
        }
        return try {
            val root = JSONObject(rawJson)
            val meta = root.optJSONObject("metadata")
            val timestamp = meta?.optLong("exportedAt") ?: System.currentTimeMillis()
            val version = meta?.optInt("version") ?: 1

            val calcArray = root.optJSONArray("calculations")
            val salesArray = root.optJSONArray("salesTransactions")
            val mixtureArray = root.optJSONArray("mixtureRecipes")
            val settingsObj = root.optJSONObject("settings")

            val calcCount = calcArray?.length() ?: 0
            val salesCount = salesArray?.length() ?: 0
            val mixtureCount = mixtureArray?.length() ?: 0
            val hasSettings = settingsObj != null

            if (calcCount == 0 && salesCount == 0 && mixtureCount == 0 && !hasSettings) {
                BackupValidationResult(
                    isValid = false,
                    errorMessage = "No valid data tables or settings found in this backup JSON."
                )
            } else {
                BackupValidationResult(
                    isValid = true,
                    version = version,
                    timestamp = timestamp,
                    calculationsCount = calcCount,
                    salesTransactionsCount = salesCount,
                    mixtureRecipesCount = mixtureCount,
                    hasPreferences = hasSettings
                )
            }
        } catch (e: Exception) {
            BackupValidationResult(
                isValid = false,
                errorMessage = "Invalid JSON format: ${e.localizedMessage ?: "Parsing error"}"
            )
        }
    }

    suspend fun getLocalSnapshotContent(): String? = withContext(Dispatchers.IO) {
        try {
            if (snapshotFile.exists() && snapshotFile.length() > 10) {
                snapshotFile.readText()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun restoreFromJson(
        rawJson: String,
        mode: RestoreMode,
        restoreSettings: Boolean = true
    ): RestoreSummary = withContext(Dispatchers.IO) {
        val validation = validateBackupJson(rawJson)
        if (!validation.isValid) {
            return@withContext RestoreSummary(
                success = false,
                message = validation.errorMessage ?: "Failed to parse backup payload."
            )
        }

        try {
            val root = JSONObject(rawJson)

            if (mode == RestoreMode.OVERWRITE) {
                calculationDao.clearAllHistory()
                salesDao.clearAllTransactions()
                mixtureDao.clearAll()
            }

            // 1. Calculations
            var restoredCalcs = 0
            val calcArray = root.optJSONArray("calculations")
            if (calcArray != null && calcArray.length() > 0) {
                val list = mutableListOf<CalculationEntity>()
                for (i in 0 until calcArray.length()) {
                    val obj = calcArray.getJSONObject(i)
                    list.add(
                        CalculationEntity(
                            id = if (mode == RestoreMode.OVERWRITE) obj.optLong("id", 0L) else 0L,
                            expression = obj.optString("expression", ""),
                            result = obj.optString("result", ""),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                            tag = obj.optString("tag", "General"),
                            note = obj.optString("note", ""),
                            isPinned = obj.optBoolean("isPinned", false)
                        )
                    )
                }
                calculationDao.insertAll(list)
                restoredCalcs = list.size
            }

            // 2. Sales Transactions
            var restoredSales = 0
            val salesArray = root.optJSONArray("salesTransactions")
            if (salesArray != null && salesArray.length() > 0) {
                val list = mutableListOf<SalesTransactionEntity>()
                for (i in 0 until salesArray.length()) {
                    val obj = salesArray.getJSONObject(i)
                    list.add(
                        SalesTransactionEntity(
                            id = if (mode == RestoreMode.OVERWRITE) obj.optLong("id", 0L) else 0L,
                            salesRep = obj.optString("salesRep", "Representative"),
                            dateMillis = obj.optLong("dateMillis", System.currentTimeMillis()),
                            totalSoldQty = obj.optDouble("totalSoldQty", 0.0),
                            returnedQty = obj.optDouble("returnedQty", 0.0),
                            expiredQty = obj.optDouble("expiredQty", 0.0),
                            sharedQty = obj.optDouble("sharedQty", 0.0),
                            creditSalesQty = obj.optDouble("creditSalesQty", 0.0),
                            recoveredCredit = obj.optDouble("recoveredCredit", 0.0),
                            physicalCashInHand = obj.optDouble("physicalCashInHand", 0.0),
                            expense = obj.optDouble("expense", 0.0),
                            unitSalePrice = obj.optDouble("unitSalePrice", 0.0),
                            commissionPerUnit = obj.optDouble("commissionPerUnit", 0.0),
                            grossCommission = obj.optDouble("grossCommission", 0.0),
                            commissionPaid = obj.optDouble("commissionPaid", 0.0),
                            totalCommission = obj.optDouble("totalCommission", 0.0),
                            netSoldUnits = obj.optDouble("netSoldUnits", 0.0),
                            cashQtyUnits = obj.optDouble("cashQtyUnits", 0.0),
                            expectedCash = obj.optDouble("expectedCash", 0.0),
                            reconciledCash = obj.optDouble("reconciledCash", 0.0),
                            discrepancy = obj.optDouble("discrepancy", 0.0),
                            status = obj.optString("status", "Balanced"),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                            note = obj.optString("note", "")
                        )
                    )
                }
                salesDao.insertAll(list)
                restoredSales = list.size
            }

            // 3. Mixture Recipes
            var restoredMixtures = 0
            val mixtureArray = root.optJSONArray("mixtureRecipes")
            if (mixtureArray != null && mixtureArray.length() > 0) {
                val list = mutableListOf<MixtureRecipeEntity>()
                for (i in 0 until mixtureArray.length()) {
                    val obj = mixtureArray.getJSONObject(i)
                    list.add(
                        MixtureRecipeEntity(
                            id = if (mode == RestoreMode.OVERWRITE) obj.optLong("id", 0L) else 0L,
                            title = obj.optString("title", "Saved Mixture"),
                            category = obj.optString("category", "Structural"),
                            description = obj.optString("description", ""),
                            anchorIngredientName = obj.optString("anchorIngredientName", "Component A"),
                            scaleFactor = obj.optDouble("scaleFactor", 1.0),
                            totalTargetWeight = obj.optDouble("totalTargetWeight", 100.0),
                            unit = obj.optString("unit", "kg"),
                            ingredientsJson = obj.optString("ingredientsJson", "[]"),
                            batchCount = obj.optInt("batchCount", 1),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                            isFavorite = obj.optBoolean("isFavorite", false)
                        )
                    )
                }
                mixtureDao.insertAll(list)
                restoredMixtures = list.size
            }

            // 4. Settings
            var restoredSettingsFlag = false
            if (restoreSettings) {
                val settingsObj = root.optJSONObject("settings")
                if (settingsObj != null) {
                    try {
                        settingsObj.optString("themeMode").takeIf { it.isNotBlank() }?.let {
                            prefs.themeMode = ThemeMode.valueOf(it)
                        }
                        settingsObj.optString("accentTheme").takeIf { it.isNotBlank() }?.let {
                            prefs.accentTheme = AccentTheme.valueOf(it)
                        }
                        settingsObj.optString("shapeStructureStyle").takeIf { it.isNotBlank() }?.let {
                            prefs.shapeStructureStyle = ShapeStructureStyle.valueOf(it)
                        }
                        settingsObj.optString("componentSizeStyle").takeIf { it.isNotBlank() }?.let {
                            prefs.componentSizeStyle = ComponentSizeStyle.valueOf(it)
                        }
                        settingsObj.optString("animationIntensity").takeIf { it.isNotBlank() }?.let {
                            prefs.animationIntensity = AnimationIntensity.valueOf(it)
                        }
                        settingsObj.optString("hapticIntensity").takeIf { it.isNotBlank() }?.let {
                            prefs.hapticIntensity = HapticIntensity.valueOf(it)
                        }
                        settingsObj.optString("soundIntensity").takeIf { it.isNotBlank() }?.let {
                            prefs.soundIntensity = SoundIntensity.valueOf(it)
                        }
                        settingsObj.optString("lastHistoryTimeSplit").takeIf { it.isNotBlank() }?.let {
                            prefs.lastHistoryTimeSplit = HistoryTimeSplit.valueOf(it)
                        }
                        if (settingsObj.has("showDoubleZeroKey")) {
                            prefs.showDoubleZeroKey = settingsObj.getBoolean("showDoubleZeroKey")
                        }
                        restoredSettingsFlag = true
                    } catch (e: Exception) {
                        // ignore setting parse mismatch
                    }
                }
            }

            val modeText = if (mode == RestoreMode.OVERWRITE) "overwritten & restored" else "merged"
            RestoreSummary(
                success = true,
                restoredCalculations = restoredCalcs,
                restoredSales = restoredSales,
                restoredMixtures = restoredMixtures,
                restoredSettings = restoredSettingsFlag,
                message = "Database successfully $modeText: $restoredCalcs calculations, $restoredSales sales audits, $restoredMixtures mixtures."
            )
        } catch (e: Exception) {
            RestoreSummary(
                success = false,
                message = "Restore failed: ${e.localizedMessage ?: "Unknown error"}"
            )
        }
    }

    suspend fun clearAllData(
        clearCalculations: Boolean = true,
        clearSales: Boolean = true,
        clearMixtures: Boolean = true
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            if (clearCalculations) calculationDao.clearAllHistory()
            if (clearSales) salesDao.clearAllTransactions()
            if (clearMixtures) mixtureDao.clearAll()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun shareBackup(context: Context, backupJson: String) {
        try {
            val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, backupJson)
                putExtra(Intent.EXTRA_TITLE, "Calculator_Backup_$dateStr.json")
                putExtra(Intent.EXTRA_SUBJECT, "App Database Backup ($dateStr)")
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, "Share or Save App Backup")
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(shareIntent)
        } catch (e: Exception) {
            // fallback handled by caller
        }
    }
}

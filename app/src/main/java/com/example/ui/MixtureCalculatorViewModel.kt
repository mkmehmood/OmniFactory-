package com.example.ui

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.DatabaseBackupManager
import com.example.data.preferences.MixtureCalculatorStore
import com.example.domain.mixture.CalcDerived
import com.example.domain.mixture.CalcIngType
import com.example.domain.mixture.CalcInputs
import com.example.domain.mixture.CalcMode
import com.example.domain.mixture.CalcRow
import com.example.domain.mixture.CalcSavedFormula
import com.example.domain.mixture.CalcSavedIngredient
import com.example.domain.mixture.CalcSession
import com.example.domain.mixture.CalcSessionIngredient
import com.example.domain.mixture.CalcStoreState
import com.example.domain.mixture.CalcUnit
import com.example.domain.mixture.FormulaCalcEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

enum class CalcTab(val title: String) {
    FORMULA("Formula"),
    REBALANCE("Re-balance"),
    REPORT("Report")
}

/**
 * State holder for the native Mixture Calculator screen.
 *
 * Everything the person types lives in [inputs]; every number on screen comes from [derived]
 * ([FormulaCalcEngine.derive]). Names, saved formulas and the working session are stored by
 * [MixtureCalculatorStore], which is also what the full-app backup / restore carries.
 */
class MixtureCalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext: Context get() = getApplication<Application>().applicationContext

    var inputs by mutableStateOf(CalcInputs())
        private set
    var derived by mutableStateOf(CalcDerived())
        private set
    var tab by mutableStateOf(CalcTab.FORMULA)
        private set

    var savedNames by mutableStateOf<List<String>>(FormulaCalcEngine.DEFAULT_NAMES)
        private set
    var formulas by mutableStateOf<List<CalcSavedFormula>>(emptyList())
        private set

    // Saved-names manager
    var selectedName by mutableStateOf("")
        private set
    var newNameInput by mutableStateOf("")
        private set

    // Saved-formulas manager
    var formulaNameInput by mutableStateOf("")
        private set
    var selectedFormulaId by mutableStateOf<Long?>(null)
        private set

    // Backup text box
    var restoreText by mutableStateOf("")
        private set

    private var nextId = 2
    private var manualScale: Double? = null
    private var knownSignature: String? = null
    private var saveJob: Job? = null
    private var dirty = false

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 16)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    init {
        reloadFromStore()
        // Reload when a Settings restore / purge changes the stored data while this screen exists.
        viewModelScope.launch {
            var seen = MixtureCalculatorStore.changes.value
            MixtureCalculatorStore.changes.collect { value ->
                if (value != seen) {
                    seen = value
                    reloadFromStore()
                }
            }
        }
    }

    // ================================================================== state plumbing

    private fun toast(text: String) {
        _messages.tryEmit(text)
    }

    private fun recompute() {
        val d = FormulaCalcEngine.derive(inputs, manualScale, knownSignature)
        manualScale = d.manualScale
        knownSignature = d.signature
        derived = d

        // A target batch drives the multiplier, so the multiplier box mirrors it (as before).
        if (FormulaCalcEngine.parseNum(inputs.capText) > 0) {
            val m = if (d.batch.multiplierFromCap) String.format(Locale.US, "%.4f", d.batch.multiplier) else ""
            if (m != inputs.multText) inputs = inputs.copy(multText = m)
        }
    }

    private fun update(block: (CalcInputs) -> CalcInputs) {
        inputs = block(inputs)
        recompute()
        queueSave()
    }

    private fun newId(): Int = nextId++

    // ================================================================== navigation / unit / mode

    fun selectTab(t: CalcTab) {
        tab = t
    }

    fun toggleUnit() {
        update { i ->
            val from = i.unit
            val to = from.other
            fun c(s: String) = FormulaCalcEngine.convertText(s, from, to)
            i.copy(
                unit = to,
                baseText = c(i.baseText),
                capText = c(i.capText),
                origText = c(i.origText),
                usedText = c(i.usedText),
                rows = i.rows.map { it.copy(valueText = c(it.valueText)) },
                targetTexts = i.targetTexts.mapValues { c(it.value) }
            )
        }
    }

    fun setMode(mode: CalcMode) = update { it.copy(mode = mode) }

    fun resetAll() {
        nextId = 2
        manualScale = null
        knownSignature = null
        update { CalcInputs() }
    }

    // ================================================================== batch settings

    fun setBase(text: String) = update { it.copy(baseText = FormulaCalcEngine.sanitizeNumeric(text)) }

    fun setCap(text: String) = update { it.copy(capText = FormulaCalcEngine.sanitizeNumeric(text)) }

    /** Typing a multiplier clears the target batch so the multiplier is the one in charge. */
    fun setMultiplier(text: String) =
        update { it.copy(multText = FormulaCalcEngine.sanitizeNumeric(text), capText = "") }

    fun clearCap() = update { it.copy(capText = "", multText = "") }

    fun setOrig(text: String) = update { it.copy(origText = FormulaCalcEngine.sanitizeNumeric(text)) }

    fun setUsed(text: String) = update { it.copy(usedText = FormulaCalcEngine.sanitizeNumeric(text)) }

    // ================================================================== ingredient rows

    fun addRow() = update { it.copy(rows = it.rows + CalcRow(newId())) }

    fun deleteRow(id: Int) = update {
        it.copy(rows = it.rows.filterNot { r -> r.id == id }, targetTexts = it.targetTexts - id)
    }

    fun setRowName(id: Int, name: String) = update { i ->
        i.copy(rows = i.rows.map { if (it.id == id) it.copy(name = name) else it })
    }

    fun setRowType(id: Int, type: CalcIngType) = update { i ->
        i.copy(rows = i.rows.map { if (it.id == id) it.copy(type = type) else it })
    }

    fun setRowValue(id: Int, text: String) = update { i ->
        val clean = FormulaCalcEngine.sanitizeNumeric(text)
        i.copy(rows = i.rows.map { if (it.id == id) it.copy(valueText = clean) else it })
    }

    // ================================================================== re-balance target

    /** What to show in a Target Formula box. */
    fun targetTextFor(id: Int): String =
        if (inputs.targetDirty) inputs.targetTexts[id] ?: ""
        else FormulaCalcEngine.toDisp(derived.effective.firstOrNull { it.id == id }?.valueKg ?: 0.0, inputs.unit)

    fun setTarget(id: Int, text: String) {
        val clean = FormulaCalcEngine.sanitizeNumeric(text)
        update { i ->
            val base = if (i.targetDirty) i.targetTexts else derived.effective.associate {
                it.id to FormulaCalcEngine.toDisp(it.valueKg, i.unit)
            }
            i.copy(targetDirty = true, targetTexts = base + (id to clean))
        }
    }

    fun syncTargetWithOriginal() = update { it.copy(targetDirty = false, targetTexts = emptyMap()) }

    /** An "Add" box was edited: rescale everything else so the formula ratio stays the same. */
    fun onAddEdit(id: Int, raw: String) {
        val row = derived.correction.rows.firstOrNull { it.id == id } ?: return
        if (!(row.targetUnitKg > 0)) return
        val typed = raw.trim().replace(',', '.').toDoubleOrNull()
        manualScale = if (typed == null) null
        else FormulaCalcEngine.scaleForAdd(row.remKg, row.targetUnitKg, typed * inputs.unit.factor)
        recompute()
    }

    fun resetCorrectionScale() {
        manualScale = null
        recompute()
    }

    // ================================================================== saved names

    fun onSelectName(name: String) {
        selectedName = name
        newNameInput = name
    }

    fun onNewNameChange(text: String) {
        newNameInput = text
    }

    fun saveNewName() {
        val name = newNameInput.trim()
        when {
            name.isEmpty() -> toast("Type a name first")
            savedNames.contains(name) -> toast("Name already in list")
            else -> {
                savedNames = savedNames + name
                selectedName = name
                newNameInput = ""
                queueSave()
                toast("Name saved")
            }
        }
    }

    fun updateSelectedName() {
        val old = selectedName
        val nn = newNameInput.trim()
        when {
            old.isEmpty() -> toast("Pick a saved name first")
            nn.isEmpty() -> toast("Type the new name")
            nn == old -> toast("Name is unchanged")
            savedNames.contains(nn) -> toast("That name already exists")
            else -> {
                savedNames = savedNames.map { if (it == old) nn else it }
                selectedName = nn
                update { i -> i.copy(rows = i.rows.map { if (it.name == old) it.copy(name = nn) else it }) }
                toast("Name updated")
            }
        }
    }

    fun deleteSelectedName() {
        val old = selectedName
        if (old.isEmpty()) {
            toast("Pick a saved name first")
            return
        }
        savedNames = savedNames - old
        selectedName = ""
        newNameInput = ""
        queueSave()
        toast("Name deleted")
    }

    // ================================================================== saved formulas

    fun onFormulaNameChange(text: String) {
        formulaNameInput = text
    }

    fun selectFormula(id: Long?) {
        selectedFormulaId = id
    }

    fun saveFormula() {
        val name = formulaNameInput.trim()
        if (name.isEmpty()) {
            toast("Type a formula name first")
            return
        }
        val ings = derived.effective.filter { !it.isBlank }.map { CalcSavedIngredient(it.name, it.type, it.valueKg) }
        if (ings.isEmpty()) {
            toast("Add at least one ingredient first")
            return
        }
        val f = inputs.unit.factor
        val existing = formulas.indexOfFirst { it.name == name }
        val now = System.currentTimeMillis()
        val formula = CalcSavedFormula(
            id = if (existing >= 0) formulas[existing].id else now,
            name = name,
            savedAt = now,
            mode = inputs.mode,
            baseKg = FormulaCalcEngine.parseNum(inputs.baseText) * f,
            capKg = FormulaCalcEngine.parseNum(inputs.capText) * f,
            multiplier = FormulaCalcEngine.parseNum(inputs.multText),
            ingredients = ings
        )
        formulas = if (existing >= 0) formulas.toMutableList().also { it[existing] = formula } else formulas + formula
        selectedFormulaId = formula.id
        formulaNameInput = ""
        queueSave()
        toast(if (existing >= 0) "Formula updated" else "Formula saved")
    }

    fun loadFormula() {
        val f = formulas.firstOrNull { it.id == selectedFormulaId }
        if (f == null) {
            toast("Pick a saved formula first")
            return
        }
        val unit = inputs.unit
        var rows = f.ingredients.map { CalcRow(newId(), it.name, it.type, FormulaCalcEngine.toDisp(it.valueKg, unit)) }
        if (rows.isEmpty()) rows = listOf(CalcRow(newId()))
        manualScale = null
        update {
            it.copy(
                mode = f.mode,
                baseText = FormulaCalcEngine.toDisp(f.baseKg, unit),
                capText = FormulaCalcEngine.toDisp(f.capKg, unit),
                multText = FormulaCalcEngine.toDisp(f.multiplier, CalcUnit.KG),
                rows = rows,
                targetDirty = false,
                targetTexts = emptyMap()
            )
        }
        toast("Formula loaded")
    }

    fun deleteFormula() {
        val idx = formulas.indexOfFirst { it.id == selectedFormulaId }
        if (idx < 0) {
            toast("Pick a saved formula first")
            return
        }
        formulas = formulas.toMutableList().also { it.removeAt(idx) }
        selectedFormulaId = null
        queueSave()
        toast("Formula deleted")
    }

    // ================================================================== reports / clipboard

    private fun dateText(): String = DateFormat.getDateTimeInstance().format(Date())

    fun copyBatchReport() =
        copyText("Mixture Calculator", FormulaCalcEngine.buildBatchReport(inputs, derived, dateText()))

    fun copyComparisonReport() =
        copyText("Mixture Calculator", FormulaCalcEngine.buildComparisonReport(inputs, derived, dateText()))

    private fun copyText(label: String, text: String) {
        try {
            val cm = appContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText(label, text))
            toast("Copied to clipboard")
        } catch (e: Exception) {
            toast("Could not copy")
        }
    }

    // ================================================================== backup / restore

    fun onRestoreTextChange(text: String) {
        restoreText = text
    }

    fun copyBackup() {
        saveNowBlocking()
        copyText("Mixture Calculator backup", MixtureCalculatorStore.exportText(appContext))
    }

    /** Restore from the pasted text. A full-app backup pasted here works too. */
    fun restoreFromText(replace: Boolean) {
        val raw = restoreText
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                saveNowBlocking()
                MixtureCalculatorStore.importText(appContext, raw, replace)
            }
            if (result.ok) restoreText = ""
            toast(result.message)
        }
    }

    fun exportToUri(uri: Uri) {
        viewModelScope.launch {
            val text = withContext(Dispatchers.IO) {
                saveNowBlocking()
                MixtureCalculatorStore.exportText(appContext)
            }
            val ok = DatabaseBackupManager(appContext).writeBackupToUri(appContext, uri, text)
            toast(if (ok) "Backup file saved" else "Failed to save backup file")
        }
    }

    fun importFromUri(uri: Uri, replace: Boolean) {
        viewModelScope.launch {
            val content = DatabaseBackupManager(appContext).readBackupFromUri(appContext, uri)
            if (content.isNullOrBlank()) {
                toast("Failed to read backup file")
                return@launch
            }
            val result = withContext(Dispatchers.IO) {
                saveNowBlocking()
                MixtureCalculatorStore.importText(appContext, content, replace)
            }
            toast(result.message)
        }
    }

    // ================================================================== persistence

    private fun buildState(): CalcStoreState {
        val i = inputs
        val f = i.unit.factor
        fun kg(text: String) = FormulaCalcEngine.parseNum(text) * f
        val main = i.rows.map { CalcSessionIngredient(it.id, it.name, it.type, max(0.0, kg(it.valueText))) }
        val target = if (i.targetDirty) {
            i.rows.map { CalcSessionIngredient(it.id, it.name, it.type, max(0.0, kg(i.targetTexts[it.id] ?: ""))) }
        } else emptyList()
        val session = CalcSession(
            unit = i.unit,
            mode = i.mode,
            base = i.baseText,
            cap = i.capText,
            mult = i.multText,
            orig = i.origText,
            used = i.usedText,
            ingredients = main,
            targetDirty = i.targetDirty,
            target = target
        )
        return CalcStoreState(savedNames = savedNames, formulas = formulas, session = session)
    }

    private fun queueSave() {
        dirty = true
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(300)
            val state = buildState()
            dirty = false
            withContext(Dispatchers.IO) { MixtureCalculatorStore.save(appContext, state) }
        }
    }

    /** Writes right away (used when the app goes to the background and before backups). */
    fun flush() {
        if (dirty) saveNowBlocking()
    }

    private fun saveNowBlocking() {
        saveJob?.cancel()
        dirty = false
        MixtureCalculatorStore.save(appContext, buildState())
    }

    private fun reloadFromStore() {
        // The stored data is the truth now; a pending save of older edits must not overwrite it.
        saveJob?.cancel()
        dirty = false
        val s = MixtureCalculatorStore.load(appContext)
        savedNames = s.savedNames
        formulas = s.formulas
        if (selectedName.isNotEmpty() && !savedNames.contains(selectedName)) selectedName = ""
        if (selectedFormulaId != null && formulas.none { it.id == selectedFormulaId }) selectedFormulaId = null
        manualScale = null
        knownSignature = null
        applySession(s.session)
    }

    private fun applySession(se: CalcSession?) {
        if (se == null) {
            nextId = 2
            inputs = CalcInputs()
            recompute()
            return
        }
        val unit = se.unit
        var next = 1
        val used = HashSet<Int>()
        val rows = se.ingredients.map { ing ->
            var id = ing.id
            if (id <= 0 || !used.add(id)) {
                while (used.contains(next) || next <= 0) next++
                id = next
                used.add(id)
            }
            next = max(next, id + 1)
            CalcRow(id, ing.name, ing.type, FormulaCalcEngine.toDisp(ing.valueKg, unit))
        }.ifEmpty { listOf(CalcRow(next++)) }
        nextId = max(next, (rows.maxOfOrNull { it.id } ?: 0) + 1)

        val targetTexts = if (se.targetDirty) {
            se.target.filter { t -> rows.any { it.id == t.id } }.associate { it.id to FormulaCalcEngine.toDisp(it.valueKg, unit) }
        } else emptyMap()

        inputs = CalcInputs(
            unit = unit,
            mode = se.mode,
            baseText = se.base,
            capText = se.cap,
            multText = se.mult,
            origText = se.orig,
            usedText = se.used,
            rows = rows,
            targetDirty = se.targetDirty && targetTexts.isNotEmpty(),
            targetTexts = targetTexts
        )
        recompute()
    }

    override fun onCleared() {
        // viewModelScope is already cancelled here, so write synchronously.
        saveJob?.cancel()
        try {
            if (dirty) MixtureCalculatorStore.save(appContext, buildState())
        } catch (e: Exception) {
            // nothing more to do
        }
        super.onCleared()
    }
}

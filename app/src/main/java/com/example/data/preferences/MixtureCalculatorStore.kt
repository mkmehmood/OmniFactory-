package com.example.data.preferences

import android.content.Context
import com.example.domain.mixture.CalcIngType
import com.example.domain.mixture.CalcMode
import com.example.domain.mixture.CalcSavedFormula
import com.example.domain.mixture.CalcSavedIngredient
import com.example.domain.mixture.CalcSession
import com.example.domain.mixture.CalcSessionIngredient
import com.example.domain.mixture.CalcStoreState
import com.example.domain.mixture.CalcUnit
import com.example.domain.mixture.FormulaCalcEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/** Outcome of importing pasted / file backup text. */
data class CalcImportResult(
    val ok: Boolean,
    val message: String,
    val names: Int = 0,
    val formulas: Int = 0
)

/**
 * On-device storage for the native Mixture Calculator.
 *
 * One JSON document holds the saved ingredient names, the saved formulas and the last working
 * session. It lives in a private file, so it survives restarts and is picked up by the full-app and
 * mixture-only backups (see DatabaseBackupManager, key "mixtureCalculator").
 *
 * Safety:
 *  - writes go to a temp file that is renamed over the real one, and the previous good copy is kept
 *    as `.bak`, so a crash mid-write can never lose the saved names / formulas;
 *  - reads fall back to the `.bak` copy when the main file is damaged;
 *  - anything restored from a backup is sanitized first (bad rows dropped, schema normalised);
 *  - [changes] ticks whenever the data is changed from outside the calculator (a Settings restore or
 *    purge), so an open calculator reloads instead of overwriting the restored data.
 */
object MixtureCalculatorStore {

    private const val FILE_NAME = "mixture_calculator_state.json"
    private const val SCHEMA_VERSION = 2

    private val _changes = MutableStateFlow(0)

    /** Increments after replace / merge / clear / import (never after the calculator's own saves). */
    val changes: StateFlow<Int> = _changes.asStateFlow()

    private fun notifyChanged() = _changes.update { it + 1 }

    private fun file(context: Context): File = File(context.filesDir, FILE_NAME)
    private fun backupFile(context: Context): File = File(context.filesDir, "$FILE_NAME.bak")
    private fun tempFile(context: Context): File = File(context.filesDir, "$FILE_NAME.tmp")

    // ------------------------------------------------------------------ raw file access

    private fun readValid(f: File): String? {
        return try {
            if (!f.exists() || f.length() <= 2) return null
            val text = f.readText()
            JSONObject(text) // must parse
            text
        } catch (e: Exception) {
            null
        }
    }

    /** Raw JSON text, or an empty string when nothing has been saved yet. */
    @Synchronized
    fun read(context: Context): String =
        readValid(file(context)) ?: readValid(backupFile(context)) ?: ""

    /** Parsed state, or null when nothing (valid) is stored. */
    fun readJson(context: Context): JSONObject? {
        val raw = read(context)
        if (raw.isBlank()) return null
        return try {
            JSONObject(raw)
        } catch (e: Exception) {
            null
        }
    }

    @Synchronized
    fun write(context: Context, json: String): Boolean {
        return try {
            JSONObject(json) // validate before touching any file
            val main = file(context)
            val temp = tempFile(context)
            temp.writeText(json)
            if (readValid(main) != null) {
                try {
                    main.copyTo(backupFile(context), overwrite = true)
                } catch (e: Exception) {
                    // the backup copy is a bonus; never block the save
                }
            }
            if (!temp.renameTo(main)) {
                main.writeText(json)
                temp.delete()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    // ------------------------------------------------------------------ typed access (used by the screen)

    /** Saved state for the calculator. Default ingredient names are used until names are saved. */
    fun load(context: Context): CalcStoreState {
        val obj = readJson(context) ?: return CalcStoreState()
        return try {
            toState(obj)
        } catch (e: Exception) {
            CalcStoreState()
        }
    }

    /** Persists the calculator's own state. Does not notify [changes]. */
    fun save(context: Context, state: CalcStoreState): Boolean =
        write(context, toJson(state.copy(updatedAt = System.currentTimeMillis())).toString())

    // ------------------------------------------------------------------ backup / restore

    /** Pretty JSON of the current data, for the in-app "Copy backup" and file export. */
    fun exportText(context: Context): String =
        toJson(load(context).copy(updatedAt = System.currentTimeMillis())).toString(2)

    /**
     * Imports pasted or file text. Accepts a calculator backup, a mixture backup, or a full app
     * backup (the calculator data is found under "mixtureCalculator").
     */
    fun importText(context: Context, raw: String, replace: Boolean): CalcImportResult {
        val root = try {
            JSONObject(raw.trim())
        } catch (e: Exception) {
            return CalcImportResult(false, "Not a valid backup")
        }
        return importObject(context, root.optJSONObject("mixtureCalculator") ?: root, replace)
    }

    /** Same as [importText] for an already parsed JSON object (used by the backup manager). */
    fun importObject(context: Context, incoming: JSONObject, replace: Boolean): CalcImportResult {
        val clean = sanitize(incoming) ?: return CalcImportResult(false, "Not a valid backup")
        val ok = if (replace) replace(context, clean) else merge(context, clean)
        if (!ok) return CalcImportResult(false, "Could not save the restored data")
        val names = clean.optJSONArray("savedNames")?.length() ?: 0
        val formulas = clean.optJSONArray("formulas")?.length() ?: 0
        return CalcImportResult(true, if (replace) "Backup restored (replaced)" else "Backup restored (merged)", names, formulas)
    }

    /**
     * Replace with the supplied state. Saved names are kept when the backup has none; saved formulas
     * and the working session come from the backup.
     */
    fun replace(context: Context, incoming: JSONObject): Boolean {
        return try {
            val clean = sanitize(incoming) ?: return false
            val current = load(context)
            val inState = toState(clean)
            val names = if (clean.has("savedNames") && inState.savedNames.isNotEmpty()) inState.savedNames else current.savedNames
            val merged = CalcStoreState(
                savedNames = names,
                formulas = inState.formulas,
                session = inState.session ?: current.session,
                updatedAt = System.currentTimeMillis()
            )
            val ok = write(context, toJson(merged).toString())
            if (ok) notifyChanged()
            ok
        } catch (e: Exception) {
            false
        }
    }

    /** Merge: union of names, formulas added when their name is new, current session kept. */
    fun merge(context: Context, incoming: JSONObject): Boolean {
        return try {
            val clean = sanitize(incoming) ?: return false
            val current = load(context)
            val inState = toState(clean)

            val names = LinkedHashSet<String>(current.savedNames)
            if (clean.has("savedNames")) names.addAll(inState.savedNames)

            val known = current.formulas.map { it.name }.toMutableSet()
            val formulas = current.formulas.toMutableList()
            inState.formulas.forEach { f -> if (known.add(f.name)) formulas.add(f) }

            val merged = CalcStoreState(
                savedNames = names.toList(),
                formulas = formulas,
                session = current.session ?: inState.session,
                updatedAt = System.currentTimeMillis()
            )
            val ok = write(context, toJson(merged).toString())
            if (ok) notifyChanged()
            ok
        } catch (e: Exception) {
            false
        }
    }

    @Synchronized
    fun clear(context: Context) {
        try {
            file(context).delete()
            backupFile(context).delete()
            tempFile(context).delete()
        } catch (e: Exception) {
            // non-fatal
        }
        notifyChanged()
    }

    fun formulaCount(context: Context): Int =
        readJson(context)?.optJSONArray("formulas")?.length() ?: 0

    fun nameCount(context: Context): Int =
        readJson(context)?.optJSONArray("savedNames")?.length() ?: 0

    // ------------------------------------------------------------------ sanitizing & JSON codec

    /**
     * Returns a cleaned copy of [obj] containing only the parts that are present and valid, or null
     * when it holds no calculator data at all.
     */
    fun sanitize(obj: JSONObject): JSONObject? {
        val namesArr = obj.optJSONArray("savedNames")
        val formulasArr = obj.optJSONArray("formulas")
        val sessionObj = obj.optJSONObject("session")
        if (namesArr == null && formulasArr == null && sessionObj == null) return null

        val out = JSONObject()
        out.put("v", SCHEMA_VERSION)
        if (namesArr != null) out.put("savedNames", JSONArray(parseNames(namesArr)))
        if (formulasArr != null) out.put("formulas", formulasToJson(parseFormulas(formulasArr)))
        if (sessionObj != null) out.put("session", sessionToJson(parseSession(sessionObj)))
        out.put("updatedAt", obj.optLong("updatedAt", System.currentTimeMillis()))
        return out
    }

    private fun toState(obj: JSONObject): CalcStoreState {
        val names = obj.optJSONArray("savedNames")?.let { parseNames(it) } ?: FormulaCalcEngine.DEFAULT_NAMES
        val formulas = obj.optJSONArray("formulas")?.let { parseFormulas(it) } ?: emptyList()
        val session = obj.optJSONObject("session")?.let { parseSession(it) }
        return CalcStoreState(names, formulas, session, obj.optLong("updatedAt", 0L))
    }

    private fun toJson(state: CalcStoreState): JSONObject {
        val out = JSONObject()
        out.put("v", SCHEMA_VERSION)
        out.put("savedNames", JSONArray(state.savedNames))
        out.put("formulas", formulasToJson(state.formulas))
        state.session?.let { out.put("session", sessionToJson(it)) }
        out.put("updatedAt", state.updatedAt)
        return out
    }

    private fun parseNames(array: JSONArray): List<String> {
        val seen = LinkedHashSet<String>()
        for (i in 0 until array.length()) {
            val n = array.optString(i, "").trim()
            if (n.isNotEmpty()) seen.add(n)
        }
        return seen.toList()
    }

    private fun parseFormulas(array: JSONArray): List<CalcSavedFormula> {
        val out = ArrayList<CalcSavedFormula>()
        val usedIds = HashSet<Long>()
        val usedNames = HashSet<String>()
        for (i in 0 until array.length()) {
            val o = array.optJSONObject(i) ?: continue
            val name = o.optString("name", "").trim()
            val ingArr = o.optJSONArray("ingredients")
            if (name.isEmpty() || ingArr == null || !usedNames.add(name)) continue
            val ings = ArrayList<CalcSavedIngredient>()
            for (j in 0 until ingArr.length()) {
                val io = ingArr.optJSONObject(j) ?: continue
                val iname = io.optString("name", "").trim()
                if (iname.isEmpty()) continue
                ings.add(CalcSavedIngredient(iname, CalcIngType.fromKey(io.optString("type")), nonNegative(io.optDouble("value", 0.0))))
            }
            var id = o.optLong("id", System.currentTimeMillis() + i)
            while (!usedIds.add(id)) id += 1
            out.add(
                CalcSavedFormula(
                    id = id,
                    name = name,
                    savedAt = o.optLong("savedAt", id),
                    mode = CalcMode.fromKey(o.optString("mode")),
                    baseKg = nonNegative(o.optDouble("baseKg", 0.0)),
                    capKg = nonNegative(o.optDouble("capKg", 0.0)),
                    multiplier = nonNegative(o.optDouble("mult", 0.0)),
                    ingredients = ings
                )
            )
        }
        return out
    }

    private fun formulasToJson(list: List<CalcSavedFormula>): JSONArray {
        val arr = JSONArray()
        list.forEach { f ->
            val ings = JSONArray()
            f.ingredients.forEach { i ->
                ings.put(JSONObject().put("name", i.name).put("type", i.type.key).put("value", i.valueKg))
            }
            arr.put(
                JSONObject()
                    .put("id", f.id)
                    .put("name", f.name)
                    .put("savedAt", f.savedAt)
                    .put("mode", f.mode.key)
                    .put("baseKg", f.baseKg)
                    .put("capKg", f.capKg)
                    .put("mult", f.multiplier)
                    .put("ingredients", ings)
            )
        }
        return arr
    }

    private fun parseSession(o: JSONObject): CalcSession {
        fun ings(array: JSONArray?): List<CalcSessionIngredient> {
            if (array == null) return emptyList()
            val list = ArrayList<CalcSessionIngredient>()
            for (i in 0 until array.length()) {
                val io = array.optJSONObject(i) ?: continue
                list.add(
                    CalcSessionIngredient(
                        id = io.optInt("id", -1),
                        name = io.optString("name", ""),
                        type = CalcIngType.fromKey(io.optString("type")),
                        valueKg = nonNegative(io.optDouble("value", 0.0))
                    )
                )
            }
            return list
        }

        val target = ings(o.optJSONArray("target"))
        return CalcSession(
            unit = CalcUnit.fromKey(o.optString("unit")),
            mode = CalcMode.fromKey(o.optString("mode")),
            base = o.optString("base", ""),
            cap = o.optString("cap", ""),
            mult = o.optString("mult", ""),
            orig = o.optString("orig", ""),
            used = o.optString("used", ""),
            ingredients = ings(o.optJSONArray("ingredients")),
            targetDirty = o.optBoolean("targetDirty", false) && target.isNotEmpty(),
            target = target
        )
    }

    private fun sessionToJson(s: CalcSession): JSONObject {
        fun ings(list: List<CalcSessionIngredient>): JSONArray {
            val arr = JSONArray()
            list.forEach { i ->
                arr.put(JSONObject().put("id", i.id).put("name", i.name).put("type", i.type.key).put("ratio", 0).put("value", i.valueKg))
            }
            return arr
        }
        return JSONObject()
            .put("unit", s.unit.label)
            .put("mode", s.mode.key)
            .put("base", s.base)
            .put("cap", s.cap)
            .put("mult", s.mult)
            .put("orig", s.orig)
            .put("used", s.used)
            .put("ingredients", ings(s.ingredients))
            .put("targetDirty", s.targetDirty)
            .put("target", ings(s.target))
    }

    private fun nonNegative(v: Double): Double = if (v.isNaN() || v.isInfinite() || v < 0) 0.0 else v
}

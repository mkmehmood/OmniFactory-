package com.example.data.preferences

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * On-device storage for the WebView based Mixture Calculator.
 *
 * The calculator keeps one JSON document (saved ingredient names, saved formulas and the last
 * working session). It lives in a private file so it survives app restarts and is picked up by
 * the full-app backup / restore (see DatabaseBackupManager, key "mixtureCalculator").
 */
object MixtureCalculatorStore {

    private const val FILE_NAME = "mixture_calculator_state.json"

    private fun file(context: Context): File = File(context.filesDir, FILE_NAME)

    /** Raw JSON text, or an empty string when nothing has been saved yet. */
    @Synchronized
    fun read(context: Context): String {
        return try {
            val f = file(context)
            if (f.exists() && f.length() > 2) f.readText() else ""
        } catch (e: Exception) {
            ""
        }
    }

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
            JSONObject(json) // validate before touching the file
            file(context).writeText(json)
            true
        } catch (e: Exception) {
            false
        }
    }

    /** Replace everything with the supplied state. */
    fun replace(context: Context, incoming: JSONObject): Boolean = write(context, incoming.toString())

    /** Merge: union of names, formulas added when their name is new, current session kept. */
    fun merge(context: Context, incoming: JSONObject): Boolean {
        return try {
            val current = readJson(context) ?: JSONObject()

            val names = LinkedHashSet<String>()
            collectNames(current.optJSONArray("savedNames"), names)
            collectNames(incoming.optJSONArray("savedNames"), names)
            current.put("savedNames", JSONArray(names.toList()))

            val formulas = current.optJSONArray("formulas") ?: JSONArray()
            val knownNames = HashSet<String>()
            for (i in 0 until formulas.length()) {
                val existing = formulas.optJSONObject(i) ?: continue
                knownNames.add(existing.optString("name"))
            }
            val incomingFormulas = incoming.optJSONArray("formulas")
            if (incomingFormulas != null) {
                for (i in 0 until incomingFormulas.length()) {
                    val f = incomingFormulas.optJSONObject(i) ?: continue
                    if (knownNames.add(f.optString("name"))) formulas.put(f)
                }
            }
            current.put("formulas", formulas)

            if (!current.has("session") && incoming.has("session")) {
                current.put("session", incoming.get("session"))
            }
            current.put("v", 2)
            write(context, current.toString())
        } catch (e: Exception) {
            false
        }
    }

    @Synchronized
    fun clear(context: Context) {
        try {
            file(context).delete()
        } catch (e: Exception) {
            // non-fatal
        }
    }

    fun formulaCount(context: Context): Int =
        readJson(context)?.optJSONArray("formulas")?.length() ?: 0

    fun nameCount(context: Context): Int =
        readJson(context)?.optJSONArray("savedNames")?.length() ?: 0

    private fun collectNames(array: JSONArray?, into: MutableSet<String>) {
        if (array == null) return
        for (i in 0 until array.length()) {
            val n = array.optString(i, "").trim()
            if (n.isNotEmpty()) into.add(n)
        }
    }
}

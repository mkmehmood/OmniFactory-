package com.example.domain.mixture

import org.json.JSONArray
import org.json.JSONObject

/** Per-ingredient solver settings, stored in the same order as the saved ingredient list. */
data class IngredientFormulaConfig(
    val componentRole: ComponentRole = ComponentRole.SCALE,
    val fixedText: String = "",
    val referenceRole: ReferenceRole = ReferenceRole.MAIN,
    val batchRole: BatchRole = BatchRole.PROPORTIONAL,
    val inTubText: String = ""
)

/**
 * Everything needed to restore the formulation method and its inputs when a saved recipe is reloaded.
 * Ingredient ids are regenerated on load, so per-ingredient values are matched by position.
 */
data class MixtureFormulaConfig(
    val method: EquationMode = EquationMode.ANCHOR,
    val targetTotalText: String = "",
    val targetTotalUnit: String = "kg",
    val referenceWeightText: String = "",
    val referenceUnit: String = "kg",
    val referenceResultUnit: String = "kg",
    val baseTargetText: String = "",
    val baseTargetUnit: String = "kg",
    val batchMultiplierText: String = "1",
    val ingredients: List<IngredientFormulaConfig> = emptyList()
) {
    fun toJson(): String {
        val root = JSONObject()
        root.put("method", method.name)
        root.put("targetTotalText", targetTotalText)
        root.put("targetTotalUnit", targetTotalUnit)
        root.put("referenceWeightText", referenceWeightText)
        root.put("referenceUnit", referenceUnit)
        root.put("referenceResultUnit", referenceResultUnit)
        root.put("baseTargetText", baseTargetText)
        root.put("baseTargetUnit", baseTargetUnit)
        root.put("batchMultiplierText", batchMultiplierText)
        val arr = JSONArray()
        ingredients.forEach { i ->
            arr.put(
                JSONObject().apply {
                    put("componentRole", i.componentRole.name)
                    put("fixedText", i.fixedText)
                    put("referenceRole", i.referenceRole.name)
                    put("batchRole", i.batchRole.name)
                    put("inTubText", i.inTubText)
                }
            )
        }
        root.put("ingredients", arr)
        return root.toString()
    }

    companion object {
        /** Returns null for empty or unreadable JSON (e.g. recipes saved before this feature existed). */
        fun fromJson(json: String?): MixtureFormulaConfig? {
            if (json.isNullOrBlank()) return null
            return try {
                val root = JSONObject(json)
                val arr = root.optJSONArray("ingredients") ?: JSONArray()
                val list = (0 until arr.length()).map { idx ->
                    val o = arr.getJSONObject(idx)
                    IngredientFormulaConfig(
                        componentRole = enumOrDefault(o.optString("componentRole"), ComponentRole.SCALE),
                        fixedText = o.optString("fixedText", ""),
                        referenceRole = enumOrDefault(o.optString("referenceRole"), ReferenceRole.MAIN),
                        batchRole = enumOrDefault(o.optString("batchRole"), BatchRole.PROPORTIONAL),
                        inTubText = o.optString("inTubText", "")
                    )
                }
                MixtureFormulaConfig(
                    method = enumOrDefault(root.optString("method"), EquationMode.ANCHOR),
                    targetTotalText = root.optString("targetTotalText", ""),
                    targetTotalUnit = root.optString("targetTotalUnit", "kg"),
                    referenceWeightText = root.optString("referenceWeightText", ""),
                    referenceUnit = root.optString("referenceUnit", "kg"),
                    referenceResultUnit = root.optString("referenceResultUnit", "kg"),
                    baseTargetText = root.optString("baseTargetText", ""),
                    baseTargetUnit = root.optString("baseTargetUnit", "kg"),
                    batchMultiplierText = root.optString("batchMultiplierText", "1"),
                    ingredients = list
                )
            } catch (e: Exception) {
                null
            }
        }

        private inline fun <reified T : Enum<T>> enumOrDefault(name: String, default: T): T =
            enumValues<T>().firstOrNull { it.name == name } ?: default
    }
}

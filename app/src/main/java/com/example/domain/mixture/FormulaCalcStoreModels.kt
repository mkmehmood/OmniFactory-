package com.example.domain.mixture

/** Ingredient inside a saved formula. Amount is in kg. */
data class CalcSavedIngredient(
    val name: String,
    val type: CalcIngType,
    val valueKg: Double
)

/** A named formula the person saved to reuse later. */
data class CalcSavedFormula(
    val id: Long,
    val name: String,
    val savedAt: Long,
    val mode: CalcMode,
    val baseKg: Double,
    val capKg: Double,
    val multiplier: Double,
    val ingredients: List<CalcSavedIngredient>
)

/** Ingredient row of the working session. Amount is in kg. */
data class CalcSessionIngredient(
    val id: Int,
    val name: String,
    val type: CalcIngType,
    val valueKg: Double
)

/** The last working session, restored when the calculator is reopened. */
data class CalcSession(
    val unit: CalcUnit = CalcUnit.KG,
    val mode: CalcMode = CalcMode.UNIT_BASED,
    val base: String = "",
    val cap: String = "",
    val mult: String = "",
    val orig: String = "",
    val used: String = "",
    val ingredients: List<CalcSessionIngredient> = emptyList(),
    val targetDirty: Boolean = false,
    val target: List<CalcSessionIngredient> = emptyList()
)

/** Everything the calculator keeps on the device (and what backups carry). */
data class CalcStoreState(
    val savedNames: List<String> = FormulaCalcEngine.DEFAULT_NAMES,
    val formulas: List<CalcSavedFormula> = emptyList(),
    val session: CalcSession? = null,
    val updatedAt: Long = 0L
)

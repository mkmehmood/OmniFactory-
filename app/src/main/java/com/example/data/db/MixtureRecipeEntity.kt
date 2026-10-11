package com.example.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "mixture_recipes",
    indices = [
        Index(value = ["category"]),
        Index(value = ["timestamp"])
    ]
)
data class MixtureRecipeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String, // "Structural", "Chemical", "Culinary", "Industrial", "Custom"
    val description: String,
    val anchorIngredientName: String,
    val scaleFactor: Double = 1.0,
    val totalTargetWeight: Double,
    val unit: String = "kg",
    val ingredientsJson: String, // serialized target ingredients
    val batchCount: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val simplifiedRatioString: String = "",
    val anchorTargetQuantity: Double = 0.0,
    val anchorActualAdded: Double = 0.0,
    val scaleExplanation: String = "",
    val recalibratedProportionsJson: String = "[]",
    val totalNewMixtureWeight: Double = 0.0,
    val batchDivisionJson: String = "[]",
    val totalWeightPerBatch: Double = 0.0,
    /** JSON of the formulation method and solver inputs; empty for recipes saved before it existed. */
    @ColumnInfo(defaultValue = "''")
    val formulaConfigJson: String = ""
)

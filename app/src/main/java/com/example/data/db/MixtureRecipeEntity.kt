package com.example.data.db

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
    val isFavorite: Boolean = false
)

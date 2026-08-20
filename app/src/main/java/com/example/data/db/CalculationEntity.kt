package com.example.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "calculation_history",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["isPinned"]),
        Index(value = ["tag"])
    ]
)
data class CalculationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val expression: String,
    val result: String,
    val timestamp: Long = System.currentTimeMillis(),
    val tag: String = "General",
    val note: String = "",
    val isPinned: Boolean = false
)

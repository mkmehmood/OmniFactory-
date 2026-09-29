package com.example.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "batch_scaling_history",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["title"])
    ]
)
data class BatchScalingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val scalingMode: String = "TARGET_TOTAL", // "TARGET_TOTAL" or "BATCH_SIZE"
    val batchSubMode: String = "BY_BATCH_COUNT", // "BY_BATCH_COUNT" or "BY_AVAILABLE_AMOUNT"
    val targetTotalText: String = "",
    val batchSizeText: String = "",
    val batchCountText: String = "1",
    val availableAmountText: String = "",
    val roundingOption: String = "ONE_DECIMAL",
    val componentsJson: String = "[]", // List of ComponentInput (id, name, amountText, isLocked)
    val originalGrandTotal: Double = 0.0,
    val scaledGrandTotal: Double = 0.0,
    val scalingFactor: Double = 1.0,
    val singleBatchGrandTotal: Double = 0.0,
    val componentsCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val note: String = "",
    val formulaStepsJson: String = "[]",
    val arithmeticSummary: String = "",
    val scaledComponentsJson: String = "[]",
    val lockedTotal: Double = 0.0,
    val unlockedOriginalTotal: Double = 0.0,
    val unlockedScaledTotal: Double = 0.0
)

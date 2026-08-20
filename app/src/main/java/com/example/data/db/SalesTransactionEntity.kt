package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sales_transactions")
data class SalesTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val salesRep: String,
    val dateMillis: Long,
    val totalSoldQty: Double,
    val returnedQty: Double,
    val expiredQty: Double,
    val creditSalesQty: Double,
    val recoveredCredit: Double,
    val physicalCashInHand: Double,
    val expense: Double,
    val unitSalePrice: Double,
    val commissionPerUnit: Double = 0.0,
    val totalCommission: Double = 0.0,
    val netSoldUnits: Double,
    val cashQtyUnits: Double,
    val expectedCash: Double,
    val reconciledCash: Double = physicalCashInHand + expense,
    val discrepancy: Double,
    val status: String,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = ""
)

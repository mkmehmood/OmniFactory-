package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ledger_transactions")
data class LedgerTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String,                       // "CASH_IN", "CASH_OUT", "OPENING_BALANCE"
    val amount: Double,
    val category: String,                   // "Sales", "Customer Payment", "Raw Materials", "Groceries & Food", etc.
    val paymentMode: String,                // "Cash", "Bank Transfer", "Easypaisa", "JazzCash", "Cheque", etc. (For TRANSFER: FROM mode)
    val transferToMode: String = "",        // For TRANSFER: Destination / TO mode (e.g. "Bank Transfer")
    val expenseScope: String = "BUSINESS",  // "BUSINESS", "HOME", or "" (for Income / Cash In)
    val partyName: String = "",             // Customer / Vendor / Party name
    val remarks: String = "",               // Description / note
    val referenceNo: String = "",           // Bill #, Invoice #, UPI / Cheque ref #
    val dateMillis: Long,                   // Transaction date (epoch millis)
    val timestamp: Long = System.currentTimeMillis() // Creation timestamp
)

package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ledger_transactions")
data class LedgerTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String,              // "CASH_IN", "CASH_OUT", "OPENING_BALANCE"
    val amount: Double,
    val category: String,          // "Sales", "Customer Payment", "Expense", "Salary", "Purchase", etc.
    val paymentMode: String,       // "CASH", "BANK_TRANSFER", "UPI_DIGITAL", "CHEQUE", "OTHER"
    val partyName: String = "",    // Customer / Vendor / Party name
    val remarks: String = "",      // Description / note
    val referenceNo: String = "",  // Bill #, Invoice #, UPI ref #
    val dateMillis: Long,          // Transaction date (epoch millis)
    val timestamp: Long = System.currentTimeMillis() // Creation timestamp
)

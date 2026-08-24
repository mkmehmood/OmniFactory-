package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LedgerTransactionDao {
    @Query("SELECT * FROM ledger_transactions ORDER BY dateMillis DESC, timestamp DESC")
    fun getAllTransactions(): Flow<List<LedgerTransactionEntity>>

    @Query("SELECT * FROM ledger_transactions WHERE dateMillis >= :startMillis AND dateMillis <= :endMillis ORDER BY dateMillis DESC, timestamp DESC")
    fun getTransactionsBetween(startMillis: Long, endMillis: Long): Flow<List<LedgerTransactionEntity>>

    @Query("SELECT * FROM ledger_transactions ORDER BY dateMillis DESC, timestamp DESC")
    suspend fun getAllList(): List<LedgerTransactionEntity>

    @Query("SELECT COUNT(*) FROM ledger_transactions")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(entity: LedgerTransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<LedgerTransactionEntity>)

    @Update
    suspend fun updateTransaction(entity: LedgerTransactionEntity)

    @Delete
    suspend fun deleteTransaction(entity: LedgerTransactionEntity)

    @Query("DELETE FROM ledger_transactions")
    suspend fun clearAllTransactions()
}

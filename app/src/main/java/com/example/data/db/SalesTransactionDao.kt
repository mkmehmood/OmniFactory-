package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SalesTransactionDao {
    @Query("SELECT * FROM sales_transactions ORDER BY dateMillis DESC, timestamp DESC")
    fun getAllTransactions(): Flow<List<SalesTransactionEntity>>

    @Query("SELECT * FROM sales_transactions ORDER BY dateMillis DESC, timestamp DESC")
    suspend fun getAllList(): List<SalesTransactionEntity>

    @Query("SELECT COUNT(*) FROM sales_transactions")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(entity: SalesTransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<SalesTransactionEntity>)

    @Delete
    suspend fun deleteTransaction(entity: SalesTransactionEntity)

    @Query("DELETE FROM sales_transactions")
    suspend fun clearAllTransactions()
}

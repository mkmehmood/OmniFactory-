package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CalculationDao {
    @Query("SELECT * FROM calculation_history ORDER BY isPinned DESC, timestamp DESC")
    fun getAllHistory(): Flow<List<CalculationEntity>>

    @Query("SELECT * FROM calculation_history WHERE tag = :tag ORDER BY isPinned DESC, timestamp DESC")
    fun getHistoryByTag(tag: String): Flow<List<CalculationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalculation(entity: CalculationEntity): Long

    @Update
    suspend fun updateCalculation(entity: CalculationEntity)

    @Delete
    suspend fun deleteCalculation(entity: CalculationEntity)

    @Query("DELETE FROM calculation_history")
    suspend fun clearAllHistory()
}

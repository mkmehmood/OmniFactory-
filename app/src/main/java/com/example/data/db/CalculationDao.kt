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

    @Query("SELECT * FROM calculation_history WHERE expression LIKE '%' || :query || '%' OR result LIKE '%' || :query || '%' OR note LIKE '%' || :query || '%' ORDER BY isPinned DESC, timestamp DESC")
    fun searchHistory(query: String): Flow<List<CalculationEntity>>

    @Query("SELECT COUNT(*) FROM calculation_history")
    suspend fun getHistoryCount(): Int

    @Query("SELECT COUNT(*) FROM calculation_history")
    fun getHistoryCountFlow(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalculation(entity: CalculationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<CalculationEntity>)

    @Update
    suspend fun updateCalculation(entity: CalculationEntity)

    @Delete
    suspend fun deleteCalculation(entity: CalculationEntity)

    @Query("DELETE FROM calculation_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM calculation_history")
    suspend fun clearAllHistory()
}

package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BatchScalingDao {

    @Query("SELECT * FROM batch_scaling_history ORDER BY isPinned DESC, timestamp DESC")
    fun getAllHistory(): Flow<List<BatchScalingEntity>>

    @Query("SELECT * FROM batch_scaling_history ORDER BY isPinned DESC, timestamp DESC")
    suspend fun getAllList(): List<BatchScalingEntity>

    @Query("SELECT * FROM batch_scaling_history WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): BatchScalingEntity?

    @Query("SELECT COUNT(*) FROM batch_scaling_history")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BatchScalingEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<BatchScalingEntity>)

    @Update
    suspend fun update(entity: BatchScalingEntity)

    @Delete
    suspend fun delete(entity: BatchScalingEntity)

    @Query("DELETE FROM batch_scaling_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM batch_scaling_history")
    suspend fun clearAll()
}

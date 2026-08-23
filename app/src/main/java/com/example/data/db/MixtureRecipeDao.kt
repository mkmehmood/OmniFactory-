package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MixtureRecipeDao {

    @Query("SELECT * FROM mixture_recipes ORDER BY timestamp DESC")
    fun getAllRecipes(): Flow<List<MixtureRecipeEntity>>

    @Query("SELECT * FROM mixture_recipes ORDER BY timestamp DESC")
    suspend fun getAllList(): List<MixtureRecipeEntity>

    @Query("SELECT * FROM mixture_recipes WHERE category = :category ORDER BY timestamp DESC")
    fun getRecipesByCategory(category: String): Flow<List<MixtureRecipeEntity>>

    @Query("SELECT * FROM mixture_recipes WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteRecipes(): Flow<List<MixtureRecipeEntity>>

    @Query("SELECT * FROM mixture_recipes WHERE id = :id LIMIT 1")
    suspend fun getRecipeById(id: Long): MixtureRecipeEntity?

    @Query("SELECT COUNT(*) FROM mixture_recipes")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recipe: MixtureRecipeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(recipes: List<MixtureRecipeEntity>)

    @Update
    suspend fun update(recipe: MixtureRecipeEntity)

    @Delete
    suspend fun delete(recipe: MixtureRecipeEntity)

    @Query("DELETE FROM mixture_recipes WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM mixture_recipes")
    suspend fun clearAll()
}

package com.example.data.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "structural_formulas",
    indices = [
        Index(value = ["category"]),
        Index(value = ["title"])
    ]
)
data class MathematicalFormulaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String, // "Structural Mechanics", "Geometry & Trigonometry", "Thermodynamics", "Civil Engineering", "Material Science", "Financial Math"
    val formulaNotation: String,
    val description: String,
    val variableDefinitions: String, // e.g. "σ = Force/Area, E = Young's Modulus"
    val exampleCalculation: String,
    val isFavorite: Boolean = false
)

@Dao
interface MathematicalFormulaDao {

    @Query("SELECT * FROM structural_formulas ORDER BY id ASC")
    fun getAllFormulas(): Flow<List<MathematicalFormulaEntity>>

    @Query("SELECT * FROM structural_formulas WHERE category = :category ORDER BY id ASC")
    fun getFormulasByCategory(category: String): Flow<List<MathematicalFormulaEntity>>

    @Query("SELECT * FROM structural_formulas WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchFormulas(query: String): Flow<List<MathematicalFormulaEntity>>

    @Query("SELECT COUNT(*) FROM structural_formulas")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(formulas: List<MathematicalFormulaEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(formula: MathematicalFormulaEntity): Long
}

package com.example.data.repository

import com.example.data.db.CalculationDao
import com.example.data.db.CalculationEntity
import kotlinx.coroutines.flow.Flow

class HistoryRepository(private val calculationDao: CalculationDao) {
    val allHistory: Flow<List<CalculationEntity>> = calculationDao.getAllHistory()

    fun getHistoryByTag(tag: String): Flow<List<CalculationEntity>> {
        return calculationDao.getHistoryByTag(tag)
    }

    suspend fun insertCalculation(expression: String, result: String, tag: String = "General", note: String = ""): Long {
        val entity = CalculationEntity(
            expression = expression,
            result = result,
            tag = tag,
            note = note,
            timestamp = System.currentTimeMillis()
        )
        return calculationDao.insertCalculation(entity)
    }

    suspend fun updateCalculation(entity: CalculationEntity) {
        calculationDao.updateCalculation(entity)
    }

    suspend fun deleteCalculation(entity: CalculationEntity) {
        calculationDao.deleteCalculation(entity)
    }

    suspend fun clearAll() {
        calculationDao.clearAllHistory()
    }
}

package com.example.data.repository

import com.example.data.db.SalesTransactionDao
import com.example.data.db.SalesTransactionEntity
import kotlinx.coroutines.flow.Flow

class SalesRepository(private val salesTransactionDao: SalesTransactionDao) {
    val allTransactions: Flow<List<SalesTransactionEntity>> = salesTransactionDao.getAllTransactions()

    suspend fun insertTransaction(entity: SalesTransactionEntity): Long {
        return salesTransactionDao.insertTransaction(entity)
    }

    suspend fun deleteTransaction(entity: SalesTransactionEntity) {
        salesTransactionDao.deleteTransaction(entity)
    }

    suspend fun clearAll() {
        salesTransactionDao.clearAllTransactions()
    }
}

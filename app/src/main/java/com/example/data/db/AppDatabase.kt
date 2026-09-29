package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CalculationEntity::class,
        SalesTransactionEntity::class,
        MixtureRecipeEntity::class,
        LedgerTransactionEntity::class,
        BatchScalingEntity::class
    ],
    version = 12,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun calculationDao(): CalculationDao
    abstract fun salesTransactionDao(): SalesTransactionDao
    abstract fun mixtureRecipeDao(): MixtureRecipeDao
    abstract fun ledgerTransactionDao(): LedgerTransactionDao
    abstract fun batchScalingDao(): BatchScalingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "calculator_persistent_db"
                )
                .fallbackToDestructiveMigration(dropAllTables = false)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

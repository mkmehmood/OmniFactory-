package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        CalculationEntity::class,
        SalesTransactionEntity::class,
        MixtureRecipeEntity::class,
        LedgerTransactionEntity::class
    ],
    version = 14,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun calculationDao(): CalculationDao
    abstract fun salesTransactionDao(): SalesTransactionDao
    abstract fun mixtureRecipeDao(): MixtureRecipeDao
    abstract fun ledgerTransactionDao(): LedgerTransactionDao

    companion object {
        /** Adds the stored formulation method / solver inputs to saved mixture recipes without losing data. */
        private val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE mixture_recipes ADD COLUMN formulaConfigJson TEXT NOT NULL DEFAULT ''")
            }
        }

        /** The standalone Batch Scaler was replaced by the Mixture Calculator's scale step; drop its table. */
        private val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS batch_scaling_history")
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "calculator_persistent_db"
                )
                .addMigrations(MIGRATION_12_13, MIGRATION_13_14)
                .fallbackToDestructiveMigration(dropAllTables = false)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

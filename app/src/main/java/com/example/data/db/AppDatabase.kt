package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        CalculationEntity::class,
        SalesTransactionEntity::class,
        MixtureRecipeEntity::class,
        MathematicalFormulaEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun calculationDao(): CalculationDao
    abstract fun salesTransactionDao(): SalesTransactionDao
    abstract fun mixtureRecipeDao(): MixtureRecipeDao
    abstract fun mathematicalFormulaDao(): MathematicalFormulaDao

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
                .addCallback(DatabaseCallback(context.applicationContext))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val context: Context
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Prepopulate persistent database with structural and mathematical data
            CoroutineScope(Dispatchers.IO).launch {
                val database = getDatabase(context)
                populateInitialStructuralData(database)
            }
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            CoroutineScope(Dispatchers.IO).launch {
                val database = getDatabase(context)
                // Ensure data exists if empty
                if (database.mathematicalFormulaDao().getCount() == 0) {
                    populateInitialStructuralData(database)
                }
            }
        }

        private suspend fun populateInitialStructuralData(database: AppDatabase) {
            // 1. Prepopulate Curated Structural & Mathematical Formulas Database
            val formulas = listOf(
                MathematicalFormulaEntity(
                    title = "Euler-Bernoulli Beam Bending Stress",
                    category = "Structural Mechanics",
                    formulaNotation = "σ = (M · y) / I",
                    description = "Calculates maximum flexural tensile or compressive normal stress in a beam under transverse bending moment M.",
                    variableDefinitions = "M = Bending Moment (N·m), y = Distance from neutral axis (m), I = Second moment of area (m⁴)",
                    exampleCalculation = "M = 15,000 N·m, y = 0.15 m, I = 0.00045 m⁴ → σ = 5.00 MPa"
                ),
                MathematicalFormulaEntity(
                    title = "Column Critical Buckling Load (Euler's Formula)",
                    category = "Structural Mechanics",
                    formulaNotation = "P_cr = (π² · E · I) / (K · L)²",
                    description = "Determines critical axial load at which an ideal slender structural column will buckle elastically.",
                    variableDefinitions = "E = Modulus of Elasticity (GPa), I = Area moment of inertia (m⁴), L = Column length (m), K = Effective length factor",
                    exampleCalculation = "E = 200 GPa, I = 8.33×10⁻⁶ m⁴, L = 4.0 m, K = 1.0 (pinned-pinned) → P_cr = 1,027.6 kN"
                ),
                MathematicalFormulaEntity(
                    title = "Hooke's Law & Axial Deformation",
                    category = "Structural Mechanics",
                    formulaNotation = "δ = (P · L) / (A · E)",
                    description = "Computes total linear elongation or compression of an axial structural member under uniform axial tensile or compressive load P.",
                    variableDefinitions = "P = Axial load (N), L = Original length (m), A = Cross-sectional area (m²), E = Modulus of elasticity (Pa)",
                    exampleCalculation = "P = 50,000 N, L = 2.5 m, A = 0.0012 m², E = 210 GPa → δ = 0.496 mm"
                ),
                MathematicalFormulaEntity(
                    title = "Torsional Shear Stress in Circular Shafts",
                    category = "Structural Mechanics",
                    formulaNotation = "τ_max = (T · r) / J",
                    description = "Determines maximum torsional shear stress generated in a solid or hollow cylindrical shaft subjected to twisting torque T.",
                    variableDefinitions = "T = Torque (N·m), r = Outer radius (m), J = Polar moment of inertia (m⁴, J = π·r⁴/2)",
                    exampleCalculation = "T = 2,400 N·m, r = 0.035 m, J = 2.357×10⁻⁶ m⁴ → τ = 35.64 MPa"
                ),
                MathematicalFormulaEntity(
                    title = "Structural Concrete Mix Design (IS/ACI)",
                    category = "Material Science",
                    formulaNotation = "Water-Cement Ratio: W/C = 0.45",
                    description = "Standard high-durability C30 concrete mix ratio by weight (1 : 1.8 : 2.8 : 0.45).",
                    variableDefinitions = "Cement = 1.0, Fine Agg (Sand) = 1.8, Coarse Agg (Gravel) = 2.8, Water = 0.45",
                    exampleCalculation = "Per 1 m³: Cement 380 kg, Sand 684 kg, Aggregate 1064 kg, Water 171 kg"
                ),
                MathematicalFormulaEntity(
                    title = "Compound Interest & Capital Growth",
                    category = "Financial Math",
                    formulaNotation = "A = P · (1 + r/n)^(n·t)",
                    description = "Calculates accumulated future balance resulting from initial principal P compounding at annual rate r over t years.",
                    variableDefinitions = "P = Principal sum, r = Annual nominal interest rate, n = Compounding frequency per year, t = Time in years",
                    exampleCalculation = "P = PKR 500,000, r = 12% (0.12), n = 12 (monthly), t = 5 yrs → A = PKR 908,348.35"
                )
            )
            database.mathematicalFormulaDao().insertAll(formulas)

            // 2. Prepopulate Authentic Structural Mixture Formulations
            val recipes = listOf(
                MixtureRecipeEntity(
                    title = "High-Strength Structural Concrete (C35/45)",
                    category = "Structural",
                    description = "Standard heavy structural concrete mix design with silica fume for bridge piers, foundation rafts, and columns.",
                    anchorIngredientName = "Portland Cement (CEM I 52.5N)",
                    scaleFactor = 1.0,
                    totalTargetWeight = 2450.0,
                    unit = "kg",
                    ingredientsJson = """[{"name":"Portland Cement (CEM I 52.5N)","quantity":400.0,"unit":"kg"},{"name":"Fine Silica Sand (0-4mm)","quantity":720.0,"unit":"kg"},{"name":"Crushed Basalt Aggregate (10-20mm)","quantity":1150.0,"unit":"kg"},{"name":"Batching Water","quantity":160.0,"unit":"kg"},{"name":"Superplasticizer Admixture","quantity":20.0,"unit":"kg"}]""",
                    batchCount = 4
                ),
                MixtureRecipeEntity(
                    title = "Aerospace Structural Epoxy Matrix (2:1 Ratio)",
                    category = "Industrial",
                    description = "High-performance carbon fiber laminating epoxy matrix with reactive cycloaliphatic amine hardener.",
                    anchorIngredientName = "Epoxy Base Resin (Part A)",
                    scaleFactor = 1.0,
                    totalTargetWeight = 150.0,
                    unit = "kg",
                    ingredientsJson = """[{"name":"Epoxy Base Resin (Part A)","quantity":100.0,"unit":"kg"},{"name":"Amine Hardener (Part B)","quantity":50.0,"unit":"kg"}]""",
                    batchCount = 3
                ),
                MixtureRecipeEntity(
                    title = "Structural Masonry Mortar (M10 High Bond)",
                    category = "Structural",
                    description = "Engineered building mortar for load-bearing brickwork and stone masonry compliance with ASTM C270.",
                    anchorIngredientName = "Portland Cement",
                    scaleFactor = 1.0,
                    totalTargetWeight = 200.0,
                    unit = "kg",
                    ingredientsJson = """[{"name":"Portland Cement","quantity":50.0,"unit":"kg"},{"name":"Hydrated Lime Type S","quantity":15.0,"unit":"kg"},{"name":"Washed River Sand","quantity":135.0,"unit":"kg"}]""",
                    batchCount = 2
                )
            )
            database.mixtureRecipeDao().insertAll(recipes)

            // 3. Prepopulate Authentic Sales Audit Records in PKR
            val initialAudits = listOf(
                SalesTransactionEntity(
                    salesRep = "Tariq Mahmood",
                    dateMillis = System.currentTimeMillis() - 86400000L * 2,
                    totalSoldQty = 520.0,
                    returnedQty = 15.0,
                    expiredQty = 5.0,
                    creditSalesQty = 60.0,
                    recoveredCredit = 18500.0,
                    physicalCashInHand = 145000.0,
                    expense = 6500.0,
                    unitSalePrice = 300.0,
                    netSoldUnits = 500.0,
                    cashQtyUnits = 440.0,
                    expectedCash = 150500.0, // (440 * 300) + 18500 = 132000 + 18500 = 150500
                    reconciledCash = 151500.0, // 145000 + 6500 = 151500
                    discrepancy = 1000.0,
                    status = "OVER",
                    note = "Lahore South Industrial Hub Audit"
                ),
                SalesTransactionEntity(
                    salesRep = "Zainab Malik",
                    dateMillis = System.currentTimeMillis() - 86400000L,
                    totalSoldQty = 350.0,
                    returnedQty = 10.0,
                    expiredQty = 0.0,
                    creditSalesQty = 40.0,
                    recoveredCredit = 12000.0,
                    physicalCashInHand = 72000.0,
                    expense = 3000.0,
                    unitSalePrice = 250.0,
                    netSoldUnits = 340.0,
                    cashQtyUnits = 300.0,
                    expectedCash = 87000.0, // (300 * 250) + 12000 = 75000 + 12000 = 87000
                    reconciledCash = 75000.0, // 72000 + 3000 = 75000
                    discrepancy = -12000.0,
                    status = "SHORT",
                    note = "Islamabad Sector F-10 Distribution"
                ),
                SalesTransactionEntity(
                    salesRep = "Farhan Ahmed",
                    dateMillis = System.currentTimeMillis() - 3600000L * 4,
                    totalSoldQty = 480.0,
                    returnedQty = 20.0,
                    expiredQty = 10.0,
                    creditSalesQty = 50.0,
                    recoveredCredit = 25000.0,
                    physicalCashInHand = 200000.0,
                    expense = 9000.0,
                    unitSalePrice = 460.0,
                    netSoldUnits = 450.0,
                    cashQtyUnits = 400.0,
                    expectedCash = 209000.0, // (400 * 460) + 25000 = 184000 + 25000 = 209000
                    reconciledCash = 209000.0, // 200000 + 9000 = 209000
                    discrepancy = 0.0,
                    status = "BALANCED",
                    note = "Karachi Port Wholesale Terminal"
                )
            )
            database.salesTransactionDao().insertAll(initialAudits)
        }
    }
}

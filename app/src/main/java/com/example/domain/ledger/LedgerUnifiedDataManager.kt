package com.example.domain.ledger

import android.content.Context

object LedgerUnifiedDataManager {
    private const val PREFS_NAME = "ledger_and_expenses_unified_prefs"
    private const val KEY_BIZ_CATEGORIES = "unified_biz_categories"
    private const val KEY_HOME_CATEGORIES = "unified_home_categories"
    private const val KEY_INCOME_CATEGORIES = "unified_income_categories"
    private const val KEY_PAYMENT_MODES = "unified_payment_modes"

    val DEFAULT_BUSINESS_CATEGORIES = listOf(
        "Raw Materials",
        "Salaries & Labor",
        "Rent & Lease",
        "Electricity / Power",
        "Maintenance & Repairs",
        "Transport & Fuel",
        "Tea & Refreshment",
        "Packaging Supplies",
        "Office Supplies",
        "Taxes & Govt Fees",
        "Marketing & Ads",
        "Miscellaneous Business",
        "Noran Shah",
        "Noman Shah"
    )

    val DEFAULT_HOME_CATEGORIES = listOf(
        "Groceries & Food",
        "Home Electricity & Gas",
        "Water & Maintenance",
        "Home Rent",
        "School / Tuition Fees",
        "Medical & Healthcare",
        "House Help & Maid",
        "Clothing & Shopping",
        "Mobile & Internet",
        "Home Repairs",
        "Gifts & Charity",
        "Miscellaneous Home",
        "Home",
        "Kitchen Supplies",
        "Personal & Family"
    )

    val DEFAULT_INCOME_CATEGORIES = listOf(
        "Sales",
        "Customer Payment",
        "Recovery",
        "Advance Received",
        "Capital In",
        "Commission",
        "Loan Received",
        "Other Income",
        "Noran Shah",
        "Noman Shah"
    )

    val DEFAULT_PAYMENT_MODES = listOf(
        "Cash",
        "Bank Transfer",
        "Easypaisa",
        "JazzCash",
        "Cheque",
        "Online Transfer",
        "UPI",
        "Debit Card",
        "Credit Card"
    )

    fun getBusinessCategories(context: Context): List<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getStringSet(KEY_BIZ_CATEGORIES, null)
        return if (saved != null && saved.isNotEmpty()) {
            (DEFAULT_BUSINESS_CATEGORIES + saved).distinct()
        } else {
            DEFAULT_BUSINESS_CATEGORIES
        }
    }

    fun getHomeCategories(context: Context): List<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getStringSet(KEY_HOME_CATEGORIES, null)
        return if (saved != null && saved.isNotEmpty()) {
            (DEFAULT_HOME_CATEGORIES + saved).distinct()
        } else {
            DEFAULT_HOME_CATEGORIES
        }
    }

    fun getIncomeCategories(context: Context): List<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getStringSet(KEY_INCOME_CATEGORIES, null)
        return if (saved != null && saved.isNotEmpty()) {
            (DEFAULT_INCOME_CATEGORIES + saved).distinct()
        } else {
            DEFAULT_INCOME_CATEGORIES
        }
    }

    fun getAllCategories(context: Context): List<String> {
        return (getBusinessCategories(context) + getHomeCategories(context) + getIncomeCategories(context)).distinct()
    }

    fun getPaymentModes(context: Context): List<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getStringSet(KEY_PAYMENT_MODES, null)
        return if (saved != null && saved.isNotEmpty()) {
            (DEFAULT_PAYMENT_MODES + saved).distinct()
        } else {
            DEFAULT_PAYMENT_MODES
        }
    }

    fun saveCategoryIfNew(context: Context, category: String, scope: String) {
        val trimmed = category.trim()
        if (trimmed.isEmpty()) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val upperScope = scope.trim().uppercase()
        when {
            upperScope.contains("HOME") || upperScope.contains("PERSONAL") -> {
                val current = getHomeCategories(context)
                if (current.none { it.equals(trimmed, ignoreCase = true) }) {
                    val updated = (current + trimmed).toSet()
                    prefs.edit().putStringSet(KEY_HOME_CATEGORIES, updated).apply()
                }
            }
            upperScope.contains("INCOME") || upperScope.contains("CASH_IN") -> {
                val current = getIncomeCategories(context)
                if (current.none { it.equals(trimmed, ignoreCase = true) }) {
                    val updated = (current + trimmed).toSet()
                    prefs.edit().putStringSet(KEY_INCOME_CATEGORIES, updated).apply()
                }
            }
            else -> { // Default BUSINESS
                val current = getBusinessCategories(context)
                if (current.none { it.equals(trimmed, ignoreCase = true) }) {
                    val updated = (current + trimmed).toSet()
                    prefs.edit().putStringSet(KEY_BIZ_CATEGORIES, updated).apply()
                }
            }
        }
    }

    fun savePaymentModeIfNew(context: Context, mode: String) {
        val trimmed = mode.trim()
        if (trimmed.isEmpty()) return
        val current = getPaymentModes(context)
        if (current.none { it.equals(trimmed, ignoreCase = true) }) {
            val updated = (current + trimmed).toSet()
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putStringSet(KEY_PAYMENT_MODES, updated).apply()
        }
    }

    fun isHomeExpense(category: String, expenseScope: String = ""): Boolean {
        val scopeTrim = expenseScope.trim().uppercase()
        if (scopeTrim.contains("HOME") || scopeTrim.contains("PERSONAL")) return true

        val lower = category.lowercase().trim()
        if (lower.isEmpty()) return false

        // Check if category is a known home category or contains home-related terms
        val isKnownHome = lower == "home" || lower.contains("home") || lower.contains("grocer") ||
                lower.contains("school") || lower.contains("tuition") || lower.contains("maid") ||
                lower.contains("doctor") || lower.contains("medical") || lower.contains("clothing") ||
                lower.contains("personal") || lower.contains("family") || lower.contains("kitchen") ||
                lower.contains("house") || lower.contains("gas") || lower.contains("water") ||
                lower.contains("ration") || lower.contains("sabzi") || lower.contains("milk") ||
                lower.contains("child") || lower.contains("domestic") || lower.contains("food") ||
                lower.contains("shopping") || lower.contains("charity") || lower.contains("gift")

        if (isKnownHome) return true
        if (scopeTrim == "BUSINESS" || scopeTrim == "BUSINESS_EXPENSE") return false
        return false
    }
}

package com.example.expensetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Category {
    FOOD, TRANSPORT, AIRTIME_DATA, RENT_UTILITIES, SHOPPING,
    HEALTH, EDUCATION, ENTERTAINMENT, PEER_DEBTS, HELB_INCOME, FEES, OTHER
}

/** Who assigned the category. Logging this lets you measure model accuracy vs. user corrections. */
enum class CategorySource { USER, RULES, MODEL }

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountMinor: Long,              // store money as integer minor units (cents)
    val currency: String = "KES",
    val merchant: String,
    val note: String = "",
    val category: Category,
    val categorySource: CategorySource,
    val timestamp: Long = System.currentTimeMillis(),
    val transactionCode: String? = null, // e.g. M-Pesa ID "QG789XYZ" for deduplication
    val isIncome: Boolean = false        // true for income/disbursements, false for expenses
)

data class CategoryTotal(val category: Category, val total: Long)

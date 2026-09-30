package com.example.expensetracker.ai

import com.example.expensetracker.data.Category
import com.example.expensetracker.data.CategorySource

data class Prediction(
    val category: Category,
    val confidence: Float,          // 0..1
    val source: CategorySource,
)

/** Common interface so rule-based, on-device ML, and hybrid approaches are interchangeable. */
interface ExpenseCategorizer {
    suspend fun predict(merchant: String, note: String = ""): Prediction

    /** Returns full probability distribution across all categories (for AI diagnostics & sandbox). */
    fun predictAll(merchant: String, note: String = ""): List<Pair<Category, Float>> = emptyList()

    /** Optional online learning callback to update model parameters from user feedback. */
    fun train(merchant: String, note: String, category: Category) {}
}

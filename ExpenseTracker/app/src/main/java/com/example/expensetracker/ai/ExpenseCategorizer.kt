package com.example.expensetracker.ai

import com.example.expensetracker.data.Category
import com.example.expensetracker.data.CategorySource

data class Prediction(
    val category: Category,
    val confidence: Float,          // 0..1
    val source: CategorySource
)

/** Common interface so rule-based, on-device ML, and hybrid approaches are interchangeable (good for comparison experiments). */
interface ExpenseCategorizer {
    suspend fun predict(merchant: String, note: String = ""): Prediction
}

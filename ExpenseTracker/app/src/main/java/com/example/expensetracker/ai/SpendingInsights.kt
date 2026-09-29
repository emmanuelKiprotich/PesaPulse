package com.example.expensetracker.ai

import com.example.expensetracker.data.Category
import com.example.expensetracker.data.Expense
import kotlin.math.sqrt

data class Anomaly(val expense: Expense, val zScore: Double, val message: String)

/** Lightweight statistical anomaly detection: flags amounts far above the category's typical spend. */
object SpendingInsights {

    fun flagAnomalies(expenses: List<Expense>, zThreshold: Double = 2.0, minSamples: Int = 5): List<Anomaly> =
        expenses.groupBy { it.category }.flatMap { (category, items) ->
            if (items.size < minSamples) return@flatMap emptyList<Anomaly>()
            val amounts = items.map { it.amountMinor.toDouble() }
            val mean = amounts.average()
            val std = sqrt(amounts.map { (it - mean) * (it - mean) }.average())
            if (std == 0.0) return@flatMap emptyList<Anomaly>()
            items.mapNotNull { e ->
                val z = (e.amountMinor - mean) / std
                if (z >= zThreshold) Anomaly(e, z, "Unusually high ${label(category)} spend at ${e.merchant}") else null
            }
        }

    /** Naive next-month projection from the average of the most recent months (replace with a real model later). */
    fun projectMonthlyTotal(monthlyTotals: List<Long>): Long =
        if (monthlyTotals.isEmpty()) 0 else monthlyTotals.takeLast(3).average().toLong()

    private fun label(c: Category) = c.name.lowercase().replace('_', ' ')
}

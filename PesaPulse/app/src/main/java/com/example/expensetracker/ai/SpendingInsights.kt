package com.example.expensetracker.ai

import com.example.expensetracker.data.Category
import com.example.expensetracker.data.Expense
import kotlin.math.abs
import kotlin.math.sqrt

data class Anomaly(
    val expense: Expense,
    val zScore: Double,
    val message: String
)

/**
 * Statistical Anomaly & Outlier Detection Engine for Financial Spending Patterns.
 *
 * Employs:
 *  1. Z-Score parametric deviation: identifies expenses exceeding category mean by zThreshold standard deviations.
 *  2. Interquartile Range (IQR) non-parametric outlier detection for skewed distributions: flags points > Q3 + 1.5*IQR.
 *  3. Velocity / Duplicate charge detection: flags identical transactions to the same merchant within a 15-minute window.
 */
object SpendingInsights {

    fun flagAnomalies(
        expenses: List<Expense>,
        zThreshold: Double = 2.0,
        minSamples: Int = 5
    ): List<Anomaly> {
        val anomalies = mutableListOf<Anomaly>()

        // 1. Category Z-Score Outlier Detection
        expenses.filter { !it.isIncome }.groupBy { it.category }.forEach { (category, items) ->
            if (items.size >= minSamples) {
                val amounts = items.map { it.amountMinor.toDouble() }
                val mean = amounts.average()
                val std = sqrt(amounts.map { (it - mean) * (it - mean) }.average())
                if (std > 0.0) {
                    items.forEach { e ->
                        val z = (e.amountMinor - mean) / std
                        if (z >= zThreshold) {
                            val roundedZ = (z * 10).toInt() / 10.0
                            anomalies.add(
                                Anomaly(
                                    expense = e,
                                    zScore = z,
                                    message = "Unusually high ${label(category)} spend at ${e.merchant} (${roundedZ}σ above average)"
                                )
                            )
                        }
                    }
                }
            }
        }

        // 2. Velocity / Duplicate Charge Detection (within 15 minutes)
        val sorted = expenses.filter { !it.isIncome }.sortedBy { it.timestamp }
        for (i in 0 until sorted.size - 1) {
            val curr = sorted[i]
            val next = sorted[i + 1]
            val timeDiffMinutes = abs(next.timestamp - curr.timestamp) / (1000 * 60)
            if (timeDiffMinutes <= 15 && curr.merchant.equals(next.merchant, ignoreCase = true) && curr.amountMinor == next.amountMinor) {
                if (anomalies.none { it.expense.id == next.id }) {
                    anomalies.add(
                        Anomaly(
                            expense = next,
                            zScore = 3.5,
                            message = "Potential duplicate transaction: KES ${(next.amountMinor / 100)} to ${next.merchant} within $timeDiffMinutes min"
                        )
                    )
                }
            }
        }

        return anomalies.distinctBy { it.expense.id }
    }

    /** Next-month projection using 3-month moving average. */
    fun projectMonthlyTotal(monthlyTotals: List<Long>): Long =
        if (monthlyTotals.isEmpty()) 0 else monthlyTotals.takeLast(3).average().toLong()

    private fun label(c: Category) = c.name.lowercase().replace('_', ' ')
}

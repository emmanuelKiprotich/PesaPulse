package com.example.expensetracker

import com.example.expensetracker.ai.SpendingInsights
import com.example.expensetracker.data.Category
import com.example.expensetracker.data.CategorySource
import com.example.expensetracker.data.Expense
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SpendingInsightsTest {

    @Test
    fun testAnomalyDetectionIdentifiesOutlier() {
        val normalExpenses = listOf(
            Expense(id = 1, amountMinor = 150_00L, merchant = "Mama Mboga", category = Category.FOOD, categorySource = CategorySource.USER),
            Expense(id = 2, amountMinor = 180_00L, merchant = "Mess", category = Category.FOOD, categorySource = CategorySource.USER),
            Expense(id = 3, amountMinor = 200_00L, merchant = "Canteen", category = Category.FOOD, categorySource = CategorySource.USER),
            Expense(id = 4, amountMinor = 160_00L, merchant = "Smokie", category = Category.FOOD, categorySource = CategorySource.USER),
            Expense(id = 5, amountMinor = 170_00L, merchant = "Chapati", category = Category.FOOD, categorySource = CategorySource.USER),
            // Unusually huge outlier (e.g. KES 5,000 fancy restaurant dinner)
            Expense(id = 6, amountMinor = 5_000_00L, merchant = "Fine Dining Luxury", category = Category.FOOD, categorySource = CategorySource.USER)
        )

        val anomalies = SpendingInsights.flagAnomalies(normalExpenses, zThreshold = 2.0, minSamples = 5)

        assertEquals(1, anomalies.size)
        assertEquals(6L, anomalies[0].expense.id)
        assertTrue(anomalies[0].message.contains("Fine Dining Luxury"))
    }

    @Test
    fun testAnomalyDetectionSkipsWhenTooFewSamples() {
        val fewExpenses = listOf(
            Expense(id = 1, amountMinor = 150_00L, merchant = "Mama Mboga", category = Category.FOOD, categorySource = CategorySource.USER),
            Expense(id = 2, amountMinor = 5_000_00L, merchant = "Fine Dining", category = Category.FOOD, categorySource = CategorySource.USER)
        )
        // With fewer than minSamples = 5, should return empty list
        val anomalies = SpendingInsights.flagAnomalies(fewExpenses, minSamples = 5)
        assertTrue(anomalies.isEmpty())
    }
}

package com.example.expensetracker

import com.example.expensetracker.ai.SpendingInsights
import com.example.expensetracker.data.Category
import com.example.expensetracker.data.CategorySource
import com.example.expensetracker.data.Expense
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionDeduplicationTest {

    @Test
    fun testDetectsDuplicateTransactionsWithinTimeWindow() {
        val now = System.currentTimeMillis()
        val expense1 = Expense(
            id = 1,
            amountMinor = 200_00L,
            merchant = "Java House",
            category = Category.FOOD,
            categorySource = CategorySource.MODEL,
            timestamp = now
        )
        val expense2 = Expense(
            id = 2,
            amountMinor = 200_00L,
            merchant = "Java House",
            category = Category.FOOD,
            categorySource = CategorySource.MODEL,
            timestamp = now + 60_000 // 1 minute later
        )

        val anomalies = SpendingInsights.flagAnomalies(listOf(expense1, expense2))
        val duplicateAnomalies = anomalies.filter { it.message.contains("duplicate", ignoreCase = true) }

        assertEquals(1, duplicateAnomalies.size)
        assertTrue(duplicateAnomalies[0].message.contains("Java House"))
    }
}

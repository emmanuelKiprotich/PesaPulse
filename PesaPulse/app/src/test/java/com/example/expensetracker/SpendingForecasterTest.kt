package com.example.expensetracker

import com.example.expensetracker.ai.BurnTrend
import com.example.expensetracker.ai.SpendingForecaster
import com.example.expensetracker.data.Category
import com.example.expensetracker.data.CategorySource
import com.example.expensetracker.data.Expense
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class SpendingForecasterTest {

    @Test
    fun testRunwayForecastingCalculatesDepletionDate() {
        val now = System.currentTimeMillis()
        val oneDayMs = TimeUnit.DAYS.toMillis(1)

        val expenses = listOf(
            Expense(id = 1, amountMinor = 500_00L, merchant = "Naivas", category = Category.FOOD, categorySource = CategorySource.MODEL, timestamp = now - 3 * oneDayMs),
            Expense(id = 2, amountMinor = 400_00L, merchant = "Super Metro", category = Category.TRANSPORT, categorySource = CategorySource.MODEL, timestamp = now - 2 * oneDayMs),
            Expense(id = 3, amountMinor = 600_00L, merchant = "KPLC", category = Category.RENT_UTILITIES, categorySource = CategorySource.MODEL, timestamp = now - 1 * oneDayMs),
            Expense(id = 4, amountMinor = 500_00L, merchant = "Java", category = Category.FOOD, categorySource = CategorySource.MODEL, timestamp = now)
        )

        // Available balance: KES 10,000 (1,000,000 minor) with 30 days remaining
        val forecast = SpendingForecaster.forecastRunway(expenses, availableBalanceMinor = 10_000_00L, cycleDaysRemaining = 30)

        assertTrue(forecast.dailyBurnRateMinor > 0L)
        assertTrue(forecast.daysOfRunwayRemaining > 0)
        assertNotNull(forecast.predictedDepletionDate)
        assertTrue(forecast.rSquared in 0.0..1.0)
    }

    @Test
    fun testAcceleratedBurnRateTriggersFastBurnWarning() {
        val now = System.currentTimeMillis()
        val oneDayMs = TimeUnit.DAYS.toMillis(1)

        // Heavy spending: KES 2,000/day
        val expenses = listOf(
            Expense(id = 1, amountMinor = 2_000_00L, merchant = "Mall", category = Category.SHOPPING, categorySource = CategorySource.MODEL, timestamp = now - 2 * oneDayMs),
            Expense(id = 2, amountMinor = 2_000_00L, merchant = "Club", category = Category.ENTERTAINMENT, categorySource = CategorySource.MODEL, timestamp = now - 1 * oneDayMs),
            Expense(id = 3, amountMinor = 2_000_00L, merchant = "Electronics", category = Category.SHOPPING, categorySource = CategorySource.MODEL, timestamp = now)
        )

        // Only KES 6,000 balance left with 20 days remaining (runway ~ 3 days vs 20 needed)
        val forecast = SpendingForecaster.forecastRunway(expenses, availableBalanceMinor = 6_000_00L, cycleDaysRemaining = 20)

        assertEquals(BurnTrend.ACCELERATING, forecast.trend)
        assertTrue(forecast.daysOfRunwayRemaining < 20)
        assertTrue(forecast.message.contains("High burn velocity"))
    }
}

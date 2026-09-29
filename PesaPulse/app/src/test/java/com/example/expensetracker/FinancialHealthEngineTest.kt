package com.example.expensetracker

import com.example.expensetracker.ai.FinancialHealthEngine
import com.example.expensetracker.data.Category
import com.example.expensetracker.data.CategorySource
import com.example.expensetracker.data.ChamaGoal
import com.example.expensetracker.data.Expense
import com.example.expensetracker.data.MobileLoan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FinancialHealthEngineTest {

    @Test
    fun testHealthyFinancesYieldHighGrade() {
        val expenses = listOf(
            Expense(id = 1, amountMinor = 400_00L, merchant = "Naivas", category = Category.FOOD, categorySource = CategorySource.MODEL),
            Expense(id = 2, amountMinor = 100_00L, merchant = "Boda", category = Category.TRANSPORT, categorySource = CategorySource.MODEL)
        )
        val chamas = listOf(
            ChamaGoal(id = 1, title = "Rent Chama", targetAmountMinor = 15_000_00L, currentSavedMinor = 8_000_00L, deadline = "Dec")
        )
        val mobileLoans = emptyList<MobileLoan>()

        val report = FinancialHealthEngine.computeHealthScore(
            expenses = expenses,
            chamaGoals = chamas,
            mobileLoans = mobileLoans,
            dailyBurnMinor = 400_00L,
            safeDailyLimitMinor = 600_00L,
            anomaliesCount = 0
        )

        assertTrue("Expected score >= 80, got ${report.totalScore}", report.totalScore >= 80)
        assertTrue(report.grade in listOf("A", "A+"))
        assertEquals(5, report.pillars.size)
    }

    @Test
    fun testUnpaidMobileLoansPenalizeScore() {
        val expenses = listOf(
            Expense(id = 1, amountMinor = 3_000_00L, merchant = "Club", category = Category.ENTERTAINMENT, categorySource = CategorySource.MODEL)
        )
        val chamas = emptyList<ChamaGoal>()
        // Heavy unpaid Fuliza loan
        val mobileLoans = listOf(
            MobileLoan(id = 1, provider = "Fuliza", principalMinor = 8_000_00L, dailyInterestRatePercent = 0.5, isRepaid = false)
        )

        val report = FinancialHealthEngine.computeHealthScore(
            expenses = expenses,
            chamaGoals = chamas,
            mobileLoans = mobileLoans,
            dailyBurnMinor = 1_000_00L,
            safeDailyLimitMinor = 300_00L,
            anomaliesCount = 2
        )

        assertTrue("Expected low score due to loans & burn, got ${report.totalScore}", report.totalScore < 55)
        assertTrue(report.dynamicAiRecommendation.contains("mobile loans") || report.dynamicAiRecommendation.contains("Slow Down") || report.dynamicAiRecommendation.contains("Alert"))
    }
}

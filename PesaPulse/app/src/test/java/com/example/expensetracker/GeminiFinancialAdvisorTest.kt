package com.example.expensetracker

import com.example.expensetracker.ai.FinancialContext
import com.example.expensetracker.ai.GeminiFinancialAdvisor
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class GeminiFinancialAdvisorTest {

    @Test
    fun testOfflineAdviceGenerationWithoutApiKey() = runTest {
        val context = FinancialContext(
            totalSpentMinor = 5_000_00L,
            totalIncomeMinor = 15_000_00L,
            netBalanceMinor = 10_000_00L,
            dailyBurnMinor = 500_00L,
            safeDailyLimitMinor = 400_00L,
            daysOfRunwayRemaining = 10,
            unpaidLoans = listOf("Fuliza" to 2_000_00L)
        )

        val advice = GeminiFinancialAdvisor.getFinancialAdvice(apiKey = null, context = context, query = "How do I clear my loan?")

        assertTrue(advice.contains("Fuliza"))
        assertTrue(advice.contains("How do I clear my loan?"))
        assertTrue(advice.contains("Runway Alert"))
    }

    @Test
    fun testOfflineAuditGenerationWithoutQuery() = runTest {
        val context = FinancialContext(
            totalSpentMinor = 2_000_00L,
            totalIncomeMinor = 20_000_00L,
            netBalanceMinor = 18_000_00L,
            daysOfRunwayRemaining = 30
        )

        val advice = GeminiFinancialAdvisor.getFinancialAdvice(apiKey = "", context = context, query = null)

        assertTrue(advice.contains("Financial Audit"))
        assertTrue(advice.contains("Runway Health"))
    }
}

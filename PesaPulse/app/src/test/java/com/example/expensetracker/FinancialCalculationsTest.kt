package com.example.expensetracker

import com.example.expensetracker.data.MobileLoan
import com.example.expensetracker.ui.BreathingRoomState
import com.example.expensetracker.ui.SemesterBudgetState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FinancialCalculationsTest {

    @Test
    fun testBreathingRoomCalculation() {
        val state = BreathingRoomState(
            startingBalanceMinor = 15_000_00L,
            totalIncomeMinor = 5_000_00L,
            totalSpentMinor = 8_000_00L,
            totalActiveLoansMinor = 2_000_00L
        )

        // Effective budget: 15,000 + 5,000 = 20,000
        assertEquals(20_000_00L, state.effectiveBudgetMinor)
        // Obligations: 8,000 + 2,000 = 10,000
        assertEquals(10_000_00L, state.totalObligationsMinor)
        // Remaining breathing room: 20,000 - 10,000 = 10,000
        assertEquals(10_000_00L, state.breathingRoomMinor)
        // Daily: 10,000 / 30 = 333.33 -> 333_33L
        assertEquals(333_33L, state.dailyBreathingRoomMinor)
        assertFalse(state.isCritical)
    }

    @Test
    fun testBreathingRoomOverdrawn() {
        val state = BreathingRoomState(
            startingBalanceMinor = 10_000_00L,
            totalIncomeMinor = 0L,
            totalSpentMinor = 12_000_00L,
            totalActiveLoansMinor = 3_000_00L
        )
        // Breathing room clamped to 0
        assertEquals(0L, state.breathingRoomMinor)
        assertTrue(state.isCritical)
    }

    @Test
    fun testSemesterBudgetPacing() {
        // 120 days semester, 30 days elapsed -> 90 days remaining
        // 30,000 total disbursement, 6,000 spent so far -> 24,000 remaining
        val state = SemesterBudgetState(
            totalDisbursementMinor = 30_000_00L,
            totalSpentMinor = 6_000_00L,
            totalDays = 120,
            daysElapsed = 30
        )

        assertEquals(24_000_00L, state.remainingMinor)
        assertEquals(90, state.daysRemaining)
        // Recommended daily budget: 24,000 / 90 = 266.66 -> 266_66L
        assertEquals(266_66L, state.dailyBudgetRecommendedMinor)
        // Current burn rate: 6,000 / 30 = 200.00 -> 200_00L
        assertEquals(200_00L, state.currentDailyBurnRateMinor)
        // 200/day <= 266/day -> Pacing well!
        assertTrue(state.isPacingWell)
    }

    @Test
    fun testSemesterBudgetOverpacing() {
        // Spent 15,000 in first 15 days of 120 days
        val state = SemesterBudgetState(
            totalDisbursementMinor = 30_000_00L,
            totalSpentMinor = 15_000_00L,
            totalDays = 120,
            daysElapsed = 15
        )
        // Burn rate: 15,000 / 15 = 1,000/day
        assertEquals(1_000_00L, state.currentDailyBurnRateMinor)
        // Recommended daily: 15,000 / 105 = 142.85 -> 142_85L
        assertEquals(142_85L, state.dailyBudgetRecommendedMinor)
        // 1000 > 142 -> Not pacing well!
        assertFalse(state.isPacingWell)
    }

    @Test
    fun testMobileLoanInterestAccrual() {
        val tenDaysAgo = System.currentTimeMillis() - (10L * 24 * 60 * 60 * 1000L)
        val loan = MobileLoan(
            provider = "Fuliza",
            principalMinor = 1_000_00L, // KES 1,000
            dailyInterestRatePercent = 0.5, // 0.5% per day
            dateTakenMillis = tenDaysAgo,
            isRepaid = false
        )

        assertEquals(10L, loan.daysActive)
        // Interest: 1000 * 0.005 * 10 = KES 50 -> 50_00L
        assertEquals(50_00L, loan.totalInterestMinor)
        assertEquals(1_050_00L, loan.totalDueMinor)
    }
}

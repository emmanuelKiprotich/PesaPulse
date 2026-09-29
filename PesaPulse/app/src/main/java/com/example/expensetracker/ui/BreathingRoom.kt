package com.example.expensetracker.ui

data class BreathingRoomState(
    val startingBalanceMinor: Long = 15_000_00L, // baseline monthly allowance / upkeep
    val totalIncomeMinor: Long = 0L,              // total incoming funds / deposits
    val totalSpentMinor: Long = 0L,               // total expenses spent
    val totalActiveLoansMinor: Long = 0L          // total active unpaid mobile loans + interest
) {
    val effectiveBudgetMinor: Long get() = startingBalanceMinor + totalIncomeMinor
    val totalObligationsMinor: Long get() = totalSpentMinor + totalActiveLoansMinor
    val breathingRoomMinor: Long get() = (effectiveBudgetMinor - totalObligationsMinor).coerceAtLeast(0L)
    val dailyBreathingRoomMinor: Long get() = breathingRoomMinor / 30L
    val isCritical: Boolean get() = totalObligationsMinor > effectiveBudgetMinor
    val spentPercentage: Float get() = if (effectiveBudgetMinor > 0) (totalObligationsMinor.toFloat() / effectiveBudgetMinor).coerceIn(0f, 1f) else 1f
}

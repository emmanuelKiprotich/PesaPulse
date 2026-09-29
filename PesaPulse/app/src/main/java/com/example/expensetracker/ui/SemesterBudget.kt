package com.example.expensetracker.ui

data class SemesterBudgetState(
    val totalDisbursementMinor: Long = 30_000_00L, // KES 30,000 HELB/HEF semester loan
    val totalSpentMinor: Long = 0L,
    val totalDays: Int = 120, // 4-month semester
    val daysElapsed: Int = 30
) {
    val remainingMinor: Long get() = (totalDisbursementMinor - totalSpentMinor).coerceAtLeast(0L)
    val daysRemaining: Int get() = (totalDays - daysElapsed).coerceAtLeast(1)
    val dailyBudgetRecommendedMinor: Long get() = remainingMinor / daysRemaining
    val currentDailyBurnRateMinor: Long get() = if (daysElapsed > 0) totalSpentMinor / daysElapsed else 0L
    val isPacingWell: Boolean get() = currentDailyBurnRateMinor <= dailyBudgetRecommendedMinor
    val spentPercentage: Float get() = if (totalDisbursementMinor > 0) (totalSpentMinor.toFloat() / totalDisbursementMinor).coerceIn(0f, 1f) else 0f
    val timePercentage: Float get() = (daysElapsed.toFloat() / totalDays).coerceIn(0f, 1f)
}

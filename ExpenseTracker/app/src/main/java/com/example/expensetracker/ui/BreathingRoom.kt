package com.example.expensetracker.ui

data class BreathingRoomState(
    val startingBalanceMinor: Long = 15_000_00L, // KES 15,000 baseline monthly allowance / HELB
    val totalSpentMinor: Long = 0L,
    val totalActiveLoansMinor: Long = 0L
) {
    val breathingRoomMinor: Long get() = (startingBalanceMinor - totalSpentMinor - totalActiveLoansMinor).coerceAtLeast(0L)
    val dailyBreathingRoomMinor: Long get() = breathingRoomMinor / 30L
}

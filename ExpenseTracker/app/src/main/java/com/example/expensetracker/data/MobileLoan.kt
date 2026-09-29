package com.example.expensetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mobile_loans")
data class MobileLoan(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val provider: String, // e.g. "Fuliza", "M-Shwari", "Hustler Fund"
    val principalMinor: Long,
    val dailyInterestRatePercent: Double, // e.g. 0.5% per day for Fuliza
    val dateTakenMillis: Long = System.currentTimeMillis(),
    val isRepaid: Boolean = false
) {
    val daysActive: Long get() = ((System.currentTimeMillis() - dateTakenMillis) / (1000 * 60 * 60 * 24)).coerceAtLeast(0L)
    val totalInterestMinor: Long get() = (principalMinor * (dailyInterestRatePercent / 100.0) * daysActive).toLong()
    val totalDueMinor: Long get() = principalMinor + totalInterestMinor
}

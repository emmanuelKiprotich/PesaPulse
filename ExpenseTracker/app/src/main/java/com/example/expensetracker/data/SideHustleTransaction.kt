package com.example.expensetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "side_hustle_transactions")
data class SideHustleTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val businessName: String, // e.g. "Hostel Wi-Fi", "Freelance Writing", "Printing & Photocopy"
    val isIncome: Boolean, // true for revenue, false for business expenses
    val amountMinor: Long,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)

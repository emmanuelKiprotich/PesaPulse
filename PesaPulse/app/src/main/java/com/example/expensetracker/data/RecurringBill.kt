package com.example.expensetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recurring_bills")
data class RecurringBill(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String, // e.g. "Hostel Rent", "KPLC Token", "Wi-Fi Monthly"
    val amountMinor: Long,
    val dueDayOfMonth: Int, // 1 to 31
    val category: Category,
    val isActive: Boolean = true
)

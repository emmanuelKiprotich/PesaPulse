package com.example.expensetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chama_goals")
data class ChamaGoal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val targetAmountMinor: Long,
    val currentSavedMinor: Long = 0L,
    val deadline: String = "End of Semester"
)

package com.example.expensetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "peer_debts")
data class PeerDebt(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val peerName: String,
    val amountMinor: Long, // KES cents
    val description: String,
    val isOwedToMe: Boolean, // true if they owe me, false if I owe them
    val isSettled: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

package com.example.expensetracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PeerDebtDao {
    @Query("SELECT * FROM peer_debts ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<PeerDebt>>

    @Insert
    suspend fun insert(debt: PeerDebt)

    @Update
    suspend fun update(debt: PeerDebt)

    @Delete
    suspend fun delete(debt: PeerDebt)
}

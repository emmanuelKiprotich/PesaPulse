package com.example.expensetracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SideHustleDao {
    @Query("SELECT * FROM side_hustle_transactions ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<SideHustleTransaction>>

    @Insert
    suspend fun insert(tx: SideHustleTransaction)

    @Update
    suspend fun update(tx: SideHustleTransaction)

    @Delete
    suspend fun delete(tx: SideHustleTransaction)
}

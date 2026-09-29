package com.example.expensetracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MobileLoanDao {
    @Query("SELECT * FROM mobile_loans ORDER BY dateTakenMillis DESC")
    fun observeAll(): Flow<List<MobileLoan>>

    @Insert
    suspend fun insert(loan: MobileLoan)

    @Update
    suspend fun update(loan: MobileLoan)

    @Delete
    suspend fun delete(loan: MobileLoan)
}

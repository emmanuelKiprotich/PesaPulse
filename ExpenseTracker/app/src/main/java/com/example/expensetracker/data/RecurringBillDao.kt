package com.example.expensetracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringBillDao {
    @Query("SELECT * FROM recurring_bills")
    fun observeAll(): Flow<List<RecurringBill>>

    @Insert
    suspend fun insert(bill: RecurringBill)

    @Update
    suspend fun update(bill: RecurringBill)

    @Delete
    suspend fun delete(bill: RecurringBill)
}

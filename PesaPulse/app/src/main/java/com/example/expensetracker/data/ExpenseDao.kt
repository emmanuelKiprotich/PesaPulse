package com.example.expensetracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<Expense>>

    @Query("SELECT category AS category, SUM(amountMinor) AS total FROM expenses WHERE isIncome = 0 GROUP BY category")
    fun observeTotalsByCategory(): Flow<List<CategoryTotal>>

    @Query("SELECT * FROM expenses WHERE transactionCode IS NOT NULL AND transactionCode = :code LIMIT 1")
    suspend fun getByTransactionCode(code: String): Expense?

    @Query("SELECT * FROM expenses WHERE merchant = :merchant AND amountMinor = :amountMinor AND isIncome = :isIncome AND abs(timestamp - :timestamp) <= :windowMs LIMIT 1")
    suspend fun findDuplicateCandidate(
        merchant: String,
        amountMinor: Long,
        isIncome: Boolean,
        timestamp: Long,
        windowMs: Long = 180_000L
    ): Expense?

    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    suspend fun getAllExpenses(): List<Expense>

    @Query("SELECT SUM(amountMinor) FROM expenses WHERE isIncome = 0")
    fun observeTotalSpent(): Flow<Long?>

    @Query("SELECT SUM(amountMinor) FROM expenses WHERE isIncome = 1")
    fun observeTotalIncome(): Flow<Long?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(expense: Expense): Long

    @Update
    suspend fun update(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)

    @Query("DELETE FROM expenses WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>): Int

    @Query("DELETE FROM expenses")
    suspend fun deleteAll()
}

package com.example.expensetracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChamaGoalDao {
    @Query("SELECT * FROM chama_goals")
    fun observeAll(): Flow<List<ChamaGoal>>

    @Insert
    suspend fun insert(goal: ChamaGoal)

    @Update
    suspend fun update(goal: ChamaGoal)

    @Delete
    suspend fun delete(goal: ChamaGoal)
}

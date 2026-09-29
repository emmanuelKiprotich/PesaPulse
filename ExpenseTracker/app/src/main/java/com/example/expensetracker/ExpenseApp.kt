package com.example.expensetracker

import android.app.Application
import androidx.room.Room
import com.example.expensetracker.ai.ExpenseCategorizer
import com.example.expensetracker.ai.HybridCategorizer
import com.example.expensetracker.ai.RuleBasedCategorizer
import com.example.expensetracker.ai.TfliteCategorizer
import com.example.expensetracker.data.AppDatabase
import com.example.expensetracker.data.ExpenseRepository

/** Simple manual dependency container (swap for Hilt/Koin later if you want). */
class AppContainer(app: Application) {
    private val db = Room.databaseBuilder(app, AppDatabase::class.java, "expenses.db").build()
    val repository = ExpenseRepository(db.expenseDao())
    val categorizer: ExpenseCategorizer = HybridCategorizer(
        primary = TfliteCategorizer(app),
        fallback = RuleBasedCategorizer(),
        confidenceThreshold = 0.6f
    )
}

class ExpenseApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

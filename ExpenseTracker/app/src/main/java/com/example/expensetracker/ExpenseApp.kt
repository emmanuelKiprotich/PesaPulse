package com.example.expensetracker

import android.app.Application
import androidx.room.Room
import com.example.expensetracker.ai.ExpenseCategorizer
import com.example.expensetracker.ai.HybridCategorizer
import com.example.expensetracker.ai.RuleBasedCategorizer
import com.example.expensetracker.ai.TfliteCategorizer
import com.example.expensetracker.data.AppDatabase
import com.example.expensetracker.data.ExpenseRepository
import com.example.expensetracker.worker.SmsSyncWorker

/** Simple manual dependency container (swap for Hilt/Koin later if you want). */
class AppContainer(app: Application) {
    private val db = Room.databaseBuilder(app, AppDatabase::class.java, "expenses.db")
        .fallbackToDestructiveMigration(true)
        .build()
    val repository = ExpenseRepository(
        expenseDao = db.expenseDao(),
        peerDebtDao = db.peerDebtDao(),
        chamaGoalDao = db.chamaGoalDao(),
        mobileLoanDao = db.mobileLoanDao(),
        sideHustleDao = db.sideHustleDao(),
        recurringBillDao = db.recurringBillDao()
    )
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
        try {
            SmsSyncWorker.schedule(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

package com.example.expensetracker

import android.app.Application
import androidx.room.Room
import com.example.expensetracker.ai.ExpenseCategorizer
import com.example.expensetracker.ai.HybridCategorizer
import com.example.expensetracker.ai.NaiveBayesCategorizer
import com.example.expensetracker.ai.RuleBasedCategorizer
import com.example.expensetracker.data.AppDatabase
import com.example.expensetracker.data.ExpenseRepository
import com.example.expensetracker.worker.SmsSyncWorker

/** App dependency container with on-device AI ML models. */
class AppContainer(app: Application) {
    private val db = Room.databaseBuilder(app, AppDatabase::class.java, "expenses.db")
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()

    val repository = ExpenseRepository(
        expenseDao = db.expenseDao(),
        peerDebtDao = db.peerDebtDao(),
        chamaGoalDao = db.chamaGoalDao(),
        mobileLoanDao = db.mobileLoanDao(),
        sideHustleDao = db.sideHustleDao(),
        recurringBillDao = db.recurringBillDao(),
    )

    // On-Device NLP Machine Learning Classifier with online adaptation
    val mlCategorizer = NaiveBayesCategorizer(app)

    val categorizer: ExpenseCategorizer = HybridCategorizer(
        primary = mlCategorizer,
        fallback = RuleBasedCategorizer(),
        confidenceThreshold = 0.50f
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

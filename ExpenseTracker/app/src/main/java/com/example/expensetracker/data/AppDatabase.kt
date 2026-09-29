package com.example.expensetracker.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        Expense::class,
        PeerDebt::class,
        ChamaGoal::class,
        MobileLoan::class,
        SideHustleTransaction::class,
        RecurringBill::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun peerDebtDao(): PeerDebtDao
    abstract fun chamaGoalDao(): ChamaGoalDao
    abstract fun mobileLoanDao(): MobileLoanDao
    abstract fun sideHustleDao(): SideHustleDao
    abstract fun recurringBillDao(): RecurringBillDao
}

package com.example.expensetracker.data

import kotlinx.coroutines.flow.Flow

class ExpenseRepository(
    private val expenseDao: ExpenseDao,
    private val peerDebtDao: PeerDebtDao,
    private val chamaGoalDao: ChamaGoalDao,
    private val mobileLoanDao: MobileLoanDao,
    private val sideHustleDao: SideHustleDao,
    private val recurringBillDao: RecurringBillDao
) {
    fun observeAll(): Flow<List<Expense>> = expenseDao.observeAll()
    fun observeTotalsByCategory(): Flow<List<CategoryTotal>> = expenseDao.observeTotalsByCategory()
    suspend fun add(expense: Expense) = expenseDao.insert(expense)
    suspend fun delete(expense: Expense) = expenseDao.delete(expense)

    fun observePeerDebts(): Flow<List<PeerDebt>> = peerDebtDao.observeAll()
    suspend fun addPeerDebt(debt: PeerDebt) = peerDebtDao.insert(debt)
    suspend fun updatePeerDebt(debt: PeerDebt) = peerDebtDao.update(debt)
    suspend fun deletePeerDebt(debt: PeerDebt) = peerDebtDao.delete(debt)

    fun observeChamaGoals(): Flow<List<ChamaGoal>> = chamaGoalDao.observeAll()
    suspend fun addChamaGoal(goal: ChamaGoal) = chamaGoalDao.insert(goal)
    suspend fun updateChamaGoal(goal: ChamaGoal) = chamaGoalDao.update(goal)
    suspend fun deleteChamaGoal(goal: ChamaGoal) = chamaGoalDao.delete(goal)

    fun observeMobileLoans(): Flow<List<MobileLoan>> = mobileLoanDao.observeAll()
    suspend fun addMobileLoan(loan: MobileLoan) = mobileLoanDao.insert(loan)
    suspend fun updateMobileLoan(loan: MobileLoan) = mobileLoanDao.update(loan)
    suspend fun deleteMobileLoan(loan: MobileLoan) = mobileLoanDao.delete(loan)

    fun observeSideHustles(): Flow<List<SideHustleTransaction>> = sideHustleDao.observeAll()
    suspend fun addSideHustle(tx: SideHustleTransaction) = sideHustleDao.insert(tx)
    suspend fun updateSideHustle(tx: SideHustleTransaction) = sideHustleDao.update(tx)
    suspend fun deleteSideHustle(tx: SideHustleTransaction) = sideHustleDao.delete(tx)

    fun observeRecurringBills(): Flow<List<RecurringBill>> = recurringBillDao.observeAll()
    suspend fun addRecurringBill(bill: RecurringBill) = recurringBillDao.insert(bill)
    suspend fun updateRecurringBill(bill: RecurringBill) = recurringBillDao.update(bill)
    suspend fun deleteRecurringBill(bill: RecurringBill) = recurringBillDao.delete(bill)
}

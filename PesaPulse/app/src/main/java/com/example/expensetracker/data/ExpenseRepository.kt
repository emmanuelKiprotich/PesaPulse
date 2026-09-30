package com.example.expensetracker.data

import kotlinx.coroutines.flow.Flow

class ExpenseRepository(
    private val expenseDao: ExpenseDao,
    private val peerDebtDao: PeerDebtDao,
    private val chamaGoalDao: ChamaGoalDao,
    private val mobileLoanDao: MobileLoanDao,
    private val sideHustleDao: SideHustleDao,
    private val recurringBillDao: RecurringBillDao,
) {
    fun observeAll(): Flow<List<Expense>> = expenseDao.observeAll()
    fun observeTotalsByCategory(): Flow<List<CategoryTotal>> = expenseDao.observeTotalsByCategory()
    fun observeTotalSpent(): Flow<Long?> = expenseDao.observeTotalSpent()
    fun observeTotalIncome(): Flow<Long?> = expenseDao.observeTotalIncome()
    suspend fun getByTransactionCode(code: String): Expense? = expenseDao.getByTransactionCode(code)
    suspend fun getAllExpenses(): List<Expense> = expenseDao.getAllExpenses()

    /**
     * Inserts an expense while preventing duplicates:
     * 1. Checks if the unique transactionCode already exists in the database.
     * 2. Checks for near-duplicates (same merchant, same amount, same direction within 3 minutes).
     * 3. Merges/promotes carrier transaction codes if an incoming SMS matches an earlier push notification.
     * 4. Enforces Room unique index on conflict IGNORE.
     * Returns true if successfully inserted, false if skipped as duplicate.
     */
    suspend fun add(expense: Expense): Boolean {
        val cleanCode = expense.transactionCode?.trim()?.ifEmpty { null }
        val sanitized = expense.copy(transactionCode = cleanCode)

        // 1. Transaction Code check
        if (cleanCode != null) {
            val existingByCode = expenseDao.getByTransactionCode(cleanCode)
            if (existingByCode != null) {
                return false
            }
        }

        // 2. Velocity / near-duplicate check (within 3 minutes)
        val candidate = expenseDao.findDuplicateCandidate(
            merchant = sanitized.merchant,
            amountMinor = sanitized.amountMinor,
            isIncome = sanitized.isIncome,
            timestamp = sanitized.timestamp,
            windowMs = 180_000L
        )

        if (candidate != null) {
            // If existing record was a push notification without official M-Pesa code, and new one has official code, update it
            val isCandidateGeneric = candidate.transactionCode.isNullOrEmpty() ||
                    candidate.transactionCode.startsWith("NOTIF-") ||
                    candidate.transactionCode.startsWith("LIVE-SMS-")
            val isNewOfficial = cleanCode != null && !cleanCode.startsWith("NOTIF-") && !cleanCode.startsWith("LIVE-SMS-")

            if (isCandidateGeneric && isNewOfficial) {
                expenseDao.update(candidate.copy(transactionCode = cleanCode))
            }
            return false
        }

        val rowId = expenseDao.insert(sanitized)
        return rowId != -1L
    }

    suspend fun update(expense: Expense) = expenseDao.update(expense)
    suspend fun delete(expense: Expense) = expenseDao.delete(expense)
    suspend fun clearAllExpenses() = expenseDao.deleteAll()

    /**
     * Scans the database and purges all duplicate transactions:
     * - Duplicate transaction codes
     * - Identical merchant & amount within 5 minutes
     * Returns the count of removed duplicates.
     */
    suspend fun deduplicateExpenses(): Int {
        val all = expenseDao.getAllExpenses().sortedBy { it.timestamp }
        val toDelete = mutableListOf<Long>()
        val seenCodes = mutableSetOf<String>()
        val kept = mutableListOf<Expense>()

        for (e in all) {
            val code = e.transactionCode?.trim()?.ifEmpty { null }
            if (code != null) {
                if (seenCodes.contains(code)) {
                    toDelete.add(e.id)
                    continue
                }
                seenCodes.add(code)
            }

            val isDuplicate = kept.any { k ->
                k.amountMinor == e.amountMinor &&
                k.isIncome == e.isIncome &&
                kotlin.math.abs(k.timestamp - e.timestamp) <= 300_000L &&
                (k.merchant.equals(e.merchant, ignoreCase = true) ||
                 k.merchant.contains(e.merchant, ignoreCase = true) ||
                 e.merchant.contains(k.merchant, ignoreCase = true))
            }

            if (isDuplicate) {
                toDelete.add(e.id)
            } else {
                kept.add(e)
            }
        }

        if (toDelete.isNotEmpty()) {
            expenseDao.deleteByIds(toDelete)
        }
        return toDelete.size
    }

    suspend fun countPotentialDuplicates(): Int {
        val all = expenseDao.getAllExpenses().sortedBy { it.timestamp }
        var count = 0
        val seenCodes = mutableSetOf<String>()
        val kept = mutableListOf<Expense>()

        for (e in all) {
            val code = e.transactionCode?.trim()?.ifEmpty { null }
            if (code != null) {
                if (seenCodes.contains(code)) {
                    count++
                    continue
                }
                seenCodes.add(code)
            }

            val isDuplicate = kept.any { k ->
                k.amountMinor == e.amountMinor &&
                k.isIncome == e.isIncome &&
                kotlin.math.abs(k.timestamp - e.timestamp) <= 300_000L &&
                (k.merchant.equals(e.merchant, ignoreCase = true) ||
                 k.merchant.contains(e.merchant, ignoreCase = true) ||
                 e.merchant.contains(k.merchant, ignoreCase = true))
            }

            if (isDuplicate) {
                count++
            } else {
                kept.add(e)
            }
        }
        return count
    }

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

package com.example.expensetracker

import com.example.expensetracker.data.Category
import com.example.expensetracker.data.CategorySource
import com.example.expensetracker.data.CategoryTotal
import com.example.expensetracker.data.ChamaGoal
import com.example.expensetracker.data.ChamaGoalDao
import com.example.expensetracker.data.Expense
import com.example.expensetracker.data.ExpenseDao
import com.example.expensetracker.data.ExpenseRepository
import com.example.expensetracker.data.MobileLoan
import com.example.expensetracker.data.MobileLoanDao
import com.example.expensetracker.data.PeerDebt
import com.example.expensetracker.data.PeerDebtDao
import com.example.expensetracker.data.RecurringBill
import com.example.expensetracker.data.RecurringBillDao
import com.example.expensetracker.data.SideHustleDao
import com.example.expensetracker.data.SideHustleTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.math.abs

class TransactionDeduplicationTest {

    private lateinit var fakeDao: FakeExpenseDao
    private lateinit var repository: ExpenseRepository

    @Before
    fun setUp() {
        fakeDao = FakeExpenseDao()
        repository = ExpenseRepository(
            expenseDao = fakeDao,
            peerDebtDao = FakePeerDebtDao(),
            chamaGoalDao = FakeChamaGoalDao(),
            mobileLoanDao = FakeMobileLoanDao(),
            sideHustleDao = FakeSideHustleDao(),
            recurringBillDao = FakeRecurringBillDao(),
        )
    }

    @Test
    fun testDuplicateTransactionCodeIsRejected() = runBlocking {
        val expense1 = Expense(
            id = 1L,
            amountMinor = 500_00L,
            merchant = "[M-Pesa] Naivas Supermarket",
            category = Category.FOOD,
            categorySource = CategorySource.RULES,
            transactionCode = "QG789XYZ",
        )
        fakeDao.insert(expense1)

        val duplicate = Expense(
            amountMinor = 500_00L,
            merchant = "[M-Pesa] Naivas Supermarket",
            category = Category.FOOD,
            categorySource = CategorySource.RULES,
            transactionCode = "QG789XYZ",
        )

        val added = repository.add(duplicate)
        assertFalse(added)
    }

    @Test
    fun testNearDuplicateWithinWindowIsRejected() = runBlocking {
        val now = System.currentTimeMillis()
        val expense1 = Expense(
            id = 1L,
            amountMinor = 200_00L,
            merchant = "[M-Pesa] Boda Stage",
            category = Category.TRANSPORT,
            categorySource = CategorySource.RULES,
            timestamp = now - 60_000L,
            transactionCode = "QG123456",
        )
        fakeDao.insert(expense1)

        val nearDuplicate = Expense(
            amountMinor = 200_00L,
            merchant = "[M-Pesa] Boda Stage",
            category = Category.TRANSPORT,
            categorySource = CategorySource.RULES,
            timestamp = now,
            transactionCode = "QG123457",
        )

        val added = repository.add(nearDuplicate)
        assertFalse(added)
    }

    @Test
    fun testUniqueTransactionIsSuccessfullyInserted() = runBlocking {
        val now = System.currentTimeMillis()
        val newExpense = Expense(
            amountMinor = 450_00L,
            merchant = "[M-Pesa] Java House",
            category = Category.FOOD,
            categorySource = CategorySource.RULES,
            timestamp = now,
            transactionCode = "QG999888",
        )

        val added = repository.add(newExpense)
        assertTrue(added)
    }
}

private class FakeExpenseDao : ExpenseDao {
    private val expenses = mutableListOf<Expense>()
    private var idCounter = 1L

    override fun observeAll(): Flow<List<Expense>> = flowOf(expenses)
    override fun observeTotalsByCategory(): Flow<List<CategoryTotal>> = flowOf(emptyList())

    override suspend fun getByTransactionCode(code: String): Expense? {
        return expenses.firstOrNull { it.transactionCode == code }
    }

    override suspend fun findDuplicateCandidate(
        merchant: String,
        amountMinor: Long,
        isIncome: Boolean,
        timestamp: Long,
        windowMs: Long
    ): Expense? {
        return expenses.firstOrNull { e ->
            e.merchant == merchant &&
                    e.amountMinor == amountMinor &&
                    e.isIncome == isIncome &&
                    abs(e.timestamp - timestamp) <= windowMs
        }
    }

    override suspend fun getAllExpenses(): List<Expense> = expenses.toList()
    override fun observeTotalSpent(): Flow<Long?> = flowOf(0L)
    override fun observeTotalIncome(): Flow<Long?> = flowOf(0L)

    override suspend fun insert(expense: Expense): Long {
        val assigned = expense.copy(id = idCounter++)
        expenses.add(assigned)
        return assigned.id
    }

    override suspend fun update(expense: Expense) {
        val index = expenses.indexOfFirst { it.id == expense.id }
        if (index != -1) {
            expenses[index] = expense
        }
    }

    override suspend fun delete(expense: Expense) {
        expenses.removeIf { it.id == expense.id }
    }

    override suspend fun deleteByIds(ids: List<Long>): Int {
        val initialSize = expenses.size
        expenses.removeIf { it.id in ids }
        return initialSize - expenses.size
    }

    override suspend fun deleteAll() {
        expenses.clear()
    }
}

private class FakePeerDebtDao : PeerDebtDao {
    override fun observeAll(): Flow<List<PeerDebt>> = flowOf(emptyList())
    override suspend fun insert(debt: PeerDebt) {}
    override suspend fun update(debt: PeerDebt) {}
    override suspend fun delete(debt: PeerDebt) {}
}

private class FakeChamaGoalDao : ChamaGoalDao {
    override fun observeAll(): Flow<List<ChamaGoal>> = flowOf(emptyList())
    override suspend fun insert(goal: ChamaGoal) {}
    override suspend fun update(goal: ChamaGoal) {}
    override suspend fun delete(goal: ChamaGoal) {}
}

private class FakeMobileLoanDao : MobileLoanDao {
    override fun observeAll(): Flow<List<MobileLoan>> = flowOf(emptyList())
    override suspend fun insert(loan: MobileLoan) {}
    override suspend fun update(loan: MobileLoan) {}
    override suspend fun delete(loan: MobileLoan) {}
}

private class FakeSideHustleDao : SideHustleDao {
    override fun observeAll(): Flow<List<SideHustleTransaction>> = flowOf(emptyList())
    override suspend fun insert(tx: SideHustleTransaction) {}
    override suspend fun update(tx: SideHustleTransaction) {}
    override suspend fun delete(tx: SideHustleTransaction) {}
}

private class FakeRecurringBillDao : RecurringBillDao {
    override fun observeAll(): Flow<List<RecurringBill>> = flowOf(emptyList())
    override suspend fun insert(bill: RecurringBill) {}
    override suspend fun update(bill: RecurringBill) {}
    override suspend fun delete(bill: RecurringBill) {}
}

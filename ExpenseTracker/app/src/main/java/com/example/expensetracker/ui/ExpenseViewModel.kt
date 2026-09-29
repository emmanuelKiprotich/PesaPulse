package com.example.expensetracker.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.ai.Anomaly
import com.example.expensetracker.ai.EmailReceiptParser
import com.example.expensetracker.ai.ExpenseCategorizer
import com.example.expensetracker.ai.FinancialCoach
import com.example.expensetracker.ai.Prediction
import com.example.expensetracker.ai.SpendingInsights
import com.example.expensetracker.ai.TransactionParser
import com.example.expensetracker.data.Category
import com.example.expensetracker.data.CategorySource
import com.example.expensetracker.data.ChamaGoal
import com.example.expensetracker.data.Expense
import com.example.expensetracker.data.ExpenseRepository
import com.example.expensetracker.data.MobileLoan
import com.example.expensetracker.data.PeerDebt
import com.example.expensetracker.data.RecurringBill
import com.example.expensetracker.data.SideHustleTransaction
import com.example.expensetracker.worker.SmsImporter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToLong

class ExpenseViewModel(
    private val repository: ExpenseRepository,
    private val categorizer: ExpenseCategorizer
) : ViewModel() {

    private val transactionParser = TransactionParser(categorizer)
    private val emailParser = EmailReceiptParser(categorizer)

    val expenses: StateFlow<List<Expense>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val peerDebts: StateFlow<List<PeerDebt>> = repository.observePeerDebts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val chamaGoals: StateFlow<List<ChamaGoal>> = repository.observeChamaGoals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val mobileLoans: StateFlow<List<MobileLoan>> = repository.observeMobileLoans()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val sideHustles: StateFlow<List<SideHustleTransaction>> = repository.observeSideHustles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val recurringBills: StateFlow<List<RecurringBill>> = repository.observeRecurringBills()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val financialTips = FinancialCoach.tips

    val anomalies: StateFlow<List<Anomaly>> = expenses
        .map { SpendingInsights.flagAnomalies(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val semesterBudgetState: StateFlow<SemesterBudgetState> = expenses
        .map { list ->
            val totalSpent = list.filter { it.category != Category.HELB_INCOME }.sumOf { it.amountMinor }
            val helbIncome = list.filter { it.category == Category.HELB_INCOME }.sumOf { it.amountMinor }
            val disbursement = if (helbIncome > 0) helbIncome else 30_000_00L
            SemesterBudgetState(totalDisbursementMinor = disbursement, totalSpentMinor = totalSpent)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SemesterBudgetState())

    val breathingRoomState: StateFlow<BreathingRoomState> = expenses
        .map { list ->
            val totalSpent = list.filter { it.category != Category.HELB_INCOME }.sumOf { it.amountMinor }
            val helbIncome = list.filter { it.category == Category.HELB_INCOME }.sumOf { it.amountMinor }
            val startingBalance = if (helbIncome > 0) helbIncome else 15_000_00L
            BreathingRoomState(startingBalanceMinor = startingBalance, totalSpentMinor = totalSpent)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BreathingRoomState())

    private val _suggestion = MutableStateFlow<Prediction?>(null)
    val suggestion: StateFlow<Prediction?> = _suggestion.asStateFlow()

    private var suggestJob: Job? = null

    fun onMerchantChanged(merchant: String) {
        suggestJob?.cancel()
        if (merchant.isBlank()) {
            _suggestion.value = null
            return
        }
        suggestJob = viewModelScope.launch {
            delay(300)
            _suggestion.value = categorizer.predict(merchant.trim())
        }
    }

    fun addExpense(merchant: String, amountText: String, chosen: Category?): Boolean {
        val amount = amountText.toDoubleOrNull() ?: return false
        if (merchant.isBlank() || amount <= 0) return false

        val suggested = _suggestion.value
        val (category, source) = when {
            chosen != null -> chosen to CategorySource.USER
            suggested != null -> suggested.category to suggested.source
            else -> Category.OTHER to CategorySource.RULES
        }
        viewModelScope.launch {
            repository.add(
                Expense(
                    amountMinor = (amount * 100).roundToLong(),
                    currency = "KES",
                    merchant = merchant.trim(),
                    category = category,
                    categorySource = source
                )
            )
        }
        _suggestion.value = null
        return true
    }

    fun syncPhoneSms(context: Context, onResult: (Int) -> Unit) {
        viewModelScope.launch {
            val count = SmsImporter.syncInboxSms(context, repository, categorizer)
            onResult(count)
        }
    }

    // Peer Debts
    fun addPeerDebt(peerName: String, amountText: String, description: String, isOwedToMe: Boolean): Boolean {
        val amount = amountText.toDoubleOrNull() ?: return false
        if (peerName.isBlank() || amount <= 0) return false
        viewModelScope.launch {
            repository.addPeerDebt(PeerDebt(peerName = peerName.trim(), amountMinor = (amount * 100).roundToLong(), description = description.trim().ifEmpty { "Bill split" }, isOwedToMe = isOwedToMe))
        }
        return true
    }
    fun toggleDebtSettled(debt: PeerDebt) { viewModelScope.launch { repository.updatePeerDebt(debt.copy(isSettled = !debt.isSettled)) } }
    fun deleteDebt(debt: PeerDebt) { viewModelScope.launch { repository.deletePeerDebt(debt) } }

    // Chama Goals
    fun addChamaGoal(title: String, targetAmountText: String, deadline: String): Boolean {
        val target = targetAmountText.toDoubleOrNull() ?: return false
        if (title.isBlank() || target <= 0) return false
        viewModelScope.launch {
            repository.addChamaGoal(ChamaGoal(title = title.trim(), targetAmountMinor = (target * 100).roundToLong(), deadline = deadline.trim().ifEmpty { "End of Semester" }))
        }
        return true
    }
    fun contributeChamaGoal(goal: ChamaGoal, amountText: String): Boolean {
        val amt = amountText.toDoubleOrNull() ?: return false
        if (amt <= 0) return false
        viewModelScope.launch {
            repository.updateChamaGoal(goal.copy(currentSavedMinor = goal.currentSavedMinor + (amt * 100).roundToLong()))
        }
        return true
    }
    fun deleteChamaGoal(goal: ChamaGoal) { viewModelScope.launch { repository.deleteChamaGoal(goal) } }

    // Mobile Loans (Fuliza, M-Shwari, Hustler Fund)
    fun addMobileLoan(provider: String, principalText: String, dailyRate: Double): Boolean {
        val principal = principalText.toDoubleOrNull() ?: return false
        if (provider.isBlank() || principal <= 0) return false
        viewModelScope.launch {
            repository.addMobileLoan(MobileLoan(provider = provider.trim(), principalMinor = (principal * 100).roundToLong(), dailyInterestRatePercent = dailyRate))
        }
        return true
    }
    fun toggleLoanRepaid(loan: MobileLoan) { viewModelScope.launch { repository.updateMobileLoan(loan.copy(isRepaid = !loan.isRepaid)) } }
    fun deleteLoan(loan: MobileLoan) { viewModelScope.launch { repository.deleteMobileLoan(loan) } }

    // Side Hustle
    fun addSideHustle(businessName: String, isIncome: Boolean, amountText: String, desc: String): Boolean {
        val amt = amountText.toDoubleOrNull() ?: return false
        if (businessName.isBlank() || amt <= 0) return false
        viewModelScope.launch {
            repository.addSideHustle(SideHustleTransaction(businessName = businessName.trim(), isIncome = isIncome, amountMinor = (amt * 100).roundToLong(), description = desc.trim()))
        }
        return true
    }
    fun deleteSideHustle(tx: SideHustleTransaction) { viewModelScope.launch { repository.deleteSideHustle(tx) } }

    // Recurring Bills
    fun addRecurringBill(title: String, amountText: String, dueDay: Int, category: Category): Boolean {
        val amt = amountText.toDoubleOrNull() ?: return false
        if (title.isBlank() || amt <= 0) return false
        viewModelScope.launch {
            repository.addRecurringBill(RecurringBill(title = title.trim(), amountMinor = (amt * 100).roundToLong(), dueDayOfMonth = dueDay, category = category))
        }
        return true
    }
    fun deleteRecurringBill(bill: RecurringBill) { viewModelScope.launch { repository.deleteRecurringBill(bill) } }

    // Simulators
    fun simulateMpesaTransaction() {
        viewModelScope.launch {
            val parsed = transactionParser.parse("QG789XYZ Confirmed. Ksh 1,450.00 sent to JAVA HOUSE RVR on 29/9/26 at 4:15 PM. New balance is Ksh 5,230.00. Transaction cost, Ksh 22.00.")
            if (parsed != null) {
                repository.add(Expense(amountMinor = parsed.amountMinor, currency = parsed.currency, merchant = parsed.merchant, category = parsed.category, categorySource = parsed.source, note = "Automated M-Pesa transaction"))
                if (parsed.feeMinor > 0) {
                    repository.add(Expense(amountMinor = parsed.feeMinor, currency = parsed.currency, merchant = "[M-Pesa Fee]", category = Category.FEES, categorySource = CategorySource.RULES, note = "Transaction cost / fee"))
                }
            }
        }
    }
    fun simulateAirtelTransaction() {
        viewModelScope.launch {
            val parsed = transactionParser.parse("Airtel Money confirmed. Sent KES 850.00 to NAIVAS SUPERMARKET. Fee was KES 15.00.")
            if (parsed != null) {
                repository.add(Expense(amountMinor = parsed.amountMinor, currency = parsed.currency, merchant = parsed.merchant, category = parsed.category, categorySource = parsed.source, note = "Automated Airtel Money transaction"))
                if (parsed.feeMinor > 0) {
                    repository.add(Expense(amountMinor = parsed.feeMinor, currency = parsed.currency, merchant = "[Airtel Fee]", category = Category.FEES, categorySource = CategorySource.RULES, note = "Transaction cost / fee"))
                }
            }
        }
    }
    fun simulateHelbDisbursement() {
        viewModelScope.launch {
            repository.add(Expense(amountMinor = 30_000_00L, currency = "KES", merchant = "HELB / HEF Semester Disbursement", category = Category.HELB_INCOME, categorySource = CategorySource.RULES, note = "Lump-sum student loan disbursement"))
        }
    }
    fun simulateEmailReceipt() {
        viewModelScope.launch {
            val expense = emailParser.parseEmail("Your Jumia Order Receipt - #9876", "Total: KES 1,850.00 for Stationery & Books")
            repository.add(expense)
        }
    }

    fun delete(expense: Expense) { viewModelScope.launch { repository.delete(expense) } }
}

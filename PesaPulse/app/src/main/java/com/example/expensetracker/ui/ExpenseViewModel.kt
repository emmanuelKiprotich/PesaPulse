package com.example.expensetracker.ui

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.ai.Anomaly
import com.example.expensetracker.ai.EmailReceiptParser
import com.example.expensetracker.ai.ExpenseCategorizer
import com.example.expensetracker.ai.FinancialCoach
import com.example.expensetracker.ai.ParsedTransaction
import com.example.expensetracker.ai.Prediction
import com.example.expensetracker.ai.ReceiptScanner
import com.example.expensetracker.ai.SpendingInsights
import com.example.expensetracker.ai.TransactionParser
import com.example.expensetracker.data.Category
import com.example.expensetracker.data.CategorySource
import com.example.expensetracker.data.CategoryTotal
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.expensetracker.ai.FinancialContext
import com.example.expensetracker.ai.FinancialHealthEngine
import com.example.expensetracker.ai.FinancialHealthReport
import com.example.expensetracker.ai.GeminiFinancialAdvisor
import com.example.expensetracker.ai.RunwayForecast
import com.example.expensetracker.ai.SpendingForecaster
import kotlin.math.max
import kotlin.math.roundToLong

data class AiChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val message: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class ExpenseViewModel(
    private val repository: ExpenseRepository,
    private val categorizer: ExpenseCategorizer
) : ViewModel() {

    private val transactionParser = TransactionParser(categorizer)
    private val emailParser = EmailReceiptParser(categorizer)
    private val receiptScanner = ReceiptScanner()

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

    val totalsByCategory: StateFlow<List<CategoryTotal>> = repository.observeTotalsByCategory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val financialTips = FinancialCoach.tips

    // Potential duplicates tracker
    val potentialDuplicateCount: StateFlow<Int> = expenses.map { list ->
        var count = 0
        val seenCodes = mutableSetOf<String>()
        val kept = mutableListOf<Expense>()
        for (e in list.sortedBy { it.timestamp }) {
            val code = e.transactionCode?.trim()?.ifEmpty { null }
            if (code != null) {
                if (seenCodes.contains(code)) {
                    count++
                    continue
                }
                seenCodes.add(code)
            }
            val isDup = kept.any { k ->
                k.amountMinor == e.amountMinor &&
                k.isIncome == e.isIncome &&
                kotlin.math.abs(k.timestamp - e.timestamp) <= 300_000L &&
                (k.merchant.equals(e.merchant, ignoreCase = true) ||
                 k.merchant.contains(e.merchant, ignoreCase = true) ||
                 e.merchant.contains(k.merchant, ignoreCase = true))
            }
            if (isDup) count++ else kept.add(e)
        }
        count
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    // Gemini & On-Device AI Financial Advisor State
    val geminiApiKey = MutableStateFlow("")
    val isAiLoading = MutableStateFlow(false)
    val aiChatMessages = MutableStateFlow<List<AiChatMessage>>(
        listOf(
            AiChatMessage(
                message = "👋 Hello! I am PesaPouch AI, your personal financial advisor. Ask me anything about your budget, Fuliza management, or tap below to generate a real-time financial audit!",
                isFromUser = false
            )
        )
    )

    // Search & Filter
    val searchQuery = MutableStateFlow("")
    val categoryFilter = MutableStateFlow<Category?>(null)

    val filteredExpenses: StateFlow<List<Expense>> = combine(
        expenses,
        searchQuery,
        categoryFilter
    ) { list, query, cat ->
        list.filter { item ->
            val matchesQuery = query.isBlank() ||
                item.merchant.contains(query, ignoreCase = true) ||
                item.note.contains(query, ignoreCase = true) ||
                (item.transactionCode?.contains(query, ignoreCase = true) == true)
            val matchesCat = cat == null || item.category == cat
            matchesQuery && matchesCat
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val anomalies: StateFlow<List<Anomaly>> = expenses
        .map { SpendingInsights.flagAnomalies(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Budget Configuration
    val baselineAllowanceMinor = MutableStateFlow(15_000_00L) // Default KES 15,000 monthly upkeep
    val customSemesterDisbursementMinor = MutableStateFlow<Long?>(null)
    val semesterTotalDays = MutableStateFlow(120)
    val semesterDaysElapsed = MutableStateFlow(30)

    val semesterBudgetState: StateFlow<SemesterBudgetState> = combine(
        expenses,
        customSemesterDisbursementMinor,
        semesterTotalDays,
        semesterDaysElapsed
    ) { list, customDisb, days, elapsed ->
        val totalSpent = list.filter { !it.isIncome }.sumOf { it.amountMinor }
        val disbursement = if (customDisb != null && customDisb > 0) customDisb else 30_000_00L
        SemesterBudgetState(
            totalDisbursementMinor = disbursement,
            totalSpentMinor = totalSpent,
            totalDays = days,
            daysElapsed = elapsed
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SemesterBudgetState())

    val breathingRoomState: StateFlow<BreathingRoomState> = combine(
        expenses,
        mobileLoans,
        baselineAllowanceMinor
    ) { list, loans, baseline ->
        val totalSpent = list.filter { !it.isIncome }.sumOf { it.amountMinor }
        val totalIncome = list.filter { it.isIncome }.sumOf { it.amountMinor }
        val activeLoans = loans.filter { !it.isRepaid }.sumOf { it.totalDueMinor }
        BreathingRoomState(
            startingBalanceMinor = baseline,
            totalIncomeMinor = totalIncome,
            totalSpentMinor = totalSpent,
            totalActiveLoansMinor = activeLoans
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BreathingRoomState())

    val runwayForecast: StateFlow<RunwayForecast> = combine(
        expenses,
        breathingRoomState,
        semesterBudgetState
    ) { list, breathing, sem ->
        val daysRemaining = max(1, sem.totalDays - sem.daysElapsed)
        SpendingForecaster.forecastRunway(
            expenses = list,
            availableBalanceMinor = breathing.breathingRoomMinor,
            cycleDaysRemaining = daysRemaining
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SpendingForecaster.forecastRunway(emptyList(), 0L))

    val financialHealthReport: StateFlow<FinancialHealthReport> = combine(
        expenses,
        chamaGoals,
        mobileLoans,
        runwayForecast,
        anomalies
    ) { list, chamas, loans, runway, anoms ->
        FinancialHealthEngine.computeHealthScore(
            expenses = list,
            chamaGoals = chamas,
            mobileLoans = loans,
            dailyBurnMinor = runway.dailyBurnRateMinor,
            safeDailyLimitMinor = runway.targetSafeDailyLimitMinor,
            anomaliesCount = anoms.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FinancialHealthEngine.computeHealthScore(emptyList(), emptyList(), emptyList(), 0L, 0L, 0))

    // Interactive On-Device AI Sandbox
    val aiSandboxScores = MutableStateFlow<List<Pair<Category, Float>>>(emptyList())
    val aiSandboxInput = MutableStateFlow("")

    fun testAiSandbox(text: String) {
        aiSandboxInput.value = text
        if (text.isBlank()) {
            aiSandboxScores.value = emptyList()
            return
        }
        aiSandboxScores.value = categorizer.predictAll(text.trim())
    }

    fun updateExpenseCategory(expense: Expense, newCategory: Category) {
        viewModelScope.launch {
            repository.update(
                expense.copy(
                    category = newCategory,
                    categorySource = CategorySource.USER
                )
            )
        }
        categorizer.train(expense.merchant, expense.note, newCategory)
    }

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

    fun addExpense(
        merchant: String,
        amountText: String,
        chosen: Category?,
        isIncome: Boolean = false,
        note: String = ""
    ): Boolean {
        val amount = amountText.toDoubleOrNull() ?: return false
        if (merchant.isBlank() || amount <= 0) return false

        val suggested = _suggestion.value
        val (category, source) = when {
            chosen != null -> chosen to CategorySource.USER
            suggested != null -> suggested.category to suggested.source
            isIncome -> Category.OTHER to CategorySource.USER
            else -> Category.OTHER to CategorySource.RULES
        }
        viewModelScope.launch {
            repository.add(
                Expense(
                    amountMinor = (amount * 100).roundToLong(),
                    currency = "KES",
                    merchant = merchant.trim(),
                    note = note.trim(),
                    category = category,
                    categorySource = source,
                    isIncome = isIncome
                )
            )
        }
        // Continuous online learning
        categorizer.train(merchant.trim(), note.trim(), category)
        _suggestion.value = null
        return true
    }

    fun syncPhoneSms(context: Context, onResult: (Int) -> Unit) {
        viewModelScope.launch {
            val count = SmsImporter.syncInboxSms(context, repository, categorizer)
            onResult(count)
        }
    }

    // Receipt OCR Scanning
    fun scanReceipt(bitmap: Bitmap, onResult: (merchant: String, amountStr: String, category: Category, rawText: String) -> Unit) {
        viewModelScope.launch {
            try {
                val scanned = receiptScanner.scan(bitmap)
                val mName = scanned.merchant ?: "Scanned Receipt"
                val prediction = categorizer.predict(mName, scanned.rawText)
                val amtStr = if (scanned.totalMinor != null && scanned.totalMinor > 0) {
                    "%.2f".format(scanned.totalMinor / 100.0)
                } else ""
                onResult(mName, amtStr, prediction.category, scanned.rawText)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Direct Text / Clipboard Parse
    fun parseArbitraryText(text: String, onResult: (ParsedTransaction?) -> Unit) {
        viewModelScope.launch {
            val parsed = transactionParser.parse(text)
            onResult(parsed)
        }
    }

    fun addParsedTransaction(parsed: ParsedTransaction): Boolean {
        viewModelScope.launch {
            val code = parsed.transactionCode ?: TransactionParser.generateDeterministicCode("MANUAL", parsed.merchant + ":" + parsed.rawMerchant, parsed.amountMinor)
            val inserted = repository.add(
                Expense(
                    amountMinor = parsed.amountMinor,
                    currency = parsed.currency,
                    merchant = parsed.merchant,
                    category = parsed.category,
                    categorySource = parsed.source,
                    note = "Parsed from text",
                    transactionCode = code,
                    isIncome = parsed.isIncome
                )
            )
            if (inserted && parsed.feeMinor > 0) {
                repository.add(
                    Expense(
                        amountMinor = parsed.feeMinor,
                        currency = parsed.currency,
                        merchant = "[Transaction Fee]",
                        category = Category.FEES,
                        categorySource = CategorySource.RULES,
                        note = "Transaction fee",
                        transactionCode = "${code}-FEE",
                        isIncome = false
                    )
                )
            }
            if (inserted) {
                categorizer.train(parsed.merchant, "", parsed.category)
            }
        }
        return true
    }

    // Peer Debts
    fun addPeerDebt(peerName: String, amountText: String, description: String, isOwedToMe: Boolean): Boolean {
        val amount = amountText.toDoubleOrNull() ?: return false
        if (peerName.isBlank() || amount <= 0) return false
        viewModelScope.launch {
            repository.addPeerDebt(
                PeerDebt(
                    peerName = peerName.trim(),
                    amountMinor = (amount * 100).roundToLong(),
                    description = description.trim().ifEmpty { "Bill split" },
                    isOwedToMe = isOwedToMe
                )
            )
        }
        return true
    }
    fun toggleDebtSettled(debt: PeerDebt) {
        viewModelScope.launch { repository.updatePeerDebt(debt.copy(isSettled = !debt.isSettled)) }
    }
    fun deleteDebt(debt: PeerDebt) {
        viewModelScope.launch { repository.deletePeerDebt(debt) }
    }

    // Chama Goals
    fun addChamaGoal(title: String, targetAmountText: String, deadline: String): Boolean {
        val target = targetAmountText.toDoubleOrNull() ?: return false
        if (title.isBlank() || target <= 0) return false
        viewModelScope.launch {
            repository.addChamaGoal(
                ChamaGoal(
                    title = title.trim(),
                    targetAmountMinor = (target * 100).roundToLong(),
                    deadline = deadline.trim().ifEmpty { "End of Semester" }
                )
            )
        }
        return true
    }
    fun contributeChamaGoal(goal: ChamaGoal, amountText: String): Boolean {
        val amt = amountText.toDoubleOrNull() ?: return false
        if (amt <= 0) return false
        viewModelScope.launch {
            repository.updateChamaGoal(
                goal.copy(currentSavedMinor = goal.currentSavedMinor + (amt * 100).roundToLong())
            )
        }
        return true
    }
    fun deleteChamaGoal(goal: ChamaGoal) {
        viewModelScope.launch { repository.deleteChamaGoal(goal) }
    }

    // Mobile Loans (Fuliza, M-Shwari, Hustler Fund)
    fun addMobileLoan(provider: String, principalText: String, dailyRate: Double): Boolean {
        val principal = principalText.toDoubleOrNull() ?: return false
        if (provider.isBlank() || principal <= 0) return false
        viewModelScope.launch {
            repository.addMobileLoan(
                MobileLoan(
                    provider = provider.trim(),
                    principalMinor = (principal * 100).roundToLong(),
                    dailyInterestRatePercent = dailyRate
                )
            )
        }
        return true
    }
    fun toggleLoanRepaid(loan: MobileLoan) {
        viewModelScope.launch { repository.updateMobileLoan(loan.copy(isRepaid = !loan.isRepaid)) }
    }
    fun deleteLoan(loan: MobileLoan) {
        viewModelScope.launch { repository.deleteMobileLoan(loan) }
    }

    // Side Hustle
    fun addSideHustle(businessName: String, isIncome: Boolean, amountText: String, desc: String): Boolean {
        val amt = amountText.toDoubleOrNull() ?: return false
        if (businessName.isBlank() || amt <= 0) return false
        viewModelScope.launch {
            repository.addSideHustle(
                SideHustleTransaction(
                    businessName = businessName.trim(),
                    isIncome = isIncome,
                    amountMinor = (amt * 100).roundToLong(),
                    description = desc.trim()
                )
            )
        }
        return true
    }
    fun deleteSideHustle(tx: SideHustleTransaction) {
        viewModelScope.launch { repository.deleteSideHustle(tx) }
    }

    // Recurring Bills
    fun addRecurringBill(title: String, amountText: String, dueDay: Int, category: Category): Boolean {
        val amt = amountText.toDoubleOrNull() ?: return false
        if (title.isBlank() || amt <= 0) return false
        viewModelScope.launch {
            repository.addRecurringBill(
                RecurringBill(
                    title = title.trim(),
                    amountMinor = (amt * 100).roundToLong(),
                    dueDayOfMonth = dueDay,
                    category = category
                )
            )
        }
        return true
    }
    fun payRecurringBill(bill: RecurringBill) {
        viewModelScope.launch {
            repository.add(
                Expense(
                    amountMinor = bill.amountMinor,
                    currency = "KES",
                    merchant = bill.title,
                    category = bill.category,
                    categorySource = CategorySource.USER,
                    note = "Paid recurring monthly bill"
                )
            )
        }
    }
    fun deleteRecurringBill(bill: RecurringBill) {
        viewModelScope.launch { repository.deleteRecurringBill(bill) }
    }

    // Simulators
    fun simulateMpesaTransaction() {
        viewModelScope.launch {
            val code = "QG789XYZ"
            if (repository.getByTransactionCode(code) != null) return@launch
            val parsed = transactionParser.parse("QG789XYZ Confirmed. Ksh 1,450.00 sent to JAVA HOUSE RVR on 29/9/26 at 4:15 PM. New balance is Ksh 5,230.00. Transaction cost, Ksh 22.00.")
            if (parsed != null) {
                repository.add(
                    Expense(
                        amountMinor = parsed.amountMinor,
                        currency = parsed.currency,
                        merchant = parsed.merchant,
                        category = parsed.category,
                        categorySource = parsed.source,
                        note = "Automated M-Pesa transaction",
                        transactionCode = code,
                        isIncome = false
                    )
                )
                if (parsed.feeMinor > 0) {
                    repository.add(
                        Expense(
                            amountMinor = parsed.feeMinor,
                            currency = parsed.currency,
                            merchant = "[M-Pesa Fee]",
                            category = Category.FEES,
                            categorySource = CategorySource.RULES,
                            note = "Transaction fee",
                            transactionCode = "${code}-FEE",
                            isIncome = false
                        )
                    )
                }
            }
        }
    }

    fun simulateAirtelTransaction() {
        viewModelScope.launch {
            val code = "AIRTEL-9871"
            if (repository.getByTransactionCode(code) != null) return@launch
            val parsed = transactionParser.parse("Airtel Money confirmed. Sent KES 850.00 to NAIVAS SUPERMARKET. Fee was KES 15.00.")
            if (parsed != null) {
                repository.add(
                    Expense(
                        amountMinor = parsed.amountMinor,
                        currency = parsed.currency,
                        merchant = parsed.merchant,
                        category = parsed.category,
                        categorySource = parsed.source,
                        note = "Automated Airtel Money transaction",
                        transactionCode = code,
                        isIncome = false
                    )
                )
                if (parsed.feeMinor > 0) {
                    repository.add(
                        Expense(
                            amountMinor = parsed.feeMinor,
                            currency = parsed.currency,
                            merchant = "[Airtel Fee]",
                            category = Category.FEES,
                            categorySource = CategorySource.RULES,
                            note = "Transaction fee",
                            transactionCode = "${code}-FEE",
                            isIncome = false
                        )
                    )
                }
            }
        }
    }



    fun simulateEmailReceipt() {
        viewModelScope.launch {
            val expense = emailParser.parseEmail(
                "Your Naivas Order Receipt - #9876",
                "Total: KES 1,850.00 for Stationery & Books at Naivas Kenya"
            )
            repository.add(expense)
        }
    }

    fun delete(expense: Expense) {
        viewModelScope.launch { repository.delete(expense) }
    }

    fun clearAllExpenses() {
        viewModelScope.launch { repository.clearAllExpenses() }
    }

    fun cleanDuplicateTransactions(onResult: (Int) -> Unit = {}) {
        viewModelScope.launch {
            val removed = repository.deduplicateExpenses()
            onResult(removed)
        }
    }

    // AI Financial Advisor & Gemini Integration
    fun setGeminiApiKey(key: String) {
        geminiApiKey.value = key.trim()
    }

    fun buildCurrentFinancialContext(): FinancialContext {
        val allExpenses = expenses.value
        val totalSpent = allExpenses.filter { !it.isIncome }.sumOf { it.amountMinor }
        val totalInc = allExpenses.filter { it.isIncome }.sumOf { it.amountMinor }
        val runway = runwayForecast.value
        val topCats = totalsByCategory.value.map { it.category.name to it.total }
        val loans = mobileLoans.value.filter { !it.isRepaid }.map { it.provider to it.principalMinor }
        val chamas = chamaGoals.value.map { it.title to it.currentSavedMinor }
        val sem = semesterBudgetState.value

        return FinancialContext(
            totalSpentMinor = totalSpent,
            totalIncomeMinor = totalInc,
            netBalanceMinor = totalInc - totalSpent,
            dailyBurnMinor = runway.dailyBurnRateMinor,
            safeDailyLimitMinor = runway.targetSafeDailyLimitMinor,
            daysOfRunwayRemaining = runway.daysOfRunwayRemaining,
            burnTrend = runway.trend.name,
            topCategories = topCats,
            unpaidLoans = loans,
            chamaGoals = chamas,
            anomaliesCount = anomalies.value.size,
            semesterDaysRemaining = sem.daysRemaining,
            recommendedDailyBudgetMinor = sem.dailyBudgetRecommendedMinor
        )
    }

    fun sendAiMessage(prompt: String) {
        val trimmed = prompt.trim()
        if (trimmed.isBlank()) return
        val userMsg = AiChatMessage(message = trimmed, isFromUser = true)
        aiChatMessages.value = aiChatMessages.value + userMsg
        isAiLoading.value = true

        viewModelScope.launch {
            try {
                val ctx = buildCurrentFinancialContext()
                val reply = GeminiFinancialAdvisor.getFinancialAdvice(geminiApiKey.value, ctx, trimmed)
                aiChatMessages.value = aiChatMessages.value + AiChatMessage(message = reply, isFromUser = false)
            } catch (e: Exception) {
                aiChatMessages.value = aiChatMessages.value + AiChatMessage(
                    message = "⚠️ Could not generate advice: ${e.localizedMessage ?: "Unknown error"}",
                    isFromUser = false
                )
            } finally {
                isAiLoading.value = false
            }
        }
    }

    fun requestAiFinancialAudit() {
        val userMsg = AiChatMessage(message = "Run comprehensive financial audit & recommendations", isFromUser = true)
        aiChatMessages.value = aiChatMessages.value + userMsg
        isAiLoading.value = true

        viewModelScope.launch {
            try {
                val ctx = buildCurrentFinancialContext()
                val reply = GeminiFinancialAdvisor.getFinancialAdvice(geminiApiKey.value, ctx, null)
                aiChatMessages.value = aiChatMessages.value + AiChatMessage(message = reply, isFromUser = false)
            } catch (e: Exception) {
                aiChatMessages.value = aiChatMessages.value + AiChatMessage(
                    message = "⚠️ Could not generate audit: ${e.localizedMessage ?: "Unknown error"}",
                    isFromUser = false
                )
            } finally {
                isAiLoading.value = false
            }
        }
    }

    // CSV Generation for Export
    fun generateCsv(expensesList: List<Expense>): String {
        val sb = StringBuilder()
        sb.append("ID,Date,Merchant,Amount (KES),Category,Type,Note,Reference\n")
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
        for (e in expensesList) {
            val date = sdf.format(java.util.Date(e.timestamp))
            val type = if (e.isIncome) "Income" else "Expense"
            val amt = "%.2f".format(e.amountMinor / 100.0)
            val merchant = "\"${e.merchant.replace("\"", "\"\"")}\""
            val note = "\"${e.note.replace("\"", "\"\"")}\""
            val code = e.transactionCode ?: ""
            sb.append("${e.id},$date,$merchant,$amt,${e.category.name},$type,$note,$code\n")
        }
        return sb.toString()
    }
}

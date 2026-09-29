package com.example.expensetracker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensetracker.data.Category
import com.example.expensetracker.data.Expense
import com.example.expensetracker.data.MobileLoan
import com.example.expensetracker.data.PeerDebt
import com.example.expensetracker.data.RecurringBill

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(vm: ExpenseViewModel) {
    val expenses by vm.expenses.collectAsStateWithLifecycle()
    val peerDebts by vm.peerDebts.collectAsStateWithLifecycle()
    val chamaGoals by vm.chamaGoals.collectAsStateWithLifecycle()
    val mobileLoans by vm.mobileLoans.collectAsStateWithLifecycle()
    val sideHustles by vm.sideHustles.collectAsStateWithLifecycle()
    val recurringBills by vm.recurringBills.collectAsStateWithLifecycle()
    val semesterBudget by vm.semesterBudgetState.collectAsStateWithLifecycle()
    val breathingRoom by vm.breathingRoomState.collectAsStateWithLifecycle()
    val anomalies by vm.anomalies.collectAsStateWithLifecycle()
    val suggestion by vm.suggestion.collectAsStateWithLifecycle()
    val financialTips = vm.financialTips

    var privacyMode by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current
    var syncMessage by remember { mutableStateOf<String?>(null) }
    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            vm.syncPhoneSms(context) { count ->
                syncMessage = "Successfully synced $count transaction(s) from phone SMS!"
            }
        } else {
            syncMessage = "SMS permission denied. Cannot sync phone statements."
        }
    }

    var merchant by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var chosen by remember { mutableStateOf<Category?>(null) }

    // Chama goal input
    var chamaTitle by remember { mutableStateOf("") }
    var chamaTarget by remember { mutableStateOf("") }
    var chamaDeadline by remember { mutableStateOf("End of Semester") }

    // Mobile loan input
    var loanProvider by remember { mutableStateOf("Fuliza") }
    var loanPrincipal by remember { mutableStateOf("") }

    // Side hustle input
    var hustleName by remember { mutableStateOf("") }
    var hustleAmt by remember { mutableStateOf("") }
    var hustleDesc by remember { mutableStateOf("") }
    var hustleIsIncome by remember { mutableStateOf(true) }

    // Bill input
    var billTitle by remember { mutableStateOf("") }
    var billAmt by remember { mutableStateOf("") }
    var billDay by remember { mutableStateOf("5") }

    // Peer debt input
    var peerName by remember { mutableStateOf("") }
    var peerAmount by remember { mutableStateOf("") }
    var peerDesc by remember { mutableStateOf("") }
    var isOwedToMe by remember { mutableStateOf(true) }

    var selectedTab by remember { mutableStateOf(0) } // 0: Expenses, 1: HELB/Coach, 2: Chama & Loans, 3: Side Hustle, 4: Bills & Debts

    fun formatMoney(amountMinor: Long): String {
        return if (privacyMode) "KES •••••" else "KES %,.2f".format(amountMinor / 100.0)
    }

    val groupedExpenses = remember(expenses) {
        expenses.groupBy { expense ->
            val netDate = java.util.Date(expense.timestamp)
            val now = java.util.Date()
            val sdfDay = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault())
            val txDay = sdfDay.format(netDate)
            val todayDay = sdfDay.format(now)
            val cal = java.util.Calendar.getInstance().apply { time = now; add(java.util.Calendar.DAY_OF_YEAR, -1) }
            val yesterdayDay = sdfDay.format(cal.time)

            when (txDay) {
                todayDay -> "Today"
                yesterdayDay -> "Yesterday"
                else -> java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(netDate)
            }
        }
    }

    Scaffold { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("PesaPulse Kenya", style = MaterialTheme.typography.headlineSmall)
                FilterChip(
                    selected = privacyMode,
                    onClick = { privacyMode = !privacyMode },
                    label = { Text(if (privacyMode) "🔒 Privacy: ON" else "👁️ Privacy: OFF") }
                )
            }

            // Tab navigation
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = selectedTab == 0, onClick = { selectedTab = 0 }, label = { Text("💸 Expenses & Breathing Room") })
                FilterChip(selected = selectedTab == 1, onClick = { selectedTab = 1 }, label = { Text("🎓 HELB & Coach") })
                FilterChip(selected = selectedTab == 2, onClick = { selectedTab = 2 }, label = { Text("🤝 Chama & Loans") })
                FilterChip(selected = selectedTab == 3, onClick = { selectedTab = 3 }, label = { Text("💼 Side Hustle") })
                FilterChip(selected = selectedTab == 4, onClick = { selectedTab = 4 }, label = { Text("📅 Bills & Debts") })
            }

            if (selectedTab == 0) {
                // Tab 0: Breathing Room Hero Card & M-Pesa / Airtel Auto-Capture
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                    item {
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("🌿 Breathing Room (Remaining to Spend)", style = MaterialTheme.typography.titleMedium)
                                Text(formatMoney(breathingRoom.breathingRoomMinor), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                                Text("Available balance from your baseline budget after all offline M-Pesa & Airtel spending.", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    item {
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("M-Pesa & Airtel Money SMS Sync (100% Offline)", style = MaterialTheme.typography.titleSmall)
                                Text("Automatically parses M-Pesa & Airtel Money SMS messages (including transaction fees) to track spending without internet:", style = MaterialTheme.typography.bodySmall)
                                Button(
                                    onClick = { permissionLauncher.launch(android.Manifest.permission.READ_SMS) },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("📥 Sync Phone SMS (M-Pesa & Airtel)") }
                                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(onClick = { vm.simulateMpesaTransaction() }) { Text("Simulate M-Pesa") }
                                    Button(onClick = { vm.simulateAirtelTransaction() }) { Text("Simulate Airtel") }
                                    Button(onClick = { vm.simulateHelbDisbursement() }) { Text("Simulate HELB") }
                                }
                                syncMessage?.let {
                                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }

                    item {
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Add Manual Expense", style = MaterialTheme.typography.titleSmall)
                                OutlinedTextField(
                                    value = merchant,
                                    onValueChange = { merchant = it; chosen = null; vm.onMerchantChanged(it) },
                                    label = { Text("Merchant / Description (e.g. Mama Mboga)") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = amount,
                                    onValueChange = { amount = it },
                                    label = { Text("Amount (KES)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                suggestion?.let {
                                    Text(
                                        "AI Suggested: ${it.category.pretty()} (${(it.confidence * 100).toInt()}%)",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Category.entries.forEach { c ->
                                        FilterChip(
                                            selected = (chosen ?: suggestion?.category) == c,
                                            onClick = { chosen = c },
                                            label = { Text(c.pretty()) }
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        if (vm.addExpense(merchant, amount, chosen)) {
                                            merchant = ""; amount = ""; chosen = null
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("Save Expense") }
                            }
                        }
                    }

                    if (anomalies.isNotEmpty()) {
                        item {
                            Card(Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(12.dp)) {
                                    Text("Spending Insights & Alerts", style = MaterialTheme.typography.titleSmall)
                                    anomalies.take(3).forEach { Text("• ${it.message}", style = MaterialTheme.typography.bodySmall) }
                                }
                            }
                        }
                    }

                    groupedExpenses.forEach { (dateGroup, itemsInGroup) ->
                        item {
                            Text(
                                text = "📅 $dateGroup",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }
                        items(itemsInGroup, key = { it.id }) { expense ->
                            ExpenseRow(expense, formatMoney = { amt -> formatMoney(amt) }, onDelete = { vm.delete(expense) })
                        }
                    }
                }
            } else if (selectedTab == 1) {
                // Tab 1: HELB / Semester Pacing & AI Financial Coach
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                    item {
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Semester Budget Pacing (HEF / HELB)", style = MaterialTheme.typography.titleMedium)
                                Text("Total Disbursement: ${formatMoney(semesterBudget.totalDisbursementMinor)}", style = MaterialTheme.typography.bodyMedium)
                                Text("Total Spent: ${formatMoney(semesterBudget.totalSpentMinor)}", style = MaterialTheme.typography.bodyMedium)
                                Text("Remaining Balance: ${formatMoney(semesterBudget.remainingMinor)}", style = MaterialTheme.typography.bodyMedium)
                                Text("Recommended Daily Budget: ${formatMoney(semesterBudget.dailyBudgetRecommendedMinor)} / day", style = MaterialTheme.typography.bodySmall)
                                Text("Current Daily Burn Rate: ${formatMoney(semesterBudget.currentDailyBurnRateMinor)} / day", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    item {
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("🤖 AI Financial Coach & Literacy Tips", style = MaterialTheme.typography.titleMedium)
                                financialTips.forEach { tip ->
                                    Text(tip, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 4.dp))
                                }
                            }
                        }
                    }
                }
            } else if (selectedTab == 2) {
                // Tab 2: Chama & Short-term Mobile Loans
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                    item {
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Create Chama Savings Goal", style = MaterialTheme.typography.titleSmall)
                                OutlinedTextField(value = chamaTitle, onValueChange = { chamaTitle = it }, label = { Text("Goal Title (e.g. Semester Books Chama)") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = chamaTarget, onValueChange = { chamaTarget = it }, label = { Text("Target Amount (KES)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                                Button(onClick = { if (vm.addChamaGoal(chamaTitle, chamaTarget, chamaDeadline)) { chamaTitle = ""; chamaTarget = "" } }, modifier = Modifier.fillMaxWidth()) { Text("Add Chama Goal") }
                            }
                        }
                    }
                    items(chamaGoals, key = { it.id }) { goal ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(goal.title, style = MaterialTheme.typography.titleSmall)
                                Text("Saved: ${formatMoney(goal.currentSavedMinor)} / ${formatMoney(goal.targetAmountMinor)}", style = MaterialTheme.typography.bodySmall)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(onClick = { vm.contributeChamaGoal(goal, "500") }) { Text("+ KES 500") }
                                    TextButton(onClick = { vm.deleteChamaGoal(goal) }) { Text("Delete") }
                                }
                            }
                        }
                    }

                    item {
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Track Mobile Loan (Fuliza / M-Shwari / Hustler Fund)", style = MaterialTheme.typography.titleSmall)
                                OutlinedTextField(value = loanProvider, onValueChange = { loanProvider = it }, label = { Text("Provider (Fuliza / M-Shwari)") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = loanPrincipal, onValueChange = { loanPrincipal = it }, label = { Text("Principal Amount (KES)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                                Button(onClick = { if (vm.addMobileLoan(loanProvider, loanPrincipal, 0.5)) { loanPrincipal = "" } }, modifier = Modifier.fillMaxWidth()) { Text("Add Mobile Loan") }
                            }
                        }
                    }
                    items(mobileLoans, key = { it.id }) { loan ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("${loan.provider} Loan: ${formatMoney(loan.principalMinor)} principal", style = MaterialTheme.typography.titleSmall)
                                Text("Active Days: ${loan.daysActive} | Daily Interest: ${loan.dailyInterestRatePercent}%", style = MaterialTheme.typography.bodySmall)
                                Text("Total Accrued Due: ${formatMoney(loan.totalDueMinor)} (Interest: ${formatMoney(loan.totalInterestMinor)})", style = MaterialTheme.typography.bodySmall)
                                Text("Status: ${if (loan.isRepaid) "Repaid ✅" else "Active ⚠️"}", style = MaterialTheme.typography.bodySmall)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    TextButton(onClick = { vm.toggleLoanRepaid(loan) }) { Text(if (loan.isRepaid) "Mark Active" else "Mark Repaid") }
                                    TextButton(onClick = { vm.deleteLoan(loan) }) { Text("Delete") }
                                }
                            }
                        }
                    }
                }
            } else if (selectedTab == 3) {
                // Tab 3: Side Hustle Profitability
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                    item {
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Add Campus Side Hustle Transaction", style = MaterialTheme.typography.titleSmall)
                                OutlinedTextField(value = hustleName, onValueChange = { hustleName = it }, label = { Text("Business Name (e.g. Hostel Wi-Fi / Printing)") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = hustleAmt, onValueChange = { hustleAmt = it }, label = { Text("Amount (KES)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = hustleDesc, onValueChange = { hustleDesc = it }, label = { Text("Description / Note") }, modifier = Modifier.fillMaxWidth())
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilterChip(selected = hustleIsIncome, onClick = { hustleIsIncome = true }, label = { Text("Revenue 📈") })
                                    FilterChip(selected = !hustleIsIncome, onClick = { hustleIsIncome = false }, label = { Text("Business Expense 📉") })
                                }
                                Button(onClick = { if (vm.addSideHustle(hustleName, hustleIsIncome, hustleAmt, hustleDesc)) { hustleName = ""; hustleAmt = ""; hustleDesc = "" } }, modifier = Modifier.fillMaxWidth()) { Text("Save Side Hustle Entry") }
                            }
                        }
                    }

                    val totalHustleRev = sideHustles.filter { it.isIncome }.sumOf { it.amountMinor }
                    val totalHustleExp = sideHustles.filter { !it.isIncome }.sumOf { it.amountMinor }
                    val netHustleProfit = totalHustleRev - totalHustleExp

                    item {
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp)) {
                                Text("Side Hustle P&L Summary", style = MaterialTheme.typography.titleSmall)
                                Text("Total Revenue: ${formatMoney(totalHustleRev)}", style = MaterialTheme.typography.bodySmall)
                                Text("Total Expenses: ${formatMoney(totalHustleExp)}", style = MaterialTheme.typography.bodySmall)
                                Text("Net Profit: ${formatMoney(netHustleProfit)}", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }

                    items(sideHustles, key = { it.id }) { tx ->
                        Card(Modifier.fillMaxWidth()) {
                            Row(Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("${tx.businessName} (${if (tx.isIncome) "Revenue" else "Expense"})", style = MaterialTheme.typography.titleSmall)
                                    Text(tx.description, style = MaterialTheme.typography.bodySmall)
                                }
                                Column {
                                    Text("${if (tx.isIncome) "+" else "-"}${formatMoney(tx.amountMinor)}")
                                    TextButton(onClick = { vm.deleteSideHustle(tx) }) { Text("Delete") }
                                }
                            }
                        }
                    }
                }
            } else {
                // Tab 4: Bills Calendar & Peer Debts
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                    item {
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Add Recurring Bill Reminder (Rent / KPLC)", style = MaterialTheme.typography.titleSmall)
                                OutlinedTextField(value = billTitle, onValueChange = { billTitle = it }, label = { Text("Bill Title (e.g. Hostel Rent)") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = billAmt, onValueChange = { billAmt = it }, label = { Text("Amount (KES)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = billDay, onValueChange = { billDay = it }, label = { Text("Due Day of Month (1-31)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                                Button(onClick = { if (vm.addRecurringBill(billTitle, billAmt, billDay.toIntOrNull() ?: 5, Category.RENT_UTILITIES)) { billTitle = ""; billAmt = "" } }, modifier = Modifier.fillMaxWidth()) { Text("Save Recurring Bill") }
                            }
                        }
                    }
                    items(recurringBills, key = { it.id }) { bill ->
                        Card(Modifier.fillMaxWidth()) {
                            Row(Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text(bill.title, style = MaterialTheme.typography.titleSmall)
                                    Text("Due on Day ${bill.dueDayOfMonth} of every month", style = MaterialTheme.typography.bodySmall)
                                }
                                Column {
                                    Text(formatMoney(bill.amountMinor))
                                    TextButton(onClick = { vm.deleteRecurringBill(bill) }) { Text("Delete") }
                                }
                            }
                        }
                    }

                    item {
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Track Informal Peer Debt / Bill Split", style = MaterialTheme.typography.titleSmall)
                                OutlinedTextField(value = peerName, onValueChange = { peerName = it }, label = { Text("Peer Name") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = peerAmount, onValueChange = { peerAmount = it }, label = { Text("Amount (KES)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = peerDesc, onValueChange = { peerDesc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilterChip(selected = isOwedToMe, onClick = { isOwedToMe = true }, label = { Text("They Owe Me") })
                                    FilterChip(selected = !isOwedToMe, onClick = { isOwedToMe = false }, label = { Text("I Owe Them") })
                                }
                                Button(onClick = { if (vm.addPeerDebt(peerName, peerAmount, peerDesc, isOwedToMe)) { peerName = ""; peerAmount = ""; peerDesc = "" } }, modifier = Modifier.fillMaxWidth()) { Text("Save Peer Debt") }
                            }
                        }
                    }
                    items(peerDebts, key = { it.id }) { debt ->
                        PeerDebtRow(debt = debt, formatMoney = { amt -> formatMoney(amt) }, onToggle = { vm.toggleDebtSettled(debt) }, onDelete = { vm.deleteDebt(debt) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpenseRow(expense: Expense, formatMoney: (Long) -> String, onDelete: () -> Unit) {
    val dateStr = remember(expense.timestamp) {
        val sdf = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault())
        sdf.format(java.util.Date(expense.timestamp))
    }
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(expense.merchant, style = MaterialTheme.typography.titleSmall)
                Text("${expense.category.pretty()} · ${expense.categorySource.name.lowercase()}", style = MaterialTheme.typography.bodySmall)
                Text(dateStr, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }
            Column {
                Text(formatMoney(expense.amountMinor))
                TextButton(onClick = onDelete) { Text("Delete") }
            }
        }
    }
}

@Composable
private fun PeerDebtRow(debt: PeerDebt, formatMoney: (Long) -> String, onToggle: () -> Unit, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("${debt.peerName} (${if (debt.isOwedToMe) "Owes You" else "You Owe"})", style = MaterialTheme.typography.titleSmall)
                Text(debt.description, style = MaterialTheme.typography.bodySmall)
                Text("Status: ${if (debt.isSettled) "Settled ✅" else "Pending ⏳"}", style = MaterialTheme.typography.bodySmall)
            }
            Column {
                Text(formatMoney(debt.amountMinor))
                Row {
                    TextButton(onClick = onToggle) { Text(if (debt.isSettled) "Unsettle" else "Settle") }
                    TextButton(onClick = onDelete) { Text("Delete") }
                }
            }
        }
    }
}

private fun Category.pretty() = name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }

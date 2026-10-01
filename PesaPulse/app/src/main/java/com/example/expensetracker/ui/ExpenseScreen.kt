package com.example.expensetracker.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.expensetracker.auth.BiometricAuthManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensetracker.ai.Anomaly
import com.example.expensetracker.ai.BurnTrend
import com.example.expensetracker.ai.FinancialHealthReport
import com.example.expensetracker.ai.ParsedTransaction
import com.example.expensetracker.ai.RunwayForecast
import com.example.expensetracker.data.Category
import com.example.expensetracker.data.CategorySource
import com.example.expensetracker.data.CategoryTotal
import com.example.expensetracker.data.ChamaGoal
import com.example.expensetracker.data.Expense
import com.example.expensetracker.data.MobileLoan
import com.example.expensetracker.data.PeerDebt
import com.example.expensetracker.data.RecurringBill
import com.example.expensetracker.data.SideHustleTransaction
import com.example.expensetracker.ui.theme.ChamaViolet
import com.example.expensetracker.ui.theme.MoneyExpense
import com.example.expensetracker.ui.theme.MoneyIncome
import com.example.expensetracker.ui.theme.MoneyWarning
import com.example.expensetracker.ui.theme.PesaGreen
import com.example.expensetracker.ui.theme.PesaGreenDark
import com.example.expensetracker.ui.theme.getCategoryColor
import com.example.expensetracker.ui.theme.getCategoryIcon
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(vm: ExpenseViewModel) {
    val context = LocalContext.current
    val allExpenses by vm.expenses.collectAsStateWithLifecycle()
    val filteredExpenses by vm.filteredExpenses.collectAsStateWithLifecycle()
    val peerDebts by vm.peerDebts.collectAsStateWithLifecycle()
    val chamaGoals by vm.chamaGoals.collectAsStateWithLifecycle()
    val mobileLoans by vm.mobileLoans.collectAsStateWithLifecycle()
    val sideHustles by vm.sideHustles.collectAsStateWithLifecycle()
    val recurringBills by vm.recurringBills.collectAsStateWithLifecycle()
    val semesterBudget by vm.semesterBudgetState.collectAsStateWithLifecycle()
    val breathingRoom by vm.breathingRoomState.collectAsStateWithLifecycle()
    val totalsByCategory by vm.totalsByCategory.collectAsStateWithLifecycle()
    val anomalies by vm.anomalies.collectAsStateWithLifecycle()
    val runwayForecast by vm.runwayForecast.collectAsStateWithLifecycle()
    val healthReport by vm.financialHealthReport.collectAsStateWithLifecycle()
    val suggestion by vm.suggestion.collectAsStateWithLifecycle()
    val financialTips = vm.financialTips
    val duplicateCount by vm.potentialDuplicateCount.collectAsStateWithLifecycle()

    val isUnlocked by vm.isUnlocked.collectAsStateWithLifecycle()
    val syncedMpesaBalance by vm.syncedMpesaBalance.collectAsStateWithLifecycle()

    val searchQuery by vm.searchQuery.collectAsStateWithLifecycle()
    val categoryFilter by vm.categoryFilter.collectAsStateWithLifecycle()

    var privacyMode by remember { mutableStateOf(false) }
    var syncMessage by remember { mutableStateOf<String?>(null) }
    var selectedNavTab by remember { mutableStateOf(0) }

    // Bottom Sheet for adding transactions
    var showAddSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    // Dialog states
    var showScanDialog by remember { mutableStateOf(false) }
    var scannedMerchant by remember { mutableStateOf("") }
    var scannedAmount by remember { mutableStateOf("") }
    var scannedCategory by remember { mutableStateOf(Category.SHOPPING) }

    var showSemesterSettingsDialog by remember { mutableStateOf(false) }
    var tempDisbursement by remember { mutableStateOf("30000") }
    var tempSemesterDays by remember { mutableStateOf("120") }
    var tempDaysElapsed by remember { mutableStateOf("30") }

    // Manual Expense inputs
    var merchant by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var chosen by remember { mutableStateOf<Category?>(null) }
    var isManualIncome by remember { mutableStateOf(false) }

    // Chama goal inputs
    var chamaTitle by remember { mutableStateOf("") }
    var chamaTarget by remember { mutableStateOf("") }
    var chamaDeadline by remember { mutableStateOf("End of Semester") }

    // Mobile loan inputs
    var loanProvider by remember { mutableStateOf("Fuliza") }
    var loanPrincipal by remember { mutableStateOf("") }
    var loanDailyRate by remember { mutableStateOf("0.5") }

    // Side hustle inputs
    var hustleName by remember { mutableStateOf("") }
    var hustleAmt by remember { mutableStateOf("") }
    var hustleDesc by remember { mutableStateOf("") }
    var hustleIsIncome by remember { mutableStateOf(true) }

    // Recurring Bill inputs
    var billTitle by remember { mutableStateOf("") }
    var billAmt by remember { mutableStateOf("") }
    var billDay by remember { mutableStateOf("5") }

    // Peer debt inputs
    var peerName by remember { mutableStateOf("") }
    var peerAmount by remember { mutableStateOf("") }
    var peerDesc by remember { mutableStateOf("") }
    var isOwedToMe by remember { mutableStateOf(true) }

    // Permissions & Pickers
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            vm.syncPhoneSms(context) { count ->
                syncMessage = "✅ Imported $count transaction(s)!"
            }
        } else {
            syncMessage = "❌ SMS permission denied."
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= 28) {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                vm.scanReceipt(bitmap) { scannedM, scannedA, scannedC, _ ->
                    scannedMerchant = scannedM
                    scannedAmount = scannedA
                    scannedCategory = scannedC
                    showScanDialog = true
                }
            } catch (e: Exception) {
                syncMessage = "Error: ${e.message}"
            }
        }
    }

    LaunchedEffect(Unit) {
        vm.refreshMpesaBalance(context)
        val activity = context as? FragmentActivity
        if (activity != null && BiometricAuthManager.isBiometricAvailable(context)) {
            BiometricAuthManager.authenticate(
                activity = activity,
                onSuccess = { vm.unlockApp() },
                onError = { err -> syncMessage = err }
            )
        } else {
            vm.unlockApp()
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {
            vm.syncPhoneSms(context) { count ->
                if (count > 0) {
                    syncMessage = "✅ Auto-synced $count real transaction(s) from Messages app!"
                }
            }
        }
    }

    if (!isUnlocked) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(PesaGreen, Color(0xFF042F2E)))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🔒", fontSize = 40.sp)
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    "PesaPulse Financial Lock",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Authenticate using Fingerprint, Face ID, or PIN to unlock your M-Pesa & financial data",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(28.dp))
                Button(
                    onClick = {
                        val activity = context as? FragmentActivity
                        if (activity != null) {
                            BiometricAuthManager.authenticate(
                                activity = activity,
                                onSuccess = { vm.unlockApp() },
                                onError = { err -> syncMessage = err }
                            )
                        } else {
                            vm.unlockApp()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.8f).height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PesaGreen)
                ) {
                    Text("🔓 Unlock App", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                syncMessage?.let {
                    Spacer(Modifier.height(12.dp))
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }
        }
        return
    }

    fun formatMoney(amountMinor: Long): String {
        return if (privacyMode) "KES •••••" else "KES %,.2f".format(amountMinor / 100.0)
    }

    val currentLocale = LocalConfiguration.current.locales[0]

    val groupedExpenses = remember(filteredExpenses, currentLocale) {
        filteredExpenses.groupBy { expense ->
            val netDate = Date(expense.timestamp)
            val now = Date()
            val sdfDay = SimpleDateFormat("yyyyMMdd", currentLocale)
            val txDay = sdfDay.format(netDate)
            val todayDay = sdfDay.format(now)
            val cal = Calendar.getInstance().apply { time = now; add(Calendar.DAY_OF_YEAR, -1) }
            val yesterdayDay = sdfDay.format(cal.time)

            when (txDay) {
                todayDay -> "Today"
                yesterdayDay -> "Yesterday"
                else -> SimpleDateFormat("dd MMM yyyy", currentLocale).format(netDate)
            }
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                val navItems = listOf(
                    Triple(0, "💸", "Spend"),
                    Triple(1, "🧠", "Insights"),
                    Triple(2, "🤖", "AI Coach"),
                    Triple(3, "🤝", "Chama"),
                    Triple(4, "💼", "Hustle"),
                    Triple(5, "📅", "Bills")
                )
                navItems.forEach { (index, icon, label) ->
                    NavigationBarItem(
                        selected = selectedNavTab == index,
                        onClick = { selectedNavTab = index },
                        icon = { Text(icon, fontSize = 18.sp) },
                        label = {
                            Text(
                                label,
                                fontSize = 10.sp,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = if (selectedNavTab == index) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PesaGreen,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            if (selectedNavTab == 0) {
                FloatingActionButton(
                    onClick = { showAddSheet = true },
                    containerColor = PesaGreen,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Text("➕", fontSize = 22.sp)
                }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Modern Top Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        Brush.linearGradient(listOf(PesaGreen, Color(0xFF042F2E)))
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🇰🇪", fontSize = 18.sp)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "PesaPouch",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    "Campus Financial Intelligence",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = privacyMode,
                                onClick = { privacyMode = !privacyMode },
                                label = { Text(if (privacyMode) "🔒" else "👁️", fontSize = 13.sp) },
                                shape = CircleShape,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                            IconButtonBox(emoji = "📷", tooltip = "Scan Receipt") {
                                imagePickerLauncher.launch("image/*")
                            }
                            if (duplicateCount > 0) {
                                IconButtonBox(emoji = "🧹", tooltip = "Clean $duplicateCount Duplicates") {
                                    vm.cleanDuplicateTransactions { count ->
                                        syncMessage = "Cleaned $count duplicate transactions"
                                    }
                                }
                            }
                            IconButtonBox(emoji = "🔒", tooltip = "Lock App") {
                                vm.lockApp()
                            }
                            IconButtonBox(emoji = "📤", tooltip = "Export CSV") {
                                val csv = vm.generateCsv(allExpenses)
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    putExtra(Intent.EXTRA_TEXT, csv)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Export Expenses CSV"))
                            }
                        }
                    }
                }
            }

            // Body Content based on Navigation Tab
            Box(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp)) {
                when (selectedNavTab) {
                    0 -> SpendingTab(
                        vm = vm,
                        breathingRoom = breathingRoom,
                        syncedMpesaBalance = syncedMpesaBalance,
                        groupedExpenses = groupedExpenses,
                        filteredExpenses = filteredExpenses,
                        allExpenses = allExpenses,
                        anomalies = anomalies,
                        searchQuery = searchQuery,
                        categoryFilter = categoryFilter,
                        syncMessage = syncMessage,
                        duplicateCount = duplicateCount,
                        onCleanDuplicates = {
                            vm.cleanDuplicateTransactions { count ->
                                syncMessage = "Cleaned $count duplicate transactions"
                            }
                        },
                        formatMoney = { amt -> formatMoney(amt) },
                        onSyncClick = { permissionLauncher.launch(Manifest.permission.READ_SMS) }
                    )
                    1 -> AiInsightsTab(
                        vm = vm,
                        allExpenses = allExpenses,
                        totalsByCategory = totalsByCategory,
                        healthReport = healthReport,
                        runwayForecast = runwayForecast,
                        anomalies = anomalies,
                        formatMoney = { amt -> formatMoney(amt) }
                    )
                    2 -> SemesterCoachTab(
                        vm = vm,
                        semesterBudget = semesterBudget,
                        financialTips = financialTips,
                        formatMoney = { amt -> formatMoney(amt) },
                        onAdjustClick = { showSemesterSettingsDialog = true }
                    )
                    3 -> ChamaLoansTab(
                        vm = vm,
                        chamaGoals = chamaGoals,
                        mobileLoans = mobileLoans,
                        chamaTitle = chamaTitle,
                        onChamaTitleChange = { chamaTitle = it },
                        chamaTarget = chamaTarget,
                        onChamaTargetChange = { chamaTarget = it },
                        chamaDeadline = chamaDeadline,
                        onChamaDeadlineChange = { chamaDeadline = it },
                        loanProvider = loanProvider,
                        onLoanProviderChange = { loanProvider = it },
                        loanPrincipal = loanPrincipal,
                        onLoanPrincipalChange = { loanPrincipal = it },
                        loanDailyRate = loanDailyRate,
                        onLoanDailyRateChange = { loanDailyRate = it },
                        formatMoney = { amt -> formatMoney(amt) }
                    )
                    4 -> SideHustleTab(
                        vm = vm,
                        sideHustles = sideHustles,
                        hustleName = hustleName,
                        onHustleNameChange = { hustleName = it },
                        hustleAmt = hustleAmt,
                        onHustleAmtChange = { hustleAmt = it },
                        hustleDesc = hustleDesc,
                        onHustleDescChange = { hustleDesc = it },
                        hustleIsIncome = hustleIsIncome,
                        onHustleIsIncomeChange = { hustleIsIncome = it },
                        formatMoney = { amt -> formatMoney(amt) }
                    )
                    5 -> BillsDebtsTab(
                        vm = vm,
                        context = context,
                        recurringBills = recurringBills,
                        peerDebts = peerDebts,
                        billTitle = billTitle,
                        onBillTitleChange = { billTitle = it },
                        billAmt = billAmt,
                        onBillAmtChange = { billAmt = it },
                        billDay = billDay,
                        onBillDayChange = { billDay = it },
                        peerName = peerName,
                        onPeerNameChange = { peerName = it },
                        peerAmount = peerAmount,
                        onPeerAmountChange = { peerAmount = it },
                        peerDesc = peerDesc,
                        onPeerDescChange = { peerDesc = it },
                        isOwedToMe = isOwedToMe,
                        onIsOwedToMeChange = { isOwedToMe = it },
                        formatMoney = { amt -> formatMoney(amt) }
                    )
                }
            }
        }
    }

    // BOTTOM SHEET: Add Transaction
    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            sheetState = sheetState
        ) {
            Column(
                Modifier
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    if (isManualIncome) "📈 Add Income / Upkeep" else "💸 Add New Expense",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !isManualIncome,
                        onClick = { isManualIncome = false },
                        label = { Text("Expense 📉") },
                        shape = RoundedCornerShape(12.dp)
                    )
                    FilterChip(
                        selected = isManualIncome,
                        onClick = { isManualIncome = true },
                        label = { Text("Income / Upkeep 📈") },
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (KES)") },
                    prefix = { Text("KES ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                OutlinedTextField(
                    value = merchant,
                    onValueChange = { merchant = it; chosen = null; vm.onMerchantChanged(it) },
                    label = { Text(if (isManualIncome) "Source (e.g. Upkeep, Parents, Freelance)" else "Merchant / Recipient (e.g. Mama Mboga)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note / Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                if (!isManualIncome) {
                    suggestion?.let {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(PesaGreen.copy(alpha = 0.1f))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🤖 AI Suggestion: ", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PesaGreenDark)
                            Text("${it.category.pretty()} (${(it.confidence * 100).toInt()}%)", fontSize = 12.sp, color = PesaGreenDark)
                        }
                    }

                    Text("Category", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Category.entries.forEach { c ->
                            val isSelected = (chosen ?: suggestion?.category) == c
                            FilterChip(
                                selected = isSelected,
                                onClick = { chosen = c },
                                label = { Text("${getCategoryIcon(c)} ${c.pretty()}") },
                                shape = RoundedCornerShape(10.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = getCategoryColor(c).copy(alpha = 0.2f),
                                    selectedLabelColor = getCategoryColor(c)
                                )
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        val cat = if (isManualIncome) Category.OTHER else chosen
                        if (vm.addExpense(merchant, amount, cat, isIncome = isManualIncome, note = note)) {
                            merchant = ""; amount = ""; note = ""; chosen = null
                            scope.launch { sheetState.hide(); showAddSheet = false }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isManualIncome) MoneyIncome else PesaGreen)
                ) {
                    Text(if (isManualIncome) "Save Income Entry" else "Record Expense", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // DIALOG: Scanned Receipt Confirmation
    if (showScanDialog) {
        AlertDialog(
            onDismissRequest = { showScanDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("📷", fontSize = 24.sp)
                    Text("Receipt Scanned Successfully")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("ML Kit OCR extracted details from your receipt:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    OutlinedTextField(
                        value = scannedMerchant,
                        onValueChange = { scannedMerchant = it },
                        label = { Text("Merchant / Store") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = scannedAmount,
                        onValueChange = { scannedAmount = it },
                        label = { Text("Total Amount (KES)") },
                        prefix = { Text("KES ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Text("Category:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Category.entries.forEach { c ->
                            FilterChip(
                                selected = scannedCategory == c,
                                onClick = { scannedCategory = c },
                                label = { Text("${getCategoryIcon(c)} ${c.pretty()}") },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (vm.addExpense(scannedMerchant, scannedAmount, scannedCategory, note = "Scanned receipt OCR")) {
                            showScanDialog = false
                        }
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Confirm & Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showScanDialog = false }) { Text("Cancel") }
            }
        )
    // DIALOG: Semester Settings
    if (showSemesterSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSemesterSettingsDialog = false },
            title = { Text("⚙️ Configure Semester Budget") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = tempDisbursement,
                        onValueChange = { tempDisbursement = it },
                        label = { Text("Semester Allowance / Loan (KES)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = tempSemesterDays,
                        onValueChange = { tempSemesterDays = it },
                        label = { Text("Semester Duration (Days, e.g. 120)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = tempDaysElapsed,
                        onValueChange = { tempDaysElapsed = it },
                        label = { Text("Days Elapsed so far (e.g. 30)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        tempDisbursement.toDoubleOrNull()?.let {
                            vm.customSemesterDisbursementMinor.value = (it * 100).toLong()
                        }
                        tempSemesterDays.toIntOrNull()?.let { vm.semesterTotalDays.value = it }
                        tempDaysElapsed.toIntOrNull()?.let { vm.semesterDaysElapsed.value = it }
                        showSemesterSettingsDialog = false
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSemesterSettingsDialog = false }) { Text("Cancel") }
            }
        )
    }
        }
    }

// ==========================================
// SUB-VIEWS FOR EACH TAB
// ==========================================

@Composable
private fun SpendingTab(
    vm: ExpenseViewModel,
    breathingRoom: BreathingRoomState,
    syncedMpesaBalance: Pair<Long?, Long>,
    groupedExpenses: Map<String, List<Expense>>,
    filteredExpenses: List<Expense>,
    allExpenses: List<Expense>,
    anomalies: List<com.example.expensetracker.ai.Anomaly>,
    searchQuery: String,
    categoryFilter: Category?,
    syncMessage: String?,
    duplicateCount: Int,
    onCleanDuplicates: () -> Unit,
    formatMoney: (Long) -> String,
    onSyncClick: () -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Fintech Digital Wallet Hero Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                if (breathingRoom.isCritical)
                                    listOf(Color(0xFF7F1D1D), Color(0xFF450A0A))
                                else
                                    listOf(Color(0xFF065F46), Color(0xFF042F2E))
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "🌿 BREATHING ROOM",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.8f),
                                letterSpacing = 1.sp
                            )
                            Surface(
                                color = if (breathingRoom.isCritical) MoneyExpense.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    if (breathingRoom.isCritical) "⚠️ OVERDRAWN" else "🟢 SAFE LIMIT",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Text(
                            formatMoney(breathingRoom.breathingRoomMinor),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )

                        LinearProgressIndicator(
                            progress = { breathingRoom.spentPercentage },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = if (breathingRoom.isCritical) MoneyExpense else Color(0xFF34D399),
                            trackColor = Color.White.copy(alpha = 0.2f)
                        )

                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                "Safe Daily: ${formatMoney(breathingRoom.dailyBreathingRoomMinor)} / day",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            Text(
                                "${(breathingRoom.spentPercentage * 100).toInt()}% used",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }

                        HorizontalDivider(Modifier.padding(vertical = 4.dp), color = Color.White.copy(alpha = 0.15f))

                        // Mini metrics row
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(Modifier.weight(1f)) {
                                Text("Inflow", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                                Text("+${formatMoney(breathingRoom.totalIncomeMinor)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6EE7B7), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Spent", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                                Text("-${formatMoney(breathingRoom.totalSpentMinor)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                Text("Mobile Loans", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                                Text(formatMoney(breathingRoom.totalActiveLoansMinor), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFCA5A5), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }

                        syncedMpesaBalance.first?.let { balance ->
                            HorizontalDivider(Modifier.padding(vertical = 4.dp), color = Color.White.copy(alpha = 0.15f))
                            Surface(
                                color = Color.White.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("📱 Live Synced M-Pesa Balance", fontSize = 11.sp, color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.Medium)
                                    Text("KES %,.2f".format(balance / 100.0), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6EE7B7))
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            val ctx = LocalContext.current
            if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("🔐", fontSize = 20.sp)
                            Text("SMS Permission Required for Auto-Sync", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Text(
                            "PesaPulse reads your M-Pesa & bank messages directly from your device to automatically track expenses and sync your live M-Pesa balance offline. Your data stays 100% private.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Button(
                            onClick = onSyncClick,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PesaGreen)
                        ) {
                            Text("Grant SMS Permission", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Duplicate Transactions Banner
        if (duplicateCount > 0) {
            item {
                Surface(
                    color = Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("⚠️", fontSize = 18.sp)
                            Column {
                                Text("Duplicate Transactions Detected", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF92400E))
                                Text("$duplicateCount potential duplicate(s) found.", fontSize = 11.sp, color = Color(0xFF78350F))
                            }
                        }
                        Button(
                            onClick = onCleanDuplicates,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("🧹 Clean", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        // SMS Sync & Fast Simulators Card
        item {
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("📱 Live Messages App & Notification Reader", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("Reads real transactions directly from Messages app & bank alerts", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = onSyncClick,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Read Messages App", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(
                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(onClick = onSyncClick, shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)) { Text("🔄 Re-sync Messages", fontSize = 11.sp) }
                    }

                    syncMessage?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Anomalies / Spending Insights Alert
        if (anomalies.isNotEmpty()) {
            item {
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f))
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("⚠️", fontSize = 16.sp)
                            Text("Unusual Spending Alerts", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        anomalies.take(2).forEach {
                            Text("• ${it.message}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        // Search & Filter
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { vm.searchQuery.value = it },
                    placeholder = { Text("🔍 Search merchant, note, or code...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = categoryFilter == null,
                        onClick = { vm.categoryFilter.value = null },
                        label = { Text("All (${allExpenses.size})", fontSize = 11.sp) },
                        shape = RoundedCornerShape(10.dp)
                    )
                    Category.entries.forEach { cat ->
                        FilterChip(
                            selected = categoryFilter == cat,
                            onClick = { vm.categoryFilter.value = if (categoryFilter == cat) null else cat },
                            label = { Text("${getCategoryIcon(cat)} ${cat.pretty()}", fontSize = 11.sp) },
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }
        }

        // Grouped Transactions List
        if (filteredExpenses.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(36.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("🍃", fontSize = 42.sp)
                        Text("No transactions logged yet", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
                        Text("Tap '+' or 'Sync Inbox' to start tracking", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        } else {
            groupedExpenses.forEach { (dateGroup, itemsInGroup) ->
                item {
                    Text(
                        text = "📅 $dateGroup",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                    )
                }
                items(itemsInGroup, key = { it.id }) { expense ->
                    ModernExpenseRow(
                        expense = expense,
                        formatMoney = formatMoney,
                        onDelete = { vm.delete(expense) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AiInsightsTab(
    vm: ExpenseViewModel,
    allExpenses: List<Expense>,
    totalsByCategory: List<CategoryTotal>,
    healthReport: FinancialHealthReport,
    runwayForecast: RunwayForecast,
    anomalies: List<Anomaly>,
    formatMoney: (Long) -> String
) {
    val totalExpensesMinor = allExpenses.filter { !it.isIncome }.sumOf { it.amountMinor }
    val totalIncomeMinor = allExpenses.filter { it.isIncome }.sumOf { it.amountMinor }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // 1. AI Financial Health Score Card
        item {
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "🧠 AI Financial Health Index",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                healthReport.status,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = when (healthReport.grade) {
                                "A+", "A" -> Color(0xFF10B981)
                                "B" -> Color(0xFF3B82F6)
                                "C" -> Color(0xFFF59E0B)
                                else -> Color(0xFFEF4444)
                            }.copy(alpha = 0.15f)
                        ) {
                            Text(
                                "Grade ${healthReport.grade}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = when (healthReport.grade) {
                                    "A+", "A" -> Color(0xFF047857)
                                    "B" -> Color(0xFF1D4ED8)
                                    "C" -> Color(0xFFB45309)
                                    else -> Color(0xFFB91C1C)
                                },
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // Score Progress Meter
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "${healthReport.totalScore}",
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Column(Modifier.weight(1f)) {
                            Text("Composite Score (out of 100)", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            Spacer(Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { (healthReport.totalScore / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                                color = PesaGreen,
                                trackColor = PesaGreen.copy(alpha = 0.2f)
                            )
                        }
                    }

                    // Dynamic AI Advice Callout
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("💡", fontSize = 20.sp)
                            Column(Modifier.weight(1f)) {
                                Text("AI Dynamic Recommendation", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(healthReport.dynamicAiRecommendation, fontSize = 12.sp, lineHeight = 16.sp)
                            }
                        }
                    }

                    // 5 Evaluation Pillars
                    Text("Evaluation Pillars", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
                    healthReport.pillars.forEach { pillar ->
                        val ratio = if (pillar.maxScore > 0) pillar.score.toFloat() / pillar.maxScore else 0f
                        Column(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    pillar.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("${pillar.score}/${pillar.maxScore} pts", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(3.dp))
                            LinearProgressIndicator(
                                progress = { ratio.coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)),
                                color = if (ratio >= 0.7f) PesaGreen else if (ratio >= 0.4f) Color(0xFFF59E0B) else Color(0xFFEF4444)
                            )
                        }
                    }
                }
            }
        }

        // 2. Predictive Budget Runway & Depletion Forecast (Linear Regression)
        item {
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "📈 Predictive Budget Runway",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when (runwayForecast.trend) {
                                BurnTrend.ACCELERATING -> Color(0xFFEF4444).copy(alpha = 0.15f)
                                BurnTrend.ON_TRACK -> Color(0xFF10B981).copy(alpha = 0.15f)
                                BurnTrend.SURPLUS -> Color(0xFF3B82F6).copy(alpha = 0.15f)
                            }
                        ) {
                            Text(
                                when (runwayForecast.trend) {
                                    BurnTrend.ACCELERATING -> "⚠️ Fast Burn"
                                    BurnTrend.ON_TRACK -> "⚖️ On Track"
                                    BurnTrend.SURPLUS -> "🛡️ Surplus"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (runwayForecast.trend) {
                                    BurnTrend.ACCELERATING -> Color(0xFFB91C1C)
                                    BurnTrend.ON_TRACK -> Color(0xFF047857)
                                    BurnTrend.SURPLUS -> Color(0xFF1D4ED8)
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text("Current Daily Burn", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            Text(formatMoney(runwayForecast.dailyBurnRateMinor), fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                            Text("Safe Target Limit", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            Text(formatMoney(runwayForecast.targetSafeDailyLimitMinor), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = PesaGreen, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }

                    runwayForecast.predictedDepletionDate?.let { date ->
                        val locale = LocalConfiguration.current.locales[0]
                        val dateFmt = SimpleDateFormat("dd MMMM yyyy", locale).format(date)
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("⏳", fontSize = 18.sp)
                                Column(Modifier.weight(1f)) {
                                    Text("Projected Depletion Date", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                    Text("$dateFmt (${runwayForecast.daysOfRunwayRemaining} days runway)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    Text(runwayForecast.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("OLS Regression Fit: R² = ${"%.2f".format(runwayForecast.rSquared)}", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                        Text("Model: Time-Series Linear Trend", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }

        // 3. Statistical Spending Anomaly Radar
        item {
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("🔍 Spending Anomaly Radar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    if (anomalies.isEmpty()) {
                        Surface(
                            color = Color(0xFF10B981).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("✅", fontSize = 18.sp)
                                Text("Zero anomalies detected. All spending conforms to expected Gaussian category distributions.", fontSize = 12.sp, modifier = Modifier.weight(1f))
                            }
                        }
                    } else {
                        anomalies.forEach { anom ->
                            Surface(
                                color = Color(0xFFEF4444).copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text("⚠️", fontSize = 18.sp)
                                    Column(Modifier.weight(1f)) {
                                        Text(anom.message, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                        Text("Deviation: ${"%.1f".format(anom.zScore)}σ • Amount: ${formatMoney(anom.expense.amountMinor)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                    }
                                    if (anom.message.contains("duplicate", ignoreCase = true)) {
                                        OutlinedButton(
                                            onClick = { vm.delete(anom.expense) },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Delete", fontSize = 11.sp, color = MoneyExpense)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. On-Device AI Engine & Sandbox Inspector
        item {
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🤖 On-Device AI Telemetry", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PesaGreen.copy(alpha = 0.15f)
                        ) {
                            Text("Active • Edge ML", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PesaGreen, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("• NLP Model: Multinomial Naive Bayes (Laplace smoothed)", fontSize = 12.sp)
                        Text("• Computer Vision: Google ML Kit On-Device Text OCR", fontSize = 12.sp)
                        Text("• Forecasting: Ordinary Least Squares (OLS) Linear Trend", fontSize = 12.sp)
                        Text("• Online Learning: Bayesian prior updating on user edits", fontSize = 12.sp)
                    }
                }
            }
        }

        // 5. Cashflow Summary
        item {
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("📊 Cashflow Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Surface(
                            color = MoneyIncome.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f).padding(end = 6.dp)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text("Total Inflow", style = MaterialTheme.typography.labelSmall, color = MoneyIncome)
                                Text(formatMoney(totalIncomeMinor), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MoneyIncome, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }

                        Surface(
                            color = MoneyExpense.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f).padding(start = 6.dp)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text("Total Outflow", style = MaterialTheme.typography.labelSmall, color = MoneyExpense)
                                Text(formatMoney(totalExpensesMinor), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MoneyExpense, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        }

        // 6. Spending Distribution
        item {
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("🏷️ Spending Distribution", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    if (totalsByCategory.isEmpty()) {
                        Text("No expense categories to show yet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    } else {
                        totalsByCategory.sortedByDescending { it.total }.forEach { catTotal ->
                            val pct = if (totalExpensesMinor > 0) {
                                (catTotal.total.toFloat() / totalExpensesMinor).coerceIn(0f, 1f)
                            } else 0f
                            val catColor = getCategoryColor(catTotal.category)

                            Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(getCategoryIcon(catTotal.category), fontSize = 16.sp)
                                        Text(
                                            catTotal.category.pretty(),
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "${formatMoney(catTotal.total)} (${(pct * 100).toInt()}%)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { pct },
                                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                    color = catColor,
                                    trackColor = catColor.copy(alpha = 0.15f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SemesterCoachTab(
    vm: ExpenseViewModel,
    semesterBudget: SemesterBudgetState,
    financialTips: List<String>,
    formatMoney: (Long) -> String,
    onAdjustClick: () -> Unit
) {
    val geminiApiKey by vm.geminiApiKey.collectAsStateWithLifecycle()
    val isAiLoading by vm.isAiLoading.collectAsStateWithLifecycle()
    val aiChatMessages by vm.aiChatMessages.collectAsStateWithLifecycle()
    var userQuery by remember { mutableStateOf("") }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var tempKeyInput by remember { mutableStateOf(geminiApiKey) }

    if (showApiKeyDialog) {
        AlertDialog(
            onDismissRequest = { showApiKeyDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🔑", fontSize = 20.sp)
                    Text("Gemini AI Setup", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Enter your Google Gemini API key to activate cloud generative AI responses. If left empty, PesaPouch Edge AI operates 100% offline.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    OutlinedTextField(
                        value = tempKeyInput,
                        onValueChange = { tempKeyInput = it },
                        placeholder = { Text("AIzaSy...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    if (geminiApiKey.isNotBlank()) {
                        TextButton(
                            onClick = {
                                tempKeyInput = ""
                                vm.setGeminiApiKey("")
                                showApiKeyDialog = false
                            }
                        ) {
                            Text("Clear Stored Key", color = MoneyExpense)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        vm.setGeminiApiKey(tempKeyInput)
                        showApiKeyDialog = false
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save Key")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApiKeyDialog = false }) { Text("Cancel") }
            }
        )
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // AI Financial Coach Interactive Card
        item {
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                            Text("🤖", fontSize = 24.sp)
                            Column {
                                Text("PesaPouch AI Advisor", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    if (geminiApiKey.isNotBlank()) "⚡ Gemini 2.5 Flash Online" else "🧠 Edge AI On-Device",
                                    fontSize = 11.sp,
                                    color = if (geminiApiKey.isNotBlank()) PesaGreen else MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(
                                onClick = {
                                    tempKeyInput = geminiApiKey
                                    showApiKeyDialog = true
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(if (geminiApiKey.isNotBlank()) "🔑 Key ✓" else "🔑 Key", fontSize = 11.sp)
                            }
                            Button(
                                onClick = { vm.requestAiFinancialAudit() },
                                shape = RoundedCornerShape(10.dp),
                                enabled = !isAiLoading,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("✨ Audit", fontSize = 11.sp)
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    // Conversation History
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        aiChatMessages.takeLast(6).forEach { msg ->
                            val isUser = msg.isFromUser
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(
                                        topStart = 14.dp,
                                        topEnd = 14.dp,
                                        bottomStart = if (isUser) 14.dp else 2.dp,
                                        bottomEnd = if (isUser) 2.dp else 14.dp
                                    ),
                                    color = if (isUser) PesaGreen else MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.widthIn(max = 280.dp)
                                ) {
                                    Text(
                                        text = msg.message,
                                        fontSize = 12.sp,
                                        color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }

                        if (isAiLoading) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(6.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = PesaGreen
                                )
                                Text("PesaPouch AI is analyzing your finances...", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }

                    // Quick Prompt Chips
                    Text("💡 Quick AI Advice:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
                    Row(
                        Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            onClick = { vm.sendAiMessage("Run a comprehensive financial health audit and tell me my biggest spending leak.") },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text("📊 Budget Audit", fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                        Surface(
                            onClick = { vm.sendAiMessage("How can I stretch my remaining semester allowance to survive through finals week?") },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text("🎓 Stretch Budget", fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                        Surface(
                            onClick = { vm.sendAiMessage("Give me a step-by-step strategy to eliminate my mobile loans and avoid Fuliza.") },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text("🚨 Clear Mobile Debt", fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                        Surface(
                            onClick = { vm.sendAiMessage("How do I structure a weekly campus Chama goal with my friends?") },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text("🤝 Chama Strategy", fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }

                    // Query Input Field
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = userQuery,
                            onValueChange = { userQuery = it },
                            placeholder = { Text("Ask anything about budget, Fuliza...", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                if (userQuery.isNotBlank()) {
                                    val text = userQuery
                                    userQuery = ""
                                    vm.sendAiMessage(text)
                                }
                            },
                            enabled = userQuery.isNotBlank() && !isAiLoading,
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            Text("Send", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Card 2: Semester Budget Pacing
        item {
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("🎓 Semester Budget Pacing", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("Survive all 120 days until exam week", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Spacer(Modifier.width(8.dp))
                        OutlinedButton(onClick = onAdjustClick, shape = RoundedCornerShape(10.dp), contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)) {
                            Text("⚙️ Adjust", fontSize = 12.sp)
                        }
                    }

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text("Loan Disbursed", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Text(formatMoney(semesterBudget.totalDisbursementMinor), fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total Spent", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Text(formatMoney(semesterBudget.totalSpentMinor), fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                            Text("Remaining", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Text(formatMoney(semesterBudget.remainingMinor), fontWeight = FontWeight.Bold, color = PesaGreen, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }

                    Spacer(Modifier.height(4.dp))
                    Text("Semester Timeline (${semesterBudget.daysElapsed} of ${semesterBudget.totalDays} days elapsed):", fontSize = 12.sp)
                    LinearProgressIndicator(
                        progress = { semesterBudget.timePercentage },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Text("Budget Burned (${(semesterBudget.spentPercentage * 100).toInt()}% used):", fontSize = 12.sp)
                    LinearProgressIndicator(
                        progress = { semesterBudget.spentPercentage },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = if (semesterBudget.isPacingWell) PesaGreen else MoneyExpense
                    )

                    HorizontalDivider(Modifier.padding(vertical = 4.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text("Safe Daily Spend:", style = MaterialTheme.typography.labelSmall)
                            Text("${formatMoney(semesterBudget.dailyBudgetRecommendedMinor)} / day", fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                            Text("Actual Daily Burn:", style = MaterialTheme.typography.labelSmall)
                            Text(
                                "${formatMoney(semesterBudget.currentDailyBurnRateMinor)} / day",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (semesterBudget.isPacingWell) PesaGreen else MoneyExpense,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Surface(
                        color = if (semesterBudget.isPacingWell) PesaGreen.copy(alpha = 0.12f) else MoneyExpense.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (semesterBudget.isPacingWell)
                                "✅ Great pacing! You are on track to have funds lasting through finals."
                            else
                                "⚠️ Warning: Spending faster than recommended. Reduce non-essential dining/airtime to avoid going broke before end-of-semester.",
                            fontSize = 12.sp,
                            modifier = Modifier.padding(10.dp),
                            color = if (semesterBudget.isPacingWell) PesaGreenDark else MoneyExpense
                        )
                    }
                }
            }
        }

        // Card 3: Financial Literacy Tips
        item {
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("💡 Financial Literacy for Campus", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    financialTips.forEach { tip ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                        ) {
                            Text(tip, fontSize = 12.sp, modifier = Modifier.padding(10.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChamaLoansTab(
    vm: ExpenseViewModel,
    chamaGoals: List<ChamaGoal>,
    mobileLoans: List<MobileLoan>,
    chamaTitle: String,
    onChamaTitleChange: (String) -> Unit,
    chamaTarget: String,
    onChamaTargetChange: (String) -> Unit,
    chamaDeadline: String,
    onChamaDeadlineChange: (String) -> Unit,
    loanProvider: String,
    onLoanProviderChange: (String) -> Unit,
    loanPrincipal: String,
    onLoanPrincipalChange: (String) -> Unit,
    loanDailyRate: String,
    onLoanDailyRateChange: (String) -> Unit,
    formatMoney: (Long) -> String
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Create Chama
        item {
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🤝 Campus Chama & Savings Goal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = chamaTitle,
                        onValueChange = onChamaTitleChange,
                        label = { Text("Goal Title (e.g. Laptop Fund, Rent Chama)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = chamaTarget,
                        onValueChange = onChamaTargetChange,
                        label = { Text("Target Amount (KES)") },
                        prefix = { Text("KES ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = chamaDeadline,
                        onValueChange = onChamaDeadlineChange,
                        label = { Text("Target Deadline") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Button(
                        onClick = {
                            if (vm.addChamaGoal(chamaTitle, chamaTarget, chamaDeadline)) {
                                onChamaTitleChange(""); onChamaTargetChange("")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ChamaViolet)
                    ) {
                        Text("Add Chama Goal")
                    }
                }
            }
        }

        items(chamaGoals, key = { it.id }) { goal ->
            val progress = if (goal.targetAmountMinor > 0) {
                (goal.currentSavedMinor.toFloat() / goal.targetAmountMinor).coerceIn(0f, 1f)
            } else 0f
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(goal.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Spacer(Modifier.width(8.dp))
                        Text("📅 ${goal.deadline}", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    }
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = ChamaViolet,
                        trackColor = ChamaViolet.copy(alpha = 0.2f)
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Saved: ${formatMoney(goal.currentSavedMinor)} / ${formatMoney(goal.targetAmountMinor)}", fontSize = 12.sp)
                        Text("${(progress * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ChamaViolet)
                    }
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(onClick = { vm.contributeChamaGoal(goal, "200") }, shape = RoundedCornerShape(8.dp)) { Text("+200") }
                        Button(onClick = { vm.contributeChamaGoal(goal, "500") }, shape = RoundedCornerShape(8.dp)) { Text("+500") }
                        Button(onClick = { vm.contributeChamaGoal(goal, "1000") }, shape = RoundedCornerShape(8.dp)) { Text("+1,000") }
                        TextButton(onClick = { vm.deleteChamaGoal(goal) }) { Text("Delete") }
                    }
                }
            }
        }

        // Mobile Loan Tracker
        item {
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("📱 Mobile Loan Tracker (Fuliza / Hustler Fund)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Tracks daily compound interest so you don't get trapped.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)

                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Fuliza", "M-Shwari", "Hustler Fund", "KCB M-Pesa").forEach { p ->
                            FilterChip(selected = loanProvider == p, onClick = { onLoanProviderChange(p) }, label = { Text(p, fontSize = 12.sp) }, shape = RoundedCornerShape(8.dp))
                        }
                    }

                    OutlinedTextField(
                        value = loanPrincipal,
                        onValueChange = onLoanPrincipalChange,
                        label = { Text("Principal Amount (KES)") },
                        prefix = { Text("KES ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = loanDailyRate,
                        onValueChange = onLoanDailyRateChange,
                        label = { Text("Daily Interest % (e.g. 0.5%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Button(
                        onClick = {
                            val rate = loanDailyRate.toDoubleOrNull() ?: 0.5
                            if (vm.addMobileLoan(loanProvider, loanPrincipal, rate)) {
                                onLoanPrincipalChange("")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MoneyExpense)
                    ) {
                        Text("Record Active Mobile Loan")
                    }
                }
            }
        }

        items(mobileLoans, key = { it.id }) { loan ->
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (loan.isRepaid) MaterialTheme.colorScheme.surface else MoneyExpense.copy(alpha = 0.08f)
                )
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${loan.provider} Facility", fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            color = if (loan.isRepaid) PesaGreen.copy(alpha = 0.15f) else MoneyExpense.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                if (loan.isRepaid) "REPAID ✅" else "ACTIVE ⚠️",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (loan.isRepaid) PesaGreenDark else MoneyExpense,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text("Principal: ${formatMoney(loan.principalMinor)} | Active: ${loan.daysActive} days", fontSize = 12.sp)
                    Text("Accrued Interest: ${formatMoney(loan.totalInterestMinor)} (@ ${loan.dailyInterestRatePercent}%/day)", fontSize = 12.sp)
                    Text("Total Due: ${formatMoney(loan.totalDueMinor)}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { vm.toggleLoanRepaid(loan) }, shape = RoundedCornerShape(8.dp)) {
                            Text(if (loan.isRepaid) "Mark Active" else "Mark Fully Repaid", fontSize = 12.sp)
                        }
                        TextButton(onClick = { vm.deleteLoan(loan) }) { Text("Delete") }
                    }
                }
            }
        }
    }
}

@Composable
private fun SideHustleTab(
    vm: ExpenseViewModel,
    sideHustles: List<com.example.expensetracker.data.SideHustleTransaction>,
    hustleName: String,
    onHustleNameChange: (String) -> Unit,
    hustleAmt: String,
    onHustleAmtChange: (String) -> Unit,
    hustleDesc: String,
    onHustleDescChange: (String) -> Unit,
    hustleIsIncome: Boolean,
    onHustleIsIncomeChange: (Boolean) -> Unit,
    formatMoney: (Long) -> String
) {
    val totalRev = sideHustles.filter { it.isIncome }.sumOf { it.amountMinor }
    val totalExp = sideHustles.filter { !it.isIncome }.sumOf { it.amountMinor }
    val netProfit = totalRev - totalExp
    val marginPct = if (totalRev > 0) ((netProfit.toDouble() / totalRev) * 100).toInt() else 0

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("💼 Campus Side Hustle Bookkeeping", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = hustleName,
                        onValueChange = onHustleNameChange,
                        label = { Text("Business Name (e.g. Hostel Wi-Fi, Smokie Pasua)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = hustleAmt,
                        onValueChange = onHustleAmtChange,
                        label = { Text("Amount (KES)") },
                        prefix = { Text("KES ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = hustleDesc,
                        onValueChange = onHustleDescChange,
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = hustleIsIncome, onClick = { onHustleIsIncomeChange(true) }, label = { Text("Revenue 📈") }, shape = RoundedCornerShape(8.dp))
                        FilterChip(selected = !hustleIsIncome, onClick = { onHustleIsIncomeChange(false) }, label = { Text("Expense 📉") }, shape = RoundedCornerShape(8.dp))
                    }
                    Button(
                        onClick = {
                            if (vm.addSideHustle(hustleName, hustleIsIncome, hustleAmt, hustleDesc)) {
                                onHustleNameChange(""); onHustleAmtChange(""); onHustleDescChange("")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Record Business Transaction")
                    }
                }
            }
        }

        item {
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("📊 Business Profit & Loss", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Revenue: ${formatMoney(totalRev)}", fontSize = 13.sp, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("Costs: ${formatMoney(totalExp)}", fontSize = 13.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.End, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Net Profit: ${formatMoney(netProfit)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (netProfit >= 0) PesaGreen else MoneyExpense, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("Margin: $marginPct%", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        items(sideHustles, key = { it.id }) { tx ->
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(tx.businessName, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(tx.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "${if (tx.isIncome) "+" else "-"}${formatMoney(tx.amountMinor)}",
                            fontWeight = FontWeight.Bold,
                            color = if (tx.isIncome) PesaGreen else MoneyExpense
                        )
                        TextButton(onClick = { vm.deleteSideHustle(tx) }) { Text("Delete", fontSize = 11.sp) }
                    }
                }
            }
        }
    }
}

@Composable
private fun BillsDebtsTab(
    vm: ExpenseViewModel,
    context: Context,
    recurringBills: List<RecurringBill>,
    peerDebts: List<PeerDebt>,
    billTitle: String,
    onBillTitleChange: (String) -> Unit,
    billAmt: String,
    onBillAmtChange: (String) -> Unit,
    billDay: String,
    onBillDayChange: (String) -> Unit,
    peerName: String,
    onPeerNameChange: (String) -> Unit,
    peerAmount: String,
    onPeerAmountChange: (String) -> Unit,
    peerDesc: String,
    onPeerDescChange: (String) -> Unit,
    isOwedToMe: Boolean,
    onIsOwedToMeChange: (Boolean) -> Unit,
    formatMoney: (Long) -> String
) {
    val owedToMeTotal = peerDebts.filter { it.isOwedToMe && !it.isSettled }.sumOf { it.amountMinor }
    val iOweTotal = peerDebts.filter { !it.isOwedToMe && !it.isSettled }.sumOf { it.amountMinor }
    val netPeerBalance = owedToMeTotal - iOweTotal

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Bills
        item {
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("📅 Recurring Bills (Rent / Tokens / Wi-Fi)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = billTitle,
                        onValueChange = onBillTitleChange,
                        label = { Text("Bill Title (e.g. Hostel Rent, KPLC)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = billAmt,
                        onValueChange = onBillAmtChange,
                        label = { Text("Amount (KES)") },
                        prefix = { Text("KES ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = billDay,
                        onValueChange = onBillDayChange,
                        label = { Text("Due Day of Month (1 - 31)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Button(
                        onClick = {
                            if (vm.addRecurringBill(billTitle, billAmt, billDay.toIntOrNull() ?: 5, Category.RENT_UTILITIES)) {
                                onBillTitleChange(""); onBillAmtChange("")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Add Recurring Bill")
                    }
                }
            }
        }

        items(recurringBills, key = { it.id }) { bill ->
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    Modifier.padding(14.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(bill.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("Due every month on day ${bill.dueDayOfMonth}", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                        Text(formatMoney(bill.amountMinor), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Row {
                        OutlinedButton(onClick = { vm.payRecurringBill(bill) }, shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)) {
                            Text("Pay & Record", fontSize = 11.sp)
                        }
                        TextButton(onClick = { vm.deleteRecurringBill(bill) }) { Text("Delete", fontSize = 11.sp) }
                    }
                }
            }
        }

        // Peer Debts Summary
        item {
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("🤝 Peer Debts & Bill Splits", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("They Owe You: ${formatMoney(owedToMeTotal)}", fontSize = 13.sp, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("You Owe: ${formatMoney(iOweTotal)}", fontSize = 13.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.End, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Text(
                        if (netPeerBalance >= 0) "Net: +${formatMoney(netPeerBalance)} (You are owed)" else "Net: -${formatMoney(-netPeerBalance)} (You owe)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (netPeerBalance >= 0) PesaGreen else MoneyExpense,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Add Peer Debt
        item {
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Track Peer Debt", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = peerName,
                        onValueChange = onPeerNameChange,
                        label = { Text("Friend's Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = peerAmount,
                        onValueChange = onPeerAmountChange,
                        label = { Text("Amount (KES)") },
                        prefix = { Text("KES ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = peerDesc,
                        onValueChange = onPeerDescChange,
                        label = { Text("Reason (e.g. Lunch split, Fare)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = isOwedToMe, onClick = { onIsOwedToMeChange(true) }, label = { Text("They Owe Me 🟢") }, shape = RoundedCornerShape(8.dp))
                        FilterChip(selected = !isOwedToMe, onClick = { onIsOwedToMeChange(false) }, label = { Text("I Owe Them 🔴") }, shape = RoundedCornerShape(8.dp))
                    }
                    Button(
                        onClick = {
                            if (vm.addPeerDebt(peerName, peerAmount, peerDesc, isOwedToMe)) {
                                onPeerNameChange(""); onPeerAmountChange(""); onPeerDescChange("")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save Peer Debt")
                    }
                }
            }
        }

        items(peerDebts, key = { it.id }) { debt ->
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            "${debt.peerName} (${if (debt.isOwedToMe) "Owes You" else "You Owe"})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            formatMoney(debt.amountMinor),
                            fontWeight = FontWeight.Bold,
                            color = if (debt.isOwedToMe) PesaGreen else MoneyExpense
                        )
                    }
                    Text(debt.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text("Status: ${if (debt.isSettled) "Settled ✅" else "Pending ⏳"}", fontSize = 11.sp)

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row {
                            TextButton(onClick = { vm.toggleDebtSettled(debt) }) {
                                Text(if (debt.isSettled) "Unsettle" else "Mark Settled", fontSize = 11.sp)
                            }
                            TextButton(onClick = { vm.deleteDebt(debt) }) { Text("Delete", fontSize = 11.sp) }
                        }
                        if (debt.isOwedToMe && !debt.isSettled) {
                            OutlinedButton(
                                onClick = {
                                    val msg = "Hey ${debt.peerName}, just a friendly reminder regarding the KES ${debt.amountMinor / 100.0} for ${debt.description} 😊"
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        putExtra(Intent.EXTRA_TEXT, msg)
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Send Reminder"))
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("💬 WhatsApp", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModernExpenseRow(
    expense: Expense,
    formatMoney: (Long) -> String,
    onDelete: () -> Unit
) {
    val dateStr = remember(expense.timestamp) {
        val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
        sdf.format(Date(expense.timestamp))
    }
    val catColor = getCategoryColor(expense.category)
    val catIcon = getCategoryIcon(expense.category)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            Modifier
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Circular Category Avatar
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(catColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(catIcon, fontSize = 18.sp)
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        expense.merchant,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            expense.category.pretty(),
                            fontSize = 11.sp,
                            color = catColor,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Text("•", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                        Text(dateStr, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)

                        val (sourceText, sourceBg, sourceFg) = when (expense.categorySource) {
                            CategorySource.MODEL -> Triple("🤖 AI", Color(0xFF10B981).copy(alpha = 0.15f), Color(0xFF047857))
                            CategorySource.RULES -> Triple("⚡ Rule", Color(0xFFF59E0B).copy(alpha = 0.15f), Color(0xFFB45309))
                            CategorySource.USER -> Triple("👤 User", Color(0xFF6B7280).copy(alpha = 0.15f), Color(0xFF374151))
                        }
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = sourceBg
                        ) {
                            Text(
                                sourceText,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = sourceFg,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    if (expense.note.isNotBlank()) {
                        Text(
                            expense.note,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (expense.isIncome) "+" else "-"}${formatMoney(expense.amountMinor)}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = if (expense.isIncome) MoneyIncome else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "Delete",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .clickable { onDelete() }
                        .padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun IconButtonBox(emoji: String, tooltip: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(emoji, fontSize = 15.sp)
    }
}

private fun Category.pretty() = name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }

package com.example.expensetracker.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.expensetracker.ExpenseApp
import com.example.expensetracker.ai.TransactionParser
import com.example.expensetracker.data.Category
import com.example.expensetracker.data.CategorySource
import com.example.expensetracker.data.Expense
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Intercepts banking push notifications and automatically records expenses.
 */
class BankNotificationListenerService : NotificationListenerService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val extras = sbn.notification.extras
        val title = extras.getCharSequence("android.title")?.toString() ?: ""
        val text = extras.getCharSequence("android.text")?.toString() ?: ""
        val combined = "$title: $text"

        if (isTransactionNotification(combined)) {
            val app = applicationContext as? ExpenseApp ?: return
            scope.launch {
                val parser = TransactionParser(app.container.categorizer)
                val parsed = parser.parse(combined)
                if (parsed != null) {
                    val code = parsed.transactionCode ?: "NOTIF-${sbn.postTime}-${parsed.amountMinor}"
                    if (app.container.repository.getByTransactionCode(code) != null) {
                        return@launch
                    }
                    app.container.repository.add(
                        Expense(
                            amountMinor = parsed.amountMinor,
                            currency = parsed.currency,
                            merchant = parsed.merchant,
                            category = parsed.category,
                            categorySource = parsed.source,
                            note = "Auto-captured from notification (${sbn.packageName})",
                            timestamp = sbn.postTime,
                            transactionCode = code,
                            isIncome = parsed.isIncome
                        )
                    )
                    if (parsed.feeMinor > 0) {
                        val feeCode = "${code}-FEE"
                        if (app.container.repository.getByTransactionCode(feeCode) == null) {
                            app.container.repository.add(
                                Expense(
                                    amountMinor = parsed.feeMinor,
                                    currency = parsed.currency,
                                    merchant = "[Bank Fee]",
                                    category = Category.FEES,
                                    categorySource = CategorySource.RULES,
                                    note = "Transaction fee from notification",
                                    timestamp = sbn.postTime,
                                    transactionCode = feeCode,
                                    isIncome = false
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    private fun isTransactionNotification(text: String): Boolean {
        val lower = text.lowercase()
        return listOf("spent", "paid", "bought", "debited", "charged", "received", "credited", "ksh", "kes", "$", "usd").any { it in lower }
    }
}

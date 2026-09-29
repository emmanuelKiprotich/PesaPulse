package com.example.expensetracker.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.expensetracker.ExpenseApp
import com.example.expensetracker.ai.TransactionParser
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
                    app.container.repository.add(
                        com.example.expensetracker.data.Expense(
                            amountMinor = parsed.amountMinor,
                            currency = parsed.currency,
                            merchant = "[Notification] ${parsed.merchant}",
                            category = parsed.category,
                            categorySource = parsed.source,
                            note = "Auto-captured from notification (${sbn.packageName})"
                        )
                    )
                }
            }
        }
    }

    private fun isTransactionNotification(text: String): Boolean {
        val lower = text.lowercase()
        return listOf("spent", "paid", "bought", "debited", "charged", "received", "ksh", "kes", "$", "usd").any { it in lower }
    }
}

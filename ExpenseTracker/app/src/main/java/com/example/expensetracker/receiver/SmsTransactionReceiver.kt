package com.example.expensetracker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.example.expensetracker.ExpenseApp
import com.example.expensetracker.ai.TransactionParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Listens for incoming SMS messages from bank shortcodes, parses them, and saves expenses automatically.
 */
class SmsTransactionReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val app = context.applicationContext as? ExpenseApp ?: return
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return

        scope.launch {
            val parser = TransactionParser(app.container.categorizer)
            for (sms in messages) {
                val body = sms.messageBody ?: continue
                val sender = sms.originatingAddress ?: ""
                if (isFinancialMessage(body)) {
                    val parsed = parser.parse(body)
                    if (parsed != null) {
                        app.container.repository.add(
                            com.example.expensetracker.data.Expense(
                                amountMinor = parsed.amountMinor,
                                currency = parsed.currency,
                                merchant = "[SMS] ${parsed.merchant}",
                                category = parsed.category,
                                categorySource = parsed.source,
                                note = "Auto-captured from SMS ($sender)"
                            )
                        )
                    }
                }
            }
        }
    }

    private fun isFinancialMessage(body: String): Boolean {
        val lower = body.lowercase()
        return listOf("sent", "paid", "debited", "credited", "bought", "confirmed", "spend", "ksh", "kes", "usd", "$").any { it in lower }
    }
}

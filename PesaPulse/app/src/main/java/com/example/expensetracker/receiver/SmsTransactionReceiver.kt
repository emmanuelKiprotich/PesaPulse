package com.example.expensetracker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
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
                val timestamp = if (sms.timestampMillis > 0) sms.timestampMillis else System.currentTimeMillis()
                if (isFinancialMessage(body)) {
                    val parsed = parser.parse(body)
                    if (parsed != null) {
                        val code = parsed.transactionCode ?: TransactionParser.generateDeterministicCode(sender, body, parsed.amountMinor)
                        val expense = Expense(
                            amountMinor = parsed.amountMinor,
                            currency = parsed.currency,
                            merchant = parsed.merchant,
                            category = parsed.category,
                            categorySource = parsed.source,
                            note = "Auto-captured from SMS ($sender)",
                            timestamp = timestamp,
                            transactionCode = code,
                            isIncome = parsed.isIncome
                        )
                        val inserted = app.container.repository.add(expense)
                        if (inserted && parsed.feeMinor > 0) {
                            val feeCode = "$code-FEE"
                            val provider = if (body.contains("airtel", ignoreCase = true)) "Airtel Fee" else "M-Pesa Fee"
                            app.container.repository.add(
                                Expense(
                                    amountMinor = parsed.feeMinor,
                                    currency = parsed.currency,
                                    merchant = "[$provider]",
                                    category = Category.FEES,
                                    categorySource = CategorySource.RULES,
                                    note = "Transaction fee",
                                    timestamp = timestamp,
                                    transactionCode = feeCode,
                                    isIncome = false,
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    private fun isFinancialMessage(body: String): Boolean {
        val lower = body.lowercase()
        return listOf("sent", "paid", "debited", "credited", "bought", "confirmed", "spend", "ksh", "kes", "usd", "$", "disbursement").any { it in lower }
    }
}

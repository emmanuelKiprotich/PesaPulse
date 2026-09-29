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
                if (isFinancialMessage(body)) {
                    val parsed = parser.parse(body)
                    if (parsed != null) {
                        val code = parsed.transactionCode ?: "LIVE-SMS-${System.currentTimeMillis()}-${parsed.amountMinor}"
                        if (app.container.repository.getByTransactionCode(code) != null) {
                            continue
                        }
                        app.container.repository.add(
                            Expense(
                                amountMinor = parsed.amountMinor,
                                currency = parsed.currency,
                                merchant = parsed.merchant,
                                category = parsed.category,
                                categorySource = parsed.source,
                                note = "Auto-captured from SMS ($sender)",
                                timestamp = System.currentTimeMillis(),
                                transactionCode = code,
                                isIncome = parsed.isIncome
                            )
                        )
                        if (parsed.feeMinor > 0) {
                            val feeCode = "${code}-FEE"
                            if (app.container.repository.getByTransactionCode(feeCode) == null) {
                                val provider = if (body.contains("airtel", true)) "Airtel Fee" else "M-Pesa Fee"
                                app.container.repository.add(
                                    Expense(
                                        amountMinor = parsed.feeMinor,
                                        currency = parsed.currency,
                                        merchant = "[$provider]",
                                        category = Category.FEES,
                                        categorySource = CategorySource.RULES,
                                        note = "Transaction fee",
                                        timestamp = System.currentTimeMillis(),
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
    }

    private fun isFinancialMessage(body: String): Boolean {
        val lower = body.lowercase()
        return listOf("sent", "paid", "debited", "credited", "bought", "confirmed", "spend", "ksh", "kes", "usd", "$", "disbursement").any { it in lower }
    }
}

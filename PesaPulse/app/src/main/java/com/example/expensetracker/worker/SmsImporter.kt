package com.example.expensetracker.worker

import android.content.Context
import android.net.Uri
import com.example.expensetracker.ai.ExpenseCategorizer
import com.example.expensetracker.ai.TransactionParser
import com.example.expensetracker.data.Category
import com.example.expensetracker.data.CategorySource
import com.example.expensetracker.data.Expense
import com.example.expensetracker.data.ExpenseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Reads SMS transaction messages directly from the device's Messages app ContentProvider.
 * Captures M-Pesa, Airtel Money, and all major Kenyan bank transaction alerts.
 */
object SmsImporter {

    suspend fun syncInboxSms(
        context: Context,
        repository: ExpenseRepository,
        categorizer: ExpenseCategorizer
    ): Int = withContext(Dispatchers.IO) {
        val parser = TransactionParser(categorizer)
        val uri = Uri.parse("content://sms/inbox")
        val projection = arrayOf("body", "date", "address")

        var importedCount = 0
        try {
            val cursor = context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                "date DESC LIMIT 500"
            )
            cursor?.use {
                val bodyCol = it.getColumnIndex("body")
                val addressCol = it.getColumnIndex("address")
                val dateCol = it.getColumnIndex("date")

                while (it.moveToNext()) {
                    val body = if (bodyCol != -1) it.getString(bodyCol) else ""
                    val address = if (addressCol != -1) it.getString(addressCol) else ""
                    val date = if (dateCol != -1) it.getLong(dateCol) else System.currentTimeMillis()

                    if (isFinancialSenderOrMessage(address, body)) {
                        val parsed = parser.parse(body)
                        if (parsed != null) {
                            if (parsed.balanceMinor != null) {
                                saveMpesaBalance(context, parsed.balanceMinor, date)
                            }
                            val code = parsed.transactionCode
                                ?: TransactionParser.generateDeterministicCode(address, body, parsed.amountMinor)
                            val expense = Expense(
                                amountMinor = parsed.amountMinor,
                                currency = parsed.currency,
                                merchant = parsed.merchant,
                                category = parsed.category,
                                categorySource = parsed.source,
                                note = "Auto-synced from Messages ($address)",
                                timestamp = date,
                                transactionCode = code,
                                isIncome = parsed.isIncome,
                            )
                            val inserted = repository.add(expense)
                            if (inserted) {
                                importedCount++
                                if (parsed.feeMinor > 0) {
                                    val feeCode = "$code-FEE"
                                    val providerTag = if (address.contains("airtel", ignoreCase = true)) "Airtel Fee" else "M-Pesa Fee"
                                    repository.add(
                                        Expense(
                                            amountMinor = parsed.feeMinor,
                                            currency = parsed.currency,
                                            merchant = "[$providerTag]",
                                            category = Category.FEES,
                                            categorySource = CategorySource.RULES,
                                            note = "Transaction fee",
                                            timestamp = date,
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
        importedCount
    }

    fun saveMpesaBalance(context: Context, balanceMinor: Long, timestamp: Long) {
        val prefs = context.getSharedPreferences("pesapouch_mpesa_balance", Context.MODE_PRIVATE)
        val currentTs = prefs.getLong("latest_mpesa_balance_timestamp", 0L)
        if (timestamp >= currentTs) {
            prefs.edit()
                .putLong("latest_mpesa_balance_minor", balanceMinor)
                .putLong("latest_mpesa_balance_timestamp", timestamp)
                .apply()
        }
    }

    fun getMpesaBalance(context: Context): Pair<Long?, Long> {
        val prefs = context.getSharedPreferences("pesapouch_mpesa_balance", Context.MODE_PRIVATE)
        if (!prefs.contains("latest_mpesa_balance_minor")) return null to 0L
        val balance = prefs.getLong("latest_mpesa_balance_minor", 0L)
        val ts = prefs.getLong("latest_mpesa_balance_timestamp", 0L)
        return balance to ts
    }

    private fun isFinancialSenderOrMessage(address: String, body: String): Boolean {
        val upperAddress = address.uppercase()
        val lowerBody = body.lowercase()

        val isProvider = upperAddress.contains("MPESA") ||
                upperAddress.contains("M-PESA") ||
                upperAddress.contains("AIRTEL") ||
                upperAddress.contains("KCB") ||
                upperAddress.contains("EQUITY") ||
                upperAddress.contains("CO-OP") ||
                upperAddress.contains("COOP") ||
                upperAddress.contains("ABSA") ||
                upperAddress.contains("NCBA") ||
                upperAddress.contains("STANBIC") ||
                upperAddress.contains("DTB") ||
                upperAddress.contains("FAMILY") ||
                upperAddress.contains("IMBANK") ||
                upperAddress.contains("STANCHART") ||
                upperAddress.contains("FULIZA") ||
                upperAddress.contains("HUSTLER") ||
                upperAddress.contains("MSHWARI") ||
                upperAddress.contains("20807") ||
                upperAddress.contains("20021") ||
                upperAddress.contains("22222") ||
                upperAddress.contains("20033")

        val hasKeywords = listOf(
            "confirmed", "sent", "paid", "debited", "credited", "ksh", "kes", "usd", "$",
            "balance", "trans. cost", "airtel money", "disbursement", "received", "bought",
            "fuliza", "hustler", "mshwari", "kcb mpesa", "withdraw", "deposit", "transfer"
        ).any { it in lowerBody }

        return isProvider || hasKeywords
    }
}

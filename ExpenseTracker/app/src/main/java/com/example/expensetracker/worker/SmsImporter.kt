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

object SmsImporter {
    suspend fun syncInboxSms(context: Context, repository: ExpenseRepository, categorizer: ExpenseCategorizer): Int = withContext(Dispatchers.IO) {
        val parser = TransactionParser(categorizer)
        val uri = Uri.parse("content://sms/inbox")
        val projection = arrayOf("body", "date", "address")
        
        var importedCount = 0
        try {
            val cursor = context.contentResolver.query(uri, projection, null, null, "date DESC LIMIT 100")
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
                            repository.add(
                                Expense(
                                    amountMinor = parsed.amountMinor,
                                    currency = parsed.currency,
                                    merchant = parsed.merchant,
                                    category = parsed.category,
                                    categorySource = parsed.source,
                                    note = "Auto-synced from SMS ($address)",
                                    timestamp = date
                                )
                            )
                            if (parsed.feeMinor > 0) {
                                val providerTag = if (address.contains("airtel", true)) "Airtel Fee" else "M-Pesa Fee"
                                repository.add(
                                    Expense(
                                        amountMinor = parsed.feeMinor,
                                        currency = parsed.currency,
                                        merchant = "[$providerTag]",
                                        category = Category.FEES,
                                        categorySource = CategorySource.RULES,
                                        note = "Transaction cost / fee",
                                        timestamp = date
                                    )
                                )
                            }
                            importedCount++
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        importedCount
    }

    private fun isFinancialSenderOrMessage(address: String, body: String): Boolean {
        val upperAddress = address.uppercase()
        val lowerBody = body.lowercase()
        val isProvider = upperAddress.contains("MPESA") || upperAddress.contains("AIRTEL") || upperAddress.contains("KCB") || upperAddress.contains("EQUITY") || upperAddress.contains("CO-OP") || upperAddress.contains("ABSA") || upperAddress.contains("NCBA") || upperAddress.contains("HELB")
        val hasKeywords = listOf("confirmed", "sent", "paid", "debited", "credited", "ksh", "kes", "balance", "trans. cost", "airtel money").any { it in lowerBody }
        return isProvider || hasKeywords
    }
}

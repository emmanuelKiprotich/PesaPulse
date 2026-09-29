package com.example.expensetracker.ai

import com.example.expensetracker.data.Expense
import kotlin.math.roundToLong

class EmailReceiptParser(private val categorizer: ExpenseCategorizer) {

    suspend fun parseEmail(subject: String, body: String): Expense {
        val combined = "$subject\n$body"
        
        val amountRegex = Regex("""(?:KES|Ksh|\$|USD|EUR|GBP)\s*([\d,]+(?:\.\d{2})?)|\b([\d,]+\.\d{2})\b""", RegexOption.IGNORE_CASE)
        val match = amountRegex.find(combined)
        val amountStr = match?.let { (it.groupValues[1].ifEmpty { it.groupValues[2] }).replace(",", "") } ?: "0.00"
        val amountDouble = amountStr.toDoubleOrNull() ?: 0.0
        val amountMinor = (amountDouble * 100).roundToLong()

        val currency = when {
            combined.contains("KES", true) || combined.contains("Ksh", true) -> "KES"
            combined.contains("USD", true) || combined.contains("$", true) -> "USD"
            else -> "USD"
        }

        val merchant = if (subject.contains("receipt", true) || subject.contains("order", true)) {
            subject.replace("Your", "", true).replace("Receipt", "", true).replace("Order", "", true).trim()
        } else {
            "Online Merchant"
        }

        val prediction = categorizer.predict(merchant, combined)

        return Expense(
            amountMinor = amountMinor,
            currency = currency,
            merchant = "[Email] $merchant",
            category = prediction.category,
            categorySource = prediction.source,
            note = "Auto-captured from E-Receipt"
        )
    }
}

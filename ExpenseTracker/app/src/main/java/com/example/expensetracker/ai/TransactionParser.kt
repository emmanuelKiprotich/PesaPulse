package com.example.expensetracker.ai

import com.example.expensetracker.data.Category
import com.example.expensetracker.data.CategorySource
import kotlin.math.roundToLong

data class ParsedTransaction(
    val amountMinor: Long,
    val currency: String,
    val merchant: String,
    val category: Category,
    val source: CategorySource,
    val feeMinor: Long = 0L
)

class TransactionParser(private val categorizer: ExpenseCategorizer) {

    suspend fun parse(text: String): ParsedTransaction? {
        val amountRegex = Regex("""(?:KES|Ksh|\$|USD|EUR|GBP)\s*([\d,]+(?:\.\d{2})?)|\b([\d,]+\.\d{2})\b""", RegexOption.IGNORE_CASE)
        val matches = amountRegex.findAll(text).toList()
        if (matches.isEmpty()) return null

        val mainMatch = matches.first()
        val amountStr = (mainMatch.groupValues[1].ifEmpty { mainMatch.groupValues[2] }).replace(",", "")
        val amountDouble = amountStr.toDoubleOrNull() ?: return null
        val amountMinor = (amountDouble * 100).roundToLong()

        var feeMinor = 0L
        val feeRegex = Regex("""(?:Trans\.?\s*Cost|Transaction\s*Cost|Fee)[,:\s]*(?:Ksh|KES)?\s*([\d,]+\.\d{2})""", RegexOption.IGNORE_CASE)
        val feeMatch = feeRegex.find(text)
        if (feeMatch != null) {
            val feeStr = feeMatch.groupValues[1].replace(",", "")
            val feeDouble = feeStr.toDoubleOrNull() ?: 0.0
            feeMinor = (feeDouble * 100).roundToLong()
        } else if (matches.size > 1) {
            val secondStr = (matches[1].groupValues[1].ifEmpty { matches[1].groupValues[2] }).replace(",", "")
            val secondDouble = secondStr.toDoubleOrNull() ?: 0.0
            if (secondDouble > 0 && secondDouble < 150 && text.contains("cost", true)) {
                feeMinor = (secondDouble * 100).roundToLong()
            }
        }

        val currency = "KES"
        val providerTag = if (text.contains("airtel", true)) "Airtel Money" else "M-Pesa"

        val merchantRegex = Regex("""(?:at|to|paid\s+to|from|sent\s+to)\s+([A-Za-z0-9\s&]+?)(?:\s+on|\s+via|\s+Bal|\s+Account|\.|\n|$)""", RegexOption.IGNORE_CASE)
        val merchantMatch = merchantRegex.find(text)
        val rawMerchant = merchantMatch?.groupValues?.get(1)?.trim() ?: "$providerTag Transaction"
        val merchant = "[$providerTag] $rawMerchant"

        val prediction = categorizer.predict(rawMerchant, text)

        return ParsedTransaction(
            amountMinor = amountMinor,
            currency = currency,
            merchant = merchant,
            category = prediction.category,
            source = prediction.source,
            feeMinor = feeMinor
        )
    }
}

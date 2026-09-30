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
    val feeMinor: Long = 0L,
    val transactionCode: String? = null,
    val isIncome: Boolean = false,
    val balanceMinor: Long? = null,
    val rawMerchant: String = "",
)

class TransactionParser(private val categorizer: ExpenseCategorizer) {

    suspend fun parse(text: String): ParsedTransaction? {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return null

        // 1. Transaction Code (e.g. QG789XYZ, QA123456, TXN1234, Ref: FT123)
        val codeRegex = Regex("""\b([A-Z0-9]{8,12})\s+(?:Confirmed|CONFIRMED)\b""", RegexOption.IGNORE_CASE)
        var transactionCode = codeRegex.find(trimmed)?.groupValues?.get(1)?.uppercase()

        if (transactionCode == null) {
            val refRegex = Regex("""(?:Txn\s*ID|Transaction\s*ID|Trans\s*ID|Ref(?:\s*No)?|Reference|Ref\s*Number)[\s:#]+([A-Z0-9\-_]{6,16})""", RegexOption.IGNORE_CASE)
            transactionCode = refRegex.find(trimmed)?.groupValues?.get(1)?.uppercase()
        }

        if (transactionCode == null) {
            val startCodeRegex = Regex("""^([A-Z0-9]{8,12})\b""")
            val startMatch = startCodeRegex.find(trimmed)
            if (startMatch != null) {
                val candidate = startMatch.groupValues[1].uppercase()
                if (candidate.any { it.isLetter() } && candidate.any { it.isDigit() }) {
                    transactionCode = candidate
                }
            }
        }

        // 2. Income vs Outgoing Detection
        val lower = trimmed.lowercase()
        val isIncome = when {
            lower.contains("you have received") ||
            lower.contains("received ksh") ||
            lower.contains("received kes") ||
            lower.contains("received usd") ||
            lower.contains("credited") ||
            lower.contains("disbursement") ||
            lower.contains("allocation") ||
            lower.contains("deposit of") ||
            lower.contains("deposited to") ||
            lower.contains("salary") ||
            lower.contains("refund") -> true
            else -> false
        }

        // 3. Amount Extraction
        val amountRegex = Regex(
            """(?:KES|Ksh|USD|\$|EUR|GBP)\s*([\d,]+(?:\.\d{1,2})?)|\b([\d,]+\.\d{2})\b""",
            RegexOption.IGNORE_CASE
        )
        val matches = amountRegex.findAll(trimmed).toList()
        if (matches.isEmpty()) return null

        val firstMatch = matches.first()
        val amountStr = (firstMatch.groupValues[1].ifEmpty { firstMatch.groupValues[2] }).replace(",", "")
        val amountDouble = amountStr.toDoubleOrNull() ?: return null
        val amountMinor = (amountDouble * 100).roundToLong()
        if (amountMinor <= 0) return null

        // 4. Transaction Fee Extraction
        var feeMinor = 0L
        val feeRegex = Regex(
            """(?:Trans\.?\s*Cost|Transaction\s*Cost|Fee)(?:\s+was|\s+is)?[,:\s]*(?:Ksh|KES)?\s*([\d,]+(?:\.\d{1,2})?)""",
            RegexOption.IGNORE_CASE
        )
        val feeMatch = feeRegex.find(trimmed)
        if (feeMatch != null) {
            val feeStr = feeMatch.groupValues[1].replace(",", "")
            val feeDouble = feeStr.toDoubleOrNull() ?: 0.0
            feeMinor = (feeDouble * 100).roundToLong()
        }

        // 5. Account Balance Extraction (if available)
        var balanceMinor: Long? = null
        val balRegex = Regex(
            """(?:New\s+(?:M-PESA\s+)?balance\s+is|Avail(?:able)?\s*Bal(?:ance)?(?:\s+is)?|New\s+balance\s+is)[\s:]*(?:Ksh|KES)?\s*([\d,]+(?:\.\d{1,2})?)""",
            RegexOption.IGNORE_CASE
        )
        val balMatch = balRegex.find(trimmed)
        if (balMatch != null) {
            val balStr = balMatch.groupValues[1].replace(",", "")
            balStr.toDoubleOrNull()?.let { balanceMinor = (it * 100).roundToLong() }
        }

        // 6. Provider Identification
        val providerTag = when {
            lower.contains("airtel") -> "Airtel Money"
            lower.contains("equity") -> "Equity Bank"
            lower.contains("kcb") -> "KCB Bank"
            lower.contains("co-op") || lower.contains("cooperative") -> "Co-op Bank"
            lower.contains("absa") -> "Absa Bank"
            lower.contains("ncba") -> "NCBA Bank"
            lower.contains("stanbic") -> "Stanbic Bank"
            lower.contains("dtb") -> "DTB Bank"
            lower.contains("family bank") -> "Family Bank"
            lower.contains("fuliza") -> "Fuliza M-Pesa"
            else -> "M-Pesa"
        }

        // 7. Merchant / Counterparty Extraction
        val rawMerchant = when {
            lower.contains("bought") && (lower.contains("airtime") || lower.contains("data")) -> {
                "$providerTag Airtime"
            }
            isIncome -> {
                val fromRegex = Regex("""from\s+([A-Za-z0-9\s&'-]+?)(?:\s+on|\s+via|\s+Bal|\s+Account|\.|\n|$)""", RegexOption.IGNORE_CASE)
                val fromMatch = fromRegex.find(trimmed)
                fromMatch?.groupValues?.get(1)?.trim() ?: "Funds Received"
            }
            else -> {
                val toRegex = Regex("""(?:to|paid\s+to|sent\s+to|at)\s+([A-Za-z0-9\s&'-]+?)(?:\s+on|\s+via|\s+Bal|\s+Account|\s+for\s+account|\.|\n|$)""", RegexOption.IGNORE_CASE)
                val toMatch = toRegex.find(trimmed)
                toMatch?.groupValues?.get(1)?.trim() ?: "$providerTag Transaction"
            }
        }

        val cleanMerchant = rawMerchant
            .replace(Regex("""\b0\d{9}\b"""), "")
            .replace(Regex("""\b254\d{9}\b"""), "")
            .trim()
            .ifEmpty { "$providerTag Merchant" }

        val displayMerchant = "[$providerTag] $cleanMerchant"

        // 8. Category Prediction
        val prediction = if (isIncome) {
            Prediction(Category.OTHER, 0.70f, CategorySource.RULES)
        } else {
            categorizer.predict(cleanMerchant, trimmed)
        }

        return ParsedTransaction(
            amountMinor = amountMinor,
            currency = "KES",
            merchant = displayMerchant,
            category = prediction.category,
            source = prediction.source,
            feeMinor = feeMinor,
            transactionCode = transactionCode,
            isIncome = isIncome,
            balanceMinor = balanceMinor,
            rawMerchant = cleanMerchant,
        )
    }

    companion object {
        fun generateDeterministicCode(sourceOrSender: String, text: String, amountMinor: Long): String {
            val normalized = (sourceOrSender.trim().uppercase() + ":" + text.trim().replace(Regex("""\s+"""), " ")).lowercase()
            val hash = (normalized.hashCode().toLong() and 0xFFFFFFFFL)
            return "DET-$hash-$amountMinor"
        }
    }
}

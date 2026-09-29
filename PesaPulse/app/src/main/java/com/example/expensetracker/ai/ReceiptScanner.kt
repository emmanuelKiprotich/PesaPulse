package com.example.expensetracker.ai

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

data class ScannedReceipt(
    val merchant: String?,
    val totalMinor: Long?,
    val rawText: String,
    val dateText: String? = null
)

/** On-device receipt OCR using ML Kit. Pass a Bitmap from the camera or gallery picker. */
class ReceiptScanner {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun scan(bitmap: Bitmap): ScannedReceipt {
        val text = suspendCoroutine<String> { cont ->
            recognizer.process(InputImage.fromBitmap(bitmap, 0))
                .addOnSuccessListener { cont.resume(it.text) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }
        val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }
        val merchant = extractMerchant(lines)
        val total = extractTotal(lines, text)
        val date = extractDate(text)

        return ScannedReceipt(
            merchant = merchant,
            totalMinor = total,
            rawText = text,
            dateText = date
        )
    }

    private fun extractMerchant(lines: List<String>): String? {
        // Look for common merchant names in Kenyan retail/dining
        val knownKeywords = listOf(
            "naivas", "quickmart", "carrefour", "java house", "artcaffe",
            "kfc", "chandarana", "cleanshelf", "tuskys", "subway",
            "pharmacy", "chemist", "bookshop", "stationery", "hospital",
            "petrol", "shell", "total", "rubis", "ola", "canteen"
        )
        for (line in lines.take(6)) {
            val lower = line.lowercase()
            if (knownKeywords.any { it in lower }) {
                return line
            }
        }
        // Fallback: the first line that is mostly alphabetic and not a date/PIN/tax header
        return lines.take(4).firstOrNull { line ->
            line.length in 3..40 &&
            !line.contains("receipt", ignoreCase = true) &&
            !line.contains("tax invoice", ignoreCase = true) &&
            !line.contains("welcome", ignoreCase = true) &&
            !line.contains("tel:", ignoreCase = true) &&
            !line.contains("pin:", ignoreCase = true) &&
            line.any { it.isLetter() }
        } ?: lines.firstOrNull()
    }

    private fun extractTotal(lines: List<String>, fullText: String): Long? {
        val amountRegex = Regex("""(?:KES|Ksh|\$)?\s*(\d{1,3}(?:,\d{3})*|\d+)(?:\.(\d{2}))?""")
        fun parseAmount(s: String): Long? = amountRegex.findAll(s).mapNotNull {
            val whole = it.groupValues[1].replace(",", "").toLongOrNull() ?: return@mapNotNull null
            val cents = it.groupValues[2].ifEmpty { "00" }.toLong()
            whole * 100 + cents
        }.maxOrNull()

        // Prioritize explicit TOTAL or AMOUNT DUE lines
        val priorityLines = lines.filter { line ->
            val l = line.lowercase()
            (l.contains("total") || l.contains("amount due") || l.contains("grand total") || l.contains("net amount")) &&
            !l.contains("subtotal") && !l.contains("item") && !l.contains("tax")
        }
        for (line in priorityLines.reversed()) {
            val amt = parseAmount(line)
            if (amt != null && amt > 0) return amt
        }

        // Second fallback: Subtotal line
        val subtotalLine = lines.lastOrNull { it.contains("subtotal", ignoreCase = true) }
        subtotalLine?.let { parseAmount(it) }?.let { if (it > 0) return it }

        // Final fallback: largest plausible monetary amount in the text
        return amountRegex.findAll(fullText).mapNotNull {
            val whole = it.groupValues[1].replace(",", "").toLongOrNull() ?: return@mapNotNull null
            val cents = it.groupValues[2].ifEmpty { "00" }.toLong()
            val total = whole * 100 + cents
            if (total in 10_00L..500_000_00L) total else null
        }.maxOrNull()
    }

    private fun extractDate(text: String): String? {
        val dateRegex = Regex("""\b(\d{1,2}[/-]\d{1,2}[/-]\d{2,4})\b""")
        return dateRegex.find(text)?.groupValues?.get(1)
    }
}

package com.example.expensetracker.ai

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

data class ScannedReceipt(val merchant: String?, val totalMinor: Long?, val rawText: String)

/** On-device receipt OCR using ML Kit. Pass a Bitmap from the camera or gallery picker. */
class ReceiptScanner {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun scan(bitmap: Bitmap): ScannedReceipt {
        val text = suspendCoroutine<String> { cont ->
            recognizer.process(InputImage.fromBitmap(bitmap, 0))
                .addOnSuccessListener { cont.resume(it.text) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }
        return ScannedReceipt(
            merchant = text.lines().firstOrNull { it.isNotBlank() }?.trim(),
            totalMinor = extractTotal(text),
            rawText = text
        )
    }

    /** Looks for a "TOTAL 1,250.00" style line; falls back to the largest amount on the receipt. */
    private fun extractTotal(text: String): Long? {
        val amount = Regex("""(\d{1,3}(?:,\d{3})*|\d+)(?:\.(\d{2}))?""")
        fun parse(s: String): Long? = amount.findAll(s).mapNotNull {
            val whole = it.groupValues[1].replace(",", "").toLongOrNull() ?: return@mapNotNull null
            val cents = it.groupValues[2].ifEmpty { "00" }.toLong()
            whole * 100 + cents
        }.maxOrNull()

        val totalLine = text.lines().lastOrNull { it.contains("total", ignoreCase = true) }
        return totalLine?.let(::parse) ?: parse(text)
    }
}

package com.example.expensetracker.ai

/** Uses the primary (ML) model when it is confident enough, otherwise the fallback (rules). */
class HybridCategorizer(
    private val primary: ExpenseCategorizer,
    private val fallback: ExpenseCategorizer,
    private val confidenceThreshold: Float = 0.6f
) : ExpenseCategorizer {

    override suspend fun predict(merchant: String, note: String): Prediction {
        val p = primary.predict(merchant, note)
        return if (p.confidence >= confidenceThreshold) p else fallback.predict(merchant, note)
    }
}

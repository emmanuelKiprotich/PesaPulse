package com.example.expensetracker.ai

import com.example.expensetracker.data.Category

/** Uses the primary (ML) model when it is confident enough, otherwise the fallback (rules). */
class HybridCategorizer(
    private val primary: ExpenseCategorizer,
    private val fallback: ExpenseCategorizer,
    private val confidenceThreshold: Float = 0.50f,
) : ExpenseCategorizer {

    override suspend fun predict(merchant: String, note: String): Prediction {
        val p = primary.predict(merchant, note)
        return if (p.confidence >= confidenceThreshold) p else fallback.predict(merchant, note)
    }

    override fun predictAll(merchant: String, note: String): List<Pair<Category, Float>> {
        return primary.predictAll(merchant, note)
    }

    override fun train(merchant: String, note: String, category: Category) {
        primary.train(merchant, note, category)
        fallback.train(merchant, note, category)
    }
}

package com.example.expensetracker.ai

import android.content.Context
import com.example.expensetracker.data.Category
import com.example.expensetracker.data.CategorySource

/**
 * On-device text classifier stub.
 *
 * TODO to make this real:
 *  1. Train a small text classifier (merchant + note -> Category) and export to
 *     app/src/main/assets/category_model.tflite (plus a vocab/labels file).
 *  2. Uncomment the tensorflow-lite dependency in app/build.gradle.kts.
 *  3. Load the model with Interpreter(FileUtil.loadMappedFile(...)) and implement tokenize().
 *
 * Until then it reports "not available" (confidence 0) so HybridCategorizer falls back to rules.
 */
class TfliteCategorizer(private val context: Context) : ExpenseCategorizer {

    private val labels = Category.entries

    // private val interpreter: Interpreter? by lazy { loadModelOrNull() }

    override suspend fun predict(merchant: String, note: String): Prediction {
        // val input = tokenize("$merchant $note")
        // val output = Array(1) { FloatArray(labels.size) }
        // interpreter?.run(input, output)
        // val probs = output[0]
        // val best = probs.indices.maxByOrNull { probs[it] } ?: return unavailable()
        // return Prediction(labels[best], probs[best], CategorySource.MODEL)
        return unavailable()
    }

    private fun unavailable() = Prediction(Category.OTHER, 0f, CategorySource.MODEL)
}

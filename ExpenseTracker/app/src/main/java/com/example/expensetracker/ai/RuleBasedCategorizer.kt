package com.example.expensetracker.ai

import com.example.expensetracker.data.Category
import com.example.expensetracker.data.CategorySource

/** Baseline heuristic. Extend the keyword lists with local merchants (Kenyan examples included). */
class RuleBasedCategorizer : ExpenseCategorizer {

    private val keywords: Map<Category, List<String>> = mapOf(
        Category.FOOD to listOf("java house", "kfc", "naivas", "quickmart", "carrefour", "restaurant", "cafe", "chips", "mama mboga", "glovo food"),
        Category.TRANSPORT to listOf("uber", "bolt", "little cab", "matatu", "fuel", "petrol", "shell", "total", "rubis", "sgr", "parking"),
        Category.AIRTIME_DATA to listOf("safaricom", "airtel", "airtime", "data bundle", "bundles", "faiba"),
        Category.RENT_UTILITIES to listOf("rent", "kplc", "kenya power", "water", "nairobi water", "zuku", "wifi"),
        Category.SHOPPING to listOf("jumia", "kilimall", "mall", "clothes", "shoes", "amazon"),
        Category.HEALTH to listOf("pharmacy", "hospital", "clinic", "chemist", "sha", "nhif"),
        Category.EDUCATION to listOf("tuition", "school fees", "books", "strathmore", "stationery", "course"),
        Category.ENTERTAINMENT to listOf("netflix", "showmax", "spotify", "cinema", "movie", "bar", "club")
    )

    override suspend fun predict(merchant: String, note: String): Prediction {
        val text = "$merchant $note".lowercase()
        val hit = keywords.entries.firstOrNull { (_, words) -> words.any { it in text } }
        return if (hit != null) Prediction(hit.key, 0.8f, CategorySource.RULES)
        else Prediction(Category.OTHER, 0.2f, CategorySource.RULES)
    }
}

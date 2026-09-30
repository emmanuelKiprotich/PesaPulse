package com.example.expensetracker.ai

import com.example.expensetracker.data.Category
import com.example.expensetracker.data.CategorySource

/** Baseline heuristic tailored for Kenyan university student life. */
class RuleBasedCategorizer : ExpenseCategorizer {

    private val keywords: Map<Category, List<String>> = mapOf(
        Category.FOOD to listOf("mama mboga", "mess", "canteen", "cafeteria", "java house", "kfc", "naivas", "quickmart", "carrefour", "smokie", "mandazi", "ugali", "chips", "cafe", "restaurant", "glovo"),
        Category.TRANSPORT to listOf("matatu", "boda", "uber", "bolt", "little cab", "sgr", "stage", "fare", "fuel", "total energies", "rubis", "shell"),
        Category.AIRTIME_DATA to listOf("safaricom", "airtel", "telkom", "airtime", "data bundle", "bundles", "faiba", "kopa kred", "ochi"),
        Category.RENT_UTILITIES to listOf("hostel", "rent", "kplc", "kenya power", "token", "water", "nairobi water", "zuku", "wifi", "caretaker"),
        Category.SHOPPING to listOf("photocopying", "photocopy", "print", "stationery", "books", "kilimall", "mitumba", "clothes"),
        Category.HEALTH to listOf("pharmacy", "hospital", "clinic", "chemist", "sha", "nhif", "dispensary"),
        Category.EDUCATION to listOf("tuition", "school fees", "semester", "exam fee", "library", "lab", "unit registration", "course"),
        Category.ENTERTAINMENT to listOf("netflix", "showmax", "spotify", "cinema", "movie", "bar", "club", "hangout", "chill"),
        Category.PEER_DEBTS to listOf("bill split", "debt", "lent", "borrowed", "send money", "roommate"),
        Category.FEES to listOf("trans. cost", "transaction cost", "charge", "fee"),
    )

    override suspend fun predict(merchant: String, note: String): Prediction {
        val text = "$merchant $note".lowercase()
        val hit = keywords.entries.firstOrNull { (_, words) -> words.any { it in text } }
        return if (hit != null) Prediction(hit.key, 0.85f, CategorySource.RULES)
        else Prediction(Category.OTHER, 0.2f, CategorySource.RULES)
    }
}

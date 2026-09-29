package com.example.expensetracker

import com.example.expensetracker.ai.NaiveBayesCategorizer
import com.example.expensetracker.data.Category
import com.example.expensetracker.data.CategorySource
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NaiveBayesCategorizerTest {

    private lateinit var categorizer: NaiveBayesCategorizer

    @Before
    fun setUp() {
        categorizer = NaiveBayesCategorizer()
    }

    @Test
    fun testPreTrainedKenyanTransactions() = runBlocking {
        // Food
        val foodPred = categorizer.predict("Naivas Supermarket", "bought milk bread and eggs")
        assertEquals(Category.FOOD, foodPred.category)
        assertTrue(foodPred.confidence > 0.4f)
        assertEquals(CategorySource.MODEL, foodPred.source)

        // Transport
        val transportPred = categorizer.predict("Super Metro Sacco", "fare to town stage")
        assertEquals(Category.TRANSPORT, transportPred.category)

        // Rent & Utilities
        val kplcPred = categorizer.predict("KPLC Prepaid", "bought electricity meter token")
        assertEquals(Category.RENT_UTILITIES, kplcPred.category)

        // Airtime & Data
        val airtimePred = categorizer.predict("Safaricom Airtime", "airtime top up")
        assertEquals(Category.AIRTIME_DATA, airtimePred.category)

        // Entertainment
        val netflixPred = categorizer.predict("Netflix Kenya", "monthly streaming")
        assertEquals(Category.ENTERTAINMENT, netflixPred.category)
    }

    @Test
    fun testOnlineContinuousLearning() = runBlocking {
        // Unseen custom vendor
        val unseenVendor = "Mama Oliech Fish Joint"
        val unseenNote = "Tilapia with ugali special"

        // Online training
        categorizer.train(unseenVendor, unseenNote, Category.FOOD)

        // Verify model adapts and predicts Category.FOOD
        val pred = categorizer.predict(unseenVendor, "lunch meal")
        assertEquals(Category.FOOD, pred.category)
        assertTrue(pred.confidence > 0.3f)
    }

    @Test
    fun testPredictAllProbabilitiesSumToOne() {
        val scores = categorizer.predictAllScores("Quickmart Ruiru", "fresh fruits")
        assertTrue(scores.isNotEmpty())
        val sum = scores.sumOf { it.second.toDouble() }
        assertTrue("Softmax probabilities should sum to ~1.0: $sum", sum in 0.98..1.02)
    }
}

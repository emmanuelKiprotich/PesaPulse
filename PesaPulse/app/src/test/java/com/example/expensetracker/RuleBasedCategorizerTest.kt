package com.example.expensetracker

import com.example.expensetracker.ai.RuleBasedCategorizer
import com.example.expensetracker.data.Category
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RuleBasedCategorizerTest {

    private lateinit var categorizer: RuleBasedCategorizer

    @Before
    fun setUp() {
        categorizer = RuleBasedCategorizer()
    }

    @Test
    fun testFoodKeywords() = runBlocking {
        assertEquals(Category.FOOD, categorizer.predict("Mama Mboga Groceries").category)
        assertEquals(Category.FOOD, categorizer.predict("Campus Mess Cafeteria").category)
        assertEquals(Category.FOOD, categorizer.predict("Java House").category)
        assertEquals(Category.FOOD, categorizer.predict("KFC Westlands").category)
    }

    @Test
    fun testTransportKeywords() = runBlocking {
        assertEquals(Category.TRANSPORT, categorizer.predict("Boda stage fare").category)
        assertEquals(Category.TRANSPORT, categorizer.predict("Matatu fare to town").category)
        assertEquals(Category.TRANSPORT, categorizer.predict("Uber ride").category)
        assertEquals(Category.TRANSPORT, categorizer.predict("Shell Petrol Station").category)
    }

    @Test
    fun testAirtimeKeywords() = runBlocking {
        assertEquals(Category.AIRTIME_DATA, categorizer.predict("Safaricom Airtime Topup").category)
        assertEquals(Category.AIRTIME_DATA, categorizer.predict("Data bundle purchase").category)
    }

    @Test
    fun testUtilitiesKeywords() = runBlocking {
        assertEquals(Category.RENT_UTILITIES, categorizer.predict("KPLC Prepaid Tokens").category)
        assertEquals(Category.RENT_UTILITIES, categorizer.predict("Hostel Room Rent").category)
        assertEquals(Category.RENT_UTILITIES, categorizer.predict("Zuku Wi-Fi").category)
    }

    @Test
    fun testShoppingKeywords() = runBlocking {
        assertEquals(Category.SHOPPING, categorizer.predict("Printing and photocopying").category)
        assertEquals(Category.SHOPPING, categorizer.predict("Stationery and pens").category)
        assertEquals(Category.SHOPPING, categorizer.predict("Stationery and books").category)
    }

    @Test
    fun testHealthKeywords() = runBlocking {
        assertEquals(Category.HEALTH, categorizer.predict("Goodlife Pharmacy").category)
        assertEquals(Category.HEALTH, categorizer.predict("Campus Clinic Chemist").category)
    }
}

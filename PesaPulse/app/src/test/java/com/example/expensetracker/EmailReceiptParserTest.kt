package com.example.expensetracker

import com.example.expensetracker.ai.EmailReceiptParser
import com.example.expensetracker.ai.RuleBasedCategorizer
import com.example.expensetracker.data.Category
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EmailReceiptParserTest {

    private lateinit var emailParser: EmailReceiptParser

    @Before
    fun setUp() {
        emailParser = EmailReceiptParser(RuleBasedCategorizer())
    }

    @Test
    fun testParseJumiaReceipt() = runBlocking {
        val subject = "Your Jumia Order Receipt - #9876"
        val body = "Thank you for shopping on Jumia Kenya! Total: KES 1,850.00 for Stationery & Books."
        val expense = emailParser.parseEmail(subject, body)

        assertEquals(185000L, expense.amountMinor)
        assertEquals("KES", expense.currency)
        assertTrue(expense.merchant.contains("Jumia"))
        assertEquals(Category.SHOPPING, expense.category)
        assertFalse(expense.isIncome)
        assertTrue(expense.transactionCode != null)
    }

    @Test
    fun testParseUsdReceipt() = runBlocking {
        val subject = "Your Receipt for Spotify Subscription"
        val body = "Payment confirmed. Total: $ 2.99 on 25/09/2026."
        val expense = emailParser.parseEmail(subject, body)

        assertEquals(299L, expense.amountMinor)
        assertEquals("USD", expense.currency)
        assertEquals(Category.ENTERTAINMENT, expense.category)
    }
}

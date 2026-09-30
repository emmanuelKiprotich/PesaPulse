package com.example.expensetracker

import com.example.expensetracker.ai.RuleBasedCategorizer
import com.example.expensetracker.ai.TransactionParser
import com.example.expensetracker.data.Category
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TransactionParserTest {

    private lateinit var parser: TransactionParser

    @Before
    fun setUp() {
        parser = TransactionParser(RuleBasedCategorizer())
    }

    @Test
    fun testParseStandardMpesaPaybill() = runBlocking {
        val sms = "QG789XYZ Confirmed. Ksh 1,450.00 sent to JAVA HOUSE RVR on 29/9/26 at 4:15 PM. New balance is Ksh 5,230.00. Transaction cost, Ksh 22.00."
        val result = parser.parse(sms)

        assertNotNull(result)
        assertEquals("QG789XYZ", result?.transactionCode)
        assertEquals(145000L, result?.amountMinor)
        assertEquals(2200L, result?.feeMinor)
        assertEquals(523000L, result?.balanceMinor)
        assertEquals("[M-Pesa] JAVA HOUSE RVR", result?.merchant)
        assertEquals(Category.FOOD, result?.category)
        assertFalse(result!!.isIncome)
    }

    @Test
    fun testParseMpesaMamaMboga() = runBlocking {
        val sms = "QA123456 Confirmed. Ksh500.00 paid to MAMA MBOGA. on 28/9/26 at 1:20 PM. New M-PESA balance is Ksh4,708.00. Transaction cost, Ksh 0.00."
        val result = parser.parse(sms)

        assertNotNull(result)
        assertEquals("QA123456", result?.transactionCode)
        assertEquals(50000L, result?.amountMinor)
        assertEquals(0L, result?.feeMinor)
        assertEquals(470800L, result?.balanceMinor)
        assertEquals(Category.FOOD, result?.category)
        assertFalse(result!!.isIncome)
    }

    @Test
    fun testParseAirtimePurchase() = runBlocking {
        val sms = "QD112233 Confirmed. Ksh 100.00 bought airtime on 29/9/26 at 10:00 AM."
        val result = parser.parse(sms)

        assertNotNull(result)
        assertEquals("QD112233", result?.transactionCode)
        assertEquals(10000L, result?.amountMinor)
        assertEquals(Category.AIRTIME_DATA, result?.category)
        assertFalse(result!!.isIncome)
    }

    @Test
    fun testParseReceivedPeerTransfer() = runBlocking {
        val sms = "QC987654 Confirmed. You have received Ksh 2,500.00 from KEVIN OCHIENG 0712345678 on 27/9/26 at 6:30 PM."
        val result = parser.parse(sms)

        assertNotNull(result)
        assertEquals("QC987654", result?.transactionCode)
        assertEquals(250000L, result?.amountMinor)
        assertTrue(result!!.isIncome)
        assertTrue(result.merchant.contains("KEVIN OCHIENG"))
    }

    @Test
    fun testParseAirtelMoney() = runBlocking {
        val sms = "Airtel Money confirmed. Sent KES 850.00 to NAIVAS SUPERMARKET. Fee was KES 15.00."
        val result = parser.parse(sms)

        assertNotNull(result)
        assertEquals(85000L, result?.amountMinor)
        assertEquals(1500L, result?.feeMinor)
        assertTrue(result!!.merchant.contains("Airtel Money"))
        assertEquals(Category.FOOD, result.category)
        assertFalse(result.isIncome)
    }

    @Test
    fun testParseKplcTokens() = runBlocking {
        val sms = "QK998877 Confirmed. Ksh 300.00 paid to KPLC PREPAID for account 123456789. Transaction cost, Ksh 0.00."
        val result = parser.parse(sms)

        assertNotNull(result)
        assertEquals(30000L, result?.amountMinor)
        assertEquals(Category.RENT_UTILITIES, result?.category)
        assertFalse(result!!.isIncome)
    }
}

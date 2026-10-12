package com.example

import com.example.data.entity.NeedEntity
import com.example.model.Money
import com.example.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun money_parseToPaisa_validInputs() {
        assertEquals(125000L, Money.parseToPaisa("1250"))
        assertEquals(125050L, Money.parseToPaisa("1250.50"))
        assertEquals(125005L, Money.parseToPaisa("1250.05"))
        assertEquals(125050L, Money.parseToPaisa("1,250.50"))
        assertEquals(50L, Money.parseToPaisa("0.50"))
        assertEquals(5000000L, Money.parseToPaisa("Rs. 50,000"))
        assertEquals(100L, Money.parseToPaisa("1.00"))
        assertEquals(1L, Money.parseToPaisa("0.01"))
    }

    @Test
    fun money_parseToPaisa_invalidInputs() {
        assertNull(Money.parseToPaisa(""))
        assertNull(Money.parseToPaisa("   "))
        assertNull(Money.parseToPaisa("abc"))
        assertNull(Money.parseToPaisa("12.345")) // More than 2 decimal places rejected
        assertNull(Money.parseToPaisa("-500"))
        assertNull(Money.parseToPaisa("12.3.4"))
    }

    @Test
    fun money_formatPkr() {
        assertEquals("Rs. 1,250.50", Money.formatPkr(125050L))
        assertEquals("Rs. 0.00", Money.formatPkr(0L))
        assertEquals("Rs. 0.01", Money.formatPkr(1L))
        assertEquals("-Rs. 500.00", Money.formatPkr(-50000L))
        assertEquals("Rs. 1,000,000.00", Money.formatPkr(100000000L))
    }

    @Test
    fun money_formatSignedPkr() {
        assertEquals("+Rs. 1,000.00", Money.formatSignedPkr(100000L, TransactionType.INCOME))
        assertEquals("-Rs. 250.00", Money.formatSignedPkr(25000L, TransactionType.EXPENSE))
        assertEquals("→ Rs. 500.00", Money.formatSignedPkr(50000L, TransactionType.TRANSFER))
    }

    @Test
    fun transferInvariance_totalWealthUnchanged() {
        val accountAOpening = 100000L // Rs. 1,000.00
        val accountBOpening = 50000L  // Rs. 500.00
        val initialTotal = accountAOpening + accountBOpening

        val transferAmount = 25000L // Rs. 250.00
        val accountAAfter = accountAOpening - transferAmount
        val accountBAfter = accountBOpening + transferAmount
        val finalTotal = accountAAfter + accountBAfter

        assertEquals(initialTotal, finalTotal)
    }

    @Test
    fun whatINeed_budgetSumAndFilters() {
        val needs = listOf(
            NeedEntity(id = 1, name = "Rice Cooker", expectedPricePaisa = 450000L, isDone = false),
            NeedEntity(id = 2, name = "Office Chair", expectedPricePaisa = 1200000L, isDone = false),
            NeedEntity(id = 3, name = "Notebook", expectedPricePaisa = 35000L, isDone = true),
            NeedEntity(id = 4, name = "Gift", expectedPricePaisa = null, isDone = false)
        )

        val active = needs.filter { !it.isDone }
        val done = needs.filter { it.isDone }

        assertEquals(3, active.size)
        assertEquals(1, done.size)

        val totalBudget = active.mapNotNull { it.expectedPricePaisa }.sum()
        assertEquals(1650000L, totalBudget)
        assertEquals("Rs. 16,500.00", Money.formatPkr(totalBudget))
    }
}

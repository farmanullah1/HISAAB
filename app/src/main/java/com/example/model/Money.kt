package com.example.model

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Deterministic Money abstraction in Pakistani Rupees (PKR).
 * Stored internally as integer minor units (paisa: 100 paisa = 1 PKR)
 * to avoid floating-point rounding errors.
 */
object Money {
    private val decimalFormat: DecimalFormat by lazy {
        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = ','
            decimalSeparator = '.'
        }
        DecimalFormat("#,##0.00", symbols)
    }

    /**
     * Formats Long paisa into PKR representation: "Rs. 12,345.00"
     */
    fun formatPkr(paisa: Long): String {
        val isNegative = paisa < 0
        val absPaisa = Math.abs(paisa)
        val rupees = absPaisa / 100.0
        val formatted = decimalFormat.format(rupees)
        return if (isNegative) "-Rs. $formatted" else "Rs. $formatted"
    }

    /**
     * Formats with sign for ledger rows:
     * INCOME: "+Rs. 500.00"
     * EXPENSE: "-Rs. 250.00"
     * TRANSFER: "→ Rs. 1,000.00"
     */
    fun formatSigned(paisa: Long, type: TransactionType): String {
        val absFormatted = formatPkr(Math.abs(paisa))
        return when (type) {
            TransactionType.INCOME -> "+$absFormatted"
            TransactionType.EXPENSE -> "-$absFormatted"
            TransactionType.TRANSFER -> "→ $absFormatted"
        }
    }

    fun formatSignedPkr(paisa: Long, type: TransactionType): String = formatSigned(paisa, type)

    /**
     * Parses "1234", "1234.5", "1234.56" -> Long paisa.
     * Reject: negatives, >2 decimals, blank, non-numeric. Return null.
     */
    fun parsePaisa(input: String): Long? {
        val sanitized = input.trim().replace(",", "").replace("Rs.", "").replace("PKR", "").trim()
        if (sanitized.isEmpty()) return null

        val parts = sanitized.split(".")
        if (parts.size > 2) return null

        val wholePart = parts[0]
        if (wholePart.isEmpty() && parts.size == 1) return null

        val whole = if (wholePart.isEmpty()) 0L else wholePart.toLongOrNull() ?: return null
        if (whole < 0) return null

        val fraction = if (parts.size == 2) {
            val decimalStr = parts[1]
            if (decimalStr.length > 2) return null // Reject >2 decimals
            when (decimalStr.length) {
                0 -> 0L
                1 -> (decimalStr + "0").toLongOrNull() ?: return null
                2 -> decimalStr.toLongOrNull() ?: return null
                else -> return null
            }
        } else {
            0L
        }

        return (whole * 100) + fraction
    }

    fun parseToPaisa(input: String): Long? = parsePaisa(input)
}

enum class TransactionType {
    INCOME,
    EXPENSE,
    TRANSFER
}

enum class AccountType(val displayName: String) {
    CASH("Cash"),
    BANK("Bank")
}

package com.example.budgettracker.ui.utils

import java.text.NumberFormat
import java.util.Locale

object CurrencyUtils {
    // We use a custom NumberFormat for Philippine Peso
    private val format = NumberFormat.getCurrencyInstance(Locale("en", "PH"))

    fun formatAmount(amount: Double): String {
        return format.format(amount)
    }
}

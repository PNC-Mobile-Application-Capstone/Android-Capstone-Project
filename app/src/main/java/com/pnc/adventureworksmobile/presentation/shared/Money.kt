package com.pnc.adventureworksmobile.presentation.shared

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

fun formatPrice(amount: BigDecimal): String =
    NumberFormat.getCurrencyInstance(Locale.US).format(amount)

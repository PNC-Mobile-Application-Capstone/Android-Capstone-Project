package com.pnc.adventureworksmobile.data.local

import java.math.BigDecimal
import java.math.RoundingMode

internal fun Double.toMoney(): BigDecimal = BigDecimal.valueOf(this).setScale(2, RoundingMode.HALF_UP)

internal fun BigDecimal.toMoneyColumn(): Double = toDouble()

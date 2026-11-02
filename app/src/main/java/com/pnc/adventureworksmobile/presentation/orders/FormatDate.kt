package com.pnc.adventureworksmobile.presentation.orders

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun formatDate(isoDate: String): String {
    val instant = Instant.parse("${isoDate}Z")
    val formatter = DateTimeFormatter.ofPattern("MMMM d, uuuu, h:mm a z").withZone(ZoneId.systemDefault())
    return formatter.format(instant)
}
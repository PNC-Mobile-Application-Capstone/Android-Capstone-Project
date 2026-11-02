package com.pnc.adventureworksmobile.presentation.shared

import java.util.Locale

fun productCategory(productName: String): String {
    val name = productName.lowercase(Locale.US)
    return when {
        name.containsAny("jersey", "shorts", "glove", "sock", "cap", "vest") -> "Clothing"
        name.containsAny("frame", "fork", "wheel", "tire", "tube", "chain", "brake", "pedal") ->
            "Components"
        name.containsAny("helmet", "bottle", "wash", "lock", "pump", "light", "pack", "fender") ->
            "Accessories"
        name.contains("bike") || name.startsWith("mountain-") || name.startsWith("road-") ||
                name.startsWith("touring-") -> "Bikes"
        else -> "Accessories"
    }
}
private fun String.containsAny(vararg terms: String): Boolean = terms.any(::contains)
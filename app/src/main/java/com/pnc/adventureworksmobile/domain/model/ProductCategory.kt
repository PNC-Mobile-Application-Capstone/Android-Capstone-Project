package com.pnc.adventureworksmobile.domain.model

import java.util.Locale

const val ALL_CATEGORIES = "All"

val PRODUCT_CATEGORIES = listOf(ALL_CATEGORIES, "Bikes", "Components", "Clothing", "Accessories")

fun productCategory(productName: String): String {
    val name = productName.lowercase(Locale.US)
    return when {
        name.containsAny("jersey", "shorts", "glove", "sock", "cap", "vest", "nylon") -> "Clothing"
        name.containsAny("frame", "fork", "wheel", "tire", "tube", "chain", "brake", "pedal") ->
            "Components"
        name.containsAny("helmet", "bottle", "wash", "lock", "pump", "light", "pack", "fender", "stand", "rack", "pannier") ->
            "Accessories"
        name.contains("bike") || name.startsWith("mountain-") || name.startsWith("road-") ||
                name.startsWith("touring-") -> "Bikes"
        else -> "Accessories"
    }
}

private fun String.containsAny(vararg terms: String): Boolean = terms.any(::contains)

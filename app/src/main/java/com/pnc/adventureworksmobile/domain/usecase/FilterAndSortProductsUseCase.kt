package com.pnc.adventureworksmobile.domain.usecase

import com.pnc.adventureworksmobile.domain.model.ALL_CATEGORIES
import com.pnc.adventureworksmobile.domain.model.Product
import com.pnc.adventureworksmobile.domain.model.ProductSort
import com.pnc.adventureworksmobile.domain.model.productCategory
import java.util.Locale
import javax.inject.Inject

class FilterAndSortProductsUseCase @Inject constructor() {

    operator fun invoke(
        products: List<Product>,
        searchQuery: String,
        category: String,
        sort: ProductSort,
    ): List<Product> = products
        .filter { it.matchesSearch(searchQuery) }
        .filter { it.isInCategory(category) }
        .sortedWith(sort.comparator())

    private fun Product.matchesSearch(query: String): Boolean {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return true
        return name.contains(trimmed, ignoreCase = true) ||
            productNumber.contains(trimmed, ignoreCase = true) ||
            productCategory(name).contains(trimmed, ignoreCase = true)
    }

    private fun Product.isInCategory(category: String): Boolean =
        category == ALL_CATEGORIES || productCategory(name) == category

    private fun ProductSort.comparator(): Comparator<Product> {
        val byName = compareBy<Product> { it.name.lowercase(Locale.US) }
        return when (this) {
            ProductSort.NameAscending -> byName
            ProductSort.PriceAscending -> compareBy<Product> { it.listPrice }.then(byName)
            ProductSort.PriceDescending -> compareByDescending<Product> { it.listPrice }.then(byName)
        }
    }
}

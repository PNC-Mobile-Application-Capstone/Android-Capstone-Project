package com.pnc.adventureworksmobile.domain.usecase

import com.pnc.adventureworksmobile.domain.model.ALL_CATEGORIES
import com.pnc.adventureworksmobile.domain.model.Product
import com.pnc.adventureworksmobile.domain.model.ProductSort
import com.pnc.adventureworksmobile.domain.model.productCategory
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Test

class FilterAndSortProductsUseCaseTest {
    private val filterAndSort = FilterAndSortProductsUseCase()

    private val products = listOf(
        product(id = 1, name = "Trail Mountain Bike"),
        product(id = 2, name = "Road Sport Helmet"),
        product(id = 3, name = "All-Weather Jersey"),
    )

    private fun visibleNames(
        products: List<Product> = this.products,
        searchQuery: String = "",
        category: String = ALL_CATEGORIES,
        sort: ProductSort = ProductSort.NameAscending,
    ) = filterAndSort(products, searchQuery, category, sort).map { it.name }

    @Test
    fun categorySelectionFiltersProducts() {
        assertEquals(listOf("Trail Mountain Bike"), visibleNames(category = "Bikes"))
    }

    @Test
    fun searchMatchesProductNameIgnoringCase() {
        assertEquals(listOf("Road Sport Helmet"), visibleNames(searchQuery = "helmet"))
    }

    @Test
    fun searchAndCategoryMustBothMatch() {
        assertEquals(emptyList<String>(), visibleNames(searchQuery = "bike", category = "Clothing"))
    }

    @Test
    fun priceAscendingSortsLowestPriceFirst() {
        val products = listOf(
            product(id = 1, name = "Expensive", price = "500.00"),
            product(id = 2, name = "Affordable", price = "20.00"),
        )

        assertEquals(listOf("Affordable", "Expensive"), visibleNames(products, sort = ProductSort.PriceAscending))
    }

    @Test
    fun priceDescendingSortsHighestPriceFirst() {
        val products = listOf(
            product(id = 1, name = "Affordable", price = "20.00"),
            product(id = 2, name = "Expensive", price = "500.00"),
        )

        assertEquals(listOf("Expensive", "Affordable"), visibleNames(products, sort = ProductSort.PriceDescending))
    }

    @Test
    fun searchAlsoMatchesProductNumber() {
        assertEquals(listOf("Road Sport Helmet"), visibleNames(searchQuery = "PN-2"))
    }

    @Test
    fun bikeFramesAreClassifiedAsComponents() {
        assertEquals("Components", productCategory("HL Mountain Bike Frame - Black, 48"))
    }

    @Test
    fun completeBikeModelNamesAreClassifiedAsBikes() {
        assertEquals("Bikes", productCategory("Mountain-100 Black, 42"))
        assertEquals("Bikes", productCategory("Road-150 Red, 62"))
        assertEquals("Bikes", productCategory("Touring-1000 Yellow, 50"))
    }

    @Test
    fun bikeAccessoriesAreNotClassifiedAsBikes() {
        assertEquals("Accessories", productCategory("Mountain Bottle Cage"))
        assertEquals("Accessories", productCategory("Bike Wash - Dissolver"))
        assertEquals("Accessories", productCategory("All-Purpose Bike Stand"))
        assertEquals("Accessories", productCategory("Hitch Rack - 4-Bike"))
        assertEquals("Accessories", productCategory("Touring-Panniers, Large"))
    }

    private fun product(id: Int, name: String, price: String = "10.00") = Product(
        id = id,
        name = name,
        productNumber = "PN-$id",
        thumbnailBase64 = null,
        thumbnailPhotoFileName = null,
        listPrice = BigDecimal(price),
    )
}

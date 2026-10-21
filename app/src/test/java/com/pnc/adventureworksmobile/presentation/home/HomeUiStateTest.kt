package com.pnc.adventureworksmobile.presentation.home

import com.pnc.adventureworksmobile.core.session.DemoCustomer
import com.pnc.adventureworksmobile.domain.model.Product
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeUiStateTest {
    private val customer = DemoCustomer(id = 11_711, displayName = "Daniel Davis")
    private val products = listOf(
        product(id = 1, name = "Trail Mountain Bike").toUiModel(),
        product(id = 2, name = "Road Sport Helmet").toUiModel(),
        product(id = 3, name = "All-Weather Jersey").toUiModel(),
    )

    @Test
    fun categorySelectionFiltersProducts() {
        val state = HomeUiState(
            demoCustomer = customer,
            selectedCategory = "Bikes",
            products = products,
        )

        assertEquals(listOf("Trail Mountain Bike"), state.visibleProducts.map { it.name })
    }

    @Test
    fun searchMatchesProductNameIgnoringCase() {
        val state = HomeUiState(
            demoCustomer = customer,
            searchQuery = "helmet",
            products = products,
        )

        assertEquals(listOf("Road Sport Helmet"), state.visibleProducts.map { it.name })
    }

    @Test
    fun searchAndCategoryMustBothMatch() {
        val state = HomeUiState(
            demoCustomer = customer,
            searchQuery = "bike",
            selectedCategory = "Clothing",
            products = products,
        )

        assertEquals(emptyList<ProductUiModel>(), state.visibleProducts)
    }

    @Test
    fun priceAscendingSortsLowestPriceFirst() {
        val state = HomeUiState(
            demoCustomer = customer,
            selectedSort = ProductSort.PriceAscending,
            products = listOf(
                product(id = 1, name = "Expensive", price = 500.0).toUiModel(),
                product(id = 2, name = "Affordable", price = 20.0).toUiModel(),
            ),
        )

        assertEquals(listOf("Affordable", "Expensive"), state.visibleProducts.map { it.name })
    }

    @Test
    fun priceDescendingSortsHighestPriceFirst() {
        val state = HomeUiState(
            demoCustomer = customer,
            selectedSort = ProductSort.PriceDescending,
            products = listOf(
                product(id = 1, name = "Affordable", price = 20.0).toUiModel(),
                product(id = 2, name = "Expensive", price = 500.0).toUiModel(),
            ),
        )

        assertEquals(listOf("Expensive", "Affordable"), state.visibleProducts.map { it.name })
    }

    @Test
    fun searchAlsoMatchesProductNumber() {
        val state = HomeUiState(
            demoCustomer = customer,
            searchQuery = "PN-2",
            products = products,
        )

        assertEquals(listOf("Road Sport Helmet"), state.visibleProducts.map { it.name })
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
    }

    private fun product(id: Int, name: String, price: Double = 10.0) = Product(
        id = id,
        name = name,
        productNumber = "PN-$id",
        thumbnailBase64 = null,
        listPrice = price,
    )
}

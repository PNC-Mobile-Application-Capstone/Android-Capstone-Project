package com.pnc.adventureworksmobile.presentation.home

import com.pnc.adventureworksmobile.domain.model.ProductAvailability
import com.pnc.adventureworksmobile.testutil.builders.aProduct
import java.math.BigDecimal
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class ProductUiModelTest {

    @Test
    fun `maps every field to the ui model`() {
        val product = aProduct(
            id = 749,
            name = "Road-150 Red, 62",
            productNumber = "BK-R93R-62",
            thumbnailBase64 = "thumbnail-base64",
            thumbnailPhotoFileName = "road150_small.gif",
            listPrice = BigDecimal("3578.27"),
            stockLevel = 133,
        )

        val uiModel = product.toUiModel()

        assertEquals(
            ProductUiModel(
                id = 749,
                name = "Road-150 Red, 62",
                productNumber = "BK-R93R-62",
                category = "Bikes",
                price = "$3,578.27",
                thumbnailBase64 = "thumbnail-base64",
                thumbnailPhotoFileName = "road150_small.gif",
                availability = ProductAvailability.InStock,
                isSaved = false,
            ),
            uiModel,
        )
    }

    @Test
    fun `formats the price as US dollars`() {
        val uiModel = aProduct(listPrice = BigDecimal("48151623.42")).toUiModel()

        assertEquals("$48,151,623.42", uiModel.price)
    }

    @Test
    fun `a zero price is formatted with two decimals`() {
        val uiModel = aProduct(listPrice = BigDecimal.ZERO).toUiModel()

        assertEquals("$0.00", uiModel.price)
    }

    @Test
    fun `prices are rounded to the nearest cent`() {
        val uiModel = aProduct(listPrice = BigDecimal("66.999")).toUiModel()

        assertEquals("$67.00", uiModel.price)
    }

    @Test
    fun `a missing stock level shows availability unknown`() {
        val uiModel = aProduct(stockLevel = null).toUiModel()

        assertEquals(ProductAvailability.Unknown, uiModel.availability)
    }

    @Test
    fun `assigns a category from the product name`() {
        val uiModel = aProduct(name = "Sport-100 Helmet, Red").toUiModel()

        assertEquals("Accessories", uiModel.category)
    }

    @Test
    fun `is not saved unless told otherwise`() {
        val uiModel = aProduct().toUiModel()

        assertFalse(uiModel.isSaved)
    }

    @Test
    fun `carries the saved flag through the ui model`() {
        val uiModel = aProduct().toUiModel(isSaved = true)

        assertTrue(uiModel.isSaved)
    }
}

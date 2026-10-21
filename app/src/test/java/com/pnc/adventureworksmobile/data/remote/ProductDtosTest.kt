package com.pnc.adventureworksmobile.data.remote

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProductDtosTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun productListResponseMapsToDomainModel() {
        val dto = json.decodeFromString<ProductListDto>(
            """
            {
              "productId": 707,
              "name": "Sport-100 Helmet, Red",
              "productNumber": "HL-U509-R",
              "summary": "Universal fit",
              "thumbnailPhoto": null,
              "thumbnailPhotoFileName": "helmet.gif",
              "color": "Red",
              "listPrice": 34.99,
              "futureField": "ignored"
            }
            """.trimIndent(),
        )

        val product = dto.toDomain()

        assertEquals(707, product.id)
        assertEquals("Sport-100 Helmet, Red", product.name)
        assertEquals("HL-U509-R", product.productNumber)
        assertEquals(34.99, product.listPrice, 0.0)
        assertNull(product.thumbnailBase64)
    }
}

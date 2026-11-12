package com.pnc.adventureworksmobile.data.remote

import com.pnc.adventureworksmobile.domain.model.ProductDetails
import com.pnc.adventureworksmobile.testutil.builders.aProductDetailsDto
import com.pnc.adventureworksmobile.testutil.builders.aProductListDto
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

@Suppress("DANGEROUS_CHARACTERS")
class ProductDtosTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `product list response maps to domain model`() {
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

    @Test
    fun `a blank name becomes "Unnamed product"`() {
        val dto = aProductListDto(name = "   ")

        val product = dto.toDomain()

        assertEquals("Unnamed product", product.name)
    }

    @Test
    fun `a missing name becomes "Unnamed product"`() {
        val dto = aProductListDto(name = null)

        val product = dto.toDomain()

        assertEquals("Unnamed product", product.name)
    }

    @Test
    fun `a missing product number becomes empty`() {
        val dto = aProductListDto(productNumber = null)

        val product = dto.toDomain()

        assertEquals("", product.productNumber)
    }

    @Test
    fun `a missing list price decodes as zero`() {
        val dto = json.decodeFromString<ProductListDto>(
            """{ "productId": 1, "name": "Water Bottle" }""",
        )

        val product = dto.toDomain()

        assertEquals(0.0, product.listPrice, 0.0)
    }

    @Test
    fun `thumbnail photo and file name pass through unchanged`() {
        val dto = aProductListDto(
            thumbnailPhoto = "R0lGODlhUAAxAPcAAA==",
            thumbnailPhotoFileName = "helmet_small.gif",
        )

        val product = dto.toDomain()

        assertEquals("R0lGODlhUAAxAPcAAA==", product.thumbnailBase64)
        assertEquals("helmet_small.gif", product.thumbnailPhotoFileName)
    }

    @Test
    fun `missing thumbnail fields stay null`() {
        val dto = aProductListDto(thumbnailPhoto = null, thumbnailPhotoFileName = null)

        val product = dto.toDomain()

        assertNull(product.thumbnailBase64)
        assertNull(product.thumbnailPhotoFileName)
    }

    @Test
    fun `a non-blank name is kept as-is without trimming`() {
        val dto = aProductListDto(name = "  Road-150 Red, 62 ")

        val product = dto.toDomain()

        assertEquals("  Road-150 Red, 62 ", product.name)
    }

    @Test
    fun `an empty name becomes "Unnamed product"`() {
        val product = aProductListDto(name = "").toDomain()

        assertEquals("Unnamed product", product.name)
    }

    @Test
    fun `optional list fields default when absent from json`() {
        val dto = json.decodeFromString<ProductListDto>("""{ "productId": 42 }""")

        val product = dto.toDomain()

        assertEquals(42, product.id)
        assertEquals("Unnamed product", product.name)
        assertEquals("", product.productNumber)
        assertNull(product.thumbnailBase64)
        assertNull(product.thumbnailPhotoFileName)
        assertEquals(0.0, product.listPrice, 0.0)
    }

    @Test
    fun `a list response without productId fails to decode`() {
        assertThrows(SerializationException::class.java) {
            json.decodeFromString<ProductListDto>("""{ "name": "Water Bottle" }""")
        }
    }

    @Test
    fun `a json array of list products decodes in order`() {
        val dtos = json.decodeFromString<List<ProductListDto>>(
            """
            [
              { "productId": 1, "name": "First", "listPrice": 1.5 },
              { "productId": 2, "name": "Second", "listPrice": 2.5 }
            ]
            """.trimIndent(),
        )

        val products = dtos.map { it.toDomain() }

        assertEquals(listOf(1, 2), products.map { it.id })
        assertEquals(listOf("First", "Second"), products.map { it.name })
        assertEquals(listOf(1.5, 2.5), products.map { it.listPrice })
    }

    @Test
    fun `product details response maps every field to domain model`() {
        val dto = json.decodeFromString<ProductDetailsDto>(
            """
            {
              "productId": 749,
              "productName": "Road-150 Red, 62",
              "productNumber": "BK-R93R-62",
              "summary": "This bike is ridden by race winners.",
              "photo": "photo-base64",
              "photoFileName": "road150.gif",
              "color": "Red",
              "listPrice": 3578.27,
              "productLine": "R",
              "productModelName": "Road-150",
              "riderExperience": "Advanced",
              "manufacturer": "AdventureWorks",
              "material": "Aluminum",
              "style": "Unisex",
              "warrantyPeriod": "4 years",
              "warrantyDescription": "parts and labor",
              "maintenanceDescription": "maintenance contract available",
              "futureField": "ignored"
            }
            """.trimIndent(),
        )

        val details = dto.toDomain()

        assertEquals(
            ProductDetails(
                id = 749,
                name = "Road-150 Red, 62",
                productNumber = "BK-R93R-62",
                summary = "This bike is ridden by race winners.",
                photoBase64 = "photo-base64",
                photoFileName = "road150.gif",
                color = "Red",
                listPrice = 3578.27,
                productLine = "R",
                productModelName = "Road-150",
                riderExperience = "Advanced",
                manufacturer = "AdventureWorks",
                material = "Aluminum",
                style = "Unisex",
                warrantyPeriod = "4 years",
                warrantyDescription = "parts and labor",
                maintenanceDescription = "maintenance contract available",
            ),
            details,
        )
    }

    @Test
    fun `a blank details name becomes "Unnamed product"`() {
        val details = aProductDetailsDto(productName = "  ").toDomain()

        assertEquals("Unnamed product", details.name)
    }

    @Test
    fun `a missing details name becomes "Unnamed product"`() {
        val details = aProductDetailsDto(productName = null).toDomain()

        assertEquals("Unnamed product", details.name)
    }

    @Test
    fun `a missing details product number becomes empty`() {
        val details = aProductDetailsDto(productNumber = null).toDomain()

        assertEquals("", details.productNumber)
    }

    @Test
    fun `a missing summary becomes empty`() {
        val details = aProductDetailsDto(summary = null).toDomain()

        assertEquals("", details.summary)
    }

    @Test
    fun `missing optional details fields stay null`() {
        val details = aProductDetailsDto(photo = null, photoFileName = null, color = null).toDomain()

        assertNull(details.photoBase64)
        assertNull(details.photoFileName)
        assertNull(details.color)
        assertNull(details.productLine)
        assertNull(details.productModelName)
        assertNull(details.riderExperience)
        assertNull(details.manufacturer)
        assertNull(details.material)
        assertNull(details.style)
        assertNull(details.warrantyPeriod)
        assertNull(details.warrantyDescription)
        assertNull(details.maintenanceDescription)
    }

    @Test
    fun `optional details fields default when absent from json`() {
        val dto = json.decodeFromString<ProductDetailsDto>("""{ "productId": 7 }""")

        val details = dto.toDomain()

        assertEquals(7, details.id)
        assertEquals("Unnamed product", details.name)
        assertEquals("", details.productNumber)
        assertEquals("", details.summary)
        assertEquals(0.0, details.listPrice, 0.0)
        assertNull(details.photoBase64)
        assertNull(details.color)
    }

    @Test
    fun `a details response without productId fails to decode`() {
        assertThrows(SerializationException::class.java) {
            json.decodeFromString<ProductDetailsDto>("""{ "productName": "Road-150" }""")
        }
    }
}

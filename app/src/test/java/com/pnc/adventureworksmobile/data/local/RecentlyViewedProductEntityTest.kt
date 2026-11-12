package com.pnc.adventureworksmobile.data.local

import com.pnc.adventureworksmobile.domain.model.Product
import com.pnc.adventureworksmobile.testutil.builders.aProductDetails
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Test

class RecentlyViewedProductEntityTest {

    @Test
    fun `product details become an entity stamped with the given view time`() {
        val details = aProductDetails(
            id = 749,
            name = "Road-150 Red",
            productNumber = "BK-R93R-62",
            listPrice = 3578.27,
            photoBase64 = "photo-base64",
            photoFileName = "road150.gif",
        )

        val entity = details.toRecentlyViewedEntity(viewedAtEpochMillis = 1000L)

        assertEquals(
            RecentlyViewedProductEntity(
                productId = 749,
                viewedAtEpochMillis = 1000L,
                name = "Road-150 Red",
                productNumber = "BK-R93R-62",
                listPrice = 3578.27,
                photoBase64 = "photo-base64",
                photoFileName = "road150.gif"
            ),
            entity
        )
    }

    @Test
    fun `missing photo fields stay null in the entity`() {
        val details = aProductDetails(photoBase64 = null, photoFileName = null)

        val entity = details.toRecentlyViewedEntity(viewedAtEpochMillis = 1000L)

        assertNull(entity.photoBase64)
        assertNull(entity.photoFileName)
    }

    @Test
    fun `an entity becomes a product with its photo as the thumbnail`() {
        val entity = RecentlyViewedProductEntity(
            productId = 250208,
            viewedAtEpochMillis = 1000L,
            name = "Road-150 Red",
            productNumber = "BK-R93R-62",
            listPrice = 3578.27,
            photoBase64 = "photo-base64",
            photoFileName = "road150.gif",
        )

        val product = entity.toProduct()

        assertEquals(
            Product(
                id = 250208,
                name = "Road-150 Red",
                productNumber = "BK-R93R-62",
                thumbnailBase64 = "photo-base64",
                thumbnailPhotoFileName = "road150.gif",
                listPrice = 3578.27
            ),
            product
        )

    }

    @Test
    fun `details survive the round trip to a product`() {
        val details = aProductDetails(
            id = 250208,
            name = "Road-150 Red",
            productNumber = "BK-R93R-62",
            listPrice = 3578.27
        )

        val product = details.toRecentlyViewedEntity(viewedAtEpochMillis = 1000L).toProduct()

        assertEquals(details.id, product.id)
        assertEquals(details.name, product.name)
        assertEquals(details.productNumber, product.productNumber)
        assertEquals(details.listPrice, product.listPrice, 0.0)
        assertEquals(details.photoBase64, product.thumbnailBase64)
        assertEquals(details.photoFileName, product.thumbnailPhotoFileName)
    }
}
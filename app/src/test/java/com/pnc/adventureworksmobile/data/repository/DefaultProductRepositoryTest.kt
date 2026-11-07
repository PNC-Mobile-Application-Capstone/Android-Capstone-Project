package com.pnc.adventureworksmobile.data.repository

import com.pnc.adventureworksmobile.testutil.builders.aProduct

import app.cash.turbine.test
import com.pnc.adventureworksmobile.data.local.FakeRecentlyViewedProductDao
import com.pnc.adventureworksmobile.data.local.FakeSavedProductDao
import com.pnc.adventureworksmobile.data.local.RecentlyViewedProductDao
import com.pnc.adventureworksmobile.data.local.RecentlyViewedProductEntity
import com.pnc.adventureworksmobile.data.local.SavedProductDao
import com.pnc.adventureworksmobile.data.local.SavedProductEntity
import com.pnc.adventureworksmobile.data.local.toProduct
import com.pnc.adventureworksmobile.data.remote.FakeProductApi
import com.pnc.adventureworksmobile.data.remote.toDomain
import com.pnc.adventureworksmobile.testutil.builders.aProductDetails
import com.pnc.adventureworksmobile.testutil.builders.aProductDetailsDto
import com.pnc.adventureworksmobile.testutil.builders.aProductListDto
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.IOException

class DefaultProductRepositoryTest {

    private val api = FakeProductApi()
    private val savedDao = FakeSavedProductDao()
    private val recentDao = FakeRecentlyViewedProductDao()
    private val repository = DefaultProductRepository(api, savedDao, recentDao)

    private fun savedRow(productId: Int, name: String = "Product $productId", listPrice: Double = 10.0) =
        SavedProductEntity(
            productId = productId,
            savedAtEpochMillis = 1_000L,
            name = name,
            productNumber = "PN-$productId",
            listPrice = listPrice,
            thumbnailBase64 = null,
            thumbnailPhotoFileName = null,
        )

    private fun recentRow(productId: Int, viewedAt: Long = 1_000L) = RecentlyViewedProductEntity(
        productId = productId,
        viewedAtEpochMillis = viewedAt,
        name = "Product $productId",
        productNumber = "PN-$productId",
        listPrice = 10.0,
        photoBase64 = null,
        photoFileName = null,
    )

    // getProducts
    @Test
    fun `getProducts maps every product from the API`() = runTest {
        api.products = listOf(
            aProductListDto(productId = 1, name = "Product 1"),
            aProductListDto(productId = 2, name = "Product 2"),
        )

        val products = repository.getProducts().getOrThrow()

        assertEquals(api.products.map { it.toDomain() }, products)
    }

    @Test
    fun `getProducts returns an empty list when the API has none`() = runTest {
        val products = repository.getProducts().getOrThrow()

        assertTrue(products.isEmpty())
    }

    @Test
    fun `a getProducts error comes back as a failed Result`() = runTest {
        api.failure = IOException("offline")

        val result = repository.getProducts()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
    }

    @Test
    fun `getProducts refreshes the snapshot of products that are saved`() = runTest {
        savedDao.saved.value = listOf(savedRow(749, name = "Old name", listPrice = 1.0))
        api.products = listOf(aProductListDto(productId = 749, name = "New name", listPrice = 99.0))

        repository.getProducts()

        val row = savedDao.saved.value.single()
        assertEquals("New name", row.name)
        assertEquals(99.0, row.listPrice, 0.0)
        assertEquals(1_000L, row.savedAtEpochMillis)   // keeps when it was originally saved
    }

    @Test
    fun `getProducts does not save products that were not already saved`() = runTest {
        savedDao.saved.value = listOf(savedRow(749))
        api.products = listOf(aProductListDto(productId = 749), aProductListDto(productId = 750))

        repository.getProducts()

        assertEquals(listOf(749), savedDao.saved.value.map { it.productId })
    }


    // getProduct
    @Test
    fun `getProduct asks the API for that product id`() = runTest {
        repository.getProduct(productId = 749)

        assertEquals(749, api.lastProductId)
    }

    @Test
    fun `getProduct returns the mapped details`() = runTest {
        api.productDetails = aProductDetailsDto(productId = 749, productName = "Road-150 Red, 62")

        val details = repository.getProduct(productId = 749).getOrThrow()

        assertEquals(api.productDetails.toDomain(), details)
    }

    @Test
    fun `a getProduct error comes back as a failed Result`() = runTest {
        api.failure = IOException("offline")

        val result = repository.getProduct(productId = 749)

        assertTrue(result.isFailure)
        assertEquals(749, api.lastProductId)
    }


    // setSaved
    @Test
    fun `saving a product stores a snapshot of it`() = runTest {
        val product = aProduct(
            id = 749,
            name = "Road-150 Red, 62",
            productNumber = "BK-R93R-62",
            listPrice = 3578.27,
            thumbnailBase64 = "thumbnail-base64",
            thumbnailPhotoFileName = "road150.gif",
        )

        repository.saveProduct(product)

        assertEquals(product, savedDao.saved.value.single().toProduct())
    }

    @Test
    fun `saving a product stamps it with the current time`() = runTest {
        val before = System.currentTimeMillis()

        repository.saveProduct(aProduct(id = 749))

        val after = System.currentTimeMillis()
        val stamp = savedDao.saved.value.single().savedAtEpochMillis
        assertTrue(stamp in before..after)
    }

    @Test
    fun `removing a saved product removes only that product`() = runTest {
        savedDao.saved.value = listOf(savedRow(749), savedRow(750))

        repository.removeSavedProduct(749)

        assertEquals(listOf(750), savedDao.saved.value.map { it.productId })
    }

    @Test
    fun `removing a product that is not saved changes nothing`() = runTest {
        savedDao.saved.value = listOf(savedRow(749), savedRow(750))

        repository.removeSavedProduct(751)

        assertEquals(listOf(749, 750), savedDao.saved.value.map { it.productId })
    }

    @Test
    fun `saving and removing a product does not touch the recently viewed history`() = runTest {
        recentDao.recent.value = listOf(recentRow(99))

        repository.saveProduct(aProduct(id = 1))
        repository.removeSavedProduct(1)

        assertEquals(listOf(recentRow(99)), recentDao.recent.value)
        assertTrue(recentDao.calls.isEmpty())
    }


    // recordRecentlyViewed

    @Test
    fun `recording a view stores the product`() = runTest {
        val details = aProductDetails(id = 749, name = "Road-150 Red, 62", listPrice = 3578.27)

        repository.recordRecentlyViewed(details)

        val entity = recentDao.recent.value.single()
        assertEquals(749, entity.productId)
        assertEquals("Road-150 Red, 62", entity.name)
        assertEquals(3578.27, entity.listPrice, 0.0)
    }

    @Test
    fun `recording a view stamps it with the current time`() = runTest {
        val before = System.currentTimeMillis()

        repository.recordRecentlyViewed(aProductDetails(id = 749))

        val after = System.currentTimeMillis()
        val stamp = recentDao.recent.value.single().viewedAtEpochMillis
        assertTrue(stamp in before..after)
    }

    @Test
    fun `recording a view trims the history afterwards`() = runTest {
        repository.recordRecentlyViewed(aProductDetails(id = 749))

        assertEquals(listOf("record 749", "trim"), recentDao.calls)
    }


    // observeSavedProductIds
    @Test
    fun `saved products start out empty`() = runTest {
        repository.observeSavedProducts().test {
            assertTrue(awaitItem().isEmpty())
        }
    }

    @Test
    fun `saved rows come back as products in the DAO's order`() = runTest {
        savedDao.saved.value = listOf(savedRow(750), savedRow(749))

        repository.observeSavedProducts().test {
            assertEquals(listOf(750, 749), awaitItem().map { it.id })
        }
    }

    @Test
    fun `a saved row with a blank name shows as Saved product`() = runTest {
        savedDao.saved.value = listOf(savedRow(749, name = ""))

        repository.observeSavedProducts().test {
            assertEquals("Saved product", awaitItem().single().name)
        }
    }

    @Test
    fun `saving and removing a product updates the saved products`() = runTest {
        repository.observeSavedProducts().test {
            assertTrue(awaitItem().isEmpty())

            repository.saveProduct(aProduct(id = 749))
            assertEquals(listOf(749), awaitItem().map { it.id })

            repository.removeSavedProduct(749)
            assertTrue(awaitItem().isEmpty())
        }
    }

    @Test
    fun `a DAO error is passed through to the saved products`() = runTest {
        val failingDao = object : SavedProductDao by FakeSavedProductDao() {
            override fun observeAll(): Flow<List<SavedProductEntity>> = flow {
                emit(listOf(savedRow(749)))
                throw IOException("db closed")
            }
        }
        val failingRepository = DefaultProductRepository(api, failingDao, recentDao)

        failingRepository.observeSavedProducts().test {
            assertEquals(listOf(749), awaitItem().map { it.id })
            assertEquals("db closed", awaitError().message)
        }
    }


    // observeRecentlyViewedProducts
    @Test
    fun `recently viewed entries come back as products`() = runTest {
        recentDao.recent.value = listOf(recentRow(749))

        repository.observeRecentlyViewedProducts().test {
            assertEquals(listOf(recentRow(749).toProduct()), awaitItem())
        }
    }

    @Test
    fun `recently viewed keeps the DAO's order`() = runTest {
        recentDao.recent.value = listOf(
            recentRow(750, viewedAt = 2_000L),
            recentRow(749, viewedAt = 1_000L),
        )

        repository.observeRecentlyViewedProducts().test {
            assertEquals(listOf(750, 749), awaitItem().map { it.id })
        }
    }

    @Test
    fun `recording a view updates the recently viewed list`() = runTest {
        repository.observeRecentlyViewedProducts().test {
            assertTrue(awaitItem().isEmpty())

            repository.recordRecentlyViewed(aProductDetails(id = 749, name = "Road-150 Red, 62"))

            assertEquals(listOf(749), awaitItem().map { it.id })
        }
    }

    @Test
    fun `recently viewed completes when the DAO flow completes`() = runTest {
        val finiteDao = object : RecentlyViewedProductDao by FakeRecentlyViewedProductDao() {
            override fun observeAll(): Flow<List<RecentlyViewedProductEntity>> =
                flowOf(listOf(recentRow(749)))
        }
        val finiteRepository = DefaultProductRepository(api, savedDao, finiteDao)

        finiteRepository.observeRecentlyViewedProducts().test {
            skipItems(1)
            awaitComplete()
        }
    }
}
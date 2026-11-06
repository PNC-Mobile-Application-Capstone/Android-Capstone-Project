package com.pnc.adventureworksmobile.data.repository

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

    private fun savedRow(productId: Int) =
        SavedProductEntity(productId = productId, savedAtEpochMillis = 1000L)

    private fun recentRow(productId: Int, viewedAt: Long = 1000L) = RecentlyViewedProductEntity(
        productId = productId,
        viewedAtEpochMillis = viewedAt,
        name = "Product $productId",
        productNumber = "PN-$productId",
        listPrice = 10.0,
        photoBase64 = null,
        photoFileName = null
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


    // getProduct
    @Test
    fun `getProduct asks the API for that product id`() = runTest {
        repository.getProduct(productId = 749)

        assertEquals(749, api.lastProductId)
    }

    @Test
    fun `getProduct returns the mapped details`() = runTest {
        api.productDetails =
            aProductDetailsDto(productId = 749, productName = "Road-150 Red, 62")

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
    fun `saving a product stores its id`() = runTest {
        repository.setSaved(productId = 749, saved = true)

        assertEquals(listOf(749), savedDao.saved.value.map { it.productId })
    }

    @Test
    fun `saving a product stamps it with the current time`() = runTest {
        val before = System.currentTimeMillis()

        repository.setSaved(productId = 749, saved = true)

        val after = System.currentTimeMillis()
        val stamp = savedDao.saved.value.single().savedAtEpochMillis
        assertTrue(stamp in before..after)
    }

    @Test
    fun `unsaving a product removes only that product`() = runTest {
        savedDao.saved.value = listOf(
            SavedProductEntity(productId = 749, savedAtEpochMillis = 1000L),
            SavedProductEntity(productId = 750, savedAtEpochMillis = 2000L),
        )
        repository.setSaved(productId = 749, saved = false)

        assertEquals(listOf(750), savedDao.saved.value.map { it.productId })
    }

    @Test
    fun `unsaving a product that is not saved changes nothing`() = runTest {
        savedDao.saved.value = listOf(
            SavedProductEntity(productId = 749, savedAtEpochMillis = 1000L),
            SavedProductEntity(productId = 750, savedAtEpochMillis = 2000L),
        )
        repository.setSaved(productId = 751, saved = false)

        assertEquals(listOf(749, 750), savedDao.saved.value.map { it.productId })
    }


    // recordRecentlyViewed

    @Test
    fun `recording a view stores the product`() = runTest {
        val details = aProductDetails(
            id = 749,
            name = "Road-150 Red, 62",
            listPrice = 3578.27,
        )

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
    fun `saved ids start out empty`() = runTest {
        repository.observeSavedProductIds().test {
            assertEquals(emptySet<Int>(), awaitItem())
        }
    }

    @Test
    fun `saved ids are the product ids from the DAO`() = runTest {
        savedDao.saved.value = listOf(savedRow(749), savedRow(750))

        repository.observeSavedProductIds().test {
            assertEquals(setOf(749, 750), awaitItem())
        }
    }

    @Test
    fun `saving and unsaving a product updates the saved ids`() = runTest {
        repository.observeSavedProductIds().test {
            assertEquals(emptySet<Int>(), awaitItem())

            repository.setSaved(productId = 749, saved = true)
            assertEquals(setOf(749), awaitItem())

            repository.setSaved(productId = 749, saved = false)
            assertEquals(emptySet<Int>(), awaitItem())
        }
    }

    @Test
    fun `a DAO error is passed through to the saved ids`() = runTest {
        val failingDao = object : SavedProductDao by FakeSavedProductDao() {
            override fun observeAll(): Flow<List<SavedProductEntity>> = flow {
                emit(listOf(savedRow(749)))
                throw IOException("db closed")
            }
        }
        val failingRepository = DefaultProductRepository(api, failingDao, recentDao)

        failingRepository.observeSavedProductIds().test {
            assertEquals(setOf(749), awaitItem())
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
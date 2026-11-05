package com.pnc.adventureworksmobile.data.repository

import com.pnc.adventureworksmobile.data.local.RecentlyViewedProductDao
import com.pnc.adventureworksmobile.data.local.RecentlyViewedProductEntity
import com.pnc.adventureworksmobile.data.local.SavedProductDao
import com.pnc.adventureworksmobile.data.local.SavedProductEntity
import com.pnc.adventureworksmobile.data.remote.ProductApi
import com.pnc.adventureworksmobile.data.remote.ProductDetailsDto
import com.pnc.adventureworksmobile.data.remote.ProductListDto
import com.pnc.adventureworksmobile.domain.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultProductRepositoryTest {
    @Test
    fun savingAndRemovingProductDoesNotChangeRecentlyViewedHistory() = runTest {
        val recentProduct = RecentlyViewedProductEntity(
            productId = 99,
            viewedAtEpochMillis = 100L,
            name = "Recently viewed",
            productNumber = "REC-99",
            listPrice = 20.0,
            photoBase64 = null,
            photoFileName = null,
        )
        val savedDao = FakeSavedProductDao()
        val recentlyViewedDao = FakeRecentlyViewedProductDao(listOf(recentProduct))
        val repository = DefaultProductRepository(FakeProductApi(), savedDao, recentlyViewedDao)
        val product = product(1)

        repository.saveProduct(product)

        assertEquals(listOf(1), savedDao.products.value.map { it.productId })
        assertEquals(listOf(recentProduct), recentlyViewedDao.products.value)

        repository.removeSavedProduct(product.id)

        assertEquals(emptyList<SavedProductEntity>(), savedDao.products.value)
        assertEquals(listOf(recentProduct), recentlyViewedDao.products.value)
        assertEquals(0, recentlyViewedDao.recordCalls)
    }
}

private class FakeProductApi : ProductApi {
    override suspend fun getProducts(): List<ProductListDto> = emptyList()

    override suspend fun getProduct(productId: Int): ProductDetailsDto =
        error("Not needed by this test")
}

private class FakeSavedProductDao : SavedProductDao {
    val products = MutableStateFlow<List<SavedProductEntity>>(emptyList())

    override fun observeAll(): Flow<List<SavedProductEntity>> = products

    override suspend fun getSavedProductIds(): List<Int> = products.value.map { it.productId }

    override suspend fun save(product: SavedProductEntity) {
        products.value = listOf(product) + products.value.filterNot { it.productId == product.productId }
    }

    override suspend fun updateSnapshot(
        productId: Int,
        name: String,
        productNumber: String,
        listPrice: Double,
        thumbnailBase64: String?,
        thumbnailPhotoFileName: String?,
    ) {
        products.value = products.value.map { product ->
            if (product.productId == productId) {
                product.copy(
                    name = name,
                    productNumber = productNumber,
                    listPrice = listPrice,
                    thumbnailBase64 = thumbnailBase64,
                    thumbnailPhotoFileName = thumbnailPhotoFileName,
                )
            } else {
                product
            }
        }
    }

    override suspend fun remove(productId: Int) {
        products.value = products.value.filterNot { it.productId == productId }
    }
}

private class FakeRecentlyViewedProductDao(
    initialProducts: List<RecentlyViewedProductEntity>,
) : RecentlyViewedProductDao {
    val products = MutableStateFlow(initialProducts)
    var recordCalls = 0

    override fun observeAll(): Flow<List<RecentlyViewedProductEntity>> = products

    override suspend fun record(product: RecentlyViewedProductEntity) {
        recordCalls += 1
        products.value = listOf(product) + products.value.filterNot { it.productId == product.productId }
    }

    override suspend fun trimToLatestTen() = Unit
}

private fun product(id: Int) = Product(
    id = id,
    name = "Product $id",
    productNumber = "P-$id",
    thumbnailBase64 = null,
    thumbnailPhotoFileName = null,
    listPrice = id * 10.0,
)

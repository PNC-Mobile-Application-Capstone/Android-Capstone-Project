package com.pnc.adventureworksmobile.data.repository

import com.pnc.adventureworksmobile.data.error.safeCall
import com.pnc.adventureworksmobile.data.local.RecentlyViewedProductDao
import com.pnc.adventureworksmobile.data.local.toProduct
import com.pnc.adventureworksmobile.data.local.toRecentlyViewedEntity
import com.pnc.adventureworksmobile.data.local.SavedProductDao
import com.pnc.adventureworksmobile.data.local.toSavedProductEntity
import com.pnc.adventureworksmobile.data.remote.ProductApi
import com.pnc.adventureworksmobile.data.remote.toDomain
import com.pnc.adventureworksmobile.domain.model.Product
import com.pnc.adventureworksmobile.domain.model.ProductDetails
import com.pnc.adventureworksmobile.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DefaultProductRepository(
    private val api: ProductApi,
    private val savedProductDao: SavedProductDao,
    private val recentlyViewedProductDao: RecentlyViewedProductDao,
) : ProductRepository {
    override suspend fun getProducts(): Result<List<Product>> = safeCall {
        api.getProducts().map { it.toDomain() }.also { products ->
            val savedProductIds = savedProductDao.getSavedProductIds().toSet()
            products.filter { it.id in savedProductIds }.forEach { product ->
                savedProductDao.updateSnapshot(
                    productId = product.id,
                    name = product.name,
                    productNumber = product.productNumber,
                    listPrice = product.listPrice,
                    thumbnailBase64 = product.thumbnailBase64,
                    thumbnailPhotoFileName = product.thumbnailPhotoFileName,
                )
            }
        }
    }

    override suspend fun getProduct(productId: Int): Result<ProductDetails> = runCatching {
        api.getProduct(productId).toDomain()
    }

    override fun observeSavedProducts(): Flow<List<Product>> =
        savedProductDao.observeAll().map { products -> products.map { it.toProduct() } }

    override suspend fun saveProduct(product: Product) {
        savedProductDao.save(product.toSavedProductEntity(System.currentTimeMillis()))
    }

    override suspend fun removeSavedProduct(productId: Int) {
        savedProductDao.remove(productId)
    }

    override suspend fun recordRecentlyViewed(product: ProductDetails) {
        recentlyViewedProductDao.record(
            product.toRecentlyViewedEntity(viewedAtEpochMillis = System.currentTimeMillis()),
        )
        recentlyViewedProductDao.trimToLatestTen()
    }

    override fun observeRecentlyViewedProducts(): Flow<List<Product>> =
        recentlyViewedProductDao.observeAll().map { entries -> entries.map { it.toProduct() } }
}

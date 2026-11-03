package com.pnc.adventureworksmobile.data.repository

import com.pnc.adventureworksmobile.data.local.RecentlyViewedProductDao
import com.pnc.adventureworksmobile.data.local.toProduct
import com.pnc.adventureworksmobile.data.local.toRecentlyViewedEntity
import com.pnc.adventureworksmobile.data.local.SavedProductDao
import com.pnc.adventureworksmobile.data.local.SavedProductEntity
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
    override suspend fun getProducts(): Result<List<Product>> = runCatching {
        api.getProducts().map { it.toDomain() }
    }

    override suspend fun getProduct(productId: Int): Result<ProductDetails> = runCatching {
        api.getProduct(productId).toDomain()
    }

    override fun observeSavedProductIds(): Flow<Set<Int>> =
        savedProductDao.observeAll().map { products -> products.mapTo(mutableSetOf()) { it.productId } }

    override suspend fun setSaved(productId: Int, saved: Boolean) {
        if (saved) {
            savedProductDao.save(
                SavedProductEntity(
                    productId = productId,
                    savedAtEpochMillis = System.currentTimeMillis(),
                ),
            )
        } else {
            savedProductDao.remove(productId)
        }
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

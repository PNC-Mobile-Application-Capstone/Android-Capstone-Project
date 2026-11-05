package com.pnc.adventureworksmobile.domain.repository

import com.pnc.adventureworksmobile.domain.model.Product
import com.pnc.adventureworksmobile.domain.model.ProductDetails
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    suspend fun getProducts(): Result<List<Product>>

    suspend fun getProduct(productId: Int): Result<ProductDetails>

    fun observeSavedProducts(): Flow<List<Product>>

    suspend fun saveProduct(product: Product)

    suspend fun removeSavedProduct(productId: Int)

    suspend fun recordRecentlyViewed(product: ProductDetails)

    fun observeRecentlyViewedProducts(): Flow<List<Product>>
}

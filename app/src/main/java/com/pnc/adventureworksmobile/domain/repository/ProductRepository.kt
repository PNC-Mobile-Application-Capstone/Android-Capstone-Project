package com.pnc.adventureworksmobile.domain.repository

import com.pnc.adventureworksmobile.domain.model.Product
import com.pnc.adventureworksmobile.domain.model.ProductDetails
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    suspend fun getProducts(): Result<List<Product>>

    suspend fun getProduct(productId: Int): Result<ProductDetails>

    fun observeSavedProductIds(): Flow<Set<Int>>

    suspend fun setSaved(productId: Int, saved: Boolean)

    suspend fun recordRecentlyViewed(productId: Int)
}

package com.pnc.adventureworksmobile.domain.repository

import com.pnc.adventureworksmobile.domain.model.Product
import com.pnc.adventureworksmobile.domain.model.ProductDetails
import com.pnc.adventureworksmobile.testutil.builders.aProductDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeProductRepository : ProductRepository {

    var productsResult: Result<List<Product>> = Result.success(emptyList())
    var productDetailsResult: Result<ProductDetails> = Result.success(aProductDetails())

    val savedIds = MutableStateFlow<Set<Int>>(emptySet())
    val recentlyViewed = MutableStateFlow<List<Product>>(emptyList())

    var getProductsCalls = 0
        private set
    var lastProductId: Int? = null
        private set
    val setSavedCalls = mutableListOf<Pair<Int, Boolean>>()
    val recordedViews = mutableListOf<ProductDetails>()

    override suspend fun getProducts(): Result<List<Product>> {
        getProductsCalls++
        return productsResult
    }

    override suspend fun getProduct(productId: Int): Result<ProductDetails> {
        lastProductId = productId
        return productDetailsResult
    }

    override fun observeSavedProductIds(): Flow<Set<Int>> = savedIds

    override suspend fun setSaved(productId: Int, saved: Boolean) {
        setSavedCalls += productId to saved
        savedIds.value = if (saved) savedIds.value + productId else savedIds.value - productId
    }

    override suspend fun recordRecentlyViewed(product: ProductDetails) {
        recordedViews += product
    }

    override fun observeRecentlyViewedProducts(): Flow<List<Product>> = recentlyViewed
}
package com.pnc.adventureworksmobile.domain.repository

import com.pnc.adventureworksmobile.domain.model.Product
import com.pnc.adventureworksmobile.domain.model.ProductDetails
import com.pnc.adventureworksmobile.testutil.builders.aProductDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeProductRepository : ProductRepository {

    var productsResult: Result<List<Product>> = Result.success(emptyList())
    var productDetailsResult: Result<ProductDetails> = Result.success(aProductDetails())

    val savedProducts = MutableStateFlow<List<Product>>(emptyList())
    val recentlyViewed = MutableStateFlow<List<Product>>(emptyList())

    var getProductsCalls = 0
        private set
    var lastProductId: Int? = null
        private set
    val savedCalls = mutableListOf<Product>()
    val removedIds = mutableListOf<Int>()
    val recordedViews = mutableListOf<ProductDetails>()

    override suspend fun getProducts(): Result<List<Product>> {
        getProductsCalls++
        return productsResult
    }

    override suspend fun getProduct(productId: Int): Result<ProductDetails> {
        lastProductId = productId
        return productDetailsResult
    }

    override fun observeSavedProducts(): Flow<List<Product>> = savedProducts

    override suspend fun saveProduct(product: Product) {
        savedCalls += product
        savedProducts.value = savedProducts.value.filterNot { it.id == product.id } + product
    }

    override suspend fun removeSavedProduct(productId: Int) {
        removedIds += productId
        savedProducts.value = savedProducts.value.filterNot { it.id == productId }
    }

    override suspend fun recordRecentlyViewed(product: ProductDetails) {
        recordedViews += product
    }

    override fun observeRecentlyViewedProducts(): Flow<List<Product>> = recentlyViewed
}
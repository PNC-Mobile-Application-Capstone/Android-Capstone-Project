package com.pnc.adventureworksmobile.data.remote

import com.pnc.adventureworksmobile.testutil.builders.aProductDetailsDto

class FakeProductApi: ProductApi {

    var products: List<ProductListDto> = emptyList()
    var productDetails: ProductDetailsDto = aProductDetailsDto()
    var failure: Throwable? = null

    var getProductsCalls = 0
        private set

    var lastProductId: Int? = null
        private set

    override suspend fun getProducts(): List<ProductListDto> {
        getProductsCalls++
        failure?.let { throw it }
        return products
    }

    override suspend fun getProduct(productId: Int): ProductDetailsDto {
        lastProductId = productId
        failure?.let { throw it }
        return productDetails
    }
}
package com.pnc.adventureworksmobile.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

interface ProductApi {
    @GET("api/Product")
    suspend fun getProducts(): List<ProductListDto>

    @GET("api/Product/{id}")
    suspend fun getProduct(@Path("id") productId: Int): ProductDetailsDto
}

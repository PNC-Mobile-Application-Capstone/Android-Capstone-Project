package com.pnc.adventureworksmobile.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

interface OrderApi {
    @GET("api/Order/customer")
    suspend fun getOrders(): List<OrderListDto>

    @GET("api/Order/customer/{id}")
    suspend fun getOrder(@Path("id") orderId: Int): OrderDetailsDto
}
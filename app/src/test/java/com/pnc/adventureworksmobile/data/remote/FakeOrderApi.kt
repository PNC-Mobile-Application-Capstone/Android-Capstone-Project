package com.pnc.adventureworksmobile.data.remote

import com.pnc.adventureworksmobile.testutil.builders.anOrderDetailsDto

class FakeOrderApi: OrderApi {

    var orders: List<OrderListDto> = emptyList()
    var orderDetails: OrderDetailsDto = anOrderDetailsDto()
    var failure: Throwable? = null

    var getOrdersCalls = 0
        private set

    var lastOrderId: Int? = null
        private set

    override suspend fun getOrders(): List<OrderListDto> {
        getOrdersCalls++
        failure?.let { throw it }
        return orders
    }

    override suspend fun getOrder(orderId: Int): OrderDetailsDto {
        lastOrderId = orderId
        failure?.let { throw it }
        return orderDetails
    }

}
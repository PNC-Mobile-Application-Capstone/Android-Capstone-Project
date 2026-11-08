package com.pnc.adventureworksmobile.domain.repository

import com.pnc.adventureworksmobile.domain.model.Order
import com.pnc.adventureworksmobile.domain.model.OrderDetails
import com.pnc.adventureworksmobile.testutil.builders.anOrderDetails

class FakeOrderRepository : OrderRepository {

    var ordersResult: Result<List<Order>> = Result.success(emptyList())
    var orderDetailsResult: Result<OrderDetails> = Result.success(anOrderDetails())

    var getOrdersCalls = 0
        private set
    var lastCustomerId: Int? = null
        private set
    var lastOrderId: Int? = null
        private set

    override suspend fun getOrders(customerId: Int): Result<List<Order>> {
        getOrdersCalls++
        lastCustomerId = customerId
        return ordersResult
    }

    override suspend fun getOrder(orderId: Int): Result<OrderDetails> {
        lastOrderId = orderId
        return orderDetailsResult
    }
}
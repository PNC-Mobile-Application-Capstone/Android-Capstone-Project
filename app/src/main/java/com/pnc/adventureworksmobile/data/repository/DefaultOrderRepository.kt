package com.pnc.adventureworksmobile.data.repository

import com.pnc.adventureworksmobile.data.remote.OrderApi
import com.pnc.adventureworksmobile.data.remote.toDomain
import com.pnc.adventureworksmobile.domain.model.Order
import com.pnc.adventureworksmobile.domain.model.OrderDetails
import com.pnc.adventureworksmobile.domain.repository.OrderRepository

class DefaultOrderRepository(
    private val api: OrderApi,
) : OrderRepository {
    override suspend fun getOrders(customerId: Int): Result<List<Order>> = runCatching {
        api.getOrders()
            .filter { it.customerId == customerId }
            .map { it.toDomain() }
    }

    override suspend fun getOrder(orderId: Int): Result<OrderDetails> = runCatching {
        api.getOrder(orderId).toDomain()
    }
}

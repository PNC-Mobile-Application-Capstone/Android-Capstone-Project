package com.pnc.adventureworksmobile.domain.repository

import com.pnc.adventureworksmobile.domain.model.Order
import com.pnc.adventureworksmobile.domain.model.OrderDetails

interface OrderRepository {
    suspend fun getOrders(customerId: Int): Result<List<Order>>

    suspend fun getOrder(orderId: Int): Result<OrderDetails>
}

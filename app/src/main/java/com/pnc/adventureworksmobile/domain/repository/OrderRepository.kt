package com.pnc.adventureworksmobile.domain.repository

import com.pnc.adventureworksmobile.domain.model.OrderDetails
import com.pnc.adventureworksmobile.domain.model.OrderLine

interface OrderRepository {
    suspend fun getOrderLines(customerId: Int): Result<List<OrderLine>>

    suspend fun getOrder(orderId: Int): Result<OrderDetails>
}

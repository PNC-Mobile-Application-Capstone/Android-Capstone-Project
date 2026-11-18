package com.pnc.adventureworksmobile.data.repository

import com.pnc.adventureworksmobile.data.error.safeCall
import com.pnc.adventureworksmobile.data.remote.OrderApi
import com.pnc.adventureworksmobile.data.remote.toDomain
import com.pnc.adventureworksmobile.domain.model.OrderLine
import com.pnc.adventureworksmobile.domain.model.OrderLineDetails
import com.pnc.adventureworksmobile.domain.repository.OrderRepository

class DefaultOrderRepository(
    private val api: OrderApi,
) : OrderRepository {
    override suspend fun getOrderLines(customerId: Int): Result<List<OrderLine>> = safeCall {
        api.getOrders()
            .filter { it.customerId == customerId }
            .map { it.toDomain() }
    }

    override suspend fun getOrderLineDetails(orderLines: List<OrderLine>): Result<List<OrderLineDetails>> = safeCall {
        orderLines.map { api.getOrder(it.id).toDomain() }
    }
}

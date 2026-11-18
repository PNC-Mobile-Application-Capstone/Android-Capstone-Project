package com.pnc.adventureworksmobile.domain.repository

import com.pnc.adventureworksmobile.domain.model.OrderLine
import com.pnc.adventureworksmobile.domain.model.OrderLineDetails

interface OrderRepository {
    suspend fun getOrderLines(customerId: Int): Result<List<OrderLine>>

    suspend fun getOrderLineDetails(orderLines: List<OrderLine>): Result<List<OrderLineDetails>>
}

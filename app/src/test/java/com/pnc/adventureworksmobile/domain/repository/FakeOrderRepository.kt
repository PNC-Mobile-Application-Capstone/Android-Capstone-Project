package com.pnc.adventureworksmobile.domain.repository

import com.pnc.adventureworksmobile.domain.model.OrderLine
import com.pnc.adventureworksmobile.domain.model.OrderLineDetails
import com.pnc.adventureworksmobile.testutil.builders.anOrderLineDetails

class FakeOrderRepository : OrderRepository {

    var orderLinesResult: Result<List<OrderLine>> = Result.success(emptyList())
    var orderLineDetailsResult: Result<List<OrderLineDetails>> = Result.success(listOf(anOrderLineDetails()))

    var getOrderLinesCalls = 0
        private set
    var lastCustomerId: Int? = null
        private set
    var lastOrderLines: List<OrderLine>? = null
        private set

    override suspend fun getOrderLines(customerId: Int): Result<List<OrderLine>> {
        getOrderLinesCalls++
        lastCustomerId = customerId
        return orderLinesResult
    }

    override suspend fun getOrderLineDetails(orderLines: List<OrderLine>): Result<List<OrderLineDetails>> {
        lastOrderLines = orderLines
        return orderLineDetailsResult
    }
}
package com.pnc.adventureworksmobile.data.repository

import com.pnc.adventureworksmobile.data.remote.FakeOrderApi
import com.pnc.adventureworksmobile.data.remote.toDomain
import com.pnc.adventureworksmobile.testutil.builders.anOrderDetailsDto
import com.pnc.adventureworksmobile.testutil.builders.anOrderListDto
import com.pnc.adventureworksmobile.testutil.builders.anOrderLine
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.IOException
import com.pnc.adventureworksmobile.domain.error.AppError
import com.pnc.adventureworksmobile.domain.error.asAppError

class DefaultOrderRepositoryTest {

    private val api = FakeOrderApi()
    private val repository = DefaultOrderRepository(api)

    // getOrderLines

    @Test
    fun `only returns lines for the requested customer`() = runTest {
        api.orders = listOf(
            anOrderListDto(id = 1, customerId = 11711),
            anOrderListDto(id = 2, customerId = 99),
            anOrderListDto(id = 3, customerId = 11711),
        )

        val orders = repository.getOrderLines(customerId = 11711).getOrThrow()

        assertEquals(listOf(1, 3), orders.map { it.id })
    }

    @Test
    fun `returns an empty list when the customer has no orders`() = runTest {
        api.orders = listOf(anOrderListDto(customerId = 99))

        val orders = repository.getOrderLines(customerId = 11_711).getOrThrow()

        assertTrue(orders.isEmpty())
    }

    @Test
    fun `maps each matching line to the domain`() = runTest {
        val line = anOrderListDto(
            id = 7,
            customerId = 11711,
            productName = "Road-150 Red, 62",
        )
        api.orders = listOf(line)

        val orders = repository.getOrderLines(customerId = 11711).getOrThrow()

        assertEquals(listOf(line.toDomain()), orders)
    }

    @Test
    fun `asks the API once per call`() = runTest {
        repository.getOrderLines(customerId = 11711)

        assertEquals(1, api.getOrdersCalls)
    }

    @Test
    fun `an API error comes back as a failed Result instead of throwing`() = runTest {
        api.failure = IOException("offline")

        val result = repository.getOrderLines(customerId = 11711)

        assertTrue(result.isFailure)
        assertEquals(AppError.NoConnection, result.exceptionOrNull()!!.asAppError())
    }

    // getOrderLineDetails

    @Test
    fun `getOrderLineDetails asks the API for order ids`() = runTest {
        val inputLine = anOrderLine(id = 43_659)
        repository.getOrderLineDetails(listOf(inputLine))

        assertEquals(43_659, api.lastOrderId)
    }

    @Test
    fun `getOrderLineDetails returns the mapped details`() = runTest {
        api.orderDetails = anOrderDetailsDto(id = 5, productName = "Road-150 Red, 62")
        val inputLine = anOrderLine(id = 5)

        val details = repository.getOrderLineDetails(listOf(inputLine)).getOrThrow()

        assertEquals(listOf(api.orderDetails.toDomain()), details)
    }

    @Test
    fun `a getOrderLineDetails failure comes back as a failed Result instead of throwing`() = runTest {
        api.failure = IOException("offline")
        val inputLine = anOrderLine(id = 43_659)

        val result = repository.getOrderLineDetails(listOf(inputLine))

        assertTrue(result.isFailure)
        assertEquals(43_659, api.lastOrderId)
    }
}
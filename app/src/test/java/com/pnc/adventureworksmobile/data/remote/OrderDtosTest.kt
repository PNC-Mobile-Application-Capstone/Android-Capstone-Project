package com.pnc.adventureworksmobile.data.remote

import com.pnc.adventureworksmobile.domain.model.Order
import com.pnc.adventureworksmobile.domain.model.OrderDetails
import com.pnc.adventureworksmobile.testutil.builders.anOrderListDto
import com.pnc.adventureworksmobile.testutil.builders.anOrderDetailsDto
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.junit.Assert.assertThrows
import org.junit.Test

class OrderDtosTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `order list response maps every field to domain model`() {
        val dto = json.decodeFromString<OrderListDto>(
            """
            {
              "id": 1,
              "customerId": 11711,
              "firstName": "Daniel",
              "middleName": "M",
              "lastName": "Davis",
              "suffix": "Jr.",
              "orderDate": "2003-09-25T00:00:00",
              "shipDate": "2003-10-02T00:00:00",
              "productName": "Road-150 Red, 62",
              "orderNumber": 51131,
              "orderQty": 2,
              "unitPrice": 2146.96,
              "lineTotal": 4293.92,
              "futureField": "ignored"
            }
            """.trimIndent()
        )

        val order = dto.toDomain()

        assertEquals(
            Order(
                id = 1,
                customerId = 11711,
                firstName = "Daniel",
                middleName = "M",
                lastName = "Davis",
                suffix = "Jr.",
                orderDate = "2003-09-25T00:00:00",
                shipDate = "2003-10-02T00:00:00",
                productName = "Road-150 Red, 62",
                orderNumber = 51131,
                orderQty = 2,
                unitPrice = 2146.96,
                lineTotal = 4293.92,
            ),
            order
        )
    }

    @Test
    fun `optional order list fields default when absent from json`() {
        val dto = json.decodeFromString<OrderListDto>(
            """
            {
              "id": 1,
              "customerId": 11711,
              "orderDate": "2003-09-25T00:00:00",
              "productName": "Road-150 Red",
              "orderNumber": 51131,
              "orderQty": 1
            }
            """.trimIndent()
        )

        val order = dto.toDomain()

        assertEquals(1, order.id)
        assertEquals("Road-150 Red", order.productName)
        assertNull(order.firstName)
        assertNull(order.middleName)
        assertNull(order.lastName)
        assertNull(order.suffix)
        assertNull(order.shipDate)
        assertEquals(0.0, order.unitPrice, 0.0)
        assertEquals(0.0, order.lineTotal, 0.0)
    }

    @Test
    fun `missing customer name fields stay null`() {
        val order = anOrderListDto(
            firstName = null,
            middleName = null,
            lastName = null,
            suffix = null,
        ).toDomain()

        assertNull(order.firstName)
        assertNull(order.middleName)
        assertNull(order.lastName)
        assertNull(order.suffix)
    }

    @Test
    fun `an unshpped order keeps a null ship date`() {
        val order = anOrderListDto(shipDate = null).toDomain()

        assertNull(order.shipDate)
    }

    @Test
    fun `the line total is copied, not recalculated`() {
        val order = anOrderListDto(orderQty = 3, unitPrice = 10.0, lineTotal = 67.0).toDomain()

        assertEquals(67.0, order.lineTotal, 0.0)
    }

    @Test
    fun `an order list response without customerId fails to decode`() {
        assertThrows(SerializationException::class.java) {
            json.decodeFromString<OrderListDto>(
                """
                {
                  "id": 1,
                  "orderDate": "2003-09-25T00:00:00",
                  "productName": "Road-150 Red",
                  "orderNumber": 51131,
                  "orderQty": 1
                }
                """.trimIndent()
            )
        }
    }

    @Test
    fun `an order list response with a null productName fails to decode`() {
        assertThrows(SerializationException::class.java) {
            json.decodeFromString<OrderListDto>(
                """
                {
                  "id": 1,
                  "customerId": 11711,
                  "orderDate": "2003-09-25T00:00:00",
                  "productName": null,
                  "orderNumber": 51131,
                  "orderQty": 1
                } 
                """.trimIndent()
            )
        }
    }

    @Test
    fun `a json array of order lines decodes in order`() {
        val dtos = json.decodeFromString<List<OrderListDto>>(
            """
            [
              {
              "id": 1,
              "customerId": 11711,
              "orderDate": "2003-09-25T00:00:00",
              "productName": "First",
              "orderNumber": 51131,
              "orderQty": 1
              },
              {
              "id": 2,
              "customerId": 11711,
              "orderDate": "2003-09-25T00:00:00",
              "productName": "Second",
              "orderNumber": 51131,
              "orderQty": 5
              }
            ]
            """.trimIndent()
        )

        val orders = dtos.map { it.toDomain() }

        assertEquals(listOf(1, 2), orders.map { it.id })
        assertEquals(listOf("First", "Second"), orders.map { it.productName })
        assertEquals(listOf(1, 5), orders.map { it.orderQty})
    }

    @Test
    fun `order details response maps every field to domain model`() {
        val dto = json.decodeFromString<OrderDetailsDto>(
            """
            {
              "id": 101,
              "customerId": 11711,
              "firstName": "Daniel",
              "middleName": "M",
              "lastName": "Davis",
              "suffix": "Jr.",
              "phoneNumber": "123-456-7890",
              "phoneNumberType": "Cell",
              "emailAddress": "daniel.davis@example.com",
              "orderNumber": 51131,
              "orderDate": "2003-09-25T00:00:00",
              "shipDate": "2003-10-02T00:00:00",
              "freight": 53.6,
              "freightNumber": 3,
              "shipMethodName": "CARGO TRANSPORT 5",
              "taxAmt": 171.76,
              "productId": 749,
              "productName": "Road-150 Red, 62",
              "carrierTrackingNumber": "4911-403C-98",
              "orderQty": 2,
              "unitPrice": 2146.96,
              "unitPriceDiscount": 0.05,
              "lineTotal": 4079.22,
              "futureField": "ignored"
            }
            """.trimIndent()
        )

        val details = dto.toDomain()

        assertEquals(
            OrderDetails(
                id = 101,
                customerId = 11711,
                firstName = "Daniel",
                middleName = "M",
                lastName = "Davis",
                suffix = "Jr.",
                phoneNumber = "123-456-7890",
                phoneNumberType = "Cell",
                emailAddress = "daniel.davis@example.com",
                orderNumber = 51131,
                orderDate = "2003-09-25T00:00:00",
                shipDate = "2003-10-02T00:00:00",
                freight = 53.6,
                freightNumber = 3,
                shipMethodName = "CARGO TRANSPORT 5",
                taxAmt = 171.76,
                productId = 749,
                productName = "Road-150 Red, 62",
                carrierTrackingNumber = "4911-403C-98",
                orderQty = 2,
                unitPrice = 2146.96,
                unitPriceDiscount = 0.05,
                lineTotal = 4079.22,
            ),
            details,
        )
    }

    @Test
    fun `optional order details fields default when absent from json`() {
        val dto = json.decodeFromString<OrderDetailsDto>(
            """
            {
              "id": 101,
              "customerId": 11711,
              "orderNumber": 51131,
              "orderDate": "2003-09-25T00:00:00",
              "productId": 420,
              "orderQty": 2
            }
            """.trimIndent()
        )

        val details = dto.toDomain()

        assertEquals(101, details.id)
        assertEquals(420, details.productId)
        assertNull(details.firstName)
        assertNull(details.phoneNumber)
        assertNull(details.emailAddress)
        assertNull(details.shipDate)
        assertNull(details.shipMethodName)
        assertNull(details.productName)
        assertNull(details.carrierTrackingNumber)
        assertEquals(0.0, details.unitPrice, 0.0)
        assertEquals(0, details.freightNumber)
        assertEquals(0.0, details.taxAmt)
        assertEquals(0.0, details.unitPrice)
        assertEquals(0.0, details.unitPriceDiscount)
        assertEquals(0.0, details.lineTotal)
    }

    @Test
    fun `an empty details product name stays null`() {
        val details = anOrderDetailsDto(productName = null).toDomain()

        assertNull(details.productName)
    }

    @Test
    fun `missing contact fields stay null`() {
        val details = anOrderDetailsDto(
            phoneNumber = null,
            phoneNumberType = null,
            emailAddress = null,
        ).toDomain()

        assertNull(details.phoneNumber)
        assertNull(details.phoneNumberType)
        assertNull(details.emailAddress)
    }

    @Test
    fun `an order details response without productId fails to decode`() {
        assertThrows(SerializationException::class.java) {
            json.decodeFromString<OrderDetailsDto>(
                """
                {
                "id": 101,
                "customerId": 11711,
                "orderNumber": 51131,
                "orderDate": "2003-09-25T00:00:00",
                "orderQty": 2
                }
                """.trimIndent()
            )
        }
    }


}
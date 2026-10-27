package com.pnc.adventureworksmobile.data.remote

import com.pnc.adventureworksmobile.domain.model.MyOrders
import com.pnc.adventureworksmobile.domain.model.Order
import com.pnc.adventureworksmobile.domain.model.OrderLine
import kotlinx.serialization.Serializable


@Serializable
data class MyOrdersResponseDto(
    val customerId: Int,
    val page: Int,
    val pageSize: Int,
    val totalOrders: Int,
    val orders: List<OrderDto>
)

@Serializable
data class OrderDto(
    val orderNumber: Int,
    val orderReference: String? = null,
    val orderDate: String? = null,
    val shipDate: String? = null,
    val status: Int,
    val statusName: String?,
    val subTotal: Double = 0.0,
    val taxAmt: Double = 0.0,
    val freight: Double = 0.0,
    val totalDue: Double = 0.0,
    val lines: List<OrderLineDto>
) {
}

@Serializable
data class OrderLineDto(
    val lineId: Int,
    val productId: Int,
    val productName: String?,
    val quantity: Int,
    val unitPrice: Double = 0.0,
    val unitPriceDiscount: Double = 0.0,
    val lineTotal: Double = 0.0
)

fun MyOrdersResponseDto.toDomain(): MyOrders = MyOrders(
    customerId = customerId,
    page = page,
    pageSize = pageSize,
    totalOrders = totalOrders,
    orders = orders.map {it.toDomain()}
)

fun OrderDto.toDomain(): Order = Order(
    orderNumber = orderNumber,
    reference = orderReference,
    orderDate = orderDate,
    shipDate = shipDate,
    status = status,
    statusName = statusName,
    subTotal = subTotal,
    taxAmt = taxAmt,
    freight = freight,
    totalDue = totalDue,
    lines = lines.map {it.toDomain()}
)

fun OrderLineDto.toDomain(): OrderLine = OrderLine(
    id = lineId,
    productId = productId,
    productName = productName,
    quantity = quantity,
    unitPrice = unitPrice,
    unitPriceDiscount = unitPriceDiscount,
    total = lineTotal
)
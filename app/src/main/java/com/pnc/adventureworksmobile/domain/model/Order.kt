package com.pnc.adventureworksmobile.domain.model

data class MyOrders(
    val customerId: Int,
    val page: Int,
    val pageSize: Int,
    val totalOrders: Int,
    val orders: List<Order>
)

data class Order(
    val orderNumber: Int,
    val reference: String?,
    val orderDate: String?,
    val shipDate: String?,
    val status: Int,
    val statusName: String?,
    val subTotal: Double,
    val taxAmt: Double,
    val freight: Double,
    val totalDue: Double,
    val lines: List<OrderLine>
)

data class OrderLine(
    val id: Int,
    val productId: Int,
    val productName: String?,
    val quantity: Int,
    val unitPrice: Double,
    val unitPriceDiscount: Double,
    val total: Double
)


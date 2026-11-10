package com.pnc.adventureworksmobile.domain.model

data class OrderLine(
    val id: Int,
    val customerId: Int,
    val firstName: String?,
    val middleName: String?,
    val lastName: String?,
    val suffix: String?,
    val orderDate: String,
    val shipDate: String?,
    val productName: String,
    val orderNumber: Int,
    val orderQty: Int,
    val unitPrice: Double,
    val lineTotal: Double
)

data class Order(
    val orderNumber: Int,
    val customerId: Int,
    val firstName: String?,
    val middleName: String?,
    val lastName: String?,
    val suffix: String?,
    val orderDate: String,
    val shipDate: String?,
    var orderLines: List<OrderLine>
)

data class OrderDetails(
    val id: Int,
    val customerId: Int,
    val firstName: String?,
    val middleName: String?,
    val lastName: String?,
    val suffix: String?,
    val phoneNumber: String?,
    val phoneNumberType: String?,
    val emailAddress: String?,
    val orderNumber: Int,
    val orderDate: String,
    val shipDate: String?,
    val freight: Double,
    val freightNumber: Int,
    val shipMethodName: String?,
    val taxAmt: Double,
    val productId: Int,
    val productName: String?,
    val carrierTrackingNumber: String?,
    val orderQty: Int,
    val unitPrice: Double,
    val unitPriceDiscount: Double,
    val lineTotal: Double

)

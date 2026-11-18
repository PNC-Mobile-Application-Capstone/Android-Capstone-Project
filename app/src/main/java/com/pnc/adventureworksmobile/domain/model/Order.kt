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
    val lineTotal: Double,
    val statusName: String? = null,
)

data class Order(
    // these are the values shared by every OrderLine that will be contained in this object, so they can be accessed here
    val orderNumber: Int,
    val customerId: Int,
    val firstName: String?,
    val middleName: String?,
    val lastName: String?,
    val suffix: String?,
    val orderDate: String,
    val shipDate: String?,
    val statusName: String? = null,
    var orderLines: List<OrderLine>
) {
    val totalPrice: Double
        get() = orderLines.sumOf { it.lineTotal }
}

data class OrderLineDetails(
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
    val statusName: String? = null,
    val lineTotal: Double
)

data class OrderDetails(
    // these are the values shared by every OrderLineDetails that will be contained in this object, so they can be accessed here
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
    val carrierTrackingNumber: String?,
    val statusName: String? = null,
    val orderLineDetails: List<OrderLineDetails>
) {
    val subtotal: Double
        get() = orderLineDetails.sumOf { it.lineTotal }

    val totalPrice: Double
        get() = subtotal + freight + taxAmt
}
package com.pnc.adventureworksmobile.data.remote

import com.pnc.adventureworksmobile.domain.model.OrderLineDetails
import com.pnc.adventureworksmobile.domain.model.OrderLine
import kotlinx.serialization.Serializable


@Serializable
data class OrderListDto(
    val id: Int,
    val customerId: Int,
    val firstName: String? = null,
    val middleName: String? = null,
    val lastName: String? = null,
    val suffix: String? = null,
    val orderDate: String,
    val shipDate: String? = null,
    val productName: String,
    val orderNumber: Int,
    val orderQty: Int,
    val unitPrice: Double = 0.0,
    val lineTotal: Double = 0.0,
    val statusName: String? = null
)

@Serializable
data class OrderDetailsDto(
    val id: Int,
    val customerId: Int,
    val firstName: String? = null,
    val middleName: String? = null,
    val lastName: String? = null,
    val suffix: String? = null,
    val phoneNumber: String? = null,
    val phoneNumberType: String? = null,
    val emailAddress: String? = null,
    val orderNumber: Int,
    val orderDate: String,
    val shipDate: String? = null,
    val freight: Double = 0.0,
    val freightNumber: Int = 0,
    val shipMethodName: String? = null,
    val taxAmt: Double = 0.0,
    val productId: Int,
    val productName: String? = null,
    val carrierTrackingNumber: String? = null,
    val orderQty: Int,
    val unitPrice: Double = 0.0,
    val unitPriceDiscount: Double = 0.0,
    val lineTotal: Double = 0.0,
    val statusName: String? = null
)

fun OrderListDto.toDomain(): OrderLine = OrderLine(
        id = id,
        customerId = customerId,
        firstName = firstName,
        middleName = middleName,
        lastName = lastName,
        suffix = suffix,
        orderDate = orderDate,
        shipDate = shipDate,
        productName = productName,
        orderNumber = orderNumber,
        orderQty = orderQty,
        unitPrice = unitPrice,
        lineTotal = lineTotal,
        statusName = statusName
    )

fun OrderDetailsDto.toDomain(): OrderLineDetails = OrderLineDetails(
        id = id,
        customerId = customerId,
        firstName = firstName,
        middleName = middleName,
        lastName = lastName,
        suffix = suffix,
        phoneNumber = phoneNumber,
        phoneNumberType = phoneNumberType,
        emailAddress = emailAddress,
        orderNumber = orderNumber,
        orderDate = orderDate,
        shipDate = shipDate,
        freight = freight,
        freightNumber = freightNumber,
        shipMethodName = shipMethodName,
        taxAmt = taxAmt,
        productId = productId,
        productName = productName,
        carrierTrackingNumber = carrierTrackingNumber,
        orderQty = orderQty,
        unitPrice = unitPrice,
        unitPriceDiscount = unitPriceDiscount,
        lineTotal = lineTotal,
        statusName = statusName,
    )
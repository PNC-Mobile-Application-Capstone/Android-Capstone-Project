package com.pnc.adventureworksmobile.domain.model

import java.math.BigDecimal

data class Product(
    val id: Int,
    val name: String,
    val productNumber: String,
    val thumbnailBase64: String?,
    val thumbnailPhotoFileName: String?,
    val listPrice: BigDecimal,
    val stockLevel: Int? = null,
)

data class ProductDetails(
    val id: Int,
    val name: String,
    val productNumber: String,
    val summary: String,
    val photoBase64: String?,
    val photoFileName: String?,
    val color: String?,
    val listPrice: BigDecimal,
    val productLine: String?,
    val productModelName: String?,
    val riderExperience: String?,
    val manufacturer: String?,
    val material: String?,
    val style: String?,
    val warrantyPeriod: String?,
    val warrantyDescription: String?,
    val maintenanceDescription: String?,
    val stockLevel: Int? = null,
)

fun ProductDetails.toProductSummary() = Product(
    id = id,
    name = name,
    productNumber = productNumber,
    thumbnailBase64 = photoBase64,
    thumbnailPhotoFileName = photoFileName,
    listPrice = listPrice,
    stockLevel = stockLevel,
)

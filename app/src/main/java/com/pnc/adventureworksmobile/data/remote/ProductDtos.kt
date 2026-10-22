package com.pnc.adventureworksmobile.data.remote

import com.pnc.adventureworksmobile.domain.model.Product
import com.pnc.adventureworksmobile.domain.model.ProductDetails
import kotlinx.serialization.Serializable

@Serializable
data class ProductListDto(
    val productId: Int,
    val name: String? = null,
    val productNumber: String? = null,
    val thumbnailPhoto: String? = null,
    val listPrice: Double = 0.0,
)

@Serializable
data class ProductDetailsDto(
    val productId: Int,
    val productName: String? = null,
    val productNumber: String? = null,
    val summary: String? = null,
    val photo: String? = null,
    val color: String? = null,
    val listPrice: Double = 0.0,
    val productLine: String? = null,
    val productModelName: String? = null,
    val riderExperience: String? = null,
    val manufacturer: String? = null,
    val material: String? = null,
    val style: String? = null,
    val warrantyPeriod: String? = null,
    val warrantyDescription: String? = null,
    val maintenanceDescription: String? = null,
)

fun ProductListDto.toDomain(): Product = Product(
    id = productId,
    name = name.orEmpty().ifBlank { "Unnamed product" },
    productNumber = productNumber.orEmpty(),
    thumbnailBase64 = thumbnailPhoto,
    listPrice = listPrice,
)

fun ProductDetailsDto.toDomain(): ProductDetails = ProductDetails(
    id = productId,
    name = productName.orEmpty().ifBlank { "Unnamed product" },
    productNumber = productNumber.orEmpty(),
    summary = summary.orEmpty(),
    photoBase64 = photo,
    color = color,
    listPrice = listPrice,
    productLine = productLine,
    productModelName = productModelName,
    riderExperience = riderExperience,
    manufacturer = manufacturer,
    material = material,
    style = style,
    warrantyPeriod = warrantyPeriod,
    warrantyDescription = warrantyDescription,
    maintenanceDescription = maintenanceDescription,
)

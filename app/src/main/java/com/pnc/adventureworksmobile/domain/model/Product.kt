package com.pnc.adventureworksmobile.domain.model

data class Product(
    val id: Int,
    val name: String,
    val productNumber: String,
    val thumbnailBase64: String?,
    val listPrice: Double,
)

data class ProductDetails(
    val id: Int,
    val name: String,
    val productNumber: String,
    val summary: String,
    val photoBase64: String?,
    val color: String?,
    val listPrice: Double,
    val productLine: String?,
    val productModelName: String?,
    val riderExperience: String?,
    val manufacturer: String?,
    val material: String?,
    val style: String?,
    val warrantyPeriod: String?,
    val warrantyDescription: String?,
    val maintenanceDescription: String?,
)

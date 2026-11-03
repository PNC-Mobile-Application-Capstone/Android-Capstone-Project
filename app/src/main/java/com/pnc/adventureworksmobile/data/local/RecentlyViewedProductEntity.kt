package com.pnc.adventureworksmobile.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pnc.adventureworksmobile.domain.model.Product
import com.pnc.adventureworksmobile.domain.model.ProductDetails

@Entity(tableName = "recently_viewed_products")
data class RecentlyViewedProductEntity(
    @PrimaryKey val productId: Int,
    val viewedAtEpochMillis: Long,
    val name: String,
    val productNumber: String,
    val listPrice: Double,
    val photoBase64: String?,
    val photoFileName: String?,
)

fun ProductDetails.toRecentlyViewedEntity(viewedAtEpochMillis: Long) = RecentlyViewedProductEntity(
    productId = id,
    viewedAtEpochMillis = viewedAtEpochMillis,
    name = name,
    productNumber = productNumber,
    listPrice = listPrice,
    photoBase64 = photoBase64,
    photoFileName = photoFileName,
)

fun RecentlyViewedProductEntity.toProduct() = Product(
    id = productId,
    name = name,
    productNumber = productNumber,
    thumbnailBase64 = photoBase64,
    thumbnailPhotoFileName = photoFileName,
    listPrice = listPrice,
)

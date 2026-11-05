package com.pnc.adventureworksmobile.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pnc.adventureworksmobile.domain.model.Product

@Entity(tableName = "saved_products")
data class SavedProductEntity(
    @PrimaryKey val productId: Int,
    val savedAtEpochMillis: Long,
    @ColumnInfo(defaultValue = "''") val name: String,
    @ColumnInfo(defaultValue = "''") val productNumber: String,
    @ColumnInfo(defaultValue = "0.0") val listPrice: Double,
    val thumbnailBase64: String?,
    val thumbnailPhotoFileName: String?,
)

fun Product.toSavedProductEntity(savedAtEpochMillis: Long) = SavedProductEntity(
    productId = id,
    savedAtEpochMillis = savedAtEpochMillis,
    name = name,
    productNumber = productNumber,
    listPrice = listPrice,
    thumbnailBase64 = thumbnailBase64,
    thumbnailPhotoFileName = thumbnailPhotoFileName,
)

fun SavedProductEntity.toProduct() = Product(
    id = productId,
    name = name.ifBlank { "Saved product" },
    productNumber = productNumber,
    listPrice = listPrice,
    thumbnailBase64 = thumbnailBase64,
    thumbnailPhotoFileName = thumbnailPhotoFileName,
)

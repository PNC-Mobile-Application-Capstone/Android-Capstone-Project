package com.pnc.adventureworksmobile.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedProductDao {
    @Query("SELECT * FROM saved_products ORDER BY savedAtEpochMillis DESC")
    fun observeAll(): Flow<List<SavedProductEntity>>

    @Query("SELECT productId FROM saved_products")
    suspend fun getSavedProductIds(): List<Int>

    @Upsert
    suspend fun save(product: SavedProductEntity)

    @Query(
        "UPDATE saved_products SET name = :name, productNumber = :productNumber, " +
            "listPrice = :listPrice, thumbnailBase64 = :thumbnailBase64, " +
            "thumbnailPhotoFileName = :thumbnailPhotoFileName WHERE productId = :productId",
    )
    suspend fun updateSnapshot(
        productId: Int,
        name: String,
        productNumber: String,
        listPrice: Double,
        thumbnailBase64: String?,
        thumbnailPhotoFileName: String?,
    )

    @Query("DELETE FROM saved_products WHERE productId = :productId")
    suspend fun remove(productId: Int)
}

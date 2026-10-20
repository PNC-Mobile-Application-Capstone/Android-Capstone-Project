package com.pnc.adventureworksmobile.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedProductDao {
    @Query("SELECT * FROM saved_products ORDER BY savedAtEpochMillis DESC")
    fun observeAll(): Flow<List<SavedProductEntity>>

    @Upsert
    suspend fun save(product: SavedProductEntity)

    @Query("DELETE FROM saved_products WHERE productId = :productId")
    suspend fun remove(productId: Int)
}

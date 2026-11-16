package com.pnc.adventureworksmobile.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentlyViewedProductDao {
    @Query("SELECT * FROM recently_viewed_products ORDER BY viewedAtEpochMillis DESC")
    fun observeAll(): Flow<List<RecentlyViewedProductEntity>>

    @Upsert
    suspend fun record(product: RecentlyViewedProductEntity)

    @Query(
        "DELETE FROM recently_viewed_products WHERE productId NOT IN " +
            "(SELECT productId FROM recently_viewed_products ORDER BY viewedAtEpochMillis DESC LIMIT 10)",
    )
    suspend fun trimToLatestTen()

    // Saves the view and trims the list in one transaction, so the table never holds more than 10.
    @Transaction
    suspend fun recordAndTrim(product: RecentlyViewedProductEntity) {
        record(product)
        trimToLatestTen()
    }

    // Only touches the recently viewed table. Saved products stay.
    @Query("DELETE FROM recently_viewed_products")
    suspend fun clearAll()
}

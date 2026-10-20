package com.pnc.adventureworksmobile.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentlyViewedProductDao {
    @Query("SELECT * FROM recently_viewed_products ORDER BY viewedAtEpochMillis DESC LIMIT 10")
    fun observeLatest(): Flow<List<RecentlyViewedProductEntity>>

    @Upsert
    suspend fun record(product: RecentlyViewedProductEntity)

    @Query(
        "DELETE FROM recently_viewed_products WHERE productId NOT IN " +
            "(SELECT productId FROM recently_viewed_products ORDER BY viewedAtEpochMillis DESC LIMIT 10)",
    )
    suspend fun trimToLatestTen()

    @Query("DELETE FROM recently_viewed_products")
    suspend fun clear()
}

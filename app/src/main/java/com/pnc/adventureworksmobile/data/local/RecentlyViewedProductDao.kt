package com.pnc.adventureworksmobile.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface RecentlyViewedProductDao {
    @Upsert
    suspend fun record(product: RecentlyViewedProductEntity)

    @Query(
        "DELETE FROM recently_viewed_products WHERE productId NOT IN " +
            "(SELECT productId FROM recently_viewed_products ORDER BY viewedAtEpochMillis DESC LIMIT 10)",
    )
    suspend fun trimToLatestTen()

}

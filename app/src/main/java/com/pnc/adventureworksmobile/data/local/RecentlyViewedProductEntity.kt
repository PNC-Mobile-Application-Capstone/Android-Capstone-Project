package com.pnc.adventureworksmobile.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recently_viewed_products")
data class RecentlyViewedProductEntity(
    @PrimaryKey val productId: Int,
    val viewedAtEpochMillis: Long,
)

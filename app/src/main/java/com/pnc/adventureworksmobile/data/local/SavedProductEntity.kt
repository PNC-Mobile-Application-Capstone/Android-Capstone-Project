package com.pnc.adventureworksmobile.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_products")
data class SavedProductEntity(
    @PrimaryKey val productId: Int,
    val savedAtEpochMillis: Long,
)

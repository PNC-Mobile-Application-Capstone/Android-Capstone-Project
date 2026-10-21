package com.pnc.adventureworksmobile.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        SavedProductEntity::class,
        RecentlyViewedProductEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class AdventureWorksDatabase : RoomDatabase() {
    abstract fun savedProductDao(): SavedProductDao

    abstract fun recentlyViewedProductDao(): RecentlyViewedProductDao
}

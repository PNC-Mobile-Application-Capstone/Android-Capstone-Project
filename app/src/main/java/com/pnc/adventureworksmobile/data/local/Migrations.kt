package com.pnc.adventureworksmobile.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS `recently_viewed_products`")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `recently_viewed_products` (" +
                "`productId` INTEGER NOT NULL, " +
                "`viewedAtEpochMillis` INTEGER NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`productNumber` TEXT NOT NULL, " +
                "`listPrice` REAL NOT NULL, " +
                "`photoBase64` TEXT, " +
                "`photoFileName` TEXT, " +
                "PRIMARY KEY(`productId`))",
        )
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `saved_products` ADD COLUMN `name` TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE `saved_products` ADD COLUMN `productNumber` TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE `saved_products` ADD COLUMN `listPrice` REAL NOT NULL DEFAULT 0.0")
        db.execSQL("ALTER TABLE `saved_products` ADD COLUMN `thumbnailBase64` TEXT")
        db.execSQL("ALTER TABLE `saved_products` ADD COLUMN `thumbnailPhotoFileName` TEXT")
    }
}

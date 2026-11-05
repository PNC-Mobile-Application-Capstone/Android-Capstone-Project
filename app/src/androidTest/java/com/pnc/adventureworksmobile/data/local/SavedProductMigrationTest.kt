package com.pnc.adventureworksmobile.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SavedProductMigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AdventureWorksDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory(),
    )

    @Test
    fun migrate2To3PreservesSavedIdsAndAddsSnapshotFields() {
        helper.createDatabase(TEST_DATABASE, 2).apply {
            execSQL(
                "INSERT INTO saved_products (productId, savedAtEpochMillis) VALUES (42, 1234)",
            )
            close()
        }

        helper.runMigrationsAndValidate(TEST_DATABASE, 3, true, MIGRATION_2_3).use { database ->
            database.query(
                "SELECT productId, savedAtEpochMillis, name, productNumber, listPrice " +
                    "FROM saved_products WHERE productId = 42",
            ).use { cursor ->
                check(cursor.moveToFirst())
                assertEquals(42, cursor.getInt(0))
                assertEquals(1234L, cursor.getLong(1))
                assertEquals("", cursor.getString(2))
                assertEquals("", cursor.getString(3))
                assertEquals(0.0, cursor.getDouble(4), 0.0)
            }
        }
    }

    private companion object {
        const val TEST_DATABASE = "saved-product-migration-test"
    }
}

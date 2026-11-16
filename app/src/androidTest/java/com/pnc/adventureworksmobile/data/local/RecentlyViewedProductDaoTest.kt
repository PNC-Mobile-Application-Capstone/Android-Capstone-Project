package com.pnc.adventureworksmobile.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// Runs the real Room SQL against an in-memory database.
// The fakes in src/test cannot prove the upsert, the trim query, or the clear query.
@RunWith(AndroidJUnit4::class)
class RecentlyViewedProductDaoTest {

    private lateinit var database: AdventureWorksDatabase
    private lateinit var recentDao: RecentlyViewedProductDao
    private lateinit var savedDao: SavedProductDao

    @Before
    fun createDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AdventureWorksDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        recentDao = database.recentlyViewedProductDao()
        savedDao = database.savedProductDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun viewingTheSameProductTwiceKeepsOneRow() = runBlocking {
        recentDao.recordAndTrim(recentRow(productId = 749, viewedAt = 1_000L))
        recentDao.recordAndTrim(recentRow(productId = 749, viewedAt = 2_000L))

        val rows = recentDao.observeAll().first()
        assertEquals(listOf(749), rows.map { it.productId })
        assertEquals(2_000L, rows.single().viewedAtEpochMillis)
    }

    @Test
    fun revisitingAProductMovesItToTheTop() = runBlocking {
        recentDao.recordAndTrim(recentRow(productId = 749, viewedAt = 1_000L))
        recentDao.recordAndTrim(recentRow(productId = 750, viewedAt = 2_000L))
        recentDao.recordAndTrim(recentRow(productId = 751, viewedAt = 3_000L))

        recentDao.recordAndTrim(recentRow(productId = 749, viewedAt = 4_000L))

        val ids = recentDao.observeAll().first().map { it.productId }
        assertEquals(listOf(749, 751, 750), ids)
    }

    @Test
    fun onlyTheNewestTenAreKept() = runBlocking {
        (1..12).forEach { index ->
            recentDao.recordAndTrim(recentRow(productId = 700 + index, viewedAt = index * 1_000L))
        }

        val ids = recentDao.observeAll().first().map { it.productId }
        assertEquals(10, ids.size)
        // Newest first. The two oldest (701 and 702) are gone.
        assertEquals((712 downTo 703).toList(), ids)
    }

    @Test
    fun clearingHistoryKeepsSavedProducts() = runBlocking {
        savedDao.save(savedRow(productId = 749))
        savedDao.save(savedRow(productId = 680))
        recentDao.recordAndTrim(recentRow(productId = 749, viewedAt = 1_000L))
        recentDao.recordAndTrim(recentRow(productId = 750, viewedAt = 2_000L))

        recentDao.clearAll()

        assertTrue(recentDao.observeAll().first().isEmpty())
        assertEquals(setOf(749, 680), savedDao.getSavedProductIds().toSet())
    }

    private fun recentRow(productId: Int, viewedAt: Long) = RecentlyViewedProductEntity(
        productId = productId,
        viewedAtEpochMillis = viewedAt,
        name = "Product $productId",
        productNumber = "PN-$productId",
        listPrice = 10.0,
        photoBase64 = null,
        photoFileName = null,
    )

    private fun savedRow(productId: Int) = SavedProductEntity(
        productId = productId,
        savedAtEpochMillis = 1_000L,
        name = "Product $productId",
        productNumber = "PN-$productId",
        listPrice = 10.0,
        thumbnailBase64 = null,
        thumbnailPhotoFileName = null,
    )
}

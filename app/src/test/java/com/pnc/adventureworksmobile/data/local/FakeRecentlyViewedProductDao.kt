package com.pnc.adventureworksmobile.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeRecentlyViewedProductDao: RecentlyViewedProductDao {

    val recent = MutableStateFlow<List<RecentlyViewedProductEntity>>(emptyList())

    val calls = mutableListOf<String>()

    override fun observeAll(): Flow<List<RecentlyViewedProductEntity>> = recent

    override suspend fun record(product: RecentlyViewedProductEntity) {
        calls += "record ${product.productId}"
        recent.value = recent.value.filterNot { it.productId == product.productId } + product
    }

    override suspend fun trimToLatestTen() {
        calls += "trim"
    }

}
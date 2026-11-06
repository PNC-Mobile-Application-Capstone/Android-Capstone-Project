package com.pnc.adventureworksmobile.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSavedProductDao: SavedProductDao {

    val saved = MutableStateFlow<List<SavedProductEntity>>(emptyList())

    override fun observeAll(): Flow<List<SavedProductEntity>> = saved

    override suspend fun save(product: SavedProductEntity) {
        saved.value = saved.value.filterNot { it.productId == product.productId } + product
    }

    override suspend fun remove(productId: Int) {
        saved.value = saved.value.filterNot { it.productId == productId }
    }


}
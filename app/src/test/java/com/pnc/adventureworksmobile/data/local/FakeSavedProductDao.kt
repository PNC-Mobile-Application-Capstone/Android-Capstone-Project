package com.pnc.adventureworksmobile.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSavedProductDao: SavedProductDao {

    val saved = MutableStateFlow<List<SavedProductEntity>>(emptyList())

    override fun observeAll(): Flow<List<SavedProductEntity>> = saved

    override suspend fun getSavedProductIds(): List<Int> = saved.value.map { it.productId }

    override suspend fun save(product: SavedProductEntity) {
        saved.value = saved.value.filterNot { it.productId == product.productId } + product
    }

    override suspend fun updateSnapshot(
        productId: Int,
        name: String,
        productNumber: String,
        listPrice: Double,
        thumbnailBase64: String?,
        thumbnailPhotoFileName: String?,
    ) {
        saved.value = saved.value.map { row ->
            if (row.productId == productId) {
                row.copy(
                    name = name,
                    productNumber = productNumber,
                    listPrice = listPrice,
                    thumbnailBase64 = thumbnailBase64,
                    thumbnailPhotoFileName = thumbnailPhotoFileName,
                )
            } else {
                row
            }
        }
    }

    override suspend fun remove(productId: Int) {
        saved.value = saved.value.filterNot { it.productId == productId }
    }


}
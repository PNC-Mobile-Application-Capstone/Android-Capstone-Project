package com.pnc.adventureworksmobile.domain.model

enum class ProductAvailability {
    InStock,
    LowStock,
    Unavailable,
    Unknown,
    ;

    companion object {

        const val LOW_STOCK_THRESHOLD = 100

        fun fromStockLevel(stockLevel: Int?): ProductAvailability = when {
            stockLevel == null -> Unknown
            stockLevel < 0 -> Unknown
            stockLevel == 0 -> Unavailable
            stockLevel <= LOW_STOCK_THRESHOLD -> LowStock
            else -> InStock
        }
    }
}

val Product.availability: ProductAvailability
    get() = ProductAvailability.fromStockLevel(stockLevel)

val ProductDetails.availability: ProductAvailability
    get() = ProductAvailability.fromStockLevel(stockLevel)

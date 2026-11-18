package com.pnc.adventureworksmobile.domain.usecase

import com.pnc.adventureworksmobile.domain.model.OrderLine
import com.pnc.adventureworksmobile.domain.repository.OrderRepository
import java.math.BigDecimal
import javax.inject.Inject

data class RecentOrder(
    val orderNumber: Int,
    val orderDate: String,
    val itemCount: Int,
    val total: BigDecimal,
)

class GetRecentOrdersUseCase @Inject constructor(
    private val orderRepository: OrderRepository,
) {
    suspend operator fun invoke(customerId: Int, limit: Int = DEFAULT_LIMIT): Result<List<RecentOrder>> =
        orderRepository.getOrderLines(customerId).map { lines -> summarize(lines, limit) }

    companion object {
        const val DEFAULT_LIMIT = 3

        fun summarize(lines: List<OrderLine>, limit: Int = DEFAULT_LIMIT): List<RecentOrder> = lines
            .groupBy { it.orderNumber }
            .map { (orderNumber, orderLines) ->
                RecentOrder(
                    orderNumber = orderNumber,
                    orderDate = orderLines.first().orderDate,
                    itemCount = orderLines.sumOf { it.orderQty },
                    total = orderLines.fold(BigDecimal.ZERO) { sum, line ->
                        sum + BigDecimal.valueOf(line.lineTotal)
                    },
                )
            }
            .sortedWith(compareByDescending<RecentOrder> { it.orderDate }.thenByDescending { it.orderNumber })
            .take(limit)
    }
}
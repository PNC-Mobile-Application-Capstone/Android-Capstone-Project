package com.pnc.adventureworksmobile.presentation.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pnc.adventureworksmobile.core.session.DemoCustomerSession
import com.pnc.adventureworksmobile.domain.model.Order
import com.pnc.adventureworksmobile.domain.model.OrderLine
import com.pnc.adventureworksmobile.domain.repository.OrderRepository
import com.pnc.adventureworksmobile.presentation.shared.ErrorSubject
import com.pnc.adventureworksmobile.presentation.shared.toFriendlyMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OrdersUiState(
    val isLoading: Boolean = false,
    val orders: List<Order> = emptyList(),
    val errorMessage: String? = null,
)

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val session: DemoCustomerSession,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrdersUiState(isLoading = true))
    val uiState: StateFlow<OrdersUiState> = _uiState.asStateFlow()

    init {
        loadOrders()
    }

    fun loadOrders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            orderRepository.getOrderLines(session.activeCustomer.id)
                .onSuccess { orderLines ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            orders = getOrdersFromOrderLines(orderLines),
                            errorMessage = null,
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.toFriendlyMessage(ErrorSubject.Orders),
                        )
                    }
                }
        }
    }

    fun getOrdersFromOrderLines(orderLines: List<OrderLine>): List<Order> {
        val orders: MutableList<Order> = mutableListOf()
        orderLines.forEach { thisOrderLine ->
            // if we can find an Order in the new list, add to it; otherwise, create a new Order
            val currentOrder: Order? = orders.find {it.orderNumber == thisOrderLine.orderNumber}
            if (currentOrder != null) {
                currentOrder.orderLines += thisOrderLine
            }
            else {
                // create a list, then add this OrderLine as its first object
                val newOrderLineList = mutableListOf<OrderLine>()
                newOrderLineList.add(thisOrderLine)
                // construct a new Order based on this OrderLine
                val newOrder = Order(
                    orderNumber = thisOrderLine.orderNumber,
                    customerId = thisOrderLine.customerId,
                    firstName = thisOrderLine.firstName,
                    middleName = thisOrderLine.middleName,
                    lastName = thisOrderLine.lastName,
                    suffix = thisOrderLine.suffix,
                    orderDate = thisOrderLine.orderDate,
                    shipDate = thisOrderLine.shipDate,
                    orderLines = newOrderLineList
                )
                orders.add(newOrder)
            }
        }
        return orders
    }
}

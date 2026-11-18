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

enum class OrderSort(val label: String) {
    Newest("Newest first"),
    Oldest("Oldest first"),
}

data class OrdersUiState(
    val isLoading: Boolean = false,
    val orders: List<Order> = emptyList(),
    val selectedSort: OrderSort = OrderSort.Newest,
    val errorMessage: String? = null,
) {
    val userFirstName: String?
        get() = orders.firstNotNullOfOrNull { it.firstName?.takeIf { name -> name.isNotBlank() } }
}

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
                    val rawOrders = getOrdersFromOrderLines(orderLines)
                    val sortedOrders = sortOrders(rawOrders, _uiState.value.selectedSort)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            orders = sortedOrders,
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

    fun onSortSelected(sort: OrderSort) {
        _uiState.update { state ->
            state.copy(
                selectedSort = sort,
                orders = sortOrders(state.orders, sort),
            )
        }
    }

    private fun sortOrders(orders: List<Order>, sort: OrderSort): List<Order> {
        return when (sort) {
            OrderSort.Newest -> orders.sortedByDescending { it.orderDate }
            OrderSort.Oldest -> orders.sortedBy { it.orderDate }
        }
    }

    fun getOrdersFromOrderLines(orderLines: List<OrderLine>): List<Order> {
        val orders: MutableList<Order> = mutableListOf()
        orderLines.forEach { thisOrderLine ->
            // if we can find an Order in the new list, add to it; otherwise, create a new Order
            val currentOrder: Order? = orders.find { it.orderNumber == thisOrderLine.orderNumber }
            if (currentOrder != null) {
                currentOrder.orderLines += thisOrderLine
            } else {
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
                    statusName = thisOrderLine.statusName,
                    orderLines = newOrderLineList,
                )
                orders.add(newOrder)
            }
        }
        return orders
    }
}
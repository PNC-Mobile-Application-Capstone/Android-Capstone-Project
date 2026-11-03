package com.pnc.adventureworksmobile.presentation.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pnc.adventureworksmobile.core.session.DemoCustomerSession
import com.pnc.adventureworksmobile.domain.model.Order
import com.pnc.adventureworksmobile.domain.repository.OrderRepository
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
            orderRepository.getOrders(session.activeCustomer.id)
                .onSuccess { orders ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            orders = orders,
                            errorMessage = null,
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Failed to load orders",
                        )
                    }
                }
        }
    }
}

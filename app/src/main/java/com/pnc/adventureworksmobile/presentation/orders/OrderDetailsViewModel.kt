package com.pnc.adventureworksmobile.presentation.orders

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pnc.adventureworksmobile.domain.model.OrderDetails
import com.pnc.adventureworksmobile.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OrderDetailsUiState(
    val isLoading: Boolean = true,
    val orderDetails: OrderDetails? = null,
    val errorMessage: String? = null,
)

@HiltViewModel
class OrderDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val orderRepository: OrderRepository,
) : ViewModel() {

    private val orderId: Int = checkNotNull(savedStateHandle["orderId"])
    private val _uiState = MutableStateFlow(OrderDetailsUiState())
    val uiState: StateFlow<OrderDetailsUiState> = _uiState.asStateFlow()

    init {
        loadOrderDetails()
    }

    fun retry() = loadOrderDetails()

    private fun loadOrderDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            orderRepository.getOrder(orderId)
                .onSuccess { details ->
                    _uiState.update { it.copy(isLoading = false, orderDetails = details) }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Failed to load order details",
                        )
                    }
                }
        }
    }
}

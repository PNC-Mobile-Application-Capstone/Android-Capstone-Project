package com.pnc.adventureworksmobile.presentation.orders

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pnc.adventureworksmobile.core.session.DemoCustomerSession
import com.pnc.adventureworksmobile.domain.error.AppError
import com.pnc.adventureworksmobile.domain.error.AppException
import com.pnc.adventureworksmobile.domain.model.OrderDetails
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

data class OrderDetailsUiState(
    val isLoading: Boolean = true,
    val orderDetails: OrderDetails? = null,
    val errorMessage: String? = null,
)

@HiltViewModel
class OrderDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val orderRepository: OrderRepository,
    private val session: DemoCustomerSession,
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
            orderRepository.getOrderLines(session.activeCustomer.id)
                .onSuccess { lines ->
                    val matchingLines = lines.filter { it.orderNumber == orderId }
                    if (matchingLines.isEmpty()) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = AppException(AppError.NotFound).toFriendlyMessage(ErrorSubject.OrderDetails),
                            )
                        }
                        return@launch
                    }
                    orderRepository.getOrderLineDetails(matchingLines)
                        .onSuccess { lineDetailsList ->
                            if (lineDetailsList.isNotEmpty()) {
                                val first = lineDetailsList.first()
                                val orderDetails = OrderDetails(
                                    customerId = first.customerId,
                                    firstName = first.firstName,
                                    middleName = first.middleName,
                                    lastName = first.lastName,
                                    suffix = first.suffix,
                                    phoneNumber = first.phoneNumber,
                                    phoneNumberType = first.phoneNumberType,
                                    emailAddress = first.emailAddress,
                                    orderNumber = first.orderNumber,
                                    orderDate = first.orderDate,
                                    shipDate = first.shipDate,
                                    freight = first.freight,
                                    freightNumber = first.freightNumber,
                                    shipMethodName = first.shipMethodName,
                                    taxAmt = first.taxAmt,
                                    carrierTrackingNumber = first.carrierTrackingNumber,
                                    statusName = first.statusName,
                                    orderLineDetails = lineDetailsList,
                                )
                                _uiState.update { it.copy(isLoading = false, orderDetails = orderDetails) }
                            } else {
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        errorMessage = AppException(AppError.NotFound).toFriendlyMessage(ErrorSubject.OrderDetails),
                                    )
                                }
                            }
                        }
                        .onFailure { throwable ->
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = throwable.toFriendlyMessage(ErrorSubject.OrderDetails),
                                )
                            }
                        }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.toFriendlyMessage(ErrorSubject.OrderDetails),
                        )
                    }
                }
        }
    }
}

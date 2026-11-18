package com.pnc.adventureworksmobile.presentation.orders

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.pnc.adventureworksmobile.core.session.FakeDemoCustomerSession
import com.pnc.adventureworksmobile.domain.repository.FakeOrderRepository
import com.pnc.adventureworksmobile.testutil.MainDispatcherRule
import com.pnc.adventureworksmobile.testutil.builders.anOrderDetails
import com.pnc.adventureworksmobile.testutil.builders.anOrderLine
import com.pnc.adventureworksmobile.testutil.builders.anOrderLineDetails
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import com.pnc.adventureworksmobile.domain.error.AppError
import com.pnc.adventureworksmobile.domain.error.AppException

@OptIn(ExperimentalCoroutinesApi::class)
class OrderDetailsViewModelTest {

    private val detailsError = "We couldn't load this order. Check your internet connection and try again."

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeOrderRepository()
    private val session = FakeDemoCustomerSession()

    private fun createViewModel(orderId: Int = 43_659) = OrderDetailsViewModel(
        savedStateHandle = SavedStateHandle(mapOf("orderId" to orderId)),
        orderRepository = repository,
        session = session,
    )

    @Test
    fun `starts out loading`() = runTest {
        val viewModel = createViewModel()

        assertEquals(OrderDetailsUiState(), viewModel.uiState.value)
    }

    @Test
    fun `loads the order lines for session customer`() = runTest {
        val line = anOrderLine(orderNumber = 51_131)
        repository.orderLinesResult = Result.success(listOf(line))

        createViewModel(orderId = 51_131)

        advanceUntilIdle()

        assertEquals(session.activeCustomer.id, repository.lastCustomerId)
    }

    @Test
    fun `shows the order details once loaded`() = runTest {
        val line = anOrderLine(orderNumber = 43_659, id = 101, productName = "Road-150 Red, 62")
        val lineDetails = anOrderLineDetails(orderNumber = 43_659, id = 101, productName = "Road-150 Red, 62")
        val details = anOrderDetails(orderNumber = 43_659, orderLineDetails = listOf(lineDetails))

        repository.orderLinesResult = Result.success(listOf(line))
        repository.orderLineDetailsResult = Result.success(listOf(lineDetails))

        val viewModel = createViewModel(orderId = 43_659)
        advanceUntilIdle()

        assertEquals(
            OrderDetailsUiState(isLoading = false, orderDetails = details),
            viewModel.uiState.value,
        )
    }

    @Test
    fun `shows the error message when loading order lines fails`() = runTest {
        repository.orderLinesResult = Result.failure(AppException(AppError.NoConnection))

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(
            OrderDetailsUiState(isLoading = false, errorMessage = detailsError),
            viewModel.uiState.value,
        )
    }

    @Test
    fun `an unwrapped error shows the generic message, not raw text`() = runTest {
        repository.orderLinesResult = Result.failure(IOException("java.net.UnknownHostException"))

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(
            "We couldn't load this order. Something went wrong. Please try again.",
            viewModel.uiState.value.errorMessage,
        )
    }

    @Test
    fun `a missing order gets the not found message`() = runTest {
        repository.orderLinesResult = Result.success(emptyList())

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(
            "We couldn't load this order. It may no longer be available.",
            viewModel.uiState.value.errorMessage,
        )
    }

    @Test
    fun `retrying after a failure shows loading again and then the details`() = runTest {
        val line = anOrderLine(orderNumber = 43_659, id = 101)
        val lineDetails = anOrderLineDetails(orderNumber = 43_659, id = 101)
        val details = anOrderDetails(orderNumber = 43_659, orderLineDetails = listOf(lineDetails))

        repository.orderLinesResult = Result.failure(AppException(AppError.NoConnection))
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(OrderDetailsUiState(), awaitItem())
            assertEquals(OrderDetailsUiState(isLoading = false, errorMessage = detailsError), awaitItem())

            repository.orderLinesResult = Result.success(listOf(line))
            repository.orderLineDetailsResult = Result.success(listOf(lineDetails))
            viewModel.retry()

            assertEquals(OrderDetailsUiState(), awaitItem())
            assertEquals(OrderDetailsUiState(isLoading = false, orderDetails = details), awaitItem())
        }
    }

    @Test
    fun `fails fast without an order id`() {
        assertThrows(IllegalStateException::class.java) {
            OrderDetailsViewModel(savedStateHandle = SavedStateHandle(), orderRepository = repository, session = session)
        }
    }
}
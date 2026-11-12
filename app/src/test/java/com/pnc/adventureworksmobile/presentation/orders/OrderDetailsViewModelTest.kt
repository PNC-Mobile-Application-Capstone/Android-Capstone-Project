package com.pnc.adventureworksmobile.presentation.orders

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.pnc.adventureworksmobile.domain.repository.FakeOrderRepository
import com.pnc.adventureworksmobile.testutil.MainDispatcherRule
import com.pnc.adventureworksmobile.testutil.builders.anOrderDetails
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

    private fun createViewModel(orderId: Int = 43_659) = OrderDetailsViewModel(
        savedStateHandle = SavedStateHandle(mapOf("orderId" to orderId)),
        orderRepository = repository,
    )

    @Test
    fun `starts out loading`() = runTest {
        val viewModel = createViewModel()

        assertEquals(OrderDetailsUiState(), viewModel.uiState.value)
    }

    @Test
    fun `loads the order id it was navigated with`() = runTest {
        createViewModel(orderId = 51_131)

        advanceUntilIdle()

        assertEquals(51_131, repository.lastOrderId)
    }

    @Test
    fun `shows the order details once loaded`() = runTest {
        val details = anOrderDetails(id = 101, productName = "Road-150 Red, 62")
        repository.orderDetailsResult = Result.success(details)

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(
            OrderDetailsUiState(isLoading = false, orderDetails = details),
            viewModel.uiState.value,
        )
    }

    @Test
    fun `shows the error message when loading fails`() = runTest {
        repository.orderDetailsResult = Result.failure(AppException(AppError.NoConnection))

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(
            OrderDetailsUiState(isLoading = false, errorMessage = detailsError),
            viewModel.uiState.value,
        )
    }

    @Test
    fun `an unwrapped error shows the generic message, not raw text`() = runTest {
        repository.orderDetailsResult = Result.failure(IOException("java.net.UnknownHostException"))

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(
            "We couldn't load this order. Something went wrong. Please try again.",
            viewModel.uiState.value.errorMessage,
        )
    }

    @Test
    fun `a missing order gets the not found message`() = runTest {
        repository.orderDetailsResult = Result.failure(AppException(AppError.NotFound))

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(
            "We couldn't load this order. It may no longer be available.",
            viewModel.uiState.value.errorMessage,
        )
    }

    @Test
    fun `retrying after a failure shows loading again and then the details`() = runTest {
        val details = anOrderDetails(id = 101)
        repository.orderDetailsResult = Result.failure(AppException(AppError.NoConnection))
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(OrderDetailsUiState(), awaitItem())
            assertEquals(OrderDetailsUiState(isLoading = false, errorMessage = detailsError), awaitItem())

            repository.orderDetailsResult = Result.success(details)
            viewModel.retry()

            assertEquals(OrderDetailsUiState(), awaitItem())
            assertEquals(OrderDetailsUiState(isLoading = false, orderDetails = details), awaitItem())
        }
    }

    @Test
    fun `fails fast without an order id`() {
        assertThrows(IllegalStateException::class.java) {
            OrderDetailsViewModel(savedStateHandle = SavedStateHandle(), orderRepository = repository)
        }
    }
}
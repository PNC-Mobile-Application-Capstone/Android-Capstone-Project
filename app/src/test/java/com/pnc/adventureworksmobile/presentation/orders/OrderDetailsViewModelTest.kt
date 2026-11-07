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

@OptIn(ExperimentalCoroutinesApi::class)
class OrderDetailsViewModelTest {

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
        repository.orderDetailsResult = Result.failure(IOException("network down"))

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(
            OrderDetailsUiState(isLoading = false, errorMessage = "network down"),
            viewModel.uiState.value,
        )
    }

    @Test
    fun `falls back to a default message when the error has none`() = runTest {
        repository.orderDetailsResult = Result.failure(IOException())

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals("Failed to load order details", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `retrying after a failure shows loading again and then the details`() = runTest {
        val details = anOrderDetails(id = 101)
        repository.orderDetailsResult = Result.failure(IOException("network down"))
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(OrderDetailsUiState(), awaitItem())
            assertEquals(OrderDetailsUiState(isLoading = false, errorMessage = "network down"), awaitItem())

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
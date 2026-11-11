package com.pnc.adventureworksmobile.presentation.orders

import androidx.lifecycle.viewModelScope
import app.cash.turbine.test
import com.pnc.adventureworksmobile.core.session.FakeDemoCustomerSession
import com.pnc.adventureworksmobile.domain.model.Order
import com.pnc.adventureworksmobile.domain.repository.FakeOrderRepository
import com.pnc.adventureworksmobile.domain.repository.OrderRepository
import com.pnc.adventureworksmobile.testutil.MainDispatcherRule
import com.pnc.adventureworksmobile.testutil.builders.anOrder
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import com.pnc.adventureworksmobile.domain.error.AppError
import com.pnc.adventureworksmobile.domain.error.AppException

@OptIn(ExperimentalCoroutinesApi::class)
class OrdersViewModelTest {

    private val ordersError = "We couldn't load your orders. Check your internet connection and try again."

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeOrderRepository()
    private val session = FakeDemoCustomerSession()

    private fun createViewModel(orderRepository: OrderRepository = repository) =
        OrdersViewModel(orderRepository = orderRepository, session = session)


    @Test
    fun `starts out loading`() = runTest {
        val viewModel = createViewModel()

        assertEquals(OrdersUiState(isLoading = true), viewModel.uiState.value)
    }

    @Test
    fun `asks for the session customer's order`() = runTest {
        createViewModel()
        advanceUntilIdle()
        assertEquals(session.activeCustomer.id, repository.lastCustomerId)
    }

    @Test
    fun `shows the orders once loaded`() = runTest {
        val orders = listOf(anOrder(id = 1), anOrder(id = 2))
        repository.ordersResult = Result.success(orders)

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(OrdersUiState(isLoading = false, orders = orders), viewModel.uiState.value)
    }

    @Test
    fun `shows the error message when loading fails`() = runTest {
        repository.ordersResult = Result.failure(AppException(AppError.NoConnection))

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(
            OrdersUiState(isLoading = false, errorMessage = ordersError),
            viewModel.uiState.value
        )
    }

    @Test
    fun `an unwrapped error shows the generic message, not raw text`() = runTest {
        repository.ordersResult = Result.failure(IOException("java.net.UnknownHostException"))

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(
            "We couldn't load your orders. Something went wrong. Please try again.",
            viewModel.uiState.value.errorMessage,
        )
    }

    @Test
    fun `a server error gets its own message`() = runTest {
        repository.ordersResult = Result.failure(AppException(AppError.Server(500)))

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(
            "We couldn't load your orders. Our server had a problem. Please try again in a few minutes.",
            viewModel.uiState.value.errorMessage,
        )
    }

    @Test
    fun `retrying after a failure shows loading again, the nthe orders`() = runTest {
        val orders = listOf(anOrder(id = 1))
        repository.ordersResult = Result.failure(AppException(AppError.NoConnection))
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(OrdersUiState(isLoading = true), awaitItem())
            assertEquals(OrdersUiState(isLoading = false, errorMessage = ordersError), awaitItem())

            repository.ordersResult = Result.success(orders)
            viewModel.loadOrders()

            assertEquals(OrdersUiState(isLoading = true), awaitItem())
            assertEquals(OrdersUiState(isLoading = false, orders = orders), awaitItem())
        }
    }

    @Test
    fun `a failed reload keeps the orders already shown`() = runTest {
        val orders = listOf(anOrder(id = 1))
        repository.ordersResult = Result.success(orders)
        val viewModel = createViewModel()
        advanceUntilIdle()

        repository.ordersResult = Result.failure(AppException(AppError.NoConnection))
        viewModel.loadOrders()
        advanceUntilIdle()

        assertEquals(OrdersUiState(isLoading = false, orders = orders, errorMessage = ordersError),
        viewModel.uiState.value
        )
    }

    @Test
    fun `does not report an error if cancelled mid-load`() = runTest {
        val slowRepository = object : OrderRepository by FakeOrderRepository() {
            override suspend fun getOrders(customerId: Int): Result<List<Order>> {
                delay(5000)
                return Result.success(emptyList())
            }
        }
        val viewModel = createViewModel(orderRepository = slowRepository)

        advanceTimeBy(1000)
        viewModel.viewModelScope.cancel()
        advanceUntilIdle()

        assertEquals(OrdersUiState(isLoading = true), viewModel.uiState.value)
    }

}
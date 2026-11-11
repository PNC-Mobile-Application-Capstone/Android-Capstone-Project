package com.pnc.adventureworksmobile.presentation.recent

import app.cash.turbine.test
import com.pnc.adventureworksmobile.domain.model.Product
import com.pnc.adventureworksmobile.domain.repository.FakeProductRepository
import com.pnc.adventureworksmobile.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import com.pnc.adventureworksmobile.presentation.home.toUiModel
import com.pnc.adventureworksmobile.testutil.MainDispatcherRule
import com.pnc.adventureworksmobile.testutil.builders.aProduct
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecentlyViewedViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeProductRepository()

    private fun createViewModel() = RecentlyViewedViewModel(productRepository = repository)

    @Test
    fun `starts out loading`() = runTest {
        val viewModel = createViewModel()

        assertEquals(RecentlyViewedUiState(), viewModel.uiState.value)
    }

    @Test
    fun `shows recently viewed products as ui models`() = runTest {
        val product = aProduct(id = 749, name = "Road-150 Red, 62", listPrice = 3578.27)
        repository.recentlyViewed.value = listOf(product)

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(
            RecentlyViewedUiState(isLoading = false, products = listOf(product.toUiModel())),
            viewModel.uiState.value,
        )
    }

    @Test
    fun `keeps the repository's order`() = runTest {
        repository.recentlyViewed.value = listOf(aProduct(id = 750), aProduct(id = 749))

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(listOf(750, 749), viewModel.uiState.value.products.map { it.id })
    }


    @Test
    fun `an empty history stops loading and shows nothing`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(RecentlyViewedUiState(isLoading = false), viewModel.uiState.value)
    }

    @Test
    fun `updates when another product is viewed`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(RecentlyViewedUiState(), awaitItem())
            assertEquals(RecentlyViewedUiState(isLoading = false), awaitItem())

            repository.recentlyViewed.value = listOf(aProduct(id = 749))
            assertEquals(listOf(749), awaitItem().products.map { it.id })

            repository.recentlyViewed.value = listOf(aProduct(id = 750), aProduct(id = 749))
            assertEquals(listOf(750, 749), awaitItem().products.map { it.id })
        }
    }

    @Test
    fun `recently viewed products are never shown as saved`() = runTest {
        repository.savedProducts.value = listOf(aProduct(id = 749))
        repository.recentlyViewed.value = listOf(aProduct(id = 749))

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.products.single().isSaved)
    }

    @Test
    fun `a database error shows a message instead of crashing`() = runTest {
        val failingRepository = object : ProductRepository by repository {
            override fun observeRecentlyViewedProducts(): Flow<List<Product>> =
                flow { throw IllegalStateException("db closed") }
        }
        val viewModel = RecentlyViewedViewModel(productRepository = failingRepository)
        advanceUntilIdle()

        assertEquals(
            RecentlyViewedUiState(
                isLoading = false,
                errorMessage = "We couldn't load your recently viewed products. Something went wrong with the data saved on this phone. Please try again.",
            ),
            viewModel.uiState.value,
        )
    }

    @Test
    fun `retry clears the error once the history loads`() = runTest {
        var fail = true
        val flakyRepository = object : ProductRepository by repository {
            override fun observeRecentlyViewedProducts(): Flow<List<Product>> =
                if (fail) flow { throw IllegalStateException("db closed") }
                else repository.observeRecentlyViewedProducts()
        }
        repository.recentlyViewed.value = listOf(aProduct(id = 749))
        val viewModel = RecentlyViewedViewModel(productRepository = flakyRepository)
        advanceUntilIdle()

        fail = false
        viewModel.retry()
        advanceUntilIdle()

        assertEquals(null, viewModel.uiState.value.errorMessage)
        assertEquals(listOf(749), viewModel.uiState.value.products.map { it.id })
    }
}

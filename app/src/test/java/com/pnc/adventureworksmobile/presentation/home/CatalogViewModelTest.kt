package com.pnc.adventureworksmobile.presentation.home

import com.pnc.adventureworksmobile.core.session.FakeDemoCustomerSession
import com.pnc.adventureworksmobile.domain.model.Product
import com.pnc.adventureworksmobile.domain.repository.FakeProductRepository
import com.pnc.adventureworksmobile.domain.repository.ProductRepository
import com.pnc.adventureworksmobile.testutil.MainDispatcherRule
import com.pnc.adventureworksmobile.testutil.builders.aProduct
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeProductRepository()
    private val session = FakeDemoCustomerSession()

    private val catalogError = "We couldn't load the catalog. Check your connection and try again."

    private fun createViewModel(productRepository: ProductRepository = repository) =
        CatalogViewModel(session = session, productRepository = productRepository)

    @Test
    fun `starts with the session customer and loading`() = runTest {
        val viewModel = createViewModel()

        assertEquals(CatalogUiState(demoCustomer = session.activeCustomer), viewModel.uiState.value)
    }

    @Test
    fun `loaded products become ui models`() = runTest {
        val product = aProduct(id = 749, name = "Road-150 Red, 62", listPrice = 3578.27)
        repository.productsResult = Result.success(listOf(product))

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(listOf(product.toUiModel()), viewModel.uiState.value.products)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `a failed load shows the friendly catalog message`() = runTest {
        repository.productsResult = Result.failure(IOException("network down"))

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(
            CatalogUiState(demoCustomer = session.activeCustomer, isLoading = false, errorMessage = catalogError),
            viewModel.uiState.value,
        )
    }

    @Test
    fun `a failed refresh keeps the products already shown`() = runTest {
        val products = listOf(aProduct(id = 749))
        repository.productsResult = Result.success(products)
        val viewModel = createViewModel()
        advanceUntilIdle()

        repository.productsResult = Result.failure(IOException("network down"))
        viewModel.refresh()
        advanceUntilIdle()

        assertEquals(products.map { it.toUiModel() }, viewModel.uiState.value.products)
        assertEquals(catalogError, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `refreshing with products already shown does not show loading`() = runTest {
        val slowRepository = object : ProductRepository by repository {
            override suspend fun getProducts(): Result<List<Product>> {
                delay(1_000)
                return Result.success(listOf(aProduct(id = 749)))
            }
        }
        val viewModel = createViewModel(productRepository = slowRepository)
        advanceUntilIdle()                  // the first load finishes

        viewModel.refresh()
        runCurrent()                        // the refresh starts, then waits at its delay

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(1, viewModel.uiState.value.products.size)
    }

    @Test
    fun `products saved before the catalog loads are marked saved`() = runTest {
        repository.savedProducts.value = listOf(aProduct(id = 749))
        repository.productsResult = Result.success(listOf(aProduct(id = 749), aProduct(id = 750)))

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(
            listOf(749 to true, 750 to false),
            viewModel.uiState.value.products.map { it.id to it.isSaved },
        )
    }

    @Test
    fun `saving a product later marks it in the list`() = runTest {
        repository.productsResult = Result.success(listOf(aProduct(id = 749)))
        val viewModel = createViewModel()
        advanceUntilIdle()

        repository.savedProducts.value = listOf(aProduct(id = 749))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.products.single().isSaved)
    }

    @Test
    fun `search, category and sort changes update the state`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onSearchQueryChange("helmet")
        viewModel.onCategorySelected("Accessories")
        viewModel.onSortSelected(ProductSort.PriceDescending)

        val state = viewModel.uiState.value
        assertEquals("helmet", state.searchQuery)
        assertEquals("Accessories", state.selectedCategory)
        assertEquals(ProductSort.PriceDescending, state.selectedSort)
    }

    @Test
    fun `the search query filters the visible products`() = runTest {
        repository.productsResult = Result.success(
            listOf(
                aProduct(id = 1, name = "Sport-100 Helmet, Red"),
                aProduct(id = 2, name = "Long-Sleeve Logo Jersey, M"),
            ),
        )
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onSearchQueryChange("helmet")

        assertEquals(listOf(1), viewModel.uiState.value.visibleProducts.map { it.id })
    }

    @Test
    fun `toggling a product saves the full catalog product and marks it in the list`() = runTest {
        val product = aProduct(id = 749, name = "Road-150 Red, 62")
        repository.productsResult = Result.success(listOf(product))
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onSavedToggle(749)
        advanceUntilIdle()

        assertEquals(listOf(product), repository.savedCalls)
        assertTrue(viewModel.uiState.value.products.single().isSaved)
    }

    @Test
    fun `toggling a saved product removes it`() = runTest {
        repository.productsResult = Result.success(listOf(aProduct(id = 749)))
        repository.savedProducts.value = listOf(aProduct(id = 749))
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onSavedToggle(749)
        advanceUntilIdle()

        assertEquals(listOf(749), repository.removedIds)
        assertFalse(viewModel.uiState.value.products.single().isSaved)
    }

    @Test
    fun `toggling a product that is not in the list does nothing`() = runTest {
        repository.productsResult = Result.success(listOf(aProduct(id = 749)))
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onSavedToggle(999)
        advanceUntilIdle()

        assertTrue(repository.savedCalls.isEmpty())
        assertTrue(repository.removedIds.isEmpty())
    }
}
package com.pnc.adventureworksmobile.presentation.saved

import com.pnc.adventureworksmobile.domain.model.Product
import com.pnc.adventureworksmobile.domain.model.ProductDetails
import com.pnc.adventureworksmobile.domain.repository.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SavedViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun savedProductsKeepMostRecentFirstOrder() = runTest(dispatcher) {
        val repository = FakeProductRepository(savedProducts = listOf(product(2), product(1)))
        val viewModel = SavedViewModel(repository)

        advanceUntilIdle()

        assertEquals(listOf(2, 1), viewModel.uiState.value.products.map { it.id })
        assertEquals(false, viewModel.uiState.value.isLoading)
    }

    @Test
    fun removingProductUpdatesSavedList() = runTest(dispatcher) {
        val repository = FakeProductRepository(savedProducts = listOf(product(2), product(1)))
        val viewModel = SavedViewModel(repository)
        advanceUntilIdle()

        viewModel.removeSavedProduct(1)
        advanceUntilIdle()

        assertEquals(listOf(2), viewModel.uiState.value.products.map { it.id })
    }

    @Test
    fun savedProductsFailureShowsRetryableError() = runTest(dispatcher) {
        val repository = FakeProductRepository(
            savedProducts = emptyList(),
            failSavedObservation = true,
        )
        val viewModel = SavedViewModel(repository)

        advanceUntilIdle()

        assertEquals(false, viewModel.uiState.value.isLoading)
        assertEquals(
            "We couldn't load your saved products. Something went wrong with the data saved on this phone. Please try again.",
            viewModel.uiState.value.errorMessage,
        )
    }
}

private class FakeProductRepository(
    savedProducts: List<Product>,
    private val failSavedObservation: Boolean = false,
) : ProductRepository {
    private val savedProductsFlow = MutableStateFlow(savedProducts)

    override suspend fun getProducts(): Result<List<Product>> = Result.success(emptyList())

    override suspend fun getProduct(productId: Int): Result<ProductDetails> =
        error("Not needed by these tests")

    override fun observeSavedProducts(): Flow<List<Product>> = if (failSavedObservation) {
        flow { error("Database unavailable") }
    } else {
        savedProductsFlow
    }

    override suspend fun saveProduct(product: Product) {
        savedProductsFlow.value = listOf(product) + savedProductsFlow.value.filterNot { it.id == product.id }
    }

    override suspend fun removeSavedProduct(productId: Int) {
        savedProductsFlow.value = savedProductsFlow.value.filterNot { it.id == productId }
    }

    override suspend fun recordRecentlyViewed(product: ProductDetails) = Unit

    override fun observeRecentlyViewedProducts(): Flow<List<Product>> = flowOf(emptyList())
}

private fun product(id: Int) = Product(
    id = id,
    name = "Product $id",
    productNumber = "P-$id",
    thumbnailBase64 = null,
    thumbnailPhotoFileName = null,
    listPrice = id * 10.0,
)

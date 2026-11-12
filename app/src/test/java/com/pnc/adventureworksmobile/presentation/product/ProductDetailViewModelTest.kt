package com.pnc.adventureworksmobile.presentation.product

import androidx.lifecycle.SavedStateHandle
import com.pnc.adventureworksmobile.domain.repository.FakeProductRepository
import com.pnc.adventureworksmobile.testutil.MainDispatcherRule
import com.pnc.adventureworksmobile.testutil.builders.aProductDetails
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import com.pnc.adventureworksmobile.domain.error.AppError
import com.pnc.adventureworksmobile.domain.error.AppException
import com.pnc.adventureworksmobile.domain.model.Product
import com.pnc.adventureworksmobile.domain.model.ProductDetails
import com.pnc.adventureworksmobile.domain.repository.ProductRepository
import com.pnc.adventureworksmobile.domain.model.toProductSummary
import com.pnc.adventureworksmobile.testutil.builders.aProduct

@OptIn(ExperimentalCoroutinesApi::class)
class ProductDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeProductRepository()

    private fun createViewModel(productId: Int = 749) = ProductDetailViewModel(
        savedStateHandle = SavedStateHandle(mapOf("productId" to productId)),
        productRepository = repository,
    )

    @Test
    fun `starts out loading`() = runTest {
        val viewModel = createViewModel()

        assertEquals(ProductDetailUiState(), viewModel.uiState.value)
    }

    @Test
    fun `loads the product id it was navigated with`() = runTest {
        createViewModel(productId = 707)

        advanceUntilIdle()

        assertEquals(707, repository.lastProductId)
    }

    @Test
    fun `shows the product once loaded`() = runTest {
        val details = aProductDetails(id = 749, name = "Road-150 Red, 62")
        repository.productDetailsResult = Result.success(details)

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(
            ProductDetailUiState(isLoading = false, product = details),
            viewModel.uiState.value,
        )
    }

    @Test
    fun `records the product as recently viewed after it loads`() = runTest {
        val details = aProductDetails(id = 749)
        repository.productDetailsResult = Result.success(details)

        createViewModel()
        advanceUntilIdle()

        assertEquals(listOf(details), repository.recordedViews)
    }

    @Test
    fun `a failed load shows the friendly message`() = runTest {
        repository.productDetailsResult = Result.failure(AppException(AppError.NoConnection))

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(
            ProductDetailUiState(
                isLoading = false,
                errorMessage = "We couldn't load this product. Check your internet connection and try again.",
            ),
            viewModel.uiState.value,
        )
    }

    @Test
    fun `does not record anything when loading fails`() = runTest {
        repository.productDetailsResult = Result.failure(IOException("network down"))

        createViewModel()
        advanceUntilIdle()

        assertTrue(repository.recordedViews.isEmpty())
    }

    @Test
    fun `retrying after a failure shows the product`() = runTest {
        val details = aProductDetails(id = 749)
        repository.productDetailsResult = Result.failure(IOException("network down"))
        val viewModel = createViewModel()
        advanceUntilIdle()

        repository.productDetailsResult = Result.success(details)
        viewModel.retry()
        advanceUntilIdle()

        assertEquals(
            ProductDetailUiState(isLoading = false, product = details),
            viewModel.uiState.value,
        )
    }

    @Test
    fun `shows saved when the product is already saved`() = runTest {
        repository.savedProducts.value = listOf(aProduct(id = 749))

        val viewModel = createViewModel(productId = 749)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSaved)
    }

    @Test
    fun `saving a different product does not mark this one`() = runTest {
        repository.savedProducts.value = listOf(aProduct(id = 750))

        val viewModel = createViewModel(productId = 749)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSaved)
    }

    @Test
    fun `isSaved follows changes to the saved products`() = runTest {
        val viewModel = createViewModel(productId = 749)
        advanceUntilIdle()

        repository.savedProducts.value = listOf(aProduct(id = 749))
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isSaved)

        repository.savedProducts.value = emptyList()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isSaved)
    }

    @Test
    fun `toggling an unsaved product saves it`() = runTest {
        val details = aProductDetails(id = 749, name = "Road-150 Red, 62")
        repository.productDetailsResult = Result.success(details)
        val viewModel = createViewModel(productId = 749)
        advanceUntilIdle()

        viewModel.onSavedToggle()
        advanceUntilIdle()

        assertEquals(listOf(details.toProductSummary()), repository.savedCalls)
        assertTrue(viewModel.uiState.value.isSaved)
    }

    @Test
    fun `toggling a saved product removes it`() = runTest {
        repository.productDetailsResult = Result.success(aProductDetails(id = 749))
        repository.savedProducts.value = listOf(aProduct(id = 749))
        val viewModel = createViewModel(productId = 749)
        advanceUntilIdle()

        viewModel.onSavedToggle()
        advanceUntilIdle()

        assertEquals(listOf(749), repository.removedIds)
        assertFalse(viewModel.uiState.value.isSaved)
    }

    @Test
    fun `toggling before the product has loaded does nothing`() = runTest {
        repository.productDetailsResult = Result.failure(IOException("network down"))
        val viewModel = createViewModel(productId = 749)
        advanceUntilIdle()

        viewModel.onSavedToggle()
        advanceUntilIdle()

        assertTrue(repository.savedCalls.isEmpty())
        assertTrue(repository.removedIds.isEmpty())
    }

    @Test
    fun `fails fast without a product id`() {
        assertThrows(IllegalStateException::class.java) {
            ProductDetailViewModel(savedStateHandle = SavedStateHandle(), productRepository = repository)
        }
    }

    @Test
    fun `a removed product gets the not found message`() = runTest {
        repository.productDetailsResult = Result.failure(AppException(AppError.NotFound))

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(
            "We couldn't load this product. It may no longer be available.",
            viewModel.uiState.value.errorMessage,
        )
    }

    @Test
    fun `a failed history write still shows the product`() = runTest {
        val details = aProductDetails(id = 749)
        repository.productDetailsResult = Result.success(details)
        val failingRepository = object : ProductRepository by repository {
            override suspend fun recordRecentlyViewed(product: ProductDetails) {
                throw IllegalStateException("disk full")
            }
        }
        val viewModel = ProductDetailViewModel(
            savedStateHandle = SavedStateHandle(mapOf("productId" to 749)),
            productRepository = failingRepository,
        )
        advanceUntilIdle()

        assertEquals(ProductDetailUiState(isLoading = false, product = details), viewModel.uiState.value)
    }

    @Test
    fun `a failed save shows the saved products message instead of crashing`() = runTest {
        repository.productDetailsResult = Result.success(aProductDetails(id = 749))
        val failingRepository = object : ProductRepository by repository {
            override suspend fun saveProduct(product: Product) {
                throw IllegalStateException("disk full")
            }
        }
        val viewModel = ProductDetailViewModel(
            savedStateHandle = SavedStateHandle(mapOf("productId" to 749)),
            productRepository = failingRepository,
        )
        advanceUntilIdle()

        viewModel.onSavedToggle()
        advanceUntilIdle()

        assertEquals(
            "We couldn't update your saved products. Something went wrong with the data saved on this phone. Please try again.",
            viewModel.uiState.value.errorMessage,
        )
        assertFalse(viewModel.uiState.value.isSaved)
    }
}

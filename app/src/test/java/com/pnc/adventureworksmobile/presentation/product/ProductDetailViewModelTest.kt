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
        repository.productDetailsResult = Result.failure(IOException("network down"))

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(
            ProductDetailUiState(
                isLoading = false,
                errorMessage = "We couldn't load this product. Please try again.",
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
        repository.savedIds.value = setOf(749)

        val viewModel = createViewModel(productId = 749)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSaved)
    }

    @Test
    fun `saving a different product does not mark this one`() = runTest {
        repository.savedIds.value = setOf(750)

        val viewModel = createViewModel(productId = 749)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSaved)
    }

    @Test
    fun `isSaved follows changes to the saved ids`() = runTest {
        val viewModel = createViewModel(productId = 749)
        advanceUntilIdle()

        repository.savedIds.value = setOf(749)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isSaved)

        repository.savedIds.value = emptySet()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isSaved)
    }

    @Test
    fun `toggling an unsaved product saves it`() = runTest {
        val viewModel = createViewModel(productId = 749)
        advanceUntilIdle()

        viewModel.onSavedToggle()
        advanceUntilIdle()

        assertEquals(listOf(749 to true), repository.setSavedCalls)
        assertTrue(viewModel.uiState.value.isSaved)
    }

    @Test
    fun `toggling a saved product unsaves it`() = runTest {
        repository.savedIds.value = setOf(749)
        val viewModel = createViewModel(productId = 749)
        advanceUntilIdle()

        viewModel.onSavedToggle()
        advanceUntilIdle()

        assertEquals(listOf(749 to false), repository.setSavedCalls)
        assertFalse(viewModel.uiState.value.isSaved)
    }

    @Test
    fun `fails fast without a product id`() {
        assertThrows(IllegalStateException::class.java) {
            ProductDetailViewModel(savedStateHandle = SavedStateHandle(), productRepository = repository)
        }
    }

}
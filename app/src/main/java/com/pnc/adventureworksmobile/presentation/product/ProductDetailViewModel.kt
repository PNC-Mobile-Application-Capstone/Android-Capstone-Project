package com.pnc.adventureworksmobile.presentation.product

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pnc.adventureworksmobile.domain.error.AppError
import com.pnc.adventureworksmobile.domain.model.ProductDetails
import com.pnc.adventureworksmobile.domain.model.toProductSummary
import com.pnc.adventureworksmobile.domain.repository.ProductRepository
import com.pnc.adventureworksmobile.presentation.shared.ErrorSubject
import com.pnc.adventureworksmobile.presentation.shared.toFriendlyMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProductDetailUiState(
    val isLoading: Boolean = true,
    val product: ProductDetails? = null,
    val isSaved: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val productRepository: ProductRepository,
) : ViewModel() {
    private val productId: Int = checkNotNull(savedStateHandle["productId"])
    private val _uiState = MutableStateFlow(ProductDetailUiState())

    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    init {
        observeSavedState()
        loadProduct()
    }

    fun retry() = loadProduct()

    fun onSavedToggle() {
        val product = _uiState.value.product ?: return
        viewModelScope.launch {
            try {
                if (_uiState.value.isSaved) {
                    productRepository.removeSavedProduct(productId)
                } else {
                    productRepository.saveProduct(product.toProductSummary())
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = AppError.LocalStorage.toFriendlyMessage(ErrorSubject.UpdateSaved))
                }
            }
        }
    }

    private fun loadProduct() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            productRepository.getProduct(productId)
                .onSuccess { product ->
                    recordRecentlyViewed(product)
                    _uiState.update { it.copy(isLoading = false, product = product) }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.toFriendlyMessage(ErrorSubject.ProductDetails),
                        )
                    }
                }
        }
    }

    // History is a nice extra. If Room fails, still show the product.
    private suspend fun recordRecentlyViewed(product: ProductDetails) {
        try {
            productRepository.recordRecentlyViewed(product)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Ignored on purpose. The product loaded fine.
        }
    }

    private fun observeSavedState() {
        viewModelScope.launch {
            // If Room fails here, the heart just shows as unsaved.
            productRepository.observeSavedProducts().catch { }.collectLatest { products ->
                val ids = products.mapTo(mutableSetOf()) { it.id }
                _uiState.update { it.copy(isSaved = productId in ids) }
            }
        }
    }
}

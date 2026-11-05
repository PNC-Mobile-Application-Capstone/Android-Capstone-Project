package com.pnc.adventureworksmobile.presentation.product

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pnc.adventureworksmobile.domain.model.ProductDetails
import com.pnc.adventureworksmobile.domain.model.toProductSummary
import com.pnc.adventureworksmobile.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
            if (_uiState.value.isSaved) {
                productRepository.removeSavedProduct(productId)
            } else {
                productRepository.saveProduct(product.toProductSummary())
            }
        }
    }

    private fun loadProduct() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            productRepository.getProduct(productId)
                .onSuccess { product ->
                    productRepository.recordRecentlyViewed(product)
                    _uiState.update { it.copy(isLoading = false, product = product) }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "We couldn't load this product. Please try again.",
                        )
                    }
                }
        }
    }

    private fun observeSavedState() {
        viewModelScope.launch {
            productRepository.observeSavedProducts().collectLatest { products ->
                val ids = products.mapTo(mutableSetOf()) { it.id }
                _uiState.update { it.copy(isSaved = productId in ids) }
            }
        }
    }
}

package com.pnc.adventureworksmobile.presentation.saved

import androidx.lifecycle.ViewModel
import com.pnc.adventureworksmobile.domain.model.Product
import com.pnc.adventureworksmobile.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SavedUiState(
    val savedProductIds: Set<Int> = emptySet(),
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

@HiltViewModel
class SavedViewModel @Inject constructor(
    private val productRepository: ProductRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SavedUiState())
    val uiState: StateFlow<SavedUiState> = _uiState.asStateFlow()

    init {
        observeSavedProducts()
    }

    fun retry() {
        loadSavedProducts(_uiState.value.savedProductIds)
    }

    fun removeSavedProduct(productId: Int) {
        viewModelScope.launch {
            productRepository.setSaved(
                productId = productId,
                saved = false,
            )
        }
    }

    private fun observeSavedProducts() {
        viewModelScope.launch {
            productRepository.observeSavedProductIds().collectLatest { savedIds ->
                _uiState.update { currentState ->
                    currentState.copy(savedProductIds = savedIds)
                }
                loadSavedProducts(savedIds)

            }
        }
    }

    private fun loadSavedProducts(savedIds: Set<Int>) {
        if (savedIds.isEmpty()) {
            _uiState.update { currentState ->
                currentState.copy(products = emptyList(), isLoading = false, errorMessage = null)
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(isLoading = true, errorMessage = null)
            }

            productRepository.getProducts()
                .onSuccess { products ->
                    val savedProducts = products.filter { product ->
                        product.id in savedIds
                    }

                    _uiState.update { currentState ->
                        currentState.copy(
                            products = savedProducts,
                            isLoading = false,
                            errorMessage = null,
                        )
                    }
                }
                .onFailure {
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            errorMessage = "We couldn't load your saved products. Please try again.",
                        )
                    }
                }
        }
    }
}

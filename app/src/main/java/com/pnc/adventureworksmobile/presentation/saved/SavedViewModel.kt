package com.pnc.adventureworksmobile.presentation.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pnc.adventureworksmobile.domain.model.Product
import com.pnc.adventureworksmobile.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SavedUiState(
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
    private var savedProductsJob: Job? = null

    init {
        observeSavedProducts()
    }

    fun retry() {
        observeSavedProducts()
    }

    fun removeSavedProduct(productId: Int) {
        viewModelScope.launch {
            productRepository.removeSavedProduct(productId)
        }
    }

    private fun observeSavedProducts() {
        savedProductsJob?.cancel()
        savedProductsJob = viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = currentState.products.isEmpty(),
                    errorMessage = null,
                )
            }
            productRepository.observeSavedProducts()
                .catch {
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            errorMessage = "We couldn't load your saved products. Please try again.",
                        )
                    }
                }
                .collectLatest { products ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            products = products,
                            isLoading = false,
                            errorMessage = null,
                        )
                    }
                }
        }
    }
}

package com.pnc.adventureworksmobile.presentation.recent

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pnc.adventureworksmobile.domain.model.ProductDetails
import com.pnc.adventureworksmobile.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecentlyViewedUiState(
    val isLoading: Boolean = true,
    val product: ProductDetails? = null,
    val isSaved: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class RecentlyViewedViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val productRepository: ProductRepository,
) : ViewModel() {
    private val productId: Int = checkNotNull(savedStateHandle["productId"])
    private val _uiState = MutableStateFlow(RecentlyViewedUiState())

    val uiState: StateFlow<RecentlyViewedUiState> = _uiState.asStateFlow()

    init {
        observeSavedState()
        loadProduct()
    }

    fun retry() = loadProduct()

    fun onSavedToggle() {
        viewModelScope.launch {
            productRepository.setSaved(productId, saved = !_uiState.value.isSaved)
        }
    }

    private fun loadProduct() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            productRepository.getProduct(productId)
                .onSuccess { product ->
                    productRepository.recordRecentlyViewed(productId)
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
            productRepository.observeSavedProductIds().collectLatest { ids ->
                _uiState.update { it.copy(isSaved = productId in ids) }
            }
        }
    }
}

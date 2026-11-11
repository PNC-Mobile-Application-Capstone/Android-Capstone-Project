package com.pnc.adventureworksmobile.presentation.recent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pnc.adventureworksmobile.domain.error.AppError
import com.pnc.adventureworksmobile.domain.repository.ProductRepository
import com.pnc.adventureworksmobile.presentation.home.ProductUiModel
import com.pnc.adventureworksmobile.presentation.home.toUiModel
import com.pnc.adventureworksmobile.presentation.shared.ErrorSubject
import com.pnc.adventureworksmobile.presentation.shared.toFriendlyMessage
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

data class RecentlyViewedUiState(
    val isLoading: Boolean = true,
    val products: List<ProductUiModel> = emptyList(),
    val errorMessage: String? = null,
)

@HiltViewModel
class RecentlyViewedViewModel @Inject constructor(
    private val productRepository: ProductRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RecentlyViewedUiState())

    val uiState: StateFlow<RecentlyViewedUiState> = _uiState.asStateFlow()
    private var recentProductsJob: Job? = null

    init {
        observeRecentlyViewed()
    }

    fun retry() = observeRecentlyViewed()

    private fun observeRecentlyViewed() {
        recentProductsJob?.cancel()
        recentProductsJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = it.products.isEmpty(), errorMessage = null) }
            productRepository.observeRecentlyViewedProducts()
                .catch {
                    // This Flow only reads Room, so any error here is a local storage error.
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = AppError.LocalStorage.toFriendlyMessage(ErrorSubject.RecentlyViewed),
                        )
                    }
                }
                .collectLatest { products ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            products = products.map { product -> product.toUiModel() },
                            errorMessage = null,
                        )
                    }
                }
        }
    }
}

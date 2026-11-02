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
    val productList: List<ProductDetails> = emptyList(),
    val errorMessage: String? = null,
)

@HiltViewModel
class RecentlyViewedViewModel @Inject constructor(
    private val productRepository: ProductRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RecentlyViewedUiState())

    val uiState: StateFlow<RecentlyViewedUiState> = _uiState.asStateFlow()

    fun onButtonClick() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun addToRecentlyViewed() {
        viewModelScope.launch {

        }
    }
}


package com.pnc.adventureworksmobile.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pnc.adventureworksmobile.core.session.DemoCustomer
import com.pnc.adventureworksmobile.core.session.DemoCustomerSession
import com.pnc.adventureworksmobile.domain.model.Product
import com.pnc.adventureworksmobile.domain.repository.ProductRepository
import com.pnc.adventureworksmobile.presentation.shared.productCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val demoCustomer: DemoCustomer,
    val searchQuery: String = "",
    val selectedCategory: String = "All",
    val selectedSort: ProductSort = ProductSort.NameAscending,
    val categories: List<String> = listOf("All", "Bikes", "Components", "Clothing", "Accessories"),
    val products: List<ProductUiModel> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
) {
    val visibleProducts: List<ProductUiModel>
        get() {
            val filteredProducts = products.filter { product ->
                val matchesCategory = selectedCategory == "All" || product.category == selectedCategory
                val matchesSearch = searchQuery.isBlank() ||
                    product.name.contains(searchQuery, ignoreCase = true) ||
                    product.category.contains(searchQuery, ignoreCase = true) ||
                    product.productNumber.contains(searchQuery, ignoreCase = true)
                matchesCategory && matchesSearch
            }

            return when (selectedSort) {
                ProductSort.NameAscending -> filteredProducts.sortedBy { it.name.lowercase(Locale.US) }
                ProductSort.PriceAscending -> filteredProducts.sortedBy { it.listPrice }
                ProductSort.PriceDescending -> filteredProducts.sortedByDescending { it.listPrice }
            }
        }
}

enum class ProductSort(val label: String) {
    NameAscending("Name A–Z"),
    PriceAscending("Price: Low to high"),
    PriceDescending("Price: High to low"),
}

data class ProductUiModel(
    val id: Int,
    val name: String,
    val productNumber: String,
    val category: String,
    val price: String,
    val listPrice: Double,
    val thumbnailBase64: String?,
    val thumbnailPhotoFileName: String?,
    val isSaved: Boolean = false,
)

internal fun Product.toUiModel(isSaved: Boolean = false): ProductUiModel {
    val category = productCategory(name)
    return ProductUiModel(
        id = id,
        name = name,
        productNumber = productNumber,
        category = category,
        price = NumberFormat.getCurrencyInstance(Locale.US).format(listPrice),
        listPrice = listPrice,
        thumbnailBase64 = thumbnailBase64,
        thumbnailPhotoFileName = thumbnailPhotoFileName,
        isSaved = isSaved,
    )
}





@HiltViewModel
class HomeViewModel @Inject constructor(
    session: DemoCustomerSession,
    private val productRepository: ProductRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState(demoCustomer = session.activeCustomer))
    private var savedProductIds = emptySet<Int>()

    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeSavedProducts()
        refresh()
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onCategorySelected(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun onSortSelected(sort: ProductSort) {
        _uiState.update { it.copy(selectedSort = sort) }
    }

    fun onSavedToggle(productId: Int) {
        val product = _uiState.value.products.firstOrNull { it.id == productId } ?: return
        viewModelScope.launch {
            productRepository.setSaved(productId, saved = !product.isSaved)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = it.products.isEmpty(), errorMessage = null) }
            productRepository.getProducts()
                .onSuccess { products ->
                    _uiState.update { state ->
                        state.copy(
                            products = products.map { it.toUiModel(it.id in savedProductIds) },
                            isLoading = false,
                            errorMessage = null,
                        )
                    }
                }
                .onFailure {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            errorMessage = "We couldn't load the catalog. Check your connection and try again.",
                        )
                    }
                }
        }
    }

    private fun observeSavedProducts() {
        viewModelScope.launch {
            productRepository.observeSavedProductIds().collectLatest { ids ->
                savedProductIds = ids
                _uiState.update { state ->
                    state.copy(products = state.products.map { it.copy(isSaved = it.id in ids) })
                }
            }
        }
    }
}

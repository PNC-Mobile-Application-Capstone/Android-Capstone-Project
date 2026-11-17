package com.pnc.adventureworksmobile.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pnc.adventureworksmobile.core.session.DemoCustomer
import com.pnc.adventureworksmobile.core.session.DemoCustomerSession
import com.pnc.adventureworksmobile.domain.error.AppError
import com.pnc.adventureworksmobile.domain.model.ALL_CATEGORIES
import com.pnc.adventureworksmobile.domain.model.PRODUCT_CATEGORIES
import com.pnc.adventureworksmobile.domain.model.Product
import com.pnc.adventureworksmobile.domain.model.ProductAvailability
import com.pnc.adventureworksmobile.domain.model.ProductSort
import com.pnc.adventureworksmobile.domain.model.availability
import com.pnc.adventureworksmobile.domain.model.productCategory
import com.pnc.adventureworksmobile.domain.repository.ProductRepository
import com.pnc.adventureworksmobile.domain.usecase.FilterAndSortProductsUseCase
import com.pnc.adventureworksmobile.domain.usecase.GetRecentOrdersUseCase
import com.pnc.adventureworksmobile.domain.usecase.RecentOrder
import com.pnc.adventureworksmobile.presentation.shared.ErrorSubject
import com.pnc.adventureworksmobile.presentation.shared.formatPrice
import com.pnc.adventureworksmobile.presentation.shared.toFriendlyMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CatalogUiState(
    val demoCustomer: DemoCustomer,
    val searchQuery: String = "",
    val selectedCategory: String = ALL_CATEGORIES,
    val selectedSort: ProductSort = ProductSort.NameAscending,
    val categories: List<String> = PRODUCT_CATEGORIES,

    val products: List<ProductUiModel> = emptyList(),

    val visibleProducts: List<ProductUiModel> = emptyList(),

    val isLoading: Boolean = true,

    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val recentlyViewed: RecentlyViewedSection = RecentlyViewedSection(),
    val recentOrders: RecentOrdersSection = RecentOrdersSection(),
) {

    val showsHomeSections: Boolean
        get() = searchQuery.isBlank() && selectedCategory == ALL_CATEGORIES
}

data class RecentlyViewedSection(
    val isLoading: Boolean = true,
    val products: List<ProductUiModel> = emptyList(),
    val errorMessage: String? = null,
)

data class RecentOrdersSection(
    val isLoading: Boolean = true,
    val orders: List<RecentOrderUiModel> = emptyList(),
    val errorMessage: String? = null,
)

data class RecentOrderUiModel(
    val orderNumber: Int,
    val title: String,
    val date: String,
    val itemSummary: String,
    val total: String,
)

val ProductSort.label: String
    get() = when (this) {
        ProductSort.NameAscending -> "Name A–Z"
        ProductSort.PriceAscending -> "Price: Low to high"
        ProductSort.PriceDescending -> "Price: High to low"
    }

data class ProductUiModel(
    val id: Int,
    val name: String,
    val productNumber: String,
    val category: String,
    val price: String,
    val thumbnailBase64: String?,
    val thumbnailPhotoFileName: String?,
    val availability: ProductAvailability = ProductAvailability.Unknown,
    val isSaved: Boolean = false,
)

internal fun Product.toUiModel(isSaved: Boolean = false) = ProductUiModel(
    id = id,
    name = name,
    productNumber = productNumber,
    category = productCategory(name),
    price = formatPrice(listPrice),
    thumbnailBase64 = thumbnailBase64,
    thumbnailPhotoFileName = thumbnailPhotoFileName,
    availability = availability,
    isSaved = isSaved,
)

internal fun RecentOrder.toUiModel() = RecentOrderUiModel(
    orderNumber = orderNumber,
    title = "Order #$orderNumber",
    date = formatOrderDate(orderDate),
    itemSummary = if (itemCount == 1) "1 item" else "$itemCount items",
    total = formatPrice(total),
)

private val orderDateFormatter = DateTimeFormatter.ofPattern("MMM d, uuuu", Locale.US)

internal fun formatOrderDate(isoDate: String): String = try {
    LocalDateTime.parse(isoDate).format(orderDateFormatter)
} catch (e: Exception) {
    "Date unavailable"
}

@HiltViewModel
class CatalogViewModel @Inject constructor(
    private val session: DemoCustomerSession,
    private val productRepository: ProductRepository,
    private val filterAndSortProducts: FilterAndSortProductsUseCase,
    private val getRecentOrders: GetRecentOrdersUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CatalogUiState(demoCustomer = session.activeCustomer))
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    private var catalogProducts = emptyList<Product>()
    private var savedProductIds = emptySet<Int>()
    private var refreshJob: Job? = null
    private var recentOrdersJob: Job? = null

    init {
        observeSavedProducts()
        observeRecentlyViewed()
        refresh()
    }

    fun onSearchQueryChange(query: String) = updateFilters { it.copy(searchQuery = query) }
    fun onCategorySelected(category: String) = updateFilters { it.copy(selectedCategory = category) }
    fun onSortSelected(sort: ProductSort) = updateFilters { it.copy(selectedSort = sort) }

    fun onSavedToggle(productId: Int) {
        val product = catalogProducts.firstOrNull { it.id == productId } ?: return
        val isSaved = productId in savedProductIds
        viewModelScope.launch {
            try {
                if (isSaved) productRepository.removeSavedProduct(productId)
                else productRepository.saveProduct(product)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = AppError.LocalStorage.toFriendlyMessage(ErrorSubject.UpdateSaved)) }
            }
        }
    }

    fun refresh() {
        if (refreshJob?.isActive == true) return
        recentOrdersJob?.cancel()
        refreshJob = viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    isLoading = state.products.isEmpty(),
                    isRefreshing = state.products.isNotEmpty(),
                    errorMessage = null,
                )
            }
            coroutineScope {
                launch { loadProducts() }
                launch { loadRecentOrders() }
            }
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun retryRecentOrders() {
        if (refreshJob?.isActive == true || recentOrdersJob?.isActive == true) return
        recentOrdersJob = viewModelScope.launch { loadRecentOrders() }
    }

    private suspend fun loadProducts() {
        productRepository.getProducts()
            .onSuccess { products ->
                catalogProducts = products
                _uiState.update { state -> state.withProducts().copy(isLoading = false, errorMessage = null) }
            }
            .onFailure { throwable ->

                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        errorMessage = throwable.toFriendlyMessage(ErrorSubject.Catalog),
                    )
                }
            }
    }

    private suspend fun loadRecentOrders() {
        _uiState.update { state ->
            state.copy(
                recentOrders = state.recentOrders.copy(
                    isLoading = state.recentOrders.orders.isEmpty(),
                    errorMessage = null,
                ),
            )
        }
        getRecentOrders(session.activeCustomer.id)
            .onSuccess { orders ->
                _uiState.update { state ->
                    state.copy(recentOrders = RecentOrdersSection(isLoading = false, orders = orders.map { it.toUiModel() }))
                }
            }
            .onFailure { throwable ->

                _uiState.update { state ->
                    state.copy(
                        recentOrders = state.recentOrders.copy(
                            isLoading = false,
                            errorMessage = throwable.toFriendlyMessage(ErrorSubject.Orders),
                        ),
                    )
                }
            }
    }

    private fun updateFilters(change: (CatalogUiState) -> CatalogUiState) {
        _uiState.update { state -> change(state).withProducts() }
    }

    private fun CatalogUiState.withProducts(): CatalogUiState {
        val visible = filterAndSortProducts(
            products = catalogProducts,
            searchQuery = searchQuery,
            category = selectedCategory,
            sort = selectedSort,
        )
        return copy(
            products = catalogProducts.map { it.toUiModel(it.id in savedProductIds) },
            visibleProducts = visible.map { it.toUiModel(it.id in savedProductIds) },
        )
    }

    private fun observeSavedProducts() {
        viewModelScope.launch {

            productRepository.observeSavedProducts().catch { }.collectLatest { savedProducts ->
                savedProductIds = savedProducts.mapTo(mutableSetOf()) { it.id }
                _uiState.update { it.withProducts() }
            }
        }
    }

    private fun observeRecentlyViewed() {
        viewModelScope.launch {
            productRepository.observeRecentlyViewedProducts()
                .catch {

                    _uiState.update { state ->
                        state.copy(
                            recentlyViewed = state.recentlyViewed.copy(
                                isLoading = false,
                                errorMessage = AppError.LocalStorage.toFriendlyMessage(ErrorSubject.RecentlyViewed),
                            ),
                        )
                    }
                }
                .collectLatest { products ->
                    _uiState.update { state ->
                        state.copy(recentlyViewed = RecentlyViewedSection(isLoading = false, products = products.map { it.toUiModel() }))
                    }
                }
        }
    }
}

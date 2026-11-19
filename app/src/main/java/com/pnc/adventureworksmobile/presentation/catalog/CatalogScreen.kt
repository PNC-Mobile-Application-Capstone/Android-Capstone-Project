package com.pnc.adventureworksmobile.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pnc.adventureworksmobile.core.session.DemoCustomer
import com.pnc.adventureworksmobile.domain.model.ProductAvailability
import com.pnc.adventureworksmobile.domain.model.ProductSort
import com.pnc.adventureworksmobile.presentation.shared.AvailabilityChip
import com.pnc.adventureworksmobile.presentation.shared.ErrorView
import com.pnc.adventureworksmobile.presentation.shared.ProductImage
import com.pnc.adventureworksmobile.ui.theme.AdventureWorksMobileTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    uiState: CatalogUiState,
    onSearchQueryChange: (String) -> Unit,
    onCategorySelected: (String) -> Unit,
    onSortSelected: (ProductSort) -> Unit,
    onRetry: () -> Unit,
    onSavedToggle: (Int) -> Unit,
    onProductSelected: (Int) -> Unit = {},
    onRetryRecentOrders: () -> Unit = {},
    onSeeAllOrders: () -> Unit = {},
    onSeeAllRecentlyViewed: () -> Unit = {},
) {
    val gridState = rememberLazyGridState()

    val filterKey = uiState.selectedCategory to uiState.selectedSort
    var previousFilterKey by remember { mutableStateOf(filterKey) }
    // On filter change, scroll to the "All products" header (chips sit just below it).
    val filterScrollIndex = if (uiState.showsHomeSections) 3 else 1
    LaunchedEffect(filterKey) {
        if (filterKey != previousFilterKey) {
            previousFilterKey = filterKey
            gridState.scrollToItem(filterScrollIndex)
        }
    }

    PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = onRetry,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            modifier = Modifier.fillMaxSize(),
            state = gridState,
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            fullWidthItem("header") {
                HomeHeader(
                    customerName = uiState.demoCustomer.displayName,
                    searchQuery = uiState.searchQuery,
                    onSearchQueryChange = onSearchQueryChange,
                )
            }
            if (uiState.showsHomeSections) {
                fullWidthItem("recently-viewed") {
                    RecentlyViewedHomeSection(
                        section = uiState.recentlyViewed,
                        onProductSelected = onProductSelected,
                        onSeeAll = onSeeAllRecentlyViewed,
                    )
                }
                fullWidthItem("recent-orders") {
                    RecentOrdersHomeSection(
                        section = uiState.recentOrders,
                        onRetry = onRetryRecentOrders,
                        onSeeAll = onSeeAllOrders,
                    )
                }
            }

            fullWidthItem("products-title") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SectionTitle(text = "All products", modifier = Modifier.weight(1f))
                    SortMenu(
                        selectedSort = uiState.selectedSort,
                        itemCount = uiState.visibleProducts.size,
                        onSortSelected = onSortSelected,
                    )
                }
            }

            fullWidthItem("categories") {
                CategoryChips(
                    categories = uiState.categories,
                    selectedCategory = uiState.selectedCategory,
                    onCategorySelected = onCategorySelected,
                )
            }

            productItems(
                uiState = uiState,
                onRetry = onRetry,
                onProductSelected = onProductSelected,
                onSavedToggle = onSavedToggle,
            )
        }
    }
}

private fun LazyGridScope.fullWidthItem(key: String, content: @Composable () -> Unit) {
    item(key = key, span = { GridItemSpan(maxLineSpan) }) { content() }
}

private fun LazyGridScope.productItems(
    uiState: CatalogUiState,
    onRetry: () -> Unit,
    onProductSelected: (Int) -> Unit,
    onSavedToggle: (Int) -> Unit,
) {
    when {
        uiState.isLoading -> fullWidthItem("products-loading") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        uiState.errorMessage != null && uiState.products.isEmpty() -> fullWidthItem("products-error") {
            ErrorView(
                message = uiState.errorMessage,
                onRetry = onRetry,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        else -> {

            if (uiState.errorMessage != null) {
                fullWidthItem("products-error-banner") {
                    InlineError(message = uiState.errorMessage, onRetry = onRetry)
                }
            }
            if (uiState.visibleProducts.isEmpty()) {
                fullWidthItem("products-empty") {
                    EmptyCatalog(catalogIsEmpty = uiState.products.isEmpty())
                }
            } else {
                items(uiState.visibleProducts, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        onClick = { onProductSelected(product.id) },
                        onSavedToggle = { onSavedToggle(product.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier.semantics { heading() },
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun SectionHeader(
    title: String,
    actionLabel: String?,
    actionDescription: String,
    onAction: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SectionTitle(text = title, modifier = Modifier.weight(1f))
        if (actionLabel != null) {
            TextButton(
                onClick = onAction,
                modifier = Modifier.semantics { this.contentDescription = actionDescription },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary),
            ) {
                Text(actionLabel)
            }
        }
    }
}

@Composable
private fun CategoryChips(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(categories) { category ->
            FilterChip(
                selected = category == selectedCategory,
                onClick = { onCategorySelected(category) },
                label = { Text(category) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedContainerColor = MaterialTheme.colorScheme.secondary,
                    selectedLabelColor = MaterialTheme.colorScheme.onSecondary,
                ),
            )
        }
    }
}

@Composable
private fun RecentlyViewedHomeSection(
    section: RecentlyViewedSection,
    onProductSelected: (Int) -> Unit,
    onSeeAll: () -> Unit,
) {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        SectionHeader(
            title = "Recently viewed",
            actionLabel = if (section.products.isNotEmpty()) "See all" else null,
            actionDescription = "See all recently viewed products",
            onAction = onSeeAll,
        )
        when {
            section.isLoading -> SectionLoading()
            section.errorMessage != null -> SectionMessage(section.errorMessage)
            section.products.isEmpty() -> SectionMessage("Products you open will appear here.")
            else -> LazyRow(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(section.products, key = { it.id }) { product ->
                    RecentProductCard(product = product, onClick = { onProductSelected(product.id) })
                }
            }
        }
    }
}

@Composable
private fun RecentProductCard(product: ProductUiModel, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable(role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.25f)
                .background(Color.White),
        ) {
            ProductImage(
                imageBase64 = product.thumbnailBase64,
                imageFileName = product.thumbnailPhotoFileName,
                category = product.category,
                productName = product.name,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
            )
        }
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = product.name,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = product.price,
                modifier = Modifier.padding(top = 4.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun RecentOrdersHomeSection(
    section: RecentOrdersSection,
    onRetry: () -> Unit,
    onSeeAll: () -> Unit,
) {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        SectionHeader(
            title = "Recent orders",
            actionLabel = "See all",
            actionDescription = "See all orders",
            onAction = onSeeAll,
        )
        when {
            section.isLoading -> SectionLoading()
            section.errorMessage != null && section.orders.isEmpty() ->
                InlineError(message = section.errorMessage, onRetry = onRetry)
            section.orders.isEmpty() -> SectionMessage("You don't have any orders yet.")
            else -> Column(
                modifier = Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (section.errorMessage != null) {
                    InlineError(message = section.errorMessage, onRetry = onRetry)
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(section.orders, key = { it.orderNumber }) { order ->
                        RecentOrderCard(order = order, onClick = onSeeAll)
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentOrderCard(order: RecentOrderUiModel, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .heightIn(min = 48.dp)
            .clickable(role = Role.Button, onClickLabel = "open your orders", onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = order.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = order.date,
                modifier = Modifier.padding(top = 2.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = order.itemSummary,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = order.total,
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun SectionLoading() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(modifier = Modifier.size(28.dp))
    }
}

@Composable
private fun SectionMessage(message: String) {
    Text(
        text = message,
        modifier = Modifier.padding(vertical = 8.dp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
private fun InlineError(message: String, onRetry: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = message,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
            )
            TextButton(onClick = onRetry) {
                Text("Try again")
            }
        }
    }
}

@Composable
private fun SortMenu(
    selectedSort: ProductSort,
    itemCount: Int,
    onSortSelected: (ProductSort) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        TextButton(
            onClick = { expanded = true },
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.secondary,
            ),
        ) {
            Text("$itemCount items · ${selectedSort.label}")
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = null,
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            ProductSort.entries.forEach { sort ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (sort == selectedSort) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 8.dp),
                                )
                            }
                            Text(sort.label)
                        }
                    },
                    onClick = {
                        onSortSelected(sort)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun HomeHeader(
    customerName: String,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
) {
    Column(modifier = Modifier.padding(top = 20.dp)) {
        Text(
            text = "Adventure Works",
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Find your next adventure",
            modifier = Modifier
                .padding(top = 4.dp)
                .semantics { heading() },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Welcome back, ${customerName.substringBefore(' ')}",
            modifier = Modifier.padding(top = 4.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp),
            placeholder = { Text("Search bikes, gear, and clothing") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search",
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
            ),
        )
    }
}

@Composable
private fun ProductCard(
    product: ProductUiModel,
    onClick: () -> Unit,
    onSavedToggle: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.25f)
                .background(Color.White),
        ) {
            ProductImage(
                imageBase64 = product.thumbnailBase64,
                imageFileName = product.thumbnailPhotoFileName,
                category = product.category,
                productName = product.name,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
            )
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(36.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            ) {
                IconButton(onClick = onSavedToggle) {
                    Icon(
                        imageVector = if (product.isSaved) {
                            Icons.Outlined.Favorite
                        } else {
                            Icons.Outlined.FavoriteBorder
                        },
                        contentDescription = if (product.isSaved) {
                            "Remove ${product.name} from saved products"
                        } else {
                            "Save ${product.name}"
                        },
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = product.category,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = product.name,
                modifier = Modifier.padding(top = 4.dp),
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = product.price,
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            AvailabilityChip(
                availability = product.availability,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun EmptyCatalog(catalogIsEmpty: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = if (catalogIsEmpty) "No products available" else "No products found",
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (catalogIsEmpty) {
                "The catalog is empty right now. Pull down to refresh."
            } else {
                "Try a different search or category."
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun CatalogScreenPreview() {
    val bike = ProductUiModel(
        id = 1,
        name = "Trail Mountain Bike",
        productNumber = "BK-M68B-42",
        category = "Bikes",
        price = "$1,299.00",
        thumbnailBase64 = null,
        thumbnailPhotoFileName = "no_image_available_large.gif",
        availability = ProductAvailability.LowStock,
    )
    AdventureWorksMobileTheme(darkTheme = false) {
        CatalogScreen(
            uiState = CatalogUiState(
                demoCustomer = DemoCustomer(id = 0, displayName = "Daniel Davis"),
                products = listOf(bike),
                visibleProducts = listOf(bike),
                isLoading = false,
                recentlyViewed = RecentlyViewedSection(isLoading = false, products = listOf(bike)),
                recentOrders = RecentOrdersSection(
                    isLoading = false,
                    orders = listOf(
                        RecentOrderUiModel(
                            orderNumber = 43659,
                            title = "Order #43659",
                            date = "Sep 25, 2003",
                            itemSummary = "3 items",
                            total = "$2,024.99",
                        ),
                    ),
                ),
            ),
            onSearchQueryChange = {},
            onCategorySelected = {},
            onSortSelected = {},
            onRetry = {},
            onSavedToggle = {},
        )
    }
}

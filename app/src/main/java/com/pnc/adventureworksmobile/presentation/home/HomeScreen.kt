package com.pnc.adventureworksmobile.presentation.home

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pnc.adventureworksmobile.core.session.DemoCustomer
import com.pnc.adventureworksmobile.ui.theme.AdventureWorksMobileTheme

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onSearchQueryChange: (String) -> Unit,
    onCategorySelected: (String) -> Unit,
    onSortSelected: (ProductSort) -> Unit,
    onRetry: () -> Unit,
    onProductSelected: (Int) -> Unit = {},
    onSavedToggle: (Int) -> Unit = {},
) {
    val productGridState = rememberLazyGridState()

    LaunchedEffect(uiState.selectedCategory, uiState.selectedSort) {
        productGridState.scrollToItem(0)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        HomeHeader(
            customerName = uiState.demoCustomer.displayName,
            searchQuery = uiState.searchQuery,
            onSearchQueryChange = onSearchQueryChange,
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(uiState.categories) { category ->
                FilterChip(
                    selected = category == uiState.selectedCategory,
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "All products",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            SortMenu(
                selectedSort = uiState.selectedSort,
                itemCount = uiState.visibleProducts.size,
                onSortSelected = onSortSelected,
            )
        }

        when {
            uiState.isLoading -> LoadingCatalog(modifier = Modifier.fillMaxSize())
            uiState.errorMessage != null && uiState.products.isEmpty() -> ErrorCatalog(
                message = uiState.errorMessage,
                onRetry = onRetry,
                modifier = Modifier.fillMaxSize(),
            )
            uiState.visibleProducts.isEmpty() -> EmptyCatalog(modifier = Modifier.fillMaxSize())
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    modifier = Modifier.fillMaxSize(),
                    state = productGridState,
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
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
    Column(
        modifier = Modifier.padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 12.dp),
    ) {
        Text(
            text = "Adventure Works",
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Find your next adventure",
            modifier = Modifier.padding(top = 4.dp),
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
        }
    }
}

//@Composable
//internal fun ProductImage(
//    imageBase64: String?,
//    imageFileName: String?,
//    category: String,
//    productName: String,
//    modifier: Modifier = Modifier,
//) {
//    val currentImage = if (imageFileName?.startsWith("no_image_available") ?: true) defaultProductImage(category) else imageBase64
//
//    val imageBitmap = remember(currentImage) {
//        currentImage?.let { encoded ->
//            runCatching {
//                val bytes = Base64.decode(encoded, Base64.DEFAULT)
//                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
//            }.getOrNull()
//        }
//    }
//
//    if (imageBitmap != null) {
//        Image(
//            bitmap = imageBitmap,
//            contentDescription = productName,
//            modifier = modifier,
//            contentScale = ContentScale.Fit,
//        )
//    } else {
//        Box(modifier = modifier, contentAlignment = Alignment.Center) {
//            Text(
//                text = "Image unavailable",
//                color = MaterialTheme.colorScheme.onSurfaceVariant,
//                style = MaterialTheme.typography.labelMedium,
//                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
//            )
//        }
//    }
//}

@Composable
private fun LoadingCatalog(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorCatalog(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        Button(onClick = onRetry, modifier = Modifier.padding(top = 16.dp)) {
            Text("Try again")
        }
    }
}

@Composable
private fun EmptyCatalog(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "No products found", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Try a different search or category.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun HomeScreenPreview() {
    AdventureWorksMobileTheme(darkTheme = false) {
        HomeScreen(
            uiState = HomeUiState(
                demoCustomer = DemoCustomer(id = 0, displayName = "Daniel Davis"),
                products = listOf(
                    ProductUiModel(
                        id = 1,
                        name = "Trail Mountain Bike",
                        productNumber = "BK-M68B-42",
                        category = "Bikes",
                        price = "$1,299.00",
                        listPrice = 1299.0,
                        thumbnailBase64 = null,
                        thumbnailPhotoFileName = "no_image_available_large.gif"
                    ),
                ),
                isLoading = false,
            ),
            onSearchQueryChange = {},
            onCategorySelected = {},
            onSortSelected = {},
            onRetry = {},
        )
    }
}

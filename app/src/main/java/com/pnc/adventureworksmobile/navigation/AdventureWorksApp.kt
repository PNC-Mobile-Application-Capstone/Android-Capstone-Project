package com.pnc.adventureworksmobile.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.WatchLater
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.pnc.adventureworksmobile.presentation.home.CatalogViewModel
import com.pnc.adventureworksmobile.presentation.home.CatalogScreen
import com.pnc.adventureworksmobile.presentation.product.ProductDetailScreen
import com.pnc.adventureworksmobile.presentation.product.ProductDetailViewModel
import com.pnc.adventureworksmobile.presentation.recent.RecentlyViewedScreen
import com.pnc.adventureworksmobile.presentation.EmptyDestination
import com.pnc.adventureworksmobile.presentation.recent.RecentlyViewedUiState
import com.pnc.adventureworksmobile.presentation.recent.RecentlyViewedViewModel


enum class TopLevelDestination(
    val route: String,
    val title: String,
    val icon: ImageVector,
) {
    Catalog(route = "catalog", title = "Catalog", icon = Icons.Outlined.Storefront),
    Orders(route = "orders", title = "Orders", icon = Icons.AutoMirrored.Outlined.ReceiptLong),
    Saved(route = "saved", title = "Saved", icon = Icons.Outlined.FavoriteBorder),
    Recent(route = "recent", title = "Recent Products", icon = Icons.Outlined.WatchLater),
}

@Composable
fun AdventureWorksApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val isTopLevelDestination = TopLevelDestination.entries.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (isTopLevelDestination) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    tonalElevation = 0.dp,
                ) {
                    TopLevelDestination.entries.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = {
                                navController.navigate(destination.route) {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(TopLevelDestination.Catalog.route) {
                                        saveState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = null,
                                )
                            },
                            label = { Text(destination.title) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = TopLevelDestination.Catalog.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None },
        ) {

            composable(TopLevelDestination.Catalog.route) {
                val viewModel: CatalogViewModel = hiltViewModel()

                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                CatalogScreen(
                    uiState = uiState,
                    onSearchQueryChange = viewModel::onSearchQueryChange,
                    onCategorySelected = viewModel::onCategorySelected,
                    onSortSelected = viewModel::onSortSelected,
                    onRetry = viewModel::refresh,
                    onProductSelected = { productId ->
                        navController.navigate("product/$productId")
                    },
                    onSavedToggle = viewModel::onSavedToggle,
                )
            }
            composable(
                route = "product/{productId}",
                arguments = listOf(navArgument("productId") { type = NavType.IntType }),
            ) {
                val viewModel: ProductDetailViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                ProductDetailScreen(
                    uiState = uiState,
                    onBack = navController::navigateUp,
                    onRetry = viewModel::retry,
                    onSavedToggle = viewModel::onSavedToggle,
                )
            }
            composable(TopLevelDestination.Orders.route) {
                EmptyDestination(
                    title = "Orders",
                    message = "Your orders will appear here.",
                )
            }
            composable(TopLevelDestination.Saved.route) {
                EmptyDestination(
                    title = "Saved",
                    message = "Your saved products will appear here.",
                )
            }
            composable(TopLevelDestination.Recent.route) {
                val viewModel: RecentlyViewedViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                RecentlyViewedScreen(
                    uiState = uiState,
                    onButtonClick = viewModel::onButtonClick
                )
            }
        }
    }
}

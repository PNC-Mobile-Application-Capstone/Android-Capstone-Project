package com.pnc.adventureworksmobile.presentation.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pnc.adventureworksmobile.presentation.home.HomeViewModel

enum class TopLevelDestination(
    val route: String,
    val title: String,
) {
    Discover(route = "discover", title = "Discover"),
    Orders(route = "orders", title = "Orders"),
    Saved(route = "saved", title = "Saved"),
}

@Composable
fun AdventureWorksApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                TopLevelDestination.entries.forEach { destination ->
                    NavigationBarItem(
                        selected = currentRoute == destination.route,
                        onClick = {
                            navController.navigate(destination.route) {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(TopLevelDestination.Discover.route) {
                                    saveState = true
                                }
                            }
                        },
                        icon = { Text(destination.title.take(1)) },
                        label = { Text(destination.title) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = TopLevelDestination.Discover.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(TopLevelDestination.Discover.route) {
                DiscoverStarterScreen()
            }
            composable(TopLevelDestination.Orders.route) {
                StarterDestination(
                    title = "Orders",
                    message = "Order history and order details will be built in this feature slice.",
                )
            }
            composable(TopLevelDestination.Saved.route) {
                StarterDestination(
                    title = "Saved",
                    message = "Saved products will use the Room foundation included in this setup.",
                )
            }
        }
    }
}

@Composable
private fun DiscoverStarterScreen(
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    StarterDestination(
        title = "AdventureWorks Customer Companion",
        message = "Demo customer: ${uiState.demoCustomer.displayName} " +
            "(#${uiState.demoCustomer.id})",
    )
}

@Composable
private fun StarterDestination(
    title: String,
    message: String,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = title,
            textAlign = TextAlign.Center,
        )
        Text(
            text = message,
            modifier = Modifier.padding(top = 12.dp),
            textAlign = TextAlign.Center,
        )
    }
}

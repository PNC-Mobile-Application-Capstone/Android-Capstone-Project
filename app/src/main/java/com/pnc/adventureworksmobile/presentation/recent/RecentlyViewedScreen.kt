package com.pnc.adventureworksmobile.presentation.recent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.pnc.adventureworksmobile.presentation.EmptyDestination

@Composable
fun RecentlyViewedScreen(
    uiState: RecentlyViewedUiState,
    onButtonClick: () -> Unit,
) {
    when {
        uiState.isLoading -> Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center,
        ) {
            Column() {
                CircularProgressIndicator()
                Button(onClick = onButtonClick) { Text("Load") }
            }
        }
        !uiState.isLoading -> EmptyDestination(
            title = "Recently Viewed Products",
            message = "Your saved products will appear here.",
        )
    }
}
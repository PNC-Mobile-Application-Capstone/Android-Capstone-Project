package com.pnc.adventureworksmobile.presentation.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pnc.adventureworksmobile.domain.model.OrderDetails
import java.text.NumberFormat
import java.util.Locale

@Composable
fun OrderDetailsScreen(
    uiState: OrderDetailsUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            uiState.errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "Order unavailable",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.errorMessage,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onRetry) {
                        Text("Try again")
                    }
                    TextButton(onClick = onBack) {
                        Text("Back to orders")
                    }
                }
            }

            uiState.orderDetails != null -> {
                OrderDetailsContent(
                    details = uiState.orderDetails,
                    onBack = onBack,
                )
            }
        }
    }
}

@Composable
private fun OrderDetailsContent(
    details: OrderDetails,
    onBack: () -> Unit,
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = null,
                )
                Text("Back")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Order #${details.orderNumber}",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )

        Text(
            text = "Placed on ${details.orderDate}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Product info
        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Item Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                details.productName?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                DetailRow("Product ID", details.productId.toString())
                DetailRow("Quantity", details.orderQty.toString())
                DetailRow("Unit Price", currencyFormatter.format(details.unitPrice))
                if (details.unitPriceDiscount > 0.0) {
                    DetailRow(
                        "Discount",
                        currencyFormatter.format(details.unitPriceDiscount),
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                DetailRow(
                    label = "Line Total",
                    value = currencyFormatter.format(details.lineTotal),
                    isBold = true,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Shipping & Summary
        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Shipping & Costs",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                details.shipDate?.let { DetailRow("Ship Date", it) }
                details.shipMethodName?.let { DetailRow("Shipping Method", it) }
                details.carrierTrackingNumber?.let { DetailRow("Tracking Number", it) }
                DetailRow("Freight Amount", currencyFormatter.format(details.freight))
                DetailRow("Tax Amount", currencyFormatter.format(details.taxAmt))
            }
        }

        // Contact info if present
        val fullName = listOfNotNull(details.firstName, details.middleName, details.lastName)
            .filter { it.isNotBlank() }
            .joinToString(" ")
        if (fullName.isNotBlank() || details.emailAddress != null || details.phoneNumber != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Customer Info",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (fullName.isNotBlank()) DetailRow("Name", fullName)
                    details.emailAddress?.let { DetailRow("Email", it) }
                    details.phoneNumber?.let { DetailRow("Phone", it) }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    isBold: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

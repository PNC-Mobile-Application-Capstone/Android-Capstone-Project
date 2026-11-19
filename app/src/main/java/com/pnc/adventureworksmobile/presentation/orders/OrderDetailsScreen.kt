package com.pnc.adventureworksmobile.presentation.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.pnc.adventureworksmobile.domain.model.OrderDetails
import com.pnc.adventureworksmobile.presentation.shared.ErrorView
import java.text.NumberFormat
import java.util.Locale

@Composable
fun OrderDetailsScreen(
    uiState: OrderDetailsUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onProductClick: (Int) -> Unit,
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
                ErrorView(
                    message = uiState.errorMessage,
                    onRetry = onRetry,
                    modifier = Modifier.align(Alignment.Center),
                    title = "Order unavailable",
                    secondaryActionLabel = "Back to orders",
                    onSecondaryAction = onBack,
                )
            }

            uiState.orderDetails != null -> {
                OrderDetailsContent(
                    details = uiState.orderDetails,
                    onBack = onBack,
                    onProductClick = onProductClick,
                    modifier = Modifier.align(Alignment.TopCenter),
                )
            }
        }
    }
}

@Composable
private fun OrderDetailsContent(
    details: OrderDetails,
    onBack: () -> Unit,
    onProductClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)

    Column(
        modifier = modifier
            .fillMaxSize()
            .widthIn(max = 840.dp)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = onBack,
                modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.padding(horizontal = 2.dp))
                Text("Back")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Column(modifier = Modifier.weight(1f, fill = false)) {
                Text(
                    text = "Order #${details.orderNumber}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    text = "Placed on ${formatDate(details.orderDate)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                details.statusName?.let { status ->
                    Text(
                        text = "Status: $status",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
            Text(
                text = currencyFormatter.format(details.totalPrice),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Item Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics { heading() },
                )
                Spacer(modifier = Modifier.height(8.dp))
                details.orderLineDetails.forEachIndexed { index, line ->
                    if (index > 0) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    }
                    line.productName?.let {
                        Text(
                            text = it,
                            modifier = Modifier
                                .defaultMinSize(minHeight = 48.dp)
                                .clickable(
                                    role = Role.Button,
                                    onClickLabel = "view product",
                                    onClick = { onProductClick(line.productId) },
                                )
                                .padding(vertical = 12.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    DetailRow("Product ID", line.productId.toString())
                    DetailRow("Quantity", line.orderQty.toString())
                    DetailRow("Unit Price", currencyFormatter.format(line.unitPrice))
                    if (line.unitPriceDiscount > 0.0) {
                        DetailRow(
                            "Discount",
                            currencyFormatter.format(line.unitPriceDiscount),
                        )
                    }
                    DetailRow(
                        label = "Line Total",
                        value = currencyFormatter.format(line.lineTotal),
                        isBold = true,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Shipping & Costs",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics { heading() },
                )
                Spacer(modifier = Modifier.height(8.dp))
                details.statusName?.let { DetailRow("Status", it) }
                DetailRow("Items Subtotal", currencyFormatter.format(details.subtotal))
                details.shipDate?.let { DetailRow("Ship Date", formatDate(it)) }
                details.shipMethodName?.let { DetailRow("Shipping Method", it) }
                details.carrierTrackingNumber?.let { DetailRow("Tracking Number", it) }
                DetailRow("Freight Amount", currencyFormatter.format(details.freight))
                DetailRow("Tax Amount", currencyFormatter.format(details.taxAmt))
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                DetailRow(
                    label = "Total Price",
                    value = currencyFormatter.format(details.totalPrice),
                    isBold = true,
                )
            }
        }

        val fullName = listOfNotNull(details.firstName, details.middleName, details.lastName)
            .filter { it.isNotBlank() }
            .joinToString(" ")
        if (fullName.isNotBlank() || details.emailAddress != null || details.phoneNumber != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Customer Info",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.semantics { heading() },
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
            .padding(vertical = 4.dp)
            .semantics(mergeDescendants = true) {},
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
package com.pnc.adventureworksmobile.presentation.home

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pnc.adventureworksmobile.domain.model.ProductDetails
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ProductDetailScreen(
    uiState: ProductDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onSavedToggle: () -> Unit,
) {
    when {
        uiState.isLoading -> Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        uiState.product != null -> ProductDetailContent(
            product = uiState.product,
            isSaved = uiState.isSaved,
            onBack = onBack,
            onSavedToggle = onSavedToggle,
        )
        else -> ProductDetailError(
            message = uiState.errorMessage.orEmpty(),
            onBack = onBack,
            onRetry = onRetry,
        )
    }
}

@Composable
private fun ProductDetailContent(
    product: ProductDetails,
    isSaved: Boolean,
    onBack: () -> Unit,
    onSavedToggle: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onBack) {
                Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null)
                Text("Back")
            }
            TextButton(onClick = onSavedToggle) {
                Icon(
                    imageVector = if (isSaved) {
                        Icons.Outlined.Favorite
                    } else {
                        Icons.Outlined.FavoriteBorder
                    },
                    contentDescription = null,
                )
                Text(if (isSaved) "Saved" else "Save")
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            ProductImage(
                imageBase64 = product.photoBase64,
                productName = product.name,
                modifier = Modifier.padding(28.dp),
            )
        }

        Text(
            text = product.productModelName ?: product.productNumber,
            modifier = Modifier.padding(top = 22.dp),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = product.name,
            modifier = Modifier.padding(top = 6.dp),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = NumberFormat.getCurrencyInstance(Locale.US).format(product.listPrice),
            modifier = Modifier.padding(top = 10.dp),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        if (product.summary.isNotBlank()) {
            DetailSection(title = "About", body = product.summary)
        }

        val specifications = listOfNotNull(
            product.color?.let { "Color" to it },
            product.material?.let { "Material" to it },
            product.style?.let { "Style" to it },
            product.productLine?.let { "Product line" to it },
            product.riderExperience?.let { "Best for" to it },
            product.manufacturer?.let { "Manufacturer" to it },
        )
        if (specifications.isNotEmpty()) {
            Text(
                text = "Details",
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            specifications.forEach { (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(text = value, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                }
            }
        }

        product.warrantyDescription?.takeIf { it.isNotBlank() }?.let {
            DetailSection(title = product.warrantyPeriod ?: "Warranty", body = it)
        }
        product.maintenanceDescription?.takeIf { it.isNotBlank() }?.let {
            DetailSection(title = "Care", body = it)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun DetailSection(title: String, body: String) {
    Text(
        text = title,
        modifier = Modifier.padding(top = 24.dp, bottom = 6.dp),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
    )
    Text(
        text = body,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodyLarge,
    )
}

@Composable
private fun ProductDetailError(
    message: String,
    onBack: () -> Unit,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "Product unavailable", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(
            text = message,
            modifier = Modifier.padding(top = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onRetry, modifier = Modifier.padding(top = 18.dp)) { Text("Try again") }
        TextButton(onClick = onBack) { Text("Back to catalog") }
    }
}

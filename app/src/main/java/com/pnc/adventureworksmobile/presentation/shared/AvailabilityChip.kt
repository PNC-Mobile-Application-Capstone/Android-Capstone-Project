package com.pnc.adventureworksmobile.presentation.shared

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pnc.adventureworksmobile.domain.model.ProductAvailability

val ProductAvailability.label: String
    get() = when (this) {
        ProductAvailability.InStock -> "In stock"
        ProductAvailability.LowStock -> "Low stock"
        ProductAvailability.Unavailable -> "Unavailable"
        ProductAvailability.Unknown -> "Availability unknown"
    }

private val ProductAvailability.icon: ImageVector
    get() = when (this) {
        ProductAvailability.InStock -> Icons.Outlined.CheckCircle
        ProductAvailability.LowStock -> Icons.Outlined.Warning
        ProductAvailability.Unavailable -> Icons.Outlined.Block
        ProductAvailability.Unknown -> Icons.AutoMirrored.Outlined.HelpOutline
    }

@Composable
private fun ProductAvailability.color(): Color = when (this) {
    ProductAvailability.InStock -> MaterialTheme.colorScheme.primary
    ProductAvailability.LowStock -> MaterialTheme.colorScheme.secondary
    ProductAvailability.Unavailable -> MaterialTheme.colorScheme.error
    ProductAvailability.Unknown -> MaterialTheme.colorScheme.onSurfaceVariant
}

@Composable
fun AvailabilityChip(
    availability: ProductAvailability,
    modifier: Modifier = Modifier,
) {
    val color = availability.color()
    Surface(
        modifier = modifier.clearAndSetSemantics {
            contentDescription = "Availability: ${availability.label}"
        },
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f),
        contentColor = color,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = availability.icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = availability.label,
                modifier = Modifier.padding(start = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

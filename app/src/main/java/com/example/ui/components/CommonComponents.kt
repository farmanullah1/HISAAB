package com.example.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun getCategoryIcon(iconKey: String): ImageVector {
    return when (iconKey.lowercase()) {
        "food", "restaurant" -> Icons.Default.Restaurant
        "groceries", "grocery" -> Icons.Default.ShoppingCart
        "fuel", "transport" -> Icons.Default.LocalGasStation
        "bills" -> Icons.Default.Receipt
        "rent" -> Icons.Default.Home
        "health", "medical" -> Icons.Default.MedicalServices
        "shopping" -> Icons.Default.ShoppingBag
        "education", "school" -> Icons.Default.School
        "salary", "payments" -> Icons.Default.Payments
        else -> Icons.Default.Category
    }
}

fun formatDateHeader(epochMs: Long): String {
    val cal = Calendar.getInstance()
    val todayYear = cal.get(Calendar.YEAR)
    val todayDay = cal.get(Calendar.DAY_OF_YEAR)

    cal.timeInMillis = epochMs
    val itemYear = cal.get(Calendar.YEAR)
    val itemDay = cal.get(Calendar.DAY_OF_YEAR)

    return when {
        todayYear == itemYear && todayDay == itemDay -> "Today"
        todayYear == itemYear && todayDay - 1 == itemDay -> "Yesterday"
        else -> SimpleDateFormat("dd MMM, yyyy", Locale.US).format(Date(epochMs))
    }
}

@Composable
fun QuietCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp)),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    ) {
        content()
    }
}

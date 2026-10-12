package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NumericKeypad(
    currentValue: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(".", "0", "BACK")
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        keys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { key ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(58.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .clickable {
                                handleKeyPress(currentValue, key, onValueChange)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (key == "BACK") {
                            Icon(
                                imageVector = Icons.Default.Backspace,
                                contentDescription = "Backspace",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(4.dp)
                            )
                        } else {
                            Text(
                                text = key,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun handleKeyPress(
    current: String,
    key: String,
    onValueChange: (String) -> Unit
) {
    when (key) {
        "BACK" -> {
            if (current.isNotEmpty()) {
                onValueChange(current.dropLast(1))
            }
        }
        "." -> {
            if (!current.contains(".")) {
                onValueChange(if (current.isEmpty()) "0." else "$current.")
            }
        }
        else -> {
            // Numbers 0-9
            if (current == "0") {
                onValueChange(key)
                return
            }
            if (current.contains(".")) {
                val parts = current.split(".")
                if (parts.size > 1 && parts[1].length >= 2) {
                    // Maximum 2 decimal places allowed, ignore extra keys
                    return
                }
            }
            if (current.length < 10) {
                onValueChange(current + key)
            }
        }
    }
}

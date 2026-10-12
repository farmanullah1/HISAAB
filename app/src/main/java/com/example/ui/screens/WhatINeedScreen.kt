package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.NeedEntity
import com.example.model.Money
import com.example.ui.components.QuietCard
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.HeroGradientBrush
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.TextDeepCharcoal
import com.example.ui.theme.TextMediumGray
import com.example.ui.theme.WarmCreamBackground
import com.example.ui.viewmodel.NeedsUiState

enum class NeedFilter(val label: String) {
    ACTIVE("Pending"),
    ALL("All Items"),
    DONE("Done")
}

@Composable
fun WhatINeedScreen(
    state: NeedsUiState,
    onAddNeed: (name: String, pricePaisa: Long?, where: String, note: String) -> Unit,
    onToggleDone: (Long, Boolean) -> Unit,
    onDeleteNeed: (NeedEntity) -> Unit,
    onClearDone: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf(NeedFilter.ACTIVE) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }

    val displayedNeeds = remember(state, selectedFilter) {
        when (selectedFilter) {
            NeedFilter.ACTIVE -> state.activeNeeds
            NeedFilter.DONE -> state.doneNeeds
            NeedFilter.ALL -> state.activeNeeds + state.doneNeeds
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmCreamBackground)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "What I Need",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDeepCharcoal
                        )
                        Text(
                            text = "Personal wishlist & shopping planner",
                            fontSize = 13.sp,
                            color = TextMediumGray
                        )
                    }

                    if (state.doneNeeds.isNotEmpty()) {
                        TextButton(
                            onClick = { showClearConfirm = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = TextMediumGray)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ClearAll,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Clear done", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Summary Card (Hero Style)
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp)),
                    color = Color.Transparent,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(HeroGradientBrush)
                            .padding(20.dp)
                    ) {
                        Column {
                            Text(
                                text = "ESTIMATED BUDGET NEEDED",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = Money.formatPkr(state.totalExpectedPricePaisa),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(Color.White, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${state.activeNeeds.size} items pending",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White
                                    )
                                }
                                Text(
                                    text = "${state.doneNeeds.size} acquired",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                }
            }

            // Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NeedFilter.values().forEach { filter ->
                        val isSelected = selectedFilter == filter
                        val count = when (filter) {
                            NeedFilter.ACTIVE -> state.activeNeeds.size
                            NeedFilter.DONE -> state.doneNeeds.size
                            NeedFilter.ALL -> state.activeNeeds.size + state.doneNeeds.size
                        }
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = filter },
                            label = { Text("${filter.label} ($count)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CoralPrimary,
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = TextMediumGray
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) CoralPrimary else MaterialTheme.colorScheme.outline
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            // Empty State
            if (displayedNeeds.isEmpty()) {
                item {
                    QuietCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = null,
                                tint = CoralPrimary.copy(alpha = 0.6f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = when (selectedFilter) {
                                    NeedFilter.ACTIVE -> "All caught up!"
                                    NeedFilter.DONE -> "No acquired items yet"
                                    NeedFilter.ALL -> "No items on your list"
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextDeepCharcoal
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Keep track of items you plan to buy, prices, and where to get them.",
                                fontSize = 13.sp,
                                color = TextMediumGray,
                                modifier = Modifier.padding(horizontal = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(displayedNeeds, key = { it.id }) { need ->
                    NeedItemCard(
                        need = need,
                        onToggleDone = { onToggleDone(need.id, !need.isDone) },
                        onDelete = { onDeleteNeed(need) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // FAB to add new need
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_need_button"),
            containerColor = CoralPrimary,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Item")
        }
    }

    if (showAddDialog) {
        AddNeedDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, pricePaisa, where, note ->
                onAddNeed(name, pricePaisa, where, note)
                showAddDialog = false
            }
        )
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear Completed Items") },
            text = { Text("Are you sure you want to remove all acquired items from the list?") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearDone()
                        showClearConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun NeedItemCard(
    need: NeedEntity,
    onToggleDone: () -> Unit,
    onDelete: () -> Unit
) {
    QuietCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleDone() }
            .testTag("need_item_${need.id}")
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // Checkbox
            IconButton(
                onClick = onToggleDone,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("toggle_need_${need.id}")
            ) {
                Icon(
                    imageVector = if (need.isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = if (need.isDone) "Mark pending" else "Mark acquired",
                    tint = if (need.isDone) IncomeGreen else TextMediumGray.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = need.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (need.isDone) TextMediumGray else TextDeepCharcoal,
                    textDecoration = if (need.isDone) TextDecoration.LineThrough else TextDecoration.None
                )

                if (need.expectedPricePaisa != null && need.expectedPricePaisa > 0) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Expected: ${Money.formatPkr(need.expectedPricePaisa)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = CoralPrimary
                    )
                }

                if (need.whereToGet.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = TextMediumGray,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = need.whereToGet,
                            fontSize = 12.sp,
                            color = TextMediumGray
                        )
                    }
                }

                if (need.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Notes,
                            contentDescription = null,
                            tint = TextMediumGray,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = need.note,
                            fontSize = 12.sp,
                            color = TextMediumGray,
                            maxLines = 2
                        )
                    }
                }
            }

            // Delete button
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("delete_need_${need.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete Item",
                    tint = TextMediumGray.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun AddNeedDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, pricePaisa: Long?, where: String, note: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var whereToGet by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add to Wishlist",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextDeepCharcoal
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    label = { Text("What thing do you need? *") },
                    placeholder = { Text("e.g. Rice cooker, Book, Oil") },
                    isError = nameError,
                    supportingText = if (nameError) { { Text("Item name is required") } } else null,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("need_name_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CoralPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Expected Price in PKR (Optional)") },
                    placeholder = { Text("e.g. 2500") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("need_price_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CoralPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                OutlinedTextField(
                    value = whereToGet,
                    onValueChange = { whereToGet = it },
                    label = { Text("Where or how to get it (Optional)") },
                    placeholder = { Text("e.g. Saddar Market, Daraz, Shop") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("need_where_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CoralPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (Optional)") },
                    placeholder = { Text("e.g. Compare prices first") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("need_note_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CoralPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                    } else {
                        val parsedPrice = Money.parsePaisa(priceText)
                        onSave(name.trim(), parsedPrice, whereToGet.trim(), note.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("save_need_button")
            ) {
                Text("Add Item")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Cancel", color = TextMediumGray)
            }
        }
    )
}

package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.AccountEntity
import com.example.data.entity.CategoryEntity
import com.example.model.Money
import com.example.model.TransactionType
import com.example.ui.components.NumericKeypad
import com.example.ui.components.getCategoryIcon
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.ExpenseCoral
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.TextDeepCharcoal
import com.example.ui.theme.TextMediumGray
import com.example.ui.theme.TransferBlue
import com.example.ui.theme.WarmCreamBackground
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    accounts: List<AccountEntity>,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSaveExpense: (amountPaisa: Long, fromAccountId: Long, categoryId: Long, note: String, date: Long) -> Unit,
    onSaveIncome: (amountPaisa: Long, toAccountId: Long, categoryId: Long, note: String, date: Long) -> Unit,
    onSaveTransfer: (amountPaisa: Long, fromId: Long, toId: Long, note: String, date: Long) -> Unit
) {
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var rawAmountInput by remember { mutableStateOf("") }
    var selectedFromAccountId by remember { mutableLongStateOf(accounts.firstOrNull()?.id ?: 0L) }
    var selectedToAccountId by remember {
        mutableLongStateOf(accounts.getOrNull(1)?.id ?: accounts.firstOrNull()?.id ?: 0L)
    }
    var selectedCategoryId by remember { mutableLongStateOf(0L) }
    var noteInput by remember { mutableStateOf("") }
    var selectedDateMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val filteredCategories = remember(categories, selectedType) {
        categories.filter { it.type == selectedType }
    }

    // Set default category when type or category list changes
    LaunchedEffect(selectedType, filteredCategories) {
        if (selectedType != TransactionType.TRANSFER && filteredCategories.isNotEmpty()) {
            if (filteredCategories.none { it.id == selectedCategoryId }) {
                selectedCategoryId = filteredCategories.first().id
            }
        }
    }

    val parsedPaisa = Money.parsePaisa(rawAmountInput) ?: 0L
    val isAmountValid = parsedPaisa > 0L

    val isSaveEnabled = when (selectedType) {
        TransactionType.EXPENSE -> isAmountValid && selectedFromAccountId != 0L && selectedCategoryId != 0L
        TransactionType.INCOME -> isAmountValid && selectedToAccountId != 0L && selectedCategoryId != 0L
        TransactionType.TRANSFER -> isAmountValid && selectedFromAccountId != 0L && selectedToAccountId != 0L && selectedFromAccountId != selectedToAccountId
    }

    val dateFormatter = remember { SimpleDateFormat("EEE, dd MMM yyyy", Locale.US) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmCreamBackground)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "New Entry",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextDeepCharcoal
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("close_add_transaction")
            ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextDeepCharcoal)
            }
        }

        // Scrollable Upper Half Form
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            // Segmented Control: [ Expense ] [ Income ] [ Transfer ]
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp)),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    listOf(
                        TransactionType.EXPENSE to "Expense",
                        TransactionType.INCOME to "Income",
                        TransactionType.TRANSFER to "Transfer"
                    ).forEach { (type, label) ->
                        val isSelected = selectedType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) {
                                        when (type) {
                                            TransactionType.EXPENSE -> ExpenseCoral
                                            TransactionType.INCOME -> IncomeGreen
                                            TransactionType.TRANSFER -> TransferBlue
                                        }
                                    } else Color.Transparent
                                )
                                .clickable { selectedType = type }
                                .padding(vertical = 10.dp)
                                .testTag("select_type_${type.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) Color.White else TextMediumGray
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Amount Display
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "AMOUNT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = TextMediumGray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = Money.formatPkr(parsedPaisa),
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (selectedType) {
                            TransactionType.EXPENSE -> ExpenseCoral
                            TransactionType.INCOME -> IncomeGreen
                            TransactionType.TRANSFER -> TransferBlue
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Pickers based on TransactionType
            when (selectedType) {
                TransactionType.EXPENSE -> {
                    // Account Picker Row (Source)
                    Text(
                        text = "From Account",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMediumGray
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        accounts.forEach { acc ->
                            val isSelected = acc.id == selectedFromAccountId
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedFromAccountId = acc.id },
                                label = { Text(acc.name) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CoralPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Category Chip Row
                    Text(
                        text = "Category",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMediumGray
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filteredCategories.forEach { cat ->
                            val isSelected = cat.id == selectedCategoryId
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategoryId = cat.id },
                                label = { Text(cat.name) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = getCategoryIcon(cat.iconKey),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ExpenseCoral,
                                    selectedLabelColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }

                TransactionType.INCOME -> {
                    // Destination Account Picker Row
                    Text(
                        text = "To Account",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMediumGray
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        accounts.forEach { acc ->
                            val isSelected = acc.id == selectedToAccountId
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedToAccountId = acc.id },
                                label = { Text(acc.name) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = IncomeGreen,
                                    selectedLabelColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Income Category Chips
                    Text(
                        text = "Category",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMediumGray
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filteredCategories.forEach { cat ->
                            val isSelected = cat.id == selectedCategoryId
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategoryId = cat.id },
                                label = { Text(cat.name) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = getCategoryIcon(cat.iconKey),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = IncomeGreen,
                                    selectedLabelColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }

                TransactionType.TRANSFER -> {
                    Text(
                        text = "From Account",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMediumGray
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        accounts.forEach { acc ->
                            val isSelected = acc.id == selectedFromAccountId
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedFromAccountId = acc.id },
                                label = { Text(acc.name) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TransferBlue,
                                    selectedLabelColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "To Account",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMediumGray
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        accounts.filter { it.id != selectedFromAccountId }.forEach { acc ->
                            val isSelected = acc.id == selectedToAccountId
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedToAccountId = acc.id },
                                label = { Text(acc.name) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TransferBlue,
                                    selectedLabelColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Optional Note Field
            OutlinedTextField(
                value = noteInput,
                onValueChange = { noteInput = it },
                label = { Text("What was this for? (Optional)") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("transaction_note_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedBorderColor = CoralPrimary
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Date Row
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                    .clickable { showDatePicker = true }
                    .padding(14.dp)
                    .testTag("transaction_date_picker"),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Date",
                        tint = TextMediumGray,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = dateFormatter.format(Date(selectedDateMs)),
                        fontSize = 13.sp,
                        color = TextDeepCharcoal
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // Bottom Half: Numeric Keypad & Primary Save Button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .padding(bottom = 16.dp)
        ) {
            NumericKeypad(
                currentValue = rawAmountInput,
                onValueChange = { rawAmountInput = it }
            )

            Button(
                onClick = {
                    when (selectedType) {
                        TransactionType.EXPENSE -> {
                            onSaveExpense(parsedPaisa, selectedFromAccountId, selectedCategoryId, noteInput, selectedDateMs)
                        }
                        TransactionType.INCOME -> {
                            onSaveIncome(parsedPaisa, selectedToAccountId, selectedCategoryId, noteInput, selectedDateMs)
                        }
                        TransactionType.TRANSFER -> {
                            onSaveTransfer(parsedPaisa, selectedFromAccountId, selectedToAccountId, noteInput, selectedDateMs)
                        }
                    }
                    onDismiss()
                },
                enabled = isSaveEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(52.dp)
                    .testTag("save_transaction_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CoralPrimary,
                    disabledContainerColor = CoralPrimary.copy(alpha = 0.3f)
                )
            ) {
                Text(
                    text = "Save",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMs)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedDateMs = it }
                        showDatePicker = false
                    }
                ) {
                    Text("OK", color = CoralPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = TextMediumGray)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

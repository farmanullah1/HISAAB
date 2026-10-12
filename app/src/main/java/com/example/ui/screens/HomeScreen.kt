package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import coil.compose.AsyncImage
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.AccountEntity
import com.example.data.entity.CategoryEntity
import com.example.data.entity.TransactionEntity
import com.example.model.Money
import com.example.model.TransactionType
import com.example.ui.components.QuietCard
import com.example.ui.components.formatDateHeader
import com.example.ui.components.getCategoryIcon
import com.example.ui.theme.BorderSoftOrange
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.ExpenseCoral
import com.example.ui.theme.HeroGradientBrush
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.TextDeepCharcoal
import com.example.ui.theme.TextMediumGray
import com.example.ui.theme.TransferBlue
import com.example.ui.theme.WarmCreamBackground
import com.example.ui.viewmodel.HomeUiState

@Composable
fun HomeScreen(
    state: HomeUiState,
    onDeleteTransaction: (Long) -> Unit,
    onEditTransaction: (TransactionEntity) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilterId by remember { mutableStateOf<Long?>(null) }

    // Filter transactions based on search and selected category
    val filteredTransactions = remember(state.transactions, searchQuery, selectedCategoryFilterId) {
        state.transactions.filter { txn ->
            val matchesCategory = selectedCategoryFilterId == null || txn.categoryId == selectedCategoryFilterId
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                val q = searchQuery.trim().lowercase()
                val catName = state.categories.find { it.id == txn.categoryId }?.name?.lowercase() ?: ""
                val fromName = state.accounts.find { it.id == txn.fromAccountId }?.name?.lowercase() ?: ""
                val toName = state.accounts.find { it.id == txn.toAccountId }?.name?.lowercase() ?: ""
                txn.note.lowercase().contains(q) || catName.contains(q) || fromName.contains(q) || toName.contains(q)
            }
            matchesCategory && matchesSearch
        }
    }

    // Group filtered transactions by date header
    val groupedTransactions = remember(filteredTransactions) {
        filteredTransactions.groupBy { formatDateHeader(it.dateEpochMs) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmCreamBackground),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // App Header
        item {
            Column {
                Text(
                    text = "Hisaab",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDeepCharcoal
                )
                Text(
                    text = "Personal expense & daily transactions",
                    fontSize = 13.sp,
                    color = TextMediumGray
                )
            }
        }

        // Top Section — Hero Gradient Balance Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp)),
                color = Color.Transparent,
                shape = RoundedCornerShape(22.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(HeroGradientBrush)
                        .padding(22.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TOTAL BALANCE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.25f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "PKR",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = Money.formatPkr(state.totalBalancePaisa),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Account breakdowns in white semi-transparent container
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Payments,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Cash",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White
                                    )
                                }
                                Text(
                                    text = Money.formatPkr(state.cashBalancePaisa),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalance,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Bank",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White
                                    )
                                }
                                Text(
                                    text = Money.formatPkr(state.bankBalancePaisa),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // Middle Section — This Month Summary Card
        item {
            QuietCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(IncomeGreen.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = IncomeGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Income this month",
                                    fontSize = 11.sp,
                                    color = TextMediumGray
                                )
                                Text(
                                    text = Money.formatPkr(state.thisMonthIncomePaisa),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IncomeGreen
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .height(30.dp)
                            .width(1.dp)
                            .background(MaterialTheme.colorScheme.outline)
                    )

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(ExpenseCoral.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    tint = ExpenseCoral,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Expense this month",
                                    fontSize = 11.sp,
                                    color = TextMediumGray
                                )
                                Text(
                                    text = Money.formatPkr(state.thisMonthExpensePaisa),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ExpenseCoral
                                )
                            }
                        }
                    }
                }
            }
        }

        // Search & Category Filters Row (Only show if there are transactions)
        if (state.transactions.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Search field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search transactions, notes, categories...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = TextMediumGray,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = TextMediumGray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("transaction_search_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CoralPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    // Category chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedCategoryFilterId == null,
                                onClick = { selectedCategoryFilterId = null },
                                label = { Text("All") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CoralPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                        items(state.categories) { cat ->
                            val isSelected = selectedCategoryFilterId == cat.id
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedCategoryFilterId = if (isSelected) null else cat.id
                                },
                                label = { Text(cat.name) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = getCategoryIcon(cat.iconKey),
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CoralPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Section Title: Ledger
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ledger",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDeepCharcoal
                )
                if (filteredTransactions.isNotEmpty()) {
                    Text(
                        text = "${filteredTransactions.size} records",
                        fontSize = 12.sp,
                        color = TextMediumGray
                    )
                }
            }
        }

        // Empty state
        if (filteredTransactions.isEmpty()) {
            item {
                QuietCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = CoralPrimary.copy(alpha = 0.6f),
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty() || selectedCategoryFilterId != null) {
                                "No transactions match your filter"
                            } else {
                                "No entries yet"
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDeepCharcoal
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty() || selectedCategoryFilterId != null) {
                                "Try clearing the search or category filter."
                            } else {
                                "Tap + to record your first transaction."
                            },
                            fontSize = 13.sp,
                            color = TextMediumGray
                        )
                    }
                }
            }
        } else {
            groupedTransactions.forEach { (dateHeader, txnsForDate) ->
                item {
                    Text(
                        text = dateHeader,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextMediumGray,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }

                items(txnsForDate, key = { it.id }) { txn ->
                    val category = state.categories.find { it.id == txn.categoryId }
                    val fromAccount = state.accounts.find { it.id == txn.fromAccountId }
                    val toAccount = state.accounts.find { it.id == txn.toAccountId }

                    QuietCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEditTransaction(txn) }
                            .testTag("transaction_item_${txn.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Category Icon Bubble
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(
                                        when (txn.type) {
                                            TransactionType.INCOME -> IncomeGreen.copy(alpha = 0.15f)
                                            TransactionType.EXPENSE -> ExpenseCoral.copy(alpha = 0.15f)
                                            TransactionType.TRANSFER -> TransferBlue.copy(alpha = 0.15f)
                                        },
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (txn.type == TransactionType.TRANSFER) {
                                    Icon(
                                        imageVector = Icons.Default.SwapHoriz,
                                        contentDescription = "Transfer",
                                        tint = TransferBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = getCategoryIcon(category?.iconKey ?: "other"),
                                        contentDescription = category?.name ?: "Category",
                                        tint = when (txn.type) {
                                            TransactionType.INCOME -> IncomeGreen
                                            TransactionType.EXPENSE -> ExpenseCoral
                                            else -> TransferBlue
                                        },
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            // Name + Note
                            Column(modifier = Modifier.weight(1f)) {
                                val title = when (txn.type) {
                                    TransactionType.TRANSFER -> "${fromAccount?.name ?: "Account"} → ${toAccount?.name ?: "Account"}"
                                    else -> category?.name ?: "Transaction"
                                }
                                Text(
                                    text = title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextDeepCharcoal
                                )
                                if (txn.note.isNotBlank()) {
                                    Text(
                                        text = txn.note,
                                        fontSize = 12.sp,
                                        color = TextMediumGray,
                                        maxLines = 1
                                    )
                                } else if (txn.type != TransactionType.TRANSFER) {
                                    val acc = if (txn.type == TransactionType.EXPENSE) fromAccount?.name else toAccount?.name
                                    acc?.let {
                                        Text(text = it, fontSize = 12.sp, color = TextMediumGray)
                                    }
                                }

                                if (!txn.receiptUri.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AttachFile,
                                            contentDescription = "Receipt attached",
                                            tint = ExpenseCoral,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "Receipt attached",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = ExpenseCoral
                                        )
                                    }
                                }
                            }

                            if (!txn.receiptUri.isNullOrBlank()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                AsyncImage(
                                    model = txn.receiptUri,
                                    contentDescription = "Receipt thumbnail",
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Amount on right
                            Text(
                                text = Money.formatSigned(txn.amountPaisa, txn.type),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (txn.type) {
                                    TransactionType.INCOME -> IncomeGreen
                                    TransactionType.EXPENSE -> ExpenseCoral
                                    TransactionType.TRANSFER -> TransferBlue
                                }
                            )

                            // Quick delete button
                            IconButton(
                                onClick = { onDeleteTransaction(txn.id) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("delete_transaction_${txn.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete",
                                    tint = TextMediumGray.copy(alpha = 0.5f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

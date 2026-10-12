package com.example.ui.screens

import android.content.Context
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.AccountEntity
import com.example.data.entity.CategoryEntity
import com.example.model.AccountType
import com.example.model.Money
import com.example.model.TransactionType
import com.example.ui.components.QuietCard
import com.example.ui.components.getCategoryIcon
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.TextDeepCharcoal
import com.example.ui.theme.TextMediumGray
import com.example.ui.theme.TransferBlue
import com.example.ui.theme.WarmCreamBackground
import com.example.ui.viewmodel.HomeUiState

@Composable
fun AccountsAndExportScreen(
    state: HomeUiState,
    onTransfer: (amountPaisa: Long, fromId: Long, toId: Long, note: String, date: Long, onSuccess: (String) -> Unit) -> Unit,
    onUpdateOpeningBalance: (AccountEntity, Long) -> Unit,
    onExportJson: (Context, (String) -> Unit) -> Unit
) {
    val context = LocalContext.current
    var showTransferDialog by remember { mutableStateOf(false) }
    var accountToEditOpening by remember { mutableStateOf<AccountEntity?>(null) }
    var exportStatus by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmCreamBackground),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Accounts & Storage",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDeepCharcoal
                )
                Text(
                    text = "Manage your accounts, balances and backup",
                    fontSize = 13.sp,
                    color = TextMediumGray
                )
            }
        }

        // Transfer Button Card
        item {
            QuietCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Transfer Funds",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDeepCharcoal
                        )
                        Text(
                            text = "Move money between Cash, Bank, and Wallet atomically",
                            fontSize = 12.sp,
                            color = TextMediumGray
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = { showTransferDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("open_transfer_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Transfer")
                    }
                }
            }
        }

        // Section: Accounts List
        item {
            Text(
                text = "Accounts (${state.accounts.size})",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextDeepCharcoal
            )
        }

        items(state.accounts, key = { it.id }) { account ->
            val balance = state.accountBalances[account.id] ?: 0L
            QuietCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("account_card_${account.id}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    when (account.type) {
                                        AccountType.CASH -> IncomeGreen.copy(alpha = 0.15f)
                                        AccountType.BANK -> TransferBlue.copy(alpha = 0.15f)
                                        AccountType.WALLET -> CoralPrimary.copy(alpha = 0.15f)
                                    },
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (account.type) {
                                    AccountType.CASH -> Icons.Default.AttachMoney
                                    AccountType.BANK -> Icons.Default.AccountBalance
                                    AccountType.WALLET -> Icons.Default.AccountBalanceWallet
                                },
                                contentDescription = null,
                                tint = when (account.type) {
                                    AccountType.CASH -> IncomeGreen
                                    AccountType.BANK -> TransferBlue
                                    AccountType.WALLET -> CoralPrimary
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = account.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextDeepCharcoal
                            )
                            Text(
                                text = "Opening: ${Money.formatPkr(account.openingBalancePaisa)}",
                                fontSize = 12.sp,
                                color = TextMediumGray
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = Money.formatPkr(balance),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDeepCharcoal
                        )
                        IconButton(
                            onClick = { accountToEditOpening = account },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("edit_opening_${account.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit opening balance",
                                tint = TextMediumGray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Section: Data & Privacy
        item {
            Text(
                text = "Backup & Data Integrity",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextDeepCharcoal
            )
        }

        item {
            QuietCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = IncomeGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "100% Offline & Private",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDeepCharcoal
                        )
                    }

                    Text(
                        text = "All your financial transactions, accounts, and wishlist items are saved solely on your local device. No telemetry, cloud sync, or external analytics.",
                        fontSize = 13.sp,
                        color = TextMediumGray
                    )

                    Button(
                        onClick = {
                            onExportJson(context) { msg ->
                                exportStatus = msg
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("export_data_button")
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export All Data as JSON")
                    }

                    exportStatus?.let { status ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp)),
                            color = IncomeGreen.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = status,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = IncomeGreen,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }
        }

        // App Information
        item {
            QuietCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = TextMediumGray,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "About Hisaab",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDeepCharcoal
                        )
                    }
                    Text(
                        text = "Hisaab do Farmanullah Ansari v1.0.0",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMediumGray
                    )
                    Text(
                        text = "Deterministic, 64-bit integer money engine in Pakistani Rupees (PKR). Exact calculations with zero drift.",
                        fontSize = 12.sp,
                        color = TextMediumGray
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(70.dp))
        }
    }

    // Transfer Funds Dialog
    if (showTransferDialog) {
        TransferFundsDialog(
            accounts = state.accounts,
            onDismiss = { showTransferDialog = false },
            onTransfer = { amountPaisa, fromId, toId, note ->
                onTransfer(amountPaisa, fromId, toId, note, System.currentTimeMillis()) {
                    showTransferDialog = false
                }
            }
        )
    }

    // Edit Opening Balance Dialog
    accountToEditOpening?.let { account ->
        EditOpeningBalanceDialog(
            account = account,
            onDismiss = { accountToEditOpening = null },
            onSave = { newPaisa ->
                onUpdateOpeningBalance(account, newPaisa)
                accountToEditOpening = null
            }
        )
    }
}

@Composable
fun TransferFundsDialog(
    accounts: List<AccountEntity>,
    onDismiss: () -> Unit,
    onTransfer: (amountPaisa: Long, fromId: Long, toId: Long, note: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var fromAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: 0L) }
    var toAccountId by remember {
        mutableStateOf(accounts.getOrNull(1)?.id ?: accounts.firstOrNull()?.id ?: 0L)
    }
    var note by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Transfer Funds",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextDeepCharcoal
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        errorMessage = null
                    },
                    label = { Text("Transfer Amount in PKR *") },
                    placeholder = { Text("e.g. 5000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transfer_amount_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CoralPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                // From Account Selector
                Text(text = "From Account", fontSize = 12.sp, color = TextMediumGray)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    accounts.forEach { acc ->
                        val isSelected = acc.id == fromAccountId
                        OutlinedButton(
                            onClick = {
                                fromAccountId = acc.id
                                if (toAccountId == acc.id) {
                                    toAccountId = accounts.firstOrNull { it.id != acc.id }?.id ?: 0L
                                }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) CoralPrimary else Color.Transparent,
                                contentColor = if (isSelected) Color.White else TextDeepCharcoal
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(acc.name, fontSize = 12.sp)
                        }
                    }
                }

                // To Account Selector
                Text(text = "To Account", fontSize = 12.sp, color = TextMediumGray)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    accounts.filter { it.id != fromAccountId }.forEach { acc ->
                        val isSelected = acc.id == toAccountId
                        OutlinedButton(
                            onClick = { toAccountId = acc.id },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) TransferBlue else Color.Transparent,
                                contentColor = if (isSelected) Color.White else TextDeepCharcoal
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(acc.name, fontSize = 12.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (Optional)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transfer_note_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CoralPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                errorMessage?.let { err ->
                    Text(text = err, fontSize = 12.sp, color = CoralPrimary)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = Money.parsePaisa(amountText)
                    if (parsed == null || parsed <= 0) {
                        errorMessage = "Please enter a valid amount."
                    } else if (fromAccountId == toAccountId) {
                        errorMessage = "Source and destination must be different."
                    } else {
                        onTransfer(parsed, fromAccountId, toAccountId, note.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("confirm_transfer_button")
            ) {
                Text("Transfer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMediumGray)
            }
        }
    )
}

@Composable
fun EditOpeningBalanceDialog(
    account: AccountEntity,
    onDismiss: () -> Unit,
    onSave: (Long) -> Unit
) {
    var amountText by remember {
        mutableStateOf(if (account.openingBalancePaisa > 0) (account.openingBalancePaisa / 100).toString() else "")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Adjust Opening Balance",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextDeepCharcoal
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Set starting funds for ${account.name}. This is not counted as monthly income.",
                    fontSize = 13.sp,
                    color = TextMediumGray
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Opening Balance in PKR") },
                    placeholder = { Text("e.g. 50000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("opening_balance_input"),
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
                    val parsed = Money.parsePaisa(amountText) ?: 0L
                    onSave(parsed)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMediumGray)
            }
        }
    )
}

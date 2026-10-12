package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.AccountsAndExportScreen
import com.example.ui.screens.AddTransactionScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.WhatINeedScreen
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextDeepCharcoal
import com.example.ui.theme.TextMediumGray
import com.example.ui.theme.WarmCreamBackground
import com.example.ui.viewmodel.HisaabViewModel
import kotlinx.coroutines.launch

enum class ScreenTab(val title: String, val icon: ImageVector) {
    HISAAB("Hisaab", Icons.Default.ReceiptLong),
    WHAT_I_NEED("What I Need", Icons.Default.ShoppingBag),
    ACCOUNTS("Accounts", Icons.Default.AccountBalance)
}

class MainActivity : ComponentActivity() {

    private val viewModel: HisaabViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                HisaabApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun HisaabApp(viewModel: HisaabViewModel) {
    val homeState by viewModel.homeState.collectAsStateWithLifecycle()
    val needsState by viewModel.needsState.collectAsStateWithLifecycle()
    val snackbarMsg by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(ScreenTab.HISAAB) }
    var showAddTransactionSheet by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Handle back button on sub-tabs
    if (currentTab != ScreenTab.HISAAB) {
        BackHandler {
            currentTab = ScreenTab.HISAAB
        }
    }

    // Snackbar listener with Undo action support
    LaunchedEffect(snackbarMsg) {
        snackbarMsg?.let { msg ->
            scope.launch {
                val hasUndo = viewModel.lastDeletedTransaction != null || viewModel.lastDeletedNeed != null
                val result = snackbarHostState.showSnackbar(
                    message = msg,
                    actionLabel = if (hasUndo) "Undo" else null,
                    duration = SnackbarDuration.Short
                )
                if (result == SnackbarResult.ActionPerformed) {
                    if (viewModel.lastDeletedTransaction != null) {
                        viewModel.undoDeleteTransaction()
                    } else if (viewModel.lastDeletedNeed != null) {
                        viewModel.undoDeleteNeed()
                    }
                }
                viewModel.clearSnackbar()
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.navigationBarsPadding(),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                ScreenTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title
                            )
                        },
                        label = { Text(tab.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CoralPrimary,
                            selectedTextColor = CoralPrimary,
                            indicatorColor = CoralPrimary.copy(alpha = 0.15f),
                            unselectedIconColor = TextMediumGray,
                            unselectedTextColor = TextMediumGray
                        ),
                        modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                    )
                }
            }
        },
        floatingActionButton = {
            if (currentTab == ScreenTab.HISAAB) {
                FloatingActionButton(
                    onClick = { showAddTransactionSheet = true },
                    containerColor = CoralPrimary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_transaction_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Transaction")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(WarmCreamBackground)
        ) {
            when (currentTab) {
                ScreenTab.HISAAB -> {
                    HomeScreen(
                        state = homeState,
                        onDeleteTransaction = { txnId ->
                            viewModel.deleteTransactionWithUndo(txnId)
                        },
                        onEditTransaction = { /* quick tap preview or edit */ }
                    )
                }

                ScreenTab.WHAT_I_NEED -> {
                    WhatINeedScreen(
                        state = needsState,
                        onAddNeed = { name, pricePaisa, where, note ->
                            viewModel.addNeed(name, pricePaisa, where, note)
                        },
                        onToggleDone = { id, isDone ->
                            viewModel.toggleNeedDone(id, isDone)
                        },
                        onDeleteNeed = { need ->
                            viewModel.deleteNeedWithUndo(need)
                        },
                        onClearDone = {
                            viewModel.clearAllDoneNeeds()
                        }
                    )
                }

                ScreenTab.ACCOUNTS -> {
                    AccountsAndExportScreen(
                        state = homeState,
                        onTransfer = { amountPaisa, fromId, toId, note, date, onSuccess ->
                            viewModel.recordTransfer(amountPaisa, fromId, toId, note, date, onSuccess)
                        },
                        onUpdateOpeningBalance = { account, newPaisa ->
                            viewModel.updateOpeningBalance(account, newPaisa)
                        },
                        onExportJson = { context, onResult ->
                            viewModel.exportDataAsJson(context, onResult)
                        }
                    )
                }
            }
        }
    }

    // Modal Add Transaction Screen / Sheet
    if (showAddTransactionSheet) {
        AddTransactionScreen(
            accounts = homeState.accounts,
            categories = homeState.categories,
            onDismiss = { showAddTransactionSheet = false },
            onSaveExpense = { amountPaisa, fromAccountId, categoryId, note, date, receiptUri ->
                viewModel.recordExpense(amountPaisa, fromAccountId, categoryId, note, date, receiptUri) { msg ->
                    scope.launch { snackbarHostState.showSnackbar(msg) }
                }
            },
            onSaveIncome = { amountPaisa, toAccountId, categoryId, note, date ->
                viewModel.recordIncome(amountPaisa, toAccountId, categoryId, note, date) { msg ->
                    scope.launch { snackbarHostState.showSnackbar(msg) }
                }
            },
            onSaveTransfer = { amountPaisa, fromId, toId, note, date ->
                viewModel.recordTransfer(amountPaisa, fromId, toId, note, date) { msg ->
                    scope.launch { snackbarHostState.showSnackbar(msg) }
                }
            }
        )
    }
}

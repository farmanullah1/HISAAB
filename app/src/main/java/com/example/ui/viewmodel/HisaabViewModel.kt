package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.entity.AccountEntity
import com.example.data.entity.CategoryEntity
import com.example.data.entity.NeedEntity
import com.example.data.entity.TransactionEntity
import com.example.data.repository.HisaabRepository
import com.example.model.AccountType
import com.example.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.Calendar

data class HomeUiState(
    val totalBalancePaisa: Long = 0L,
    val cashBalancePaisa: Long = 0L,
    val bankBalancePaisa: Long = 0L,
    val thisMonthIncomePaisa: Long = 0L,
    val thisMonthExpensePaisa: Long = 0L,
    val transactions: List<TransactionEntity> = emptyList(),
    val accounts: List<AccountEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val accountBalances: Map<Long, Long> = emptyMap()
)

data class NeedsUiState(
    val activeNeeds: List<NeedEntity> = emptyList(),
    val doneNeeds: List<NeedEntity> = emptyList(),
    val totalExpectedPricePaisa: Long = 0L,
    val activeCount: Int = 0
)

class HisaabViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = HisaabRepository(db)

    // Last deleted items for 5-second Undo support
    var lastDeletedTransaction: TransactionEntity? = null
        private set
    var lastDeletedNeed: NeedEntity? = null
        private set

    val snackbarMessage = MutableStateFlow<String?>(null)

    // Home UI State combining accounts, transactions, and categories with derived balances
    val homeState: StateFlow<HomeUiState> = combine(
        repository.accounts,
        repository.transactions,
        repository.categories
    ) { accounts, txns, categories ->
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH)
        val currentYear = cal.get(Calendar.YEAR)

        // Calculate derived balance for each account:
        // balance = openingBalance + inflows - outflows
        val balancesMap = accounts.associate { account ->
            val inflows = txns.filter { it.toAccountId == account.id }.sumOf { it.amountPaisa }
            val outflows = txns.filter { it.fromAccountId == account.id }.sumOf { it.amountPaisa }
            account.id to (account.openingBalancePaisa + inflows - outflows)
        }

        val totalBalance = balancesMap.values.sum()

        val cashAcc = accounts.find { it.type == AccountType.CASH }
        val bankAcc = accounts.find { it.type == AccountType.BANK }

        val cashBal = cashAcc?.let { balancesMap[it.id] } ?: 0L
        val bankBal = bankAcc?.let { balancesMap[it.id] } ?: 0L

        // This Month totals
        val currentMonthTxns = txns.filter { txn ->
            val txnCal = Calendar.getInstance().apply { timeInMillis = txn.dateEpochMs }
            txnCal.get(Calendar.MONTH) == currentMonth && txnCal.get(Calendar.YEAR) == currentYear
        }

        val monthIncome = currentMonthTxns.filter { it.type == TransactionType.INCOME }.sumOf { it.amountPaisa }
        val monthExpense = currentMonthTxns.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountPaisa }

        HomeUiState(
            totalBalancePaisa = totalBalance,
            cashBalancePaisa = cashBal,
            bankBalancePaisa = bankBal,
            thisMonthIncomePaisa = monthIncome,
            thisMonthExpensePaisa = monthExpense,
            transactions = txns,
            accounts = accounts,
            categories = categories,
            accountBalances = balancesMap
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    // Needs UI State
    val needsState: StateFlow<NeedsUiState> = repository.needs.map { allNeeds ->
        val active = allNeeds.filter { !it.isDone }
        val done = allNeeds.filter { it.isDone }
        val totalExpected = active.mapNotNull { it.expectedPricePaisa }.sum()

        NeedsUiState(
            activeNeeds = active,
            doneNeeds = done,
            totalExpectedPricePaisa = totalExpected,
            activeCount = active.size
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NeedsUiState()
    )

    // Flow of all accounts for pickers
    val accounts: StateFlow<List<AccountEntity>> = repository.accounts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Flow of all categories
    val categories: StateFlow<List<CategoryEntity>> = repository.categories.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // ==========================================
    // Transaction Actions
    // ==========================================

    fun recordExpense(
        amountPaisa: Long,
        fromAccountId: Long,
        categoryId: Long,
        note: String,
        date: Long,
        receiptUri: String? = null,
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            repository.addExpense(amountPaisa, fromAccountId, categoryId, note, date, receiptUri)
            val accName = homeState.value.accounts.find { it.id == fromAccountId }?.name ?: "Account"
            onSuccess("Expense recorded from $accName")
        }
    }

    fun recordIncome(
        amountPaisa: Long,
        toAccountId: Long,
        categoryId: Long,
        note: String,
        date: Long,
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            repository.addIncome(amountPaisa, toAccountId, categoryId, note, date)
            val accName = homeState.value.accounts.find { it.id == toAccountId }?.name ?: "Account"
            onSuccess("Income recorded into $accName")
        }
    }

    fun recordTransfer(
        amountPaisa: Long,
        fromId: Long,
        toId: Long,
        note: String,
        date: Long,
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            repository.addTransfer(amountPaisa, fromId, toId, note, date)
            val fromName = homeState.value.accounts.find { it.id == fromId }?.name ?: "From"
            val toName = homeState.value.accounts.find { it.id == toId }?.name ?: "To"
            onSuccess("Transferred from $fromName to $toName")
        }
    }

    fun deleteTransactionWithUndo(id: Long) {
        viewModelScope.launch {
            val txn = repository.getTransactionById(id)
            if (txn != null) {
                lastDeletedTransaction = txn
                repository.deleteTransaction(id)
                snackbarMessage.value = "Deleted"
            }
        }
    }

    fun undoDeleteTransaction() {
        viewModelScope.launch {
            lastDeletedTransaction?.let { txn ->
                repository.restoreTransaction(txn)
                lastDeletedTransaction = null
                snackbarMessage.value = "Restored"
            }
        }
    }

    fun updateTransaction(txn: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(txn)
        }
    }

    // ==========================================
    // What I Need Actions
    // ==========================================

    fun addNeed(
        name: String,
        expectedPricePaisa: Long?,
        whereToGet: String,
        note: String
    ) {
        viewModelScope.launch {
            repository.addNeed(name, expectedPricePaisa, whereToGet, note)
        }
    }

    fun toggleNeedDone(id: Long, isDone: Boolean) {
        viewModelScope.launch {
            repository.toggleNeedDone(id, isDone)
        }
    }

    fun updateNeed(need: NeedEntity) {
        viewModelScope.launch {
            repository.updateNeed(need)
        }
    }

    fun deleteNeedWithUndo(need: NeedEntity) {
        viewModelScope.launch {
            lastDeletedNeed = need
            repository.deleteNeed(need.id)
            snackbarMessage.value = "Item deleted"
        }
    }

    fun undoDeleteNeed() {
        viewModelScope.launch {
            lastDeletedNeed?.let { need ->
                repository.restoreNeed(need)
                lastDeletedNeed = null
                snackbarMessage.value = "Item restored"
            }
        }
    }

    fun clearAllDoneNeeds() {
        viewModelScope.launch {
            repository.clearDoneNeeds()
        }
    }

    // ==========================================
    // Settings Actions
    // ==========================================

    fun updateAccount(account: AccountEntity) {
        viewModelScope.launch {
            repository.updateAccount(account)
        }
    }

    fun updateOpeningBalance(account: AccountEntity, newOpeningPaisa: Long) {
        viewModelScope.launch {
            val updated = account.copy(openingBalancePaisa = newOpeningPaisa)
            repository.updateAccount(updated)
            snackbarMessage.value = "Opening balance updated for ${account.name}"
        }
    }

    fun updateCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.updateCategory(category)
        }
    }

    fun exportDataAsJson(context: Context, onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val accounts = homeState.value.accounts
                val txns = homeState.value.transactions
                val categories = homeState.value.categories
                val needs = needsState.value.activeNeeds + needsState.value.doneNeeds

                val root = JSONObject().apply {
                    put("app", "Hisaab")
                    put("exportedAt", System.currentTimeMillis())

                    val accArr = JSONArray()
                    accounts.forEach { acc ->
                        accArr.put(JSONObject().apply {
                            put("id", acc.id)
                            put("name", acc.name)
                            put("type", acc.type.name)
                            put("openingBalancePaisa", acc.openingBalancePaisa)
                            put("colorHex", acc.colorHex)
                        })
                    }
                    put("accounts", accArr)

                    val txnArr = JSONArray()
                    txns.forEach { t ->
                        txnArr.put(JSONObject().apply {
                            put("id", t.id)
                            put("type", t.type.name)
                            put("amountPaisa", t.amountPaisa)
                            put("fromAccountId", t.fromAccountId)
                            put("toAccountId", t.toAccountId)
                            put("categoryId", t.categoryId)
                            put("note", t.note)
                            put("dateEpochMs", t.dateEpochMs)
                        })
                    }
                    put("transactions", txnArr)

                    val needsArr = JSONArray()
                    needs.forEach { n ->
                        needsArr.put(JSONObject().apply {
                            put("name", n.name)
                            put("expectedPricePaisa", n.expectedPricePaisa)
                            put("whereToGet", n.whereToGet)
                            put("isDone", n.isDone)
                        })
                    }
                    put("needs", needsArr)
                }

                val exportFile = File(context.filesDir, "hisaab-export.json")
                exportFile.writeText(root.toString(2))
                onResult("Data exported to hisaab-export.json (${exportFile.length()} bytes)")
            } catch (e: Exception) {
                onResult("Export failed: ${e.message}")
            }
        }
    }

    fun clearSnackbar() {
        snackbarMessage.value = null
    }
}

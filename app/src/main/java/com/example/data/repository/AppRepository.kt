package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.AppDatabase
import com.example.data.entity.AccountEntity
import com.example.data.entity.CategoryEntity
import com.example.data.entity.NeedEntity
import com.example.data.entity.TransactionEntity
import com.example.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/**
 * Authoritative financial repository for Hisaab.
 * Derived balance calculations prevent balance drift.
 * All monetary amounts are handled as 64-bit Long paisa.
 */
class HisaabRepository(private val db: AppDatabase) {

    private val accountDao = db.accountDao()
    private val transactionDao = db.transactionDao()
    private val categoryDao = db.categoryDao()
    private val needDao = db.needDao()

    val accounts: Flow<List<AccountEntity>> = accountDao.getAllActiveAccounts()
    val allAccounts: Flow<List<AccountEntity>> = accountDao.getAllAccounts()
    val categories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    val transactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val needs: Flow<List<NeedEntity>> = needDao.getAllNeeds()
    val activeNeeds: Flow<List<NeedEntity>> = needDao.getActiveNeeds()
    val doneNeeds: Flow<List<NeedEntity>> = needDao.getDoneNeeds()

    // Derived balances — computed from opening balance and transactions
    fun balanceOf(accountId: Long): Flow<Long> {
        return combine(allAccounts, transactions) { accList, txnList ->
            val account = accList.find { it.id == accountId } ?: return@combine 0L
            val inflows = txnList.filter { it.toAccountId == accountId }.sumOf { it.amountPaisa }
            val outflows = txnList.filter { it.fromAccountId == accountId }.sumOf { it.amountPaisa }
            account.openingBalancePaisa + inflows - outflows
        }
    }

    fun totalBalance(): Flow<Long> {
        return combine(accounts, transactions) { accList, txnList ->
            val openingSum = accList.sumOf { it.openingBalancePaisa }
            val incomeSum = txnList.filter { it.type == TransactionType.INCOME }.sumOf { it.amountPaisa }
            val expenseSum = txnList.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountPaisa }
            openingSum + incomeSum - expenseSum
        }
    }

    // ==========================================
    // Transaction Operations
    // ==========================================

    suspend fun addExpense(
        amountPaisa: Long,
        fromAccountId: Long,
        categoryId: Long,
        note: String,
        date: Long,
        receiptUri: String? = null
    ): Long {
        require(amountPaisa > 0) { "Expense amount must be greater than zero." }
        val txn = TransactionEntity(
            type = TransactionType.EXPENSE,
            amountPaisa = amountPaisa,
            fromAccountId = fromAccountId,
            toAccountId = null,
            categoryId = categoryId,
            note = note.trim(),
            receiptUri = receiptUri,
            dateEpochMs = date
        )
        return transactionDao.insertTransaction(txn)
    }

    suspend fun addIncome(
        amountPaisa: Long,
        toAccountId: Long,
        categoryId: Long,
        note: String,
        date: Long
    ): Long {
        require(amountPaisa > 0) { "Income amount must be greater than zero." }
        val txn = TransactionEntity(
            type = TransactionType.INCOME,
            amountPaisa = amountPaisa,
            fromAccountId = null,
            toAccountId = toAccountId,
            categoryId = categoryId,
            note = note.trim(),
            dateEpochMs = date
        )
        return transactionDao.insertTransaction(txn)
    }

    suspend fun addTransfer(
        amountPaisa: Long,
        fromId: Long,
        toId: Long,
        note: String,
        date: Long
    ): Long {
        require(fromId != toId) { "Source and destination accounts must be different." }
        require(amountPaisa > 0) { "Transfer amount must be greater than zero." }
        val txn = TransactionEntity(
            type = TransactionType.TRANSFER,
            amountPaisa = amountPaisa,
            fromAccountId = fromId,
            toAccountId = toId,
            categoryId = null,
            note = note.trim(),
            dateEpochMs = date
        )
        return transactionDao.insertTransaction(txn)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(id: Long) {
        transactionDao.deleteTransaction(id)
    }

    suspend fun getTransactionById(id: Long): TransactionEntity? {
        return transactionDao.getTransactionById(id)
    }

    suspend fun restoreTransaction(transaction: TransactionEntity): Long {
        return transactionDao.insertTransaction(transaction)
    }

    // ==========================================
    // Account Operations
    // ==========================================

    suspend fun addAccount(account: AccountEntity): Long {
        return accountDao.insertAccount(account)
    }

    suspend fun updateAccount(account: AccountEntity) {
        accountDao.updateAccount(account)
    }

    suspend fun archiveAccount(id: Long) {
        accountDao.archiveAccount(id)
    }

    suspend fun deleteAccount(id: Long) {
        accountDao.deleteAccount(id)
    }

    // ==========================================
    // Category Operations
    // ==========================================

    suspend fun updateCategory(category: CategoryEntity) {
        categoryDao.updateCategory(category)
    }

    // ==========================================
    // What I Need (Wishlist / Shopping Planner)
    // ==========================================

    suspend fun addNeed(
        name: String,
        pricePaisa: Long?,
        where: String,
        note: String
    ): Long {
        require(name.isNotBlank()) { "Item name cannot be empty." }
        val need = NeedEntity(
            name = name.trim(),
            expectedPricePaisa = pricePaisa,
            whereToGet = where.trim(),
            note = note.trim(),
            isDone = false,
            createdAtEpochMs = System.currentTimeMillis()
        )
        return needDao.insertNeed(need)
    }

    suspend fun toggleNeedDone(id: Long, done: Boolean) {
        val doneAtMs = if (done) System.currentTimeMillis() else null
        needDao.setNeedDone(id, done, doneAtMs)
    }

    suspend fun updateNeed(need: NeedEntity) {
        needDao.updateNeed(need)
    }

    suspend fun deleteNeed(id: Long) {
        needDao.deleteNeed(id)
    }

    suspend fun clearDoneNeeds() {
        needDao.clearAllDone()
    }

    suspend fun restoreNeed(need: NeedEntity): Long {
        return needDao.insertNeed(need)
    }

    fun getRawDatabase(): AppDatabase = db
}

typealias AppRepository = HisaabRepository

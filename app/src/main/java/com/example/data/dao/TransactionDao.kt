package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY dateEpochMs DESC, id DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE fromAccountId = :accountId OR toAccountId = :accountId ORDER BY dateEpochMs DESC, id DESC")
    fun getTransactionsForAccount(accountId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE dateEpochMs BETWEEN :startMs AND :endMs ORDER BY dateEpochMs DESC, id DESC")
    fun getTransactionsBetween(startMs: Long, endMs: Long): Flow<List<TransactionEntity>>

    @Query("SELECT COALESCE(SUM(amountPaisa), 0) FROM transactions WHERE toAccountId = :accountId")
    fun getInflowForAccount(accountId: Long): Flow<Long>

    @Query("SELECT COALESCE(SUM(amountPaisa), 0) FROM transactions WHERE fromAccountId = :accountId")
    fun getOutflowForAccount(accountId: Long): Flow<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransaction(id: Long)

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun getTransactionCount(): Int
}

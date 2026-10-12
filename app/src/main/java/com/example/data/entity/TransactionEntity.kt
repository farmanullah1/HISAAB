package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.model.TransactionType

@Entity(
    tableName = "transactions",
    indices = [
        Index("fromAccountId"),
        Index("toAccountId"),
        Index("categoryId"),
        Index("dateEpochMs")
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: TransactionType,
    val amountPaisa: Long,
    val fromAccountId: Long? = null,
    val toAccountId: Long? = null,
    val categoryId: Long? = null,
    val note: String = "",
    val dateEpochMs: Long,
    val createdAtEpochMs: Long = System.currentTimeMillis()
)

package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.AccountType

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: AccountType,
    val openingBalancePaisa: Long = 0L,
    val colorHex: String = "#7C9A6B",
    val isArchived: Boolean = false,
    val createdAtEpochMs: Long = System.currentTimeMillis()
)

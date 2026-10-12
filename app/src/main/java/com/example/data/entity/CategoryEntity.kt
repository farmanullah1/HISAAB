package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.TransactionType

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: TransactionType,
    val iconKey: String,
    val colorHex: String,
    val monthlyLimitPaisa: Long? = null
)

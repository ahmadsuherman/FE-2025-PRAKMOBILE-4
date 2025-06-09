package com.example.pasundanebudget.data.local.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val categoryId: Int,
    val type: String, // "in" atau "out"
    val amount: Double,
    val date: Long,
    val categoryName: String? = "",
)

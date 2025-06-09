package com.example.pasundanebudget.data.remote.models

data class TransactionResponse(
    val id: Int,
    val user_id: Int,
    val category: CategoryResponse,
    val type: String,       // "in" atau "out"
    val amount: Double,
    val date: String,
    val categoryName: String
)

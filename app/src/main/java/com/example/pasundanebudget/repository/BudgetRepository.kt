package com.example.pasundanebudget.repository

import android.util.Log
import com.example.pasundanebudget.data.local.db.dao.CategoryDao
import com.example.pasundanebudget.data.local.db.dao.TransactionDao
import com.example.pasundanebudget.data.local.db.entities.CategoryEntity
import com.example.pasundanebudget.data.local.db.entities.TransactionEntity
import com.example.pasundanebudget.data.remote.api.ApiService
import com.example.pasundanebudget.data.remote.models.CategoryResponse
import com.example.pasundanebudget.data.remote.models.TransactionResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.*

class BudgetRepository @Inject constructor(
    private val apiService: ApiService,
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao
) {

    // Local data streams
    fun getCategories(userId: Int): Flow<List<CategoryEntity>> = categoryDao.getCategories(userId)

    fun getTransactions(userId: Int): Flow<List<TransactionEntity>> = transactionDao.getTransactions(userId)

    // Sync categories from API to local DB
    suspend fun syncCategories(apiToken: String, userId: Int) {
        val bearerToken = "Bearer $apiToken"
        val response = apiService.getCategories(bearerToken)

        if (response.isSuccessful) {
            response.body()?.let { list ->
                val entities = list.map { mapCategoryResponseToEntity(it, userId) }
                // Insert/update local DB
                entities.forEach { categoryDao.insert(it) }
            }
        }
    }

    // Add category locally and remotely
    suspend fun addCategory(apiToken: String, userId: Int, name: String) {
        val bearerToken = "Bearer $apiToken"
        val response = apiService.addCategory(bearerToken, mapOf("name" to name))

        if (response.isSuccessful) {
            response.body()?.let {
                val entity = mapCategoryResponseToEntity(it, userId)
                categoryDao.insert(entity)
            }
        } else {
            throw Exception("Add category failed: ${response.message()}")
        }
    }

    suspend fun updateCategory(apiToken: String, userId: Int, id: Int, newName: String) {
        val bearerToken = "Bearer $apiToken"
        val body = mapOf(
            "_method" to "PUT",
            "name" to newName
        )
        val response = apiService.updateCategory(bearerToken, id, body)

        if (response.isSuccessful) {
            response.body()?.let {
                categoryDao.insert(mapCategoryResponseToEntity(it, userId))
            }
        } else {
            throw Exception("Update failed: ${response.message()}")
        }
    }

    suspend fun deleteCategory(apiToken: String, userId: Int, id: Int) {
        val bearerToken = "Bearer $apiToken"
        val body = mapOf(
            "_method" to "DELETE",
        )
        val response = apiService.deleteCategory(bearerToken, id, body)
        if (response.isSuccessful) {
            // Hapus dari DB lokal
            categoryDao.getCategories(userId).firstOrNull()?.find { it.id == id }?.let {
                categoryDao.delete(it)
            }
        } else {
            throw Exception("Delete failed: ${response.message()}")
        }
    }

    // Sync transactions from API to local DB
    suspend fun syncTransactions(apiToken: String, userId: Int) {
        val bearerToken = "Bearer $apiToken"
        val response = apiService.getTransactions(bearerToken)

        if (response.isSuccessful) {
            response.body()?.let { list ->
                val entities = list.map { mapTransactionResponseToEntity(it, userId) }
                entities.forEach { transactionDao.insert(it) }
            }
        }
    }

    // Add transaction locally and remotely
    suspend fun addTransaction(
        apiToken: String,
        userId: Int,
        categoryId: Int,
        type: String,
        amount: Double,
        formattedDate: String
    ) {
        val body = mapOf(
            "category_id" to categoryId,
            "type" to type,
            "amount" to amount,
            "date" to formattedDate
        )
        val bearerToken = "Bearer $apiToken"
        val response = apiService.addTransaction(bearerToken, body)

        if (response.isSuccessful) {
            response.body()?.let {
                val entity = mapTransactionResponseToEntity(it, userId)
                transactionDao.insert(entity)
            }
        } else {
            throw Exception("Create failed: ${response.message()}")
        }
    }

    suspend fun updateTransaction(apiToken: String, id: Int, body: Map<String, Any>) {
        val bearerToken = "Bearer $apiToken"
        val response = apiService.updateTransaction(bearerToken, id, body)

        val errorBody = response.errorBody()?.string()
        Log.e("API Error UPDATE", errorBody ?: "No error body")

        if (!response.isSuccessful) {
            throw Exception("Update failed: ${response.message()}")
        }
        response.body()?.let {
            transactionDao.insert(mapTransactionResponseToEntity(it, 0))
        }
    }

    suspend fun deleteTransaction(apiToken: String, userId: Int, id: Int) {
        val bearerToken = "Bearer $apiToken"
        val body = mapOf(
            "_method" to "DELETE",
        )
        val response = apiService.deleteTransaction(bearerToken, id, body)
        if (response.isSuccessful) {
            transactionDao.getTransactions(userId).firstOrNull()?.find { it.id == id }?.let {
                transactionDao.delete(it)
            }
        } else {
            throw Exception("Delete failed: ${response.message()}")
        }
    }

    // Mapping helpers

    private fun mapCategoryResponseToEntity(response: CategoryResponse, userId: Int) =
        CategoryEntity(
            id = response.id,
            userId = userId,
            name = response.name
        )

    private fun mapTransactionResponseToEntity(response: TransactionResponse, userId: Int) =
        TransactionEntity(
            id = response.id,
            userId = userId,
            categoryId = response.category?.id ?: 0,  // kalau null kasih default 0
            type = response.type,
            amount = response.amount,
            date = java.text.SimpleDateFormat("yyyy-MM-dd").parse(response.date)?.time ?: 0L,
            categoryName = response.category?.name ?: "",
        )

    suspend fun getSummary(userId: Int): Pair<Double, Double> {
        val incomes = transactionDao.getTransactions(userId)
            .first()
            .filter { it.type == "in" }
            .sumOf { it.amount }

        val expenses = transactionDao.getTransactions(userId)
            .first()
            .filter { it.type == "out" }
            .sumOf { it.amount }

        return Pair(incomes, expenses)
    }

    fun getWeeklySummary(userId: Int): Flow<List<Pair<String, Pair<Double, Double>>>> {
        // Output: List hari, Pair<pemasukan, pengeluaran>
        return transactionDao.getTransactions(userId).map { transactions ->
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val calendar = Calendar.getInstance()
            calendar.time = Date()

            // Ambil 7 hari terakhir
            val days = (0..6).map {
                calendar.add(Calendar.DATE, -it)
                val day = sdf.format(calendar.time)
                calendar.add(Calendar.DATE, it) // reset
                day
            }.reversed()

            days.map { day ->
                val dailyTransactions = transactions.filter { sdf.format(Date(it.date)) == day }
                val income = dailyTransactions.filter { it.type == "in" }.sumOf { it.amount }
                val expense = dailyTransactions.filter { it.type == "out" }.sumOf { it.amount }
                day to (income to expense)
            }
        }
    }

}
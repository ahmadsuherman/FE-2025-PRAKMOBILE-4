package com.pasundane_budget.data.remote.api

import com.pasundane_budget.data.remote.models.CategoryResponse
import com.pasundane_budget.data.remote.models.TransactionResponse
import com.pasundane_budget.data.remote.models.UserResponse
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // USER AUTHENTICATION

    @POST("register")
    suspend fun register(
        @Body body: Map<String, String>
    ): Response<UserResponse>

    @POST("login")
    suspend fun login(
        @Body body: Map<String, String>
    ): Response<UserResponse>

    // CATEGORIES

    @GET("categories")
    suspend fun getCategories(
        @Header("Authorization") token: String
    ): Response<List<CategoryResponse>>

    @POST("categories")
    suspend fun addCategory(
        @Header("Authorization") token: String,
        @Body body: Map<String, String>
    ): Response<CategoryResponse>

    @POST("categories/{id}")
    suspend fun updateCategory(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body body: Map<String, String>
    ): Response<CategoryResponse>

    @POST("categories/{id}")
    suspend fun deleteCategory(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body body: Map<String, String>
    ): Response<Unit>

    // TRANSACTIONS

    @GET("transactions")
    suspend fun getTransactions(
        @Header("Authorization") token: String
    ): Response<List<TransactionResponse>>

    @POST("transactions")
    suspend fun addTransaction(
        @Header("Authorization") token: String,
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Response<TransactionResponse>

    @POST("transactions/{id}")
    suspend fun updateTransaction(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Response<TransactionResponse>

    @POST("transactions/{id}")
    suspend fun deleteTransaction(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body body: Map<String, String>
    ): Response<Unit>
}

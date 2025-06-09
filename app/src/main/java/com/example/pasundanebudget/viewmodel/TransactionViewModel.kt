package com.example.pasundanebudget.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pasundanebudget.data.local.db.entities.TransactionEntity
import com.example.pasundanebudget.data.local.preferences.UserPreferences
import com.example.pasundanebudget.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class TransactionUiState(
    val transactions: List<TransactionEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val repository: BudgetRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionUiState(isLoading = true))
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()

    fun loadTransactions(userId: Int) {
        viewModelScope.launch {
            repository.getTransactions(userId)
                .catch { e ->
                    _uiState.value = TransactionUiState(error = e.localizedMessage, isLoading = false)
                }
                .collect { list ->
                    _uiState.value = TransactionUiState(transactions = list, isLoading = false)
                }
        }
    }

    fun syncAndLoadTransactions() {
        viewModelScope.launch {
            try {
                val apiToken = userPreferences.apiTokenFlow.first()
                val userId = userPreferences.userIdFlow.first()

                if (apiToken.isNotEmpty() && userId != -1) {
                    repository.syncTransactions(apiToken, userId)
                    loadTransactions(userId)
                } else {
                    _uiState.value = TransactionUiState(error = "Token or UserId not found", isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = TransactionUiState(error = e.localizedMessage, isLoading = false)
            }
        }
    }

    fun addTransaction(
        apiToken: String,
        userId: Int,
        categoryId: Int,
        type: String,
        amount: Double,
        date: Long
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(date))
            try {
                if (apiToken.isNotEmpty() && userId != -1) {
                    repository.addTransaction(apiToken, userId, categoryId, type, amount, formattedDate)

                    repository.syncTransactions(apiToken, userId)
                    loadTransactions(userId)
                } else {
                    _uiState.value = TransactionUiState(error = "Token or UserId not found", isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = TransactionUiState(error = e.localizedMessage, isLoading = false)
            }
        }
    }

    fun updateTransaction(
        apiToken: String,
        userId: Int,
        id: Int,
        categoryId: Int,
        type: String,
        amount: Double,
        date: Long
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                if (apiToken.isNotEmpty() && userId != -1) {
                    val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(date))
                    repository.updateTransaction(apiToken, id, mapOf(
                        "category_id" to categoryId,
                        "type" to type,
                        "amount" to amount,
                        "date" to formattedDate,
                        "_method" to "PUT" // Jika backend Laravel gunakan metode ini untuk update via POST
                    ))
                    repository.syncTransactions(apiToken, userId)
                    loadTransactions(userId)
                } else {
                    _uiState.value = TransactionUiState(error = "Token or UserId not found", isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = TransactionUiState(error = e.localizedMessage, isLoading = false)
            }
        }
    }

    fun deleteTransaction(apiToken: String, userId: Int, id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                repository.deleteTransaction(apiToken, userId, id)
                repository.syncTransactions(apiToken, userId)
                loadTransactions(userId)
            } catch (e: Exception) {
                _uiState.value = TransactionUiState(error = e.localizedMessage, isLoading = false)
            }
        }
    }
}

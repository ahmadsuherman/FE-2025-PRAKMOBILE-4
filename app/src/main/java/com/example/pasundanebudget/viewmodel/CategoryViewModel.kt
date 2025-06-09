package com.example.pasundanebudget.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pasundanebudget.data.local.db.entities.CategoryEntity
import com.example.pasundanebudget.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.pasundanebudget.data.local.preferences.UserPreferences

data class CategoryUiState(
    val categories: List<CategoryEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val repository: BudgetRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryUiState(isLoading = true))
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val userId = userPreferences.userIdFlow.firstOrNull() ?: -1
            loadCategories(userId)
        }
    }

    fun loadCategories(userId: Int) {
        viewModelScope.launch {
            repository.getCategories(userId)
                .catch { e ->
                    _uiState.value = CategoryUiState(error = e.localizedMessage, isLoading = false)
                }
                .collect { list ->
                    _uiState.value = CategoryUiState(categories = list, isLoading = false)
                }
        }
    }

    fun syncAndLoadCategories() {
        viewModelScope.launch {
            try {
                val apiToken = userPreferences.apiTokenFlow.first()
                val userId = userPreferences.userIdFlow.first()
                if (apiToken.isNotEmpty() && userId != -1) {
                    repository.syncCategories(apiToken, userId)
                    loadCategories(userId)
                } else {
                    _uiState.value = CategoryUiState(error = "Token or UserId not found", isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = CategoryUiState(error = e.localizedMessage, isLoading = false)
            }
        }
    }

    fun addCategory(apiToken: String, userId: Int, name: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                if (apiToken.isNotEmpty() && userId != -1) {
                    repository.addCategory(apiToken, userId, name)
                    repository.syncCategories(apiToken, userId)
                    loadCategories(userId)
                } else {
                    _uiState.value = CategoryUiState(error = "Token or UserId not found", isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = CategoryUiState(error = e.localizedMessage, isLoading = false)
            }
        }
    }

    fun updateCategory(apiToken: String, userId: Int, id: Int, newName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                if (apiToken.isNotEmpty() && userId != -1) {
                    repository.updateCategory(apiToken, userId, id, newName)
                    loadCategories(userId)
                } else {
                    _uiState.value = CategoryUiState(error = "Token or UserId not found", isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.localizedMessage, isLoading = false)
                loadCategories(userId)
            }
        }
    }

    fun deleteCategory(apiToken: String, userId: Int, id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                repository.deleteCategory(apiToken, userId, id)
                loadCategories(userId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.localizedMessage, isLoading = false)
                loadCategories(userId)
            }
        }
    }

}

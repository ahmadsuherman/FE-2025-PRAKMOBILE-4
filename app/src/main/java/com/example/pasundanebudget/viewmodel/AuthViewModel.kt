package com.pasundane_budget.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pasundane_budget.data.remote.models.UserResponse
import com.pasundane_budget.data.remote.api.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.pasundane_budget.data.local.preferences.UserPreferences
import com.pasundane_budget.data.local.TokenManager

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val user: UserResponse) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val apiService: ApiService,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _loginState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val loginState: StateFlow<AuthUiState> get() = _loginState

    private val _registerState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val registerState: StateFlow<AuthUiState> get() = _registerState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = AuthUiState.Loading
            try {
                val body = mapOf("email" to email, "password" to password)
                val response = apiService.login(body)
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!
                    _loginState.value = AuthUiState.Success(user)
                    // Save to DataStore
                    userPreferences.saveUser(user.apiToken, user.id)

                    TokenManager.token = user.apiToken
                } else {
                    _loginState.value = AuthUiState.Error("Login failed: ${response.message()}")
                }
            } catch (e: Exception) {
                _loginState.value = AuthUiState.Error("Login error: ${e.localizedMessage}")
            }
        }
    }

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _registerState.value = AuthUiState.Loading
            try {
                val body = mapOf("name" to name, "email" to email, "password" to password)
                val response = apiService.register(body)
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!
                    _registerState.value = AuthUiState.Success(user)
                    userPreferences.saveUser(user.apiToken, user.id)

                    TokenManager.token = user.apiToken
                } else {
                    _registerState.value = AuthUiState.Error("Register failed: ${response.message()}")
                }
            } catch (e: Exception) {
                _registerState.value = AuthUiState.Error("Register error: ${e.localizedMessage}")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            userPreferences.clearUser()
            TokenManager.token = null
        }
    }
}

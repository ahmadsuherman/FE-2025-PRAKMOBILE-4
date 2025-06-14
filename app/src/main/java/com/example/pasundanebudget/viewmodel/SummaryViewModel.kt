package com.pasundane_budget.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pasundane_budget.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.asStateFlow

data class SummaryUiState(
    val income: Double = 0.0,
    val expense: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SummaryViewModel @Inject constructor(
    private val repository: BudgetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SummaryUiState(isLoading = true))
    val uiState: StateFlow<SummaryUiState> = _uiState

    fun loadSummary(userId: Int) {
        viewModelScope.launch {
            _uiState.value = SummaryUiState(isLoading = true)
            try {
                val (income, expense) = repository.getSummary(userId)
                _uiState.value = SummaryUiState(income = income, expense = expense, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = SummaryUiState(error = e.localizedMessage, isLoading = false)
            }
        }
    }

    private val _chartData = MutableStateFlow<List<Pair<String, Pair<Double, Double>>>>(emptyList())
    val chartData = _chartData.asStateFlow()

    fun loadWeeklySummary(userId: Int) {
        viewModelScope.launch {
            repository.getWeeklySummary(userId)
                .collect {
                    _chartData.value = it
                }
        }
    }
}
package com.pasundane_budget.data.local.preferences

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class SessionData(
    val apiToken: String?,
    val userId: Int?
)

class SessionManager(private val userPreferences: UserPreferences) {
    val sessionData: Flow<SessionData> = combine(
        userPreferences.apiTokenFlow,
        userPreferences.userIdFlow
    ) { token, id ->
        SessionData(token, id)
    }
}

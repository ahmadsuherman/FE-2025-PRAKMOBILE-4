package com.pasundane_budget.data.local.preferences

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("user_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        val API_TOKEN = stringPreferencesKey("api_token")
        val USER_ID = intPreferencesKey("user_id")
    }

    val apiTokenFlow: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[API_TOKEN] ?: "" }

    val userIdFlow: Flow<Int> = context.dataStore.data
        .map { prefs -> prefs[USER_ID] ?: -1 }

    suspend fun saveUser(apiToken: String, userId: Int) {
        context.dataStore.edit { prefs ->
            prefs[API_TOKEN] = apiToken
            prefs[USER_ID] = userId
        }
    }

    suspend fun clearUser() {
        context.dataStore.edit { prefs ->
            prefs.remove(API_TOKEN)
            prefs.remove(USER_ID)
        }
    }
}

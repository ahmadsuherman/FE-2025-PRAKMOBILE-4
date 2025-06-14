package com.example.pasundanebudget

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.pasundane_budget.data.local.preferences.UserPreferences
import com.pasundane_budget.ui.navigation.NavGraph
import com.pasundane_budget.ui.navigation.Screen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i("Start", "Hello Pasundan E Budget!")
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            val apiToken by userPreferences.apiTokenFlow.collectAsState(initial = null)

            if (apiToken == null) {
                // DataStore masih baca token, tampilkan loading
                Surface(modifier = Modifier.fillMaxSize()) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator()
                    }
                }
            } else {
                // Token sudah terbaca, atur startDestination sesuai token
                val startDestination = if (apiToken.isNullOrEmpty()) Screen.Login.route else Screen.Home.route

                NavGraph(
                    navController = navController,
                    startDestination = startDestination,
                    userPreferences = userPreferences
                )
            }
        }

    }
}

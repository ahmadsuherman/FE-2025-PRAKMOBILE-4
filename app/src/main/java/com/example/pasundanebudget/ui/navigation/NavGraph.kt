package com.example.pasundanebudget.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.pasundanebudget.data.local.preferences.UserPreferences
import com.example.pasundanebudget.ui.screens.*
import com.example.pasundanebudget.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import com.example.pasundanebudget.viewmodel.CategoryViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Category : Screen("category")
    object Transaction : Screen("transaction")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String,
    authViewModel: AuthViewModel = hiltViewModel(),
    userPreferences: UserPreferences = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()

    val apiToken by userPreferences.apiTokenFlow.collectAsState(initial = null)
    val userId by userPreferences.userIdFlow.collectAsState(initial = null)
    val categoryUiState by categoryViewModel.uiState.collectAsState()

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { token ->
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = { token ->
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }

        composable(Screen.Home.route) {
            if (apiToken != null && userId != null) {
                HomeScreen(
                    userId = userId!!,
                    onLogout = {
                        coroutineScope.launch {
                            authViewModel.logout()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        }
                    },
                    onNavigateToCategory = {
                        navController.navigate(Screen.Category.route)
                    },
                    onNavigateToTransaction = {
                        navController.navigate(Screen.Transaction.route)
                    }
                )
            } else {
                // Kalau belum login, langsung arahkan ke login
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            }
        }

        composable(Screen.Category.route) {
            if (apiToken != null && userId != null) {
                CategoryScreen(userId = userId!!, apiToken = apiToken!!)
            } else {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Category.route) { inclusive = true }
                    }
                }
            }
        }

        composable(Screen.Transaction.route) {

            if (apiToken != null && userId != null) {

                LaunchedEffect(Unit) {
                    categoryViewModel.syncAndLoadCategories()
                }

                TransactionScreen(
                    userId = userId!!,
                    apiToken = apiToken!!,
                    categories = categoryUiState.categories,
                    transactionViewModel = hiltViewModel()
                )

            } else {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Transaction.route) { inclusive = true }
                    }
                }
            }
        }
    }
}
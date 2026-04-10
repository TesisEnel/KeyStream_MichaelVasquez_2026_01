package com.phantomshard.keystream.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.phantomshard.keystream.data.local.ApiKeyStore
import com.phantomshard.keystream.ui.dashboard.DashboardScreen
import com.phantomshard.keystream.ui.signin.SignInScreen
import org.koin.compose.koinInject

@Composable
fun KeyStreamNavHost(navHostController: NavHostController) {
    val apiKeyStore: ApiKeyStore = koinInject()
    val startDestination = if (apiKeyStore.hasKey()) Screen.Dashboard else Screen.SignIn

    NavHost(
        navController = navHostController,
        startDestination = startDestination
    ) {
        composable<Screen.SignIn> {
            SignInScreen(
                onNavigateToDashboard = {
                    navHostController.navigate(Screen.Dashboard) {
                        popUpTo(Screen.SignIn) { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.Dashboard> {
            DashboardScreen(
                onLogout = {
                    navHostController.navigate(Screen.SignIn) {
                        popUpTo(Screen.Dashboard) { inclusive = true }
                    }
                }
            )
        }
    }
}

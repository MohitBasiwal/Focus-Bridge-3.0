package com.example.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.ui.screens.*

@Composable
fun FocusBridgeNavHost(
    navController: NavHostController,
    paddingValues: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash,
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        composable<Screen.Splash> {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home) {
                        popUpTo<Screen.Splash> { inclusive = true }
                    }
                }
            )
        }
        composable<Screen.Home> {
            HomeScreen()
        }
        composable<Screen.Timetable> {
            TimetableScreen()
        }
        composable<Screen.Analytics> {
            AnalyticsScreen()
        }
        composable<Screen.Settings> {
            SettingsScreen()
        }
    }
}

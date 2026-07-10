package com.example.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.*

@Composable
fun FocusBridgeNavHost(
    navController: NavHostController,
    paddingValues: PaddingValues,
    viewModel: com.example.ui.viewmodel.AppBlockerViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash,
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        enterTransition = {
            fadeIn(animationSpec = tween(300)) + 
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left, 
                animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 1200f)
            )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(300)) + 
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left, 
                animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 1200f)
            )
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(300)) + 
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right, 
                animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 1200f)
            )
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(300)) + 
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right, 
                animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 1200f)
            )
        }
    ) {
        composable<Screen.Splash>(
            enterTransition = { fadeIn(animationSpec = tween(400)) },
            exitTransition = { fadeOut(animationSpec = tween(400)) }
        ) {
            SplashScreen(
                onNavigateToHome = {
                    if (isOnboardingCompleted) {
                        navController.navigate(Screen.Home) {
                            popUpTo<Screen.Splash> { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Onboarding) {
                            popUpTo<Screen.Splash> { inclusive = true }
                        }
                    }
                }
            )
        }
        composable<Screen.Onboarding> {
            OnboardingScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home) {
                        popUpTo<Screen.Onboarding> { inclusive = true }
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

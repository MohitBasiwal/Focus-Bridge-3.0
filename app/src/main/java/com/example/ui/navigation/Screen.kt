package com.example.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    data object Splash : Screen

    @Serializable
    data object Home : Screen

    @Serializable
    data object Timetable : Screen

    @Serializable
    data object Analytics : Screen

    @Serializable
    data object Settings : Screen
}

package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: Screen
)

val bottomNavItems = listOf(
    BottomNavItem("Home", Icons.Default.Home, Screen.Home),
    BottomNavItem("Schedule", Icons.Default.CalendarToday, Screen.Timetable),
    BottomNavItem("Analytics", Icons.Default.BarChart, Screen.Analytics),
    BottomNavItem("Settings", Icons.Default.Settings, Screen.Settings)
)

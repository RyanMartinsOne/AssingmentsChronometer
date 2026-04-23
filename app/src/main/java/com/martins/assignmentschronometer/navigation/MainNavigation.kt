package com.martins.assignmentschronometer.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.martins.assignmentschronometer.ui.screens.assignments.AssignmentsScreen
import com.martins.assignmentschronometer.ui.screens.chronometer.ChronometerScreen
import com.martins.assignmentschronometer.ui.screens.record.RecordScreen
import com.martins.assignmentschronometer.ui.screens.settings.SettingsScreen

@Composable
fun MainNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
    ) {

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {

        composable(route = Screen.Home.route) {
            ChronometerScreen()
        }

        composable(route = Screen.Assignments.route) {
            AssignmentsScreen()
        }

        composable(route = Screen.Record.route) {
            RecordScreen()
        }

        composable(route = Screen.Settings.route) {
            SettingsScreen()
        }
    }
}
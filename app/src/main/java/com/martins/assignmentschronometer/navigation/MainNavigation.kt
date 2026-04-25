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
import com.martins.assignmentschronometer.viewmodel.SharedViewModel

@Composable
fun MainNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    sharedViewModel: SharedViewModel
    ) {

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {

        composable(route = Screen.Home.route) {
            ChronometerScreen(sharedViewModel)
        }

        composable(route = Screen.Assignments.route) {
            AssignmentsScreen(
                sharedViewModel,
                navController = navController
            )
        }

        composable(route = Screen.Record.route) {
            RecordScreen()
        }

        composable(route = Screen.Settings.route) {
            SettingsScreen()
        }
    }
}
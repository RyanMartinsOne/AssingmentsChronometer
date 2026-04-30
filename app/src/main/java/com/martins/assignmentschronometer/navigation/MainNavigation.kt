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
import com.martins.assignmentschronometer.viewmodel.WeeklyPartsViewModel

@Composable
fun MainNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    sharedViewModel: SharedViewModel,
    weeklyPartsViewModel: WeeklyPartsViewModel
    ) {

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {

        composable(route = Screen.Home.route) {
            ChronometerScreen(
                sharedViewModel,
                weeklyPartsViewModel
            )
        }

        composable(route = Screen.Assignments.route) {
            AssignmentsScreen(
                onAssignmentClick = { assignment ->
                    sharedViewModel.selectAssignment(assignment)
                    sharedViewModel.start()
                    navController.navigate(Screen.Home.route)
                }
            )
        }

        composable(route = Screen.Record.route) {
            RecordScreen(
                viewModel = weeklyPartsViewModel,
                sharedViewModel = sharedViewModel,
                onNavigateToChronometer = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.Settings.route) {
            SettingsScreen()
        }
    }
}
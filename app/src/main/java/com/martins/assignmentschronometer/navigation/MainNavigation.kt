package com.martins.assignmentschronometer.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.martins.assignmentschronometer.ui.screens.assignments.AssignmentsScreen
import com.martins.assignmentschronometer.ui.screens.chronometer.ChronometerScreen
import com.martins.assignmentschronometer.ui.screens.licenses.LicensesScreen
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
    fun navigateToTopLevel(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    val pendingShortcutRoute = weeklyPartsViewModel.pendingShortcutRoute

    LaunchedEffect(pendingShortcutRoute) {
        pendingShortcutRoute?.let { route ->
            navigateToTopLevel(route)
            weeklyPartsViewModel.onShortcutRouteHandled()
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None }
    ) {
        composable(route = Screen.Home.route) {
            ChronometerScreen(
                sharedViewModel = sharedViewModel,
                weeklyPartsViewModel = weeklyPartsViewModel
            )
        }

        composable(route = Screen.Assignments.route) {
            AssignmentsScreen(
                onAssignmentClick = { assignment ->
                    sharedViewModel.selectAssignment(assignment)
                    sharedViewModel.start()
                    navigateToTopLevel(Screen.Home.route)
                }
            )
        }

        composable(route = Screen.Record.route) {
            RecordScreen(
                viewModel = weeklyPartsViewModel,
                sharedViewModel = sharedViewModel,
                onNavigateToChronometer = {
                    navigateToTopLevel(Screen.Home.route)
                }
            )
        }

        composable(route = Screen.Settings.route) {
            SettingsScreen(
                onOpenLicenses = {
                    navController.navigate(Screen.Licenses.route)
                },
                onNavigateToRecord = {
                    navigateToTopLevel(Screen.Record.route)
                }
            )
        }

        composable(route = Screen.Licenses.route) {
            LicensesScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
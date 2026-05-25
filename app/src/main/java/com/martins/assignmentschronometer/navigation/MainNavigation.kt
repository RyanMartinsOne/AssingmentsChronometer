package com.martins.assignmentschronometer.navigation

import android.content.Intent
import android.provider.Settings as AndroidSettings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
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
                sharedViewModel,
                weeklyPartsViewModel
            )
        }

        composable(route = Screen.Assignments.route) {
            val context = LocalContext.current
            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) {
                if (AndroidSettings.canDrawOverlays(context)) {
                    sharedViewModel.start()
                    navigateToTopLevel(Screen.Home.route)
                }
            }

            AssignmentsScreen(
                onAssignmentClick = { assignment ->
                    sharedViewModel.selectAssignment(assignment)
                    val isPermissionGranted = AndroidSettings.canDrawOverlays(context)

                    sharedViewModel.safeStart(
                        hasPermission = isPermissionGranted,
                        onPermissionRequired = {
                            val intent = Intent(
                                AndroidSettings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                "package:${context.packageName}".toUri()
                            )
                            launcher.launch(intent)
                        }
                    )

                    if (isPermissionGranted) {
                        navigateToTopLevel(Screen.Home.route)
                    }
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
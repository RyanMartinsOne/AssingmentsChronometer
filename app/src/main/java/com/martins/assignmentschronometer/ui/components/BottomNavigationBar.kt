package com.martins.assignmentschronometer.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.martins.assignmentschronometer.navigation.Screen

@Composable
fun BottomNavigationBar(navController: NavController) {

    val items = listOf(
        Screen.Home,
        Screen.Assignments,
        Screen.Record,
        Screen.Settings
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        items.forEach { screen ->
            val isSelected = currentRoute == screen.route

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(
                            id = if (isSelected) screen.selectedIconsRes
                            else screen.unselectedIconRes
                        ),
                        contentDescription = stringResource(screen.titleRes)
                    )
                },
                label = {
                    Text(text = stringResource(screen.titleRes))
                }
            )
        }
    }

}
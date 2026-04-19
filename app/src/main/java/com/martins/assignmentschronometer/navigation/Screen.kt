package com.martins.assignmentschronometer.navigation

import com.martins.assignmentschronometer.R


sealed class Screen(
    val route: String,
    val titleRes: Int,
    val selectedIconsRes : Int,
    val unselectedIconRes: Int
) {
    object Home : Screen(
        "home",
        R.string.nav_home,
        R.drawable.home_outlined,
        R.drawable.home_filled
    )
    object Assignments : Screen(
        "assignments",
        R.string.nav_assignments,
        R.drawable.podium_outlined,
        R.drawable.podium_filled
    )
    object Record : Screen(
        "record",
        R.string.nav_record,
        R.drawable.record_outlined,
        R.drawable.record_filled
    )
    object Settings : Screen(
        "settings",
        R.string.nav_settings,
        R.drawable.settings_outlined,
        R.drawable.settings_filled
    )
}

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
        R.drawable.home_filled,
        R.drawable.home_outlined
    )
    object Assignments : Screen(
        "assignments",
        R.string.nav_assignments,
        R.drawable.podium_filled,
        R.drawable.podium_outlined
    )
    object Record : Screen(
        "record",
        R.string.nav_record,
        R.drawable.record_filled,
        R.drawable.record_outlined
    )
    object Settings : Screen(
        "settings",
        R.string.nav_settings,
        R.drawable.settings_filled,
        R.drawable.settings_outlined
    )

    object Licenses : Screen(
        route = "licenses",
        titleRes = 0,
        selectedIconsRes = 0,
        unselectedIconRes = 0
    )
}

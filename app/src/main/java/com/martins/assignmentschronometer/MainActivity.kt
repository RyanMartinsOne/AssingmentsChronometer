package com.martins.assignmentschronometer

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.martins.assignmentschronometer.navigation.MainNavigation
import com.martins.assignmentschronometer.overlay.ChronometerOverlayService
import com.martins.assignmentschronometer.ui.components.BottomNavigationBar
import com.martins.assignmentschronometer.ui.theme.AssignmentsChronometerTheme
import com.martins.assignmentschronometer.viewmodel.SharedViewModel
import com.martins.assignmentschronometer.viewmodel.WeeklyPartsViewModel
import kotlin.jvm.java

class MainActivity : ComponentActivity() {

    private val sharedViewModel: SharedViewModel by lazy {
        (application as App).sharedViewModel
    }
    private val weeklyPartsViewModel: WeeklyPartsViewModel by lazy {
        (application as App).weeklyPartsViewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AssignmentsChronometerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    Scaffold(
                        bottomBar = {
                            BottomNavigationBar(navController = navController)
                        }
                    ) { innerPadding ->
                        MainNavigation(
                            navController = navController,
                            modifier = Modifier.padding(innerPadding),
                            sharedViewModel = sharedViewModel,
                            weeklyPartsViewModel = weeklyPartsViewModel
                        )
                    }
                }
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (sharedViewModel.isRunning) {
            showOverlay()
        }
    }

    override fun onResume() {
        super.onResume()
        hideOverlay()
    }

    private fun showOverlay() {
        if (Settings.canDrawOverlays(this)) {
            val intent = Intent(this, ChronometerOverlayService::class.java)
            startService(intent)
        }
    }

    private fun hideOverlay() {
        val intent = Intent(this, ChronometerOverlayService::class.java)
        stopService(intent)
    }
}
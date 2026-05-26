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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.martins.assignmentschronometer.navigation.MainNavigation
import com.martins.assignmentschronometer.navigation.Screen
import com.martins.assignmentschronometer.overlay.ChronometerOverlayService
import com.martins.assignmentschronometer.ui.components.BottomNavigationBar
import com.martins.assignmentschronometer.ui.theme.AssignmentsChronometerTheme
import com.martins.assignmentschronometer.viewmodel.SharedViewModel
import com.martins.assignmentschronometer.viewmodel.WeeklyPartsViewModel

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

        if (savedInstanceState == null) {
            handleIncomingIntent(intent)
        }

        setContent {
            val settingsViewModel = (application as App).settingsViewModel
            val settingsUiState by settingsViewModel.uiState.collectAsStateWithLifecycle()

            AssignmentsChronometerTheme(
                dynamicColorsEnabled = settingsUiState.dynamicColorsEnabled
            ) {
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null || intent.action != Intent.ACTION_VIEW) return
        if ((intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) return

        val uri = intent.data ?: return

        if (uri.scheme == "chronometer") {
            when (uri.host) {
                "start" -> sharedViewModel.start()
                "import-media" -> {
                    weeklyPartsViewModel.navigateToShortcutRoute(Screen.Record.route)
                    weeklyPartsViewModel.triggerImportMedia()
                }
                "scan" -> {
                    weeklyPartsViewModel.navigateToShortcutRoute(Screen.Record.route)
                    weeklyPartsViewModel.triggerScan()
                }
                "import-acdata" -> {
                    weeklyPartsViewModel.navigateToShortcutRoute(Screen.Settings.route)
                    weeklyPartsViewModel.triggerImportAcdata()
                }
            }
            this.intent = Intent()
            return
        }

        weeklyPartsViewModel.importRecords(uri)
        this.intent = Intent()
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
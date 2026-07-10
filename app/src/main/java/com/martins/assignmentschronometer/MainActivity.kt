package com.martins.assignmentschronometer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.martins.assignmentschronometer.navigation.MainNavigation
import com.martins.assignmentschronometer.navigation.Screen
import com.martins.assignmentschronometer.overlay.ChronometerOverlayService
import com.martins.assignmentschronometer.ui.components.BottomNavigationBar
import com.martins.assignmentschronometer.ui.theme.AssignmentsChronometerTheme
import com.martins.assignmentschronometer.viewmodel.SettingsViewModel
import com.martins.assignmentschronometer.viewmodel.SharedViewModel
import com.martins.assignmentschronometer.viewmodel.WeeklyPartsViewModel

class MainActivity : ComponentActivity() {

    private val sharedViewModel: SharedViewModel by lazy {
        (application as App).sharedViewModel
    }

    private val weeklyPartsViewModel: WeeklyPartsViewModel by lazy {
        (application as App).weeklyPartsViewModel
    }

    private val settingsViewModel: SettingsViewModel by lazy {
        (application as App).settingsViewModel
    }

    // Precisa ser propriedade de classe: registerForActivityResult só pode ser
    // chamado durante a inicialização da Activity, nunca dentro de onCreate()
    // nem dentro do bloco setContent { }.
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* no-op: o cronômetro funciona de qualquer forma, só a notificação some */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (savedInstanceState == null) {
            handleIncomingIntent(intent)
        }

        setContent {
            val settingsUiState by settingsViewModel.uiState.collectAsStateWithLifecycle()

            val firstLaunchOverlayLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) {
                val granted = Settings.canDrawOverlays(this@MainActivity)
                settingsViewModel.setOverlayEnabled(granted)
                settingsViewModel.setFirstLaunchDone()
            }

            var showFirstLaunchDialog by remember { mutableStateOf(false) }

            LaunchedEffect(settingsUiState.isLoaded, settingsUiState.isFirstLaunchDone) {
                if (settingsUiState.isLoaded) {
                    showFirstLaunchDialog = !settingsUiState.isFirstLaunchDone
                }
            }

            AssignmentsChronometerTheme(
                themeMode = settingsUiState.themeMode,
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

                    if (showFirstLaunchDialog) {
                        AlertDialog(
                            onDismissRequest = {},
                            title = {
                                Text(stringResource(R.string.first_launch_overlay_title))
                            },
                            text = {
                                Text(stringResource(R.string.first_launch_overlay_message))
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        showFirstLaunchDialog = false

                                        if (Settings.canDrawOverlays(this@MainActivity)) {
                                            settingsViewModel.setOverlayEnabled(true)
                                            settingsViewModel.setFirstLaunchDone()
                                        } else {
                                            val intent = Intent(
                                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                                "package:${packageName}".toUri()
                                            )
                                            firstLaunchOverlayLauncher.launch(intent)
                                        }
                                    }
                                ) {
                                    Text(stringResource(R.string.first_launch_overlay_enable))
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = {
                                        showFirstLaunchDialog = false
                                        settingsViewModel.setOverlayEnabled(false)
                                        settingsViewModel.setFirstLaunchDone()
                                    }
                                ) {
                                    Text(stringResource(R.string.first_launch_overlay_skip))
                                }
                            }
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
        val overlayEnabled = settingsViewModel.uiState.value.overlayEnabled
        if (overlayEnabled && Settings.canDrawOverlays(this)) {
            val intent = Intent(this, ChronometerOverlayService::class.java)
            startService(intent)
        }
    }

    private fun hideOverlay() {
        val intent = Intent(this, ChronometerOverlayService::class.java)
        stopService(intent)
    }
}
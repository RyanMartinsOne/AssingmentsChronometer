@file:OptIn(ExperimentalMaterial3Api::class)

package com.martins.assignmentschronometer.ui.screens.settings

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.martins.assignmentschronometer.App

@Composable
fun SettingsScreen(
    onExportRecords: () -> Unit = {},
    onClearRecords: () -> Unit = {},
    onRequestOverlayPermission: () -> Unit = {},
    onOpenLicenses: () -> Unit = {}
) {
    val app = LocalContext.current.applicationContext as App
    val viewModel = app.settingsViewModel
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { paddingValues ->
        SettingsContent(
            uiState = uiState,
            onDarkModeChange = viewModel::setDarkModeEnabled,
            onDynamicColorsChange = viewModel::setDynamicColorsEnabled,
            onNotificationsChange = viewModel::setNotificationsEnabled,
            onAutoSaveChange = viewModel::setAutoSaveEnabled,
            onOvertimeAlertChange = viewModel::setOvertimeAlertEnabled,
            onOverlayScaleXSave = viewModel::saveOverlayScaleX,
            onOverlayScaleYSave = viewModel::saveOverlayScaleY,
            onOverlayMessageChange = viewModel::updateOverlayMessage,
            onClearOverlayMessage = viewModel::clearOverlayMessage,
            onExportRecords = onExportRecords,
            onClearRecords = onClearRecords,
            onRequestOverlayPermission = onRequestOverlayPermission,
            onOpenLicenses = onOpenLicenses,
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}
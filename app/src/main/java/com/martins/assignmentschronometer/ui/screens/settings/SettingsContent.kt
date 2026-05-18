package com.martins.assignmentschronometer.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsContent(
    uiState: SettingsUiState,
    onDarkModeChange: (Boolean) -> Unit,
    onDynamicColorsChange: (Boolean) -> Unit,
    onNotificationsChange: (Boolean) -> Unit,
    onAutoSaveChange: (Boolean) -> Unit,
    onOvertimeAlertChange: (Boolean) -> Unit,
    onOverlayScaleXSave: (Float) -> Unit,
    onOverlayScaleYSave: (Float) -> Unit,
    onOverlayMessageChange: (String?) -> Unit,
    onClearOverlayMessage: () -> Unit,
    onExportRecords: () -> Unit,
    onClearRecords: () -> Unit,
    onRequestOverlayPermission: () -> Unit,
    onOpenLicenses: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SettingsHeaderCard()
        }

        item {
            AppearanceSettingsSection(
                darkModeEnabled = uiState.darkModeEnabled,
                dynamicColorsEnabled = uiState.dynamicColorsEnabled,
                overlayScaleX = uiState.overlayScaleX,
                overlayScaleY = uiState.overlayScaleY,
                overlaySizeMessage = uiState.overlaySizeMessage,
                onDarkModeChange = onDarkModeChange,
                onDynamicColorsChange = onDynamicColorsChange,
                onOverlayScaleXSave = onOverlayScaleXSave,
                onOverlayScaleYSave = onOverlayScaleYSave,
                onOverlayMessageChange = onOverlayMessageChange,
                onClearOverlayMessage = onClearOverlayMessage
            )
        }

        item {
            NotificationsSettingsSection(
                notificationsEnabled = uiState.notificationsEnabled,
                overtimeAlertEnabled = uiState.overtimeAlertEnabled,
                onNotificationsChange = onNotificationsChange,
                onOvertimeAlertChange = onOvertimeAlertChange
            )
        }

        item {
            DataSettingsSection(
                autoSaveEnabled = uiState.autoSaveEnabled,
                onAutoSaveChange = onAutoSaveChange,
                onExportRecords = onExportRecords,
                onClearRecords = onClearRecords
            )
        }

        item {
            AdvancedSettingsSection(
                onRequestOverlayPermission = onRequestOverlayPermission,
                onOpenLicenses = onOpenLicenses
            )
        }
    }
}
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
    onDynamicColorsChange: (Boolean) -> Unit,
    onSaveOverlayDimensions: (Float, Float) -> Unit,
    onHeightResultChanged: (OverlayAdjustmentResult) -> Unit,
    onClearOverlayMessage: () -> Unit,
    onExportRecords: () -> Unit,
    onImportRecords: () -> Unit,
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
                dynamicColorsEnabled = uiState.dynamicColorsEnabled,
                overlayScaleX = uiState.overlayScaleX,
                overlayScaleY = uiState.overlayScaleY,
                overlaySizeMessageRes = uiState.overlaySizeMessageRes,
                overlaySizeMessageArgs = uiState.overlaySizeMessageArgs,
                onDynamicColorsChange = onDynamicColorsChange,
                onSaveDimensions = onSaveOverlayDimensions,
                onHeightResultChanged = onHeightResultChanged,
                onClearOverlayMessage = onClearOverlayMessage
            )
        }

        item {
            DataSettingsSection(
                onExportRecords = onExportRecords,
                onImportRecords = onImportRecords,
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
package com.martins.assignmentschronometer.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.martins.assignmentschronometer.R
import com.martins.assignmentschronometer.ui.components.ClearRecordsDialog
import com.martins.assignmentschronometer.ui.components.OverlayAppearanceSettingItem
import com.martins.assignmentschronometer.ui.components.SettingsActionItem
import com.martins.assignmentschronometer.ui.components.SettingsSelectableItem
import com.martins.assignmentschronometer.ui.components.SettingsSwitchItem
import com.martins.assignmentschronometer.ui.theme.ThemeMode

data class SettingsActions(
    val onThemeModeChange: (ThemeMode) -> Unit,
    val onDynamicColorsChange: (Boolean) -> Unit,
    val onSaveOverlayOpacity: (Float) -> Unit,
    val onSaveOverlayDimensions: (Float, Float) -> Unit,
    val onHeightResultChanged: (OverlayAdjustmentResult) -> Unit,
    val onClearOverlayMessage: () -> Unit,
    val onExportRecords: () -> Unit,
    val onImportRecords: () -> Unit,
    val onClearRecords: () -> Unit,
    val onRequestOverlayPermission: () -> Unit,
    val onOpenLicenses: () -> Unit
)

@Composable
fun SettingsContent(
    uiState: SettingsUiState,
    actions: SettingsActions,
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
                uiState = uiState,
                onThemeModeChange = actions.onThemeModeChange,
                onDynamicColorsChange = actions.onDynamicColorsChange,
                onSaveOpacity = actions.onSaveOverlayOpacity,
                onSaveDimensions = actions.onSaveOverlayDimensions,
                onHeightResultChanged = actions.onHeightResultChanged,
                onClearOverlayMessage = actions.onClearOverlayMessage
            )
        }

        item {
            DataSettingsSection(
                onExportRecords = actions.onExportRecords,
                onImportRecords = actions.onImportRecords,
                onClearRecords = actions.onClearRecords
            )
        }

        item {
            AdvancedSettingsSection(
                onRequestOverlayPermission = actions.onRequestOverlayPermission,
                onOpenLicenses = actions.onOpenLicenses
            )
        }
    }
}

@Composable
private fun AppearanceSettingsSection(
    uiState: SettingsUiState,
    onThemeModeChange: (ThemeMode) -> Unit,
    onDynamicColorsChange: (Boolean) -> Unit,
    onSaveDimensions: (Float, Float) -> Unit,
    onSaveOpacity: (Float) -> Unit,
    onHeightResultChanged: (OverlayAdjustmentResult) -> Unit,
    onClearOverlayMessage: () -> Unit
) {
    var showThemeModeDialog by remember { mutableStateOf(false) }

    SettingsSection(title = stringResource(R.string.settings_section_appearance)) {
        SettingsSelectableItem(
            icon = ImageVector.vectorResource(R.drawable.dark_mode),
            title = stringResource(R.string.settings_theme_mode_title),
            description = stringResource(R.string.settings_theme_mode_description),
            selectedValue = when (uiState.themeMode) {
                ThemeMode.SYSTEM -> stringResource(R.string.settings_theme_mode_system)
                ThemeMode.LIGHT -> stringResource(R.string.settings_theme_mode_light)
                ThemeMode.DARK -> stringResource(R.string.settings_theme_mode_dark)
            },
            onClick = { showThemeModeDialog = true }
        )

        HorizontalDivider()

        SettingsSwitchItem(
            icon = ImageVector.vectorResource(R.drawable.palette),
            title = stringResource(R.string.settings_dynamic_colors_title),
            description = stringResource(R.string.settings_dynamic_colors_description),
            checked = uiState.dynamicColorsEnabled,
            onCheckedChange = onDynamicColorsChange
        )

        HorizontalDivider()

        OverlayAppearanceSettingItem(
            icon = ImageVector.vectorResource(R.drawable.aspect_ratio),
            title = stringResource(R.string.settings_overlay_appearance_title),
            description = stringResource(R.string.settings_overlay_size_description),
            currentScaleX = uiState.overlayScaleX,
            currentScaleY = uiState.overlayScaleY,
            currentOpacity = uiState.overlayOpacity,
            messageRes = uiState.overlaySizeMessageRes,
            messageArgs = uiState.overlaySizeMessageArgs,
            onHeightResultChanged = onHeightResultChanged,
            onSaveDimensions = onSaveDimensions,
            onSaveOpacity = onSaveOpacity,
            onClearMessage = onClearOverlayMessage
        )
    }

    if (showThemeModeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeModeDialog = false },
            title = {
                Text(text = stringResource(R.string.settings_theme_mode_dialog_title))
            },
            text = {
                Column {
                    ThemeMode.entries.forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onThemeModeChange(mode)
                                    showThemeModeDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = uiState.themeMode == mode,
                                onClick = {
                                    onThemeModeChange(mode)
                                    showThemeModeDialog = false
                                }
                            )

                            Text(
                                text = when (mode) {
                                    ThemeMode.SYSTEM -> stringResource(R.string.settings_theme_mode_system)
                                    ThemeMode.LIGHT -> stringResource(R.string.settings_theme_mode_light)
                                    ThemeMode.DARK -> stringResource(R.string.settings_theme_mode_dark)
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
private fun DataSettingsSection(
    onExportRecords: () -> Unit,
    onImportRecords: () -> Unit,
    onClearRecords: () -> Unit
) {
    var showClearDialog by remember { mutableStateOf(false) }

    SettingsSection(title = stringResource(R.string.settings_section_data)) {
        SettingsActionItem(
            icon = ImageVector.vectorResource(R.drawable.download),
            title = stringResource(R.string.settings_import_title),
            description = stringResource(R.string.settings_import_description),
            onClick = onImportRecords
        )

        HorizontalDivider()

        SettingsActionItem(
            icon = ImageVector.vectorResource(R.drawable.upload),
            title = stringResource(R.string.settings_export_title),
            description = stringResource(R.string.settings_export_description),
            onClick = onExportRecords
        )

        HorizontalDivider()

        SettingsActionItem(
            icon = ImageVector.vectorResource(R.drawable.delete),
            title = stringResource(R.string.settings_clear_title),
            description = stringResource(R.string.settings_clear_description),
            onClick = { showClearDialog = true }
        )
    }

    if (showClearDialog) {
        ClearRecordsDialog(
            onConfirm = {
                showClearDialog = false
                onClearRecords()
            },
            onDismiss = { showClearDialog = false }
        )
    }
}

@Composable
private fun AdvancedSettingsSection(
    onRequestOverlayPermission: () -> Unit,
    onOpenLicenses: () -> Unit
) {
    SettingsSection(title = stringResource(R.string.settings_section_advanced)) {
        SettingsActionItem(
            icon = ImageVector.vectorResource(R.drawable.security),
            title = stringResource(R.string.settings_overlay_permission_title),
            description = stringResource(R.string.settings_overlay_permission_description),
            onClick = onRequestOverlayPermission
        )

        HorizontalDivider()

        SettingsActionItem(
            icon = ImageVector.vectorResource(R.drawable.info),
            title = stringResource(R.string.settings_licenses_title),
            description = stringResource(R.string.settings_licenses_description),
            onClick = onOpenLicenses
        )
    }
}

@Composable
private fun SettingsHeaderCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(24.dp)
            )
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.settings_header_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = stringResource(R.string.settings_header_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(24.dp)
                )
        ) {
            content()
        }
    }
}
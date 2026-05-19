package com.martins.assignmentschronometer.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.martins.assignmentschronometer.R
import com.martins.assignmentschronometer.ui.components.ClearRecordsDialog
import com.martins.assignmentschronometer.ui.components.OverlaySizeSettingItem

// ─── Appearance ───────────────────────────────────────────────────────────────

@Composable
internal fun AppearanceSettingsSection(
    dynamicColorsEnabled: Boolean,
    overlayScaleX: Float,
    overlayScaleY: Float,
    overlaySizeMessageRes: Int?,
    overlaySizeMessageArgs: List<Any>,
    onDynamicColorsChange: (Boolean) -> Unit,
    onSaveDimensions: (Float, Float) -> Unit,
    onHeightResultChanged: (OverlayAdjustmentResult) -> Unit,
    onClearOverlayMessage: () -> Unit
) {
    SettingsSection(title = stringResource(R.string.settings_section_appearance)) {
        SettingsSwitchItem(
            icon = ImageVector.vectorResource(R.drawable.palette),
            title = stringResource(R.string.settings_dynamic_colors_title),
            description = stringResource(R.string.settings_dynamic_colors_description),
            checked = dynamicColorsEnabled,
            onCheckedChange = onDynamicColorsChange
        )

        HorizontalDivider()

        OverlaySizeSettingItem(
            icon = ImageVector.vectorResource(R.drawable.aspect_ratio),
            title = stringResource(R.string.settings_overlay_size_title),
            description = stringResource(R.string.settings_overlay_size_description),
            currentScaleX = overlayScaleX,
            currentScaleY = overlayScaleY,
            messageRes = overlaySizeMessageRes,
            messageArgs = overlaySizeMessageArgs,
            onHeightResultChanged = onHeightResultChanged,
            onSaveDimensions = onSaveDimensions, // Repassa a nova função unificada
            onClearMessage = onClearOverlayMessage
        )
    }
}

// ─── Data ─────────────────────────────────────────────────────────────────────

@Composable
internal fun DataSettingsSection(
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

// ─── Advanced ─────────────────────────────────────────────────────────────────

@Composable
internal fun AdvancedSettingsSection(
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

// ─── Header ───────────────────────────────────────────────────────────────────

@Composable
fun SettingsHeaderCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
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

// ─── Internal scaffold ────────────────────────────────────────────────────────

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
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
        ) {
            content()
        }
    }
}
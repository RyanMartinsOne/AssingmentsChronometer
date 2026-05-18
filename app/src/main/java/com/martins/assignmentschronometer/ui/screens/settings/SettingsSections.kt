package com.martins.assignmentschronometer.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.martins.assignmentschronometer.R
import com.martins.assignmentschronometer.ui.components.OverlaySizeSettingItem

@Composable
internal fun AppearanceSettingsSection(
    darkModeEnabled: Boolean,
    dynamicColorsEnabled: Boolean,
    overlayScaleX: Float,
    overlayScaleY: Float,
    overlaySizeMessage: String?,
    onDarkModeChange: (Boolean) -> Unit,
    onDynamicColorsChange: (Boolean) -> Unit,
    onOverlayScaleXSave: (Float) -> Unit,
    onOverlayScaleYSave: (Float) -> Unit,
    onOverlayMessageChange: (String?) -> Unit,
    onClearOverlayMessage: () -> Unit
) {
    SettingsSection(title = "Appearance") {
        SettingsSwitchItem(
            icon = ImageVector.vectorResource(R.drawable.dark_mode),
            title = "Dark mode",
            description = "Use a darker interface for low-light environments",
            checked = darkModeEnabled,
            onCheckedChange = onDarkModeChange
        )

        HorizontalDivider()

        SettingsSwitchItem(
            icon = ImageVector.vectorResource(R.drawable.palette),
            title = "Dynamic colors",
            description = "Use Material You colors on supported devices",
            checked = dynamicColorsEnabled,
            onCheckedChange = onDynamicColorsChange
        )

        HorizontalDivider()

        OverlaySizeSettingItem(
            icon = ImageVector.vectorResource(R.drawable.aspect_ratio),
            title = "Overlay size",
            description = "Adjust width and height of the floating chronometer overlay",
            currentScaleX = overlayScaleX,
            currentScaleY = overlayScaleY,
            message = overlaySizeMessage,
            onScaleXSaved = onOverlayScaleXSave,
            onScaleYSaved = onOverlayScaleYSave,
            onMessageChange = onOverlayMessageChange,
            onClearMessage = onClearOverlayMessage
        )
    }
}

@Composable
internal fun NotificationsSettingsSection(
    notificationsEnabled: Boolean,
    overtimeAlertEnabled: Boolean,
    onNotificationsChange: (Boolean) -> Unit,
    onOvertimeAlertChange: (Boolean) -> Unit
) {
    SettingsSection(title = "Notifications") {
        SettingsSwitchItem(
            icon = ImageVector.vectorResource(R.drawable.notifications),
            title = "Notifications",
            description = "Enable chronometer notifications and reminders",
            checked = notificationsEnabled,
            onCheckedChange = onNotificationsChange
        )

        HorizontalDivider()

        SettingsSwitchItem(
            icon = ImageVector.vectorResource(R.drawable.notifications),
            title = "Overtime alerts",
            description = "Highlight overtime with alerts and accent colors",
            checked = overtimeAlertEnabled,
            onCheckedChange = onOvertimeAlertChange
        )
    }
}

@Composable
internal fun DataSettingsSection(
    autoSaveEnabled: Boolean,
    onAutoSaveChange: (Boolean) -> Unit,
    onExportRecords: () -> Unit,
    onClearRecords: () -> Unit
) {
    SettingsSection(title = "Data") {
        SettingsSwitchItem(
            icon = ImageVector.vectorResource(R.drawable.hourglass),
            title = "Auto save realized time",
            description = "Automatically save realized time when closing overlay",
            checked = autoSaveEnabled,
            onCheckedChange = onAutoSaveChange
        )

        HorizontalDivider()

        SettingsActionItem(
            icon = ImageVector.vectorResource(R.drawable.upload_file),
            title = "Export records",
            description = "Export weekly parts and realized times",
            onClick = onExportRecords
        )

        HorizontalDivider()

        SettingsActionItem(
            icon = ImageVector.vectorResource(R.drawable.delete),
            title = "Clear all records",
            description = "Remove all imported and manually created parts",
            onClick = onClearRecords
        )
    }
}

@Composable
internal fun AdvancedSettingsSection(
    onRequestOverlayPermission: () -> Unit,
    onOpenLicenses: () -> Unit
) {
    SettingsSection(title = "Advanced") {
        SettingsActionItem(
            icon = ImageVector.vectorResource(R.drawable.security),
            title = "Overlay permission",
            description = "Allow floating chronometer over other apps",
            onClick = onRequestOverlayPermission
        )

        HorizontalDivider()

        SettingsActionItem(
            icon = ImageVector.vectorResource(R.drawable.info),
            title = "Open source licenses",
            description = "ML Kit, Jetpack Compose and other libraries",
            onClick = onOpenLicenses
        )
    }
}
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
                text = "Assignments Chronometer",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Text(
                text = "Manage overlay behavior, OCR imports, chronometer preferences and appearance settings.",
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
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
        ) {
            content()
        }
    }
}
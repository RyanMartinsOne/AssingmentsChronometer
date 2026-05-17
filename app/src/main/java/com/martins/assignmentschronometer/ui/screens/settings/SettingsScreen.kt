@file:OptIn(ExperimentalMaterial3Api::class)

package com.martins.assignmentschronometer.ui.screens.settings

import android.os.Build
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.martins.assignmentschronometer.R


@Composable
fun SettingsScreen(
    onExportRecords: () -> Unit = {},
    onClearRecords: () -> Unit = {},
    onRequestOverlayPermission: () -> Unit = {},
    onOpenLicenses: () -> Unit = {}
) {
    val darkModeEnabled = remember { mutableStateOf(false) }
    val dynamicColorsEnabled = remember { mutableStateOf(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) }
    val notificationsEnabled = remember { mutableStateOf(true) }
    val autoSaveEnabled = remember { mutableStateOf(true) }
    val overtimeAlertEnabled = remember { mutableStateOf(true) }

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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))

                SettingsHeaderCard()
            }

            item {
                SettingsSection(title = "Chronometer") {
                    SettingsSwitchItem(
                        icon = ImageVector.vectorResource(R.drawable.hourglass),
                        title = "Auto save realized time",
                        description = "Automatically save realized time when closing overlay",
                        checked = autoSaveEnabled.value,
                        onCheckedChange = {
                            autoSaveEnabled.value = it
                        }
                    )

                    HorizontalDivider()

                    SettingsSwitchItem(
                        icon = ImageVector.vectorResource(R.drawable.notifications),
                        title = "Overtime alerts",
                        description = "Highlight overtime with alerts and accent colors",
                        checked = overtimeAlertEnabled.value,
                        onCheckedChange = {
                            overtimeAlertEnabled.value = it
                        }
                    )
                }
            }

            item {
                SettingsSection(title = "Appearance") {
                    SettingsSwitchItem(
                        icon = ImageVector.vectorResource(R.drawable.dark_mode),
                        title = "Dark mode",
                        description = "Use a darker interface for low-light environments",
                        checked = darkModeEnabled.value,
                        onCheckedChange = {
                            darkModeEnabled.value = it
                        }
                    )

                    HorizontalDivider()

                    SettingsSwitchItem(
                        icon = ImageVector.vectorResource(R.drawable.palette),                        title = "Dynamic colors",
                        description = "Use Material You colors on supported devices",
                        checked = dynamicColorsEnabled.value,
                        onCheckedChange = {
                            dynamicColorsEnabled.value = it
                        }
                    )
                }
            }

            item {
                SettingsSection(title = "Records & OCR") {
                    SettingsActionItem(
                        icon = ImageVector.vectorResource(R.drawable.upload_file),                        title = "Export records",
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

            item {
                SettingsSection(title = "Permissions") {
                    SettingsActionItem(
                        icon = ImageVector.vectorResource(R.drawable.security),                        title = "Overlay permission",
                        description = "Allow floating chronometer over other apps",
                        onClick = onRequestOverlayPermission
                    )

                    HorizontalDivider()

                    SettingsSwitchItem(
                        icon = ImageVector.vectorResource(R.drawable.notifications),                        title = "Notifications",
                        description = "Enable chronometer notifications and reminders",
                        checked = notificationsEnabled.value,
                        onCheckedChange = {
                            notificationsEnabled.value = it
                        }
                    )
                }
            }

            item {
                SettingsSection(title = "About") {
                    SettingsActionItem(
                        icon = ImageVector.vectorResource(R.drawable.share),                        title = "Share app",
                        description = "Share Assignments Chronometer with others",
                        onClick = {}
                    )

                    HorizontalDivider()

                    SettingsActionItem(
                        icon = ImageVector.vectorResource(R.drawable.language),                        title = "Language",
                        description = "English / Português",
                        onClick = {}
                    )

                    HorizontalDivider()

                    SettingsActionItem(
                        icon = ImageVector.vectorResource(R.drawable.backup),                        title = "Backup & restore",
                        description = "Future support for cloud and local backups",
                        onClick = {}
                    )

                    HorizontalDivider()

                    SettingsActionItem(
                        icon = ImageVector.vectorResource(R.drawable.info),                        title = "Open source licenses",
                        description = "ML Kit, Jetpack Compose and other libraries",
                        onClick = onOpenLicenses
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Assignments Chronometer v1.0.0",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }
        }
    }
}

@Composable
private fun SettingsHeaderCard() {
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

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SettingsActionItem(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
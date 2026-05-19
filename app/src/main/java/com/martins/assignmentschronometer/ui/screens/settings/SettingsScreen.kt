@file:OptIn(ExperimentalMaterial3Api::class)

package com.martins.assignmentschronometer.ui.screens.settings

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.martins.assignmentschronometer.App
import com.martins.assignmentschronometer.R
import com.martins.assignmentschronometer.viewmodel.RecordsEvent
import androidx.core.net.toUri

@Composable
fun SettingsScreen(
    onOpenLicenses: () -> Unit = {}
) {
    val context = LocalContext.current
    val app = context.applicationContext as App

    val settingsViewModel = app.settingsViewModel
    val weeklyPartsViewModel = app.weeklyPartsViewModel

    val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    val recordsEvent = weeklyPartsViewModel.recordsEvent

    val snackbarHostState = remember { SnackbarHostState() }

    // ─── Export SAF launcher ──────────────────────────────────────────────────
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        uri?.let { weeklyPartsViewModel.exportRecords(it) }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {weeklyPartsViewModel.importRecords(it)}
    }

    val exportSuccessMsg = stringResource(R.string.settings_export_success)
    val exportEmptyMsg = stringResource(R.string.settings_export_empty)
    val exportErrorMsg = stringResource(R.string.settings_export_error)
    val importErrorMsg = stringResource(R.string.settings_import_error)
    val importInvalidMsg = stringResource(R.string.settings_import_invalid)

    val importSuccessMsg = if (recordsEvent is RecordsEvent.ImportSuccess) {
        stringResource(R.string.settings_import_success, recordsEvent.count)
    } else {
        ""
    }
    LaunchedEffect(recordsEvent) {
        val message = when (recordsEvent) {
            RecordsEvent.ExportSuccess -> exportSuccessMsg
            RecordsEvent.ExportEmpty -> exportEmptyMsg
            RecordsEvent.ExportError -> exportErrorMsg
            RecordsEvent.ImportError -> importErrorMsg
            RecordsEvent.ImportInvalid -> importInvalidMsg
            is RecordsEvent.ImportSuccess -> importSuccessMsg
            null -> null
        }

        message?.let {
            snackbarHostState.showSnackbar(it)
            weeklyPartsViewModel.onRecordsEventHandled()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(snackbarData = data)
            }
        }
    ) { paddingValues ->
        SettingsContent(
            uiState = uiState,
            onDynamicColorsChange = settingsViewModel::setDynamicColorsEnabled,
            onOverlayScaleXSave = settingsViewModel::saveOverlayScaleX,
            onOverlayScaleYSave = settingsViewModel::saveOverlayScaleY,
            onOverlayMessageChange = settingsViewModel::updateOverlayMessage,
            onClearOverlayMessage = settingsViewModel::clearOverlayMessage,
            onExportRecords = {
                exportLauncher.launch("records.acdata")
            },
            onImportRecords = {
                importLauncher.launch(arrayOf("application/octet-stream"))
            },
            onClearRecords = {
                weeklyPartsViewModel.clearAll()
            },
            onRequestOverlayPermission = {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    "package:${context.packageName}".toUri()
                )
                context.startActivity(intent)
            },
            onOpenLicenses = onOpenLicenses,
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}
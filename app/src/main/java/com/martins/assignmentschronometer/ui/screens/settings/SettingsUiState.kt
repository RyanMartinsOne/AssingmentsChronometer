package com.martins.assignmentschronometer.ui.screens.settings

data class SettingsUiState(
    val darkModeEnabled: Boolean = false,
    val dynamicColorsEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val autoSaveEnabled: Boolean = true,
    val overtimeAlertEnabled: Boolean = true,
    val overlayScaleX: Float = 1.0f,
    val overlayScaleY: Float = 1.0f,
    val overlaySizeMessage: String? = null
)
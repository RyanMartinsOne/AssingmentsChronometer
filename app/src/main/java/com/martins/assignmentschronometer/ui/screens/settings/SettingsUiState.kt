package com.martins.assignmentschronometer.ui.screens.settings

import com.martins.assignmentschronometer.ui.theme.ThemeMode

data class SettingsUiState(
    val dynamicColorsEnabled: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val overlayScaleX: Float = 1.0f,
    val overlayScaleY: Float = 1.0f,
    val overlayOpacity: Float = 1.0f,
    val overlaySizeMessageRes: Int? = null,
    val overlaySizeMessageArgs: List<Any> = emptyList(),
)
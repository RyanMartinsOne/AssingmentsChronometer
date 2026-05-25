package com.martins.assignmentschronometer.ui.screens.settings

data class SettingsUiState(
    val dynamicColorsEnabled: Boolean = true,
    val overlayScaleX: Float = 1.0f,
    val overlayScaleY: Float = 1.0f,
    val overlayOpacity: Float = 1.0f,
    val overlaySizeMessageRes: Int? = null,
    val overlaySizeMessageArgs: List<Any> = emptyList(),
)
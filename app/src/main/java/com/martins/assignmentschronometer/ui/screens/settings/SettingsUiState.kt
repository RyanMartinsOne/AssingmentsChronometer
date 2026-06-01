package com.martins.assignmentschronometer.ui.screens.settings

import com.martins.assignmentschronometer.ui.theme.ThemeMode

data class SettingsUiState(
    val isLoaded: Boolean = false,
    val dynamicColorsEnabled: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val showCommentCountInOverlay: Boolean = true,
    val overlayEnabled: Boolean = true,
    val isFirstLaunchDone: Boolean = false,
    val simplifiedOverlayEnabled: Boolean = false,
    val overlayScaleX: Float = 1f,
    val overlayScaleY: Float = 1f,
    val overlayOpacity: Float = 1f,
    val overlaySizeMessageRes: Int? = null,
    val overlaySizeMessageArgs: List<Any> = emptyList()
)
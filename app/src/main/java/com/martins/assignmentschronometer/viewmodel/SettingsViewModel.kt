package com.martins.assignmentschronometer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.martins.assignmentschronometer.data.repository.SettingsRepository
import com.martins.assignmentschronometer.ui.screens.settings.OverlaySizeRules
import com.martins.assignmentschronometer.ui.screens.settings.SettingsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository(application.applicationContext)
    private val overlayMessage = MutableStateFlow<String?>(null)

    val uiState = combine(
        repository.settingsFlow,
        overlayMessage
    ) { prefs, message ->
        SettingsUiState(
            darkModeEnabled = prefs.darkModeEnabled,
            dynamicColorsEnabled = prefs.dynamicColorsEnabled,
            notificationsEnabled = prefs.notificationsEnabled,
            autoSaveEnabled = prefs.autoSaveEnabled,
            overtimeAlertEnabled = prefs.overtimeAlertEnabled,
            overlayScaleX = prefs.overlayScaleX,
            overlayScaleY = prefs.overlayScaleY,
            overlaySizeMessage = message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState()
    )

    fun setDarkModeEnabled(value: Boolean) {
        viewModelScope.launch { repository.setDarkModeEnabled(value) }
    }

    fun setDynamicColorsEnabled(value: Boolean) {
        viewModelScope.launch { repository.setDynamicColorsEnabled(value) }
    }

    fun setNotificationsEnabled(value: Boolean) {
        viewModelScope.launch { repository.setNotificationsEnabled(value) }
    }

    fun setAutoSaveEnabled(value: Boolean) {
        viewModelScope.launch { repository.setAutoSaveEnabled(value) }
    }

    fun setOvertimeAlertEnabled(value: Boolean) {
        viewModelScope.launch { repository.setOvertimeAlertEnabled(value) }
    }

    fun saveOverlayScaleX(value: Float) {
        viewModelScope.launch {
            val currentHeightScale = uiState.value.overlayScaleY

            val currentHeightLevel = OverlaySizeRules.scaleToClosestLevel(
                scale = currentHeightScale,
                levels = OverlaySizeRules.heightLevels
            )

            val newWidthLevel = OverlaySizeRules.scaleToClosestLevel(
                scale = value,
                levels = OverlaySizeRules.widthLevels
            )

            val result = OverlaySizeRules.adjustHeightForNewWidth(
                currentHeightLevel = currentHeightLevel,
                newWidthLevel = newWidthLevel
            )

            val appliedWidthScale = OverlaySizeRules.widthLevels[newWidthLevel]
            val appliedHeightScale = OverlaySizeRules.heightLevels[result.appliedHeightLevel]

            repository.setOverlayScaleX(appliedWidthScale)
            repository.setOverlayScaleY(appliedHeightScale)
            overlayMessage.value = result.message
        }
    }

    fun saveOverlayScaleY(value: Float) {
        viewModelScope.launch {
            val currentWidthScale = uiState.value.overlayScaleX
            val currentWidthLevel = OverlaySizeRules.scaleToClosestLevel(
                scale = currentWidthScale,
                levels = OverlaySizeRules.widthLevels
            )

            val requestedHeightLevel = OverlaySizeRules.scaleToClosestLevel(
                scale = value,
                levels = OverlaySizeRules.heightLevels
            )

            val result = OverlaySizeRules.tryApplyHeightLevel(
                requestedHeightLevel = requestedHeightLevel,
                currentWidthLevel = currentWidthLevel
            )

            val appliedHeightScale = OverlaySizeRules.heightLevels[result.appliedHeightLevel]

            repository.setOverlayScaleY(appliedHeightScale)
            overlayMessage.value = result.message
        }
    }

    fun updateOverlayMessage(message: String?) {
        overlayMessage.value = message
    }

    fun clearOverlayMessage() {
        overlayMessage.value = null
    }
}
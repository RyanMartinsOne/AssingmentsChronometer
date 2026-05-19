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
            dynamicColorsEnabled = prefs.dynamicColorsEnabled,
            overlayScaleX = prefs.overlayScaleX,
            overlayScaleY = prefs.overlayScaleY,
            overlaySizeMessage = message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState()
    )

    fun setDynamicColorsEnabled(value: Boolean) {
        viewModelScope.launch { repository.setDynamicColorsEnabled(value) }
    }

    fun saveOverlayScaleX(value: Float) {
        viewModelScope.launch {
            val currentHeightLevel = OverlaySizeRules.scaleToClosestLevel(
                scale = uiState.value.overlayScaleY,
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
            repository.setOverlayScaleX(OverlaySizeRules.widthLevels[newWidthLevel])
            repository.setOverlayScaleY(OverlaySizeRules.heightLevels[result.appliedHeightLevel])
            overlayMessage.value = result.message
        }
    }

    fun saveOverlayScaleY(value: Float) {
        viewModelScope.launch {
            val currentWidthLevel = OverlaySizeRules.scaleToClosestLevel(
                scale = uiState.value.overlayScaleX,
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
            repository.setOverlayScaleY(OverlaySizeRules.heightLevels[result.appliedHeightLevel])
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
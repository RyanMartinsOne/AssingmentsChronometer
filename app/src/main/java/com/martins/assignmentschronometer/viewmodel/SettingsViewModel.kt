package com.martins.assignmentschronometer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.martins.assignmentschronometer.data.repository.SettingsRepository
import com.martins.assignmentschronometer.ui.screens.settings.OverlayAdjustmentResult
import com.martins.assignmentschronometer.ui.screens.settings.OverlaySizeRules
import com.martins.assignmentschronometer.ui.screens.settings.SettingsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository(application.applicationContext)

    private data class OverlayMessage(
        val resId: Int? = null,
        val args: List<Any> = emptyList()
    )

    private val overlayMessage = MutableStateFlow(OverlayMessage())

    val uiState = combine(
        repository.settingsFlow,
        overlayMessage
    ) { prefs, message ->
        SettingsUiState(
            dynamicColorsEnabled = prefs.dynamicColorsEnabled,
            overlayScaleX = prefs.overlayScaleX,
            overlayScaleY = prefs.overlayScaleY,
            overlaySizeMessageRes = message.resId,
            overlaySizeMessageArgs = message.args
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

            overlayMessage.value = OverlayMessage(result.messageRes, result.messageArgs)
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

            overlayMessage.value = OverlayMessage(result.messageRes, result.messageArgs)
        }
    }

    fun onHeightResultChanged(result: OverlayAdjustmentResult) {
        overlayMessage.value = OverlayMessage(result.messageRes, result.messageArgs)
    }

    fun clearOverlayMessage() {
        overlayMessage.value = OverlayMessage(null, emptyList())
    }
}
package com.martins.assignmentschronometer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.martins.assignmentschronometer.data.repository.SettingsRepository
import com.martins.assignmentschronometer.ui.screens.settings.OverlayAdjustmentResult
import com.martins.assignmentschronometer.ui.screens.settings.SettingsUiState
import com.martins.assignmentschronometer.ui.theme.ThemeMode
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
            themeMode = prefs.themeMode,
            showCommentCountInOverlay = prefs.showCommentCountInOverlay,
            simplifiedOverlayEnabled = prefs.simplifiedOverlayEnabled,
            overlayScaleX = prefs.overlayScaleX,
            overlayScaleY = prefs.overlayScaleY,
            overlayOpacity = prefs.overlayOpacity,
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

    fun setThemeMode(value: ThemeMode) {
        viewModelScope.launch { repository.setThemeMode(value) }
    }

    fun setShowCommentCountInOverlay(value: Boolean) {
        viewModelScope.launch {
            repository.setShowCommentCountInOverlay(value)
        }
    }

    fun setSimplifiedOverlayEnabled(value: Boolean) {
        viewModelScope.launch {
            repository.setSimplifiedOverlayEnabled(value)
        }
    }

    fun saveOverlayDimensions(widthValue: Float, heightValue: Float) {
        viewModelScope.launch {
            repository.setOverlayScaleX(widthValue)
            repository.setOverlayScaleY(heightValue)
        }
    }

    fun saveOverlayOpacity(opacity: Float) {
        viewModelScope.launch {
            repository.setOverlayOpacity(opacity)
        }
    }

    fun onHeightResultChanged(result: OverlayAdjustmentResult) {
        overlayMessage.value = OverlayMessage(result.messageRes, result.messageArgs)
    }

    fun clearOverlayMessage() {
        overlayMessage.value = OverlayMessage(null, emptyList())
    }
}
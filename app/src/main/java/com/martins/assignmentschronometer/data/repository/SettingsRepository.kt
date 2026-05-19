package com.martins.assignmentschronometer.data.repository

import android.content.Context
import android.os.Build
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "settings"
)

data class SettingsPreferences(
    val dynamicColorsEnabled: Boolean,
    val overlayScaleX: Float,
    val overlayScaleY: Float
)

class SettingsRepository(private val context: Context) {

    private object Keys {
        val DYNAMIC_COLORS = booleanPreferencesKey("dynamic_colors")
        val OVERLAY_SCALE_X = floatPreferencesKey("overlay_scale_x")
        val OVERLAY_SCALE_Y = floatPreferencesKey("overlay_scale_y")
    }

    val settingsFlow: Flow<SettingsPreferences> = context.settingsDataStore.data.map { prefs ->
        SettingsPreferences(
            dynamicColorsEnabled = prefs[Keys.DYNAMIC_COLORS]
                ?: (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S),
            overlayScaleX = prefs[Keys.OVERLAY_SCALE_X] ?: 1.0f,
            overlayScaleY = prefs[Keys.OVERLAY_SCALE_Y] ?: 1.0f
        )
    }

    suspend fun setDynamicColorsEnabled(value: Boolean) {
        context.settingsDataStore.edit { it[Keys.DYNAMIC_COLORS] = value }
    }

    suspend fun setOverlayScaleX(value: Float) {
        context.settingsDataStore.edit { it[Keys.OVERLAY_SCALE_X] = value }
    }

    suspend fun setOverlayScaleY(value: Float) {
        context.settingsDataStore.edit { it[Keys.OVERLAY_SCALE_Y] = value }
    }
}
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
    val darkModeEnabled: Boolean,
    val dynamicColorsEnabled: Boolean,
    val notificationsEnabled: Boolean,
    val autoSaveEnabled: Boolean,
    val overtimeAlertEnabled: Boolean,
    val overlayScaleX: Float,
    val overlayScaleY: Float
)

class SettingsRepository(
    private val context: Context
) {
    private object Keys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val DYNAMIC_COLORS = booleanPreferencesKey("dynamic_colors")
        val NOTIFICATIONS = booleanPreferencesKey("notifications")
        val AUTO_SAVE = booleanPreferencesKey("auto_save")
        val OVERTIME_ALERT = booleanPreferencesKey("overtime_alert")
        val OVERLAY_SCALE_X = floatPreferencesKey("overlay_scale_x")
        val OVERLAY_SCALE_Y = floatPreferencesKey("overlay_scale_y")
    }

    val settingsFlow: Flow<SettingsPreferences> = context.settingsDataStore.data.map { prefs ->
        SettingsPreferences(
            darkModeEnabled = prefs[Keys.DARK_MODE] ?: false,
            dynamicColorsEnabled = prefs[Keys.DYNAMIC_COLORS]
                ?: (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S),
            notificationsEnabled = prefs[Keys.NOTIFICATIONS] ?: true,
            autoSaveEnabled = prefs[Keys.AUTO_SAVE] ?: true,
            overtimeAlertEnabled = prefs[Keys.OVERTIME_ALERT] ?: true,
            overlayScaleX = prefs[Keys.OVERLAY_SCALE_X] ?: 1.0f,
            overlayScaleY = prefs[Keys.OVERLAY_SCALE_Y] ?: 1.0f
        )
    }

    suspend fun setDarkModeEnabled(value: Boolean) {
        context.settingsDataStore.edit { it[Keys.DARK_MODE] = value }
    }

    suspend fun setDynamicColorsEnabled(value: Boolean) {
        context.settingsDataStore.edit { it[Keys.DYNAMIC_COLORS] = value }
    }

    suspend fun setNotificationsEnabled(value: Boolean) {
        context.settingsDataStore.edit { it[Keys.NOTIFICATIONS] = value }
    }

    suspend fun setAutoSaveEnabled(value: Boolean) {
        context.settingsDataStore.edit { it[Keys.AUTO_SAVE] = value }
    }

    suspend fun setOvertimeAlertEnabled(value: Boolean) {
        context.settingsDataStore.edit { it[Keys.OVERTIME_ALERT] = value }
    }

    suspend fun setOverlayScaleX(value: Float) {
        context.settingsDataStore.edit { it[Keys.OVERLAY_SCALE_X] = value }
    }

    suspend fun setOverlayScaleY(value: Float) {
        context.settingsDataStore.edit { it[Keys.OVERLAY_SCALE_Y] = value }
    }
}
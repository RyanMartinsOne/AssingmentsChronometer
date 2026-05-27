package com.martins.assignmentschronometer.data.repository

import android.content.Context
import android.os.Build
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.martins.assignmentschronometer.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "settings"
)

data class SettingsPreferences(
    val dynamicColorsEnabled: Boolean,
    val themeMode: ThemeMode,
    val overlayScaleX: Float,
    val overlayScaleY: Float,
    val overlayOpacity: Float,
    val showCommentCountInOverlay: Boolean,
    val simplifiedOverlayEnabled: Boolean
)

class SettingsRepository(private val context: Context) {

    private object Keys {
        val DYNAMIC_COLORS = booleanPreferencesKey("dynamic_colors")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val OVERLAY_SCALE_X = floatPreferencesKey("overlay_scale_x")
        val OVERLAY_SCALE_Y = floatPreferencesKey("overlay_scale_y")
        val OVERLAY_OPACITY = floatPreferencesKey("overlay_opacity")
        val SHOW_COMMENT_COUNT_IN_OVERLAY =
            booleanPreferencesKey("show_comment_count_in_overlay")
        val SIMPLIFIED_OVERLAY_ENABLED =
            booleanPreferencesKey("simplified_overlay_enabled")
    }

    val settingsFlow: Flow<SettingsPreferences> = context.settingsDataStore.data.map { prefs ->
        val storedThemeMode = prefs[Keys.THEME_MODE]
        val themeMode = storedThemeMode
            ?.let { runCatching { ThemeMode.valueOf(it) }.getOrDefault(ThemeMode.SYSTEM) }
            ?: ThemeMode.SYSTEM

        SettingsPreferences(
            dynamicColorsEnabled = prefs[Keys.DYNAMIC_COLORS]
                ?: (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S),
            themeMode = themeMode,
            overlayScaleX = prefs[Keys.OVERLAY_SCALE_X] ?: 1.0f,
            overlayScaleY = prefs[Keys.OVERLAY_SCALE_Y] ?: 1.0f,
            overlayOpacity = prefs[Keys.OVERLAY_OPACITY] ?: 1.0f,
            showCommentCountInOverlay = prefs[Keys.SHOW_COMMENT_COUNT_IN_OVERLAY] ?: true,
            simplifiedOverlayEnabled = prefs[Keys.SIMPLIFIED_OVERLAY_ENABLED] ?: false
        )
    }

    suspend fun setDynamicColorsEnabled(value: Boolean) {
        context.settingsDataStore.edit { it[Keys.DYNAMIC_COLORS] = value }
    }

    suspend fun setThemeMode(value: ThemeMode) {
        context.settingsDataStore.edit { it[Keys.THEME_MODE] = value.name }
    }

    suspend fun setOverlayScaleX(value: Float) {
        context.settingsDataStore.edit { it[Keys.OVERLAY_SCALE_X] = value }
    }

    suspend fun setOverlayScaleY(value: Float) {
        context.settingsDataStore.edit { it[Keys.OVERLAY_SCALE_Y] = value }
    }

    suspend fun setOverlayOpacity(value: Float) {
        context.settingsDataStore.edit { it[Keys.OVERLAY_OPACITY] = value }
    }

    suspend fun setShowCommentCountInOverlay(value: Boolean) {
        context.settingsDataStore.edit {
            it[Keys.SHOW_COMMENT_COUNT_IN_OVERLAY] = value
        }
    }

    suspend fun setSimplifiedOverlayEnabled(value: Boolean) {
        context.settingsDataStore.edit {
            it[Keys.SIMPLIFIED_OVERLAY_ENABLED] = value
        }
    }
}
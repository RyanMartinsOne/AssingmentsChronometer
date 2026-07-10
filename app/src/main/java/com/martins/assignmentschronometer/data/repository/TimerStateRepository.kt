package com.martins.assignmentschronometer.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.timerStateDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "timer_state"
)

/**
 * Snapshot of a running chronometer, persisted so it can be recovered if the
 * process is killed by the system (e.g. under memory pressure) while the
 * screen is locked. [startElapsedRealtimeMillis] is measured with
 * SystemClock.elapsedRealtime(), so it is only valid until the next reboot.
 */
data class TimerState(
    val isRunning: Boolean,
    val accumulatedMillis: Long,
    val startElapsedRealtimeMillis: Long,
    val activePartUid: String?
)

class TimerStateRepository(private val context: Context) {

    private object Keys {
        val IS_RUNNING = booleanPreferencesKey("timer_is_running")
        val ACCUMULATED_MILLIS = longPreferencesKey("timer_accumulated_millis")
        val START_ELAPSED_REALTIME = longPreferencesKey("timer_start_elapsed_realtime")
        val ACTIVE_PART_UID = stringPreferencesKey("timer_active_part_uid")
    }

    val timerStateFlow: Flow<TimerState> = context.timerStateDataStore.data.map { prefs ->
        TimerState(
            isRunning = prefs[Keys.IS_RUNNING] ?: false,
            accumulatedMillis = prefs[Keys.ACCUMULATED_MILLIS] ?: 0L,
            startElapsedRealtimeMillis = prefs[Keys.START_ELAPSED_REALTIME] ?: 0L,
            activePartUid = prefs[Keys.ACTIVE_PART_UID]
        )
    }

    suspend fun saveRunning(
        accumulatedMillis: Long,
        startElapsedRealtimeMillis: Long,
        activePartUid: String?
    ) {
        context.timerStateDataStore.edit { prefs ->
            prefs[Keys.IS_RUNNING] = true
            prefs[Keys.ACCUMULATED_MILLIS] = accumulatedMillis
            prefs[Keys.START_ELAPSED_REALTIME] = startElapsedRealtimeMillis
            if (activePartUid != null) {
                prefs[Keys.ACTIVE_PART_UID] = activePartUid
            } else {
                prefs.remove(Keys.ACTIVE_PART_UID)
            }
        }
    }

    suspend fun clear() {
        context.timerStateDataStore.edit { it.clear() }
    }
}

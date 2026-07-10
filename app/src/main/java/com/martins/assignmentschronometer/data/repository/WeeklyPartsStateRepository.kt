package com.martins.assignmentschronometer.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.martins.assignmentschronometer.data.model.WeeklyPart
import com.martins.assignmentschronometer.data.model.WeeklyPartDto
import com.martins.assignmentschronometer.data.model.toDto
import com.martins.assignmentschronometer.data.model.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val Context.weeklyPartsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "weekly_parts_state"
)

@Serializable
private data class WeeklyPartsFile(
    val version: Int = 1,
    val parts: List<WeeklyPartDto>
)

/**
 * Auto-persists the in-progress weekly parts list (imported/manual entries,
 * including any recorded times) so it survives process death — not just the
 * running chronometer. This is separate from [RecordsRepository], which only
 * writes/reads when the user explicitly exports or imports a .acdata file.
 */
class WeeklyPartsStateRepository(private val context: Context) {

    private object Keys {
        val PARTS_JSON = stringPreferencesKey("weekly_parts_json")
    }

    private val json = Json { ignoreUnknownKeys = true }

    val weeklyPartsFlow: Flow<List<WeeklyPart>> = context.weeklyPartsDataStore.data.map { prefs ->
        val raw = prefs[Keys.PARTS_JSON] ?: return@map emptyList()
        try {
            json.decodeFromString<WeeklyPartsFile>(raw).parts.map { it.toModel() }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun save(parts: List<WeeklyPart>) {
        val payload = json.encodeToString(WeeklyPartsFile(parts = parts.map { it.toDto() }))
        context.weeklyPartsDataStore.edit { prefs ->
            prefs[Keys.PARTS_JSON] = payload
        }
    }
}

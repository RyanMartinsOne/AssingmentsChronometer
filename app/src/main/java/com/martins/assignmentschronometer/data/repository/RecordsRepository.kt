package com.martins.assignmentschronometer.data.repository

import android.content.Context
import android.net.Uri
import com.martins.assignmentschronometer.data.model.WeeklyPart
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// ─── Serializable mirror of WeeklyPart ───────────────────────────────────────

@Serializable
private data class WeeklyPartDto(
    val uid: String,
    val id: String,
    val title: String,
    val durationInMinutes: Int,
    val room: String,
    val assignees: String,
    val dateText: String,
    val realizedTimeOnSeconds: Int? = null
)

@Serializable
private data class AcDataFile(
    val version: Int = 1,
    val parts: List<WeeklyPartDto>
)

// ─── Result types ─────────────────────────────────────────────────────────────

sealed class ExportResult {
    object Success : ExportResult()
    object Empty : ExportResult()
    object Error : ExportResult()
}

sealed class ImportResult {
    data class Success(val count: Int) : ImportResult()
    object Invalid : ImportResult()
    object Error : ImportResult()
}

// ─── Repository ───────────────────────────────────────────────────────────────

object RecordsRepository {

    private val json = Json { prettyPrint = false; ignoreUnknownKeys = true }

    /**
     * Writes [parts] into [uri] (a writable Uri chosen by the user via SAF).
     */
    fun export(context: Context, uri: Uri, parts: List<WeeklyPart>): ExportResult {
        if (parts.isEmpty()) return ExportResult.Empty

        return try {
            val dtos = parts.map { it.toDto() }
            val payload = json.encodeToString(AcDataFile(parts = dtos))
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                stream.write(payload.toByteArray(Charsets.UTF_8))
            } ?: return ExportResult.Error
            ExportResult.Success
        } catch (e: Exception) {
            e.printStackTrace()
            ExportResult.Error
        }
    }

    /**
     * Reads [uri] and parses it back into a list of [WeeklyPart].
     */
    fun import(context: Context, uri: Uri): ImportResult {
        return try {
            val raw = context.contentResolver.openInputStream(uri)
                ?.use { it.bufferedReader().readText() }
                ?: return ImportResult.Error

            val file = json.decodeFromString<AcDataFile>(raw)
            val parts = file.parts.map { it.toModel() }
            ImportResult.Success(parts.size)
        } catch (e: kotlinx.serialization.SerializationException) {
            ImportResult.Invalid
        } catch (e: Exception) {
            e.printStackTrace()
            ImportResult.Error
        }
    }

    // ─── Mappers ─────────────────────────────────────────────────────────────

    private fun WeeklyPart.toDto() = WeeklyPartDto(
        uid = uid,
        id = id,
        title = title,
        durationInMinutes = durationInMinutes,
        room = room,
        assignees = assignees,
        dateText = dateText,
        realizedTimeOnSeconds = realizedTimeOnSeconds
    )

    private fun WeeklyPartDto.toModel() = WeeklyPart(
        uid = uid,
        id = id,
        title = title,
        durationInMinutes = durationInMinutes,
        room = room,
        assignees = assignees,
        dateText = dateText,
        realizedTimeOnSeconds = realizedTimeOnSeconds
    )
}

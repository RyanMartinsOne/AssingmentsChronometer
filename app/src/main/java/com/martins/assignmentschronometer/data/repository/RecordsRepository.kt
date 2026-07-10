package com.martins.assignmentschronometer.data.repository

import android.content.Context
import android.net.Uri
import com.martins.assignmentschronometer.data.model.WeeklyPart
import com.martins.assignmentschronometer.data.model.WeeklyPartDto
import com.martins.assignmentschronometer.data.model.toDto
import com.martins.assignmentschronometer.data.model.toModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// ─── File format ──────────────────────────────────────────────────────────────

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
    data class Success(val parts: List<WeeklyPart>) : ImportResult()
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
            val payload = json.encodeToString(AcDataFile(parts = parts.map { it.toDto() }))
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
            ImportResult.Success(file.parts.map { it.toModel() })
        } catch (e: kotlinx.serialization.SerializationException) {
            ImportResult.Invalid
        } catch (e: Throwable) {
            e.printStackTrace()
            ImportResult.Error
        }
    }
}
package com.martins.assignmentschronometer.viewmodel

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.martins.assignmentschronometer.data.model.WeeklyPart
import com.martins.assignmentschronometer.data.repository.ExportResult
import com.martins.assignmentschronometer.data.repository.ImportResult
import com.martins.assignmentschronometer.data.repository.OcrLine
import com.martins.assignmentschronometer.data.repository.PdfOcrRepository
import com.martins.assignmentschronometer.data.repository.RecordsRepository
import com.martins.assignmentschronometer.util.OcrParser
import com.martins.assignmentschronometer.util.toShareText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WeeklyPartsViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext get() = getApplication<Application>()

    // ─── State ────────────────────────────────────────────────────────────────

    var weeklyParts by mutableStateOf<List<WeeklyPart>>(emptyList())
        private set

    val groupedWeeklyParts: Map<String, List<WeeklyPart>>
        get() = weeklyParts
            .sortedBy { it.id.toIntOrNull() ?: Int.MAX_VALUE }
            .groupBy { it.dateText }

    var shareText by mutableStateOf<String?>(null)
        private set

    /** One-shot feedback for export / import operations. */
    var recordsEvent by mutableStateOf<RecordsEvent?>(null)
        private set

    var pendingNavigationToRecord by mutableStateOf(false)
        private set

    private var lastProcessedUri: Uri? = null

    fun onNavigationHandled() {
        pendingNavigationToRecord = false
    }

    // --- Shortcuts ---

    var pendingShortcutRoute by mutableStateOf<String?>(null)
        private set

    fun navigateToShortcutRoute(route: String) {
        pendingShortcutRoute = route
    }

    fun onShortcutRouteHandled() {
        pendingShortcutRoute = null
    }

    var pendingImportMediaAction by mutableStateOf(false)
        private set

    var pendingScanAction by mutableStateOf(false)
        private set

    var pendingImportAcdataAction by mutableStateOf(false)
        private set

    fun triggerImportMedia() { pendingImportMediaAction = true }
    fun onImportMediaHandled() { pendingImportMediaAction = false }

    fun triggerScan() { pendingScanAction = true }
    fun onScanHandled() { pendingScanAction = false }

    fun triggerImportAcdata() { pendingImportAcdataAction = true }
    fun onImportAcdataHandled() { pendingImportAcdataAction = false }

    // ─── Share ────────────────────────────────────────────────────────────────

    fun requestShare(part: WeeklyPart) {
        shareText = part.toShareText()
    }

    fun onShareHandled() {
        shareText = null
    }

    // ─── OCR / PDF ────────────────────────────────────────────────────────────

    fun processExtractedText(ocrLines: List<OcrLine>) {
        weeklyParts = OcrParser.parseCurrentWeek(ocrLines)
    }

    fun processPdfUri(uri: Uri) {
        viewModelScope.launch {
            val mimeType = appContext.contentResolver.getType(uri)
            if (mimeType != "application/pdf") return@launch
            val lines = PdfOcrRepository.extractLines(appContext, uri)
            weeklyParts = OcrParser.parseCurrentWeek(lines)
        }
    }

    fun processImageOcr(inputImage: com.google.mlkit.vision.common.InputImage) {
        val recognizer = com.google.mlkit.vision.text.TextRecognition.getClient(
            com.google.mlkit.vision.text.latin.TextRecognizerOptions.DEFAULT_OPTIONS
        )
        recognizer.process(inputImage)
            .addOnSuccessListener { result ->
                val ocrLines = result.textBlocks
                    .flatMap { block -> block.lines }
                    .map { line ->
                        val box = line.boundingBox
                        OcrLine(
                            text = line.text,
                            top = box?.top ?: 0,
                            left = box?.left ?: 0,
                            right = box?.right ?: 0,
                            pageIndex = 0
                        )
                    }
                processExtractedText(ocrLines)
            }
            .addOnFailureListener { it.printStackTrace() }
            .addOnCompleteListener { recognizer.close() }
    }

    // ─── CRUD ─────────────────────────────────────────────────────────────────

    fun updatePart(updated: WeeklyPart) {
        weeklyParts = weeklyParts.map {
            if (it.uid == updated.uid) updated else it
        }
    }

    fun removePart(uid: String) {
        weeklyParts = weeklyParts.filter { it.uid != uid }
    }

    fun saveManualPart(part: WeeklyPart, originalUid: String? = null) {
        val exists = weeklyParts.any { it.uid == originalUid }
        weeklyParts = if (exists) {
            weeklyParts.map { if (it.uid == originalUid) part else it }
        } else {
            weeklyParts + part
        }
    }

    fun clearAll() {
        weeklyParts = emptyList()
    }

    // ─── Export / Import ──────────────────────────────────────────────────────

    fun exportRecords(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = RecordsRepository.export(appContext, uri, weeklyParts)
            recordsEvent = when (result) {
                ExportResult.Success -> RecordsEvent.ExportSuccess
                ExportResult.Empty   -> RecordsEvent.ExportEmpty
                ExportResult.Error   -> RecordsEvent.ExportError
            }
        }
    }

    fun importRecords(uri: Uri) {
        if (uri == lastProcessedUri) return
        lastProcessedUri = uri

        viewModelScope.launch(Dispatchers.IO) {
            when (val result = RecordsRepository.import(appContext, uri)) {
                is ImportResult.Success -> {
                    val importedParts = importPartsFrom(uri)
                    if (importedParts != null) {
                        weeklyParts = importedParts
                    }
                    recordsEvent = RecordsEvent.ImportSuccess(result.count)

                    viewModelScope.launch(Dispatchers.Main) {
                        pendingNavigationToRecord = true
                    }
                }
                ImportResult.Invalid -> recordsEvent = RecordsEvent.ImportInvalid
                ImportResult.Error   -> recordsEvent = RecordsEvent.ImportError
            }
        }
    }

    private fun importPartsFrom(uri: Uri): List<WeeklyPart>? {
        return try {
            val raw = appContext.contentResolver.openInputStream(uri)
                ?.use { it.bufferedReader().readText() }
                ?: return null
            val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }

            @kotlinx.serialization.Serializable
            data class Dto(
                val uid: String,
                val id: String,
                val title: String,
                val durationInMinutes: Int,
                val room: String,
                val assignees: String,
                val dateText: String,
                val realizedTimeOnSeconds: Int? = null
            )

            @kotlinx.serialization.Serializable
            data class File(val version: Int = 1, val parts: List<Dto>)

            val file = json.decodeFromString<File>(raw)
            file.parts.map {
                WeeklyPart(
                    uid = it.uid,
                    id = it.id,
                    title = it.title,
                    durationInMinutes = it.durationInMinutes,
                    room = it.room,
                    assignees = it.assignees,
                    dateText = it.dateText,
                    realizedTimeOnSeconds = it.realizedTimeOnSeconds
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    fun onRecordsEventHandled() {
        recordsEvent = null
    }
}

sealed class RecordsEvent {
    object ExportSuccess : RecordsEvent()
    object ExportEmpty : RecordsEvent()
    object ExportError : RecordsEvent()
    data class ImportSuccess(val count: Int) : RecordsEvent()
    object ImportInvalid : RecordsEvent()
    object ImportError : RecordsEvent()
}
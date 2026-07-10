package com.martins.assignmentschronometer.viewmodel

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
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
import com.martins.assignmentschronometer.data.repository.WeeklyPartsStateRepository
import com.martins.assignmentschronometer.util.OcrParser
import com.martins.assignmentschronometer.util.toShareText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.IOException

class WeeklyPartsViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext get() = getApplication<Application>()
    private val weeklyPartsStateRepository = WeeklyPartsStateRepository(appContext)

    var weeklyParts by mutableStateOf<List<WeeklyPart>>(emptyList())
        private set

    init {
        viewModelScope.launch {
            val restored = weeklyPartsStateRepository.weeklyPartsFlow.first()
            if (restored.isNotEmpty()) {
                weeklyParts = restored
            }
        }
    }

    /**
     * Single point of mutation for [weeklyParts]: updates the in-memory state
     * and auto-persists it, so every call site (OCR import, manual add/edit,
     * clear, etc.) stays in sync without remembering to persist separately.
     */
    private fun updateWeeklyParts(newParts: List<WeeklyPart>) {
        weeklyParts = newParts
        viewModelScope.launch {
            weeklyPartsStateRepository.save(newParts)
        }
    }

    private val groupedWeeklyPartsState: State<Map<String, List<WeeklyPart>>> = derivedStateOf {
        weeklyParts
            .sortedBy { it.id.toIntOrNull() ?: Int.MAX_VALUE }
            .groupBy { it.dateText }
    }

    val groupedWeeklyParts: Map<String, List<WeeklyPart>>
        get() = groupedWeeklyPartsState.value

    var shareText by mutableStateOf<String?>(null)
        private set

    var recordsEvent by mutableStateOf<RecordsEvent?>(null)
        private set

    var uiFeedbackMessage by mutableStateOf<String?>(null)
        private set

    var pendingNavigationToRecord by mutableStateOf(false)
        private set

    private var lastProcessedUri: Uri? = null

    fun onNavigationHandled() {
        pendingNavigationToRecord = false
    }

    fun clearUiFeedbackMessage() {
        uiFeedbackMessage = null
    }

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

    fun requestShare(part: WeeklyPart) {
        shareText = part.toShareText()
    }

    fun onShareHandled() {
        shareText = null
    }

    fun processExtractedText(ocrLines: List<OcrLine>) {
        updateWeeklyParts(OcrParser.parseCurrentWeek(ocrLines))
    }

    fun processFileUri(uri: Uri) {
        viewModelScope.launch {
            val mimeType = appContext.contentResolver.getType(uri)

            when {
                mimeType == "application/pdf" -> {
                    try {
                        val lines = PdfOcrRepository.extractLines(appContext, uri)
                        updateWeeklyParts(OcrParser.parseCurrentWeek(lines))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        uiFeedbackMessage = "Erro ao processar o PDF."
                    }
                }
                mimeType?.startsWith("image/") == true -> {
                    try {
                        val inputImage =
                            com.google.mlkit.vision.common.InputImage
                                .fromFilePath(appContext, uri)
                        processImageOcr(inputImage)
                    } catch (e: IOException) {
                        e.printStackTrace()
                        uiFeedbackMessage = "Não foi possível carregar esta imagem."
                    } catch (e: Exception) {
                        e.printStackTrace()
                        uiFeedbackMessage = "Erro ao processar a imagem."
                    }
                }
                else -> {
                    uiFeedbackMessage = "Formato não suportado. Escolha um PDF ou Imagem."
                }
            }
        }
    }

    fun processCameraImage(uri: Uri) {
        try {
            val inputImage =
                com.google.mlkit.vision.common.InputImage.fromFilePath(appContext, uri)
            processImageOcr(inputImage)
        } catch (e: IOException) {
            e.printStackTrace()
            uiFeedbackMessage = "Erro ao ler a foto capturada."
        } catch (e: Exception) {
            e.printStackTrace()
            uiFeedbackMessage = "Erro ao processar a foto."
        }
    }

    private fun processImageOcr(inputImage: com.google.mlkit.vision.common.InputImage) {
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
            .addOnFailureListener {
                it.printStackTrace()
                uiFeedbackMessage = "O reconhecimento de texto falhou."
            }
            .addOnCompleteListener { recognizer.close() }
    }

    fun updatePart(updated: WeeklyPart) {
        updateWeeklyParts(
            weeklyParts.map { if (it.uid == updated.uid) updated else it }
        )
    }

    fun removePart(uid: String) {
        updateWeeklyParts(weeklyParts.filter { it.uid != uid })
    }

    fun saveManualPart(part: WeeklyPart, originalUid: String? = null) {
        val exists = weeklyParts.any { it.uid == originalUid }
        val newParts = if (exists) {
            weeklyParts.map { if (it.uid == originalUid) part else it }
        } else {
            weeklyParts + part
        }
        updateWeeklyParts(newParts)
    }

    fun clearAll() {
        updateWeeklyParts(emptyList())
    }

    fun exportRecords(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = RecordsRepository.export(appContext, uri, weeklyParts)
            recordsEvent = when (result) {
                ExportResult.Success -> RecordsEvent.ExportSuccess
                ExportResult.Empty -> RecordsEvent.ExportEmpty
                ExportResult.Error -> RecordsEvent.ExportError
            }
        }
    }

    fun importRecords(uri: Uri) {
        if (uri == lastProcessedUri) return
        lastProcessedUri = uri

        viewModelScope.launch(Dispatchers.IO) {
            when (val result = RecordsRepository.import(appContext, uri)) {
                is ImportResult.Success -> {
                    updateWeeklyParts(result.parts)
                    recordsEvent = RecordsEvent.ImportSuccess(result.parts.size)

                    viewModelScope.launch(Dispatchers.Main) {
                        pendingNavigationToRecord = true
                    }
                }
                ImportResult.Invalid -> recordsEvent = RecordsEvent.ImportInvalid
                ImportResult.Error -> recordsEvent = RecordsEvent.ImportError
            }
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
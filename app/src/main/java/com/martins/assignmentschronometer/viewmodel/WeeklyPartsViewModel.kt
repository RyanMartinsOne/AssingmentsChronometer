package com.martins.assignmentschronometer.viewmodel

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.martins.assignmentschronometer.data.model.WeeklyPart
import com.martins.assignmentschronometer.data.repository.OcrLine
import com.martins.assignmentschronometer.data.repository.PdfOcrRepository
import com.martins.assignmentschronometer.util.OcrParser
import kotlinx.coroutines.launch
import com.martins.assignmentschronometer.util.toShareText

class WeeklyPartsViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext get() = getApplication<Application>()

    var weeklyParts by mutableStateOf<List<WeeklyPart>>(emptyList())
        private set

    val groupedWeeklyParts: Map<String, List<WeeklyPart>>
        get() = weeklyParts
            .sortedBy { it.id.toIntOrNull() ?: Int.MAX_VALUE }
            .groupBy { it.dateText }

    var shareText by mutableStateOf<String?>(null)
        private set

    fun requestShare(part: WeeklyPart) {
        shareText = part.toShareText()
    }

    fun onShareHandled() {
        shareText = null
    }

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

    fun updatePart(updated: WeeklyPart) {
        weeklyParts = weeklyParts.map {
            if (it.id == updated.id) updated else it
        }
    }

    fun addManualPart(part: WeeklyPart) {
        weeklyParts = weeklyParts + part
    }

    fun removePart(uid: String) {
        weeklyParts = weeklyParts.filter { it.uid != uid }
    }

    fun saveManualPart(part: WeeklyPart, originalUid: String? = null) {
        val exists = weeklyParts.any { it.uid == originalUid }
        if (exists) {
            weeklyParts = weeklyParts.map { if (it.uid == originalUid) part else it }
        } else {
            addManualPart(part)
        }
    }
}
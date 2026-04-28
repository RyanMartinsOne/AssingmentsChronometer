package com.martins.assignmentschronometer.viewmodel

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.app.Application
import androidx.compose.runtime.derivedStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.martins.assignmentschronometer.data.model.Assignment
import com.martins.assignmentschronometer.data.model.WeeklyPart
import com.martins.assignmentschronometer.data.repository.OcrLine
import com.martins.assignmentschronometer.data.repository.PdfOcrRepository
import com.martins.assignmentschronometer.util.OcrParser
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SharedViewModel(application: Application) : AndroidViewModel(application) {


    private val appContext get() = getApplication<Application>()
    // ─── Cronômetro ───────────────────────────────────────────────
    var totalTimeOnSeconds by mutableIntStateOf(0)
        private set
    var isRunning by mutableStateOf(false)
        private set
    var isPaused by mutableStateOf(false)
        private set

    private var timerJob: Job? = null

    val commentCount: Int by derivedStateOf {
        val duration = selectedAssignment?.durationOnSeconds ?: 0
        val remaining = duration - totalTimeOnSeconds

        (remaining / 30).coerceAtLeast(0)
    }

    private val currentTargetDurationSeconds: Int
        get() {
            return when {
                activePart != null -> activePart!!.durationInMinutes * 60
                selectedAssignment != null -> selectedAssignment!!.durationOnSeconds
                else -> 0
            }
        }

    val isOverTime: Boolean
        get() {
            val target = currentTargetDurationSeconds
            return target in 1..<totalTimeOnSeconds
        }

    val formattedTime: String
        get() {
            val hours = totalTimeOnSeconds / 3600
            val minutes = totalTimeOnSeconds / 60
            val seconds = totalTimeOnSeconds % 60
            return "%02d:%02d:%02d".format(hours, minutes, seconds)
        }

    fun start() {
        if (isRunning) return
        isRunning = true
        isPaused = false
        timerJob = viewModelScope.launch {
            while (isRunning) {
                delay(1000L)
                totalTimeOnSeconds++
            }
        }
    }

    fun pause() {
        isRunning = false
        isPaused = true
        timerJob?.cancel()
    }

    fun reset() {
        isRunning = false
        isPaused = false
        totalTimeOnSeconds = 0
        timerJob?.cancel()
        activePart = null
        selectedAssignment = null
    }

    // ─── Designação ativa no cronômetro ───────────────────────────
    var activePart by mutableStateOf<WeeklyPart?>(null)
        private set

    fun selectPartForTiming(part: WeeklyPart) {
        selectedAssignment = null
        activePart = part
        reset()
    }

    fun finishPartAndSaveTime(partId: String) {
        weeklyParts = weeklyParts.map { part ->
            if (part.id == partId && part.room == activePart?.room) {
                part.copy(realizedTimeOnSeconds = totalTimeOnSeconds)
            } else part
        }
        activePart = null
        reset()
    }

    // ─── Partes semanais (PDF/OCR) ────────────────────────────────
    var weeklyParts by mutableStateOf<List<WeeklyPart>>(emptyList())
        private set

    // ViewModel agrupa por data — a Screen só consome
    val groupedWeeklyParts: Map<String, List<WeeklyPart>>
        get() = weeklyParts.groupBy { it.dateText }

    fun processExtractedText(ocrLines: List<OcrLine>) {
        weeklyParts = OcrParser.parseCurrentWeek(ocrLines)
    }

    // ─── Designação manual (AssignmentScreen) ────────────────────
    var selectedAssignment by mutableStateOf<Assignment?>(null)
        private set

    fun selectAssignment(assignment: Assignment) {
        reset()
        activePart = null
        selectedAssignment = assignment
    }

    fun processPdfUri(uri: Uri) {
        viewModelScope.launch {
            val mimeType = appContext.contentResolver.getType(uri)
            if (mimeType != "application/pdf") return@launch

            // Usa extractLines em vez de extractText
            val lines = PdfOcrRepository.extractLines(appContext, uri)

            // Log linha por linha (mantém o debug que você já tem)
            android.util.Log.d("OCR_RAW", "=== INÍCIO DO TEXTO ===")
            lines.forEachIndexed { i, line ->
                android.util.Log.d("OCR_RAW", "[$i] top=${line.top} left=${line.left} '${line.text}'")
            }
            android.util.Log.d("OCR_RAW", "=== FIM DO TEXTO ===")

            // Passa as linhas com coordenadas direto pro parser
            weeklyParts = OcrParser.parseCurrentWeek(lines)
        }
    }
}
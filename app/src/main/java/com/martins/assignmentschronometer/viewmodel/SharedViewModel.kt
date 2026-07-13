package com.martins.assignmentschronometer.viewmodel

import android.app.Application
import android.content.Intent
import android.os.SystemClock
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.martins.assignmentschronometer.data.model.Assignment
import com.martins.assignmentschronometer.data.model.WeeklyPart
import com.martins.assignmentschronometer.data.repository.TimerStateRepository
import com.martins.assignmentschronometer.service.ChronometerTimerService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SharedViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext get() = getApplication<Application>()
    private val timerStateRepository = TimerStateRepository(appContext)

    var totalTimeOnSeconds by mutableIntStateOf(0)
        private set

    var isRunning by mutableStateOf(false)
        private set

    var isPaused by mutableStateOf(false)
        private set

    private var timerJob: Job? = null

    private var startTime = 0L
    private var accumulatedTimeMillis = 0L
    private var foregroundActive = false

    var onTimerStarted: (() -> Unit)? = null

    var pendingRestoredPartUid by mutableStateOf<String?>(null)
        private set

    fun onRestoredPartHandled() {
        pendingRestoredPartUid = null
    }

    fun reattachActivePart(part: WeeklyPart) {
        activePart = part
    }

    init {
        viewModelScope.launch {
            val saved = timerStateRepository.timerStateFlow.first()
            val now = SystemClock.elapsedRealtime()

            if (saved.isRunning && saved.startElapsedRealtimeMillis in 0..now) {
                accumulatedTimeMillis = saved.accumulatedMillis
                startTime = saved.startElapsedRealtimeMillis
                pendingRestoredPartUid = saved.activePartUid
                resumeAfterRestore()
            } else if (saved.isRunning) {
                timerStateRepository.clear()
            }
        }
    }

    val commentCount: Int by derivedStateOf {
        val duration = selectedAssignment?.durationOnSeconds ?: 0
        val remaining = duration - totalTimeOnSeconds
        (remaining / 30).coerceAtLeast(0)
    }

    private val currentTargetDurationSeconds: Int
        get() = when {
            activePart != null -> activePart!!.durationInMinutes * 60
            selectedAssignment != null -> selectedAssignment!!.durationOnSeconds
            else -> 0
        }

    val isOverTime: Boolean
        get() = currentTargetDurationSeconds in 1..<totalTimeOnSeconds

    val formattedTime: String
        get() {
            val hours = totalTimeOnSeconds / 3600
            val minutes = (totalTimeOnSeconds % 3600) / 60
            val seconds = totalTimeOnSeconds % 60
            return "%02d:%02d:%02d".format(hours, minutes, seconds)
        }

    /** Início "do zero" de um novo cronômetro (nova part/assignment selecionada). */
    fun start() {
        if (isRunning) return
        isRunning = true
        isPaused = false

        onTimerStarted?.invoke()

        startTime = SystemClock.elapsedRealtime()

        foregroundActive = true
        startForegroundTimerService()
        persistRunningState()
        launchTimerLoop()
    }

    /**
     * Retoma um cronômetro pausado, preservando accumulatedTimeMillis.
     * Diferente de [start], não reinicia onTimerStarted nem trata como
     * um novo cronômetro — apenas volta a contar a partir de onde parou.
     */
    fun resume() {
        if (isRunning || !isPaused) return
        isRunning = true
        isPaused = false

        startTime = SystemClock.elapsedRealtime()

        foregroundActive = true
        startForegroundTimerService()
        persistRunningState()
        launchTimerLoop()
    }

    private fun resumeAfterRestore() {
        isRunning = true
        isPaused = false
        foregroundActive = true
        startForegroundTimerService()
        launchTimerLoop()
    }

    private fun launchTimerLoop() {
        timerJob = viewModelScope.launch {
            while (isRunning) {
                val elapsedMillis = SystemClock.elapsedRealtime() - startTime
                val totalMillis = elapsedMillis + accumulatedTimeMillis

                totalTimeOnSeconds = (totalMillis / 1000).toInt()

                delay(200L)
            }
        }
    }

    fun pause() {
        if (!isRunning) return
        isRunning = false
        isPaused = true

        accumulatedTimeMillis += SystemClock.elapsedRealtime() - startTime
        timerJob?.cancel()
        // Mantém a notificação (foreground) visível, só congela o cronômetro
        // e troca a ação para "Retomar".
        pauseForegroundTimerService()
        clearPersistedState()
    }

    fun reset() {
        val wasForegroundActive = foregroundActive

        isRunning = false
        isPaused = false
        totalTimeOnSeconds = 0
        accumulatedTimeMillis = 0L
        startTime = 0L

        timerJob?.cancel()
        timerJob = null

        activePart = null
        selectedAssignment = null

        if (wasForegroundActive) {
            foregroundActive = false
            stopForegroundTimerServiceCompletely()
        }

        clearPersistedState()
    }

    private fun clearSelection() {
        isRunning = false
        isPaused = false
        totalTimeOnSeconds = 0
        accumulatedTimeMillis = 0L
        startTime = 0L

        timerJob?.cancel()
        timerJob = null

        activePart = null
        selectedAssignment = null

        if (foregroundActive) {
            foregroundActive = false
            stopForegroundTimerServiceCompletely()
        }

        clearPersistedState()
    }

    /**
     * Zera o tempo decorrido sem fechar a notificação/foreground service.
     * Se estava rodando, continua rodando a partir de 0; se estava pausado,
     * fica parado em 0. Usado pelo botão "Reiniciar" da notificação.
     */
    fun resetTimerKeepRunning() {
        accumulatedTimeMillis = 0L
        totalTimeOnSeconds = 0

        if (isRunning) {
            startTime = SystemClock.elapsedRealtime()
            persistRunningState()
            if (foregroundActive) startForegroundTimerService()
        } else {
            startTime = 0L
            clearPersistedState()
            if (foregroundActive) pauseForegroundTimerService()
        }
    }

    fun resetTimerOnly() {
        val wasForegroundActive = foregroundActive

        isRunning = false
        isPaused = false
        totalTimeOnSeconds = 0
        accumulatedTimeMillis = 0L
        startTime = 0L

        timerJob?.cancel()
        timerJob = null

        if (wasForegroundActive) {
            foregroundActive = false
            stopForegroundTimerServiceCompletely()
        }

        clearPersistedState()
    }

    var activePart by mutableStateOf<WeeklyPart?>(null)
        private set

    var selectedAssignment by mutableStateOf<Assignment?>(null)
        private set

    fun selectPartForTiming(part: WeeklyPart) {
        clearSelection()
        activePart = part
    }

    fun selectAssignment(assignment: Assignment) {
        clearSelection()
        selectedAssignment = assignment
    }

    fun finishPart(uid: String, onSave: (WeeklyPart) -> Unit) {
        val finished = activePart?.copy(realizedTimeOnSeconds = totalTimeOnSeconds)
        if (finished != null && finished.uid == uid) {
            onSave(finished)
        }
        reset()
    }

    fun savePartTimeAndResetForOverlay(onSave: (WeeklyPart) -> Unit) {
        val finished = activePart?.copy(realizedTimeOnSeconds = totalTimeOnSeconds)
        if (finished != null) {
            onSave(finished)
        }
        reset()
    }

    // ─── Foreground service + persisted state ──────────────────────────────

    private fun startForegroundTimerService() {
        val virtualBaseElapsedRealtime = startTime - accumulatedTimeMillis
        sendServiceAction(ChronometerTimerService.ACTION_START, virtualBaseElapsedRealtime)
    }

    private fun pauseForegroundTimerService() {
        val virtualBaseElapsedRealtime = SystemClock.elapsedRealtime() - accumulatedTimeMillis
        sendServiceAction(ChronometerTimerService.ACTION_PAUSE, virtualBaseElapsedRealtime)
    }

    private fun stopForegroundTimerServiceCompletely() {
        val intent = Intent(appContext, ChronometerTimerService::class.java).apply {
            action = ChronometerTimerService.ACTION_STOP
        }
        appContext.startService(intent)
    }

    private fun sendServiceAction(actionName: String, baseElapsedRealtime: Long) {
        val intent = Intent(appContext, ChronometerTimerService::class.java).apply {
            action = actionName
            putExtra(ChronometerTimerService.EXTRA_BASE_ELAPSED_REALTIME, baseElapsedRealtime)
        }
        ContextCompat.startForegroundService(appContext, intent)
    }

    private fun persistRunningState() {
        viewModelScope.launch {
            timerStateRepository.saveRunning(
                accumulatedMillis = accumulatedTimeMillis,
                startElapsedRealtimeMillis = startTime,
                activePartUid = activePart?.uid
            )
        }
    }

    private fun clearPersistedState() {
        viewModelScope.launch {
            timerStateRepository.clear()
        }
    }
}
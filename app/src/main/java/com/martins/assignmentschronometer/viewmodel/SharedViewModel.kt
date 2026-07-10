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

    var onTimerStarted: (() -> Unit)? = null

    /**
     * uid of a [WeeklyPart] that was being timed before the process was
     * killed while running. Screens that own the parts list should observe
     * this, look the part up once loaded, call [reattachActivePart] and then
     * [onRestoredPartHandled] — the same "pending event" pattern used
     * elsewhere in this ViewModel layer.
     */
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

            // startElapsedRealtimeMillis is only meaningful within the same
            // boot session; if it doesn't fit before "now" the device was
            // rebooted (or the value is corrupt), so just discard it.
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

    fun start() {
        if (isRunning) return
        isRunning = true
        isPaused = false

        onTimerStarted?.invoke()

        startTime = SystemClock.elapsedRealtime()

        startForegroundTimerService()
        persistRunningState()
        launchTimerLoop()
    }

    private fun resumeAfterRestore() {
        isRunning = true
        isPaused = false
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
        stopForegroundTimerService()
        clearPersistedState()
    }

    fun reset() {
        isRunning = false
        isPaused = false
        totalTimeOnSeconds = 0
        accumulatedTimeMillis = 0L
        startTime = 0L
        timerJob?.cancel()
        activePart = null
        selectedAssignment = null
        stopForegroundTimerService()
        clearPersistedState()
    }

    fun resetTimerOnly() {
        isRunning = false
        isPaused = false
        totalTimeOnSeconds = 0
        accumulatedTimeMillis = 0L
        startTime = 0L
        timerJob?.cancel()
        stopForegroundTimerService()
        clearPersistedState()
    }

    var activePart by mutableStateOf<WeeklyPart?>(null)
        private set

    var selectedAssignment by mutableStateOf<Assignment?>(null)
        private set

    fun selectPartForTiming(part: WeeklyPart) {
        reset()
        activePart = part
    }

    fun selectAssignment(assignment: Assignment) {
        reset()
        activePart = null
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
        val intent = Intent(appContext, ChronometerTimerService::class.java).apply {
            action = ChronometerTimerService.ACTION_START
            putExtra(ChronometerTimerService.EXTRA_BASE_ELAPSED_REALTIME, virtualBaseElapsedRealtime)
        }
        ContextCompat.startForegroundService(appContext, intent)
    }

    private fun stopForegroundTimerService() {
        val intent = Intent(appContext, ChronometerTimerService::class.java).apply {
            action = ChronometerTimerService.ACTION_STOP
        }
        appContext.startService(intent)
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
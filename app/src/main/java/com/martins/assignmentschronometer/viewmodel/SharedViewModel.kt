package com.martins.assignmentschronometer.viewmodel

import android.app.Application
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.martins.assignmentschronometer.data.model.Assignment
import com.martins.assignmentschronometer.data.model.WeeklyPart
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SharedViewModel(application: Application) : AndroidViewModel(application) {

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

    // ─── Designação ativa ─────────────────────────────────────────
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
}
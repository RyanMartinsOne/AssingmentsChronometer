package com.martins.assignmentschronometer.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.martins.assignmentschronometer.data.model.Assignment
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SharedViewModel : ViewModel() {

    // Chronometer variables
    var totalTimeOnSeconds by mutableIntStateOf(0)
        private set

    var isRunning by mutableStateOf(false)
        private set

    var isPaused by mutableStateOf(false)
        private set

    private var job: Job? = null

    val formattedTime: String
        get() {
            val hours = totalTimeOnSeconds / 3600
            val minutes = (totalTimeOnSeconds % 3600) / 60
            val seconds = totalTimeOnSeconds % 60

            return "%02d:%02d:%02d".format(hours, minutes, seconds)
        }

    // Assignment variables
    var timeLimitOnSeconds by mutableIntStateOf(0)
        private set

    val isOverTime: Boolean
        get() = timeLimitOnSeconds in 1..totalTimeOnSeconds

    var selectAssignment by mutableStateOf<Assignment?>(null)
        private set


    // Chronometer functionalities
    fun start() {
        if (isRunning) return

        isRunning = true
        job = viewModelScope.launch {
            while (isRunning) {
                delay(1000L)
                if (isRunning) totalTimeOnSeconds++
            }
        }
    }

    fun pause(){
        isRunning = false
        isPaused = true
        job?.cancel()
    }

    fun reset(){
        pause()
        isPaused = false
        totalTimeOnSeconds = 0
        timeLimitOnSeconds = 0
        selectAssignment = null
    }

    // Assignments functionality
    fun selectAssignment (assignment: Assignment) {
        reset()
        selectAssignment = assignment
        timeLimitOnSeconds = assignment.durationOnSeconds
    }
}

package com.martins.assignmentschronometer.ui.screens.chronometer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

class ChronometerViewModel : ViewModel() {

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

            return String.format(
                Locale.getDefault(),
                "%02d:%02d:%02d",
                hours, minutes, seconds)
        }

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
    }
}


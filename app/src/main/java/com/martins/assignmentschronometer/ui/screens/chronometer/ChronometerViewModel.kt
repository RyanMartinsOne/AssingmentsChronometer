package com.martins.assignmentschronometer.ui.screens.chronometer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ChronometerViewModel : ViewModel() {

    var totalTimeOnSeconds by mutableIntStateOf(0)
        private set

    var isRunning by mutableStateOf(false)
        private set

    fun start() {
        if (isRunning) return

        isRunning = true
        viewModelScope.launch {
            while (isRunning) {
                delay(1000L)
                if (isRunning) totalTimeOnSeconds++
            }
        }
    }

    fun pause(){
        isRunning = false
    }

    fun reset(){
        pause()
        totalTimeOnSeconds = 0
    }
}


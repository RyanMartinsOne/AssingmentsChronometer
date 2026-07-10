package com.martins.assignmentschronometer.util

import com.martins.assignmentschronometer.data.model.WeeklyPart

fun WeeklyPart.toShareText(): String {
    val totalSec = realizedTimeOnSeconds ?: 0
    val time = "Decorrido: "+"%02d:%02d".format(totalSec / 60, totalSec % 60)
    val expected = "Previsto: $durationInMinutes min"
    val delayInfo = delayText?.let { "Atraso: *$it*" } ?: ""

    return """
        $title
        *$assignees*
        $room
        $expected
        *$time*
        $delayInfo
    """.trimIndent()
}
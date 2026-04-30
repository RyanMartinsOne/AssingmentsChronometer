package com.martins.assignmentschronometer.util

import com.martins.assignmentschronometer.data.model.WeeklyPart

fun WeeklyPart.toShareText(): String {
    val totalSec = realizedTimeOnSeconds ?: 0
    val time = "%02d:%02d".format(totalSec / 60, totalSec % 60)
    val delayInfo = delayText?.let { "Atraso: *$it*" } ?: ""

    return """
        $title
        *$assignees*
        $room
        *$time*
        $delayInfo
    """.trimIndent()
}
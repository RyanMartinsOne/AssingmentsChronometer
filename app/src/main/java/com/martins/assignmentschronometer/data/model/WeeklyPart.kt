package com.martins.assignmentschronometer.data.model

data class WeeklyPart(
    val id: String,
    val title: String,
    val durationInMinutes: Int,
    val room: String,
    val assignees: String,
    val dateText: String = "Data desconhecida",
    val realizedTimeOnSeconds: Int? = null
    ) {
        val isDelayed: Boolean
            get() = realizedTimeOnSeconds != null && realizedTimeOnSeconds > (durationInMinutes * 60)

        val delayText: String?
            get() {
                if (!isDelayed || realizedTimeOnSeconds == null) return null
                val diff = realizedTimeOnSeconds - (durationInMinutes * 60)
                val min = diff / 60
                val sec = diff % 60
                return "+%02d:%02d".format(min, sec)
            }
    }
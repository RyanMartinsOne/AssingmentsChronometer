package com.martins.assignmentschronometer.data.model

data class WeeklyPart(
    val id: String,
    val title: String,
    val durationInMinutes: Int,
    val room: String,
    val assignees: String,
    val dateText: String = "Data desconhecida",
    val realizedTimeOnSeconds: Int? = null
)
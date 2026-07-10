package com.martins.assignmentschronometer.data.model

import kotlinx.serialization.Serializable

/**
 * Serializable mirror of [WeeklyPart]. Shared by every persistence path
 * (auto-saved app state and user-triggered .acdata export/import) so the
 * mapping logic only lives in one place.
 */
@Serializable
data class WeeklyPartDto(
    val uid: String,
    val id: String,
    val title: String,
    val durationInMinutes: Int,
    val room: String,
    val assignees: String,
    val dateText: String,
    val realizedTimeOnSeconds: Int? = null
)

fun WeeklyPart.toDto() = WeeklyPartDto(
    uid = uid,
    id = id,
    title = title,
    durationInMinutes = durationInMinutes,
    room = room,
    assignees = assignees,
    dateText = dateText,
    realizedTimeOnSeconds = realizedTimeOnSeconds
)

fun WeeklyPartDto.toModel() = WeeklyPart(
    uid = uid,
    id = id,
    title = title,
    durationInMinutes = durationInMinutes,
    room = room,
    assignees = assignees,
    dateText = dateText,
    realizedTimeOnSeconds = realizedTimeOnSeconds
)

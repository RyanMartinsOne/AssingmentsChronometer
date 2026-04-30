package com.martins.assignmentschronometer.util

import java.time.LocalDate

object DateUtils {

    fun parseOcrDate(dateText: String): LocalDate? {
        try {

            val regex = Regex("""(\d+)\s+de\s+([a-zA-Z]+)""")
            val match = regex.find(dateText.lowercase())

            if (match != null) {
                val dayStr = match.groupValues[1]
                val monthStr = match.groupValues[2]

                val day = dayStr.toInt()

                val month = when {
                    monthStr.startsWith("jan") -> 1
                    monthStr.startsWith("fev") -> 2
                    monthStr.startsWith("mar") -> 3
                    monthStr.startsWith("abr") -> 4
                    monthStr.startsWith("mai") -> 5
                    monthStr.startsWith("jun") -> 6
                    monthStr.startsWith("jul") -> 7
                    monthStr.startsWith("ago") -> 8
                    monthStr.startsWith("set") -> 9
                    monthStr.startsWith("out") -> 10
                    monthStr.startsWith("nov") -> 11
                    monthStr.startsWith("dez") -> 12
                    else -> return null
                }

                val currentYear = LocalDate.now().year
                val currentMonth = LocalDate.now().monthValue

                val year = when (currentMonth) {
                    12 if month == 1 -> currentYear + 1
                    1 if month == 12 -> currentYear - 1
                    else -> currentYear
                }

                return LocalDate.of(year, month, day)
            }

            if (dateText.contains("/")) {
                val parts = dateText.split("/")
                if (parts.size >= 2) {
                    val day = parts[0].replace(Regex("\\D"), "").toIntOrNull() ?: return null
                    val month = parts[1].replace(Regex("\\D"), "").toIntOrNull() ?: return null
                    return LocalDate.of(LocalDate.now().year, month, day)
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    fun formatOcrDate(date: LocalDate): String {
        val month = when (date.monthValue) {
            1 -> "janeiro"; 2 -> "fevereiro"; 3 -> "março"
            4 -> "abril"; 5 -> "maio"; 6 -> "junho"
            7 -> "julho"; 8 -> "agosto"; 9 -> "setembro"
            10 -> "outubro"; 11 -> "novembro"; 12 -> "dezembro"
            else -> ""
        }
        return "${date.dayOfMonth} de $month de ${date.year}"
    }

    fun nextMeetingDate(): String {
        val today = LocalDate.now()
        val daysUntilThursday = (4 - today.dayOfWeek.value + 7) % 7
        val thursday = if (daysUntilThursday == 0) today else today.plusDays(daysUntilThursday.toLong())
        return formatOcrDate(thursday)
    }
}
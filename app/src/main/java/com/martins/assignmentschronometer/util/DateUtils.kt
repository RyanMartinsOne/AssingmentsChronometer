    // util/DateUtils.kt
    package com.martins.assignmentschronometer.util

    import java.time.LocalDate
    import java.time.format.DateTimeFormatter
    import java.util.Locale

    object DateUtils {

        fun parseOcrDate(dateText: String): LocalDate? {
            try {

                // Regex para capturar o último dia do intervalo e o mês
                // Pega o número antes do "de" e a palavra depois do "de"
                val regex = Regex("""(\d+)\s+de\s+([a-zA-Z]+)""")
                val match = regex.find(dateText.lowercase())

                if (match != null) {
                    val dayStr = match.groupValues[1]
                    val monthStr = match.groupValues[2]

                    val day = dayStr.toInt()

                    // Mapeia os meses abreviados ou completos para número
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

                    // Pega o ano atual (se estivermos em dezembro e a data for janeiro, considera ano que vem)
                    val currentYear = LocalDate.now().year
                    val currentMonth = LocalDate.now().monthValue

                    val year = if (currentMonth == 12 && month == 1) currentYear + 1
                    else if (currentMonth == 1 && month == 12) currentYear - 1
                    else currentYear

                    return LocalDate.of(year, month, day)
                }

                // Se o OCR vier diferente (ex: "19/01"), tenta fallback simples
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
    }
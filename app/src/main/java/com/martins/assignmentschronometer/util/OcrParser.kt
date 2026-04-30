package com.martins.assignmentschronometer.util

import com.martins.assignmentschronometer.data.model.WeeklyPart
import com.martins.assignmentschronometer.data.repository.OcrLine
import java.time.LocalDate
import kotlin.math.abs

object OcrParser {

    private val partDetailRegex = Regex("""^(\d+)\.\s*(.+?)\s*\((\d+)\s*min\)""")
    private val dateRegex = Regex("""(\d{1,2}\s+de\s+\w+\s+de\s+\d{4})""", RegexOption.IGNORE_CASE)

    private val blackList = listOf(
        "Cântico", "Oração", "Comentários", "TESOUROS", "FAÇA", "NOSSA", "REUNIÃO",
        "Presidente", "Dirigente", "ISAÍAS", "Minutos", "Estudo", "Joias", "Leitura",
        "Iniciando", "Cultivando", "Explicando", "Fazendo", "Discurso", "Boletim"
    )

    fun parseCurrentWeek(
        ocrLines: List<OcrLine>,
        today: LocalDate = LocalDate.now()
    ): List<WeeklyPart> {
        val allParts = parseWithLines(ocrLines)
        if (allParts.isEmpty()) return emptyList()

        val dateMap = LinkedHashMap<String, LocalDate>()
        allParts.forEach { part ->
            if (!dateMap.containsKey(part.dateText)) {
                DateUtils.parseOcrDate(part.dateText)?.let { dateMap[part.dateText] = it }
            }
        }

        val targetDateText = dateMap.entries
            .sortedBy { it.value }
            .firstOrNull { !it.value.isBefore(today) }
            ?.key
            ?: dateMap.entries.maxByOrNull { it.value }?.key
            ?: allParts.first().dateText

        return allParts.filter { it.dateText == targetDateText }
    }

    fun parseWithLines(ocrLines: List<OcrLine>): List<WeeklyPart> {
        if (ocrLines.isEmpty()) return emptyList()

        // 1. Agrupamento em Linhas Visuais (Tolerance de 15 pixels)
        val sortedByTop = ocrLines.sortedBy { it.top }
        val rows = mutableListOf<List<OcrLine>>()
        var currentRow = mutableListOf<OcrLine>()

        for (line in sortedByTop) {
            if (currentRow.isEmpty() || abs(line.top - currentRow.first().top) <= 15) {
                currentRow.add(line)
            } else {
                rows.add(currentRow.sortedBy { it.left })
                currentRow = mutableListOf(line)
            }
        }
        if (currentRow.isNotEmpty()) rows.add(currentRow.sortedBy { it.left })

        val dateByTop = buildDateByTop(sortedByTop)
        val results = mutableListOf<WeeklyPart>()

        var lastPartId = ""
        var lastPartTitle = ""
        var lastPartTime = 0
        var currentRoom = "Principal"

        for (row in rows) {
            val date = dateByTop(row.first().top)
            val rowFullText = row.joinToString(" ") { it.text }

            val partMatch = partDetailRegex.find(rowFullText)
            if (partMatch != null) {
                lastPartId = partMatch.groupValues[1]
                lastPartTitle = partMatch.groupValues[2].trim()
                lastPartTime = partMatch.groupValues[3].toIntOrNull() ?: 0
                currentRoom = "Principal"
            }

            if (blackList.any { rowFullText.contains(it, ignoreCase = true) && (it == "Cântico" || it == "Oração") }) {
                lastPartId = ""
            }

            if (lastPartId.isEmpty()) continue

            for (item in row) {
                val txt = item.text.trim()

                if (txt.contains("Sala B", true)) currentRoom = "Sala B"
                else if (txt.contains("Salão Principal", true) || txt.contains("Sala Principal", true)) currentRoom = "Principal"

                // Lógica de Detecção de Nome:
                // 1. Deve estar à direita (left > 500) OU ser um bloco que sobrou após remover a Sala
                // 2. Não pode estar na lista negra
                // 3. Não pode ser o próprio título da parte

                val potentialName = txt
                    .replace("Salão Principal", "", ignoreCase = true)
                    .replace("Sala Principal", "", ignoreCase = true)
                    .replace("Sala B", "", ignoreCase = true)
                    .replace(Regex("""^\d+\."""), "") // Remove "1." se vier grudado
                    .trim()

                if (potentialName.length > 3 &&
                    !blackList.any { potentialName.contains(it, ignoreCase = true) } &&
                    !potentialName.contains(lastPartTitle, ignoreCase = true)) {

                    results.add(
                        WeeklyPart(
                            id = lastPartId,
                            title = lastPartTitle,
                            durationInMinutes = lastPartTime,
                            room = currentRoom,
                            assignees = potentialName,
                            dateText = date
                        )
                    )
                }
            }
        }
        return results
    }

    private fun buildDateByTop(sortedLines: List<OcrLine>): (Int) -> String {
        val datePoints = sortedLines.mapNotNull { line ->
            dateRegex.find(line.text)?.let { m ->
                line.top to m.groupValues[1].replaceFirstChar { it.uppercase() }
            }
        }
        return { top: Int ->
            datePoints.lastOrNull { it.first <= top }?.second ?: "Data desconhecida"
        }
    }
}
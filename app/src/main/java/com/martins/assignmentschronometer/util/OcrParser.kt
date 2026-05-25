package com.martins.assignmentschronometer.util

import com.martins.assignmentschronometer.data.model.WeeklyPart
import com.martins.assignmentschronometer.data.repository.OcrLine
import java.time.LocalDate
import kotlin.math.abs

object OcrParser {

    private val partDetailRegex = Regex("""^(?:(\d+)\.\s*)?(.+?)\s*\((\d+)\s*min\)""")
    private val dateRegex = Regex("""(\d{1,2}\s+de\s+\w+\s+de\s+\d{4})""", RegexOption.IGNORE_CASE)
    private val STRUCTURAL_TERMS = setOf(
        // ── Seções do programa midweek ────────────────────────────────────────
        "Cântico", "Oração", "Comentários", "Presidente", "Dirigente",
        "Minutos", "Estudo", "Joias",
        "TESOUROS", "FAÇA", "NOSSA", "REUNIÃO",
        "Tesouros da Palavra", "Joias Espirituais",
        "Nossa Vida Cristã", "Faça Seu Melhor",
        "Vida e Ministério",
        // ── Seções do programa de fim de semana ───────────────────────────────
        "Nossa Reunião", "Discurso Público", "A Sentinela",
        "Boletim", "Anúncios", "Informações",
        // ── Verbos de instrução nas partes de campo ───────────────────────────
        "Leitura", "Iniciando", "Cultivando", "Explicando",
        "Fazendo", "Discurso", "Revisão"
    )

    private fun isStructural(text: String): Boolean {
        val normalized = text.trim()

        if (normalized.contains(Regex("""\d+[:-\u2013\u2014]\d+"""))) return true

        if (normalized.matches(Regex("""^\d+\..+"""))) return true

        return STRUCTURAL_TERMS.any { term ->
            normalized.contains(term, ignoreCase = true)
        }
    }

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

        val pageWidth = ocrLines.maxOfOrNull { it.right } ?: 1000
        val nameSideThreshold = pageWidth / 2

        val dateByTop = buildDateByTop(sortedByTop)
        val results = mutableListOf<WeeklyPart>()

        var lastPartId    = ""
        var lastPartTitle = ""
        var lastPartTime  = 0
        var currentRoom   = "Principal"

        for (row in rows) {
            val date       = dateByTop(row.first().top)
            val rowFullText = row.joinToString(" ") { it.text }

            val partMatch = partDetailRegex.find(rowFullText)
            if (partMatch != null) {
                val rawId = partMatch.groupValues[1]
                lastPartId    = rawId.ifEmpty { "N/A" }
                lastPartTitle = partMatch.groupValues[2].trim()
                lastPartTime  = partMatch.groupValues[3].toIntOrNull() ?: 0
                currentRoom   = "Principal"
            }

            if (rowFullText.contains("Cântico", ignoreCase = true) ||
                rowFullText.contains("Oração", ignoreCase = true)) {
                lastPartId = ""
                continue
            }

            if (lastPartId.isEmpty()) continue

            if (rowFullText.contains("Sala B", ignoreCase = true)) {
                currentRoom = "Sala B"
            } else if (rowFullText.contains("Salão Principal", ignoreCase = true) ||
                rowFullText.contains("Sala Principal", ignoreCase = true)) {
                currentRoom = "Principal"
            }

            for (item in row) {
                val txt = item.text.trim()

                var cleaned = txt
                    .replace("Salão Principal", "", ignoreCase = true)
                    .replace("Sala Principal", "", ignoreCase = true)
                    .replace("Sala B", "", ignoreCase = true)
                    .replace(Regex("""^\d+\."""), "")
                    .trim()

                cleaned = cleaned.replace(Regex("""\s+I(?=[A-Z])"""), " / ")

                cleaned = cleaned.replace(Regex("""\s*/\s*"""), " / ").trim()

                var itemRoom = currentRoom
                if (txt.contains("Sala B", ignoreCase = true)) {
                    itemRoom = "Sala B"
                } else if (txt.contains("Salão Principal", ignoreCase = true) || txt.contains("Sala Principal", ignoreCase = true)) {
                    itemRoom = "Principal"
                }

                val itemCenter        = (item.left + item.right) / 2
                val isOnRightSide     = itemCenter >= nameSideThreshold

                val isLongEnough      = cleaned.length > 3
                val isNotStructural   = !isStructural(cleaned)
                val isNotPartTitle    = !cleaned.contains(lastPartTitle, ignoreCase = true) &&
                        !lastPartTitle.contains(cleaned, ignoreCase = true)


                if (isOnRightSide && isLongEnough && isNotStructural && isNotPartTitle) {
                    results.add(
                        WeeklyPart(
                            id              = lastPartId,
                            title           = lastPartTitle,
                            durationInMinutes = lastPartTime,
                            room            = itemRoom,
                            assignees       = cleaned,
                            dateText        = date
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
package com.martins.assignmentschronometer

import com.martins.assignmentschronometer.data.repository.OcrLine
import com.martins.assignmentschronometer.util.OcrParser
import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDate

class WeeklyPartParserTest {

    // Função auxiliar ajustada para os campos REAIS da sua OcrLine
    private fun createLine(text: String, top: Int, left: Int = 0): OcrLine {
        return OcrLine(
            text = text,
            top = top,
            left = left,
            right = left + 100,
            pageIndex = 0
        )
    }

    @Test
    fun `should parse week correctly with full date format`() {
        // Setup: Hoje é 01/05, OCR tem data de 05/05
        val mockToday = LocalDate.of(2024, 5, 1)
        val dateText = "05 de Maio de 2024"

        val ocrLines = listOf(
            createLine(dateText, top = 10),
            // O regex do seu parser espera: "1. Título (5 min)"
            createLine("1. Bible Reading (5 min)", top = 50, left = 10),
            // Seu parser busca nomes com left > 500
            createLine("John Doe", top = 50, left = 600)
        )

        // Action
        val result = OcrParser.parseCurrentWeek(ocrLines, today = mockToday)

        // Assert
        assertFalse("O resultado não deve ser vazio", result.isEmpty())
        assertEquals(dateText, result.first().dateText)
        assertEquals("John Doe", result.first().assignees)
    }

    @Test
    fun `should return empty list when OCR has no matches`() {
        val result = OcrParser.parseCurrentWeek(emptyList(), today = LocalDate.now())
        assertTrue(result.isEmpty())
    }
}

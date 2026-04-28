package com.martins.assignmentschronometer.data.repository

import android.content.Context
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import androidx.core.graphics.createBitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

data class OcrLine(
    val text: String,
    val top: Int,
    val left: Int,
    val right: Int,
    val pageIndex: Int
)

object PdfOcrRepository {

    suspend fun extractLines(context: Context, uri: Uri): List<OcrLine> {
        val allLines = mutableListOf<OcrLine>()
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
            val renderer = PdfRenderer(pfd)
            var pageOffsetY = 0

            for (i in 0 until renderer.pageCount) {
                val page = renderer.openPage(i)
                val bitmap = createBitmap(page.width * 2, page.height * 2)
                bitmap.eraseColor(Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()

                val image = InputImage.fromBitmap(bitmap, 0)
                val result = recognizer.process(image).await()

                for (block in result.textBlocks) {
                    for (line in block.lines) {
                        val box = line.boundingBox ?: continue
                        allLines.add(
                            OcrLine(
                                text      = line.text.trim(),
                                top       = box.top + pageOffsetY,
                                left      = box.left,
                                right     = box.right,
                                pageIndex = i
                            )
                        )
                    }
                }

                pageOffsetY += bitmap.height
            }

            renderer.close()
        }

        return allLines.sortedBy { it.top }
    }

    // Mantém compatibilidade com quem ainda usa extractText
    suspend fun extractText(context: Context, uri: Uri): String {
        return extractLines(context, uri).joinToString("\n") { it.text }
    }
}
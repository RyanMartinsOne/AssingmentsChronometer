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

    private const val RENDER_DPI = 300
    private const val POINTS_PER_INCH = 72f

    suspend fun extractLines(context: Context, uri: Uri): List<OcrLine> {
        val allLines = mutableListOf<OcrLine>()
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.use { recognizer ->
            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                PdfRenderer(pfd).use { renderer ->
                    var pageOffsetY = 0

                    val scale = RENDER_DPI / POINTS_PER_INCH

                    for (i in 0 until renderer.pageCount) {
                        val page = renderer.openPage(i)

                        val bitmapWidth = (page.width * scale).toInt()
                        val bitmapHeight = (page.height * scale).toInt()
                        val bitmap = createBitmap(bitmapWidth, bitmapHeight)

                        try {
                            bitmap.eraseColor(Color.WHITE)

                            page.render(
                                bitmap,
                                null,
                                null,
                                PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                            )

                            val image = InputImage.fromBitmap(bitmap, 0)
                            val result = recognizer.process(image).await()

                            for (block in result.textBlocks) {
                                for (line in block.lines) {
                                    val box = line.boundingBox ?: continue
                                    allLines.add(
                                        OcrLine(
                                            text = line.text.trim(),
                                            top = box.top + pageOffsetY,
                                            left = box.left,
                                            right = box.right,
                                            pageIndex = i
                                        )
                                    )
                                }
                            }

                            pageOffsetY += bitmapHeight
                        } finally {
                            page.close()
                            bitmap.recycle()
                        }
                    }
                }
            }
        }

        return allLines.sortedBy { it.top }
    }
}
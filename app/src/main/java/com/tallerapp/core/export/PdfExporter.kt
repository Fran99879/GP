package com.tallerapp.core.export

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

/**
 * Genera un PDF del reporte usando la API nativa de Android (sin dependencias externas).
 * El reporte del MVP entra en una sola página A4.
 */
object PdfExporter {

    fun generar(context: Context, secciones: List<SeccionReporte>): Uri {
        val documento = PdfDocument()
        // A4 en puntos (72 dpi): 595 x 842.
        val pagina = documento.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
        val canvas = pagina.canvas

        val titulo = Paint().apply { textSize = 18f; isFakeBoldText = true }
        val seccion = Paint().apply { textSize = 14f; isFakeBoldText = true }
        val texto = Paint().apply { textSize = 12f }

        var y = 40f
        canvas.drawText("TallerApp — Reporte", 40f, y, titulo)
        y += 30f

        secciones.forEach { sec ->
            canvas.drawText(sec.titulo, 40f, y, seccion)
            y += 20f
            sec.filas.forEach { (etiqueta, valor) ->
                canvas.drawText(etiqueta, 50f, y, texto)
                canvas.drawText(valor, 360f, y, texto)
                y += 18f
            }
            y += 12f
        }

        documento.finishPage(pagina)

        val archivo = File(context.cacheDir, "exports").apply { mkdirs() }
            .let { File(it, "reporte.pdf") }
        FileOutputStream(archivo).use { documento.writeTo(it) }
        documento.close()

        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
    }
}

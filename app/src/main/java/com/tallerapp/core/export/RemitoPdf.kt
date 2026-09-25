package com.tallerapp.core.export

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.tallerapp.core.util.Dinero
import com.tallerapp.core.util.Fechas
import java.io.File
import java.io.FileOutputStream

/** Un ítem del remito: descripción, cantidad y precio unitario. */
data class ItemRemito(
    val descripcion: String,
    val cantidad: Double,
    val precioCentavos: Long,
) {
    val subtotalCentavos: Long get() = Math.round(cantidad * precioCentavos)
}

/** Datos de un remito para imprimir. */
data class Remito(
    val negocio: String,
    val cliente: String,
    val numero: String,
    val fechaMillis: Long,
    val items: List<ItemRemito>,
) {
    val totalCentavos: Long get() = items.sumOf { it.subtotalCentavos }
}

/**
 * Genera el PDF de un remito con la API nativa de Android (sin dependencias externas),
 * replicando el remito de la app de escritorio (QuestPDF).
 */
object RemitoPdf {

    fun generar(context: Context, remito: Remito): Uri {
        val documento = PdfDocument()
        val pagina = documento.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
        val canvas = pagina.canvas

        val titulo = Paint().apply { textSize = 22f; isFakeBoldText = true }
        val subtitulo = Paint().apply { textSize = 12f; color = 0xFF6B6256.toInt() }
        val encabezado = Paint().apply { textSize = 12f; isFakeBoldText = true }
        val texto = Paint().apply { textSize = 12f }
        val totalPaint = Paint().apply { textSize = 16f; isFakeBoldText = true }
        val linea = Paint().apply { strokeWidth = 1f; color = 0xFFE2DACB.toInt() }

        // Columnas: descripción | cantidad | precio | subtotal
        val xDesc = 40f
        val xCant = 330f
        val xPrecio = 400f
        val xSub = 500f

        var y = 50f
        canvas.drawText("REMITO", xDesc, y, titulo)
        if (remito.numero.isNotBlank()) {
            canvas.drawText("N° ${remito.numero}", xSub, y, encabezado)
        }
        y += 20f
        canvas.drawText(remito.negocio, xDesc, y, subtitulo)
        y += 16f
        canvas.drawText("Fecha: ${Fechas.formatear(remito.fechaMillis)}", xDesc, y, subtitulo)
        y += 26f

        canvas.drawText("Cliente: ${remito.cliente}", xDesc, y, encabezado)
        y += 24f

        canvas.drawLine(xDesc, y, 555f, y, linea)
        y += 16f
        canvas.drawText("Descripción", xDesc, y, encabezado)
        canvas.drawText("Cant.", xCant, y, encabezado)
        canvas.drawText("P. unit.", xPrecio, y, encabezado)
        canvas.drawText("Subtotal", xSub, y, encabezado)
        y += 8f
        canvas.drawLine(xDesc, y, 555f, y, linea)
        y += 18f

        remito.items.forEach { item ->
            canvas.drawText(item.descripcion.take(48), xDesc, y, texto)
            canvas.drawText(formatearCantidad(item.cantidad), xCant, y, texto)
            canvas.drawText(Dinero.formatear(item.precioCentavos), xPrecio, y, texto)
            canvas.drawText(Dinero.formatear(item.subtotalCentavos), xSub, y, texto)
            y += 18f
        }

        y += 6f
        canvas.drawLine(xDesc, y, 555f, y, linea)
        y += 24f
        canvas.drawText("TOTAL", xPrecio, y, totalPaint)
        canvas.drawText(Dinero.formatear(remito.totalCentavos), xSub, y, totalPaint)

        documento.finishPage(pagina)

        val archivo = File(context.cacheDir, "exports").apply { mkdirs() }
            .let { File(it, "remito.pdf") }
        FileOutputStream(archivo).use { documento.writeTo(it) }
        documento.close()

        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
    }

    /** Muestra "2" en vez de "2.0", pero conserva decimales cuando los hay. */
    private fun formatearCantidad(c: Double): String =
        if (c == Math.floor(c)) c.toLong().toString() else c.toString()
}

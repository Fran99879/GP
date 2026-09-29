package com.tallerapp.core.export

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.tallerapp.core.util.Dinero
import com.tallerapp.core.util.Fechas
import com.tallerapp.domain.model.Factura
import java.io.File
import java.io.FileOutputStream

/**
 * PDF de una factura, con la API nativa de Android (sin dependencias externas), en la misma
 * línea visual que [RemitoPdf].
 *
 * Es un **comprobante interno**: lo dice al pie, porque no lleva CAE ni punto de venta y no
 * reemplaza una factura electrónica de ARCA/AFIP.
 *
 * A diferencia del remito, pagina: un catálogo grande puede dar veinte líneas o doscientas,
 * y las que no entran van a la hoja siguiente con el encabezado repetido.
 */
object FacturaPdf {

    private const val ANCHO = 595
    private const val ALTO = 842
    private const val MARGEN_INFERIOR = 96f

    fun generar(context: Context, factura: Factura, negocio: String): Uri {
        val documento = PdfDocument()

        val titulo = Paint().apply { textSize = 22f; isFakeBoldText = true }
        val subtitulo = Paint().apply { textSize = 12f; color = 0xFF6B6256.toInt() }
        val encabezado = Paint().apply { textSize = 11f; isFakeBoldText = true }
        val texto = Paint().apply { textSize = 11f }
        val totalPaint = Paint().apply { textSize = 16f; isFakeBoldText = true }
        val linea = Paint().apply { strokeWidth = 1f; color = 0xFFE2DACB.toInt() }
        val pie = Paint().apply { textSize = 9f; color = 0xFF6B6256.toInt() }

        // Columnas: descripción | cantidad | precio unitario | descuento | subtotal
        val xDesc = 40f
        val xCant = 300f
        val xPrecio = 360f
        val xDto = 440f
        val xSub = 490f
        val xFin = 555f

        var numeroPagina = 1
        var pagina = documento.startPage(PdfDocument.PageInfo.Builder(ANCHO, ALTO, numeroPagina).create())
        var canvas = pagina.canvas
        var y = 50f

        fun encabezadoTabla() {
            canvas.drawLine(xDesc, y, xFin, y, linea)
            y += 15f
            canvas.drawText("Descripción", xDesc, y, encabezado)
            canvas.drawText("Cant.", xCant, y, encabezado)
            canvas.drawText("P. unit.", xPrecio, y, encabezado)
            canvas.drawText("Dto.", xDto, y, encabezado)
            canvas.drawText("Subtotal", xSub, y, encabezado)
            y += 8f
            canvas.drawLine(xDesc, y, xFin, y, linea)
            y += 16f
        }

        // Encabezado del documento (solo en la primera hoja).
        canvas.drawText("FACTURA", xDesc, y, titulo)
        if (factura.numero.isNotBlank()) {
            canvas.drawText("N° ${factura.numero}", xSub, y, encabezado)
        }
        y += 20f
        canvas.drawText(negocio, xDesc, y, subtitulo)
        y += 16f
        canvas.drawText("Fecha: ${Fechas.formatear(factura.fecha)}", xDesc, y, subtitulo)
        y += 26f
        canvas.drawText("Cliente: ${factura.cliente}", xDesc, y, encabezado)
        y += 16f
        if (factura.documento.isNotBlank()) {
            canvas.drawText("Documento: ${factura.documento}", xDesc, y, texto)
            y += 16f
        }
        y += 8f
        encabezadoTabla()

        factura.items.forEach { item ->
            // Salto de página: se cierra la hoja y se repite el encabezado de la tabla.
            if (y > ALTO - MARGEN_INFERIOR) {
                documento.finishPage(pagina)
                numeroPagina++
                pagina = documento.startPage(
                    PdfDocument.PageInfo.Builder(ANCHO, ALTO, numeroPagina).create(),
                )
                canvas = pagina.canvas
                y = 50f
                canvas.drawText("FACTURA N° ${factura.numero} (hoja $numeroPagina)", xDesc, y, encabezado)
                y += 18f
                encabezadoTabla()
            }
            canvas.drawText(item.descripcion.take(42), xDesc, y, texto)
            canvas.drawText(formatearCantidad(item.cantidad), xCant, y, texto)
            canvas.drawText(Dinero.formatear(item.precioUnitCentavos), xPrecio, y, texto)
            canvas.drawText(
                if (item.descuentoPct > 0) "${formatearCantidad(item.descuentoPct)}%" else "—",
                xDto, y, texto,
            )
            canvas.drawText(Dinero.formatear(item.subtotalCentavos), xSub, y, texto)
            y += 17f
        }

        y += 6f
        canvas.drawLine(xDesc, y, xFin, y, linea)
        y += 20f

        // Subtotal y descuento general solo cuando hay descuento: si no, el total alcanza.
        if (factura.descuentoPct > 0) {
            canvas.drawText("Subtotal", xPrecio, y, encabezado)
            canvas.drawText(Dinero.formatear(factura.subtotalCentavos), xSub, y, texto)
            y += 17f
            canvas.drawText(
                "Descuento ${formatearCantidad(factura.descuentoPct)}%",
                xPrecio, y, encabezado,
            )
            canvas.drawText("- ${Dinero.formatear(factura.descuentoCentavos)}", xSub, y, texto)
            y += 22f
        }
        canvas.drawText("TOTAL", xPrecio, y, totalPaint)
        canvas.drawText(Dinero.formatear(factura.totalCentavos), xSub, y, totalPaint)

        if (factura.notas.isNotBlank()) {
            y += 26f
            canvas.drawText(factura.notas.take(90), xDesc, y, texto)
        }

        canvas.drawText(
            "Comprobante interno. No válido como factura fiscal.",
            xDesc, (ALTO - 40).toFloat(), pie,
        )

        documento.finishPage(pagina)

        val nombre = "factura-${factura.numero.ifBlank { factura.id.toString() }}.pdf"
        val archivo = File(context.cacheDir, "exports").apply { mkdirs() }.let { File(it, nombre) }
        FileOutputStream(archivo).use { documento.writeTo(it) }
        documento.close()

        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
    }

    /** Muestra "2" en vez de "2.0", pero conserva decimales cuando los hay. */
    private fun formatearCantidad(c: Double): String =
        if (c == Math.floor(c)) c.toLong().toString() else c.toString()
}

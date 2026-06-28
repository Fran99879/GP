package com.tallerapp.core.export

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/**
 * Genera un CSV del reporte (sin dependencias externas). Excel y Google Sheets lo
 * abren directamente. Se antepone BOM UTF-8 para que Excel muestre bien los acentos.
 */
object CsvExporter {

    // BOM UTF-8 (U+FEFF), construido por código para no meter un carácter invisible en el fuente.
    private val bom: Char = Char(0xFEFF)

    fun generar(context: Context, secciones: List<SeccionReporte>): Uri {
        val sb = StringBuilder()
        sb.append(bom)

        secciones.forEach { sec ->
            sb.append(escapar(sec.titulo)).append('\n')
            sec.filas.forEach { (etiqueta, valor) ->
                sb.append(escapar(etiqueta)).append(',').append(escapar(valor)).append('\n')
            }
            sb.append('\n')
        }

        val archivo = File(context.cacheDir, "exports").apply { mkdirs() }
            .let { File(it, "reporte.csv") }
        archivo.writeText(sb.toString(), Charsets.UTF_8)

        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
    }

    private fun escapar(valor: String): String =
        if (valor.contains(',') || valor.contains('"') || valor.contains('\n')) {
            "\"" + valor.replace("\"", "\"\"") + "\""
        } else {
            valor
        }
}

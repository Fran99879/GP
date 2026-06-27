package com.tallerapp.core.export

import android.content.Context
import android.content.Intent
import android.net.Uri

/** Abre el menú nativo de Android para compartir/guardar un archivo generado. */
object Compartir {

    fun archivo(context: Context, uri: Uri, mime: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "Compartir reporte")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}

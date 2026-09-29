package com.tallerapp.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import java.io.File
import java.util.UUID

/**
 * Fotos de los productos.
 *
 * El selector de fotos devuelve un `Uri` prestado: deja de valer al reiniciar el proceso.
 * Así que la imagen se **copia** a `filesDir/productos` y en la base se guarda la ruta de
 * esa copia. Se reescala a 1024px y se recomprime: una foto de cámara son 4 MB y en una
 * tarjeta de catálogo se ve igual con 150 KB.
 */
object ImagenProducto {

    private const val TAG = "ImagenProducto"
    private const val LADO_MAXIMO = 1024
    private const val CALIDAD = 85

    private fun carpeta(context: Context): File =
        File(context.filesDir, "productos").apply { mkdirs() }

    /** Copia la foto elegida y devuelve la ruta guardable, o null si no se pudo leer. */
    fun guardar(context: Context, origen: Uri): String? = try {
        val bitmap = context.contentResolver.openInputStream(origen).use { entrada ->
            BitmapFactory.decodeStream(entrada, null, opcionesEscaladas(context, origen))
        } ?: return null

        val destino = File(carpeta(context), "${UUID.randomUUID()}.jpg")
        destino.outputStream().use { salida ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, CALIDAD, salida)
        }
        bitmap.recycle()
        destino.absolutePath
    } catch (e: Exception) {
        Log.w(TAG, "No se pudo guardar la imagen", e)
        null
    }

    /** Carga la foto para mostrarla. Devuelve null si la ruta está vacía o el archivo no está. */
    fun cargar(ruta: String): Bitmap? {
        if (ruta.isBlank()) return null
        val archivo = File(ruta)
        if (!archivo.exists()) return null
        return try {
            BitmapFactory.decodeFile(ruta)
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo cargar la imagen", e)
            null
        }
    }

    /** Borra la copia. Se llama al cambiar la foto de un producto o al eliminarlo. */
    fun borrar(ruta: String) {
        if (ruta.isBlank()) return
        runCatching { File(ruta).delete() }
    }

    /**
     * Mide la imagen sin cargarla en memoria y calcula el factor de reducción, así una foto
     * de 12 MP no entra entera a la RAM antes de escalarla.
     */
    private fun opcionesEscaladas(context: Context, origen: Uri): BitmapFactory.Options {
        val medida = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(origen).use {
            BitmapFactory.decodeStream(it, null, medida)
        }
        var factor = 1
        var lado = maxOf(medida.outWidth, medida.outHeight)
        while (lado / 2 >= LADO_MAXIMO) {
            lado /= 2
            factor *= 2
        }
        return BitmapFactory.Options().apply { inSampleSize = factor }
    }
}

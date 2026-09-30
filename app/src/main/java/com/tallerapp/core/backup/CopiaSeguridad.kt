package com.tallerapp.core.backup

import android.content.Context
import android.net.Uri
import android.util.Log
import com.tallerapp.data.local.TallerDatabase
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Copia de seguridad de todos los datos de la app en un solo archivo `.zip`.
 *
 * Se copia **el archivo de la base**, no un volcado tabla por tabla. Es menos código y, sobre
 * todo, no hay forma de que una tabla nueva quede afuera de la copia por olvido: el día que se
 * agregue una, ya está adentro. El precio es que la copia queda atada a la versión del esquema,
 * y por eso el manifiesto la guarda y la restauración se niega a abrir una copia más nueva que
 * la app (Room haría un downgrade destructivo y se perderían los datos, que es justo lo que
 * esta función viene a evitar).
 *
 * Las **fotos de los productos no entran**: son archivos aparte y pesan. Después de restaurar,
 * los productos que tenían foto quedan con el ícono genérico; el resto de la ficha está entero.
 *
 * Restaurar **reemplaza** todo lo que haya en el teléfono. La pantalla lo avisa antes.
 */
object CopiaSeguridad {

    private const val TAG = "CopiaSeguridad"

    private const val NOMBRE_BASE = "tallerapp.db"
    private const val ENTRADA_BASE = "base/$NOMBRE_BASE"
    private const val ENTRADA_MANIFIESTO = "manifiesto.json"

    /** Nombre sugerido del archivo, con la fecha para no pisar copias anteriores. */
    fun nombreSugerido(): String {
        val fecha = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        return "mis-finanzas-$fecha.zip"
    }

    /** Resultado de exportar o restaurar, ya con el mensaje que ve el usuario. */
    sealed interface Resultado {
        data class Exito(val mensaje: String) : Resultado
        data class Error(val mensaje: String) : Resultado
    }

    /**
     * Escribe la copia en [destino] (el archivo que eligió el usuario con el selector).
     *
     * Antes de copiar se hace un checkpoint del WAL: si no, los últimos movimientos podrían
     * estar todavía en el `-wal` y no en el `.db`, y la copia saldría incompleta.
     */
    fun exportar(context: Context, destino: Uri): Resultado {
        return try {
        val db = TallerDatabase.obtener(context)
        db.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").use { it.moveToFirst() }

        val archivoBase = context.getDatabasePath(NOMBRE_BASE)

        context.contentResolver.openOutputStream(destino)?.use { salida ->
            ZipOutputStream(salida.buffered()).use { zip ->
                val manifiesto = JSONObject()
                    .put("app", context.packageName)
                    .put("esquema", TallerDatabase.VERSION_ESQUEMA)
                    .put("fecha", System.currentTimeMillis())
                zip.putNextEntry(ZipEntry(ENTRADA_MANIFIESTO))
                zip.write(manifiesto.toString().toByteArray())
                zip.closeEntry()

                zip.putNextEntry(ZipEntry(ENTRADA_BASE))
                archivoBase.inputStream().use { it.copyTo(zip) }
                zip.closeEntry()
            }
        } ?: return Resultado.Error("No se pudo escribir el archivo.")

            Resultado.Exito("Copia guardada.")
        } catch (e: Exception) {
            Log.w(TAG, "Fallo al exportar", e)
            Resultado.Error("No se pudo crear la copia: ${e.message}")
        }
    }

    /**
     * Reemplaza los datos actuales con los de la copia [origen].
     *
     * Se trabaja primero sobre un archivo temporal y recién cuando todo salió bien se pisa la
     * base: si la copia está rota o incompleta, el usuario se queda con lo que ya tenía.
     *
     * Devuelve éxito cuando los datos quedaron en su lugar; **la app tiene que reiniciarse**
     * después, porque Room ya tiene abierta la base vieja en memoria.
     */
    fun restaurar(context: Context, origen: Uri): Resultado {
        return try {
        val temporal = File(context.cacheDir, "restore").apply {
            deleteRecursively()
            mkdirs()
        }

        var manifiesto: JSONObject? = null
        var baseTemporal: File? = null

        context.contentResolver.openInputStream(origen)?.use { entrada ->
            ZipInputStream(entrada.buffered()).use { zip ->
                var item: ZipEntry? = zip.nextEntry
                while (item != null) {
                    when (item.name) {
                        ENTRADA_MANIFIESTO ->
                            manifiesto = JSONObject(zip.readBytes().decodeToString())

                        ENTRADA_BASE -> baseTemporal = File(temporal, NOMBRE_BASE).also { destino ->
                            destino.outputStream().use { zip.copyTo(it) }
                        }
                    }
                    zip.closeEntry()
                    item = zip.nextEntry
                }
            }
        } ?: return Resultado.Error("No se pudo leer el archivo.")

        val base = baseTemporal
            ?: return Resultado.Error("El archivo no es una copia de Mis Finanzas.")

        val esquema = manifiesto?.optInt("esquema", -1) ?: -1
        if (esquema > TallerDatabase.VERSION_ESQUEMA) {
            return Resultado.Error(
                "La copia es de una versión más nueva de la app. Actualizá Mis Finanzas y probá de nuevo.",
            )
        }

        // Desde acá se pisa lo que hay. La base se cierra para que no queden escrituras a medias.
        TallerDatabase.cerrar()
        val destinoBase = context.getDatabasePath(NOMBRE_BASE)
        destinoBase.parentFile?.mkdirs()
        base.copyTo(destinoBase, overwrite = true)
        // El WAL y el índice compartido son de la base vieja: si quedan, la corrompen.
        File(destinoBase.path + "-wal").delete()
        File(destinoBase.path + "-shm").delete()

            temporal.deleteRecursively()
            Resultado.Exito("Datos restaurados. La app se va a reiniciar.")
        } catch (e: Exception) {
            Log.w(TAG, "Fallo al restaurar", e)
            Resultado.Error("No se pudo restaurar: ${e.message}")
        }
    }
}

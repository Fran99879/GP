package com.tallerapp.core.backup

import android.content.Context
import android.database.sqlite.SQLiteDatabase
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
     * Tres controles antes de que el usuario pueda perder algo:
     * 1. El manifiesto no puede declarar un esquema más nuevo que el de la app.
     * 2. El archivo tiene que pasar `PRAGMA integrity_check` y tener la tabla `negocio`.
     * 3. Se guarda la base actual aparte; si la restaurada no abre, se vuelve a la anterior.
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

        // Antes de tocar nada: que lo que viene adentro del zip sea de verdad una base de esta
        // app. Un archivo cualquiera renombrado dejaría la app sin abrir y sin datos.
        if (!esBaseValida(base)) {
            temporal.deleteRecursively()
            return Resultado.Error("El archivo está dañado o no es una copia de Mis Finanzas.")
        }

        // Desde acá se pisa lo que hay. La base se cierra para que no queden escrituras a medias.
        TallerDatabase.cerrar()
        val destinoBase = context.getDatabasePath(NOMBRE_BASE)
        destinoBase.parentFile?.mkdirs()

        // Red de seguridad: si la copia resulta ilegible una vez puesta en su lugar, se vuelve a
        // lo que el usuario tenía. Restaurar no puede dejarlo sin nada.
        val respaldo = respaldarActual(context, destinoBase, temporal)

        base.copyTo(destinoBase, overwrite = true)
        // El WAL y el índice compartido son de la base vieja: si quedan, la corrompen.
        File(destinoBase.path + "-wal").delete()
        File(destinoBase.path + "-shm").delete()

        if (!esBaseValida(destinoBase)) {
            volverAtras(respaldo, destinoBase)
            temporal.deleteRecursively()
            return Resultado.Error("No se pudo restaurar. Tus datos quedaron como estaban.")
        }

            temporal.deleteRecursively()
            Resultado.Exito("Datos restaurados. La app se va a reiniciar.")
        } catch (e: Exception) {
            Log.w(TAG, "Fallo al restaurar", e)
            Resultado.Error("No se pudo restaurar: ${e.message}")
        }
    }

    /**
     * ¿La base que está usando la app ahora mismo está sana?
     *
     * Room no lo comprueba: si el archivo se corrompió (corte de luz a mitad de una escritura,
     * almacenamiento con fallas, una copia restaurada a mano), la app se entera recién cuando
     * revienta al leer. Esto lo pregunta de entrada para poder avisar y ofrecer restaurar.
     *
     * Corre fuera del hilo principal: es un recorrido completo del archivo.
     */
    fun baseEstaSana(context: Context): Boolean {
        val archivo = context.getDatabasePath(NOMBRE_BASE)
        // Instalación nueva: todavía no hay archivo y eso no es un problema.
        return !archivo.exists() || esBaseValida(archivo)
    }

    /**
     * ¿Es una base SQLite sana y de esta app?
     *
     * Dos preguntas en una: `PRAGMA integrity_check` dice si el archivo es una base SQLite entera,
     * y buscar la tabla `negocio` dice si es **nuestra** base y no la de otro programa. Se abre de
     * solo lectura, así que esto no modifica el archivo ni crea uno nuevo si no existe.
     */
    private fun esBaseValida(archivo: File): Boolean = try {
        SQLiteDatabase.openDatabase(archivo.path, null, SQLiteDatabase.OPEN_READONLY).use { db ->
            val integra = db.rawQuery("PRAGMA integrity_check", null).use { c ->
                c.moveToFirst() && c.getString(0).equals("ok", ignoreCase = true)
            }
            val esNuestra = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name='negocio'",
                null,
            ).use { it.moveToFirst() }

            integra && esNuestra
        }
    } catch (e: Exception) {
        // Un archivo que no es SQLite tira acá mismo: eso ya es respuesta suficiente.
        Log.w(TAG, "La copia no se pudo abrir como base", e)
        false
    }

    /** Guarda la base actual (y sus archivos de WAL) en [temporal]. Devuelve null si no había. */
    private fun respaldarActual(context: Context, actual: File, temporal: File): File? {
        if (!actual.exists()) return null
        return try {
            val copia = File(temporal, "previa.db")
            actual.copyTo(copia, overwrite = true)
            copia
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo respaldar la base actual", e)
            null
        }
    }

    /** Vuelve a la base que había antes de restaurar. */
    private fun volverAtras(respaldo: File?, destino: File) {
        try {
            if (respaldo != null && respaldo.exists()) {
                respaldo.copyTo(destino, overwrite = true)
            } else {
                // No había base previa: mejor sin archivo que con uno ilegible, así la app
                // arranca de cero en vez de reventar al abrir.
                destino.delete()
            }
            File(destino.path + "-wal").delete()
            File(destino.path + "-shm").delete()
        } catch (e: Exception) {
            Log.w(TAG, "Fallo al volver atrás", e)
        }
    }
}

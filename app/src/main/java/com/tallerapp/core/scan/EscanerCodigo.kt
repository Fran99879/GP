package com.tallerapp.core.scan

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

/**
 * Lectura de códigos de barras con el escáner de Google Play Services.
 *
 * Se usa el escáner **de Google** y no una cámara propia por dos razones concretas:
 * no hace falta el permiso `CAMERA` (la cámara la abre el proceso de Play Services, no la
 * app) y el módulo se descarga a demanda, así que no suma peso al APK. La contra es que
 * necesita Play Services; en un teléfono sin Play el código se escribe a mano, que es el
 * camino que la pantalla deja siempre abierto.
 *
 * Se aceptan los formatos de góndola (EAN/UPC) y también QR y Code 128, que es lo que usan
 * los negocios que imprimen sus propias etiquetas.
 */
object EscanerCodigo {

    private const val TAG = "EscanerCodigo"

    private val opciones = GmsBarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_EAN_13,
            Barcode.FORMAT_EAN_8,
            Barcode.FORMAT_UPC_A,
            Barcode.FORMAT_UPC_E,
            Barcode.FORMAT_CODE_128,
            Barcode.FORMAT_CODE_39,
            Barcode.FORMAT_ITF,
            Barcode.FORMAT_QR_CODE,
        )
        .enableAutoZoom()
        .build()

    /**
     * Abre la cámara y devuelve el código leído.
     *
     * [onError] recibe un mensaje listo para mostrar. Cancelar no es un error: si el usuario
     * cierra el escáner no se llama a ninguna de las dos.
     */
    fun escanear(
        context: Context,
        onCodigo: (String) -> Unit,
        onError: (String) -> Unit = {},
    ) {
        GmsBarcodeScanning.getClient(context, opciones)
            .startScan()
            .addOnSuccessListener { codigo ->
                val valor = codigo.rawValue?.trim()
                if (valor.isNullOrEmpty()) {
                    onError("No se pudo leer el código. Probá de nuevo.")
                } else {
                    onCodigo(valor)
                }
            }
            .addOnCanceledListener { /* El usuario cerró el escáner: no es un error. */ }
            .addOnFailureListener { e ->
                Log.w(TAG, "Fallo del escáner", e)
                onError("No se pudo abrir el escáner. Escribí el código a mano.")
            }
    }
}

/** Devuelve una acción lista para el botón de escanear de una pantalla. */
@Composable
fun recordarEscaner(
    onCodigo: (String) -> Unit,
    onError: (String) -> Unit = {},
): () -> Unit {
    val context = LocalContext.current
    return { EscanerCodigo.escanear(context, onCodigo, onError) }
}

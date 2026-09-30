package com.tallerapp.core.actualizacion

import android.app.Activity
import android.util.Log
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

/**
 * Aviso de actualización disponible, con la API de Play (In-App Updates).
 *
 * Se usa el flujo **flexible**: Play descarga la versión nueva de fondo mientras el usuario
 * sigue trabajando, y al terminar se le ofrece reiniciar. El flujo inmediato bloquea la
 * pantalla hasta actualizar, y en una app de finanzas cortarle el paso a alguien que está
 * cargando un movimiento no se justifica.
 *
 * Solo funciona en instalaciones **hechas desde Play**: con un APK puesto por `adb` no
 * ocurre nada, ni siquiera un error. Play además puede tardar unas horas en ofrecer una
 * versión recién publicada.
 */
class ActualizacionApp(activity: Activity) {

    private companion object {
        const val TAG = "ActualizacionApp"

        /** Código de `startUpdateFlowForResult`; no se usa, la UI la maneja Play. */
        const val CODIGO_PEDIDO = 1701
    }

    private val manager: AppUpdateManager = AppUpdateManagerFactory.create(activity)

    /** Se completó la descarga y falta reiniciar: quien observe esto muestra el aviso. */
    var descargaLista: (() -> Unit)? = null

    private val listener = InstallStateUpdatedListener { estado ->
        if (estado.installStatus() == InstallStatus.DOWNLOADED) descargaLista?.invoke()
    }

    /** Consulta a Play y, si hay versión nueva, lanza la descarga. Llamar al iniciar. */
    fun buscar(activity: Activity) {
        manager.registerListener(listener)
        manager.appUpdateInfo
            .addOnSuccessListener { info ->
                val hayActualizacion =
                    info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                if (hayActualizacion && info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                    manager.startUpdateFlowForResult(
                        info,
                        activity,
                        AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build(),
                        CODIGO_PEDIDO,
                    )
                }
                // Una descarga que quedó a medio camino en la sesión anterior se retoma sola.
                if (info.installStatus() == InstallStatus.DOWNLOADED) descargaLista?.invoke()
            }
            .addOnFailureListener { Log.w(TAG, "No se pudo consultar a Play: ${it.message}") }
    }

    /** Instala lo descargado y reinicia la app. Lo dispara el usuario desde el aviso. */
    fun completar() = manager.completeUpdate()

    fun soltar() = manager.unregisterListener(listener)
}

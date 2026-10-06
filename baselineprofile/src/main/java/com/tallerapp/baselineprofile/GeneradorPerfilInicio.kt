package com.tallerapp.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test

/**
 * Genera el Baseline Profile: la lista de métodos que Android compila de antemano, para que el
 * primer arranque no corra interpretado (ROADMAP sección 13, Paso 2).
 *
 * Se corre a mano con `gradlew :app:generateReleaseBaselineProfile`, con un emulador **sin Play
 * Store** conectado (`google_apis`, no `google_apis_playstore`): la herramienta necesita root para
 * leer el perfil, y las imágenes con Play Store no lo permiten.
 *
 * El recorrido es el que hace cualquiera al abrir la app: arranque, Dashboard, listado de
 * Movimientos y Reportes. No hace falta más: lo que no entra acá igual funciona, solo que sin
 * precompilar.
 */
class GeneradorPerfilInicio {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun arranqueYPantallasPrincipales() = rule.collect(
        packageName = PAQUETE,
        // Además del perfil, saca el "startup profile": con él, las clases del arranque quedan
        // juntas dentro del dex y se leen de una pasada en vez de saltando por el archivo.
        includeInStartupProfile = true,
    ) {
        pressHome()
        startActivityAndWait()

        // Primera instalación: la guía rápida tapa el Dashboard hasta que se la cierra.
        device.wait(Until.hasObject(By.textContains("Empezar")), ESPERA)
        device.findObject(By.textContains("Empezar"))?.click()

        device.wait(Until.hasObject(By.text("Inicio")), ESPERA)

        irA("Movimientos")
        irA("Reportes")
        irA("Inicio")
    }

    /** Toca un destino de la barra inferior y espera a que la pantalla termine de dibujarse. */
    private fun androidx.benchmark.macro.MacrobenchmarkScope.irA(destino: String) {
        device.findObject(By.text(destino))?.click()
        device.waitForIdle(ESPERA)
    }

    private companion object {
        const val PAQUETE = "com.tallerapp"
        const val ESPERA = 5_000L
    }
}

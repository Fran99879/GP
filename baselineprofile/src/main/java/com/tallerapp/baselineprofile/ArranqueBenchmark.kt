package com.tallerapp.baselineprofile

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test

/**
 * Mide el arranque en frío y el scroll de Movimientos (ROADMAP sección 13, Paso 3).
 *
 * Cada medición corre dos veces: sin el Baseline Profile y con él. Esa diferencia es el número
 * que justifica el perfil; sin medir las dos, "va más rápido" es una impresión.
 *
 * Se corre con `gradlew :baselineprofile:connectedBenchmarkReleaseAndroidTest`, con un solo
 * emulador conectado (`ANDROID_SERIAL`). En emulador los números sirven para comparar entre sí,
 * no como tiempo real de un teléfono: la máquina virtual y el host meten ruido.
 */
class ArranqueBenchmark {

    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun arranqueSinPerfil() = medirArranque(CompilationMode.None())

    @Test
    fun arranqueConPerfil() =
        medirArranque(CompilationMode.Partial(baselineProfileMode = BaselineProfileMode.Require))

    /**
     * Necesita un dispositivo **con movimientos cargados**: mide los frames al desplazar la lista,
     * y con la lista vacía no hay nada que medir (la corrida sale sin métricas, no falla sola).
     *
     * Instalar la variante `benchmarkRelease` borra los datos de la app, y al no ser depurable no
     * se le puede empujar una base con `run-as`. Así que los datos se cargan a mano desde la app
     * antes de correr esto, o se restaura una copia desde Configuración → Datos.
     */
    @Test
    fun scrollMovimientosConPerfil() = rule.measureRepeated(
        packageName = PAQUETE,
        metrics = listOf(FrameTimingMetric()),
        compilationMode = CompilationMode.Partial(baselineProfileMode = BaselineProfileMode.Require),
        iterations = ITERACIONES,
        startupMode = StartupMode.WARM,
        setupBlock = {
            pressHome()
            startActivityAndWait()
            device.findObject(By.text("Movimientos"))?.click()
            device.waitForIdle(ESPERA)
        },
    ) {
        val lista = device.wait(Until.findObject(By.scrollable(true)), ESPERA) ?: return@measureRepeated
        lista.setGestureMargin(device.displayWidth / 5)
        repeat(3) { lista.fling(Direction.DOWN) }
        device.waitForIdle(ESPERA)
    }

    private fun medirArranque(modo: CompilationMode) = rule.measureRepeated(
        packageName = PAQUETE,
        metrics = listOf(StartupTimingMetric()),
        compilationMode = modo,
        iterations = ITERACIONES,
        startupMode = StartupMode.COLD,
        setupBlock = { pressHome() },
    ) {
        startActivityAndWait()
    }

    private companion object {
        const val PAQUETE = "com.tallerapp"
        const val ESPERA = 5_000L

        /** Diez pasadas: suficiente para que la mediana no dependa de una corrida con ruido. */
        const val ITERACIONES = 10
    }
}

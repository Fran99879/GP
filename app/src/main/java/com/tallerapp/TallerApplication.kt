package com.tallerapp

import android.app.Application
import com.tallerapp.core.NegocioActual
import com.tallerapp.core.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Application: crea el contenedor de dependencias una vez al iniciar el proceso. */
class TallerApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        NegocioActual.cargar(this)
        container = AppContainer(this)
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            // Asegura el negocio inicial ("Personal") en instalaciones nuevas.
            runCatching { container.asegurarNegocioInicial() }
            // Genera los movimientos recurrentes que correspondan a este mes.
            runCatching { container.generarRecurrentes() }
        }
    }
}

package com.tallerapp

import android.app.Application
import com.tallerapp.core.di.AppContainer

/** Application: crea el contenedor de dependencias una vez al iniciar el proceso. */
class TallerApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

package com.tallerapp.core.di

import android.content.Context
import com.tallerapp.data.local.TallerDatabase
import com.tallerapp.data.repository.TrabajoRepositoryImpl
import com.tallerapp.domain.repository.TrabajoRepository
import com.tallerapp.domain.usecase.CambiarEstadoUseCase
import com.tallerapp.domain.usecase.CrearTrabajoUseCase
import com.tallerapp.domain.usecase.EditarTrabajoUseCase
import com.tallerapp.domain.usecase.EliminarTrabajoUseCase
import com.tallerapp.domain.usecase.ObservarTrabajosUseCase
import com.tallerapp.domain.usecase.ObtenerTrabajoUseCase

/**
 * Contenedor de dependencias manual (sin framework de DI, por simplicidad - Arquitectura).
 * Construye la base local, el repositorio y los casos de uso una sola vez por proceso.
 */
class AppContainer(context: Context) {

    private val database = TallerDatabase.obtener(context)

    private val trabajoRepository: TrabajoRepository =
        TrabajoRepositoryImpl(database.trabajoDao())

    // Casos de uso del módulo Trabajos (Fase 2).
    val crearTrabajo = CrearTrabajoUseCase(trabajoRepository)
    val editarTrabajo = EditarTrabajoUseCase(trabajoRepository)
    val eliminarTrabajo = EliminarTrabajoUseCase(trabajoRepository)
    val obtenerTrabajo = ObtenerTrabajoUseCase(trabajoRepository)
    val observarTrabajos = ObservarTrabajosUseCase(trabajoRepository)
    val cambiarEstado = CambiarEstadoUseCase(trabajoRepository)
}

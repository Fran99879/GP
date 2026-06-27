package com.tallerapp.core.di

import android.content.Context
import com.tallerapp.data.local.TallerDatabase
import com.tallerapp.data.repository.CobroRepositoryImpl
import com.tallerapp.data.repository.EgresoRepositoryImpl
import com.tallerapp.data.repository.IngresoRepositoryImpl
import com.tallerapp.data.repository.TrabajoRepositoryImpl
import com.tallerapp.domain.repository.CobroRepository
import com.tallerapp.domain.repository.EgresoRepository
import com.tallerapp.domain.repository.IngresoRepository
import com.tallerapp.domain.repository.TrabajoRepository
import com.tallerapp.domain.usecase.AnularCobroUseCase
import com.tallerapp.domain.usecase.CambiarEstadoUseCase
import com.tallerapp.domain.usecase.CrearTrabajoUseCase
import com.tallerapp.domain.usecase.EditarEgresoUseCase
import com.tallerapp.domain.usecase.EditarIngresoUseCase
import com.tallerapp.domain.usecase.EditarTrabajoUseCase
import com.tallerapp.domain.usecase.EliminarEgresoUseCase
import com.tallerapp.domain.usecase.EliminarIngresoUseCase
import com.tallerapp.domain.usecase.EliminarTrabajoUseCase
import com.tallerapp.domain.usecase.ObservarDashboardUseCase
import com.tallerapp.domain.usecase.ObservarEgresosDelDiaUseCase
import com.tallerapp.domain.usecase.ObservarIngresosDelDiaUseCase
import com.tallerapp.domain.usecase.ObservarReporteMensualUseCase
import com.tallerapp.domain.usecase.ObservarResumenDelDiaUseCase
import com.tallerapp.domain.usecase.ObservarTrabajosUseCase
import com.tallerapp.domain.usecase.ObtenerEgresoUseCase
import com.tallerapp.domain.usecase.ObtenerIngresoUseCase
import com.tallerapp.domain.usecase.ObtenerTrabajoUseCase
import com.tallerapp.domain.usecase.RegistrarCobroUseCase
import com.tallerapp.domain.usecase.RegistrarEgresoUseCase
import com.tallerapp.domain.usecase.RegistrarIngresoUseCase

/**
 * Contenedor de dependencias manual (sin framework de DI, por simplicidad - Arquitectura).
 * Construye la base local, los repositorios y los casos de uso una sola vez por proceso.
 */
class AppContainer(context: Context) {

    private val database = TallerDatabase.obtener(context)

    private val trabajoRepository: TrabajoRepository =
        TrabajoRepositoryImpl(database.trabajoDao())
    private val ingresoRepository: IngresoRepository =
        IngresoRepositoryImpl(database.ingresoDao())
    private val egresoRepository: EgresoRepository =
        EgresoRepositoryImpl(database.egresoDao())
    private val cobroRepository: CobroRepository =
        CobroRepositoryImpl(database, database.trabajoDao(), database.ingresoDao())

    // Trabajos (Fase 2).
    val crearTrabajo = CrearTrabajoUseCase(trabajoRepository)
    val editarTrabajo = EditarTrabajoUseCase(trabajoRepository)
    val eliminarTrabajo = EliminarTrabajoUseCase(trabajoRepository)
    val obtenerTrabajo = ObtenerTrabajoUseCase(trabajoRepository)
    val observarTrabajos = ObservarTrabajosUseCase(trabajoRepository)
    val cambiarEstado = CambiarEstadoUseCase(trabajoRepository)

    // Finanzas (Fase 3).
    val registrarIngreso = RegistrarIngresoUseCase(ingresoRepository)
    val editarIngreso = EditarIngresoUseCase(ingresoRepository)
    val eliminarIngreso = EliminarIngresoUseCase(ingresoRepository)
    val obtenerIngreso = ObtenerIngresoUseCase(ingresoRepository)
    val observarIngresosDelDia = ObservarIngresosDelDiaUseCase(ingresoRepository)

    val registrarEgreso = RegistrarEgresoUseCase(egresoRepository)
    val editarEgreso = EditarEgresoUseCase(egresoRepository)
    val eliminarEgreso = EliminarEgresoUseCase(egresoRepository)
    val obtenerEgreso = ObtenerEgresoUseCase(egresoRepository)
    val observarEgresosDelDia = ObservarEgresosDelDiaUseCase(egresoRepository)

    val observarResumenDelDia =
        ObservarResumenDelDiaUseCase(ingresoRepository, egresoRepository)

    // Cobros (Fase 4).
    val registrarCobro = RegistrarCobroUseCase(trabajoRepository, cobroRepository)
    val anularCobro = AnularCobroUseCase(trabajoRepository, ingresoRepository, cobroRepository)

    // Dashboard (Fase 5).
    val observarDashboard =
        ObservarDashboardUseCase(trabajoRepository, ingresoRepository, egresoRepository)

    // Reportes (Fase 6). El reporte diario reusa observarResumenDelDia.
    val observarReporteMensual =
        ObservarReporteMensualUseCase(trabajoRepository, ingresoRepository, egresoRepository)
}

package com.tallerapp.core.di

import android.content.Context
import com.tallerapp.data.local.TallerDatabase
import com.tallerapp.data.repository.DeudaRepositoryImpl
import com.tallerapp.data.repository.EgresoRepositoryImpl
import com.tallerapp.data.repository.IngresoRepositoryImpl
import com.tallerapp.domain.repository.DeudaRepository
import com.tallerapp.domain.repository.EgresoRepository
import com.tallerapp.domain.repository.IngresoRepository
import com.tallerapp.domain.usecase.EditarDeudaUseCase
import com.tallerapp.domain.usecase.EditarEgresoUseCase
import com.tallerapp.domain.usecase.EditarIngresoUseCase
import com.tallerapp.domain.usecase.EliminarDeudaUseCase
import com.tallerapp.domain.usecase.EliminarEgresoUseCase
import com.tallerapp.domain.usecase.EliminarIngresoUseCase
import com.tallerapp.domain.usecase.MarcarDeudaCobradaUseCase
import com.tallerapp.domain.usecase.ObservarDashboardUseCase
import com.tallerapp.domain.usecase.ObservarDeudasUseCase
import com.tallerapp.domain.usecase.ObservarEgresosDelDiaUseCase
import com.tallerapp.domain.usecase.ObservarIngresosDelDiaUseCase
import com.tallerapp.domain.usecase.ObservarReporteMensualUseCase
import com.tallerapp.domain.usecase.ObservarResumenDelDiaUseCase
import com.tallerapp.domain.usecase.ObtenerDeudaUseCase
import com.tallerapp.domain.usecase.ObtenerEgresoUseCase
import com.tallerapp.domain.usecase.ObtenerIngresoUseCase
import com.tallerapp.domain.usecase.RegistrarDeudaUseCase
import com.tallerapp.domain.usecase.RegistrarEgresoUseCase
import com.tallerapp.domain.usecase.RegistrarIngresoUseCase

/**
 * Contenedor de dependencias manual (sin framework de DI, por simplicidad).
 * Construye la base local, los repositorios y los casos de uso una sola vez por proceso.
 */
class AppContainer(context: Context) {

    private val database = TallerDatabase.obtener(context)

    private val ingresoRepository: IngresoRepository =
        IngresoRepositoryImpl(database.ingresoDao())
    private val egresoRepository: EgresoRepository =
        EgresoRepositoryImpl(database.egresoDao())
    private val deudaRepository: DeudaRepository =
        DeudaRepositoryImpl(database.deudaDao())

    // Ingresos.
    val registrarIngreso = RegistrarIngresoUseCase(ingresoRepository)
    val editarIngreso = EditarIngresoUseCase(ingresoRepository)
    val eliminarIngreso = EliminarIngresoUseCase(ingresoRepository)
    val obtenerIngreso = ObtenerIngresoUseCase(ingresoRepository)
    val observarIngresosDelDia = ObservarIngresosDelDiaUseCase(ingresoRepository)

    // Gastos.
    val registrarEgreso = RegistrarEgresoUseCase(egresoRepository)
    val editarEgreso = EditarEgresoUseCase(egresoRepository)
    val eliminarEgreso = EliminarEgresoUseCase(egresoRepository)
    val obtenerEgreso = ObtenerEgresoUseCase(egresoRepository)
    val observarEgresosDelDia = ObservarEgresosDelDiaUseCase(egresoRepository)

    val observarResumenDelDia =
        ObservarResumenDelDiaUseCase(ingresoRepository, egresoRepository)

    // Deudas ("quién te debe").
    val registrarDeuda = RegistrarDeudaUseCase(deudaRepository)
    val editarDeuda = EditarDeudaUseCase(deudaRepository)
    val eliminarDeuda = EliminarDeudaUseCase(deudaRepository)
    val obtenerDeuda = ObtenerDeudaUseCase(deudaRepository)
    val observarDeudas = ObservarDeudasUseCase(deudaRepository)
    val marcarDeudaCobrada = MarcarDeudaCobradaUseCase(deudaRepository)

    // Inicio (Dashboard).
    val observarDashboard =
        ObservarDashboardUseCase(ingresoRepository, egresoRepository, deudaRepository)

    // Reportes mensuales (por mes calendario).
    val observarReporteMensual =
        ObservarReporteMensualUseCase(ingresoRepository, egresoRepository)
}

package com.tallerapp.core.di

import android.content.Context
import com.tallerapp.data.local.TallerDatabase
import com.tallerapp.data.repository.AgendaRepositoryImpl
import com.tallerapp.data.repository.CategoriaRepositoryImpl
import com.tallerapp.data.repository.CuentaRepositoryImpl
import com.tallerapp.data.repository.DeudaRepositoryImpl
import com.tallerapp.data.repository.ContactoRepositoryImpl
import com.tallerapp.data.repository.MetaRepositoryImpl
import com.tallerapp.data.repository.NegocioRepositoryImpl
import com.tallerapp.data.repository.RecurrenteRepositoryImpl
import com.tallerapp.data.repository.EgresoRepositoryImpl
import com.tallerapp.data.repository.IngresoRepositoryImpl
import com.tallerapp.domain.repository.AgendaRepository
import com.tallerapp.domain.repository.CategoriaRepository
import com.tallerapp.domain.repository.CuentaRepository
import com.tallerapp.domain.repository.DeudaRepository
import com.tallerapp.domain.repository.ContactoRepository
import com.tallerapp.domain.repository.MetaRepository
import com.tallerapp.domain.repository.NegocioRepository
import com.tallerapp.domain.repository.RecurrenteRepository
import com.tallerapp.domain.usecase.AgregarContactoUseCase
import com.tallerapp.domain.usecase.ObservarAgendaUseCase
import com.tallerapp.domain.usecase.GuardarAgendaItemUseCase
import com.tallerapp.domain.usecase.MarcarAgendaHechoUseCase
import com.tallerapp.domain.usecase.EliminarAgendaItemUseCase
import com.tallerapp.domain.usecase.ObservarContactosUseCase
import com.tallerapp.domain.repository.EgresoRepository
import com.tallerapp.domain.repository.IngresoRepository
import com.tallerapp.domain.usecase.EliminarCategoriaUseCase
import com.tallerapp.domain.usecase.EliminarCuentaUseCase
import com.tallerapp.domain.usecase.EliminarMetaUseCase
import com.tallerapp.domain.usecase.GuardarCategoriaUseCase
import com.tallerapp.domain.usecase.GuardarCuentaUseCase
import com.tallerapp.domain.usecase.GuardarMetaUseCase
import com.tallerapp.domain.usecase.ObservarCategoriasUseCase
import com.tallerapp.domain.usecase.ObservarCuentasUseCase
import com.tallerapp.domain.usecase.EliminarRecurrenteUseCase
import com.tallerapp.domain.usecase.GenerarRecurrentesUseCase
import com.tallerapp.domain.usecase.GuardarRecurrenteUseCase
import com.tallerapp.domain.usecase.ObservarMetasUseCase
import com.tallerapp.domain.usecase.ObservarNegociosUseCase
import com.tallerapp.domain.usecase.CrearNegocioUseCase
import com.tallerapp.domain.usecase.RenombrarNegocioUseCase
import com.tallerapp.domain.usecase.AsegurarNegocioInicialUseCase
import com.tallerapp.domain.usecase.ObservarRecurrentesUseCase
import com.tallerapp.domain.usecase.SaldosCuentasUseCase
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
import com.tallerapp.domain.usecase.ObservarEgresosRangoUseCase
import com.tallerapp.domain.usecase.ObservarIngresosDelDiaUseCase
import com.tallerapp.domain.usecase.ObservarIngresosRangoUseCase
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
    private val categoriaRepository: CategoriaRepository =
        CategoriaRepositoryImpl(database.categoriaDao())
    private val cuentaRepository: CuentaRepository =
        CuentaRepositoryImpl(database.cuentaDao())
    private val metaRepository: MetaRepository =
        MetaRepositoryImpl(database.metaDao())
    private val recurrenteRepository: RecurrenteRepository =
        RecurrenteRepositoryImpl(database.recurrenteDao())
    private val contactoRepository: ContactoRepository =
        ContactoRepositoryImpl(database.contactoDao())
    private val agendaRepository: AgendaRepository =
        AgendaRepositoryImpl(database.agendaDao())
    private val negocioRepository: NegocioRepository =
        NegocioRepositoryImpl(database.negocioDao())

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
    val observarIngresosRango = ObservarIngresosRangoUseCase(ingresoRepository)
    val observarEgresosRango = ObservarEgresosRangoUseCase(egresoRepository)

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
    val observarComparativaNegocios =
        com.tallerapp.domain.usecase.ObservarComparativaNegociosUseCase(negocioRepository, ingresoRepository, egresoRepository)
    val observarEvolucion =
        com.tallerapp.domain.usecase.ObservarEvolucionUseCase(ingresoRepository, egresoRepository)

    // Categorías personalizables.
    val observarCategorias = ObservarCategoriasUseCase(categoriaRepository)
    val guardarCategoria = GuardarCategoriaUseCase(categoriaRepository)
    val eliminarCategoria = EliminarCategoriaUseCase(categoriaRepository)

    // Cuentas / medios de pago.
    val observarCuentas = ObservarCuentasUseCase(cuentaRepository)
    val saldosCuentas = SaldosCuentasUseCase(cuentaRepository)
    val guardarCuenta = GuardarCuentaUseCase(cuentaRepository)
    val eliminarCuenta = EliminarCuentaUseCase(cuentaRepository)

    // Metas de ahorro.
    val observarMetas = ObservarMetasUseCase(metaRepository)
    val guardarMeta = GuardarMetaUseCase(metaRepository)
    val eliminarMeta = EliminarMetaUseCase(metaRepository)

    // Movimientos recurrentes.
    val observarRecurrentes = ObservarRecurrentesUseCase(recurrenteRepository)
    val guardarRecurrente = GuardarRecurrenteUseCase(recurrenteRepository)
    val eliminarRecurrente = EliminarRecurrenteUseCase(recurrenteRepository)
    val generarRecurrentes = GenerarRecurrentesUseCase(recurrenteRepository, ingresoRepository, egresoRepository)

    // Contactos frecuentes.
    val observarContactos = ObservarContactosUseCase(contactoRepository)
    val agregarContacto = AgregarContactoUseCase(contactoRepository)

    // Negocios (multi-negocio).
    val observarNegocios = ObservarNegociosUseCase(negocioRepository)
    val crearNegocio = CrearNegocioUseCase(negocioRepository)
    val renombrarNegocio = RenombrarNegocioUseCase(negocioRepository)
    val asegurarNegocioInicial = AsegurarNegocioInicialUseCase(negocioRepository)

    // Agenda (tareas, turnos y productos).
    val observarAgenda = ObservarAgendaUseCase(agendaRepository)
    val guardarAgendaItem = GuardarAgendaItemUseCase(agendaRepository)
    val marcarAgendaHecho = MarcarAgendaHechoUseCase(agendaRepository)
    val eliminarAgendaItem = EliminarAgendaItemUseCase(agendaRepository)
}

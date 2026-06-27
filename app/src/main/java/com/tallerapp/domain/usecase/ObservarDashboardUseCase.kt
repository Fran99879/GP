package com.tallerapp.domain.usecase

import com.tallerapp.core.util.Fechas
import com.tallerapp.domain.model.EstadoReparacion
import com.tallerapp.domain.model.ResumenDashboard
import com.tallerapp.domain.repository.EgresoRepository
import com.tallerapp.domain.repository.IngresoRepository
import com.tallerapp.domain.repository.TrabajoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Indicadores del Dashboard (Frozen Spec 12.1). Combina trabajos + finanzas de forma
 * reactiva. Reglas: caja del día = ingresos del día (RN-4); ganancia del mes =
 * ingresos − gastos del mes calendario (RN-1/RN-7); vehículos en taller (RN-8).
 */
class ObservarDashboardUseCase(
    private val trabajoRepository: TrabajoRepository,
    private val ingresoRepository: IngresoRepository,
    private val egresoRepository: EgresoRepository,
) {
    private val enTaller = setOf(
        EstadoReparacion.PENDIENTE,
        EstadoReparacion.EN_REPARACION,
        EstadoReparacion.ESPERANDO_REPUESTOS,
        EstadoReparacion.TERMINADO,
    )

    operator fun invoke(): Flow<ResumenDashboard> {
        val (hoyInicio, hoyFin) = Fechas.rangoDeHoy()
        val (mesInicio, mesFin) = Fechas.rangoDelMesActual()

        return combine(
            trabajoRepository.observarTodos(),
            ingresoRepository.sumaRango(hoyInicio, hoyFin),
            ingresoRepository.sumaRango(mesInicio, mesFin),
            egresoRepository.sumaRango(mesInicio, mesFin),
        ) { trabajos, cajaHoy, ingresosMes, gastosMes ->
            ResumenDashboard(
                cajaDelDiaCentavos = cajaHoy,
                gananciaDelMesCentavos = ingresosMes - gastosMes,
                vehiculosEnTaller = trabajos.count { it.estadoReparacion in enTaller },
                esperandoRepuestos = trabajos.count {
                    it.estadoReparacion == EstadoReparacion.ESPERANDO_REPUESTOS
                },
                pendientes = trabajos.count { it.estadoReparacion == EstadoReparacion.PENDIENTE },
                entregadosHoy = trabajos.count {
                    it.estadoReparacion == EstadoReparacion.ENTREGADO &&
                        it.fechaEntrega?.let(Fechas::esHoy) == true
                },
            )
        }
    }
}

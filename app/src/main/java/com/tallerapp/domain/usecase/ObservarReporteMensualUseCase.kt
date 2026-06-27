package com.tallerapp.domain.usecase

import com.tallerapp.core.util.Fechas
import com.tallerapp.domain.model.ConteoServicio
import com.tallerapp.domain.model.EstadoReparacion
import com.tallerapp.domain.model.ReporteMensual
import com.tallerapp.domain.repository.EgresoRepository
import com.tallerapp.domain.repository.IngresoRepository
import com.tallerapp.domain.repository.TrabajoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Reporte mensual del mes calendario actual (Frozen Spec 12.3, RN-7/RN-9).
 * Mismas definiciones que el Dashboard para que las cifras coincidan (mitiga R2).
 */
class ObservarReporteMensualUseCase(
    private val trabajoRepository: TrabajoRepository,
    private val ingresoRepository: IngresoRepository,
    private val egresoRepository: EgresoRepository,
) {
    operator fun invoke(): Flow<ReporteMensual> {
        val (inicio, fin) = Fechas.rangoDelMesActual()
        val rangoMes = inicio until fin

        return combine(
            trabajoRepository.observarTodos(),
            ingresoRepository.sumaRango(inicio, fin),
            egresoRepository.sumaRango(inicio, fin),
        ) { trabajos, ingresos, gastos ->
            val atendidos = trabajos.filter { it.fechaIngreso in rangoMes }
            val servicios = atendidos
                .filter { it.estadoReparacion != EstadoReparacion.CANCELADO }
                .groupingBy { it.servicio }
                .eachCount()
                .map { (servicio, cantidad) -> ConteoServicio(servicio, cantidad) }
                .sortedByDescending { it.cantidad }

            ReporteMensual(
                ingresosCentavos = ingresos,
                gastosCentavos = gastos,
                trabajosRealizados = trabajos.count {
                    it.estadoReparacion == EstadoReparacion.ENTREGADO &&
                        it.fechaEntrega != null && it.fechaEntrega in rangoMes
                },
                vehiculosAtendidos = atendidos.size,
                serviciosMasRealizados = servicios,
            )
        }
    }
}

package com.tallerapp.domain.usecase

import com.tallerapp.core.util.Fechas
import com.tallerapp.domain.model.ResumenDelDia
import com.tallerapp.domain.repository.EgresoRepository
import com.tallerapp.domain.repository.IngresoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Totales del día: caja (ingresos) y gastos (RN-4). Combina ambas sumas de forma
 * reactiva para que el hub se actualice ante cualquier movimiento.
 */
class ObservarResumenDelDiaUseCase(
    private val ingresoRepository: IngresoRepository,
    private val egresoRepository: EgresoRepository,
) {
    operator fun invoke(): Flow<ResumenDelDia> {
        val (inicio, fin) = Fechas.rangoDeHoy()
        return combine(
            ingresoRepository.sumaRango(inicio, fin),
            egresoRepository.sumaRango(inicio, fin),
        ) { ingresos, gastos -> ResumenDelDia(ingresos, gastos) }
    }
}

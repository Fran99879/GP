package com.tallerapp.domain.usecase

import com.tallerapp.core.util.Fechas
import com.tallerapp.domain.model.ResumenNegocio
import com.tallerapp.domain.repository.EgresoRepository
import com.tallerapp.domain.repository.IngresoRepository
import com.tallerapp.domain.repository.NegocioRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.YearMonth

/**
 * Comparativa entre negocios para un mes: ingresos, gastos y balance de cada uno.
 * Equivale al bloque "Comparativa entre negocios" de Reportes en el escritorio.
 */
class ObservarComparativaNegociosUseCase(
    private val negocioRepository: NegocioRepository,
    private val ingresoRepository: IngresoRepository,
    private val egresoRepository: EgresoRepository,
) {
    operator fun invoke(mes: YearMonth): Flow<List<ResumenNegocio>> {
        val (inicio, fin) = Fechas.rangoDelMes(mes)
        return combine(
            negocioRepository.observar(),
            ingresoRepository.totalesPorNegocio(inicio, fin),
            egresoRepository.totalesPorNegocio(inicio, fin),
        ) { negocios, ingresos, egresos ->
            negocios.map { n ->
                ResumenNegocio(
                    negocioId = n.id,
                    nombre = n.nombre,
                    ingresosCentavos = ingresos[n.id] ?: 0L,
                    gastosCentavos = egresos[n.id] ?: 0L,
                )
            }
        }
    }
}

package com.tallerapp.domain.usecase

import com.tallerapp.core.util.Fechas
import com.tallerapp.domain.model.GastoPorCategoria
import com.tallerapp.domain.model.ReporteMensual
import com.tallerapp.domain.repository.EgresoRepository
import com.tallerapp.domain.repository.IngresoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.YearMonth

/**
 * Reporte de un mes calendario: totales de ingresos y gastos, más el desglose de
 * gastos por categoría (para el gráfico de torta). Reactivo ante cualquier cambio.
 *
 * Con [todosLosNegocios] = true combina los datos de todos los negocios
 * (equivale al check "Ver todos los negocios" de Reportes en el escritorio).
 */
class ObservarReporteMensualUseCase(
    private val ingresoRepository: IngresoRepository,
    private val egresoRepository: EgresoRepository,
) {
    operator fun invoke(mes: YearMonth, todosLosNegocios: Boolean = false): Flow<ReporteMensual> {
        val (inicio, fin) = Fechas.rangoDelMes(mes)

        val ingresosFlow = if (todosLosNegocios) {
            ingresoRepository.sumaRangoTodos(inicio, fin)
        } else {
            ingresoRepository.sumaRango(inicio, fin)
        }
        val egresosFlow = if (todosLosNegocios) {
            egresoRepository.observarRangoTodos(inicio, fin)
        } else {
            egresoRepository.observarRango(inicio, fin)
        }

        return combine(ingresosFlow, egresosFlow) { ingresos, egresos ->
            val porCategoria = egresos
                .groupBy { it.categoria }
                .map { (categoria, lista) ->
                    GastoPorCategoria(categoria, lista.sumOf { it.montoCentavos })
                }
                .sortedByDescending { it.montoCentavos }

            ReporteMensual(
                ingresosCentavos = ingresos,
                gastosCentavos = egresos.sumOf { it.montoCentavos },
                gastosPorCategoria = porCategoria,
            )
        }
    }
}

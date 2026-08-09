package com.tallerapp.domain.usecase

import com.tallerapp.core.util.Fechas
import com.tallerapp.domain.model.ResumenDashboard
import com.tallerapp.domain.repository.DeudaRepository
import com.tallerapp.domain.repository.EgresoRepository
import com.tallerapp.domain.repository.IngresoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Indicadores del inicio: caja del día, ingresos/gastos del mes calendario y
 * total pendiente de cobro (deudas a favor). Todo reactivo.
 */
class ObservarDashboardUseCase(
    private val ingresoRepository: IngresoRepository,
    private val egresoRepository: EgresoRepository,
    private val deudaRepository: DeudaRepository,
) {
    operator fun invoke(): Flow<ResumenDashboard> {
        val (hoyInicio, hoyFin) = Fechas.rangoDeHoy()
        val (mesInicio, mesFin) = Fechas.rangoDelMesActual()

        return combine(
            ingresoRepository.sumaRango(hoyInicio, hoyFin),
            ingresoRepository.sumaRango(mesInicio, mesFin),
            egresoRepository.sumaRango(mesInicio, mesFin),
            deudaRepository.sumaPendiente(),
            deudaRepository.contarPendientes(),
        ) { cajaHoy, ingresosMes, gastosMes, deudasPend, cantDeudas ->
            ResumenDashboard(
                ingresosDelMesCentavos = ingresosMes,
                gastosDelMesCentavos = gastosMes,
                cajaDelDiaCentavos = cajaHoy,
                deudasPendientesCentavos = deudasPend,
                cantidadDeudasPendientes = cantDeudas,
            )
        }
    }
}

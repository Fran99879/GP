package com.tallerapp.domain.usecase

import com.tallerapp.core.util.Fechas
import com.tallerapp.domain.repository.EgresoRepository
import com.tallerapp.domain.repository.IngresoRepository
import kotlinx.coroutines.flow.first
import java.time.YearMonth

/** Un mes de la evolución: totales de ingresos y gastos. */
data class MesEvolucion(val mes: YearMonth, val ingresosCentavos: Long, val gastosCentavos: Long)

/** Evolución de los últimos 12 meses terminando en [mesFinal]. */
class ObservarEvolucionUseCase(
    private val ingresoRepository: IngresoRepository,
    private val egresoRepository: EgresoRepository,
) {
    suspend operator fun invoke(mesFinal: YearMonth): List<MesEvolucion> {
        val lista = ArrayList<MesEvolucion>(12)
        for (k in 11 downTo 0) {
            val ym = mesFinal.minusMonths(k.toLong())
            val (ini, fin) = Fechas.rangoDelMes(ym)
            val ing = ingresoRepository.sumaRango(ini, fin).first()
            val gas = egresoRepository.sumaRango(ini, fin).first()
            lista.add(MesEvolucion(ym, ing, gas))
        }
        return lista
    }
}

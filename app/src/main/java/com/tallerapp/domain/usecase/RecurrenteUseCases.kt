package com.tallerapp.domain.usecase

import com.tallerapp.domain.model.Egreso
import com.tallerapp.domain.model.Ingreso
import com.tallerapp.domain.model.MetodoPago
import com.tallerapp.domain.model.OrigenIngreso
import com.tallerapp.domain.model.Recurrente
import com.tallerapp.domain.repository.EgresoRepository
import com.tallerapp.domain.repository.IngresoRepository
import com.tallerapp.domain.repository.RecurrenteRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneId

class ObservarRecurrentesUseCase(private val repository: RecurrenteRepository) {
    operator fun invoke(): Flow<List<Recurrente>> = repository.observar()
}

class GuardarRecurrenteUseCase(private val repository: RecurrenteRepository) {
    suspend operator fun invoke(rec: Recurrente) {
        if (rec.id == 0L) repository.crear(rec) else repository.actualizar(rec)
    }
}

class EliminarRecurrenteUseCase(private val repository: RecurrenteRepository) {
    suspend operator fun invoke(id: Long) = repository.eliminar(id)
}

/**
 * Genera los movimientos recurrentes activos cuyo día del mes ya pasó y que no se generaron
 * este mes. Idempotente por mes (ultimoGenerado = "yyyy-MM"). Devuelve cuántos creó.
 */
class GenerarRecurrentesUseCase(
    private val recurrenteRepository: RecurrenteRepository,
    private val ingresoRepository: IngresoRepository,
    private val egresoRepository: EgresoRepository,
) {
    suspend operator fun invoke(): Int {
        val hoy = LocalDate.now()
        val ym = "%04d-%02d".format(hoy.year, hoy.monthValue)
        var creados = 0

        for (rec in recurrenteRepository.activos()) {
            if (rec.ultimoGenerado == ym || rec.diaMes > hoy.dayOfMonth) continue
            val dia = minOf(rec.diaMes, hoy.lengthOfMonth())
            val fecha = LocalDate.of(hoy.year, hoy.monthValue, dia)
                .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val ahora = System.currentTimeMillis()

            if (rec.tipo == "ingreso") {
                ingresoRepository.crear(
                    Ingreso(
                        montoCentavos = rec.montoCentavos, concepto = rec.concepto,
                        metodo = MetodoPago.EFECTIVO, cuenta = rec.cuenta, reparto = null,
                        fecha = fecha, fechaRegistro = ahora, origen = OrigenIngreso.MANUAL, trabajoId = null,
                    ),
                )
            } else {
                egresoRepository.crear(
                    Egreso(
                        montoCentavos = rec.montoCentavos, categoria = rec.categoria,
                        concepto = rec.concepto, cuenta = rec.cuenta, fecha = fecha, fechaRegistro = ahora,
                    ),
                )
            }
            recurrenteRepository.marcarGenerado(rec.id, ym)
            creados++
        }
        return creados
    }
}

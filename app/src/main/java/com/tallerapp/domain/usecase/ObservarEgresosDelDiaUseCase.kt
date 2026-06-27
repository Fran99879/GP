package com.tallerapp.domain.usecase

import com.tallerapp.core.util.Fechas
import com.tallerapp.domain.model.Egreso
import com.tallerapp.domain.repository.EgresoRepository
import kotlinx.coroutines.flow.Flow

/** Egresos cuya fecha contable es hoy (alimenta el hub de Finanzas). */
class ObservarEgresosDelDiaUseCase(private val repository: EgresoRepository) {
    operator fun invoke(): Flow<List<Egreso>> {
        val (inicio, fin) = Fechas.rangoDeHoy()
        return repository.observarRango(inicio, fin)
    }
}

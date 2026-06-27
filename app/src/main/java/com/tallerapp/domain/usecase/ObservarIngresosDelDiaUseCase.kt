package com.tallerapp.domain.usecase

import com.tallerapp.core.util.Fechas
import com.tallerapp.domain.model.Ingreso
import com.tallerapp.domain.repository.IngresoRepository
import kotlinx.coroutines.flow.Flow

/** Ingresos cuya fecha contable es hoy (alimenta el hub de Finanzas). */
class ObservarIngresosDelDiaUseCase(private val repository: IngresoRepository) {
    operator fun invoke(): Flow<List<Ingreso>> {
        val (inicio, fin) = Fechas.rangoDeHoy()
        return repository.observarRango(inicio, fin)
    }
}

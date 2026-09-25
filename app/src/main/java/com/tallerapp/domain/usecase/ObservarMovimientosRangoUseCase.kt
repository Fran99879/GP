package com.tallerapp.domain.usecase

import com.tallerapp.domain.model.Egreso
import com.tallerapp.domain.model.Ingreso
import com.tallerapp.domain.repository.EgresoRepository
import com.tallerapp.domain.repository.IngresoRepository
import kotlinx.coroutines.flow.Flow

/** Ingresos en un rango [inicio, fin) (para búsqueda/filtros). */
class ObservarIngresosRangoUseCase(private val repository: IngresoRepository) {
    operator fun invoke(inicio: Long, fin: Long): Flow<List<Ingreso>> = repository.observarRango(inicio, fin)
}

/** Egresos en un rango [inicio, fin) (para búsqueda/filtros). */
class ObservarEgresosRangoUseCase(private val repository: EgresoRepository) {
    operator fun invoke(inicio: Long, fin: Long): Flow<List<Egreso>> = repository.observarRango(inicio, fin)
}

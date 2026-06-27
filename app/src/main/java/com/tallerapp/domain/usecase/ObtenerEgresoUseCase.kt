package com.tallerapp.domain.usecase

import com.tallerapp.domain.model.Egreso
import com.tallerapp.domain.repository.EgresoRepository

/** Carga un egreso por id para edición. */
class ObtenerEgresoUseCase(private val repository: EgresoRepository) {
    suspend operator fun invoke(id: Long): Egreso? = repository.obtener(id)
}

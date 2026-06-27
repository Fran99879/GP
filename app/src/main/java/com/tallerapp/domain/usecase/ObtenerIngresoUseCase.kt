package com.tallerapp.domain.usecase

import com.tallerapp.domain.model.Ingreso
import com.tallerapp.domain.repository.IngresoRepository

/** Carga un ingreso por id para edición. */
class ObtenerIngresoUseCase(private val repository: IngresoRepository) {
    suspend operator fun invoke(id: Long): Ingreso? = repository.obtener(id)
}

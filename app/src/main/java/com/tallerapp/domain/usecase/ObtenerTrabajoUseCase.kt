package com.tallerapp.domain.usecase

import com.tallerapp.domain.model.Trabajo
import com.tallerapp.domain.repository.TrabajoRepository

/** Carga un trabajo por id para el detalle/edición. Devuelve null si no existe. */
class ObtenerTrabajoUseCase(private val repository: TrabajoRepository) {
    suspend operator fun invoke(id: Long): Trabajo? = repository.obtener(id)
}

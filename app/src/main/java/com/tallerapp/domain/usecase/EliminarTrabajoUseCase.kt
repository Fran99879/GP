package com.tallerapp.domain.usecase

import com.tallerapp.domain.repository.TrabajoRepository

/** Elimina un trabajo por id. */
class EliminarTrabajoUseCase(private val repository: TrabajoRepository) {
    suspend operator fun invoke(id: Long) = repository.eliminar(id)
}

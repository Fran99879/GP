package com.tallerapp.domain.usecase

import com.tallerapp.domain.repository.DeudaRepository

/** Elimina una deuda. */
class EliminarDeudaUseCase(private val repository: DeudaRepository) {
    suspend operator fun invoke(id: Long) = repository.eliminar(id)
}

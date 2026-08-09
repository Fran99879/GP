package com.tallerapp.domain.usecase

import com.tallerapp.domain.model.Deuda
import com.tallerapp.domain.repository.DeudaRepository

/** Obtiene una deuda por id (para editar). */
class ObtenerDeudaUseCase(private val repository: DeudaRepository) {
    suspend operator fun invoke(id: Long): Deuda? = repository.obtener(id)
}

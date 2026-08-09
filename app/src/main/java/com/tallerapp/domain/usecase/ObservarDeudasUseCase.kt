package com.tallerapp.domain.usecase

import com.tallerapp.domain.model.Deuda
import com.tallerapp.domain.repository.DeudaRepository
import kotlinx.coroutines.flow.Flow

/** Lista reactiva de todas las deudas (pendientes primero). */
class ObservarDeudasUseCase(private val repository: DeudaRepository) {
    operator fun invoke(): Flow<List<Deuda>> = repository.observarTodas()
}

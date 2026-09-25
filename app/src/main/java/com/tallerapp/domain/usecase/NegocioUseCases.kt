package com.tallerapp.domain.usecase

import com.tallerapp.domain.model.Negocio
import com.tallerapp.domain.repository.NegocioRepository
import kotlinx.coroutines.flow.Flow

class ObservarNegociosUseCase(private val repository: NegocioRepository) {
    operator fun invoke(): Flow<List<Negocio>> = repository.observar()
}

class CrearNegocioUseCase(private val repository: NegocioRepository) {
    suspend operator fun invoke(nombre: String): Long = repository.crear(nombre.trim())
}

class RenombrarNegocioUseCase(private val repository: NegocioRepository) {
    suspend operator fun invoke(id: Long, nombre: String) = repository.renombrar(id, nombre.trim())
}

class AsegurarNegocioInicialUseCase(private val repository: NegocioRepository) {
    suspend operator fun invoke() = repository.asegurarInicial()
}

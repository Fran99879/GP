package com.tallerapp.domain.usecase

import com.tallerapp.domain.repository.ContactoRepository
import kotlinx.coroutines.flow.Flow

class ObservarContactosUseCase(private val repository: ContactoRepository) {
    operator fun invoke(): Flow<List<String>> = repository.observar()
}

class AgregarContactoUseCase(private val repository: ContactoRepository) {
    suspend operator fun invoke(nombre: String) = repository.agregar(nombre)
}

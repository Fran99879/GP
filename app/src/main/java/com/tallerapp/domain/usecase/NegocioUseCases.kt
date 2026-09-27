package com.tallerapp.domain.usecase

import com.tallerapp.domain.model.Negocio
import com.tallerapp.domain.repository.NegocioRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

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

/**
 * Elimina un negocio con todos sus datos. Nunca borra el último que queda:
 * la app siempre necesita al menos un negocio donde registrar.
 * Devuelve true si lo eliminó.
 */
class EliminarNegocioUseCase(
    private val repository: NegocioRepository,
    private val observarNegocios: ObservarNegociosUseCase,
) {
    suspend operator fun invoke(id: Long): Boolean {
        val negocios = observarNegocios().first()
        if (negocios.size <= 1) return false
        repository.eliminar(id)
        return true
    }
}

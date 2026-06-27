package com.tallerapp.domain.usecase

import com.tallerapp.domain.model.Trabajo
import com.tallerapp.domain.repository.TrabajoRepository
import kotlinx.coroutines.flow.Flow

/**
 * Expone la lista de trabajos de forma observable. Si hay texto de búsqueda filtra
 * por patente o cliente (Frozen Spec 11, RN-10); si está vacío devuelve todos.
 */
class ObservarTrabajosUseCase(private val repository: TrabajoRepository) {

    operator fun invoke(query: String): Flow<List<Trabajo>> =
        if (query.isBlank()) repository.observarTodos()
        else repository.observarBusqueda(query.trim())
}

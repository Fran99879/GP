package com.tallerapp.domain.usecase

import com.tallerapp.domain.model.EstadoReparacion
import com.tallerapp.domain.repository.TrabajoRepository
import com.tallerapp.domain.rules.TransicionesTrabajo

/**
 * Aplica un cambio de estado de reparación si la transición es válida (Frozen Spec 7.2, V-9).
 * Devuelve false si la transición no está permitida o el trabajo no existe.
 */
class CambiarEstadoUseCase(private val repository: TrabajoRepository) {

    suspend operator fun invoke(id: Long, destino: EstadoReparacion): Boolean {
        val trabajo = repository.obtener(id) ?: return false
        if (!TransicionesTrabajo.puedeTransicionar(trabajo.estadoReparacion, destino)) return false
        repository.actualizar(trabajo.copy(estadoReparacion = destino))
        return true
    }
}

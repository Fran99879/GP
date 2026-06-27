package com.tallerapp.domain.usecase

import com.tallerapp.core.util.Fechas
import com.tallerapp.domain.repository.IngresoRepository

/** Anula (elimina) un ingreso. Solo permitido el día de registro (V-7). */
class EliminarIngresoUseCase(private val repository: IngresoRepository) {

    suspend operator fun invoke(id: Long): Boolean {
        val existente = repository.obtener(id) ?: return false
        if (!Fechas.esHoy(existente.fechaRegistro)) return false
        repository.eliminar(id)
        return true
    }
}

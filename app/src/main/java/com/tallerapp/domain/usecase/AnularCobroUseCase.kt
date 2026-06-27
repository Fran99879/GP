package com.tallerapp.domain.usecase

import com.tallerapp.core.util.Fechas
import com.tallerapp.domain.repository.CobroRepository
import com.tallerapp.domain.repository.IngresoRepository
import com.tallerapp.domain.repository.TrabajoRepository

/**
 * Anula el cobro de un trabajo (Frozen Spec 9.4): elimina el ingreso y devuelve el
 * trabajo a Pendiente de cobro. Solo permitido el mismo día del cobro.
 * Devuelve false si no hay cobro, no existe, o el cobro no es de hoy.
 */
class AnularCobroUseCase(
    private val trabajoRepository: TrabajoRepository,
    private val ingresoRepository: IngresoRepository,
    private val cobroRepository: CobroRepository,
) {

    suspend operator fun invoke(trabajoId: Long): Boolean {
        val trabajo = trabajoRepository.obtener(trabajoId) ?: return false
        val cobroId = trabajo.cobroId ?: return false
        val ingreso = ingresoRepository.obtener(cobroId) ?: return false
        if (!Fechas.esHoy(ingreso.fechaRegistro)) return false
        return cobroRepository.anularCobro(trabajoId)
    }
}

package com.tallerapp.domain.usecase

import com.tallerapp.domain.repository.DeudaRepository

/**
 * Alterna el estado de cobro de una deuda. Al marcarla cobrada guarda el instante;
 * al reabrirla lo limpia.
 */
class MarcarDeudaCobradaUseCase(private val repository: DeudaRepository) {

    suspend operator fun invoke(id: Long, cobrada: Boolean) {
        val existente = repository.obtener(id) ?: return
        repository.actualizar(
            existente.copy(
                cobrada = cobrada,
                fechaCobro = if (cobrada) System.currentTimeMillis() else null,
            ),
        )
    }
}

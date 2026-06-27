package com.tallerapp.domain.usecase

import com.tallerapp.domain.model.EstadoCobro
import com.tallerapp.domain.model.ServicioRealizado
import com.tallerapp.domain.repository.TrabajoRepository
import com.tallerapp.domain.validation.TrabajoErrores
import com.tallerapp.domain.validation.TrabajoValidator

/**
 * Edita un trabajo existente. Valida (V-1), normaliza patente (V-4) y respeta V-8:
 * si el trabajo ya está Cobrado, el precio no se modifica. Conserva fecha de ingreso,
 * estado de reparación y estado de cobro (no se tocan en este flujo).
 */
class EditarTrabajoUseCase(private val repository: TrabajoRepository) {

    suspend operator fun invoke(
        id: Long,
        cliente: String,
        telefono: String?,
        patente: String?,
        marca: String,
        modelo: String,
        servicio: ServicioRealizado?,
        problema: String?,
        diagnostico: String?,
        precioCentavos: Long?,
    ): GuardarResultado {
        val existente = repository.obtener(id)
            ?: return GuardarResultado.Invalido(TrabajoErrores(cliente = "El trabajo no existe"))

        val errores = TrabajoValidator.validar(cliente, marca, modelo, servicio, precioCentavos)
        if (!errores.esValido) return GuardarResultado.Invalido(errores)

        // V-8: precio bloqueado si ya fue cobrado.
        val precioFinal =
            if (existente.estadoCobro == EstadoCobro.COBRADO) existente.precioCentavos
            else precioCentavos!!

        val actualizado = existente.copy(
            cliente = cliente.trim(),
            telefono = telefono?.trim()?.ifBlank { null },
            patente = TrabajoValidator.normalizarPatente(patente),
            marca = marca.trim(),
            modelo = modelo.trim(),
            servicio = servicio!!,
            problema = problema?.trim()?.ifBlank { null },
            diagnostico = diagnostico?.trim()?.ifBlank { null },
            precioCentavos = precioFinal,
        )
        repository.actualizar(actualizado)
        return GuardarResultado.Exito(id)
    }
}

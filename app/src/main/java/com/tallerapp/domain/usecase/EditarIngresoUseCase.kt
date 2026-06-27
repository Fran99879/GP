package com.tallerapp.domain.usecase

import com.tallerapp.core.util.Fechas
import com.tallerapp.domain.model.MetodoPago
import com.tallerapp.domain.model.RepartoPago
import com.tallerapp.domain.repository.IngresoRepository
import com.tallerapp.domain.validation.IngresoErrores
import com.tallerapp.domain.validation.IngresoValidator

/**
 * Edita un ingreso. Solo permitido el día de registro (V-7). Conserva origen,
 * trabajo asociado y fecha de registro.
 */
class EditarIngresoUseCase(private val repository: IngresoRepository) {

    suspend operator fun invoke(
        id: Long,
        montoCentavos: Long?,
        concepto: String,
        metodo: MetodoPago?,
        reparto: RepartoPago?,
        fecha: Long,
    ): IngresoResultado {
        val existente = repository.obtener(id)
            ?: return IngresoResultado.Invalido(IngresoErrores(general = "El ingreso no existe"))

        if (!Fechas.esHoy(existente.fechaRegistro)) {
            return IngresoResultado.Invalido(
                IngresoErrores(general = "Solo se puede editar el día de registro"),
            )
        }

        val errores = IngresoValidator.validar(montoCentavos, concepto, metodo, reparto)
        if (!errores.esValido) return IngresoResultado.Invalido(errores)

        val actualizado = existente.copy(
            montoCentavos = montoCentavos!!,
            concepto = concepto.trim(),
            metodo = metodo!!,
            reparto = if (metodo == MetodoPago.PAGO_MIXTO) reparto else null,
            fecha = fecha,
        )
        repository.actualizar(actualizado)
        return IngresoResultado.Exito(id)
    }
}

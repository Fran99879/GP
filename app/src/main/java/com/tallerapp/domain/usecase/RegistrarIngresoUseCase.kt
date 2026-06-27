package com.tallerapp.domain.usecase

import com.tallerapp.domain.model.Ingreso
import com.tallerapp.domain.model.MetodoPago
import com.tallerapp.domain.model.OrigenIngreso
import com.tallerapp.domain.model.RepartoPago
import com.tallerapp.domain.repository.IngresoRepository
import com.tallerapp.domain.validation.IngresoValidator

/**
 * Registra un ingreso manual (Frozen Spec 9.5). Valida V-2/V-6. Origen = MANUAL
 * (los cobros de trabajos se registran por su propia vía en la Fase 4, RN-3).
 */
class RegistrarIngresoUseCase(private val repository: IngresoRepository) {

    suspend operator fun invoke(
        montoCentavos: Long?,
        concepto: String,
        metodo: MetodoPago?,
        reparto: RepartoPago?,
        fecha: Long,
    ): IngresoResultado {
        val errores = IngresoValidator.validar(montoCentavos, concepto, metodo, reparto)
        if (!errores.esValido) return IngresoResultado.Invalido(errores)

        val ingreso = Ingreso(
            id = 0,
            montoCentavos = montoCentavos!!,
            concepto = concepto.trim(),
            metodo = metodo!!,
            reparto = if (metodo == MetodoPago.PAGO_MIXTO) reparto else null,
            fecha = fecha,
            fechaRegistro = System.currentTimeMillis(),
            origen = OrigenIngreso.MANUAL,
            trabajoId = null,
        )
        return IngresoResultado.Exito(repository.crear(ingreso))
    }
}

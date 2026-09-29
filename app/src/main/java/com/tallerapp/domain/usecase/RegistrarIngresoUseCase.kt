package com.tallerapp.domain.usecase

import com.tallerapp.domain.model.Ingreso
import com.tallerapp.domain.model.OrigenIngreso
import com.tallerapp.domain.repository.IngresoRepository
import com.tallerapp.domain.validation.IngresoValidator

/**
 * Registra un ingreso manual (Frozen Spec 9.5). Valida V-2. Origen = MANUAL
 * (los cobros de trabajos se registran por su propia vía en la Fase 4, RN-3).
 */
class RegistrarIngresoUseCase(private val repository: IngresoRepository) {

    suspend operator fun invoke(
        montoCentavos: Long?,
        concepto: String,
        cuenta: String,
        fecha: Long,
    ): IngresoResultado {
        val errores = IngresoValidator.validar(montoCentavos, concepto)
        if (!errores.esValido) return IngresoResultado.Invalido(errores)

        val ingreso = Ingreso(
            id = 0,
            montoCentavos = montoCentavos!!,
            concepto = concepto.trim(),
            cuenta = cuenta,
            fecha = fecha,
            fechaRegistro = System.currentTimeMillis(),
            origen = OrigenIngreso.MANUAL,
            trabajoId = null,
        )
        return IngresoResultado.Exito(repository.crear(ingreso))
    }
}

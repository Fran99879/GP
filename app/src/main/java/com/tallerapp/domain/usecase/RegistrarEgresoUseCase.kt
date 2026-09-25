package com.tallerapp.domain.usecase

import com.tallerapp.domain.model.Egreso
import com.tallerapp.domain.repository.EgresoRepository
import com.tallerapp.domain.validation.EgresoValidator

/** Registra un egreso (Frozen Spec 9.6). Valida V-3. */
class RegistrarEgresoUseCase(private val repository: EgresoRepository) {

    suspend operator fun invoke(
        montoCentavos: Long?,
        categoria: String?,
        concepto: String,
        cuenta: String,
        fecha: Long,
    ): EgresoResultado {
        val errores = EgresoValidator.validar(montoCentavos, categoria, concepto)
        if (!errores.esValido) return EgresoResultado.Invalido(errores)

        val egreso = Egreso(
            id = 0,
            montoCentavos = montoCentavos!!,
            categoria = categoria!!,
            concepto = concepto.trim(),
            cuenta = cuenta,
            fecha = fecha,
            fechaRegistro = System.currentTimeMillis(),
        )
        return EgresoResultado.Exito(repository.crear(egreso))
    }
}

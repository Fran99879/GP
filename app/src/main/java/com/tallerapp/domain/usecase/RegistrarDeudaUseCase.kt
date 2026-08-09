package com.tallerapp.domain.usecase

import com.tallerapp.domain.model.Deuda
import com.tallerapp.domain.repository.DeudaRepository
import com.tallerapp.domain.validation.DeudaValidator

/** Registra una nueva deuda a favor ("quién te debe"). */
class RegistrarDeudaUseCase(private val repository: DeudaRepository) {

    suspend operator fun invoke(
        nombre: String,
        montoCentavos: Long?,
        fecha: Long,
        nota: String,
    ): DeudaResultado {
        val errores = DeudaValidator.validar(nombre, montoCentavos)
        if (!errores.esValido) return DeudaResultado.Invalido(errores)

        val deuda = Deuda(
            nombre = nombre.trim(),
            montoCentavos = montoCentavos!!,
            fecha = fecha,
            nota = nota.trim(),
            cobrada = false,
            fechaCobro = null,
            fechaRegistro = System.currentTimeMillis(),
        )
        return DeudaResultado.Exito(repository.crear(deuda))
    }
}

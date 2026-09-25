package com.tallerapp.domain.usecase

import com.tallerapp.domain.repository.DeudaRepository
import com.tallerapp.domain.validation.DeudaErrores
import com.tallerapp.domain.validation.DeudaValidator

/** Edita una deuda existente, conservando su estado de cobro. */
class EditarDeudaUseCase(private val repository: DeudaRepository) {

    suspend operator fun invoke(
        id: Long,
        nombre: String,
        montoCentavos: Long?,
        fecha: Long,
        nota: String,
        fechaLimite: Long? = null,
    ): DeudaResultado {
        val existente = repository.obtener(id)
            ?: return DeudaResultado.Invalido(DeudaErrores(general = "La deuda no existe"))

        val errores = DeudaValidator.validar(nombre, montoCentavos)
        if (!errores.esValido) return DeudaResultado.Invalido(errores)

        repository.actualizar(
            existente.copy(
                nombre = nombre.trim(),
                montoCentavos = montoCentavos!!,
                fecha = fecha,
                nota = nota.trim(),
                fechaLimite = fechaLimite,
            ),
        )
        return DeudaResultado.Exito(id)
    }
}

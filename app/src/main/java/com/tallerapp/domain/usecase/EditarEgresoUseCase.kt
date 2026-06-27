package com.tallerapp.domain.usecase

import com.tallerapp.core.util.Fechas
import com.tallerapp.domain.model.CategoriaEgreso
import com.tallerapp.domain.repository.EgresoRepository
import com.tallerapp.domain.validation.EgresoErrores
import com.tallerapp.domain.validation.EgresoValidator

/** Edita un egreso. Solo permitido el día de registro (V-7). */
class EditarEgresoUseCase(private val repository: EgresoRepository) {

    suspend operator fun invoke(
        id: Long,
        montoCentavos: Long?,
        categoria: CategoriaEgreso?,
        concepto: String,
        fecha: Long,
    ): EgresoResultado {
        val existente = repository.obtener(id)
            ?: return EgresoResultado.Invalido(EgresoErrores(general = "El egreso no existe"))

        if (!Fechas.esHoy(existente.fechaRegistro)) {
            return EgresoResultado.Invalido(
                EgresoErrores(general = "Solo se puede editar el día de registro"),
            )
        }

        val errores = EgresoValidator.validar(montoCentavos, categoria, concepto)
        if (!errores.esValido) return EgresoResultado.Invalido(errores)

        val actualizado = existente.copy(
            montoCentavos = montoCentavos!!,
            categoria = categoria!!,
            concepto = concepto.trim(),
            fecha = fecha,
        )
        repository.actualizar(actualizado)
        return EgresoResultado.Exito(id)
    }
}

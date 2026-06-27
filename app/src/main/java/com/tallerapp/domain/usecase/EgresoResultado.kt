package com.tallerapp.domain.usecase

import com.tallerapp.domain.validation.EgresoErrores

/** Resultado de registrar o editar un egreso. */
sealed interface EgresoResultado {
    data class Exito(val id: Long) : EgresoResultado
    data class Invalido(val errores: EgresoErrores) : EgresoResultado
}

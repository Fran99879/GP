package com.tallerapp.domain.usecase

import com.tallerapp.domain.validation.CobroErrores

/** Resultado de registrar un cobro. */
sealed interface CobroResultado {
    data class Exito(val ingresoId: Long) : CobroResultado
    data class Invalido(val errores: CobroErrores) : CobroResultado
}

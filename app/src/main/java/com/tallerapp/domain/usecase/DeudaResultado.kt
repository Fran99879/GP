package com.tallerapp.domain.usecase

import com.tallerapp.domain.validation.DeudaErrores

/** Resultado de registrar o editar una deuda. */
sealed interface DeudaResultado {
    data class Exito(val id: Long) : DeudaResultado
    data class Invalido(val errores: DeudaErrores) : DeudaResultado
}

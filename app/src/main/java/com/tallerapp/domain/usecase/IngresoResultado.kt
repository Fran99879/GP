package com.tallerapp.domain.usecase

import com.tallerapp.domain.validation.IngresoErrores

/** Resultado de registrar o editar un ingreso. */
sealed interface IngresoResultado {
    data class Exito(val id: Long) : IngresoResultado
    data class Invalido(val errores: IngresoErrores) : IngresoResultado
}

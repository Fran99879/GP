package com.tallerapp.domain.usecase

import com.tallerapp.domain.validation.TrabajoErrores

/** Resultado de crear o editar un trabajo: éxito con id, o inválido con errores por campo. */
sealed interface GuardarResultado {
    data class Exito(val id: Long) : GuardarResultado
    data class Invalido(val errores: TrabajoErrores) : GuardarResultado
}

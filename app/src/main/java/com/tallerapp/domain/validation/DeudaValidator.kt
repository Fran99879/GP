package com.tallerapp.domain.validation

/** Errores de validación de una deuda. */
data class DeudaErrores(
    val general: String? = null,
    val nombre: String? = null,
    val monto: String? = null,
) {
    val esValido: Boolean get() = listOf(general, nombre, monto).all { it == null }
}

object DeudaValidator {

    fun validar(nombre: String, montoCentavos: Long?): DeudaErrores = DeudaErrores(
        nombre = if (nombre.isBlank()) "Ingresá quién te debe" else null,
        monto = if (montoCentavos == null || montoCentavos <= 0) "Ingresá un monto válido" else null,
    )
}

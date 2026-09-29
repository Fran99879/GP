package com.tallerapp.domain.validation

/** Errores de validación de un ingreso (Frozen Spec V-2). */
data class IngresoErrores(
    val general: String? = null,
    val monto: String? = null,
    val concepto: String? = null,
) {
    val esValido: Boolean
        get() = listOf(general, monto, concepto).all { it == null }
}

object IngresoValidator {

    fun validar(montoCentavos: Long?, concepto: String): IngresoErrores = IngresoErrores(
        monto = if (montoCentavos == null || montoCentavos <= 0) "Ingresá un monto válido" else null,
        concepto = if (concepto.isBlank()) "Ingresá el concepto" else null,
    )
}

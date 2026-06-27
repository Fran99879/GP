package com.tallerapp.domain.validation

import com.tallerapp.domain.model.CategoriaEgreso

/** Errores de validación de un egreso (Frozen Spec V-3). */
data class EgresoErrores(
    val general: String? = null,
    val monto: String? = null,
    val categoria: String? = null,
    val concepto: String? = null,
) {
    val esValido: Boolean
        get() = listOf(general, monto, categoria, concepto).all { it == null }
}

object EgresoValidator {

    fun validar(
        montoCentavos: Long?,
        categoria: CategoriaEgreso?,
        concepto: String,
    ): EgresoErrores = EgresoErrores(
        monto = if (montoCentavos == null || montoCentavos <= 0) "Ingresá un monto válido" else null,
        categoria = if (categoria == null) "Elegí la categoría" else null,
        concepto = if (concepto.isBlank()) "Ingresá el concepto" else null,
    )
}

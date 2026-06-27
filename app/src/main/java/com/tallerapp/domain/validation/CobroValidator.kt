package com.tallerapp.domain.validation

import com.tallerapp.domain.model.MetodoPago
import com.tallerapp.domain.model.RepartoPago

/** Errores de validación de un cobro (Frozen Spec V-5/V-6). */
data class CobroErrores(
    val general: String? = null,
    val metodo: String? = null,
    val reparto: String? = null,
) {
    val esValido: Boolean
        get() = listOf(general, metodo, reparto).all { it == null }
}

object CobroValidator {

    /** El monto del cobro es el precio del trabajo (fijo). Valida método y pago mixto (V-6). */
    fun validar(
        montoCentavos: Long,
        metodo: MetodoPago?,
        reparto: RepartoPago?,
    ): CobroErrores = CobroErrores(
        general = if (montoCentavos <= 0) "El trabajo no tiene un precio para cobrar" else null,
        metodo = if (metodo == null) "Elegí el método de pago" else null,
        reparto = validarReparto(montoCentavos, metodo, reparto),
    )

    private fun validarReparto(
        montoCentavos: Long,
        metodo: MetodoPago?,
        reparto: RepartoPago?,
    ): String? {
        if (metodo != MetodoPago.PAGO_MIXTO) return null
        return when {
            reparto == null -> "Cargá el reparto del pago mixto"
            reparto.componentesPositivos() < 2 -> "El pago mixto necesita al menos dos métodos"
            reparto.total() != montoCentavos -> "El reparto debe sumar exactamente el total"
            else -> null
        }
    }
}

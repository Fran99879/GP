package com.tallerapp.domain.validation

import com.tallerapp.domain.model.MetodoPago
import com.tallerapp.domain.model.RepartoPago

/** Errores de validación de un ingreso (Frozen Spec V-2, V-6). */
data class IngresoErrores(
    val general: String? = null,
    val monto: String? = null,
    val concepto: String? = null,
    val metodo: String? = null,
    val reparto: String? = null,
) {
    val esValido: Boolean
        get() = listOf(general, monto, concepto, metodo, reparto).all { it == null }
}

object IngresoValidator {

    fun validar(
        montoCentavos: Long?,
        concepto: String,
        metodo: MetodoPago?,
        reparto: RepartoPago?,
    ): IngresoErrores = IngresoErrores(
        monto = if (montoCentavos == null || montoCentavos <= 0) "Ingresá un monto válido" else null,
        concepto = if (concepto.isBlank()) "Ingresá el concepto" else null,
        metodo = if (metodo == null) "Elegí el método de pago" else null,
        reparto = validarReparto(montoCentavos, metodo, reparto),
    )

    // V-6: en pago mixto, al menos dos métodos y la suma debe igualar el total.
    private fun validarReparto(
        montoCentavos: Long?,
        metodo: MetodoPago?,
        reparto: RepartoPago?,
    ): String? {
        if (metodo != MetodoPago.PAGO_MIXTO) return null
        return when {
            reparto == null -> "Cargá el reparto del pago mixto"
            reparto.componentesPositivos() < 2 -> "El pago mixto necesita al menos dos métodos"
            montoCentavos != null && reparto.total() != montoCentavos ->
                "El reparto debe sumar exactamente el total"
            else -> null
        }
    }
}

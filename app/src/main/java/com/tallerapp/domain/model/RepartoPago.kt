package com.tallerapp.domain.model

/**
 * Reparto de un pago mixto entre métodos (Frozen Spec 5.5, V-6). Importes en centavos.
 * Solo aplica cuando el método de pago es PAGO_MIXTO.
 */
data class RepartoPago(
    val efectivoCentavos: Long = 0,
    val transferenciaCentavos: Long = 0,
    val tarjetaCentavos: Long = 0,
    val mercadoPagoCentavos: Long = 0,
) {
    fun total(): Long =
        efectivoCentavos + transferenciaCentavos + tarjetaCentavos + mercadoPagoCentavos

    /** Cantidad de métodos con importe mayor a cero. */
    fun componentesPositivos(): Int =
        listOf(efectivoCentavos, transferenciaCentavos, tarjetaCentavos, mercadoPagoCentavos)
            .count { it > 0 }
}

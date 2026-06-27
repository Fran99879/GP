package com.tallerapp.domain.model

/** Métodos de pago (Frozen Spec 5.5). */
enum class MetodoPago(val etiqueta: String) {
    EFECTIVO("Efectivo"),
    TRANSFERENCIA("Transferencia"),
    TARJETA("Tarjeta"),
    MERCADO_PAGO("Mercado Pago"),
    PAGO_MIXTO("Pago mixto"),
}

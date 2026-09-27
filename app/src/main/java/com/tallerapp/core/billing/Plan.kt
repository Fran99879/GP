package com.tallerapp.core.billing

/**
 * Plan del usuario. Define qué funciones están habilitadas.
 *
 * La división es: uso personal gratis, uso comercial pago (ver `PLANES.md`).
 */
enum class Plan {
    /** Finanzas personales completas, con un solo negocio. */
    GRATIS,

    /** Negocios ilimitados, comparativa, remitos y exportación. */
    PRO,
    ;

    val esPro: Boolean get() = this == PRO
}

/** Límites del plan gratuito. */
object LimitesPlan {
    /** Cuántos negocios puede tener quien no paga. */
    const val NEGOCIOS_GRATIS = 1
}

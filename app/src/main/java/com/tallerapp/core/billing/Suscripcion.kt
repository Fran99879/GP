package com.tallerapp.core.billing

import java.util.concurrent.TimeUnit

/** En qué situación está la suscripción del usuario. */
enum class EstadoSuscripcion {
    /** Sin suscripción: plan Gratis. */
    GRATIS,

    /** Dentro de los días de prueba gratis. */
    PRUEBA,

    /** Pro paga, con renovación automática activada. */
    ACTIVA,

    /** Cancelada pero todavía vigente: Pro funciona hasta que termine el período pagado. */
    CANCELADA,
}

/**
 * Cuánto le queda al plan del usuario, para la barra de días.
 *
 * **Importante**: Google Play Billing no informa la fecha de vencimiento de una suscripción
 * (eso solo lo da la Developer API, del lado del servidor, y esta app no tiene servidor).
 * Así que el fin del período se **deduce** de `purchaseTime` más la duración del plan, y por
 * eso es una estimación: si el usuario pausa, cambia de plan o Play le da un día de gracia,
 * puede diferir en un día. Quien manda sobre el acceso sigue siendo [EstadoPlan]; esta
 * cuenta es solo informativa.
 */
data class Suscripcion(
    val estado: EstadoSuscripcion = EstadoSuscripcion.GRATIS,
    /** Días que faltan para el fin del período. 0 en plan Gratis. */
    val diasRestantes: Int = 0,
    /** Duración del período en curso, en días. Es el 100% de la barra. */
    val diasTotales: Int = 0,
    /** Fin estimado del período (epoch millis). 0 si no aplica. */
    val finMillis: Long = 0,
) {
    /** Cuánto queda del período, de 0 a 1. Sirve directo para la barra. */
    val fraccionRestante: Float
        get() = if (diasTotales <= 0) 0f else (diasRestantes.toFloat() / diasTotales).coerceIn(0f, 1f)

    /** True cuando conviene avisar: quedan 3 días o menos. */
    val porTerminar: Boolean get() = diasRestantes in 1..3

    /** True si hay período que mostrar (o sea, no es el plan Gratis). */
    val tienePeriodo: Boolean get() = estado != EstadoSuscripcion.GRATIS && diasTotales > 0

    companion object {
        val GRATIS = Suscripcion()

        private val UN_DIA = TimeUnit.DAYS.toMillis(1)

        /**
         * Días que faltan desde [ahora] hasta [finMillis], redondeando hacia arriba: si
         * vence en tres horas, al usuario le queda "1 día", no "0".
         */
        fun diasHasta(finMillis: Long, ahora: Long): Int {
            val resto = finMillis - ahora
            if (resto <= 0) return 0
            return ((resto + UN_DIA - 1) / UN_DIA).toInt()
        }
    }
}

package com.tallerapp.domain.model

/** Meta de ahorro: objetivo + lo ahorrado + fecha opcional. */
data class Meta(
    val id: Long = 0,
    val nombre: String,
    val objetivoCentavos: Long,
    val actualCentavos: Long = 0,
    val fechaObjetivo: Long? = null,
    val createdAt: Long = 0,
) {
    /** Progreso 0..1. */
    val fraccion: Float
        get() = if (objetivoCentavos <= 0) 0f else (actualCentavos.toFloat() / objetivoCentavos).coerceIn(0f, 1f)
}

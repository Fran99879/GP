package com.tallerapp.domain.model

/** Movimiento recurrente (sueldo, alquiler, suscripción). Se genera cada mes el día indicado. */
data class Recurrente(
    val id: Long = 0,
    val tipo: String = "egreso",           // "ingreso" | "egreso"
    val montoCentavos: Long = 0,
    val concepto: String = "",
    val categoria: String = "Otros",
    val cuenta: String = "Efectivo",
    val diaMes: Int = 1,                    // 1..28
    val activo: Boolean = true,
    val ultimoGenerado: String = "",
    val createdAt: Long = 0,
)

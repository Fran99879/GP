package com.tallerapp.domain.model

/** Cuenta / medio de pago. [saldoCentavos] se calcula (inicial + ingresos − gastos). */
data class Cuenta(
    val id: Long = 0,
    val nombre: String,
    val icono: String = "💵",
    val saldoInicialCentavos: Long = 0,
    val orden: Int = 0,
    val activo: Boolean = true,
    val saldoCentavos: Long = 0,
) {
    val display: String get() = "$icono  $nombre"
}

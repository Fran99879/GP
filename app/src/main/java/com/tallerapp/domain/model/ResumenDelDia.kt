package com.tallerapp.domain.model

/**
 * Totales del día para el módulo de Finanzas (centavos).
 * Caja del día = ingresos del día (RN-4). La ganancia del día = ingresos − gastos.
 */
data class ResumenDelDia(
    val ingresosCentavos: Long,
    val gastosCentavos: Long,
) {
    val gananciaCentavos: Long get() = ingresosCentavos - gastosCentavos
}

package com.tallerapp.domain.model

/** Indicadores del inicio (Dashboard). Montos en centavos. */
data class ResumenDashboard(
    val ingresosDelMesCentavos: Long = 0,
    val gastosDelMesCentavos: Long = 0,
    val cajaDelDiaCentavos: Long = 0,
    val deudasPendientesCentavos: Long = 0,
    val cantidadDeudasPendientes: Int = 0,
) {
    /** Balance del mes = ingresos − gastos del mes calendario. */
    val balanceDelMesCentavos: Long get() = ingresosDelMesCentavos - gastosDelMesCentavos
}

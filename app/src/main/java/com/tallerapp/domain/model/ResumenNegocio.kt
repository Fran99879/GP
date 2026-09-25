package com.tallerapp.domain.model

/** Totales de un negocio en un período (comparativa entre negocios de Reportes). */
data class ResumenNegocio(
    val negocioId: Long,
    val nombre: String,
    val ingresosCentavos: Long,
    val gastosCentavos: Long,
) {
    val balanceCentavos: Long get() = ingresosCentavos - gastosCentavos
}

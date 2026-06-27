package com.tallerapp.domain.model

/** Conteo de un servicio para el reporte "Servicios más realizados" (Frozen Spec 12.3). */
data class ConteoServicio(
    val servicio: ServicioRealizado,
    val cantidad: Int,
)

/**
 * Reporte mensual (mes calendario, RN-7). Montos en centavos.
 * - trabajosRealizados: trabajos que llegaron a Entregado en el mes (RN-9).
 * - vehiculosAtendidos: trabajos con fecha de ingreso en el mes (RN-9).
 */
data class ReporteMensual(
    val ingresosCentavos: Long = 0,
    val gastosCentavos: Long = 0,
    val trabajosRealizados: Int = 0,
    val vehiculosAtendidos: Int = 0,
    val serviciosMasRealizados: List<ConteoServicio> = emptyList(),
) {
    val gananciaCentavos: Long get() = ingresosCentavos - gastosCentavos
}

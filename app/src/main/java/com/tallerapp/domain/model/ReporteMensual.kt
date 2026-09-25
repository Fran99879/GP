package com.tallerapp.domain.model

/** Total gastado en una categoría dentro de un período (para el gráfico de gastos). */
data class GastoPorCategoria(
    val categoria: String,
    val montoCentavos: Long,
)

/**
 * Reporte de un mes calendario. Montos en centavos.
 * [gastosPorCategoria] alimenta el gráfico de torta de gastos.
 */
data class ReporteMensual(
    val ingresosCentavos: Long = 0,
    val gastosCentavos: Long = 0,
    val gastosPorCategoria: List<GastoPorCategoria> = emptyList(),
) {
    val gananciaCentavos: Long get() = ingresosCentavos - gastosCentavos
}

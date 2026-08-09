package com.tallerapp.core.export

import com.tallerapp.core.util.Dinero
import com.tallerapp.domain.model.ReporteMensual

/** Una sección del reporte exportable: título y filas etiqueta/valor. */
data class SeccionReporte(val titulo: String, val filas: List<Pair<String, String>>)

/** Reporte listo para exportar: un encabezado y varias secciones. */
data class ReporteExportable(val titulo: String, val secciones: List<SeccionReporte>)

/**
 * Construye el contenido del reporte mensual para exportar. PDF y CSV consumen esta
 * misma estructura, garantizando que el archivo coincida con lo mostrado en pantalla.
 */
object ReporteContenido {

    fun construir(mensual: ReporteMensual, etiquetaMes: String): ReporteExportable {
        val resumen = SeccionReporte(
            titulo = "Resumen",
            filas = listOf(
                "Ingresos" to Dinero.formatear(mensual.ingresosCentavos),
                "Gastos" to Dinero.formatear(mensual.gastosCentavos),
                "Balance" to Dinero.formatear(mensual.gananciaCentavos),
            ),
        )

        val gastos = SeccionReporte(
            titulo = "Gastos por categoría",
            filas = if (mensual.gastosPorCategoria.isEmpty()) {
                listOf("Sin gastos este mes" to "")
            } else {
                mensual.gastosPorCategoria.map {
                    it.categoria.etiqueta to Dinero.formatear(it.montoCentavos)
                }
            },
        )

        return ReporteExportable(
            titulo = "Reporte de $etiquetaMes",
            secciones = listOf(resumen, gastos),
        )
    }
}

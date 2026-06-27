package com.tallerapp.core.export

import com.tallerapp.core.util.Dinero
import com.tallerapp.domain.model.ReporteMensual
import com.tallerapp.domain.model.ResumenDelDia

/** Una sección del reporte exportable: título y filas etiqueta/valor. */
data class SeccionReporte(val titulo: String, val filas: List<Pair<String, String>>)

/**
 * Construye el contenido del reporte para exportar. PDF y CSV consumen esta misma
 * estructura, garantizando que el archivo coincida con lo mostrado en pantalla (CA-12).
 */
object ReporteContenido {

    fun construir(diario: ResumenDelDia, mensual: ReporteMensual): List<SeccionReporte> {
        val dia = SeccionReporte(
            titulo = "Reporte del día",
            filas = listOf(
                "Caja del día" to Dinero.formatear(diario.ingresosCentavos),
                "Ingresos del día" to Dinero.formatear(diario.ingresosCentavos),
                "Gastos del día" to Dinero.formatear(diario.gastosCentavos),
                "Ganancia del día" to Dinero.formatear(diario.gananciaCentavos),
            ),
        )

        val mes = SeccionReporte(
            titulo = "Reporte del mes",
            filas = listOf(
                "Ingresos del mes" to Dinero.formatear(mensual.ingresosCentavos),
                "Gastos del mes" to Dinero.formatear(mensual.gastosCentavos),
                "Ganancia del mes" to Dinero.formatear(mensual.gananciaCentavos),
                "Trabajos realizados" to mensual.trabajosRealizados.toString(),
                "Vehículos atendidos" to mensual.vehiculosAtendidos.toString(),
            ),
        )

        val servicios = SeccionReporte(
            titulo = "Servicios más realizados",
            filas = if (mensual.serviciosMasRealizados.isEmpty()) {
                listOf("Sin datos este mes" to "")
            } else {
                mensual.serviciosMasRealizados.map { it.servicio.etiqueta to it.cantidad.toString() }
            },
        )

        return listOf(dia, mes, servicios)
    }
}

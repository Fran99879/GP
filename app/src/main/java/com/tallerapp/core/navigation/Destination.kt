package com.tallerapp.core.navigation

/**
 * Catálogo único de rutas de navegación (Arquitectura 5.2).
 * Mantener todas las rutas aquí evita strings dispersos y rutas inconsistentes.
 */
object Destination {
    const val DASHBOARD = "dashboard"

    const val TRABAJOS = "trabajos"
    const val NUEVO_TRABAJO = "trabajos/nuevo"
    const val TRABAJO_DETALLE = "trabajos/detalle/{trabajoId}"
    const val TRABAJO_EDITAR = "trabajos/editar/{trabajoId}"
    const val ARG_TRABAJO_ID = "trabajoId"

    const val FINANZAS = "finanzas"
    const val NUEVO_INGRESO = "finanzas/ingreso/nuevo"
    const val NUEVO_GASTO = "finanzas/egreso/nuevo"

    const val REPORTES = "reportes"

    /** Ruta concreta del detalle de un trabajo. */
    fun trabajoDetalle(trabajoId: Long): String = "trabajos/detalle/$trabajoId"

    /** Ruta concreta de edición de un trabajo. */
    fun trabajoEditar(trabajoId: Long): String = "trabajos/editar/$trabajoId"
}

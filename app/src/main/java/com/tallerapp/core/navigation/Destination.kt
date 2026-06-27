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
    const val TRABAJO_COBRO = "trabajos/cobro/{trabajoId}"
    const val ARG_TRABAJO_ID = "trabajoId"

    const val FINANZAS = "finanzas"
    const val NUEVO_INGRESO = "finanzas/ingreso/nuevo"
    const val NUEVO_GASTO = "finanzas/egreso/nuevo"
    const val INGRESO_EDITAR = "finanzas/ingreso/editar/{movimientoId}"
    const val EGRESO_EDITAR = "finanzas/egreso/editar/{movimientoId}"
    const val ARG_MOVIMIENTO_ID = "movimientoId"

    const val REPORTES = "reportes"

    /** Ruta concreta del detalle de un trabajo. */
    fun trabajoDetalle(trabajoId: Long): String = "trabajos/detalle/$trabajoId"

    /** Ruta concreta de edición de un trabajo. */
    fun trabajoEditar(trabajoId: Long): String = "trabajos/editar/$trabajoId"

    /** Ruta concreta de cobro de un trabajo. */
    fun trabajoCobro(trabajoId: Long): String = "trabajos/cobro/$trabajoId"

    /** Rutas concretas de edición de movimientos de finanzas. */
    fun ingresoEditar(id: Long): String = "finanzas/ingreso/editar/$id"
    fun egresoEditar(id: Long): String = "finanzas/egreso/editar/$id"
}

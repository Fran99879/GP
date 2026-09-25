package com.tallerapp.core.navigation

/**
 * Catálogo único de rutas de navegación. Mantener todas las rutas aquí evita
 * strings dispersos y rutas inconsistentes.
 */
object Destination {
    const val DASHBOARD = "dashboard"

    const val FINANZAS = "finanzas"
    const val NUEVO_INGRESO = "finanzas/ingreso/nuevo"
    const val NUEVO_GASTO = "finanzas/egreso/nuevo"
    const val INGRESO_EDITAR = "finanzas/ingreso/editar/{movimientoId}"
    const val EGRESO_EDITAR = "finanzas/egreso/editar/{movimientoId}"
    const val ARG_MOVIMIENTO_ID = "movimientoId"

    const val DEUDAS = "deudas"
    const val NUEVA_DEUDA = "deudas/nueva"
    const val DEUDA_EDITAR = "deudas/editar/{deudaId}"
    const val ARG_DEUDA_ID = "deudaId"

    const val REPORTES = "reportes"

    const val AJUSTES = "ajustes"
    const val CATEGORIAS = "categorias"
    const val CUENTAS = "cuentas"
    const val METAS = "metas"
    const val RECURRENTES = "recurrentes"
    const val NEGOCIOS = "negocios"
    const val CALCULADORA = "calculadora"
    const val REMITO = "remito"
    const val AGENDA = "agenda"
    const val PERFIL = "perfil"

    fun ingresoEditar(id: Long): String = "finanzas/ingreso/editar/$id"
    fun egresoEditar(id: Long): String = "finanzas/egreso/editar/$id"
    fun deudaEditar(id: Long): String = "deudas/editar/$id"
}

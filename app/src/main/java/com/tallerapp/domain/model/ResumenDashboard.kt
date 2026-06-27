package com.tallerapp.domain.model

/** Indicadores del Dashboard (Frozen Spec 12.1). Montos en centavos. */
data class ResumenDashboard(
    val cajaDelDiaCentavos: Long = 0,
    val gananciaDelMesCentavos: Long = 0,
    val vehiculosEnTaller: Int = 0,
    val esperandoRepuestos: Int = 0,
    val pendientes: Int = 0,
    val entregadosHoy: Int = 0,
)

package com.tallerapp.data.local

/** Total acumulado de un negocio en un período (para la comparativa entre negocios). */
data class TotalPorNegocio(
    val negocioId: Long,
    val total: Long,
)
